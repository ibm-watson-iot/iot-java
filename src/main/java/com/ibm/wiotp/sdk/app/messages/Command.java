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

import com.ibm.wiotp.sdk.Message;


public class Command extends Message {

	private String typeId;
	private String deviceId;
	private String commandId;
	
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
	 * @throws UnsupportedEncodingException When the encoding format id not UTF-8 
	 * 
	 */	
	public Command(String typeId, String deviceId, String commandId, String format, MqttMessage msg) throws UnsupportedEncodingException{
		super(msg, format);
		this.typeId = typeId;
		this.deviceId = deviceId;
		this.commandId = commandId;
	}
	
	/**
	 * Returns the device type
	 * @return Returns the device type
	 */
	public String getTypeId() {
		return typeId;
	}

	/**
	 * Returns the device Id
	 * @return Returns the device Id
	 */
	public String getDeviceId() {
		return deviceId;
	}
	
	/**
	 * Returns the name of the command
	 * @return Returns the name of the command
	 */
	public String getCommandId() {
		return commandId;
	}
}
