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
 * Amit M Mangalvedkar - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.application;

import java.util.Properties;

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

	public Properties options = new Properties();
	public ApplicationClient myClient = null;	
	
	public RegisteredApplicationSubscribeSample() {

		options.put("id", "app" + (Math.random() * 10000));		
		options.put("Authentication-Method","apikey");
		options.put("API-Key", "<YOUR API KEY>");		
		options.put("Authentication-Token", "<YOUR TOKEN>");
		
		try {
			myClient = new ApplicationClient(options);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myClient.connect();
		myClient.setEventCallback(new MyEventCallback());		
		myClient.setStatusCallback(new MyStatusCallback());
		
		// Subscribe to particular device type, ID, event and format
		myClient.subscribeToDeviceEvents("ManagedDT", "RasPi01", "blink", "json", 0);
		
		myClient.subscribeToDeviceStatus("ManagedDT", "RasPi01");
		myClient.subscribeToDeviceCommands();
		myClient.subscribeToApplicationStatus();
		
	}

	public static void main(String[] args) {
		new RegisteredApplicationSubscribeSample();
	}
	
	private static class MyEventCallback implements EventCallback {

		public void processEvent(Event e) {
			System.out.println("Event:: " + e.getDeviceId() + ":" + e.getEvent() + ":" + e.getPayload());
		}

		public void processCommand(Command cmd) {
			System.out.println("Command " + cmd.getPayload());			
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

}
