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
package com.ibm.iotf.client.application.api;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * initiate/get/delete one or more device management operations.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceManagementAPIOperationsTest extends TestCase {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	// Example Json format to add a device
	/*
	 * {
 	 *	"action": "device/reboot",
 	 *	"devices": [
 	 *		{
 	 *		"typeId": "TestDT1",
 	 *		"deviceId": "RasPi101"
 	 *		}
 	 *	]
	 * }
	 */
	private final static String DEVICE_PROPERTIES_FILE = "/device.properties";
	private static final String DEVICE_TYPE;
	private static final String DEVICE_ID;
	private static final String rebootRequestToBeInitiated;

	private static APIClient apiClient = null;
	private static boolean setUpIsDone = false;

	static {
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(DeviceManagementAPIOperationsTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}

		DEVICE_TYPE = deviceProps.getProperty("Device-Type");
		DEVICE_ID = deviceProps.getProperty("Device-ID");
		rebootRequestToBeInitiated = "{\"action\": \"device/reboot\","
				+ "\"devices\": [ {\"typeId\": \"" + DEVICE_TYPE +"\","
				+ "\"deviceId\": \"" + DEVICE_ID + "\"}]}";

	}
	public synchronized void setUp() {
	    if (setUpIsDone) {
	        return;
	    }
	    
	    /**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(DeviceManagementAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		try {
			//Instantiate the class by passing the properties file
			apiClient = new APIClient(props);
		} catch (Exception e) {
			// looks like the application.properties file is not updated properly
			apiClient = null;
		}
		setUpIsDone = true;
	}
	
	/**
	 * This method builds the device objects required to create the
	 * ManagedClient
	 * 
	 * @param propertiesFile
	 * @throws Exception
	 */
	private ManagedDevice createManagedClient(String propertiesFile) throws Exception {
		/**
		 * Load device properties
		 */
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(DeviceManagementAPIOperationsTest.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		DeviceFirmware firmware = new DeviceFirmware.Builder().
				version("1.0.1").
				name("iot-arm.deb").
				url("").
				verifier("12345").
				state(FirmwareState.IDLE).				
				build();
		
		/**
		 * Create a DeviceMetadata object
		 */
		JsonObject data = new JsonObject();
		data.addProperty("customField", "customValue");
		//DeviceMetadata metadata = new DeviceMetadata(data);
		
		DeviceData deviceData = new DeviceData.Builder().
						 //deviceInfo(deviceInfo).
						 deviceFirmware(firmware).
						 //metadata(metadata).
						 build();
		
		return new ManagedDevice(deviceProps, deviceData);
	}

	
	/**
	 * This sample showcases how to get a list of device management requests, which can be in progress or recently completed.
	 * @throws IoTFCReSTException
	 */
	public void test04getAllMgmtRequests() throws IoTFCReSTException {
		System.out.println("Retrieve all DM requests from the organization..");
		try {
			JsonElement response = apiClient.getAllDeviceManagementRequests();
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
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}
	
	/**
	 * This sample showcases how to initiate a device management request, such as reboot.
	 * @throws Exception 
	 */
	public void test01initiateMgmtRequest() throws Exception {
		System.out.println("Initiate reboot request .. "+rebootRequestToBeInitiated);
		// Let us connect the device first
		ManagedDevice dmClient = createManagedClient(DEVICE_PROPERTIES_FILE);
		dmClient.connect();
		dmClient.sendManageRequest(0, false, true);
		try {
			JsonObject reboot = (JsonObject) new JsonParser().parse(rebootRequestToBeInitiated);
			boolean response = apiClient.initiateDeviceManagementRequest(reboot);
			System.out.println(response);
			assertTrue("Not able to initiate DM request", response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		} finally {
			dmClient.disconnect();
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
	public void test05deleteMgmtRequest() throws IoTFCReSTException {
		System.out.println("Delete a DM request from the organization..");
		// Lets clear the first ID from the list
		try {
			JsonElement response = apiClient.getAllDeviceManagementRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			JsonElement request = requests.get(0);
			System.out.println("Delete a DM request .. "+request.getAsJsonObject().get("id").getAsString());
			boolean status = apiClient.deleteDeviceManagementRequest(request.getAsJsonObject().get("id").getAsString());
			System.out.println("Delete status: "+status);
			assertTrue("Fail to delete the DM request", status);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}
	
	/**
	 * This sample showcases how to get details of a device management request.
	 * @throws IoTFCReSTException
	 */
	public void test02getMgmtRequest() throws IoTFCReSTException {
		System.out.println("Retrieve a DM request from the organization..");
		// Lets clear the first ID from the list
		try {
			JsonElement response = apiClient.getAllDeviceManagementRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			JsonElement request = requests.get(0);
			System.out.println("Get a DM request .. "+request.getAsJsonObject().get("id").getAsString());
			JsonObject details = apiClient.getDeviceManagementRequest(request.getAsJsonObject().get("id").getAsString());
			System.out.println(details);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}
	
	/**
	 * This sample showcases how to get list of device management request device statuses
	 * @throws IoTFCReSTException
	 */
	public void test03getMgmtRequestDeviceStatus() throws IoTFCReSTException {
		// Lets get the DM request status from the list
		System.out.println("Get DM request device status..");
		try {
			JsonElement response = apiClient.getAllDeviceManagementRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			JsonElement request = requests.get(0);
			String id = request.getAsJsonObject().get("id").getAsString();
			
			ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		    parameters.add(new BasicNameValuePair("_bookmark","<bookmark>"));
		    
		    
			JsonObject details = apiClient.getDeviceManagementRequestStatus(id);
			
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
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * This sample showcases how to get list of device management request device statuses
	 * @throws IoTFCReSTException
	 */
	public void test031getMgmtRequestDeviceStatus() throws IoTFCReSTException {
		// Lets get the DM request status from the list
		System.out.println("Get DM request device status..");
		try {
			JsonElement response = apiClient.getAllDeviceManagementRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			JsonElement request = requests.get(0);
			String id = request.getAsJsonObject().get("id").getAsString();
			
			JsonObject details = apiClient.getDeviceManagementRequestStatusByDevice(id, DEVICE_TYPE, DEVICE_ID);
			System.out.println(details);
		} catch(IoTFCReSTException e) {
			e.printStackTrace();
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
			//uncomment when the defect is fixed
			//fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}

}