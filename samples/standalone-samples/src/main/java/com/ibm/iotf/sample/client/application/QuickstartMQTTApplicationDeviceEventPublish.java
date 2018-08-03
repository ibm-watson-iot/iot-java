/**
 *****************************************************************************
 * Copyright (c) 2015-16 IBM Corporation and other Contributors.

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

import java.util.Properties;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.sample.client.SystemObject;

/**
 * 
 * This sample shows how an application publish a device event 
 * using MQTT to IBM Watson IoT Platform Quickstart on behalf of the device.
 *
 */
public class QuickstartMQTTApplicationDeviceEventPublish {

	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.put("org", "quickstart");
		
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(props);
			myClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = "iotsample-ardunio";
		String deviceId = "00112233aabb";

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
				code = myClient.publishEvent(deviceType, deviceId, "blink", event);
			
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
}
