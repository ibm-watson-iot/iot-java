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
import org.joda.time.DateTime;

import com.ibm.wiotp.sdk.codecs.EventInterface;
import com.ibm.wiotp.sdk.codecs.MessageInterface;

//messagehannlder(Event<T>)
//sn
//
//
//appclient.subscribe("+", "+", "+")
//appclient.registerMessageFormat(new JsonCodec(), myCallback)
//appclient.registerMessageFormat("utf8", new UTF8Codec(), myCallback)
//
//
//myCallback(Event<JsonObject> evt) {
//	JsonObject o = evt.getData()
//}
//
//myCallback(Event<String> evt) {
//	String o = evt.getData()
//}
//
//myCallback(Event<T> evt) {
//	if evt instanceof(jsonovbject)
//	JsonObject o = evt.getData()
//}



/**
 * This class inherits from Message and denotes the device event <br>
 * This class is immutable and may be made final later on
 *
 */
public class Event<T> implements EventInterface<T> {

	private String typeId;
	private String deviceId;
	private String eventId;
	private String format;
	private MessageInterface<T> message;
	
	/**
	 * 
	 * @param typeId
	 * 			object of String which denotes event type
	 * @param deviceId
	 * 			object of String which denotes the event id
	 * @param eventId
	 * 			object of String which denotes the event 
	 * @param format 
	 * 			Format (encoding) of the event
	 * @param msg 
	 * 			The MQTT message
	 * @throws UnsupportedEncodingException when the encoding is not UTF-8
	 */
	public Event(String typeId, String deviceId, String eventId, String format, MessageInterface<T> message) throws UnsupportedEncodingException{
		this.typeId = typeId;
		this.deviceId = deviceId;
		this.eventId = eventId;
		this.message = message;
		this.format = format;
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

	public String getFormat() {
		return format;
	}
	
	@Override
	public T getData() {
		return message.getData();
	}

	@Override
	public DateTime getTimestamp() {
		return message.getTimestamp();
	}

}
