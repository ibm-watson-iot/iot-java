package com.ibm.iotf.client.device;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.ibm.iotf.client.Message;



public class Command extends Message{

	private String command, format;

	/**
	 * Note that this class does not have a default constructor <br>

	 * This class has only accessors and no mutators and later on might be made final <br>
	 * @param command
	 * 			Actual command in the form of String passed
	 * @param format
	 * 			Format is a String which can contain values such as "json"
	 * @param msg
	 * 			MqttMessage 
	 * @see <a href="Paho Client Library">http://www.eclipse.org/paho/files/javadoc/index.html</a> 
	 * @throws
	 * 			UnsupportedEncodingException
	 * 
	 */	
	
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

	/**
	 * 
	 * Provides a human readable String representation of this Command, including the timestamp and the actual command passed.
	 */
	public String toString() {
		return "[" + timestamp.toString() + "] " + command + ": " + data.toString(); 
	}

}
