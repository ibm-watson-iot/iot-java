package com.ibm.iotf.client;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Message {

	protected final static JsonParser JSON_PARSER = new JsonParser();
	protected final static DateTimeFormatter DT_PARSER = ISODateTimeFormat.dateTimeParser();
	
	protected String payload;
	protected JsonObject data = null;
	protected DateTime timestamp = null;
	
	public Message(MqttMessage msg) throws UnsupportedEncodingException{
		this.payload = new String(msg.getPayload(), "UTF8");
		
		JsonObject payloadJson = JSON_PARSER.parse(payload).getAsJsonObject();
		if (payloadJson.has("d")) {
			data = payloadJson.get("d").getAsJsonObject();
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
	}
	
	public String getPayload() {
		return payload;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	public String toString() {
		return "[" + timestamp.toString() + "] " + data.toString(); 
	}

}
