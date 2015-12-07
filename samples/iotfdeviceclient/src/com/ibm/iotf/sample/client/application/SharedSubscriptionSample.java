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

package com.ibm.iotf.sample.client.application;

import java.util.Date;
import java.util.Properties;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.iotf.sample.util.Utility;

/**
 * This sample shows how to build a scalable applications which will load balance 
 * messages across multiple instances of the application 
 *
 */
public class SharedSubscriptionSample {

	private final static String PROPERTIES_FILE_NAME = "application.prop";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";

	public static void main(String[] args) throws Exception {
		String fileName = null;
		if (args.length == 1) {
			fileName = args[0];
		} else {
			fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);
		}

		/**
		 * Load properties file "application.prop"
		 */
		Properties props = Utility.loadPropertiesFile(PROPERTIES_FILE_NAME, fileName);
		ApplicationClient myAppClient = null;
		try {
			//Instantiate the class by passing the properties file
			myAppClient = new ApplicationClient(props);
			myAppClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		myAppClient.setEventCallback(new MyEventCallback());
		myAppClient.subscribeToDeviceEvents();
	}
	
	private static class MyEventCallback implements EventCallback {

		public void processEvent(Event e) {
			System.out.println("Event:: " + e.getDeviceId() + ":" + e.getEvent() + 
					":" + e.getPayload() +" received at time:: "+new Date());
		}

		public void processCommand(Command cmd) {
			System.out.println("Command " + cmd.getPayload());			
		}
	}
}
