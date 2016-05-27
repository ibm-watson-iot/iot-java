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
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;

/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/delete device diagnostics.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceDiagnosticsAPIOperationsTest extends TestCase {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private static final String DEVICE_TYPE = "TestDT";
	private static final String DEVICE_ID = "RasPi01";
	
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
	private static APIClient apiClientWithWrongToken = null;
	private static APIClient apiClientWithWrongKey = null;
	private static APIClient apiClientWithWrongOrg= null;

	private static boolean setUpIsDone = false;
	
	public synchronized void setUp() {
	    if (setUpIsDone) {
	        return;
	    }
	    
	    /**
		  * Load device properties
		  */
		Properties props = new Properties();
		Properties propsWrongToken = new Properties();
		Properties propsWrongMethod = new Properties();
		Properties propsWrongOrg = new Properties();
		try {
			props.load(DeviceDiagnosticsAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			propsWrongToken.load(DeviceDiagnosticsAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			propsWrongMethod.load(DeviceDiagnosticsAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			propsWrongOrg.load(DeviceDiagnosticsAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}			
		
		try {
			//Instantiate the class by passing the properties file
			apiClient = new APIClient(props);
			addDeviceTypeAndDevice();
			
			props.setProperty("Authentication-Token", "Wrong");
			apiClientWithWrongKey = new APIClient(props);
			
			props.setProperty("API-Key", "Wrong");
			apiClientWithWrongToken = new APIClient(props);
			
			props.setProperty("Organization-ID", "Wrong");
			apiClientWithWrongOrg = new APIClient(props);
		} catch (Exception e) {
			// looks like the application.properties file is not updated properly
			apiClient = null;
		}
		
		
	    setUpIsDone = true;
	}
	
	/**
	 * This method adds a device & device type if its not added already 
	 * @throws IoTFCReSTException
	 */
	private void addDeviceTypeAndDevice() throws IoTFCReSTException {
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
	 * This sample showcases how to Delete a diagnostic Log.
	 * @throws IoTFCReSTException
	 */
	public void test04deleteDiagnosticLog() {
		System.out.println("Delete a Diag Log for device "+DEVICE_ID + " of type "+DEVICE_TYPE);
		JsonObject responseJson = null;
		// Lets get all diagnostic Logs and delete the first one
		try {
			JsonArray response = this.apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			int totalcount = response.size();
			JsonElement logElement = response.get(0);
			responseJson = logElement.getAsJsonObject();
			boolean status = apiClient.deleteDiagnosticLog(DEVICE_TYPE, DEVICE_ID, responseJson.get("id").getAsString());
			System.out.println("Deletion of Log ID :: "+ responseJson.get("id").getAsString() + "  "+status);
			
			JsonArray response1 = this.apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Successfully deleted the log", response1.size() == totalcount - 1);
			
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.deleteDiagnosticLog(DEVICE_TYPE, DEVICE_ID, responseJson.get("id").getAsString());
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.deleteDiagnosticLog(DEVICE_TYPE, DEVICE_ID, responseJson.get("id").getAsString());
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.deleteDiagnosticLog(DEVICE_TYPE, DEVICE_ID, responseJson.get("id").getAsString());
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
	
	/**
	 * This sample showcases how to get a diagnostic Log with a given ID.
	 * @throws IoTFCReSTException
	 */

	public void test03getDiagnosticLog() {
		JsonObject responseJson = null;
		// Lets get all diagnostic Logs and retrieve the first one based on ID
		try {
			JsonArray response = this.apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			JsonElement logElement = response.get(0);
			responseJson = logElement.getAsJsonObject();
			System.out.println("Get a Diag Log based on Id "+responseJson.get("id").getAsString());
			JsonObject log = apiClient.getDiagnosticLog(DEVICE_TYPE, DEVICE_ID, responseJson.get("id").getAsString());
			System.out.println(log);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getDiagnosticLog(DEVICE_TYPE, DEVICE_ID, responseJson.get("id").getAsString());
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.getDiagnosticLog(DEVICE_TYPE, DEVICE_ID, responseJson.get("id").getAsString());
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.getDiagnosticLog(DEVICE_TYPE, DEVICE_ID, responseJson.get("id").getAsString());
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}

	/**
	 * This sample showcases how to clear a diagnostic Log with the given ID.
	 * @throws IoTFCReSTException
	 */

	public void test05clearDiagnosticLogs() {
		System.out.println("Clear all Diag Log of device "+DEVICE_ID + " of type "+DEVICE_TYPE);
		try {
			boolean status = this.apiClient.clearAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			System.out.println(status);
			JsonArray response = this.apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Successfully cleared the log messages", status && (response.size() == 0));
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.clearAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.clearAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.clearAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}

	/**
	 * This sample showcases how to add a diagnostic Log for a particular device.
	 * @throws IoTFCReSTException
	 */
	public void test01addDiagnosticLog() {
		System.out.println("Add diag Log for device --> "+DEVICE_ID + " of type "+DEVICE_TYPE);
		JsonElement log = null;
		try {
			JsonParser parser = new JsonParser();
			log = parser.parse(logToBeAdded);
			boolean status = this.apiClient.addDiagnosticLog(DEVICE_TYPE, DEVICE_ID, log);
			System.out.println(status);
			JsonArray response = this.apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Could not get/add diagnostic logs", response.size() >=1);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.addDiagnosticLog(DEVICE_TYPE, DEVICE_ID, log);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.addDiagnosticLog(DEVICE_TYPE, DEVICE_ID, log);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.addDiagnosticLog(DEVICE_TYPE, DEVICE_ID, log);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
	
	/**
	 * This sample showcases how to get all the diagnostic Logs of a device.
	 * @throws IoTFCReSTException
	 */
	public void test02getAllDiagnosticLogs() {
		System.out.println("Get all diag Log of device --> "+DEVICE_ID + " of type "+DEVICE_TYPE);
		// Get all the diagnostics logs of device DEVICE_ID
		try {
			JsonArray response = this.apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Could not get/add diagnostic logs", response.size() >=1);
			for(Iterator<JsonElement> iterator = response.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				System.out.println(responseJson);
			}	
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
	
	// Methods related to DiagnosticErrorcodes start from here
	
	/**
	 * This sample showcases how to clear the diagnostic error codes.
	 * @throws IoTFCReSTException
	 */
	public void test08clearDiagnosticErrorCodes() {
		System.out.println("Clear all diag Log of device --> "+DEVICE_ID + " of type "+DEVICE_TYPE);
		try {
			boolean status = this.apiClient.clearAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			System.out.println(status);
			JsonArray response = this.apiClient.getAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Successfully cleared all the errorcodes", status && (response.size() == 0));
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.clearAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.clearAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.clearAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}

	/**
	 * This sample showcases how to get all the diagnostic error codes.
	 * @throws IoTFCReSTException
	 */
	public void test07getAllDiagnosticErrorCodes() {
		System.out.println("Get all diag Errorcodes of device --> "+DEVICE_ID + " of type "+DEVICE_TYPE);
		// Get all the diagnostics ErrorCodes of device DEVICE_ID
		try {
			JsonArray response = this.apiClient.getAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Successfully get/add all the errorcodes", (response.size() >= 1));
			for(Iterator<JsonElement> iterator = response.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				System.out.println(responseJson);
			}	
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.getAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.getAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
	
	/**
	 * This sample showcases how to add a diagnostic error code.
	 * 
	 * Diagnostic ErrorCode can be added using 2 ways
	 * 1. JSON Format
	 * 2. providing properties in raw format
	 */
	public void test06addDiagnosticErrorCode() {
		System.out.println("Add an Errorcode for device --> "+DEVICE_ID + " of type "+DEVICE_TYPE);
		JsonElement errorcode = null;
		try {
			JsonParser parser = new JsonParser();
			errorcode = parser.parse(errorcodeToBeAdded);
			boolean status = this.apiClient.addDiagnosticErrorCode(DEVICE_TYPE, DEVICE_ID, 0, new Date());
			System.out.println("ErrorCode addtion status : "+status);
			status = this.apiClient.addDiagnosticErrorCode(DEVICE_TYPE, DEVICE_ID, errorcode);
			System.out.println("ErrorCode addtion status : "+status);
			JsonArray response = this.apiClient.getAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			assertTrue("Successfully get/add all the errorcodes", status && (response.size() >= 1));
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.addDiagnosticErrorCode(DEVICE_TYPE, DEVICE_ID, errorcode);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.addDiagnosticErrorCode(DEVICE_TYPE, DEVICE_ID, errorcode);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.addDiagnosticErrorCode(DEVICE_TYPE, DEVICE_ID, errorcode);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
	
	/**
	 * This sample showcases how to get connection logs which is not related to the Diagnostic Log.
	 */
	public void test09getDeviceConnectionLogs() {
		System.out.println("Get device connection logs for device --> "+DEVICE_ID + " of type "+DEVICE_TYPE);
		// Get the connection logs of device DEVICE_ID
		try {
			JsonArray response = this.apiClient.getDeviceConnectionLogs(DEVICE_TYPE, DEVICE_ID);
			for(Iterator<JsonElement> iterator = response.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				System.out.println(responseJson);
			}	
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getDeviceConnectionLogs(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.getDeviceConnectionLogs(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.getDeviceConnectionLogs(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
	
	/**
	 * This sample showcases how to Delete a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void testLastdeleteDeviceTypeAndDevice() throws IoTFCReSTException {
		try {
			boolean status = apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
			assertFalse("Device is not deleted successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID));
			status = this.apiClient.deleteDeviceType(DEVICE_TYPE);
			assertFalse("Could not delete the device type", apiClient.isDeviceTypeExist(DEVICE_TYPE));
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
	}
}