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
 * Sathiskumar Palaniappan - Initial Contribution 
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.application;

import java.io.IOException;
import java.util.Properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.sample.client.SystemObject;

/**
 * 
 * This sample shows how an application publish commands to device
 * using HTTP(s) to IBM Watson IoT Platform
 *
 */
public class HttpCommandPublish {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";

	public static void main(String[] args) throws Exception {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(HttpCommandPublish.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		
		APIClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new APIClient(props);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		SystemObject obj = new SystemObject();

		/**
		 * Publishes the command 
		 */

		try {
			// Generate a JSON object of the event to be published
			JsonObject event = new JsonObject();
			event.addProperty("reboot", 5);

			boolean response = myClient.publishCommandOverHTTP("Cmd", event);
			if (response == true)
				System.out.println("Published Command Successfully!");
			else
				System.out.println("Failed Publishing Commad!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
