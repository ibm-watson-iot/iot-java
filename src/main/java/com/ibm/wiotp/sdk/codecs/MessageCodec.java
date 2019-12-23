package com.ibm.wiotp.sdk.codecs;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;

import com.ibm.wiotp.sdk.MessageInterface;
import com.ibm.wiotp.sdk.exceptions.MalformedMessageException;

public interface MessageCodec<T> {

	/**
	 * Convert an Object into a byte array suitable to send via MQTT
	 * 
	 * @param data      the Object to be encoded
	 * @param timestamp the time that the message was generated
	 * @return Byte array ready for MQTT message payload
	 */
	public byte[] encode(T data, DateTime timestamp);

	/**
	 * Convert an MQTT message into an instance of com.ibm.wiotp.sdk.Message
	 * 
	 * @param msg the MqttMessage from Paho MQTT client to decode
	 * @return Object of class T from the decoded MQTT message
	 * @throws MalformedMessageException If unable to decode the message
	 */
	public MessageInterface<T> decode(MqttMessage msg) throws MalformedMessageException;

	public Class<T> getMessageClass();

	public String getMessageFormat();

}
