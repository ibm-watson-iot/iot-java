/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt.client.listener;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.client.device.DeviceDiagnostic;
import com.ibm.iotf.devicemgmt.client.device.ManagedClient;
import com.ibm.iotf.devicemgmt.client.device.DeviceTopic;
import com.ibm.iotf.util.LoggerUtility;

public abstract class DMNotifier {
	
	private static final String CLASS_NAME = DMNotifier.class.getName();
	
	protected DeviceTopic notifyTopic = null;

	private ManagedClient dmClient;
	
	public abstract void handleEvent(PropertyChangeEvent event);
	
	public void clearEvent(PropertyChangeEvent event) {
		
	}
	
	public DMNotifier(ManagedClient dmClient) {
		this.dmClient = dmClient;
	}
	
	protected ManagedClient getDMClient() {
		return this.dmClient;
	}
	
	protected DeviceTopic getNotifyTopic() {
		return notifyTopic;
	}
	
	protected void setNotifyTopic(DeviceTopic topic) {
		notifyTopic = topic;
	}
	
	protected void notify(JsonObject payload) throws MqttException {
		notify(getNotifyTopic(), payload);
	}
	
	protected void notify(DeviceTopic topic, JsonObject payload) throws MqttException {
		final String METHOD = "notify(topic + payload)";
		
		String uuid = UUID.randomUUID().toString();
		payload.add("reqId", new JsonPrimitive(uuid));
		
		LoggerUtility.log(Level.FINER, CLASS_NAME, METHOD, "Topic(" + getNotifyTopic() + 
				") payload("+ payload.toString() + ")");
		dmClient.publish(topic, payload, 1);
	}
	
	protected JsonObject notify(JsonObject payload, long timeout) throws MqttException {
		return notify(getNotifyTopic(), payload, timeout);
	}
	

	protected JsonObject notify(DeviceTopic topic, JsonObject payload,
			long timeout) throws MqttException {
		final String METHOD = "notify with timeout (1) " + timeout + "ms ";
		LoggerUtility.log(Level.FINER, CLASS_NAME, METHOD, "Topic(" + getNotifyTopic() + 
				") payload("+ payload.toString() + ")");
		return dmClient.sendAndWait(topic, payload, timeout);
	}
}
