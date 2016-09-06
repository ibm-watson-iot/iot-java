/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial contribution
 *****************************************************************************
 */

package com.ibm.iotf.client.gateway;

import java.io.IOException;
import java.util.Properties;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.gateway.GatewayClient;

import junit.framework.TestCase;

/**
 * This Test verifies whether one can publish event successfully to the quickstart 
 * and registered service (if property file is provided).
 * 
 */

public class GatewayEventPublishTest extends TestCase{
	
	// Properties file containing the Registration details of the gateway, to connect to Watson IoT Platform
	// Present in the maven resource directory
	private final static String PROPERTIES_FILE_NAME = "/gateway.properties";
		
	private final static String DEVICE_TYPE = "iotsampleType";
	private final static String SIMULATOR_DEVICE_ID = "Arduino02";
	private static GatewayClient gwClient = null;
	private static APIClient apiClient = null;
	
	public void setUp() {
	    // do the setup
	    createGatewayClient(PROPERTIES_FILE_NAME);
	    try {
	    	if(apiClient != null) {
	    		addDeviceType(DEVICE_TYPE);
	    		addDevice(DEVICE_TYPE, SIMULATOR_DEVICE_ID);
	    	}
		} catch (IoTFCReSTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void tearDown() throws IoTFCReSTException {
		gwClient.disconnect();
		if(apiClient != null) {
			apiClient.deleteDevice(DEVICE_TYPE, SIMULATOR_DEVICE_ID);
    		apiClient.deleteDeviceType(DEVICE_TYPE);
    	}
	}
	
	/**
	 * NegativeTest, pass quickstart value to the organization
	 */
	public void tesGatewayConnect01(){

		//Provide the device specific data using Properties class
		Properties options = new Properties();
		options.setProperty("org", "quickstart");
		options.setProperty("type", "iotsample-arduino");
		options.setProperty("id", "00aabbccde03");
				
		GatewayClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new GatewayClient(options);
		} catch (Exception e) {
			return;
		}
		fail("Gateway should not be supported in quickstart mode");
	}
	
	/**
	 * NegativeTest, pass null value to the organization
	 */
	public void tesGatewayConnect02(){

		//Provide the device specific data using Properties class
		Properties options = new Properties();
		options.setProperty("type", "iotsample-arduino");
		options.setProperty("id", "00aabbccde03");
				
		GatewayClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new GatewayClient(options);
		} catch (Exception e) {
			return;
		}
		fail("Gateway should not be supported in quickstart mode");
	}
	
