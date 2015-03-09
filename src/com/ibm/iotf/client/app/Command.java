package com.ibm.iotf.client.app;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.ibm.iotf.client.Message;


public class Command extends Message{

	private String type, id, command, format;
	
	/**
	 * Note that this class does not have a default constructor
	 * @param type
	 * 			Object of String which denotes command type
	 * @param id
	 * 			Object of String which denotes command id
	 * @param command
	 * 			Object of String which denotes actual command type
	 * @param format
	 * 			Object of String which denotes command format, say json
	 * @param msg
	 * 			MqttMessage
	 * @see <a href="Paho Client Library">http://www.eclipse.org/paho/files/javadoc/index.html</a> 
	 * @throws UnsupportedEncodingException 
	 * 
	 */	
	public Command(String type, String id, String command, String format, MqttMessage msg) throws UnsupportedEncodingException{
		super(msg, format);
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
	
	/**
	 * 
	 * Provides a human readable String representation of this Command, including the timestamp, command type, command id and payload passed.
	 */
	public String toString() {
		if(format.equalsIgnoreCase("json")) {
			return "Command [" + timestamp.toString() + "] " + type + ":" + id + " - " + command + ": " + data.toString();
			
			//This else condition has been added to handle the commands which do not have json format
		} else {
			return "Command [" + timestamp.toString() + "] " + type + ":" + id + " - " + command + ": " + payload.toString();			
		}
 
	}
}
