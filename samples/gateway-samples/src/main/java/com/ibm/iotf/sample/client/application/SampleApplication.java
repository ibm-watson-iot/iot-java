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

package com.ibm.iotf.sample.client.application;

import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.iotf.sample.client.gateway.SampleRasPiGateway;

/**
 *  A sample application to parse the Arduino events (sent by Raspberry Pi Gateway) 
 *  and send the LED blink command back.
 *  
 *  The Application sample uses the com.ibm.iotf.client.app.ApplicationClient class 
 *  from the Watson IoT Platform Java Client Library that simplifies the application interactions 
 *  with IBM Watson IoT Platform. 
 *  
 *  The corresponding Gateway code is available here - refer to SampleRaspiGateway.java 
 *  in the same package. 
 */ 
public class SampleApplication {

	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private static String COMMAND_NAME = "blink";
	private static String PIR_DATAPOINT_NAME = "pir";
	private static String LED_DATAPOINT_NAME = "led";
	
	protected final static JsonParser JSON_PARSER = new JsonParser();

	private ApplicationClient myAppClient = null;
	private String deviceType;
	private String deviceId;
	
	public static void main(String[] args) throws Exception {
		
		SampleApplication sample = new SampleApplication();
		
		try {
			
			sample.createApplicationClient(PROPERTIES_FILE_NAME);
			sample.subscribeToEvents();
			sample.userAction();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.err.flush();
		} 
	}
	

	/**
	 * To receive and process the Arduino Uno events, the sample application does the following,
	 *    1. Adds an event callback method
	 *    2. Subscribes to Arduino Uno device Events
	 * 
	 * The subscribeToEvents() method adds a Event callback handler and subscribes to events from 
	 * the Arduino Uno device. The callback method processEvent() is invoked by the ApplicationClient 
	 * when it receives any event for Arduino Uno from Watson IoT Platform. 
	 * 
	 * The Event Callback class defines a BlockingQueue to store and process the events (in separate thread) 
	 * for smooth handling of MQTT publish message as shown below, 
	 * 
	 */
	private void subscribeToEvents() {
		MyEventCallback r = new MyEventCallback(this);
		myAppClient.setEventCallback(r);
		Thread t = new Thread(r);
		t.start();
		myAppClient.subscribeToDeviceEvents(this.deviceType, this.deviceId);
	}
	
	/**
	 * The createApplicationClient() method creates an ApplicationClient instance and 
	 * connects the application to IBM Watson IoT Platform by passing the required properties.
	 * 
	 * After the successful connection to the Watson IoT Platform, the application can perform 
	 * the following operations, like subscribing to device events, subscribing to device status, 
	 * publishing device events and commands.
	 * 
	 * @param fileName
	 */
	private void createApplicationClient(String fileName) {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(SampleRasPiGateway.class.getResourceAsStream(fileName));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		
		deviceType = trimedValue(props.getProperty("Device-Type"));
		deviceId = trimedValue(props.getProperty("Device-ID"));
		
		try {
			//Instantiate the class by passing the properties file
			myAppClient = new ApplicationClient(props);
			// Connect to IBM Watson IoT Platform
			myAppClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * An event call back class that handles the backup event and restore acknowledgement.
	 * 
	 * The callback method processEvent() is invoked by the ApplicationClient when it receives 
	 * any event for Arduino Uno from Watson IoT Platform. 
	 * 
	 * The Event Callback class defines a BlockingQueue to store and process the events 
	 * (in separate thread) for smooth handling of MQTT publish message as shown below,
	 */
	private static class MyEventCallback implements Runnable, EventCallback {
		
		private BlockingQueue<Event> queue = new LinkedBlockingQueue<Event>();
		SampleApplication app;
		
		public MyEventCallback(SampleApplication app) {
			this.app = app;
		}
		
		@Override
		public void run() {
			while(true) {
				Event event = null;
				try {
					event = queue.take();
					String payload = event.getPayload();
					JsonObject content;
					JsonObject payloadJson = JSON_PARSER.parse(payload).getAsJsonObject();
					if (payloadJson.has("d")) {
						content = payloadJson.get("d").getAsJsonObject();
					} else {
						content = payloadJson;
					}
					String val = content.get(PIR_DATAPOINT_NAME).getAsString();
					int value = Integer.parseInt(val);
					if(value != 0) {
						app.sendBlinkCommand(value);
					}
				} catch (InterruptedException e1) {
					// Ignore the Interuppted exception, retry
					continue;
				}
			}
		}

		public void processEvent(Event event) {
			try {
				queue.put(event);
			} catch (InterruptedException e) {
			}	
		}

		public void processCommand(Command cmd) {
		
		}
	}
	
	/**
	 * Send a blink command to the device. The body contains the number of times to blink the LED.
	 */
	private void sendBlinkCommand(int value) {
		JsonObject cmd = new JsonObject();
		cmd.addProperty(LED_DATAPOINT_NAME, value);
		this.myAppClient.publishCommand(this.deviceType, deviceId, COMMAND_NAME, cmd, 2);
		System.out.println("Sent the "+ COMMAND_NAME + " command with value "+ value);
	}
	
	private void userAction() {
		System.out.println("Enter any valid number anytime to trun the LED blink");
    	Scanner in = new Scanner(System.in);
    	while(true) {
    		try {
	            String input = in.nextLine();
	            sendBlinkCommand(Integer.parseInt(input));
	            
    		} catch(Exception e) {
    			System.out.println("Operation failed with exception "+e.getMessage());
    			continue;
    		}
    	}
    }

	private static String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}
}
