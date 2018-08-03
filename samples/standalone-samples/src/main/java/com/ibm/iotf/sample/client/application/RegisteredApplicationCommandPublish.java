/**
 *****************************************************************************
 * Copyright (c) 2015-16 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 * Amit M Mangalvedkar - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.application;

import java.io.IOException;
import java.util.Properties;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.app.ApplicationClient;

/**
 * This sample shows how one application can publish commands to the device
 *
 */
public class RegisteredApplicationCommandPublish {

	private final static String PROPERTIES_FILE_NAME = "/application.properties";

	public static void main(String[] args) throws Exception {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(RegisteredApplicationCommandPublish.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		ApplicationClient myAppClient = null;
		try {
			//Instantiate the class by passing the properties file
			myAppClient = new ApplicationClient(props);
			myAppClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/**
		 * Get the Device Type and Device Id to which the application will publish the command
		 */
		String deviceType = trimedValue(props.getProperty("Device-Type"));
		String deviceId = trimedValue(props.getProperty("Device-ID"));
		
		JsonObject data = new JsonObject();
		data.addProperty("name", "stop-rotation");
		data.addProperty("delay",  0);
		
		//Registered flow allows 0, 1 and 2 QoS
		myAppClient.publishCommand(deviceType, deviceId, "stop", data);
		myAppClient.disconnect();
	}
	
	private static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}
}
