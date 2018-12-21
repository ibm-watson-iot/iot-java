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

import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

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
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.util.LoggerUtility;

/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/delete device diagnostics.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceDiagnosticsAPIOperationsTest {
	
	//private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private static final String APP_ID = "DevApiDiagApp1";
	private static final String DEVICE_TYPE = "TestDT";
	private static final String DEVICE_ID = "RasPi01";
	
	private static final String CLASS_NAME = DeviceDiagnosticsAPIOperationsTest.class.getName();
	
	/**
	 * Sample Diagnostic Log in JSON Format
	 * {
  	 * 	"message": "Sample log",
  	 *	"severity": 0,
     *	"data": "Sample data",
  	 *	"timestamp": "2015-10-24T04:17:23.889Z"
	 * }
	 */
	
	private static final String logToBeAdded = "{\"message\": \"Sample log\",\"severity\": 0,\"data\": "
			+ "\"sample data\",\"timestamp\": \"2015-10-24T04:17:23.889Z\"}";
	
	/**
	 * Sample Diagnostic ErrorCode in JSON Format
	 * {
  	 * 	"errorCode": 0,
  	 *	"timestamp": "2015-10-24T04:17:23.892Z"
	 * }
	 */
	private static final String errorcodeToBeAdded = "{\"errorCode\": 100,\"timestamp\": \"2015-10-24T04:17:23.892Z\"}";
	
	private static APIClient apiClient = null;

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
	 * Add a diagnostic Log for a particular device.
	 */
	@Test
	public void test01AddDiagnosticLog() {
		final String METHOD = "test01AddDiagnosticLog";
		LoggerUtility.info(CLASS_NAME, METHOD, 
				"Add diag Log for device --> "+DEVICE_ID + " of type "+DEVICE_TYPE);
		JsonElement log = null;
		try {
			JsonParser parser = new JsonParser();
			log = parser.parse(logToBeAdded);
			boolean status = apiClient.addDiagnosticLog(DEVICE_TYPE, DEVICE_ID, log);
			assertTrue("Add diagnostic log", status);
			JsonArray response = apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Could not get/add diagnostic logs", response.size() >=1);
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage()
				+ " HTTP Response: " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
		
	}
	
	/**
	 * Get all the diagnostic Logs of a device.
	 */
	@Test
	public void test02GetAllDiagnosticLogs() {
		final String METHOD = "test02GetAllDiagnosticLogs";
		LoggerUtility.info(CLASS_NAME, METHOD, 
				"Get all diag Log of device --> "+DEVICE_ID + " of type "+DEVICE_TYPE);
		// Get all the diagnostics logs of device DEVICE_ID
		try {
			JsonArray response = apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Could not get/add diagnostic logs", response.size() >=1);
			for(Iterator<JsonElement> iterator = response.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				LoggerUtility.info(CLASS_NAME, METHOD, responseJson.toString());
			}	
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage()
				+ " HTTP Response: " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
		
	}
	
	/**
	 * Get a diagnostic Log with a given ID.
	 */
	@Test
	public void test03GetDiagnosticLog() {
		final String METHOD = "test03GetDiagnosticLog";
		JsonObject responseJson = null;
		// Lets get all diagnostic Logs and retrieve the first one based on ID
		try {
			JsonArray response = apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			JsonElement logElement = response.get(0);
			responseJson = logElement.getAsJsonObject();
			LoggerUtility.info(CLASS_NAME, METHOD, 
					"Get a Diag Log based on Id "+responseJson.get("id").getAsString());
			JsonObject log = apiClient.getDiagnosticLog(DEVICE_TYPE, DEVICE_ID, responseJson.get("id").getAsString());
			LoggerUtility.info(CLASS_NAME, METHOD, log.getAsString());
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage()
				+ " HTTP Response: " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
		
	}

	/**
	 * Delete a diagnostic Log.
	 */
	@Test
	public void test04DeleteDiagnosticLog() {
		final String METHOD = "test04DeleteDiagnosticLog";
		LoggerUtility.info(CLASS_NAME, METHOD, 
				"Delete a Diag Log for device "+DEVICE_ID + " of type "+DEVICE_TYPE);
		JsonObject responseJson = null;
		// Lets get all diagnostic Logs and delete the first one
		try {
			JsonArray response = apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			int totalcount = response.size();
			JsonElement logElement = response.get(0);
			responseJson = logElement.getAsJsonObject();
			boolean status = apiClient.deleteDiagnosticLog(DEVICE_TYPE, DEVICE_ID, responseJson.get("id").getAsString());
			LoggerUtility.info(CLASS_NAME, METHOD, 
					"Deletion of Log ID :: "+ responseJson.get("id").getAsString() + "  "+status);
			
			JsonArray response1 = apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Successfully deleted the log", response1.size() == totalcount - 1);
			
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage()
				+ " HTTP Response: " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
		
	}
	
	/**
	 * Clear a diagnostic Log with the given ID.
	 */
	@Test
	public void test05ClearDiagnosticLogs() {
		final String METHOD = "test05ClearDiagnosticLogs";
		LoggerUtility.info(CLASS_NAME, METHOD, 
				"Clear all Diag Log of device "+DEVICE_ID + " of type "+DEVICE_TYPE);
		try {
			boolean status = apiClient.clearAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Clear all diagnostic logs", status);
			JsonArray response = apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Successfully cleared the log messages", status && (response.size() == 0));
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage()
				+ " HTTP Response: " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
		
	}

	// Methods related to DiagnosticErrorcodes start from here
	
	/**
	 * Clear the diagnostic error codes.
	 */
	@Test
	public void test08ClearDiagnosticErrorCodes() {
		final String METHOD = "test08ClearDiagnosticErrorCodes";
		LoggerUtility.info(CLASS_NAME, METHOD,
				"Clear all diag Log of device --> "+DEVICE_ID + " of type "+DEVICE_TYPE);
		try {
			boolean status = apiClient.clearAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Clear all diagnostic error codes", status);
			JsonArray response = apiClient.getAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Successfully cleared all the errorcodes", status && (response.size() == 0));
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage()
				+ " HTTP Response: " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
		
	}

	/**
	 * Get all the diagnostic error codes.
	 */
	@Test
	public void test07GetAllDiagnosticErrorCodes() {
		final String METHOD = "test07GetAllDiagnosticErrorCodes";
		LoggerUtility.info(CLASS_NAME, METHOD,
				"Get all diag Errorcodes of device --> "+DEVICE_ID + " of type "+DEVICE_TYPE);
		// Get all the diagnostics ErrorCodes of device DEVICE_ID
		try {
			JsonArray response = apiClient.getAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Successfully get/add all the errorcodes", (response.size() >= 1));
			for(Iterator<JsonElement> iterator = response.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				LoggerUtility.info(CLASS_NAME, METHOD, responseJson.getAsString());
			}	
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage()
				+ " HTTP Response: " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
		
	}
	
	/**
	 * Add a diagnostic error code.
	 * 
	 * Diagnostic ErrorCode can be added using 2 ways
	 * 1. JSON Format
	 * 2. providing properties in raw format
	 */
	@Test
	public void test06AddDiagnosticErrorCode() {
		final String METHOD = "test06AddDiagnosticErrorCode";
		LoggerUtility.info(CLASS_NAME, METHOD,
				"Add an Errorcode for device --> "+DEVICE_ID + " of type "+DEVICE_TYPE);
		JsonElement errorcode = null;
		try {
			JsonParser parser = new JsonParser();
			errorcode = parser.parse(errorcodeToBeAdded);
			boolean status = apiClient.addDiagnosticErrorCode(DEVICE_TYPE, DEVICE_ID, 0, new Date());
			assertTrue("Add diagnostic Error Code #1", status);
			status = apiClient.addDiagnosticErrorCode(DEVICE_TYPE, DEVICE_ID, errorcode);
			assertTrue("Add diagnostic Error Code #2", status);
			JsonArray response = apiClient.getAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Successfully get/add all the errorcodes", status && (response.size() >= 1));
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage()
				+ " HTTP Response: " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
		
	}
	
	/**
	 * Get connection logs which is not related to the Diagnostic Log.
	 */
	@Test
	public void test09getDeviceConnectionLogs() {
		final String METHOD = "test09getDeviceConnectionLogs";
		
		DeviceClient devClient = null;
		Properties devProps = TestEnv.getDeviceProperties(DEVICE_TYPE, DEVICE_ID);
		try {
			devClient = new DeviceClient(devProps);
			devClient.connect(5);
			boolean connected = devClient.isConnected();
			assertTrue("Device must be connected", connected);
			devClient.disconnect();
		} catch (Exception e) {
			String failMsg = "Unexpected exception " + e.getLocalizedMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			return;
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD,
				"Get device connection logs for device --> "+DEVICE_ID + " of type "+DEVICE_TYPE);
		// Get the connection logs of device DEVICE_ID
		try {
			JsonArray response = apiClient.getDeviceConnectionLogs(DEVICE_TYPE, DEVICE_ID);
			for(Iterator<JsonElement> iterator = response.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				LoggerUtility.info(CLASS_NAME, METHOD, responseJson.getAsString());
			}	
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage()
				+ " HTTP Response: " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
	}
	
}