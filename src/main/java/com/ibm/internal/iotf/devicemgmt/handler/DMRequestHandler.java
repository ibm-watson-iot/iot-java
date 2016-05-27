/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.internal.iotf.devicemgmt.handler;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.devicemgmt.DeviceAction;
import com.ibm.internal.iotf.devicemgmt.ManagedClient;
import com.ibm.internal.iotf.devicemgmt.DMAgentTopic;
import com.ibm.iotf.util.LoggerUtility;


public abstract class DMRequestHandler implements IMqttMessageListener {
	
	protected static final String CLASS_NAME = DMRequestHandler.class.getName();
	
	private ManagedClient dmClient = null;
	
	private static Map<ManagedClient, DeviceUpdateRequestHandler> deviceUpdateHandlers = new HashMap<ManagedClient,DeviceUpdateRequestHandler>();
	private static Map<ManagedClient, ObserveRequestHandler> observeHandlers = new HashMap<ManagedClient,ObserveRequestHandler>();
	private static Map<ManagedClient, CancelRequestHandler> cancelHandlers = new HashMap<ManagedClient,CancelRequestHandler>();
	private static Map<ManagedClient, RebootRequestHandler> rebootHandlers = new HashMap<ManagedClient,RebootRequestHandler>();
	private static Map<ManagedClient, FactoryResetRequestHandler> resetHandlers = new HashMap<ManagedClient,FactoryResetRequestHandler>();
	private static Map<ManagedClient, FirmwareDownloadRequestHandler> fwDownloadHandlers = new HashMap<ManagedClient,FirmwareDownloadRequestHandler>();
	private static Map<ManagedClient, FirmwareUpdateRequestHandler> fwUpdateHandlers = new HashMap<ManagedClient,FirmwareUpdateRequestHandler>();
	
	protected abstract String getTopic();
	protected abstract void handleRequest(JsonObject jsonRequest);
	
