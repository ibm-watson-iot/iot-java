package com.ibm.iotcloud.client.app;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Status {
	protected final static JsonParser JSON_PARSER = new JsonParser();
	protected final static DateTimeFormatter DT_PARSER = ISODateTimeFormat.dateTimeParser();

	// Properties from the "Connect" status are common in "Disconnect" status too
	public String clientAddr, protocol, clientId, user, action, connectTime;
	public int port;
	public DateTime time;
	
	// Additional "Disconnect" status properties
	public int writeMsg = 0, readMsg = 0, readBytes = 0, writeBytes = 0;
	public String reason = "";
	
	public String payload;
	
	public Status(MqttMessage msg) throws UnsupportedEncodingException{
		this.payload = new String(msg.getPayload(), "UTF8");
		
		JsonObject payloadJson = JSON_PARSER.parse(payload).getAsJsonObject();
		
		// Common attributes
		clientAddr = payloadJson.get("ClientAddr").getAsString();
		protocol = payloadJson.get("Protocol").getAsString();
		clientId = payloadJson.get("ClientID").getAsString();
		user = payloadJson.get("User").getAsString();
		time = DT_PARSER.parseDateTime(payloadJson.get("Time").getAsString());
		action = payloadJson.get("Action").getAsString();
		connectTime = payloadJson.get("ConnectTime").getAsString();
		port = payloadJson.get("Port").getAsInt();
		
		if (action.equals("Disconnect")) {
			writeMsg = payloadJson.get("WriteMsg").getAsInt();
			readMsg = payloadJson.get("ReadMsg").getAsInt();
			readBytes = payloadJson.get("ReadBytes").getAsInt();
			writeBytes = payloadJson.get("WriteBytes").getAsInt();
			reason = payloadJson.get("Reason").getAsString();
		}
		
	}

	public String getPayload() {
		return payload;
	}

	
	public String toString() {
		if (action.equals("Disconnect")) {
			return "Status [" + time.toString() + "] " + clientId + ":" + action + " (" + reason + ")";
		}
		else {
			return "Status [" + time.toString() + "] " + clientId + ":" + action;
		}
	}

}
