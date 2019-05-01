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


/**
 * This class inherits from Message and denotes the device event <br>
 * This class is immutable and may be made final later on
 *
 */
public class Event extends Message {

	private String typeId;
	private String deviceId;
	private String eventId;
	
	/**
	 * 
	 * @param type
	 * 			object of String which denotes event type
	 * @param id
	 * 			object of String which denotes the event id
	 * @param event
	 * 			object of String which denotes the event 
	 * @param msg The MQTT message
	 * @throws UnsupportedEncodingException when the encoding is not UTF-8
	 */
	public Event(String typeId, String deviceId, String eventId, String format, MqttMessage msg) throws UnsupportedEncodingException{
		super(msg, format);
		this.typeId = typeId;
		this.deviceId = deviceId;
		this.eventId = eventId;
	}
	
	/**
	 * Returns the device type
	 * @return Returns the device type
	 */
	public String getTypeId() {
		return typeId;
	}

	/**
	 * Returns the device id
	 * @return Returns the device id
	 */
	public String getDeviceId() {
		return deviceId;
	}
	
	/**
	 * Returns the name of the event
	 * @return Returns the name of the event
	 */
	public String getEventId() {
		return eventId;
	}

}
