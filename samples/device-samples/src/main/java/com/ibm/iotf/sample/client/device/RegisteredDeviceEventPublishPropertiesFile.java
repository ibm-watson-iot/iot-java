/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

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
 * This sample shows how we can write a device client which publishes events, in a Registered mode, using a properties file <br>
 * It uses the Java Client Library for IBM Watson IoT Platform.
 * This sample code should be executed in a JRE running on the device.
 * 
 */

package com.ibm.iotf.sample.client.device;

import java.io.File;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;

public class RegisteredDeviceEventPublishPropertiesFile {

	public static void main(String[] args) throws MqttException {
		//Provide the device specific data, as well as Auth-key and token using Properties class
		//A Sample properties file is provided in the src folder
		Properties options = DeviceClient.parsePropertiesFile(new File("C:\\temp\\device.properties"));

		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file			
			myClient = new DeviceClient(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Connect to the IBM Watson IoT Platform	
		myClient.connect();
		
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
		
		//Registered flow allows 0, 1 and 2 QoS
		myClient.publishEvent("status", event, 1);
		System.out.println("SUCCESSFULLY POSTED......");

		//Disconnect cleanly
		myClient.disconnect();
	}
}
