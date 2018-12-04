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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.util.LoggerUtility;

import junit.framework.TestCase;

/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/delete device type(s)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceTypeAPIOperationsTest extends TestCase {

	private static final String CLASS_NAME = DeviceTypeAPIOperationsTest.class.getName();
	private static final String APP_ID = "DTypeApiOpApp1";
	private final static String DEVICE_TYPE = "ApiOpType1";
	
	/**
	 * Example Json format to add a device type
	 * 
	 * {
		  "id": "TestDT",
		  "description": "TestDT",
		  "deviceInfo": {
		        "fwVersion": "1.0.0",
		    "hwVersion": "1.0"
		  },
		  "metadata": {
		
		  }
	 * }
	 */
	private final static String deviceTypeToBeAdded = "{\"id\": \"" + DEVICE_TYPE + "\",\"description\": "
			+ "\"Device Type API Op Test\",\"deviceInfo\": {\"fwVersion\": \"1.0.0\",\"hwVersion\": \"1.0\"},\"metadata\": {}}";
	
	private final static String deviceInfoToBeAdded = "{\"fwVersion\": \"1.0.0\",\"hwVersion\": \"1.0\"}";
	
	private final static String metaDataToBeAdded = "{\"hello\": \"I'm metadata\"}";
	
	
	
	private static APIClient apiClient = null;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, null);
		apiClient = new APIClient(appProps);

		if (apiClient.isDeviceTypeExist(DEVICE_TYPE)) {
			apiClient.deleteDeviceType(DEVICE_TYPE);
		}

	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {
		if (apiClient.isDeviceTypeExist(DEVICE_TYPE)) {
			apiClient.deleteDeviceType(DEVICE_TYPE);
		}	
	}

	/**
	 * This sample showcases how to Create a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	public void test01addDeviceType() throws IoTFCReSTException {
		final String METHOD = "test01addDeviceType";
		LoggerUtility.info(CLASS_NAME, METHOD, "Add a device type "+DEVICE_TYPE);
		try {
			JsonElement type = new JsonParser().parse(deviceTypeToBeAdded);
			JsonObject response = apiClient.addDeviceType(type);
			LoggerUtility.info(CLASS_NAME, METHOD, response.toString());
			assertTrue("Could not add device type", apiClient.isDeviceTypeExist(DEVICE_TYPE));
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		}
	}
	
	/**
	 * This sample showcases how to Delete a device type using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test02deleteDeviceType() throws IoTFCReSTException {
		final String METHOD = "test02deleteDeviceType";
		LoggerUtility.info(CLASS_NAME, METHOD, "Delete device type "+DEVICE_TYPE);
		try {
			boolean status = apiClient.deleteDeviceType(DEVICE_TYPE);
			assertTrue("deleteDeviceType method should succeed", status);
			assertFalse("Device type should no longer exist", apiClient.isDeviceTypeExist(DEVICE_TYPE));
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		}
	}

	
	/**
	 * This sample showcases how to Create a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	public void test03addDeviceTypeWithMoreParameters() throws IoTFCReSTException {
		final String METHOD = "test03addDeviceTypeWithMoreParameters";
		LoggerUtility.info(CLASS_NAME, METHOD, "Add a device type with more parameters "+DEVICE_TYPE);

		try {
			JsonParser parser = new JsonParser();
			JsonElement deviceInfo = parser.parse(deviceInfoToBeAdded);
			JsonElement metadata = parser.parse(metaDataToBeAdded);
			
			JsonObject response = apiClient.addDeviceType(DEVICE_TYPE, 
					"sample description", deviceInfo, metadata);
			
			LoggerUtility.info(CLASS_NAME, METHOD, response.toString());
			
			assertTrue("Could not add device type", apiClient.isDeviceTypeExist(DEVICE_TYPE));
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		}
	}
	
	/**
	 * This sample showcases how to get the details of a device type using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test04getDeviceType() throws IoTFCReSTException {
		final String METHOD = "test04getDeviceType";
		LoggerUtility.info(CLASS_NAME, METHOD, "Get device type "+DEVICE_TYPE);
		try {
			JsonObject response = apiClient.getDeviceType(DEVICE_TYPE);
			LoggerUtility.info(CLASS_NAME, METHOD, response.toString());
			assertTrue("Could not get a device type", response != null);
		}  catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		}
	}

	/**
	 * This sample showcases how to update a device type using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test05updateDeviceType() throws IoTFCReSTException {
		final String METHOD = "test05updateDeviceType";
		LoggerUtility.info(CLASS_NAME, METHOD, "Update device type "+DEVICE_TYPE);
		try {
			JsonObject json = new JsonObject();
			json.addProperty("description", "Hello, I'm updated description");
			JsonObject response = apiClient.updateDeviceType(DEVICE_TYPE, json);
			LoggerUtility.info(CLASS_NAME, METHOD, response.toString());
			assertTrue("Failed to update a device type", response != null);
		}  catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		}
	}
	
	/**
	 * This sample showcases how to retrieve all the device types present in the given Organization.
	 * @throws IoTFCReSTException
	 */
	public void test06getAllDeviceTypes() throws IoTFCReSTException {
		final String METHOD = "test06getAllDeviceTypes";
		LoggerUtility.info(CLASS_NAME, METHOD, "Get all device types present in the Organization");
		// Get all the devices in the organization
		/**
		 * The Java ibmiotf client library provides an one argument constructor
		 * which can be used to control the output, for example, lets try to retrieve
		 * the devices in a sorted order based on device type ID.
		 */
		
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("_sort","id"));
		
		try {
			JsonObject response = apiClient.getAllDeviceTypes(parameters);
			
			
			// The response will contain more parameters that will be used to issue
			// the next request. The result element will contain the current list of devices
			JsonArray deviceTypes = response.get("results").getAsJsonArray(); 
			for(Iterator<JsonElement> iterator = deviceTypes.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				LoggerUtility.info(CLASS_NAME, METHOD, responseJson.toString());
			}
		}  catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		}
	}
	
}