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

package com.ibm.iotf.sample.devicemgmt.gateway.home;

import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.iotf.sample.devicemgmt.gateway.home.Device.DeviceType;
import com.ibm.iotf.sample.devicemgmt.gateway.home.Oven.MODE;
import com.ibm.iotf.sample.util.Utility;

/**
 *  A sample application to send commands to the home gateway to control one or more devices 
 *  attached to the gateway.
 *  
 *  The Application sample uses the com.ibm.iotf.client.app.ApplicationClient class 
 *  from the Watson IoT Platform Java Client Library that simplifies the application interactions 
 *  with IBM Watson IoT Platform. 
 *  
 *  The corresponding Gateway code is available here - refer to SampleRaspiGateway.java 
 *  in the same package. 
 */ 
public class HomeApplication {

	private final static String PROPERTIES_FILE_NAME = "application.prop";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";
	
	private final static JsonParser JSON_PARSER = new JsonParser();

	private ApplicationClient myAppClient = null;
	private String gwDeviceType;
	private String gwDeviceId;
	
	public static void main(String[] args) throws Exception {
		
		HomeApplication sample = new HomeApplication();
		
		try {
			
			String fileName = null;
			if (args.length == 1) {
				fileName = args[0];
			} else {
				fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);
			}
			
			sample.createApplicationClient(fileName);
			sample.subscribeToEvents();
			sample.userAction();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.err.flush();
		} 
	}
	

	/**
	 * To receive and process the attached device events, the sample application does the following,
	 *    1. Adds an event callback method
	 *    2. Subscribes to all attached device Events and Gateway Events
	 * 
	 * The subscribeToEvents() method adds a Event callback handler and subscribes to events from 
	 * the attached devices. The callback method processEvent() is invoked by the ApplicationClient 
	 * when it receives any event that matches the subscription. 
	 * 
	 * The Event Callback class defines a BlockingQueue to store and process the events (in separate thread) 
	 * for smooth handling of MQTT publish message as shown below, 
	 * 
	 */
	private void subscribeToEvents() {
		MyEventCallback r = new MyEventCallback();
		myAppClient.setEventCallback(r);
		Thread t = new Thread(r);
		t.start();
		// Subscribes to all device events in the organization, but
		// you can control using the overloaded methods
		myAppClient.subscribeToDeviceEvents(); 
		
		/*try {
			JsonObject response = this.myAppClient.api().getDevicesConnectedThroughGateway(this.gwDeviceType, this.gwDeviceId);
			System.out.println(response);
		} catch (IoTFCReSTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
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
		 * Load properties file "application.prop"
		 */
		Properties props = Utility.loadPropertiesFile(PROPERTIES_FILE_NAME, fileName);
		
		gwDeviceType = Utility.trimedValue(props.getProperty("Device-Type"));
		gwDeviceId = Utility.trimedValue(props.getProperty("Device-ID"));
		
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
		
		/**
		 * Let us process the Load and inform the application about it
		 * @param event
		 */
		private void processElevatorEvents(Event event) {
			JsonObject content;
			
			JsonObject payloadJson = JSON_PARSER.parse(event.getPayload()).getAsJsonObject();
			if (payloadJson.has("d")) {
				content = payloadJson.get("d").getAsJsonObject();
			} else {
				content = payloadJson;
			}
			int val = content.get("load").getAsInt();
			if(val >=18) {
				System.out.println("The current load of the elevator "+val +" is high..");
			}
		}
		
		/**
		 * Let us process the Oven stop event and inform the application user
		 * @param event
		 */
		private void processOvenEvents(Event event) {
			if(event.getEvent().equalsIgnoreCase("stop")) {
				System.out.println("Oven stopped heating/cooking !!");
			}
			
			// One can process the temperature, pressure parameters to have a better control
		}
		
		/**
		 * Let us process the Outdoor temperature and inform user 
		 * if the temperature is > 102 or less than 70 
		 * @param event
		 */
		private void processTemperatureEvents(Event event) {
			JsonObject content;
			
			JsonObject payloadJson = JSON_PARSER.parse(event.getPayload()).getAsJsonObject();
			if (payloadJson.has("d")) {
				content = payloadJson.get("d").getAsJsonObject();
			} else {
				content = payloadJson;
			}
			int val = content.get("temp").getAsInt();
			if(val > 100 || val < 70) {
				System.out.println("The current temperature "+val +" exceeds the range .. ");
			}
		}
		
		@Override
		public void run() {
			while(true) {
				Event event = null;
				try {
					event = queue.take();
					if(DeviceType.OVEN.getDeviceType().equals(event.getDeviceType())) {
						processOvenEvents(event);
					} else if (DeviceType.TEMPERATURE.getDeviceType().equals(event.getDeviceType())) {
						processTemperatureEvents(event);
					} else if(DeviceType.ELEVATOR.getDeviceType().equals(event.getDeviceType())) {
						processElevatorEvents(event);
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
	 * Send a start command to the oven device to start the heating/cooking process.
	 */
	private void sendCommandToOven(String parameters[]) {
		
		JsonObject cmd = new JsonObject();
		cmd.addProperty("mode", MODE.MODHOT.name());
		cmd.addProperty("position", "Middle");
		cmd.addProperty("timer", 300);		// Let us run for 5 minutes
		System.out.println("<-- sending command to start oven");
		boolean status = this.myAppClient.publishCommand(DeviceType.OVEN.getDeviceType(), parameters[1], "start", cmd, 2);
		if(status == false) {
			System.err.println("Failed to send the command to Oven device "+parameters[1]);
		}
	}
	
	/**
	 * Send a command to on/off the light and also control the brightness
	 * 
	 * light [Light-deviceid] [on/off] [brightness in number between 0 to 100]
	 */
	private void sendCommandToLight(String parameters[]) {
		
		JsonObject cmd = new JsonObject();
		cmd.addProperty("status", parameters[2].toUpperCase());
		cmd.addProperty("brightness", Integer.parseInt(parameters[3]));
		System.out.println("<-- sending command to control light");
		boolean status = this.myAppClient.publishCommand(DeviceType.LIGHT.getDeviceType(), parameters[1], "set", cmd, 2);
		if(status == false) {
			System.err.println("Failed to send the command to Light device "+parameters[1]);
		}
	}
	
	/**
	 * Send a command to on/off the swicth
	 * 
	 * switch [Switch-deviceId] [on/off]
	 */
	private void sendCommandToSwitch(String parameters[]) {
		
		JsonObject cmd = new JsonObject();
		cmd.addProperty("status", parameters[2].toUpperCase());
		System.out.println("<-- sending command to on/off swicth");
		boolean status = this.myAppClient.publishCommand(DeviceType.SWITCH.getDeviceType(), parameters[1], "set", cmd, 2);
		if(status == false) {
			System.err.println("Failed to send the command to Switch device "+parameters[1]);
		}
	}
	
	private void userAction() {
    	Scanner in = new Scanner(System.in);
    	printOptions();
    	while(true) {
    		try {
	    		System.out.println("Enter the command ");
	            String input = in.nextLine();
	            String[] parameters = input.split(" ");
	            
	            if(parameters.length <= 1) {
	    			System.err.println("Device-Id is not entered");
	    		}
	            
	            switch(parameters[0].toLowerCase()) {
	            
	            case "light":
	            	sendCommandToLight(parameters);
            		break;
	            
	            case "switch":
	            	sendCommandToSwitch(parameters);
	            	break;
	            		
	            case "oven":
	            	sendCommandToOven(parameters);
	            	break;
	            		
	            case "quit":
	            	this.myAppClient.disconnect();
	            	in.close();
	            	System.exit(-1);
	            
	            default:
	            	System.out.println("Unknown command received :: "+input);
	            	printOptions();
	            		
	            }
    		} catch(Exception e) {
    			System.out.println("Operation failed with exception "+e.getMessage());
    			e.printStackTrace();
    			printOptions();
    			continue;
    		}
    	}
    }
	
	private static void printOptions() {
		System.out.println("List of operations that this Application can perform are:");
		System.out.println("light [Light-deviceid] [on/off] [brightness in number between 0 to 100]");
		System.out.println("switch [Switch-deviceId] [on/off]");
		System.out.println("oven oven-device-id");
		System.out.println("quit:: quit this sample");
	}
	
	
}
