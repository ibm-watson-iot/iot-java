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
	private static Map<ManagedDevice, GenericRequestHandler> genericHandlers = new HashMap<ManagedDevice,GenericRequestHandler>();
	
	private static Map<ServerTopic, DMRequestHandler> handlers = new HashMap<ServerTopic,DMRequestHandler>();
	
	public abstract void handleRequest(JsonObject jsonRequest);
	
	@Override
	public void messageArrived(String topic, MqttMessage message) {
		final String METHOD = "messageArrived";
		try {
			String payload = new String (message.getPayload(), "UTF-8");
			JsonObject jsonRequest = new JsonParser().parse(payload).getAsJsonObject();
			LoggerUtility.fine(CLASS_NAME, METHOD, System.identityHashCode(this) + "Handler(" + this.getClass().getName() + 
					") Received request on topic " + topic + ") " + jsonRequest.toString());
			
			DMRequestHandler h = handlers.get(ServerTopic.get(topic));
			if (h != null) {
				h.handleRequest(jsonRequest);
			} else {
				if(!ServerTopic.RESPONSE.getName().equals(topic)) {
					LoggerUtility.warn(CLASS_NAME, METHOD, "No handler for topic (" + topic + 
						") payload(" + jsonRequest.toString() + ")");
				}
			}
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
	
	public static void setRequestHandlers(ManagedDevice dmClient) throws MqttException{
		
		DeviceUpdateRequestHandler device = deviceUpdateHandlers.get(dmClient);
		if (device == null) {
			device = new DeviceUpdateRequestHandler(dmClient);
			deviceUpdateHandlers.put(dmClient, device);
			handlers.put(ServerTopic.DEVICE_UPDATE, device);
		}
		
		ObserveRequestHandler observe = observeHandlers.get(dmClient);
		if (observe == null) {
			observe = new ObserveRequestHandler(dmClient);
			observeHandlers.put(dmClient, observe);
			handlers.put(ServerTopic.OBSERVE, observe);
		}
		
		CancelRequestHandler cancel = cancelHandlers.get(dmClient);
		if (cancel == null) {
			cancel = new CancelRequestHandler(dmClient);
			cancelHandlers.put(dmClient, cancel);
			handlers.put(ServerTopic.CANCEL, cancel);
		}
		
		RebootRequestHandler reboot = rebootHandlers.get(dmClient);
		if (reboot == null) {
			reboot = new RebootRequestHandler(dmClient);
			rebootHandlers.put(dmClient, reboot);
			handlers.put(ServerTopic.INITIATE_REBOOT, reboot);
		}
		
		FactoryResetRequestHandler reset = resetHandlers.get(dmClient);
		if (reset == null) {
			reset = new FactoryResetRequestHandler(dmClient);
			resetHandlers.put(dmClient, reset);
			handlers.put(ServerTopic.INITIATE_FACTORY_RESET, reset);
		}
		
		FirmwareDownloadRequestHandler fwDownload = fwDownloadHandlers.get(dmClient);
		if (fwDownload == null) {
			fwDownload = new FirmwareDownloadRequestHandler(dmClient);
			fwDownloadHandlers.put(dmClient, fwDownload);
			handlers.put(ServerTopic.INITIATE_FIRMWARE_DOWNLOAD, fwDownload);
		}
		
		FirmwareUpdateRequestHandler fwUpdate = fwUpdateHandlers.get(dmClient);
		if (fwUpdate == null) {
			fwUpdate = new FirmwareUpdateRequestHandler(dmClient);
			fwUpdateHandlers.put(dmClient, fwUpdate);
			handlers.put(ServerTopic.INITIATE_FIRMWARE_UPDATE, fwUpdate);
		}

		GenericRequestHandler generic = genericHandlers.get(dmClient);
		if (generic == null) {
			generic = new GenericRequestHandler(dmClient);
			genericHandlers.put(dmClient, generic);
			handlers.put(ServerTopic.GENERIC, generic);
		}
		// Subscribe
		generic.getDMClient().subscribe(ServerTopic.GENERIC, 1, generic);
	}
	
	public static void clearRequestHandlers(ManagedDevice dmClient) {
		
		deviceUpdateHandlers.remove(dmClient);
		observeHandlers.remove(dmClient);
		cancelHandlers.remove(dmClient);
		rebootHandlers.remove(dmClient);
		resetHandlers.remove(dmClient);
		fwDownloadHandlers.remove(dmClient);
		fwUpdateHandlers.remove(dmClient);
		genericHandlers.remove(dmClient);
		
	}

}
