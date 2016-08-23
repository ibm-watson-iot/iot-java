/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Extended from DeviceClient
 *****************************************************************************
 *
 */
package com.ibm.internal.iotf.devicemgmt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.DeviceActionHandler;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmwareHandler;

/**
 * A managed client interface that provides the contract what a managed device or Gateway
 * must implement, inorder to participate in DM activities. 
 *
 */
public interface ManagedClient {

	public void subscribe(String topic, int qos,
			IMqttMessageListener iMqttMessageListener) throws MqttException;

	public void unsubscribe(String topic) throws MqttException;

	public void publish(String response, JsonObject payload, int qos) throws MqttException;

	public DeviceData getDeviceData();

	public void subscribe(String[] topics, int[] qos,
			IMqttMessageListener[] listener) throws MqttException;

	public void unsubscribe(String[] topics) throws MqttException;

	public DMAgentTopic getDMAgentTopic();

	public DMServerTopic getDMServerTopic();

	public DeviceActionHandler getActionHandler();

	DeviceFirmwareHandler getFirmwareHandler();

}
