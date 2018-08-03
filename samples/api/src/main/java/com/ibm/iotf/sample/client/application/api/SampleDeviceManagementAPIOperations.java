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
import java.util.Iterator;
import java.util.Properties;
import java.util.ArrayList;

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
 * initiate/get/delete one or more device management operations.
 */
public class SampleDeviceManagementAPIOperations {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
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
	private static final String DEVICE_TYPE = "SampleDT";
	private static final String DEVICE_ID = "RasPi100";
	private static final String rebootRequestToBeInitiated = "{\"action\": \"device/reboot\","
			+ "\"devices\": [ {\"typeId\": \"" + DEVICE_TYPE +"\","
			+ "\"deviceId\": \"" + DEVICE_ID + "\"}]}";

	private APIClient apiClient = null;
	
	SampleDeviceManagementAPIOperations(String filePath) {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(SampleDeviceManagementAPIOperations.class.getResourceAsStream(PROPERTIES_FILE_NAME));
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
		SampleDeviceManagementAPIOperations sample = new SampleDeviceManagementAPIOperations(PROPERTIES_FILE_NAME);
		
		//add if device/type is not present in the Organization
		sample.addDevice();
		sample.initiateMgmtRequest();
		sample.getAllMgmtRequests();
		sample.initiateMgmtRequest();
		sample.deleteMgmtRequest();
		sample.getMgmtRequest();
		sample.getMgmtRequestDeviceStatus();
	}
	
	/**
	 * This method adds a device & device type if its not added already 
	 * @throws IoTFCReSTException
	 */
	private void addDevice() throws IoTFCReSTException {
		try {
			boolean status = this.apiClient.isDeviceTypeExist(DEVICE_TYPE);
			if(status == false) {
				System.out.println("Adding device Type --> "+DEVICE_TYPE);
				this.apiClient.addDeviceType(DEVICE_TYPE, DEVICE_TYPE, null, null);
				System.out.println("Adding device "+DEVICE_ID+" under type "+DEVICE_TYPE);
				this.apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID, "password", null, null, null);
			} else if (!apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
				System.out.println("Adding device "+DEVICE_ID+" under type "+DEVICE_TYPE);
				this.apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID, "password", null, null, null);
			}
			return;
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}

	/**
	 * This sample showcases how to get a list of device management requests, which can be in progress or recently completed.
	 * @throws IoTFCReSTException
	 */
	private void getAllMgmtRequests() throws IoTFCReSTException {
		System.out.println("Retrieve all DM requests from the organization..");
		try {
			JsonElement response = this.apiClient.getAllDeviceManagementRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			for(Iterator<JsonElement> iterator = requests.iterator(); iterator.hasNext(); ) {
				JsonElement request = iterator.next();
				JsonObject responseJson = request.getAsJsonObject();
				System.out.println(responseJson);
			}
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
	}
	
	/**
	 * This sample showcases how to initiate a device management request, such as reboot.
	 * @throws IoTFCReSTException
	 */
	private void initiateMgmtRequest() throws IoTFCReSTException {
		System.out.println("Initiate reboot request .. "+rebootRequestToBeInitiated);
		try {
			JsonObject reboot = (JsonObject) new JsonParser().parse(rebootRequestToBeInitiated);
			boolean response = this.apiClient.initiateDeviceManagementRequest(reboot);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
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
		System.out.println("Delete a DM request from the organization..");
		// Lets clear the first ID from the list
		try {
			JsonElement response = this.apiClient.getAllDeviceManagementRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			JsonElement request = requests.get(0);
			System.out.println("Delete a DM request .. "+request.getAsJsonObject().get("id").getAsString());
			boolean status = this.apiClient.deleteDeviceManagementRequest(request.getAsJsonObject().get("id").getAsString());
			System.out.println("Delete status: "+status);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
	}
	
	/**
	 * This sample showcases how to get details of a device management request.
	 * @throws IoTFCReSTException
	 */
	private void getMgmtRequest() throws IoTFCReSTException {
		System.out.println("Retrieve a DM request from the organization..");
		// Lets clear the first ID from the list
		try {
			JsonElement response = this.apiClient.getAllDeviceManagementRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			JsonElement request = requests.get(0);
			System.out.println("Get a DM request .. "+request.getAsJsonObject().get("id").getAsString());
			JsonObject details = this.apiClient.getDeviceManagementRequest(request.getAsJsonObject().get("id").getAsString());
			System.out.println(details);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
	}
	
	/**
	 * This sample showcases how to get list of device management request device statuses
	 * @throws IoTFCReSTException
	 */
	private void getMgmtRequestDeviceStatus() throws IoTFCReSTException {
		// Lets get the DM request status from the list
		System.out.println("Get DM request device status..");
		try {
			JsonElement response = this.apiClient.getAllDeviceManagementRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			JsonElement request = requests.get(0);
			String id = request.getAsJsonObject().get("id").getAsString();
			
			ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		    parameters.add(new BasicNameValuePair("_bookmark","<bookmark>"));
		    
		    
			JsonObject details = this.apiClient.getDeviceManagementRequestStatus(id);
			
			// The response will contain more parameters that will be used to issue
			// the next request. The results element will contain the current list of devices
			JsonArray devices = details.get("results").getAsJsonArray(); 
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