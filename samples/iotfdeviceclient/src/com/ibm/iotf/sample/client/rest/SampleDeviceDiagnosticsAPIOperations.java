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

import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

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
 * add/update/get/delete device diagnostics.
 */
public class SampleDeviceDiagnosticsAPIOperations {
	
	private final static String PROPERTIES_FILE_NAME = "application.prop";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";
	
	private static final String DEVICE_TYPE = "SampleDT";
	private static final String DEVICE_ID = "RasPi101";
	
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
	
	private APIClient apiClient = null;
	
	SampleDeviceDiagnosticsAPIOperations(String filePath) {
		
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
		
		// Make sure that we have device added to the IBM IoT Foundation already
		SampleDeviceDiagnosticsAPIOperations sample = new SampleDeviceDiagnosticsAPIOperations(fileName);
		
		sample.addDiagnosticLog();
		sample.getAllDiagnosticLogs();
		sample.getDiagnosticLog();
		sample.deleteDiagnosticLog();
		sample.clearDiagnosticLogs();
		
		sample.addDiagnosticErrorCode();
		sample.getAllDiagnosticErrorCodes();
		sample.clearDiagnosticErrorCodes();
		
		sample.getDeviceConnectionLogs();

	}

	/**
	 * This sample showcases how to Delete a diagnostic Log.
	 * @throws IoTFCReSTException
	 */
	private void deleteDiagnosticLog() {
		// Lets get all diagnostic Logs and delete the first one
		try {
			JsonArray response = this.apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			
			JsonElement logElement = response.get(0);
			JsonObject responseJson = logElement.getAsJsonObject();
			boolean status = apiClient.deleteDiagnosticLog(DEVICE_TYPE, DEVICE_ID, responseJson.get("id").getAsString());
			System.out.println("Deletion of Log ID :: "+ responseJson.get("id").getAsString() + "  "+status);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to get a diagnostic Log with a given ID.
	 * @throws IoTFCReSTException
	 */

	private void getDiagnosticLog() {
		// Lets get all diagnostic Logs and retrieve the first one based on ID
		try {
			JsonArray response = this.apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			JsonElement logElement = response.get(0);
			JsonObject responseJson = logElement.getAsJsonObject();
			JsonObject log = apiClient.getDiagnosticLog(DEVICE_TYPE, DEVICE_ID, responseJson.get("id").getAsString());
			System.out.println(log);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}

	/**
	 * This sample showcases how to clear a diagnostic Log with the given ID.
	 * @throws IoTFCReSTException
	 */

	private void clearDiagnosticLogs() {
		try {
			boolean status = this.apiClient.clearDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
			System.out.println(status);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}

	/**
	 * This sample showcases how to add a diagnostic Log for a particular device.
	 * @throws IoTFCReSTException
	 */
	private void addDiagnosticLog() {
		try {
			JsonParser parser = new JsonParser();
			JsonElement log = parser.parse(logToBeAdded);
			boolean status = this.apiClient.addDiagnosticLog(DEVICE_TYPE, DEVICE_ID, log);
			System.out.println(status);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to get all the diagnostic Logs of a device.
	 * @throws IoTFCReSTException
	 */
	private void getAllDiagnosticLogs() {
		// Get all the diagnostics logs of device DEVICE_ID
		try {
			JsonArray response = this.apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
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
	}
	
	// Methods related to DiagnosticErrorcodes start from here
	
	/**
	 * This sample showcases how to clear the diagnostic error codes.
	 * @throws IoTFCReSTException
	 */
	private void clearDiagnosticErrorCodes() {
		try {
			boolean status = this.apiClient.clearDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
			System.out.println(status);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}

	/**
	 * This sample showcases how to get all the diagnostic error codes.
	 * @throws IoTFCReSTException
	 */
	private void getAllDiagnosticErrorCodes() {
		// Get all the diagnostics ErrorCodes of device DEVICE_ID
		try {
			JsonArray response = this.apiClient.getAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
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
	}
	
	/**
	 * This sample showcases how to add a diagnostic error code.
	 * 
	 * Diagnostic ErrorCode can be added using 2 ways
	 * 1. JSON Format
	 * 2. providing properties in raw format
	 */
	private void addDiagnosticErrorCode() {
		try {
			JsonParser parser = new JsonParser();
			JsonElement errorcode = parser.parse(errorcodeToBeAdded);
			boolean status = this.apiClient.addDiagnosticErrorCode(DEVICE_TYPE, DEVICE_ID, 0, new Date());
			System.out.println("ErrorCode addtion status : "+status);
			status = this.apiClient.addDiagnosticErrorCode(DEVICE_TYPE, DEVICE_ID, errorcode);
			System.out.println("ErrorCode addtion status : "+status);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to get connection logs which is not related to the Diagnostic Log.
	 */
	private void getDeviceConnectionLogs() {
		// Get the connection logs of device DEVICE_ID
		try {
			JsonArray response = this.apiClient.getDeviceConnectLogs(DEVICE_TYPE, DEVICE_ID);
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
	}
}