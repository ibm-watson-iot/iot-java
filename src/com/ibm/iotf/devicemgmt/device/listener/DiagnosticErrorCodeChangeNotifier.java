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
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.device.DeviceDiagnostic;
import com.ibm.iotf.devicemgmt.device.DeviceTopic;
import com.ibm.iotf.devicemgmt.device.DiagnosticErrorCode;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.devicemgmt.device.resource.Resource;
import com.ibm.iotf.util.LoggerUtility;

public class DiagnosticErrorCodeChangeNotifier extends DMNotifier {
	
	private static final String CLASS_NAME = DiagnosticErrorCodeChangeNotifier.class.getName();

	public DiagnosticErrorCodeChangeNotifier(ManagedDevice client) {
		super(client);
	}
	
	@Override
	public void handleEvent(PropertyChangeEvent event) {
		final String METHOD = "handleEvent"; 
		JsonObject jsonData = new JsonObject();
		DiagnosticErrorCode diag = (DiagnosticErrorCode) event.getNewValue();
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
		DiagnosticErrorCode diag = (DiagnosticErrorCode) event.getNewValue();
		
		try {
			if (diag.getResponseRequired() == true) {
				JsonObject response = notify(DeviceTopic.CLEAR_DIAG_ERRCODES, 
												jsonData, Resource.RESPONSE_TIMEOUT);
				if (response != null) {
					diag.setRC(response.get("rc").getAsInt());
				} else {
					LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, 
							"DID NOT GET A RESPONSE FROM IOTF for location update.");
				}
			} else {
				notify(DeviceTopic.CLEAR_DIAG_ERRCODES, jsonData);
			}
		} catch (MqttException e) {
			//TODO: Inform the device application when errors occur
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, e.toString());
		}
	}
}
