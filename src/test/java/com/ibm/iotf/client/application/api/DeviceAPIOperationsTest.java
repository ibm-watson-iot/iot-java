package com.ibm.iotf.client.application.api;

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
 * Amit M Mangalvedkar - Added on 02-Mar-2018
 *****************************************************************************
 */


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import junit.extensions.TestSetup;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.test.common.TestEnv;

import org.junit.runners.MethodSorters;

/**
 * This test-case tests various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/delete device(s)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceAPIOperationsTest extends TestCase {
	
	private static final String APP_ID = "DevApiOpApp1";
	
	//private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private static final String DEVICE_TYPE = "DevApiOpType";
	private static final String DEVICE_ID = "DevApiOpId1";
	private static final String DEVICE_ID2 = "DevApiOpId2";

	/**
	 * Split the elements into multiple lines, so that we can showcase the use of multiple constructors
	 */
	private final static String locationToBeAdded = "{\"longitude\": 0, \"latitude\": 0, \"elevation\": "
			+ "0,\"measuredDateTime\": \"2015-23-07T11:23:23+00:00\"}";
	
	private final static String newlocationToBeAdded = "{\"longitude\": 10, \"latitude\": 20, \"elevation\": 0}";
	
	
	private final static String deviceInfoToBeAdded = "{\"serialNumber\": "
			+ "\"10087\",\"manufacturer\": \"IBM\",\"model\": \"7865\",\"deviceClass\": "
			+ "\"A\",\"description\": \"My DEVICE_ID2 Device\",\"fwVersion\": \"1.0.0\","
			+ "\"hwVersion\": \"1.0\",\"descriptiveLocation\": \"EGL C\"}";
	
	private final static String deviceToBeAdded = "{\"deviceId\": "
			+ "\"" + DEVICE_ID + "\",\"authToken\": \"password\"," + 
			"\"location\": " + locationToBeAdded + "," 
			+ "\"deviceInfo\": " + deviceInfoToBeAdded + "," 
			+ "\"metadata\": {}}";

	private static APIClient apiClient = null;
	
	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, null);
		apiClient = new APIClient(appProps);

		if (apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
		}
		if (apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID2)) {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID2);
		}
		
		if (apiClient.isDeviceTypeExist(DEVICE_TYPE) == false) {
			apiClient.addDeviceType(DEVICE_TYPE, null, null, null);
		}

	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {
		
		if (apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
		}
		
		if (apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID2)) {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID2);
		}
		
		if (apiClient.isDeviceTypeExist(DEVICE_TYPE)) {
			apiClient.deleteDeviceType(DEVICE_TYPE);
		}
	}
	
	/**
	 * This test-case tests how to add a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test01addDevice() {
		
		JsonParser parser = new JsonParser();
		JsonElement input = parser.parse(deviceToBeAdded);
		try {
			JsonObject response = apiClient.registerDevice(DEVICE_TYPE, input);
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID));
			assertFalse("Response must not be null", response.isJsonNull());
		} catch (IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
		try {
			
			// Lets add device with different API that accepts more args,
			
			JsonElement deviceInfo = parser.parse(deviceInfoToBeAdded);
			JsonElement location = parser.parse(locationToBeAdded);
			
			JsonObject response = apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID2, "Password", 
					deviceInfo, location, null);
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID2));
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
	}
	
	/**
	 * This test-case tests how to get device details using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test02getDevice() throws IoTFCReSTException {
		try {
			JsonObject response = apiClient.getDevice(DEVICE_TYPE, DEVICE_ID);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}
	
	/**
	 * This test-case tests how to get device details using the Java Client Library.
	 * 
	 * Negative test - Specify an invalid device type
	 * @throws IoTFCReSTException
	 */
	public void test021getDevice() throws IoTFCReSTException {
		try {
			JsonObject response = apiClient.getDevice("Non-Exist", DEVICE_ID);
			fail("Must thorw an exception, but received a response: " + response.getAsString());
		} catch (IoTFCReSTException e) {
			assertTrue("HTTP error code must be 404", e.getHttpCode() == 404);
		}
	}
	
	/**
	 * This test-case tests how to update a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test03updateDeviceLocation() throws IoTFCReSTException {
		
		JsonElement newLocation = new JsonParser().parse(newlocationToBeAdded);
		try {
			JsonObject response = apiClient.updateDeviceLocation(DEVICE_TYPE, DEVICE_ID, newLocation);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * This test-case tests how to get a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test04getDeviceLocation() throws IoTFCReSTException {
		try {
			JsonObject response = apiClient.getDeviceLocation(DEVICE_TYPE, DEVICE_ID);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * This test-case tests how to get a device location weather using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test05getDeviceLocationWeather() throws IoTFCReSTException {
		try {
			JsonObject response = apiClient.getDeviceLocationWeather(DEVICE_TYPE, DEVICE_ID);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());	
		}
	}
	
	
	/**
	 * This test-case tests how to get a management information of a device using the Java Client Library.
	 * @throws Exception 
	 */
	public void test06getDeviceManagementInformation() {
		
		String managedDevId = "manDev1";
		
		// Register a new managed device
		
		try {
			apiClient.registerDevice(DEVICE_TYPE, managedDevId, TestEnv.getDeviceToken(), null, null, null);
		} catch (IoTFCReSTException e) {
			fail("Device register failed for device ID " + managedDevId + " HTTP Error " + e.getHttpCode() );
			return;
		}
		
		
		Properties props = TestEnv.getDeviceProperties(DEVICE_TYPE, managedDevId);
		
		ManagedDevice dmClient = null;
		try {
			DeviceData data = new DeviceData.Builder().build();
			dmClient = new ManagedDevice(props, data);
			dmClient.connect();
			dmClient.sendManageRequest(0, true, true);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		try {
			JsonObject response = apiClient.getDeviceManagementInformation(DEVICE_TYPE, managedDevId);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());

		} finally {
			dmClient.disconnect();
		}
		
		try {
			apiClient.deleteDevice(DEVICE_TYPE, managedDevId);
		} catch (IoTFCReSTException e) {
			fail("Delete failed for device ID " + managedDevId + " HTTP Error " + e.getHttpCode() );
		}
	}

	/**
	 * This test-case tests how to update a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test07updateDevice() throws IoTFCReSTException {
		
		JsonObject updatedMetadata = new JsonObject();
		
		try {
			
			JsonObject metadata = new JsonObject();
			metadata.addProperty("Hi", "Hello, I'm updated metadata");
		
			updatedMetadata.add("metadata", metadata);
			JsonObject response = apiClient.updateDevice(DEVICE_TYPE, DEVICE_ID, updatedMetadata);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}

	/**
	 * This test-case tests how to retrieve all the devices in an organization using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test08getAllDevicesOfAType() throws IoTFCReSTException {
		
		// Get all the devices of type TestDT
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		try {
			/**
			 * The Java ibmiotf client library provides an one argument constructor
			 * which can be used to control the output, for example, lets try to retrieve
			 * the devices in a sorted order based on device ID.
			 */
			parameters.add(new BasicNameValuePair("_sort","deviceId"));
			
			JsonObject response = apiClient.retrieveDevices(DEVICE_TYPE, parameters);
			
			// The response will contain more parameters that will be used to issue
			// the next request. The result element will contain the current list of devices
			JsonArray devices = response.get("results").getAsJsonArray(); 
			for(Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				assertFalse("Response must not be null", responseJson.isJsonNull());
			}
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}

	/**
	 * This test-case tests how to retrieve all the devices in an organization using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test08getAllDevices() throws IoTFCReSTException {

		try {
			
			JsonObject response = apiClient.getAllDevices();
			
			// The response will contain more parameters that will be used to issue
			// the next request. The result element will contain the current list of devices
			JsonArray devices = response.get("results").getAsJsonArray(); 
			for(Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				assertFalse("Response must not be null", responseJson.isJsonNull());
			}
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}
	
	/**
	 * This test-case tests how to Delete a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test08deleteDevice() throws IoTFCReSTException {
		try {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
			assertFalse("Device is not deleted successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID));
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}
	
	/**
	 * This test-case tests how to add a device, registered under a gateway, using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test09addDeviceUnderGateway() {
		
		String gwType = "gwType1";
		String gwDevId = "gwId1";
		String newDevId = gwDevId + "_1";
		
		JsonElement eGwType = new JsonParser().parse("{\"id\": \"" + gwType + "\"}");
		
		//Add gateway type
		try {
			JsonObject response = apiClient.addGatewayDeviceType(eGwType);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch (IoTFCReSTException e) {
			fail("Add gateway type failed for  " + gwType + " HTTP Error " + e.getHttpCode() );
			return;
		}
		
		// Register gateway device
		try {
			apiClient.registerDevice(gwType, gwDevId, TestEnv.getGatewayToken(), null, null, null);
		} catch (IoTFCReSTException e) {
			fail("Register gateway device failed for " + gwDevId + " HTTP Error " + e.getHttpCode() );
			return;
		}
		
		// Register device under gateway
		try {
			JsonObject response = apiClient.registerDeviceUnderGateway(
					DEVICE_TYPE, newDevId, gwType, gwDevId);
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, newDevId));
			assertFalse("Response must not be null", response.isJsonNull());
		} catch (IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
		// Verify we can get information
		try {
			JsonObject response = apiClient.getDevicesConnectedThroughGateway(gwType, gwDevId);
			assertFalse("Response must not be null", response.isJsonNull());
			
			JsonArray devices = response.get("results").getAsJsonArray(); 
			assertTrue("The results size must be 1", devices.size() == 1);
			for (Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject deviceJson = deviceElement.getAsJsonObject();
				assertTrue("The type must be " + DEVICE_TYPE, 
						deviceJson.get("typeId").getAsString().equals(DEVICE_TYPE));
				assertTrue("The device ID must be " + newDevId, 
						deviceJson.get("deviceId").getAsString().equals(newDevId));
			}
		} catch (IoTFCReSTException e) {
			fail("Get devices under gateway failed. HTTP error: " + e.getHttpCode());
		}
		
		try {
			apiClient.deleteDevice(DEVICE_TYPE, newDevId);
		} catch (IoTFCReSTException e) {
			fail("Delete device under gateway failed. HTTP error: " + e.getHttpCode());
		}
		
		try {
			apiClient.deleteDevice(gwType, gwDevId);
		} catch (IoTFCReSTException e) {
			fail("Delete gateway device failed. HTTP error: " + e.getHttpCode());
		}
		
		try {
			apiClient.deleteDeviceType(gwType);
		} catch (IoTFCReSTException e) {
			fail("Delete gateway type failed. HTTP error: " + e.getHttpCode());
		}
		
	}
	
}