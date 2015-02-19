package com.ibm.iotf.client.app;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * This class captures the status of the device
 *
 */

public class DeviceStatus extends Status{

	public String type, id;
	
	/**
	 * 
	 * @param type
	 * 			String of device type
	 * @param id
	 * 			String of device id
	 * @param msg
	 * @throws UnsupportedEncodingException
	 */
	public DeviceStatus(String type, String id, MqttMessage msg) throws UnsupportedEncodingException {
		super(msg);
		this.type = type;
		this.id = id;
	}

}
