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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.sample.util.Utility;

/**
 * This sample showcases various ReST operations that can be performed on IoT Foundation to
 * initiate/get/delete one or more device management operations.
 */
public class SampleDeviceManagementAPIOperations {
	
	private final static String PROPERTIES_FILE_NAME = "application.prop";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";
	
	// Example Json format to add a device
	/*
	 * {
 	 *	"action": "device/reboot",
 	 *	"devices": [
 	 *		{
 	 *		"typeId": "SampleDT1",
 	 *		"deviceId": "RasPi101"
 	 *		}
 	 *	]
	 * }
	 */
	
	private static final String rebootRequestToBeInitiated = "{\"action\": \"device/reboot\",\"devices\": "
			+ "[ {\"typeId\": \"SampleDT1\",\"deviceId\": "
			+ "\"RasPi101\"}]}";

	private APIClient apiClient = null;
	
	SampleDeviceManagementAPIOperations(String filePath) {
		
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
	
	public static void main(String[] args) throws IoTFCReSTException {
	
		String fileName = null;
		if (args.length == 1) {
			fileName = args[0];
		} else {
			fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);
		}
		
		SampleDeviceManagementAPIOperations sample = new SampleDeviceManagementAPIOperations(fileName);
		sample.getMgmtRequests();
		sample.initiateMgmtRequest();
		sample.deleteMgmtRequest();
		sample.getMgmtRequest();
	}

	/**
	 * This sample showcases how to get a list of device management requests, which can be in progress or recently completed.
	 * @throws IoTFCReSTException
	 */
	private void getMgmtRequests() throws IoTFCReSTException {
		try {
			JsonElement response = this.apiClient.getMgmtRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			for(Iterator<JsonElement> iterator = requests.iterator(); iterator.hasNext(); ) {
				JsonElement request = iterator.next();
				JsonObject responseJson = request.getAsJsonObject();
				System.out.println(responseJson);
			}
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}
	
	/**
	 * This sample showcases how to initiate a device management request, such as reboot.
	 * @throws IoTFCReSTException
	 */
	private void initiateMgmtRequest() throws IoTFCReSTException {
		try {
			JsonObject reboot = (JsonObject) new JsonParser().parse(rebootRequestToBeInitiated);
			boolean response = this.apiClient.initiateMgmtRequest(reboot);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * Clears the status of a device management request. The status for a 
	 * request that has been completed is automatically cleared soon after 
	 * the request completes. You can use this operation to clear the status 
	 * for a completed request, or for an in-progress request which may never 
	 * complete due to a problem.
	 * 
	 * @throws IoTFCReSTException
	 */
	private void deleteMgmtRequest() throws IoTFCReSTException {
		// Lets clear the first ID from the list
		try {
			JsonElement response = this.apiClient.getMgmtRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			JsonElement request = requests.get(0);
			boolean status = this.apiClient.deleteMgmtRequest(request.getAsJsonObject().get("id").getAsString());
			System.out.println("Delete status: "+status);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}
	
	/**
	 * This sample showcases how to get details of a device management request.
	 * @throws IoTFCReSTException
	 */
	private void getMgmtRequest() throws IoTFCReSTException {
		// Lets clear the first ID from the list
		try {
			JsonElement response = this.apiClient.getMgmtRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			JsonElement request = requests.get(0);
			JsonObject details = this.apiClient.getMgmtRequest(request.getAsJsonObject().get("id").getAsString());
			System.out.println(details);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}
	
}