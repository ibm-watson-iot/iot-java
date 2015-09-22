/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt.device.handler;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.devicemgmt.device.DeviceTopic;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.devicemgmt.device.ServerTopic;
import com.ibm.iotf.util.LoggerUtility;


public abstract class DMRequestHandler implements IMqttMessageListener {
	
	protected static final String CLASS_NAME = DMRequestHandler.class.getName();
	
	private ManagedDevice dmClient = null;
	
	private static Map<ManagedDevice, DeviceUpdateRequestHandler> deviceUpdateHandlers = new HashMap<ManagedDevice,DeviceUpdateRequestHandler>();
	private static Map<ManagedDevice, ObserveRequestHandler> observeHandlers = new HashMap<ManagedDevice,ObserveRequestHandler>();
	private static Map<ManagedDevice, CancelRequestHandler> cancelHandlers = new HashMap<ManagedDevice,CancelRequestHandler>();
	private static Map<ManagedDevice, RebootRequestHandler> rebootHandlers = new HashMap<ManagedDevice,RebootRequestHandler>();
	private static Map<ManagedDevice, FactoryResetRequestHandler> resetHandlers = new HashMap<ManagedDevice,FactoryResetRequestHandler>();
	private static Map<ManagedDevice, FirmwareDownloadRequestHandler> fwDownloadHandlers = new HashMap<ManagedDevice,FirmwareDownloadRequestHandler>();
	private static Map<ManagedDevice, FirmwareUpdateRequestHandler> fwUpdateHandlers = new HashMap<ManagedDevice,FirmwareUpdateRequestHandler>();
	
	protected abstract void subscribe();
	protected abstract void unsubscribe();
	protected abstract ServerTopic getTopic();
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
	
	protected ManagedDevice getDMClient() {
		return dmClient;
	}
	
	protected void setDMClient(ManagedDevice client) {
		dmClient = client;
	}
	
	protected ObserveRequestHandler getObserveRequestHandler(ManagedDevice dmClient) {
		return observeHandlers.get(dmClient);
	}
	
	protected void subscribe(ServerTopic topic) {
		final String METHOD = "subscribe";
		try {
			getDMClient().subscribe(topic, 1, (IMqttMessageListener)this);
		} catch (MqttException e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, ": Unexpected Mqtt Exception, code = " + e.getReasonCode());
		}
	}
	
	protected void unsubscribe(ServerTopic topic) {
		final String METHOD = "unsubscribe";
		try {
			getDMClient().unsubscribe(topic);
		} catch (MqttException e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, ": Unexpected Mqtt Exception, code = " + e.getReasonCode());
		}
	}

	protected void respond(JsonObject payload) {
		final String METHOD = "respond";
		try {
			dmClient.publish(DeviceTopic.RESPONSE, payload, 1);
		} catch (MqttException e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Unexpected Mqtt Exception, code = " + e.getReasonCode());
		}
	}

	protected void notify(JsonObject payload) {
		final String METHOD = "notify";
		try {
			dmClient.publish(DeviceTopic.NOTIFY, payload, 1);
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
	public static void setRequestHandlers(ManagedDevice dmClient) throws MqttException{
		
		String[] topics = new String[7];
		IMqttMessageListener[] listener = new IMqttMessageListener[7];
		int index = 0;
		
		DeviceUpdateRequestHandler device = deviceUpdateHandlers.get(dmClient);
		if (device == null) {
			device = new DeviceUpdateRequestHandler(dmClient);
			topics[index] = device.getTopic().getName();
			listener[index++] = device;
			//device.subscribe();
			deviceUpdateHandlers.put(dmClient, device);
		}
		
		ObserveRequestHandler observe = observeHandlers.get(dmClient);
		if (observe == null) {
			observe = new ObserveRequestHandler(dmClient);
			topics[index] = observe.getTopic().getName();
			listener[index++] = observe;
			observeHandlers.put(dmClient, observe);
		}
		
		CancelRequestHandler cancel = cancelHandlers.get(dmClient);
		if (cancel == null) {
			cancel = new CancelRequestHandler(dmClient);
			topics[index] = cancel.getTopic().getName();
			listener[index++] = cancel;
			cancelHandlers.put(dmClient, cancel);
		}
		
		RebootRequestHandler reboot = rebootHandlers.get(dmClient);
		if (reboot == null) {
			reboot = new RebootRequestHandler(dmClient);
			topics[index] = reboot.getTopic().getName();
			listener[index++] = reboot;
			rebootHandlers.put(dmClient, reboot);
		}
		
		FactoryResetRequestHandler reset = resetHandlers.get(dmClient);
		if (reset == null) {
			reset = new FactoryResetRequestHandler(dmClient);
			topics[index] = reset.getTopic().getName();
			listener[index++] = reset;
			resetHandlers.put(dmClient, reset);
		}
		
		FirmwareDownloadRequestHandler fwDownload = fwDownloadHandlers.get(dmClient);
		if (fwDownload == null) {
			fwDownload = new FirmwareDownloadRequestHandler(dmClient);
			topics[index] = fwDownload.getTopic().getName();
			listener[index++] = fwDownload;
			fwDownloadHandlers.put(dmClient, fwDownload);
		}
		
		FirmwareUpdateRequestHandler fwUpdate = fwUpdateHandlers.get(dmClient);
		if (fwUpdate == null) {
			fwUpdate = new FirmwareUpdateRequestHandler(dmClient);
			topics[index] = fwUpdate.getTopic().getName();
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
	public static void clearRequestHandlers(ManagedDevice dmClient) throws MqttException {
		
		String[] topics = new String[7];
		IMqttMessageListener[] listener = new IMqttMessageListener[7];
		int index = 0;
		
		DeviceUpdateRequestHandler device = deviceUpdateHandlers.remove(dmClient);
		if (device != null) {
			topics[index] = device.getTopic().getName();
			listener[index++] = device;
		}
		
		ObserveRequestHandler observe = observeHandlers.remove(dmClient);
		if (observe != null) {
			topics[index] = observe.getTopic().getName();
			listener[index++] = observe;
		}
		
		CancelRequestHandler cancel = cancelHandlers.remove(dmClient);
		if (cancel != null) {
			topics[index] = cancel.getTopic().getName();
			listener[index++] = cancel;
		}
		
		RebootRequestHandler reboot = rebootHandlers.remove(dmClient);
		if (reboot != null) {
			topics[index] = reboot.getTopic().getName();
			listener[index++] = reboot;
		}
		
		FactoryResetRequestHandler reset = resetHandlers.remove(dmClient);
		if (reset != null) {
			topics[index] = reset.getTopic().getName();
			listener[index++] = reset;
		}

		FirmwareDownloadRequestHandler fwDownload = fwDownloadHandlers.remove(dmClient);
		if (fwDownload != null) {
			topics[index] = fwDownload.getTopic().getName();
			listener[index++] = fwDownload;
		}
		
		FirmwareUpdateRequestHandler fwUpdate = fwUpdateHandlers.remove(dmClient);
		if (fwUpdate != null) {
			topics[index] = fwUpdate.getTopic().getName();
			listener[index++] = fwUpdate;
		}
		
		if(index > 0) {
			int[] qos = new int[index];
			Arrays.fill(qos, 1);
			dmClient.unsubscribe(topics);
		}
		
	}

}
