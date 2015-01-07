package com.ibm.iotf.client.device;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.ibm.iotf.client.Message;

public class Command extends Message{

	private String command, format;
	
	public Command(String command, String format, MqttMessage msg) throws UnsupportedEncodingException{
		super(msg);
		this.command = command;
		this.format = format;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getFormat() {
		return format;
	}
	
	public String toString() {
		return "[" + timestamp.toString() + "] " + command + ": " + data.toString(); 
	}

}
