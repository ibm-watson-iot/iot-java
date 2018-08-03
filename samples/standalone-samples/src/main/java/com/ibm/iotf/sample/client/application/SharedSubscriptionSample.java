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

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
/**
 * This sample shows how to build a scalable applications which will load balance 
 * messages across multiple instances of the application.
 * 
 * Shared subscription allows one to build scalable applications which will load balance 
 * device events across multiple instances of the application by making a few changes to 
 * how the application connects to the IBM Watson IoT Platform. The shared subscription 
 * might be needed if one instance of back-end enterprise application can not keep up with 
 * the number of messages being published to a specific topic space, say for e.g., if many 
 * devices were publishing events that are being processed by a single application. 
 * 
 * The Watson IoT service extends the MQTT 3.1.1 specification to provide support for shared 
 * subscriptions and is limited to non-durable subscriptions only. The IoT doesn’t retain messages 
 * when the non-durable application disconnects from Watson IoT Platform.
 * 
 * In order to enable the shared subscription or scalable application support, Application(s) must 
 * supply a client id of the form A:org_id:app_id while connecting to Watson IoT Platform,
 * 
 *    A indicates the application is a scalable application
 *    org_id is your unique organization ID, assigned when you sign up with the service. It will be a 6 character alphanumeric string.
 *    app_id is a user-defined unique string identifier for this client.
 */

public class SharedSubscriptionSample {

	private final static String PROPERTIES_FILE_NAME = "/application.properties";

	public static void main(String[] args) throws Exception {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(SharedSubscriptionSample.class.getResourceAsStream(PROPERTIES_FILE_NAME));
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
		myAppClient.setEventCallback(new MyEventCallback());
		System.out.println("Subscribing to all the device events..");
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
