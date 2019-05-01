/**
 *****************************************************************************
 * Copyright (c) 2016-19 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 */
package com.ibm.wiotp.sdk.gateway;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * The Notification class  
 */
public class Notification {

	private String type;
	private String id;
	private MqttMessage message;
	
	/**
	 * Note that this class does not have a default constructor
	 * @param type
	 * 			Object of String which denotes command type
	 * @param id
	 * 			Object of String which denotes command id
	 * @param msg
	 * 			Object of MqttMessage which denotes actual message
	 * @throws UnsupportedEncodingException When the format is not UTF-8 
	 * 
	 */	
	public Notification(String type, String id, MqttMessage msg) throws UnsupportedEncodingException{
		this.type = type;
		this.id = id;
		this.message = msg;
	}
	
	public String getDeviceType() {
		return type;
	}

	public String getDeviceId() {
		return id;
	}

	public MqttMessage getMessage() {
		return message;
	}
	
	
}
