/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;

/**
 * This test verifies various bulk ReST operations that can be performed on Watson IoT Platform.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BulkAPIOperationsTest extends TestCase {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	private final static String DEVICE_TYPE = "SampleDT";
	private final static String DEVICE_ID1 = "Device01";
	private final static String DEVICE_ID2 = "Device02";
	
	// Example Json format to add a device
	/*
	 * {
		    "typeId": "SampleDT",
		    "deviceId": "RasPi100",
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
	private final static String deviceToBeAdded1 = "{\"typeId\": \""+ DEVICE_TYPE + "\",\"deviceId\": "
			+ "\"" + DEVICE_ID1 + "\",\"authToken\": \"password\",\"deviceInfo\": {\"serialNumber\": "
			+ "\"10087\",\"manufacturer\": \"IBM\",\"model\": \"7865\",\"deviceClass\": "
			+ "\"A\",\"description\": \"My RasPi01 Device\",\"fwVersion\": \"1.0.0\","
			+ "\"hwVersion\": \"1.0\",\"descriptiveLocation\": \"EGL C\"    },    "
			+ "\"location\": {\"measuredDateTime\": \"2015-23-07T11:23:23+00:00\"    "
			+ "},    \"metadata\": {}}";
	
	private final static String deviceToBeAdded2 = "{\"typeId\": \""+ DEVICE_TYPE + "\",\"deviceId\": "
			+ "\"" + DEVICE_ID2 + "\",\"authToken\": \"password\",\"deviceInfo\": {\"serialNumber\": "
			+ "\"10087\",\"manufacturer\": \"IBM\",\"model\": \"7865\",\"deviceClass\": "
			+ "\"A\",\"description\": \"My RasPi01 Device\",\"fwVersion\": \"1.0.0\","
			+ "\"hwVersion\": \"1.0\",\"descriptiveLocation\": \"EGL C\"    },    "
			+ "\"location\": {\"measuredDateTime\": \"2015-23-07T11:23:23+00:00\"    "
			+ "},    \"metadata\": {}}";


	private final static String deviceToBeDeleted1 = "{\"typeId\": \""+ DEVICE_TYPE + "\", \"deviceId\": \"" + DEVICE_ID1 + "\"}";
	private final static String deviceToBeDeleted2 = "{\"typeId\": \"" + DEVICE_TYPE + "\", \"deviceId\": \"" + DEVICE_ID2 + "\"}";
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
			props.load(BulkAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			propsWrongToken.load(BulkAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			propsWrongMethod.load(BulkAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			propsWrongOrg.load(BulkAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		try {
			//Instantiate the class by passing the properties file
			this.apiClient = new APIClient(props);
			addDeviceType(DEVICE_TYPE);
			
			propsWrongToken.setProperty("Authentication-Token", "Wrong");
			apiClientWithWrongToken = new APIClient(propsWrongToken);
			
			propsWrongMethod.setProperty("API-Key", "Wrong");
			apiClientWithWrongKey = new APIClient(propsWrongMethod);
			
			propsWrongOrg.setProperty("Organization-ID", "Wrong");
			apiClientWithWrongOrg = new APIClient(propsWrongOrg);
			
		} catch (Exception e) {
			e.printStackTrace();
			// looks like the application.properties file is not updated properly
			apiClient = null;
			apiClientWithWrongToken = null;
		}
		
	    setUpIsDone = true;
	}
		
	/**
	 * This sample verifies the bulk deletion of devices in the Platform
	 * 
	 * Json Format to delete the device
	 * [
	 *	  {
	 *	    "typeId": "string",
	 *	    "deviceId": "string"
	 *	  }
	 *	]
	 * @throws Exception 
	 */
	public void test04BulkDeleteDevices() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		System.out.println("Deleting couple of devices");
		JsonElement device1 = new JsonParser().parse(deviceToBeDeleted1);
		JsonElement device2 = new JsonParser().parse(deviceToBeDeleted2);
		JsonArray arryOfDevicesToBeDeleted = new JsonArray();
		arryOfDevicesToBeDeleted.add(device1);
		arryOfDevicesToBeDeleted.add(device2);
		JsonArray devices = null;
		try {
			devices = this.apiClient.deleteMultipleDevices(arryOfDevicesToBeDeleted);
			for(Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				System.out.println(responseJson);
			}
		} catch (IoTFCReSTException e) {
			if(e.getHttpCode() != 202) {
				throw e;
			}
		}
		
		// check if the devices are actually deleted from the platform
		assertFalse("Device "+ DEVICE_ID1 + " is present in the Platform", this.checkIfDeviceExists(DEVICE_TYPE, DEVICE_ID1));
		assertFalse("Device "+ DEVICE_ID2 + " is present in the Platform", this.checkIfDeviceExists(DEVICE_TYPE, DEVICE_ID2));
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.deleteMultipleDevices(arryOfDevicesToBeDeleted);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.deleteMultipleDevices(arryOfDevicesToBeDeleted);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.deleteMultipleDevices(arryOfDevicesToBeDeleted);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
	
	/**
	 * This sample adds a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	private void addDeviceType(String deviceType) throws IoTFCReSTException {
		
		System.out.println("<-- Checking if device type "+deviceType +" already created in Watson IoT Platform");
		boolean exist = apiClient.isDeviceTypeExist(deviceType);
		try {
			if (!exist) {
				System.out.println("<-- Adding device type "+deviceType + " now..");
				// device type to be created in WIoTP
				apiClient.addDeviceType(deviceType, deviceType, null, null);
			}
		} catch(IoTFCReSTException e) {
			System.err.println("ERROR: unable to add manually device type " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private boolean checkIfDeviceExists(String deviceType, String deviceId) throws IoTFCReSTException {
		return apiClient.isDeviceExist(deviceType, deviceId);
	}

	/**
	 * This sample verifies the bulk addition of devices in IBM Watson IoT Platform
	 * @throws Exception
	 */
	public void test02BulkAddDevices() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		System.out.println("Adding couple of devices");
		JsonElement device1 = new JsonParser().parse(deviceToBeAdded1);
		JsonElement device2 = new JsonParser().parse(deviceToBeAdded2);
		JsonArray arryOfDevicesToBeAdded = new JsonArray();
		arryOfDevicesToBeAdded.add(device1);
		arryOfDevicesToBeAdded.add(device2);
		
		JsonArray devices = null;
		try {
			devices = this.apiClient.addMultipleDevices(arryOfDevicesToBeAdded);
			for(Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				System.out.println(responseJson);
			}
		} catch(IoTFCReSTException e) {
			if(e.getHttpCode() != 202) {
				throw e;
			}
		}
		
		// check if the devices are actually present in the platform
		assertTrue("Device "+ DEVICE_ID1 + " is not present in the Platform", this.checkIfDeviceExists(DEVICE_TYPE, DEVICE_ID1));
		assertTrue("Device "+ DEVICE_ID2 + " is not present in the Platform", this.checkIfDeviceExists(DEVICE_TYPE, DEVICE_ID2));
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.addMultipleDevices(arryOfDevicesToBeAdded);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.addMultipleDevices(arryOfDevicesToBeAdded);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.addMultipleDevices(arryOfDevicesToBeAdded);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {}

	}

	/**
	 * This sample verifies the Bulk Get
	 * @throws Exception
	 */
	public void test03BulkGetAllDevices() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		System.out.println("Retrieve all devices in the Organization..");
		// Get all the devices in the organization
		/**
		 * The Java ibmiotf client library provides an one argument constructor
		 * which can be used to control the output, for example, lets try to retrieve
		 * the devices in a sorted order based on device ID.
		 */
		
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("_sort","deviceId"));
		//parameters.add(new BasicNameValuePair("_limit","2"));
		JsonObject response = this.apiClient.getAllDevices(parameters);
		
		System.out.println(response);
		// The response will contain more parameters that will be used to issue
		// the next request. The result element will contain the current list of devices
		JsonArray devices = response.get("results").getAsJsonArray(); 
		for(Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
			JsonElement deviceElement = iterator.next();
			JsonObject responseJson = deviceElement.getAsJsonObject();
			System.out.println(responseJson);
		}	
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getAllDevices();
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.getAllDevices();
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {}
				
		// Wrrong Org
		try {
			apiClientWithWrongOrg.getAllDevices();
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {}
	}

	/**
	 * This sample verfies the get organization details API
	 * @throws Exception
	 */
	public void test01GetOrganizationDetails() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		System.out.println("Retrieve Organization details...");
		// Get the organization detail
		JsonObject orgDetail = this.apiClient.getOrganizationDetails();
		System.out.println(orgDetail);
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getOrganizationDetails();
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {
			System.out.println(e.getHttpCode() + " " +e.getMessage());
		}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.getOrganizationDetails();
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {
			System.out.println(e.getHttpCode() + " " +e.getMessage());
		}
						
		// Wrrong Org
		try {
			apiClientWithWrongOrg.getOrganizationDetails();
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {
			System.out.println(e.getHttpCode() + " " +e.getMessage());
		}
	}
	
	/**
	 * This sample showcases how to Delete a device type using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test07deleteDeviceType() throws IoTFCReSTException {
		System.out.println("Delete a device type "+DEVICE_TYPE);
		try {
			boolean status = this.apiClient.deleteDeviceType(DEVICE_TYPE);
			assertFalse("Could not delete the device type", apiClient.isDeviceTypeExist(DEVICE_TYPE));
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}

}