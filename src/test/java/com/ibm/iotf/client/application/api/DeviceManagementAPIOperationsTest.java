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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
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
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.util.LoggerUtility;

import junit.framework.TestCase;

/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * initiate/get/delete one or more device management operations.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceManagementAPIOperationsTest {
	
	private static final String CLASS_NAME = DeviceManagementAPIOperationsTest.class.getName();

	private static final String APP_ID = "DMApp1";
	private static final String DEVICE_TYPE = "DMType1";
	private static final String DEVICE_ID = "DMDev1";
	private static final String rebootRequestToBeInitiated = "{\"action\": \"device/reboot\","
			+ "\"devices\": [ {\"typeId\": \"" + DEVICE_TYPE +"\","
			+ "\"deviceId\": \"" + DEVICE_ID + "\"}]}";

	private static APIClient apiClient = null;
	private static ManagedDevice managedDevice = null;
	
	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, DEVICE_ID);
		apiClient = new APIClient(appProps);

		// Delete device if it was left from the last test run
		if (apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
		}
		
		// If the device type does not exist, create it
		if (apiClient.isDeviceTypeExist(DEVICE_TYPE) == false) {
			apiClient.addDeviceType(DEVICE_TYPE, null, null, null);
		}
		
		// Register the test device DEVICE_ID
		apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID, TestEnv.getDeviceToken(), null, null, null);

		Properties devProps = TestEnv.getDeviceProperties(DEVICE_TYPE, DEVICE_ID);

		DeviceFirmware firmware = new DeviceFirmware.Builder().
				version("1.0.1").
				name("iot-arm.deb").
				url("").
				verifier("12345").
				state(FirmwareState.IDLE).				
				build();
		
		JsonObject data = new JsonObject();
		data.addProperty("customField", "customValue");
		
		DeviceData deviceData = new DeviceData.Builder().
						 deviceFirmware(firmware).
						 build();
		managedDevice = new ManagedDevice(devProps, deviceData);
	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {
		
		if (apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
		}
		
		if (apiClient.isDeviceTypeExist(DEVICE_TYPE)) {
			apiClient.deleteDeviceType(DEVICE_TYPE);
		}
	}
	

	/**
	 * This sample showcases how to initiate a device management request, such as reboot.
	 * @throws Exception 
	 */
	@Test
	public void test01initiateMgmtRequest() throws Exception {
		final String METHOD = "test01AddDiagnosticLog";
		LoggerUtility.info(CLASS_NAME, METHOD, 
				"Initiate reboot request .. " + rebootRequestToBeInitiated);
		managedDevice.connect();
		managedDevice.sendManageRequest(0, false, true);
		try {
			JsonObject reboot = (JsonObject) new JsonParser().parse(rebootRequestToBeInitiated);
			boolean response = apiClient.initiateDeviceManagementRequest(reboot);
			System.out.println(response);
			assertTrue("Not able to initiate DM request", response);
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		} finally {
			managedDevice.disconnect();
		}
	}
	
	/**
	 * This sample showcases how to get details of a device management request.
	 * @throws IoTFCReSTException
	 */
	@Test
	public void test02getMgmtRequest() throws IoTFCReSTException {
		final String METHOD = "test02getMgmtRequest";
		LoggerUtility.info(CLASS_NAME, METHOD, 
				"Retrieve a DM request from the organization..");
		// Lets clear the first ID from the list
		try {
			JsonElement response = apiClient.getAllDeviceManagementRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			JsonElement request = requests.get(0);
			LoggerUtility.info(CLASS_NAME, METHOD,
					"Get a DM request .. "+request.getAsJsonObject().get("id").getAsString());
			JsonObject details = apiClient.getDeviceManagementRequest(request.getAsJsonObject().get("id").getAsString());
			LoggerUtility.info(CLASS_NAME, METHOD,
					details.toString());
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		}
		
	}
	
	/**
	 * This sample showcases how to get list of device management request device statuses
	 * @throws IoTFCReSTException
	 */
	@Test
	public void test03getMgmtRequestDeviceStatus() throws IoTFCReSTException {
		final String METHOD = "test03getMgmtRequestDeviceStatus";
		LoggerUtility.info(CLASS_NAME, METHOD, 
				"Get DM request device status..");
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
				LoggerUtility.info(CLASS_NAME, METHOD, responseJson.toString());
			}

		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		}
	}
	
	/**
	 * This sample showcases how to get list of device management request device statuses
	 * @throws IoTFCReSTException
	 */
	@Test
	public void test031getMgmtRequestDeviceStatus() throws IoTFCReSTException {
		final String METHOD = "test031getMgmtRequestDeviceStatus";
		LoggerUtility.info(CLASS_NAME, METHOD, 
				"Get DM request device status..");
		try {
			JsonElement response = apiClient.getAllDeviceManagementRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			JsonElement request = requests.get(0);
			String id = request.getAsJsonObject().get("id").getAsString();
			
			JsonObject details = apiClient.getDeviceManagementRequestStatusByDevice(id, DEVICE_TYPE, DEVICE_ID);
			LoggerUtility.info(CLASS_NAME, METHOD, details.toString());
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		}
		
	}
	
	/**
	 * This sample showcases how to get a list of device management requests, which can be in progress or recently completed.
	 * @throws IoTFCReSTException
	 */
	@Test
	public void test04getAllMgmtRequests() throws IoTFCReSTException {
		final String METHOD = "test04getAllMgmtRequests";
		LoggerUtility.info(CLASS_NAME, METHOD, 
				"Retrieve all DM requests from the organization..");
		try {
			JsonElement response = apiClient.getAllDeviceManagementRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			for(Iterator<JsonElement> iterator = requests.iterator(); iterator.hasNext(); ) {
				JsonElement request = iterator.next();
				JsonObject responseJson = request.getAsJsonObject();
				LoggerUtility.info(CLASS_NAME, METHOD, responseJson.toString());
			}
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
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
	@Test
	public void test05deleteMgmtRequest() throws IoTFCReSTException {
		final String METHOD = "test05deleteMgmtRequest";
		LoggerUtility.info(CLASS_NAME, METHOD, 
				"Delete a DM request");
		try {
			JsonElement response = apiClient.getAllDeviceManagementRequests();
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			JsonElement request = requests.get(0);
			LoggerUtility.info(CLASS_NAME, METHOD, "Delete a DM request .. "+request.getAsJsonObject().get("id").getAsString());
			boolean status = apiClient.deleteDeviceManagementRequest(request.getAsJsonObject().get("id").getAsString());
			LoggerUtility.info(CLASS_NAME, METHOD, "Delete status: "+status);
			assertTrue("Fail to delete the DM request", status);
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		}
		
	}	

}