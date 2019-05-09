package com.ibm.wiotp.sdk.codecs;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;

import com.ibm.wiotp.sdk.exceptions.MalformedMessageException;



public interface MessageCodec<T> {

	/**
	 * Convert an Object into a byte array suitable to send via MQTT
	 *  
	 * @param data
	 * @param timestamp
	 * @return
	 */
	public byte[] encode(T data, DateTime timestamp);
	
	/**
	 * Convert an MQTT message into an instance of com.ibm.wiotp.sdk.Message
	 * 
	 * @param msg
	 * @return
	 * @throws MalformedMessageException 
	 */
	public MessageInterface<T> decode(MqttMessage msg) throws MalformedMessageException;
	
	public Class<T> getMessageClass();
	
	public String getMessageFormat();
	
}
