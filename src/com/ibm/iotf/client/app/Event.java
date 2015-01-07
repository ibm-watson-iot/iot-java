package com.ibm.iotf.client.app;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.ibm.iotf.client.Message;

public class Event extends Message{

	private String type, id, event, format;
	
	public Event(String type, String id, String event, String format, MqttMessage msg) throws UnsupportedEncodingException{
		super(msg);
		this.type = type;
		this.id = id;
		this.event = event;
		this.format = format;
	}
	
	public String getDeviceType() {
		return type;
	}

	public String getDeviceId() {
		return id;
	}
	
	public String getEvent() {
		return event;
	}
	
	public String getFormat() {
		return format;
	}
	
	public String toString() {
		return "Event [" + timestamp.toString() + "] " + type + ":" + id + " - " + event + ": " + data.toString(); 
	}

}
