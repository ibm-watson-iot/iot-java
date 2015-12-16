/**
 *****************************************************************************
 * Copyright (c) 2015 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution 
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.device;

import java.util.Properties;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.sample.client.SystemObject;
import com.ibm.iotf.sample.util.Utility;

/**
 * 
 * This sample shows how a device can publish events using MQTT to IBM IoT Foundation, 
 * Also, this sample adds a counter in the event.
 *
 */
public class DeviceEventPublishWithCounter {
	
	private final static String PROPERTIES_FILE_NAME = "device.prop";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";

	public static void main(String[] args) throws Exception {
		
		String fileName = null;
		if (args.length == 1) {
			fileName = args[0];
		} else {
			fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);
		}

		/**
		 * Load properties file "device.prop"
		 */
		Properties deviceProps = Utility.loadPropertiesFile(PROPERTIES_FILE_NAME, fileName);
		
		DeviceClient myClient = null;
		try {
			//Instantiate and connect to IBM IoT Foundation
			myClient = new DeviceClient(deviceProps);
			myClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		SystemObject obj = new SystemObject();
		
		/**
		 * Publishes the process load event for every 1 second
		 */
		long counter = 0;
		boolean status = true;
		while(true) {
			try {
				//Generate a JSON object of the event to be published
				JsonObject event = new JsonObject();
				event.addProperty("event-count", ++counter);
				event.addProperty("name", SystemObject.getName());
				event.addProperty("cpu",  obj.getProcessCpuLoad());
				event.addProperty("mem",  obj.getMemoryUsed());
				
				status = myClient.publishEvent("load", event);
				System.out.println(event);
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!status) {
				System.out.println("Failed to publish the event......");
				System.exit(-1);
			}
		}
	}
	
}
