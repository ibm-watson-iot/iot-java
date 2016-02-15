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
 * Sathiskumar Palaniappan - Initial Contribution 
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.device;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.sample.client.SystemObject;
import com.ibm.iotf.sample.util.Utility;

/**
 * 
 * This sample shows how a device can publish events 
 * using HTTP(s) to IBM Watson IoT Platform
 *
 */
public class HttpDeviceEventPublish {
	
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
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(deviceProps);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		SystemObject obj = new SystemObject();
		
		/**
		 * Publishes the process load event for every 1 second
		 */
		while(true) {
			int code = 0;
			try {
				//Generate a JSON object of the event to be published
				JsonObject event = new JsonObject();
				event.addProperty("name", SystemObject.getName());
				event.addProperty("cpu",  obj.getProcessCpuLoad());
				event.addProperty("mem",  obj.getMemoryUsed());
				
				code = myClient.publishEventOverHTTP("blink", event);
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(code == 200) {
				System.out.println("Published the event successfully !");
			} else {
				System.out.println("Failed to publish the event......");
				System.exit(-1);
			}
		}
	}
	
}
