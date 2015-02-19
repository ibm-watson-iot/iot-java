package com.ibm.iotf.client.app;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.ibm.iotf.client.Message;


/**
 * This class inherits from Message and denotes the device event <br>
 * This class is immutable and may be made final later on
 *
 */
public class Event extends Message{

	private String type, id, event, format;

	
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
	 * @param msg
	 * @throws UnsupportedEncodingException
	 */
	public Event(String type, String id, String event, String format, MqttMessage msg) throws UnsupportedEncodingException{
		super(msg, format);
		this.type = type;
		this.id = id;
		this.event = event;
		this.format = format;
	}
	
	public String getDeviceType() {
		return type;
	}

	public String getDeviceId() {
		return id;
	}
	
	public String getEvent() {
		return event;
	}
	
	public String getFormat() {
		return format;
	}
	
	
	/**
	 * Provides a human readable String representing this event and contains timestamp, type, id and payload
	 */
	public String toString() {
		if(format.equalsIgnoreCase("json")) {
			return "Event [" + timestamp.toString() + "] " + type + ":" + id + " - " + event + ": " + data.toString();			
		} else {
			return "Event [" + timestamp.toString() + "] " + type + ":" + id + " - " + event + ": " + payload.toString();			
		}
 
	}

}
