package com.ibm.iotf.client.app;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.ibm.iotf.client.Message;

public class Command extends Message{

	private String type, id, command, format;
	
	public Command(String type, String id, String command, String format, MqttMessage msg) throws UnsupportedEncodingException{
		super(msg);
		this.type = type;
		this.id = id;
		this.command = command;
		this.format = format;
	}
	
	public String getDeviceType() {
		return type;
	}

	public String getDeviceId() {
		return id;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getFormat() {
		return format;
	}
	
	public String toString() {
		return "[" + timestamp.toString() + "] " + type + ":" + id + " - " + command + ": " + data.toString(); 
	}

}
