/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

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

import java.io.IOException;
import java.util.Properties;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.sample.client.SystemObject;
/**
 * 
 * This sample shows how a device can publish events using MQTT to IBM Watson IoT Platform, 
 * Also, this sample adds a counter in the event.
 *
 */
public class CustomEventPublish {
	
	private final static String PROPERTIES_FILE_NAME = "/device.properties";

	public static void main(String[] args) throws Exception {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(CustomEventPublish.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		
		DeviceClient myClient = null;
		try {
			//Instantiate and connect to IBM Watson IoT Platform
			myClient = new DeviceClient(props);
			myClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		SystemObject obj = new SystemObject();
		
		/**
		 * Publishes the process load event for every 1 second
		 */
		boolean status = true;
		while(true) {
			try {
				byte[] cpuLoad = new byte[] {30, 35, 30, 25};
				status = myClient.publishEvent("blink", cpuLoad , "binary", 1);
				System.out.println(status);
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
