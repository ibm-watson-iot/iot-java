package com.ibm.iotf.client.app;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class DeviceStatus extends Status{

	public String type, id;
	public DeviceStatus(String type, String id, MqttMessage msg) throws UnsupportedEncodingException {
		super(msg);
		this.type = type;
		this.id = id;
	}

}
