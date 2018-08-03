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
package com.ibm.iotf.sample.client.application.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;

/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/delete device(s)
 */
public class SampleDeviceAPIOperations {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private static final String DEVICE_TYPE = "SampleDT";
	private static final String DEVICE_ID = "RasPi01";
	
	// Example Json format to add a device
	/*
	 * {
		    "typeId": "ManagedDT",
		    "deviceId": "RasPi100",
		    "authToken": "password",
		    "deviceInfo": {
		        "serialNumber": "10087",
		        "manufacturer": "IBM",
		        "model": "7865",
		        "deviceClass": "A",
		        "description": "My RasPi01 Device",
		        "fwVersion": "1.0.0",
		        "hwVersion": "1.0",
		        "descriptiveLocation": "EGL C"
		    },
		    "location": {
		        "measuredDateTime": "2015-23-07T11:23:23+00:00"
		    },
		    "metadata": {}
		}
	 */
	
	/**
	 * Split the elements into multiple lines, so that we can showcase the use of multiple constructors
	 */
	private final static String locationToBeAdded = "{\"longitude\": 0, \"latitude\": 0, \"elevation\": "
			+ "0,\"measuredDateTime\": \"2015-23-07T11:23:23+00:00\"}";
	
	private final static String newlocationToBeAdded = "{\"longitude\": 10, \"latitude\": 20, \"elevation\": 0}";
	
	
	private final static String deviceInfoToBeAdded = "{\"serialNumber\": "
			+ "\"10087\",\"manufacturer\": \"IBM\",\"model\": \"7865\",\"deviceClass\": "
			+ "\"A\",\"description\": \"My RasPi100 Device\",\"fwVersion\": \"1.0.0\","
			+ "\"hwVersion\": \"1.0\",\"descriptiveLocation\": \"EGL C\"}";
	
	private final static String deviceToBeAdded = "{\"deviceId\": "
			+ "\"RasPi100\",\"authToken\": \"password\"," + 
			"\"location\": " + locationToBeAdded + "," 
			+ "\"deviceInfo\": " + deviceInfoToBeAdded + "," 
			+ "\"metadata\": {}}";
	

	private APIClient apiClient = null;
	
	SampleDeviceAPIOperations(String filePath) {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(SampleDeviceAPIOperations.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		try {
			//Instantiate the class by passing the properties file
			this.apiClient = new APIClient(props);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) throws IoTFCReSTException {
	
		SampleDeviceAPIOperations sample = new SampleDeviceAPIOperations(PROPERTIES_FILE_NAME);

		// check whether devicetype is created already, otherwise create it
		sample.addDeviceType();
		sample.addDevice();
		sample.getAllDevices();
		sample.getDevice();
		
		sample.updateDevice();
		sample.getDeviceLocation();
		sample.updateDeviceLocation();
		sample.getDeviceManagementInformation();
		sample.deleteDevice();
	}
	
	/**
	 * This sample showcases how to Create a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	private void addDeviceType() throws IoTFCReSTException {
		try {
			boolean status = this.apiClient.isDeviceTypeExist(DEVICE_TYPE);
			if(status == false) {
				System.out.println("Adding device Type --> "+DEVICE_TYPE);
				this.apiClient.addDeviceType(DEVICE_TYPE, DEVICE_TYPE, null, null);
			}
			return;
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}

	/**
	 * This sample showcases how to Delete a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void deleteDevice() throws IoTFCReSTException {
		try {
			System.out.println("Deleting devices --> RasPi100 and "+DEVICE_ID);
			boolean status = this.apiClient.deleteDevice(DEVICE_TYPE, "RasPi100");
			System.out.println(status);
			status = this.apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
			System.out.println(status);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
	}

	/**
	 * This sample showcases how to add a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void addDevice() throws IoTFCReSTException {
		System.out.println("Adding device --> "+deviceToBeAdded);
		JsonParser parser = new JsonParser();
		try{
			JsonElement input = parser.parse(deviceToBeAdded);
			JsonObject response = this.apiClient.registerDevice(DEVICE_TYPE, input);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		try{
			
			// Lets add device with different API that accepts more args,
			
			JsonElement deviceInfo = parser.parse(deviceInfoToBeAdded);
			JsonElement location = parser.parse(locationToBeAdded);
			
			System.out.println("Adding device --> "+DEVICE_ID);
			JsonObject response = this.apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID, "Password", 
					deviceInfo, location, null);
			
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to get device details using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void getDevice() throws IoTFCReSTException {
		try {
			System.out.println("get device --> "+DEVICE_ID);
			JsonObject response = this.apiClient.getDevice(DEVICE_TYPE, DEVICE_ID);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to get a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void getDeviceLocation() throws IoTFCReSTException {
		try {
			System.out.println("get device location of device --> "+DEVICE_ID);
			JsonObject response = this.apiClient.getDeviceLocation(DEVICE_TYPE, DEVICE_ID);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to update a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void updateDeviceLocation() throws IoTFCReSTException {
		System.out.println("update device location of device --> "+DEVICE_ID);
		try {
			JsonElement newLocation = new JsonParser().parse(newlocationToBeAdded);
			JsonObject response = this.apiClient.updateDeviceLocation(DEVICE_TYPE, DEVICE_ID, newLocation);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to get a management information of a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void getDeviceManagementInformation() throws IoTFCReSTException {
		System.out.println("get device management information of device --> "+DEVICE_ID);
		try {
			JsonObject response = this.apiClient.getDeviceManagementInformation(DEVICE_TYPE, DEVICE_ID);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}

	/**
	 * This sample showcases how to update a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void updateDevice() throws IoTFCReSTException {
		try {
			System.out.println("update device --> "+DEVICE_ID);
			JsonObject metadata = new JsonObject();
			metadata.addProperty("Hi", "Hello, I'm updated metadata");
		
			JsonObject updatedMetadata = new JsonObject();
			updatedMetadata.add("metadata", metadata);
			JsonObject response = this.apiClient.updateDevice(DEVICE_TYPE, DEVICE_ID, updatedMetadata);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}

	/**
	 * This sample showcases how to retrieve all the devices in an organization using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void getAllDevices() throws IoTFCReSTException {
		System.out.println("Get all devices of device type--> "+DEVICE_TYPE);
		// Get all the devices of type SampleDT
		try {
			/**
			 * The Java ibmiotf client library provides an one argument constructor
			 * which can be used to control the output, for example, lets try to retrieve
			 * the devices in a sorted order based on device ID.
			 */
			
			ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("_sort","deviceId"));
			
			JsonObject response = this.apiClient.retrieveDevices(DEVICE_TYPE, parameters);
			
			// The response will contain more parameters that will be used to issue
			// the next request. The result element will contain the current list of devices
			JsonArray devices = response.get("results").getAsJsonArray(); 
			for(Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				System.out.println(responseJson);
			}
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
}