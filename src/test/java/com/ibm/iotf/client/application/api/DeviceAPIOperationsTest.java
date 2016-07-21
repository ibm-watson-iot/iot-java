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
 *****************************************************************************
 */


import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.FixMethodOrder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

import org.junit.runners.MethodSorters;
import org.junit.FixMethodOrder;
/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/delete device(s)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceAPIOperationsTest extends TestCase {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private static final String DEVICE_TYPE = "TestDT";
	private static final String DEVICE_ID = "RasPi01";
	private static final String DEVICE_ID2 = "RasPi02";
	private static final String NON_ASCII_DEVICE_ID = "NONASCII";
	
	// Example Json format to add a device
	/*
	 * {
		    "typeId": "ManagedDT",
		    "deviceId": "DEVICE_ID2",
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
	

	private static boolean setUpIsDone = false;
	
	private static APIClient apiClient = null;
	private static APIClient apiClientWithWrongToken = null;
	private static APIClient apiClientWithWrongKey = null;
	private static APIClient apiClientWithWrongOrg= null;
	
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
			props.load(DeviceAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			propsWrongToken.load(DeviceAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			propsWrongMethod.load(DeviceAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			propsWrongOrg.load(DeviceAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		
		try {
			//Instantiate the class by passing the properties file
			apiClient = new APIClient(props);
			addDeviceType();
			
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
	 * This sample showcases how to Create a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	private static void addDeviceType() throws IoTFCReSTException {
		try {
			boolean status = apiClient.isDeviceTypeExist(DEVICE_TYPE);
			if(status == false) {
				System.out.println("Adding device Type --> "+DEVICE_TYPE);
				apiClient.addDeviceType(DEVICE_TYPE, DEVICE_TYPE, null, null);
			}
			return;
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to Delete a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test08deleteDevice() throws IoTFCReSTException {
		try {
			System.out.println("Deleting devices --> " +  "  and "+DEVICE_ID);
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID2);
			assertFalse("Device is not deleted successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID2));
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
			assertFalse("Device is not deleted successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID));
			apiClient.deleteDevice(DEVICE_TYPE, NON_ASCII_DEVICE_ID);
			assertFalse("Device is not deleted successfully", apiClient.isDeviceExist(DEVICE_TYPE, NON_ASCII_DEVICE_ID));
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.deleteDevice(DEVICE_TYPE, DEVICE_ID2);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.deleteDevice(DEVICE_TYPE, DEVICE_ID2);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.deleteDevice(DEVICE_TYPE, DEVICE_ID2);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}	

		// negative test, it should fail
		try {
			apiClientWithWrongToken.isDeviceExist(DEVICE_TYPE, DEVICE_ID2);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.isDeviceExist(DEVICE_TYPE, DEVICE_ID2);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.isDeviceExist(DEVICE_TYPE, DEVICE_ID2);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
	}

	/**
	 * This sample showcases how to add a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test01addDevice() throws IoTFCReSTException {
		System.out.println("Adding device --> "+deviceToBeAdded);
		JsonParser parser = new JsonParser();
		JsonElement input = parser.parse(deviceToBeAdded);
		try{
			JsonObject response = this.apiClient.registerDevice(DEVICE_TYPE, input);
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID));
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		try{
			
			// Lets add device with different API that accepts more args,
			
			JsonElement deviceInfo = parser.parse(deviceInfoToBeAdded);
			JsonElement location = parser.parse(locationToBeAdded);
			
			System.out.println("Adding device --> "+DEVICE_ID2);
			JsonObject response = this.apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID2, "Password", 
					deviceInfo, location, null);
			
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID2));
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.registerDevice(DEVICE_TYPE, input);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.registerDevice(DEVICE_TYPE, input);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.registerDevice(DEVICE_TYPE, input);
			fail("Doesn't throw invild Org exception");
		} catch(IoTFCReSTException e) {}		
	}
	
	/**
	 * This sample showcases how to add a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test01addDeviceWithNonASCIIContents() throws IoTFCReSTException {
		
		String DEVICE_TEMPLATE = "'{'\"deviceId\": " + "\"{0}\",\"authToken\": \"password\","
				+ "\"deviceInfo\": '{'\"descriptiveLocation\": \"{1}\"}}";

		String location = "XÃŒ";  // Contains a non-ASCII character
		//String location = "IBM";  // Contains a non-ASCII character
		String deviceData = MessageFormat.format(DEVICE_TEMPLATE, NON_ASCII_DEVICE_ID, location);
		JsonParser parser = new JsonParser();
		try {
			JsonElement input = parser.parse(deviceData);
			JsonObject response = this.apiClient.registerDevice(DEVICE_TYPE, input);
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, NON_ASCII_DEVICE_ID));
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to get device details using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test02getDevice() throws IoTFCReSTException {
		try {
			System.out.println("get device --> "+DEVICE_ID);
			JsonObject response = this.apiClient.getDevice(DEVICE_TYPE, DEVICE_ID);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getDevice(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.getDevice(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.getDevice(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {}		

	}
	
	/**
	 * This sample showcases how to get device details using the Java Client Library.
	 * 
	 * Negative test - suppy an invalid device type
	 * @throws IoTFCReSTException
	 */
	public void test021getDevice() throws IoTFCReSTException {
		try {
			System.out.println("get device --> "+DEVICE_ID);
			JsonObject response = this.apiClient.getDevice("Non-Exist", DEVICE_ID);
			fail("Must thorw an exception");
		} catch(IoTFCReSTException e) {
			
		}
	}
	
	/**
	 * This sample showcases how to update a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test03updateDeviceLocation() throws IoTFCReSTException {
		System.out.println("update device location of device --> "+DEVICE_ID);
		JsonElement newLocation = new JsonParser().parse(newlocationToBeAdded);
		try {
			JsonObject response = this.apiClient.updateDeviceLocation(DEVICE_TYPE, DEVICE_ID, newLocation);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		// negative test, it should fail
		try {
			apiClientWithWrongToken.updateDeviceLocation(DEVICE_TYPE, DEVICE_ID, newLocation);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.updateDeviceLocation(DEVICE_TYPE, DEVICE_ID, newLocation);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.updateDeviceLocation(DEVICE_TYPE, DEVICE_ID, newLocation);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {}				
		
	}
	
	/**
	 * This sample showcases how to get a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test04getDeviceLocation() throws IoTFCReSTException {
		try {
			System.out.println("get device location of device --> "+DEVICE_ID);
			JsonObject response = this.apiClient.getDeviceLocation(DEVICE_TYPE, DEVICE_ID);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getDeviceLocation(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.getDeviceLocation(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.getDeviceLocation(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {}	
	}
	
	
	/**
	 * This sample showcases how to get a management information of a device using the Java Client Library.
	 * @throws Exception 
	 */
	public void test05getDeviceManagementInformation() throws Exception {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(DeviceAPIOperationsTest.class.getResourceAsStream("/device.properties"));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		String typeId = props.getProperty("Device-Type");
		String deviceId = props.getProperty("Device-ID");
		
		ManagedDevice dmClient = null;
		try {
			DeviceData data = new DeviceData.Builder().build();
			dmClient = new ManagedDevice(props, data);
			dmClient.connect();
			dmClient.sendManageRequest(0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		System.out.println("get device management information of device --> "+deviceId);
		try {
			JsonObject response = this.apiClient.getDeviceManagementInformation(typeId, deviceId);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		} finally {
			dmClient.disconnect();
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getDeviceManagementInformation(typeId, deviceId);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.getDeviceManagementInformation(typeId, deviceId);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.getDeviceManagementInformation(typeId, deviceId);
			fail("Doesn't throw invild Org exception");
		} catch(IoTFCReSTException e) {}	
	}

	/**
	 * This sample showcases how to update a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test06updateDevice() throws IoTFCReSTException {
		
		JsonObject updatedMetadata = new JsonObject();
		
		try {
			System.out.println("update device --> "+DEVICE_ID);
			JsonObject metadata = new JsonObject();
			metadata.addProperty("Hi", "Hello, I'm updated metadata");
		
			updatedMetadata.add("metadata", metadata);
			JsonObject response = this.apiClient.updateDevice(DEVICE_TYPE, DEVICE_ID, updatedMetadata);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.updateDevice(DEVICE_TYPE, DEVICE_ID, updatedMetadata);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.updateDevice(DEVICE_TYPE, DEVICE_ID, updatedMetadata);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.updateDevice(DEVICE_TYPE, DEVICE_ID, updatedMetadata);
			fail("Doesn't throw invild Org exception");
		} catch(IoTFCReSTException e) {}	
	}

	/**
	 * This sample showcases how to retrieve all the devices in an organization using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test07getAllDevices() throws IoTFCReSTException {
		System.out.println("Get all devices of device type--> "+DEVICE_TYPE);
		// Get all the devices of type TestDT
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		try {
			/**
			 * The Java ibmiotf client library provides an one argument constructor
			 * which can be used to control the output, for example, lets try to retrieve
			 * the devices in a sorted order based on device ID.
			 */
			parameters.add(new BasicNameValuePair("_sort","deviceId"));
			
			JsonObject response = this.apiClient.retrieveDevices(DEVICE_TYPE, parameters);
			
			// The response will contain more parameters that will be used to issue
			// the next request. The result element will contain the current list of devices
			JsonArray devices = response.get("results").getAsJsonArray(); 
			for(Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				System.out.println(responseJson);
			}
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.retrieveDevices(DEVICE_TYPE);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.retrieveDevices(DEVICE_TYPE, null);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.retrieveDevices(DEVICE_TYPE, parameters);
			fail("Doesn't throw invild Org exception");
		} catch(IoTFCReSTException e) {}	
	}
	
	/**
	 * This sample showcases how to Delete a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void testLastdeleteDeviceTypeAndDevice() throws IoTFCReSTException {
		try {
			boolean status = this.apiClient.deleteDeviceType(DEVICE_TYPE);
			assertFalse("Could not delete the device type", apiClient.isDeviceTypeExist(DEVICE_TYPE));
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.deleteDeviceType(DEVICE_TYPE);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.deleteDeviceType(DEVICE_TYPE);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.deleteDeviceType(DEVICE_TYPE);
			fail("Doesn't throw invild Org exception");
		} catch(IoTFCReSTException e) {}	
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.isDeviceTypeExist(DEVICE_TYPE);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.isDeviceTypeExist(DEVICE_TYPE);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.isDeviceTypeExist(DEVICE_TYPE);
			fail("Doesn't throw invild Org exception");
		} catch(IoTFCReSTException e) {}	
		
	}
}