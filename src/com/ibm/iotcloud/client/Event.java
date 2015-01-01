package com.ibm.iotcloud.client;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Event {

	private String payload;
	private String topic;
	
	public Event(String topic, MqttMessage msg) {
		this.topic = topic;
		this.payload = msg.getPayload().toString();
	}
	
	public String getPayload() {
		return payload;
	}
	
	public String getTopic() {
		return topic;
	}
}
