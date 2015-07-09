/**
 *****************************************************************************
 * Copyright (c) 2015 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Amit M Mangalvedkar - Initial Contribution
 *****************************************************************************
 */

/**
 * This sample shows how we can write a device client which publishes events, in a Quickstart mode <br>
 * It uses the Java Client Library for IBM IoT Foundation
 * This sample code should be executed in a JRE running on the device
 * 
 */

package com.ibm.iotf.sample.client.device;

import java.util.Properties;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;

public class QuickstartDeviceEventPublish {

	public static void main(String[] args) {
		
		//Provide the device specific data using Properties class
		Properties options = new Properties();
		options.setProperty("org", "quickstart");
		options.setProperty("type", "iotsample-arduino");
		options.setProperty("id", "00aabbccde03");
		
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Connect to the IBM IoT Foundation
		myClient.connect();
		
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
		
		//Quickstart flow allows only QoS = 0
		myClient.publishEvent("status", event, 0);
		System.out.println("SUCCESSFULLY POSTED......");

		//Disconnect cleanly
		myClient.disconnect();
	}
}
