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
package com.ibm.iotf.devicemgmt.device.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.device.DiagnosticLog;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.devicemgmt.device.internal.DeviceTopic;
import com.ibm.iotf.devicemgmt.device.resource.Resource;
import com.ibm.iotf.util.LoggerUtility;

public class DiagnosticLogChangeNotifier extends DMNotifier {
	
	private static final String CLASS_NAME = DiagnosticLogChangeNotifier.class.getName();

	public DiagnosticLogChangeNotifier(ManagedDevice client) {
		super(client);
	}
	
	@Override
	public void handleEvent(PropertyChangeEvent event) {
		final String METHOD = "handleEvent"; 
		JsonObject jsonData = new JsonObject();
		DiagnosticLog diag = (DiagnosticLog) event.getNewValue();
		jsonData.add("d", diag.toJsonObject());
		
		try {
			if (diag.getResponseRequired() == true) {
				JsonObject response = notify(jsonData, Resource.RESPONSE_TIMEOUT);
				if (response != null) {
					diag.setRC(response.get("rc").getAsInt());
				} else {
					LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, 
							"DID NOT GET A RESPONSE FROM IOTF for location update.");
				}
			} else {
				notify(jsonData);
			}
		} catch (MqttException e) {
			//TODO: Inform the device application when errors occur
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, e.toString());
		}
	}
	
	@Override
	public void clearEvent(PropertyChangeEvent event) {
		final String METHOD = "handleEvent"; 
		JsonObject jsonData = new JsonObject();
		DiagnosticLog diag = (DiagnosticLog) event.getNewValue();
		try {
			if (diag.getResponseRequired() == true) {
				JsonObject response = notify(DeviceTopic.CLEAR_DIAG_LOG, jsonData, Resource.RESPONSE_TIMEOUT);
				if (response != null) {
					diag.setRC(response.get("rc").getAsInt());
				} else {
					LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, 
							"DID NOT GET A RESPONSE FROM IOTF for location update.");
				}
			} else {
				notify(DeviceTopic.CLEAR_DIAG_LOG, jsonData);
			}
		} catch (MqttException e) {
			//TODO: Inform the device application when errors occur
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, e.toString());
		}
	}

	
}
