package com.ibm.iotcloud.client.app;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ApplicationStatus extends Status{

	public String id;
	public ApplicationStatus(String id, MqttMessage msg) throws UnsupportedEncodingException {
		super(msg);
		this.id = id;
	}

}
