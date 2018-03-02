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

/**
 * This test-case tests various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/delete device(s)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceAPIOperationsTest extends TestCase {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private static final String DEVICE_TYPE = "TestDT";
	private static final String DEVICE_ID = "RasPi01";
	private static final String DEVICE_ID2 = "RasPi02";
	
	private static final String GATEWAY_TYPE = "testgw";
	private static final String GATEWAY_ID = "testgw_01";
	
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
/*	
	private final static JsonElement gatewayTypeToBeAdded = new JsonParser().parse("{\"deviceId\": "
			+ "\"" + DEVICE_ID + "\",\"authToken\": \"password\"," + 
			"\"location\": " + locationToBeAdded + "," 
			+ "\"deviceInfo\": " + deviceInfoToBeAdded + "," 
			+ "\"metadata\": {}}");
*/	

	
	private static boolean setUpIsDone = false;
	
	private static APIClient apiClient = null;
	private static APIClient apiClientWithWrongToken = null;
	private static APIClient apiClientWithWrongKey = null;
	private static APIClient apiClientWithWrongOrg= null;
	
	
    public static TestSetup suite() {
        TestSetup setup = new TestSetup(new TestSuite(DeviceAPIOperationsTest.class)) {
            protected void setUp() throws Exception {
        			
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

            }
            protected void tearDown(  ) throws Exception {
	        		
	        		try {
	        			boolean status = apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
	        			
	        		} catch(IoTFCReSTException e) {
	        			
	        		}
	        		try {
	        			boolean status = apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID2);
	        			
	        		} catch(IoTFCReSTException e) {
	        			
	        		}
	        		try {
	        			boolean status = apiClient.deleteDeviceType(DEVICE_TYPE);
	        			
	        		} catch(IoTFCReSTException e) {
	        			
	        		}

            }
        };
        return setup;
    }
    
    /*
	public synchronized void setUp() {
	    if (setUpIsDone) {
	        return;
	    }
	    
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
	*/
    
	/**
	 * This helper method creates a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	private static void addDeviceType() throws IoTFCReSTException {
		try {
			boolean status = apiClient.isDeviceTypeExist(DEVICE_TYPE);
			if(status == false) {
				
				apiClient.addDeviceType(DEVICE_TYPE, DEVICE_TYPE, null, null);
			}
			return;
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
	}
	
	/**
	 * This helper method creates a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	/*
	private static void addGatewayType() throws IoTFCReSTException {
		try {
			boolean status = apiClient.isDeviceTypeExist(GATEWAY_TYPE);
			if(status == false) {
				apiClient.addGatewayDeviceType(gatewayTypeToBeAdded);
			}
			return;
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
	}
	*/
	
	/**
	 * This test-case tests how to add a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test01addDevice() throws IoTFCReSTException {
		
		JsonParser parser = new JsonParser();
		JsonElement input = parser.parse(deviceToBeAdded);
		try{
			if(apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
				apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
			}
			JsonObject response = apiClient.registerDevice(DEVICE_TYPE, input);
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID));
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		} /* finally {
			if(apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
				apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
			}
		}*/
		
		try{
			
			// Lets add device with different API that accepts more args,
			
			JsonElement deviceInfo = parser.parse(deviceInfoToBeAdded);
			JsonElement location = parser.parse(locationToBeAdded);
			
			
			JsonObject response = apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID2, "Password", 
					deviceInfo, location, null);
			
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID2));
			
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.registerDevice(DEVICE_TYPE, input);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {
			
		}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.registerDevice(DEVICE_TYPE, input);
			fail("Doesn't throw invalid API Key exception");
		} catch(IoTFCReSTException e) {
			
		}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.registerDevice(DEVICE_TYPE, input);
			fail("Doesn't throw invalid Org exception");
		} catch(IoTFCReSTException e) {
			
		}	
		
	}
	
	/**
	 * This test-case tests how to add a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	//This test case has been commented as non-ASCII are not supported 
	/*
	public void test01addDeviceWithNonASCIIContents() throws IoTFCReSTException {
		
		String DEVICE_TEMPLATE = "'{'\"deviceId\": " + "\"{0}\",\"authToken\": \"password\","
				+ "\"deviceInfo\": '{'\"descriptiveLocation\": \"{1}\"}}";

		String location = "XÃŒ";  // Contains a non-ASCII character
		//String location = "IBM";  // Contains a non-ASCII character
		String deviceData = MessageFormat.format(DEVICE_TEMPLATE, NON_ASCII_DEVICE_ID, location);
		JsonParser parser = new JsonParser();
		try {
			JsonElement input = parser.parse(deviceData);
			JsonObject response = apiClient.registerDevice(DEVICE_TYPE, input);
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, NON_ASCII_DEVICE_ID));
		} catch(IoTFCReSTException e) {
			System.out.println("Message = " + e.getMessage() + "\tHTTP Code = " + e.getHttpCode() + "\tResponse = " + e.getResponse() );
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
		}
	}
	*/
	
	/**
	 * This test-case tests how to get device details using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test02getDevice() throws IoTFCReSTException {
		try {
			
			JsonObject response = apiClient.getDevice(DEVICE_TYPE, DEVICE_ID);
			
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getDevice(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.getDevice(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invalid API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.getDevice(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invalid ORG exception");
		} catch(IoTFCReSTException e) {}		

	}
	
	/**
	 * This test-case tests how to get device details using the Java Client Library.
	 * 
	 * Negative test - suppy an invalid device type
	 * @throws IoTFCReSTException
	 */
	public void test021getDevice() throws IoTFCReSTException {
		try {
			
			JsonObject response = apiClient.getDevice("Non-Exist", DEVICE_ID);
			fail("Must thorw an exception");
		} catch(IoTFCReSTException e) {
			
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
			
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
		// negative test, it should fail
		try {
			apiClientWithWrongToken.updateDeviceLocation(DEVICE_TYPE, DEVICE_ID, newLocation);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.updateDeviceLocation(DEVICE_TYPE, DEVICE_ID, newLocation);
			fail("Doesn't throw invalid API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.updateDeviceLocation(DEVICE_TYPE, DEVICE_ID, newLocation);
			fail("Doesn't throw invalid ORG exception");
		} catch(IoTFCReSTException e) {}				
		
	}
	
	/**
	 * This test-case tests how to get a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test04getDeviceLocation() throws IoTFCReSTException {
		try {
			
			JsonObject response = apiClient.getDeviceLocation(DEVICE_TYPE, DEVICE_ID);
			
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getDeviceLocation(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.getDeviceLocation(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invalid API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.getDeviceLocation(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invalid ORG exception");
		} catch(IoTFCReSTException e) {}	
	}
	
	/**
	 * This test-case tests how to get a device location weather using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test05getDeviceLocationWeather() throws IoTFCReSTException {
		try {
			
			JsonObject response = apiClient.getDeviceLocationWeather(DEVICE_TYPE, DEVICE_ID);
			
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getDeviceLocationWeather(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.getDeviceLocationWeather(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invalid API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.getDeviceLocationWeather(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invalid ORG exception");
		} catch(IoTFCReSTException e) {}	
	}
	
	
	/**
	 * This test-case tests how to get a management information of a device using the Java Client Library.
	 * @throws Exception 
	 */
	public void test06getDeviceManagementInformation() throws Exception {
		
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
		
		
		try {
			JsonObject response = apiClient.getDeviceManagementInformation(typeId, deviceId);
			
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		} finally {
			dmClient.disconnect();
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getDeviceManagementInformation(typeId, deviceId);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.getDeviceManagementInformation(typeId, deviceId);
			fail("Doesn't throw invalid API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.getDeviceManagementInformation(typeId, deviceId);
			fail("Doesn't throw invalid Org exception");
		} catch(IoTFCReSTException e) {}	
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
			
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.updateDevice(DEVICE_TYPE, DEVICE_ID, updatedMetadata);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.updateDevice(DEVICE_TYPE, DEVICE_ID, updatedMetadata);
			fail("Doesn't throw invalid API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.updateDevice(DEVICE_TYPE, DEVICE_ID, updatedMetadata);
			fail("Doesn't throw invalid Org exception");
		} catch(IoTFCReSTException e) {}	
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
				
			}
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.retrieveDevices(DEVICE_TYPE);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.retrieveDevices(DEVICE_TYPE, null);
			fail("Doesn't throw invalid API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.retrieveDevices(DEVICE_TYPE, parameters);
			fail("Doesn't throw invalid Org exception");
		} catch(IoTFCReSTException e) {}	
	}

	/**
	 * This test-case tests how to retrieve all the devices in an organization using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test08getAllDevices() throws IoTFCReSTException {
		
		// Get all the devices of type TestDT
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		try {
			
			JsonObject response = apiClient.getAllDevices();
			
			// The response will contain more parameters that will be used to issue
			// the next request. The result element will contain the current list of devices
			JsonArray devices = response.get("results").getAsJsonArray(); 
			for(Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				
			}
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.retrieveDevices(DEVICE_TYPE);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.retrieveDevices(DEVICE_TYPE, null);
			fail("Doesn't throw invalid API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.retrieveDevices(DEVICE_TYPE, parameters);
			fail("Doesn't throw invalid Org exception");
		} catch(IoTFCReSTException e) {}	
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
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.deleteDevice(DEVICE_TYPE, DEVICE_ID2);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.deleteDevice(DEVICE_TYPE, DEVICE_ID2);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.deleteDevice(DEVICE_TYPE, DEVICE_ID2);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}	

		// negative test, it should fail
		try {
			apiClientWithWrongToken.isDeviceExist(DEVICE_TYPE, DEVICE_ID2);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.isDeviceExist(DEVICE_TYPE, DEVICE_ID2);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.isDeviceExist(DEVICE_TYPE, DEVICE_ID2);
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
	}
	
	/**
	 * This test-case tests how to add a device, registered under a gateway, using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test09addDeviceUnderGateway() throws IoTFCReSTException {
		
		JsonParser parser = new JsonParser();
		JsonElement input = parser.parse(deviceToBeAdded);
		try{
			if(apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
				apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
			}
			JsonObject response = apiClient.registerDeviceUnderGateway(DEVICE_TYPE, DEVICE_ID,
					"testgw", "testgw_01");
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID));
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongOrg.registerDeviceUnderGateway(DEVICE_TYPE, DEVICE_ID,
					"testgw", "testgw_01");
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {
			
		}
		
		// Wrong Method
		try {
			apiClientWithWrongOrg.registerDeviceUnderGateway(DEVICE_TYPE, DEVICE_ID,
					"testgw", "testgw_01");
			fail("Doesn't throw invalid API Key exception");
		} catch(IoTFCReSTException e) {
			
		}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.registerDeviceUnderGateway(DEVICE_TYPE, DEVICE_ID,
					"testgw", "testgw_01");
			fail("Doesn't throw invalid Org exception");
		} catch(IoTFCReSTException e) {
			
		}	
		
	}
	

}