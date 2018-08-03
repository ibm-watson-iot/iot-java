/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

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

/**
 * This sample shows how we can write a device client which subscribes to commands 
 * while sending events regularly, in a Registered mode <br>
 * 
 */

package com.ibm.iotf.sample.client.device;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.paho.client.mqttv3.MqttException;


import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.sample.client.SystemObject;


//Implement the CommandCallback class to provide the way in which you want the command to be handled
class MyNewCommandCallback implements CommandCallback, Runnable {
	
	// A queue to hold & process the commands for smooth handling of MQTT messages
	private BlockingQueue<Command> queue = new LinkedBlockingQueue<Command>();
	
	/**
	 * This method is invoked by the library whenever there is command matching the subscription criteria
	 */
	@Override
	public void processCommand(Command cmd) {
		try {
			queue.put(cmd);
			} catch (InterruptedException e) {
		}			
	}

	@Override
	public void run() {
		while(true) {
			Command cmd = null;
			try {
				//In this sample, we just display the command
				cmd = queue.take();
				System.out.println("COMMAND RECEIVED = '" + cmd.getCommand() + "'\twith Payload = '" + cmd.getPayload() + "'");
			} catch (InterruptedException e) {}
		}
	}
}

public class RegisteredDeviceCommandSubscribe {

	private final static String PROPERTIES_FILE_NAME = "/device.properties";
	
	public static void main(String[] args) throws MqttException {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(RegisteredDeviceCommandSubscribe.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		
		
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file			
			myClient = new DeviceClient(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		MyNewCommandCallback callback = new MyNewCommandCallback();
		Thread t = new Thread(callback);
		t.start();
		myClient.setCommandCallback(callback);

		//Connect to the IBM Watson IoT Platform	
		myClient.connect();
		
		// Let us publish an event every second
		SystemObject obj = new SystemObject();
		
		/**
		 * Publishes the process load event for every 1 second
		 */
		boolean status = true;
		while(true) {
			try {
				//Generate a JSON object of the event to be published
				JsonObject event = new JsonObject();
				event.addProperty("name", SystemObject.getName());
				event.addProperty("cpu",  obj.getProcessCpuLoad());
				event.addProperty("mem",  obj.getMemoryUsed());
				
				//Registered flow allows 0, 1 and 2 QoS
				status = myClient.publishEvent("load", event);
				System.out.println(event);
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!status) {
				System.out.println("Failed to publish the event......");
				//Disconnect cleanly
				myClient.disconnect();
				System.exit(-1);
			}
		}
	}
}
