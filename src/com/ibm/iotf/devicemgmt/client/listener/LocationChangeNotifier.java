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
import java.beans.PropertyChangeListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.client.device.DeviceLocation;
import com.ibm.iotf.devicemgmt.client.device.ManagedClient;
import com.ibm.iotf.devicemgmt.client.device.Resource.Resource;
import com.ibm.iotf.util.LoggerUtility;

public class LocationChangeNotifier extends DMNotifier {
	
	private static final String CLASS_NAME = LocationChangeNotifier.class.getName();
	
	public LocationChangeNotifier(ManagedClient dmClient) {
		super(dmClient);
	}
	
	@Override
	public void handleEvent(PropertyChangeEvent event) {
		final String METHOD = "propertyChange";
		JsonObject jsonData = new JsonObject();
		DeviceLocation location = (DeviceLocation) event.getNewValue();
		jsonData.add("d", location.toJsonObject());
		try {
			if (getDMClient().getDeviceData().getDeviceLocation().getResponseRequired() == true) {
				JsonObject response = notify(jsonData, Resource.RESPONSE_TIMEOUT);
				if (response != null) {
					getDMClient().getDeviceData().getDeviceLocation().setRC(response.get("rc").getAsInt());
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

	
}
