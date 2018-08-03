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

package com.ibm.iotf.sample.client.gateway;

import java.io.IOException;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.gateway.GatewayClient;

/**
 * <p>The Gateway sample uses the com.ibm.iotf.client.gateway.GatewayClient class 
 * from the Watson IoT Platform Java Client Library 
 * that simplifies the Gateway interactions with IBM Watson IoT Platform. </p>
 * 
 */
public class SimpleGatewayExample {
	
	// Properties file containing the Registration details of the gateway, to connect to Watson IoT Platform
	// Present in the maven resource directory
	private final static String PROPERTIES_FILE_NAME = "/gateway.properties";
	
	private final static String DEVICE_TYPE = "iotsampleType2";
	private final static String SIMULATOR_DEVICE_ID = "Arduino0301";
	
	private GatewayClient gwClient = null;
	SystemObject obj = new SystemObject();
	private APIClient apiClient;	
	
	public SimpleGatewayExample() {
		
	}
	
	/**
	 * This method creates a GatewayClient instance by passing the required properties 
	 * and connects the Gateway to the Watson IoT Platform by calling the connect function.
	 * 
	 * After the successful connection to the Watson IoT Platform, the Gateway can perform the following operations,
	 *   1. Publish events for itself and on behalf of devices connected behind the Gateway
	 *   2. Subscribe to commands for itself and on behalf of devices behind the Gateway
	 */
	private void createGatewayClient(String fileName) {
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
			
			this.apiClient = new APIClient(options);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * While the Gateway publishes events on behalf of the devices connected behind, the Gateway 
	 * can publish its own events as well. 
	 * 
	 * The sample publishes a blink event every second, that has the CPU and memory utilization of 
	 * this sample Gateway process.
	 */
	private void publishGatewayEvent() {
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", SystemObject.getName());
		try {
			event.addProperty("cpu",  obj.getProcessCpuLoad());
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		}
		event.addProperty("mem",  obj.getMemoryUsed());
			
		gwClient.publishGatewayEvent("blink", event, 2);
		System.out.println("Publish Gateway event:: "+event);
	}
	
	/**
	 * The method publishes a blink event every second, that has the CPU and memory utilization of 
	 * this sample Gateway process.
	 */
	private void publishDeviceEvent() {
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", SystemObject.getName() + "simulator");
		try {
			event.addProperty("cpu",  obj.getProcessCpuLoad());
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		}
		event.addProperty("mem",  obj.getMemoryUsed());
			
		gwClient.publishDeviceEvent(DEVICE_TYPE, SIMULATOR_DEVICE_ID, "blink", event, 2);
		System.out.println("Publish Device event:: "+event);
	}
	
	private void disconnect() {
		//Disconnect cleanly
		gwClient.disconnect();
	}
	
	/**
	 * This sample adds a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	private void addDeviceType(String deviceType) throws IoTFCReSTException {
		try {
			System.out.println("<-- Checking if device type "+deviceType +" already created in Watson IoT Platform");
			boolean exist = false;
			
			try {
				exist = apiClient.isDeviceTypeExist(deviceType);
			} catch(Exception e) {}
			
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
		
		System.out.println("<-- Checking if device " + deviceId +" with deviceType " +
					deviceType +" exists in Watson IoT Platform");
		boolean exist = false;
		try {
			exist = this.gwClient.api().isDeviceExist(deviceType, deviceId);
		} catch(Exception e) {}
		
		try {
			if(!exist) {
				System.out.println("<-- Creating device " + deviceId +" with deviceType " +
						deviceType +" now..");
				this.apiClient.registerDeviceUnderGateway(deviceType, deviceId,
						this.gwClient.getGWDeviceType(), 
						this.gwClient.getGWDeviceId());
			}
		} catch (IoTFCReSTException ex) {
			
			System.out.println("ERROR: unable to add manually device " + deviceId);
			ex.printStackTrace();
		}
	}	
	

	public static void main(String[] args) throws IoTFCReSTException {
		
		SimpleGatewayExample sample = new SimpleGatewayExample();
		sample.createGatewayClient(PROPERTIES_FILE_NAME);
		sample.addDeviceType(DEVICE_TYPE);
		sample.addDevice(DEVICE_TYPE, SIMULATOR_DEVICE_ID);

		System.out.println("Gateway Started");
		
		/**
		 * Try to publish a Gateway Event for every second. As like devices, the Gateway
		 * also can have attached sensors and publish events.
		 */
		while(true) {
			sample.publishGatewayEvent();
			sample.publishDeviceEvent();
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ie) {}
		}
		
		//sample.disconnect();
		
	}
}
