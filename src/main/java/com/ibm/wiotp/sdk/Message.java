/**
 *****************************************************************************
 Copyright (c) 2015-19 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 *
 */
package com.ibm.wiotp.sdk;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ibm.wiotp.sdk.app.ApplicationClient;
import com.ibm.wiotp.sdk.util.LoggerUtility;


/**
 * This class encapsulates the Message and is inherited by Event and Command <br>
 * This class may be made abstract later on
 *
 */
public class Message {
	
	private static final String CLASS_NAME = ApplicationClient.class.getName();

	protected final static JsonParser JSON_PARSER = new JsonParser();
	
	private String format;
	private JsonObject data;
	private MqttMessage msg;
	
	/**
	 * 
	 * @param msg
	 * 				MqttMessage
	 * @throws UnsupportedEncodingException If encoding is other than UTF8
	 */
	public Message(MqttMessage msg, String format) throws UnsupportedEncodingException{
		final String METHOD = "Message";
		
		this.format = format;
		this.msg = msg;

		if (! format.equals("json")) {
			throw new RuntimeException("Sorry, Java SDK is only able to support JSON encoded events and commands currently: " + format);
		}
		
		
		final String payloadInString = new String(msg.getPayload(), "UTF8");
		try {
			data = JSON_PARSER.parse(payloadInString).getAsJsonObject();
		} catch (JsonSyntaxException e) {
			LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, "JsonSyntaxException thrown", e);
		} catch (JsonParseException jpe) {
			LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, "JsonParseException thrown", jpe);							
		}
	}
	
	public byte[] getPayload() {
		return msg.getPayload();
	}

	public JsonObject getData() {
		return data;
	}
	
	public String getFormat() {
		return format;
	}
	
	/**
	 * 
	 * Provides a human readable String representation of message, including timestamp and data.
	 */
	public String toString() {
		return data.toString(); 
	}

}
