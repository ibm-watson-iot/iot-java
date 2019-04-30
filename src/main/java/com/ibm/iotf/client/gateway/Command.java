/**
 *****************************************************************************
 * Copyright (c) 2016-19 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 */
package com.ibm.iotf.client.gateway;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.ibm.iotf.client.Message;

/**
 * The Command class  
 */
public class Command extends Message {

	private String type;
	private String id;
	private String command;
	private String format;
	
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
	 * @see <a href="http://www.eclipse.org/paho/files/javadoc/index.html">Paho Client Library</a> 
	 * @throws UnsupportedEncodingException when the format is not UTF-8
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
	 * Returns the actual MQTT payload sent by the application
	 * 
	 * @return returns the command in either JSON, byte[] or String type based on the format specified.
	 */
	public Object getData() {
		return this.payload;
	}
	
	/**
	 * 
	 * Provides a human readable String representation of this Command, including the timestamp, command type, command id and payload passed.
	 */
	public String toString() {
		return "Command [" + timestamp.toString() + "] " + type + ":" + id + " - " + command + ": " + this.getPayload();
	}
}
