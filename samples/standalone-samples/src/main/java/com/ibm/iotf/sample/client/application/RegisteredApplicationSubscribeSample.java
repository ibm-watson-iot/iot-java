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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.ApplicationStatus;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.DeviceStatus;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.iotf.client.app.StatusCallback;

/**
 * This sample shows how an application subscribe to device status 
 * (like connect & disconnect from IBM Watson IoT Platform) and device events. 
 * 
 */
public class RegisteredApplicationSubscribeSample {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";

	private ApplicationClient myClient = null;	
	
	public RegisteredApplicationSubscribeSample() throws MqttException {

		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(RegisteredApplicationSubscribeSample.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		try {
			myClient = new ApplicationClient(props);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myClient.connect();
		
		/**
		 * Get the Device Type and Device Id for which the application will listen for the events
		 */
		String deviceType = trimedValue(props.getProperty("Device-Type"));
		String deviceId = trimedValue(props.getProperty("Device-ID"));
		
		MyEventCallback evtBack = new MyEventCallback();
		Thread t = new Thread(evtBack);
		t.start();
		myClient.setEventCallback(evtBack);
		
		myClient.setStatusCallback(new MyStatusCallback());
		
		System.out.println("Subscribing to load events from device " + deviceId + " of type "+deviceType);
		// Subscribe to particular device type, ID, event and format
		myClient.subscribeToDeviceEvents(deviceType, deviceId, "load", "json", 0);
		
		myClient.subscribeToDeviceStatus(deviceType, deviceId);
		
	}

	public static void main(String[] args) throws MqttException {
		new RegisteredApplicationSubscribeSample();
	}
	
	/**
	 * A sample Event callback class that processes the device events in separate thread.
	 *
	 */
	private static class MyEventCallback implements EventCallback, Runnable {

		// A queue to hold & process the Events for smooth handling of MQTT messages
		private BlockingQueue<Event> evtQueue = new LinkedBlockingQueue<Event>();
		
		public void processEvent(Event e) {
			try {
				evtQueue.put(e);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		@Override
		public void processCommand(Command cmd) {
			System.out.println("Command received:: " + cmd);			
		}

		@Override
		public void run() {
			while(true) {
				Event e = null;
				try {
					e = evtQueue.take();
					// In this example, we just output the event
					System.out.println("Event:: " + e.getDeviceId() + ":" + e.getEvent() + ":" + e.getPayload());
				} catch (InterruptedException e1) {
						// Ignore the Interuppted exception, retry
						continue;
				}
					
			}
		}

	}

	private static class MyStatusCallback implements StatusCallback {

		public void processApplicationStatus(ApplicationStatus status) {
			System.out.println("Application Status = " + status.getPayload());
		}

		public void processDeviceStatus(DeviceStatus status) {
			if(status.getAction() == "Disconnect") {
				System.out.println("device: "+status.getDeviceId()
						+ "  time: "+ status.getTime()
						+ "  action: " + status.getAction()
						+ "  reason: " + status.getReason());
			} else {
				System.out.println("device: "+status.getDeviceId()
						+ "  time: "+ status.getTime()
						+ "  action: " + status.getAction());
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
