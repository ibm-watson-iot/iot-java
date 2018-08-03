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
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.api.APIClient.ContentType;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.sample.client.SystemObject;

/**
 * 
 * This sample shows how an application publish a device event 
 * using HTTP(s) to IBM Watson IoT Platform on behalf of the device.
 *
 */
public class HttpApplicationDeviceEventPublish {

	private final static String PROPERTIES_FILE_NAME = "/application.properties";

	public static void main(String[] args) throws Exception {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(HttpApplicationDeviceEventPublish.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
				
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = trimedValue(props.getProperty("Device-Type"));
		String deviceId = trimedValue(props.getProperty("Device-ID"));
		
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
		 * Publishes this process load event for every 5 second
		 */
		while(true) {
			boolean code = false;
			try {
				
				//Generate a JSON object of the event to be published
				JsonObject event = new JsonObject();
				event.addProperty("name", SystemObject.getName());
				event.addProperty("cpu",  obj.getProcessCpuLoad());
				event.addProperty("mem",  obj.getMemoryUsed());
				
				// publish the event on behalf of device
				code = myClient.publishApplicationEventforDeviceOverHTTP(deviceId, deviceType, "blink", event, ContentType.json);
			
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(code == true) {
				System.out.println("Published the event successfully !");
			} else {
				System.out.println("Failed to publish the event......");
				System.exit(-1);
			}
		}
	}
	
	private static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}

}
