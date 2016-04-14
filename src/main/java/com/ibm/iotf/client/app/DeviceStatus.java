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
