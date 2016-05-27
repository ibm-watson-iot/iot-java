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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
 * add/update/get/delete device type(s)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceTypeAPIOperationsTest extends TestCase {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
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
	private final static String deviceTypeToBeAdded = "{\"id\": \"TestDT\",\"description\": "
			+ "\"TestDT\",\"deviceInfo\": {\"fwVersion\": \"1.0.0\",\"hwVersion\": \"1.0\"},\"metadata\": {}}";
	
	private final static String deviceInfoToBeAdded = "{\"fwVersion\": \"1.0.0\",\"hwVersion\": \"1.0\"}";
	
	private final static String metaDataToBeAdded = "{\"hello\": \"I'm metadata\"}";
	
	private final static String DEVICE_TYPE = "TestDT";
	
	private static APIClient apiClient = null;
	private static boolean setUpIsDone = false;
	
	public synchronized void setUp() {
	    if (setUpIsDone) {
	        return;
	    }
	    
	    /**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(DeviceAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		try {
			//Instantiate the class by passing the properties file
			apiClient = new APIClient(props);
		} catch (Exception e) {
			// looks like the application.properties file is not updated properly
			apiClient = null;
		}
	    setUpIsDone = true;
	}
	

	/**
	 * This sample showcases how to Delete a device type using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test02deleteDeviceType() throws IoTFCReSTException {
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

	/**
	 * This sample showcases how to Create a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	public void test01addDeviceType() throws IoTFCReSTException {
		System.out.println("Add a device type "+DEVICE_TYPE);
		try {
			JsonElement type = new JsonParser().parse(deviceTypeToBeAdded);
			JsonObject response = apiClient.addDeviceType(type);
			System.out.println(response);
			assertTrue("Could not add device type", apiClient.isDeviceTypeExist(DEVICE_TYPE));
		} catch(IoTFCReSTException e) {
			// Print if there is a partial response
			System.out.println(e.getResponse());
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * This sample showcases how to Create a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	public void test03addDeviceTypeWithMoreParameters() throws IoTFCReSTException {
		try {
			System.out.println("Add a device type "+DEVICE_TYPE);
			JsonParser parser = new JsonParser();
			JsonElement deviceInfo = parser.parse(deviceInfoToBeAdded);
			JsonElement metadata = parser.parse(metaDataToBeAdded);
			
			JsonObject response = this.apiClient.addDeviceType(DEVICE_TYPE, 
					"sample description", deviceInfo, metadata);
			
			System.out.println(response);
			assertTrue("Could not add device type", apiClient.isDeviceTypeExist(DEVICE_TYPE));
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * This sample showcases how to get the details of a device type using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test04getDeviceType() throws IoTFCReSTException {
		try {
			System.out.println("Get a device type "+DEVICE_TYPE);
			JsonObject response = this.apiClient.getDeviceType(DEVICE_TYPE);
			System.out.println(response);
			assertTrue("Could not get a device type", response != null);
		}  catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}

	/**
	 * This sample showcases how to update a device type using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test05updateDeviceType() throws IoTFCReSTException {
		System.out.println("Update a device type "+DEVICE_TYPE);
		try {
			JsonObject json = new JsonObject();
			json.addProperty("description", "Hello, I'm updated description");
			JsonObject response = this.apiClient.updateDeviceType(DEVICE_TYPE, json);
			System.out.println(response);
			assertTrue("Failed to update a device type", response != null);
		}  catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * This sample showcases how to retrieve all the device types present in the given Organization.
	 * @throws IoTFCReSTException
	 */
	public void test06getAllDeviceTypes() throws IoTFCReSTException {
		System.out.println("Get all device types present in the Organization");
		// Get all the devices in the organization
		/**
		 * The Java ibmiotf client library provides an one argument constructor
		 * which can be used to control the output, for example, lets try to retrieve
		 * the devices in a sorted order based on device type ID.
		 */
		
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("_sort","id"));
		
		try {
			JsonObject response = this.apiClient.getAllDeviceTypes(parameters);
			
			
			// The response will contain more parameters that will be used to issue
			// the next request. The result element will contain the current list of devices
			JsonArray deviceTypes = response.get("results").getAsJsonArray(); 
			for(Iterator<JsonElement> iterator = deviceTypes.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				System.out.println(responseJson);
			}
		}  catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
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