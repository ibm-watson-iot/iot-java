/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.device;

import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;

/**
 * This test verifies that the device receives the command published by the application
 * successfully.
 *
 */
public class DeviceCommandSubscriptionTest extends TestCase{
	
	private final static String DEVICE_PROPERTIES_FILE = "/device.properties";
	private final static String APPLICATION_PROPERTIES_FILE = "/application.properties";
	
	@Test
	public void testCommandReception() throws Exception {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(DeviceCommandSubscriptionTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
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
		MyCommandCallback callback = new MyCommandCallback();
		myClient.setCommandCallback(callback);
		//Connect to the IBM Watson IoT Platform	
		try {
			myClient.connect();
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			throw e1;
		} catch (java.lang.IllegalArgumentException e) {
			// looks like the property file is not modified, exit silently
			return;
		}
		
		// Ask application to publish the command to this device now
		publichCommand(1);
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Command is not received by the device", callback.commandReceived);
	}
	
	@Test
	public void testCustomCommandReception() throws Exception {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(DeviceCommandSubscriptionTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
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
		MyCommandCallback callback = new MyCommandCallback();
		myClient.setCommandCallback(callback);
		//Connect to the IBM Watson IoT Platform	
		try {
			myClient.connect();
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			throw e1;
		} catch (java.lang.IllegalArgumentException e) {
			// looks like the property file is not modified, exit silently
			return;
		}
		
		// Ask application to publish the command to this device now
		publichCommand(2);
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Event is not received by application", callback.commandReceived);
	}
	
	
	private void publichCommand(int type) throws Exception {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(DeviceCommandSubscriptionTest.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
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
		
		/**
		 * Get the Device Type and Device Id to which the application will publish the command
		 */
		String deviceType = trimedValue(props.getProperty("Device-Type"));
		String deviceId = trimedValue(props.getProperty("Device-ID"));
		
		if(type == 1) { // JSON
			JsonObject data = new JsonObject();
			data.addProperty("name", "stop-rotation");
			data.addProperty("delay",  0);
			
			//Registered flow allows 0, 1 and 2 QoS
			myAppClient.publishCommand(deviceType, deviceId, "stop", data);
		} else {
			myAppClient.publishCommand(deviceType, deviceId, "stop", "rotation:80", "custom", 1);
		}
		myAppClient.disconnect();
	}

	
	//Implement the CommandCallback class to provide the way in which you want the command to be handled
	private static class MyCommandCallback implements CommandCallback {
		private boolean commandReceived = false;
		
		/**
		 * This method is invoked by the library whenever there is command matching the subscription criteria
		 */
		@Override
		public void processCommand(Command cmd) {
			commandReceived = true;
			System.out.println("Received command, name = "+cmd.getCommand() +
					", format = " + cmd.getFormat() + ", Payload = "+cmd.getPayload() + ", time = "+cmd.getTimestamp());
		}
	}
	
	private static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}

}
