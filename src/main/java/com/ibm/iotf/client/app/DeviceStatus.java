package com.ibm.iotf.client.app;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * This class captures the status of the device
 *
 */

public class DeviceStatus extends Status {

	public String getDeviceType() {
		return deviceType;
	}

	public String getDeviceId() {
		return deviceId;
	}

	private String deviceType;
	private String deviceId;
	
	/**
	 * 
	 * @param typeId
	 * 			String of device type
	 * @param deviceId
	 * 			String of device id
	 * @param msg
	 * @throws UnsupportedEncodingException
	 */
	public DeviceStatus(String typeId, String deviceId, MqttMessage msg) throws UnsupportedEncodingException {
		super(msg);
		this.deviceType = typeId;
		this.deviceId = deviceId;
	}

}
