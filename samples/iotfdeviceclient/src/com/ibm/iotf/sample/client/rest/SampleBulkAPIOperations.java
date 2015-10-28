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

package com.ibm.iotf.sample.client.rest;

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
import com.ibm.iotf.sample.util.Utility;

/**
 * This sample showcases various bulk ReST operations that can be performed on IoT Foundation.
 */
public class SampleBulkAPIOperations {
	
	private final static String PROPERTIES_FILE_NAME = "application.prop";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";
	
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
	private final static String deviceToBeAdded = "{\"typeId\": \"ManagedDT\",\"deviceId\": "
			+ "\"RasPi100\",\"authToken\": \"password\",\"deviceInfo\": {\"serialNumber\": "
			+ "\"10087\",\"manufacturer\": \"IBM\",\"model\": \"7865\",\"deviceClass\": "
			+ "\"A\",\"description\": \"My RasPi01 Device\",\"fwVersion\": \"1.0.0\","
			+ "\"hwVersion\": \"1.0\",\"descriptiveLocation\": \"EGL C\"    },    "
			+ "\"location\": {\"measuredDateTime\": \"2015-23-07T11:23:23+00:00\"    "
			+ "},    \"metadata\": {}}";

	private final static String deviceToBeDeleted = "{\"typeId\": \"ManagedDT\", \"deviceId\": \"RasPi100\"}";
	private APIClient apiClient = null;
	
	SampleBulkAPIOperations(String filePath) {
		
		/**
		 * Load properties file "application.prop"
		 */
		Properties props = Utility.loadPropertiesFile(PROPERTIES_FILE_NAME, filePath);
		try {
			//Instantiate the class by passing the properties file
			this.apiClient = new APIClient(props);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) throws Exception {
	
		String fileName = null;
		if (args.length == 1) {
			fileName = args[0];
		} else {
			fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);
		}
		
		SampleBulkAPIOperations sample = new SampleBulkAPIOperations(fileName);
		
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
		JsonElement input = new JsonParser().parse(deviceToBeDeleted);
		JsonArray arryOfDevicesToBeDeleted = new JsonArray();
		arryOfDevicesToBeDeleted.add(input);
		try {
			JsonArray devices = this.apiClient.bulkDevicesRemove(arryOfDevicesToBeDeleted);
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
	 * This sample showcases how to add array of devices to IBM IoT Foundation.
	 * @throws Exception
	 */
	private void addDevices() throws IoTFCReSTException {
		JsonElement input = new JsonParser().parse(deviceToBeAdded);
		JsonArray arryOfDevicesToBeAdded = new JsonArray();
		arryOfDevicesToBeAdded.add(input);
		try {
			JsonArray devices = this.apiClient.bulkDevicesAdd(arryOfDevicesToBeAdded);
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
		// Get all the devices in the organization
		/**
		 * The Java ibmiotf client library provides an one argument constructor
		 * which can be used to control the output, for example, lets try to retrieve
		 * the devices in a sorted order based on device ID.
		 */
		
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("_sort","deviceId"));
		JsonObject response = this.apiClient.getDevices(parameters);
		
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
		// Get the organization detail
		JsonObject orgDetail = this.apiClient.getOrganizationDetails();
		System.out.println(orgDetail);
	}
}