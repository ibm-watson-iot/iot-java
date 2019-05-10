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
package com.ibm.wiotp.sdk.app.messages;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * This is an abstract class which is inherited by application status and device status
 *
 */
public abstract class Status {
	protected final static JsonParser JSON_PARSER = new JsonParser();
	protected final static DateTimeFormatter DT_PARSER = ISODateTimeFormat.dateTimeParser();

	// Properties from the "Connect" status are common in "Disconnect" status too
	private String clientAddr;
	private String protocol;
	private String clientId;
	private String user;
	private String action;
	private String connectTime;
	private int port = 0;
	private DateTime time;
	
	// Additional "Disconnect" status properties
	private int writeMsg = 0;
	private int readMsg = 0;
	private int readBytes = 0;
	private int writeBytes = 0;
	private String reason;
	
	private String payload;
	
	
	public String getClientAddr() {
		return clientAddr;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getClientId() {
		return clientId;
	}

	public String getUser() {
		return user;
	}

	public String getAction() {
		return action;
	}

	public String getConnectTime() {
		return connectTime;
	}

	public int getPort() {
		return port;
	}

	public DateTime getTime() {
		return time;
	}

	public int getWriteMsg() {
		return writeMsg;
	}

	public int getReadMsg() {
		return readMsg;
	}

	public int getReadBytes() {
		return readBytes;
	}

	public int getWriteBytes() {
		return writeBytes;
	}

	public String getReason() {
		return reason;
	}

	/**
	 * This class does not have a default constructor and has a single argument constructor
	 * @param msg The MQTT message
	 * @throws UnsupportedEncodingException Failure when the Format is not UTF-8
	 */
	public Status(MqttMessage msg) throws UnsupportedEncodingException{
		this.payload = new String(msg.getPayload(), "UTF8");
		
		JsonObject payloadJson = JSON_PARSER.parse(payload).getAsJsonObject();
		
		// Common attributes
		if (payloadJson.has("ClientAddr"))
			clientAddr = payloadJson.get("ClientAddr").getAsString();
		else
			clientAddr = new String();
		if (payloadJson.has("Protocol"))
			protocol = payloadJson.get("Protocol").getAsString();
		else
			protocol = new String();
		if (payloadJson.has("ClientID"))
			clientId = payloadJson.get("ClientID").getAsString();
		else
			clientId = new String();
		if (payloadJson.has("Time"))
			time = DT_PARSER.parseDateTime(payloadJson.get("Time").getAsString());
		else
			time = new DateTime();
		if (payloadJson.has("Action"))
			action = payloadJson.get("Action").getAsString();
		else
			action = new String();
		if (payloadJson.has("Port"))
			port = payloadJson.get("Port").getAsInt();
		
		if (action.equals("Disconnect")) {
			if (payloadJson.has("User"))
				user = payloadJson.get("User").getAsString();
			else
				user = new String();
			if (payloadJson.has("ConnectTime"))
				connectTime = payloadJson.get("ConnectTime").getAsString();
			else
				connectTime = new String();
			if (payloadJson.has("WriteMsg"))
				writeMsg = payloadJson.get("WriteMsg").getAsInt();
			if (payloadJson.has("ReadMsg"))
				readMsg = payloadJson.get("ReadMsg").getAsInt();
			if (payloadJson.has("ReadBytes"))
				readBytes = payloadJson.get("ReadBytes").getAsInt();
			if (payloadJson.has("WriteBytes"))
				writeBytes = payloadJson.get("WriteBytes").getAsInt();
			if (payloadJson.has("Reason"))
				reason = payloadJson.get("Reason").getAsString();
			else
				reason = new String();
		}
		
	}

	public String getPayload() {
		return payload;
	}

	/**
	 * 
	 * Provides a human readable String representation of status, including timestamp, client id, action and (possibly) reason.
	 */		
	public String toString() {
		if (action.equals("Disconnect")) {
			return "Status [" + time.toString() + "] " + clientId + ":" + action + " (" + reason + ")";
		}
		else {
			return "Status [" + time.toString() + "] " + clientId + ":" + action;
		}
	}

}
