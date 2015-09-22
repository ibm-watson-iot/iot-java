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
package com.ibm.iotf.sample.devicemgmt.device.task;

import java.util.Random;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

/**
 * This method publishes a sample device event
 * 
 *  This sample shows that one can publish events while carrying out
 *  the device management operations.
 */
public class PublishDeviceEventTask implements Runnable {
	Random random = new Random();
	private ManagedDevice dmClient;
	
	public PublishDeviceEventTask(ManagedDevice dmClient) {
		this.dmClient = dmClient;
	}
	
	public void run() {
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  random.nextInt(100));
		event.addProperty("mem",  random.nextInt(100));
		
		System.out.println("Publishing device event:: "+event);
		//Registered flow allows 0, 1 and 2 QoS	
		dmClient.publishEvent("status", event);
	}
}
