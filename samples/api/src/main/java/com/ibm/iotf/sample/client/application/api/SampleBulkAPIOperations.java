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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;

/**
 * This sample showcases various bulk ReST operations that can be performed on Watson IoT Platform.
 */
public class SampleBulkAPIOperations {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	// Example Json format to add a device
	/*
	 * {
		    "typeId": "SampleDT",
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
	private final static String deviceToBeAdded = "{\"typeId\": \"SampleDT\",\"deviceId\": "
			+ "\"RasPi100\",\"authToken\": \"password\",\"deviceInfo\": {\"serialNumber\": "
			+ "\"10087\",\"manufacturer\": \"IBM\",\"model\": \"7865\",\"deviceClass\": "
			+ "\"A\",\"description\": \"My RasPi01 Device\",\"fwVersion\": \"1.0.0\","
			+ "\"hwVersion\": \"1.0\",\"descriptiveLocation\": \"EGL C\"    },    "
			+ "\"location\": {\"measuredDateTime\": \"2015-23-07T11:23:23+00:00\"    "
			+ "},    \"metadata\": {}}";

	private final static String deviceToBeDeleted1 = "{\"typeId\": \"SampleDT\", \"deviceId\": \"RasPi100\"}";
	private final static String deviceToBeDeleted2 = "{\"typeId\": \"SampleDT\", \"deviceId\": \"RasPi101\"}";
	
	private APIClient apiClient = null;
	
	SampleBulkAPIOperations(String filePath) {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(SampleBulkAPIOperations.class.getResourceAsStream(PROPERTIES_FILE_NAME));
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
	
	public static void main(String[] args) throws Exception {
	
		SampleBulkAPIOperations sample = new SampleBulkAPIOperations(PROPERTIES_FILE_NAME);
		sample.getOrganizationDetails();
		sample.getAllDevices();
		sample.addDevices();
		sample.deleteDevices();
	}

	/**
	 * This sample showcases how to delete an array of devices.
	 * 
	 * Json Format to delete the device
	 * [
	 *	  {
	 *	    "typeId": "string",
	 *	    "deviceId": "string"
	 *	  }
	 *	]
	 * @throws Exception 
	 */
	private void deleteDevices() throws IoTFCReSTException {
		System.out.println("Deleting couple of devices");
		JsonElement device1 = new JsonParser().parse(deviceToBeDeleted1);
		JsonElement device2 = new JsonParser().parse(deviceToBeDeleted2);
		JsonArray arryOfDevicesToBeDeleted = new JsonArray();
		arryOfDevicesToBeDeleted.add(device1);
		arryOfDevicesToBeDeleted.add(device2);
		try {
			JsonArray devices = this.apiClient.deleteMultipleDevices(arryOfDevicesToBeDeleted);
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

	/**
	 * This sample showcases how to add array of devices to IBM Watson IoT Platform.
	 * @throws Exception
	 */
	private void addDevices() throws IoTFCReSTException {
		System.out.println("Adding couple of devices");
		JsonElement input = new JsonParser().parse(deviceToBeAdded);
		JsonArray arryOfDevicesToBeAdded = new JsonArray();
		arryOfDevicesToBeAdded.add(input);
		try {
			JsonArray devices = this.apiClient.addMultipleDevices(arryOfDevicesToBeAdded);
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

	/**
	 * This sample showcases how to get array of devices registered in the organization, sorted by deviceId.
	 * @throws Exception
	 */
	private void getAllDevices() throws IoTFCReSTException {
		System.out.println("Retrieve all devices in the Organization..");
		// Get all the devices in the organization
		/**
		 * The Java ibmiotf client library provides an one argument constructor
		 * which can be used to control the output, for example, lets try to retrieve
		 * the devices in a sorted order based on device ID.
		 */
		
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("_sort","deviceId"));
		//parameters.add(new BasicNameValuePair("_limit","2"));
		JsonObject response = this.apiClient.getAllDevices(parameters);
		
		System.out.println(response);
		// The response will contain more parameters that will be used to issue
		// the next request. The result element will contain the current list of devices
		JsonArray devices = response.get("results").getAsJsonArray(); 
		for(Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
			JsonElement deviceElement = iterator.next();
			JsonObject responseJson = deviceElement.getAsJsonObject();
			System.out.println(responseJson);
		}	
	}

	/**
	 * This sample showcases how to get details of the organization
	 * @throws Exception
	 */
	private void getOrganizationDetails() throws IoTFCReSTException {
		System.out.println("Retrieve Organization details...");
		// Get the organization detail
		JsonObject orgDetail = this.apiClient.getOrganizationDetails();
		System.out.println(orgDetail);
	}
}