	/**
	 * NegativeTest, pass null value to the organization
	 */
	public void tesGatewayConnect03(){

		//Provide the device specific data using Properties class
		Properties options = new Properties();
		options.setProperty("org", "hello");
		options.setProperty("type", "iotsample-arduino");
		options.setProperty("id", "00aabbccde03");
				
		GatewayClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new GatewayClient(options);
		} catch (Exception e) {
			return;
		}
		fail("Gateway should not be supported in quickstart mode");
	}
	
	
	/**
	 * This method creates a GatewayClient instance by passing the required properties 
	 * and connects the Gateway to the Watson IoT Platform by calling the connect function.
	 * 
	 * After the successful connection to the Watson IoT Platform, the Gateway can perform the following operations,
	 *   1. Publish events for itself and on behalf of devices connected behind the Gateway
	 *   2. Subscribe to commands for itself and on behalf of devices behind the Gateway
	 */
	private static void createGatewayClient(String fileName) {
		 /**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(GatewayEventPublishTest.class.getResourceAsStream(fileName));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		
		try {
			//Instantiate & connect the Gateway by passing the properties file
			gwClient = new GatewayClient(props);
			gwClient.connect();
			
			/**
			 * We need APIClient to register the devicetype in Watson IoT Platform 
			 */
			Properties options = new Properties();
			options.put("Organization-ID", props.getProperty("Organization-ID"));
			options.put("id", "app" + (Math.random() * 10000));		
			options.put("Authentication-Method","apikey");
			options.put("API-Key", props.getProperty("API-Key"));		
			options.put("Authentication-Token", props.getProperty("API-Token"));
			
			apiClient = new APIClient(options);
			
		} catch (Exception e) {
			// Looks like the gateway.property file is not updated with registeration details
			return;
		}
	}
	
	/**
	 * This sample adds a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	private void addDeviceType(String deviceType) throws IoTFCReSTException {
		try {
			System.out.println("<-- Checking if device type "+deviceType +" already created in Watson IoT Platform");
			boolean exist = apiClient.isDeviceTypeExist(deviceType);
			if (!exist) {
				System.out.println("<-- Adding device type "+deviceType + " now..");
				// device type to be created in WIoTP
				apiClient.addDeviceType(deviceType, deviceType, null, null);
			}
		} catch(IoTFCReSTException e) {
			System.err.println("ERROR: unable to add manually device type " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a device under the given gateway using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void addDevice(String deviceType, String deviceId) throws IoTFCReSTException {
		try {
			System.out.println("<-- Checking if device " + deviceId +" with deviceType " +
					deviceType +" exists in Watson IoT Platform");
			boolean exist = this.gwClient.api().isDeviceExist(deviceType, deviceId);
			if(!exist) {
				System.out.println("<-- Creating device " + deviceId +" with deviceType " +
						deviceType +" now..");
				gwClient.api().registerDeviceUnderGateway(deviceType, deviceId,
						this.gwClient.getGWDeviceType(), 
						this.gwClient.getGWDeviceId());
			}
		} catch (IoTFCReSTException ex) {
			
			System.out.println("ERROR: unable to add manually device " + deviceId);
		}
	}
	
	/**
	 * While the Gateway publishes events on behalf of the devices connected behind, the Gateway 
	 * can publish its own events as well. 
	 * 
	 * The test verifies whether the gateway can publish an event to the Platform
	 */
	public void testGatewayEventPublishMethod() {
		
		if(gwClient.isConnected() == false) {
			return;
		}
		
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
		
		boolean code = gwClient.publishGatewayEvent("blink", event, 2);
		assertTrue("Failed to publish the event......", code);
		
		// publish using default QoS
		code = gwClient.publishGatewayEvent("blink", event);
		assertTrue("Failed to publish the event......", code);
		
		// publish using default QoS0
		code = gwClient.publishGatewayEvent("blink", event, 0);
		assertTrue("Failed to publish the event......", code);
		
		// publish using default QoS0
		code = gwClient.publishGatewayEvent("blink", event, 1);
		assertTrue("Failed to publish the event......", code);
		
		gwClient.disconnect();
		code = gwClient.publishGatewayEvent("blink", event, 1);
		assertFalse("Should not publish an event after disconnect......", code);
		
		// try publish after disconnect
				
		System.out.println("Successfully published a Gateway event !!");
	}
	
	/**
	 * The test verifies whether the gateway can publish an event for the attached device to the Platform
	 */
	public void testDeviceEventPublishMethod() {
		
		if(gwClient.isConnected() == false) {
			return;
		}
		
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
		
		boolean code = gwClient.publishDeviceEvent(DEVICE_TYPE, SIMULATOR_DEVICE_ID, "blink", event, 2);
		assertTrue("Failed to publish the device event......", code);
		
		// Publish using default QoS
		code = gwClient.publishDeviceEvent(DEVICE_TYPE, SIMULATOR_DEVICE_ID, "blink", event);
		assertTrue("Failed to publish the device event......", code);
		
		// Publish using QoS0
		code = gwClient.publishDeviceEvent(DEVICE_TYPE, SIMULATOR_DEVICE_ID, "blink", event, 0);
		assertTrue("Failed to publish the device event......", code);

		// Publish using default QoS1
		code = gwClient.publishDeviceEvent(DEVICE_TYPE, SIMULATOR_DEVICE_ID, "blink", null, 1);
		assertTrue("Failed to publish the device event......", code);
				
		System.out.println("Successfully published a device event !!");
	}
	
	private static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}

}
