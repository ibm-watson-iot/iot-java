/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.util.LoggerUtility;


/**
 * This class encapsulates the Message and is inherited by Event and Command <br>
 * This class may be made abstract later on
 *
 */
public class Message {
	
	private static final String CLASS_NAME = ApplicationClient.class.getName();

	protected final static JsonParser JSON_PARSER = new JsonParser();
	protected final static DateTimeFormatter DT_PARSER = ISODateTimeFormat.dateTimeParser();
	
	protected String payload;
	protected String data = null;
	protected DateTime timestamp = null;
	
	/**
	 * 
	 * @param msg
	 * 				MqttMessage
	 * @throws UnsupportedEncodingException
	 */
	public Message(MqttMessage msg) throws UnsupportedEncodingException{
		final String METHOD = "Message(1)";
		this.payload = new String(msg.getPayload(), "UTF8");
		
		try {
			JsonObject payloadJson = JSON_PARSER.parse(payload).getAsJsonObject();
			if (payloadJson.has("d")) {
				data = payloadJson.get("d").getAsJsonObject().toString();
	
			} else {
				data = payloadJson.toString();
			}
		
			if (payloadJson.has("ts")) {
				try {
					timestamp = DT_PARSER.parseDateTime(payloadJson.get("ts").getAsString());
				} catch (IllegalArgumentException e) {
					timestamp = DateTime.now();
				}
			} else {
				timestamp = DateTime.now();
			}
		} catch (JsonSyntaxException e) {
			LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, "JsonSyntaxException thrown", e);
		} catch (JsonParseException jpe) {
			LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, "JsonParseException thrown", jpe);							
		}
	}

	/**
	 * 
	 * @param msg
	 * 				MqttMessage
	 * @param format
	 * 				an object of String which comtains format such as json
	 * @throws UnsupportedEncodingException
	 */
	public Message(MqttMessage msg, String format) throws UnsupportedEncodingException{
		final String METHOD = "Message(2)";
		this.payload = new String(msg.getPayload(), "UTF8");
		
		if(format.equalsIgnoreCase("json")) {

			try {
				JsonObject payloadJson = JSON_PARSER.parse(payload).getAsJsonObject();
				if (payloadJson.has("d")) {
					data = payloadJson.get("d").getAsJsonObject().toString();
				} else {
					data = payloadJson.toString();
				}
				if (payloadJson.has("ts")) {
					try {
						timestamp = DT_PARSER.parseDateTime(payloadJson.get("ts").getAsString());
					} catch (IllegalArgumentException e) {
						timestamp = DateTime.now();
					}
				} else {
					timestamp = DateTime.now();
				}
			} catch (JsonSyntaxException e) {
				LoggerUtility.warn(CLASS_NAME, METHOD, "JsonSyntaxException thrown");
			} catch (JsonParseException jpe) {
				LoggerUtility.warn(CLASS_NAME, METHOD, "JsonParseException thrown");							
			}			
		} else {
			data = this.payload;
			timestamp = DateTime.now();			
		}
		
	}
	
/////	
	public String getPayload() {
		return payload;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * 
	 * Provides a human readable String representation of message, including timestamp and data.
	 */
	public String toString() {
		return "[" + timestamp.toString() + "] " + data.toString(); 
	}

}
