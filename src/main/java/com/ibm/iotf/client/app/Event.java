/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.app;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.ibm.iotf.client.Message;


/**
 * This class inherits from Message and denotes the device event <br>
 * This class is immutable and may be made final later on
 *
 */
public class Event extends Message {

	private String deviceType;
	private String deviceId;
	private String event;
	private String format;
	
	/**
	 * 
	 * @param type
	 * 			object of String which denotes event type
	 * @param id
	 * 			object of String which denotes the event id
	 * @param event
	 * 			object of String which denotes the event 
	 * @param format
	 * 			object of String which denotes the format, such as json
	 * @param msg The MQTT message
	 * @throws UnsupportedEncodingException when the encoding is not UTF-8
	 */
	public Event(String type, String id, String event, String format, MqttMessage msg) throws UnsupportedEncodingException{
		super(msg, format);
		this.deviceType = type;
		this.deviceId = id;
		this.event = event;
		this.format = format;
	}
	
	/**
	 * Returns the device type
	 * @return Returns the device type
	 */
	public String getDeviceType() {
		return deviceType;
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
	public String getEvent() {
		return event;
	}
	
	/**
	 * Returns the format of the event
	 * @return Returns the format of the event
	 */
	public String getFormat() {
		return format;
	}
	
	/**
	 * This method is deprecated. Instead use {@link #getData()} or {@link #getRawPayload()}
	 * @return Returns the payload in string format
	 */
	@Deprecated
	public String getPayload() {
		return super.getPayload();
	}
	
	/**
	 * Returns the actual MQTT payload sent by the application
	 * 
	 * @return returns the Event in either JSON, byte[] or String type based on the format specified.
	 */
	public Object getData() {
		return this.payload;
	}
	
	/**
	 * Provides a human readable String representing this event and contains timestamp, type, id and payload
	 */
	public String toString() {
		return "Event [" + timestamp.toString() + "] " + deviceType + ":" + deviceId + " - " + event + ": " + this.getPayload();			
	}

}
