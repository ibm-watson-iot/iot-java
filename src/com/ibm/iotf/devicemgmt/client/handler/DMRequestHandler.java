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
package com.ibm.iotf.devicemgmt.client.handler;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.devicemgmt.client.device.DeviceTopic;
import com.ibm.iotf.devicemgmt.client.device.ManagedClient;
import com.ibm.iotf.devicemgmt.client.device.ServerTopic;
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
	private static Map<ManagedClient, GenericRequestHandler> genericHandlers = new HashMap<ManagedClient,GenericRequestHandler>();
	
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
	
	public static void setRequestHandlers(ManagedClient dmClient) throws MqttException{
		
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
	
	public static void clearRequestHandlers(ManagedClient dmClient) {
		
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