	@Override
	public void messageArrived(String topic, MqttMessage message) {
		final String METHOD = "messageArrived";
		try {
			String payload = new String (message.getPayload(), "UTF-8");
			JsonObject jsonRequest = new JsonParser().parse(payload).getAsJsonObject();
			LoggerUtility.fine(CLASS_NAME, METHOD, System.identityHashCode(this) + "Handler(" + this.getClass().getName() + 
					") Received request on topic " + topic + ") " + jsonRequest.toString());
			
			handleRequest(jsonRequest);
		} catch (Exception e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Unexpected Exception = " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	protected ManagedClient getDMClient() {
		return dmClient;
	}
	
	protected void setDMClient(ManagedClient client) {
		dmClient = client;
	}
	
	protected ObserveRequestHandler getObserveRequestHandler(ManagedClient dmClient) {
		return observeHandlers.get(dmClient);
	}
	
	protected void respond(JsonObject payload) {
		final String METHOD = "respond";
		try {
			DMAgentTopic topic = dmClient.getDMAgentTopic();
			dmClient.publish(topic.getDMServerTopic(), payload, 1);
		} catch (MqttException e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Unexpected Mqtt Exception, code = " + e.getReasonCode());
		}
	}

	protected void notify(JsonObject payload) {
		final String METHOD = "notify";
		try {
			DMAgentTopic topic = dmClient.getDMAgentTopic();
			dmClient.publish(topic.getNotifyTopic(), payload, 1);
		} catch (MqttException e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Unexpected Mqtt Exception, code = " + e.getReasonCode());
		}
	}
	
	/**
	 * Create all the necessary request handlers - this is called when manage request is
	 * called by the agent
	 * 
	 * Do a bulk subscribe to improve the performance
	 * @param dmClient
	 * @throws MqttException
	 */
	public static void setRequestHandlers(ManagedClient dmClient) throws MqttException{
		
		String[] topics = new String[7];
		IMqttMessageListener[] listener = new IMqttMessageListener[7];
		int index = 0;
		
		DeviceUpdateRequestHandler device = deviceUpdateHandlers.get(dmClient);
		if (device == null) {
			device = new DeviceUpdateRequestHandler(dmClient);
			topics[index] = device.getTopic();
			listener[index++] = device;
			//device.subscribe();
			deviceUpdateHandlers.put(dmClient, device);
		}
		
		ObserveRequestHandler observe = observeHandlers.get(dmClient);
		if (observe == null) {
			observe = new ObserveRequestHandler(dmClient);
			topics[index] = observe.getTopic();
			listener[index++] = observe;
			observeHandlers.put(dmClient, observe);
		}
		
		CancelRequestHandler cancel = cancelHandlers.get(dmClient);
		if (cancel == null) {
			cancel = new CancelRequestHandler(dmClient);
			topics[index] = cancel.getTopic();
			listener[index++] = cancel;
			cancelHandlers.put(dmClient, cancel);
		}
		
		RebootRequestHandler reboot = rebootHandlers.get(dmClient);
		if (reboot == null) {
			reboot = new RebootRequestHandler(dmClient);
			topics[index] = reboot.getTopic();
			listener[index++] = reboot;
			rebootHandlers.put(dmClient, reboot);
		}
		
		FactoryResetRequestHandler reset = resetHandlers.get(dmClient);
		if (reset == null) {
			reset = new FactoryResetRequestHandler(dmClient);
			topics[index] = reset.getTopic();
			listener[index++] = reset;
			resetHandlers.put(dmClient, reset);
		}
		
		FirmwareDownloadRequestHandler fwDownload = fwDownloadHandlers.get(dmClient);
		if (fwDownload == null) {
			fwDownload = new FirmwareDownloadRequestHandler(dmClient);
			topics[index] = fwDownload.getTopic();
			listener[index++] = fwDownload;
			fwDownloadHandlers.put(dmClient, fwDownload);
		}
		
		FirmwareUpdateRequestHandler fwUpdate = fwUpdateHandlers.get(dmClient);
		if (fwUpdate == null) {
			fwUpdate = new FirmwareUpdateRequestHandler(dmClient);
			topics[index] = fwUpdate.getTopic();
			listener[index++] = fwUpdate;
			fwUpdateHandlers.put(dmClient, fwUpdate);
		}
		
		if(index > 0) {
			int[] qos = new int[index];
			Arrays.fill(qos, 1);
			dmClient.subscribe(topics, qos, listener);
		}
	}
	
	/**
	 * Clear all the request handlers - this is called when unmanage request is
	 * called by the agent
	 * 
	 * Do a bulk unsubscribe to improve performance
	 * @param dmClient
	 * @throws MqttException 
	 */
	public static void clearRequestHandlers(ManagedClient dmClient) throws MqttException {
		
		String[] topics = new String[7];
		IMqttMessageListener[] listener = new IMqttMessageListener[7];
		int index = 0;
		
		DeviceUpdateRequestHandler device = deviceUpdateHandlers.remove(dmClient);
		if (device != null) {
			topics[index] = device.getTopic();
			listener[index++] = device;
		}
		
		ObserveRequestHandler observe = observeHandlers.remove(dmClient);
		if (observe != null) {
			topics[index] = observe.getTopic();
			listener[index++] = observe;
		}
		
		CancelRequestHandler cancel = cancelHandlers.remove(dmClient);
		if (cancel != null) {
			topics[index] = cancel.getTopic();
			listener[index++] = cancel;
		}
		
		RebootRequestHandler reboot = rebootHandlers.remove(dmClient);
		if (reboot != null) {
			topics[index] = reboot.getTopic();
			listener[index++] = reboot;
		}
		
		FactoryResetRequestHandler reset = resetHandlers.remove(dmClient);
		if (reset != null) {
			topics[index] = reset.getTopic();
			listener[index++] = reset;
		}

		FirmwareDownloadRequestHandler fwDownload = fwDownloadHandlers.remove(dmClient);
		if (fwDownload != null) {
			topics[index] = fwDownload.getTopic();
			listener[index++] = fwDownload;
		}
		
		FirmwareUpdateRequestHandler fwUpdate = fwUpdateHandlers.remove(dmClient);
		if (fwUpdate != null) {
			topics[index] = fwUpdate.getTopic();
			listener[index++] = fwUpdate;
		}
		
		if(index > 0) {
			int[] qos = new int[index];
			Arrays.fill(qos, 1);
			dmClient.unsubscribe(topics);
		}
		
	}
}
