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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.util.LoggerUtility;

/**
 * This test verifies various bulk ReST operations that can be performed on Watson IoT Platform.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BulkAPIOperationsTest {
	
	private final static String CLASS_NAME = BulkAPIOperationsTest.class.getName();
	private final static String APP_ID = "BulkApp1";
	private final static String DEVICE_TYPE = "SampleDT";
	private final static String DEVICE_ID1 = "Device01";
	private final static String DEVICE_ID2 = "Device02";
	
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
	
	private static APIClient apiClient = null;
	private static APIClient apiClientWithWrongToken = null;
	private static APIClient apiClientWithWrongKey = null;
	private static APIClient apiClientWithWrongOrg= null;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		final String METHOD = "oneTimeSetUp";
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, null);
		Properties propsWrongToken = new Properties(appProps);
		Properties propsWrongMethod = new Properties(appProps);
		Properties propsWrongOrg = new Properties(appProps);
		
		//Instantiate the class by passing the properties file
		try {
			apiClient = new APIClient(appProps);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean typeExist = false;
		try {
			typeExist = apiClient.isDeviceTypeExist(DEVICE_TYPE);
			LoggerUtility.info(CLASS_NAME, METHOD, DEVICE_TYPE + " exist = " + typeExist);
		} catch (IoTFCReSTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (typeExist) {
			
			try {
				apiClient.deleteDeviceType(DEVICE_TYPE);
				LoggerUtility.info(CLASS_NAME, METHOD, DEVICE_TYPE + " deleted.");
			} catch (IoTFCReSTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			apiClient.addDeviceType(DEVICE_TYPE, null, null, null);
			LoggerUtility.info(CLASS_NAME, METHOD, DEVICE_TYPE + " added.");
		} catch (IoTFCReSTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		propsWrongToken.setProperty("Authentication-Token", "Wrong");
		try {
			apiClientWithWrongToken = new APIClient(propsWrongToken);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		propsWrongMethod.setProperty("API-Key", "Wrong");
		try {
			apiClientWithWrongKey = new APIClient(propsWrongMethod);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		propsWrongOrg.setProperty("Organization-ID", "Wrong");
		try {
			apiClientWithWrongOrg = new APIClient(propsWrongOrg);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void oneTimeCleanUp() throws Exception {
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, null);
		apiClient = new APIClient(appProps);
		apiClient.deleteDeviceType(DEVICE_TYPE);
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
	@Test
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
			devices = apiClient.deleteMultipleDevices(arryOfDevicesToBeDeleted);
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
	
	private boolean checkIfDeviceExists(String deviceType, String deviceId) throws IoTFCReSTException {
		return apiClient.isDeviceExist(deviceType, deviceId);
	}

	/**
	 * This sample verifies the bulk addition of devices in IBM Watson IoT Platform
	 * @throws Exception
	 */
	@Test
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
			devices = apiClient.addMultipleDevices(arryOfDevicesToBeAdded);
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
			fail("Doesn't throw invalid Auth token exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.addMultipleDevices(arryOfDevicesToBeAdded);
			fail("Doesn't throw invalid API Key exception");
		} catch(IoTFCReSTException e) {}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.addMultipleDevices(arryOfDevicesToBeAdded);
			fail("Doesn't throw invalid ORG exception");
		} catch(IoTFCReSTException e) {}

	}

	/**
	 * This sample verifies the Bulk Get
	 * @throws Exception
	 */
	@Test
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
		JsonObject response = apiClient.getAllDevices(parameters);
		
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
	@Test
	public void test01GetOrganizationDetails() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		System.out.println("Retrieve Organization details...");
		// Get the organization detail
		JsonObject orgDetail = apiClient.getOrganizationDetails();
		System.out.println(orgDetail);
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getOrganizationDetails();
			fail("Doesn't throw invalid Auth token exception");
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

}