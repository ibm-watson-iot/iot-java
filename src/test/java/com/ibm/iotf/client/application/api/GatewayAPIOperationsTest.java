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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.FixMethodOrder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.device.DeviceManagementTest;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

import org.junit.runners.MethodSorters;
import org.junit.FixMethodOrder;
/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/Gateway device(s)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayAPIOperationsTest extends TestCase {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private static final String DEVICE_TYPE = "GatewayDT";
	private static final String DEVICE_ID = "Gateway01";
	
	// Attached device 
	private final static String ATTACHED_DEVICE_TYPE = "AttachedDT";
	private final static String ATTACHED_DEVICE_ID = "Dev01";
	private final static String ATTACHED_DEVICE_ID2 = "Dev02";
	
	private final static String ATTACHED_DEVICE_TO_BE_ADDED = "{\"deviceId\": "
			+ "\"" + ATTACHED_DEVICE_ID2 + "\",\"authToken\": \"password\","  + "\"metadata\": {}}";

	private static boolean setUpIsDone = false;
	
	private static APIClient apiClient = null;
	
	public synchronized void setUp() {
	    if (setUpIsDone) {
	        return;
	    }
	    
	    /**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(GatewayAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		try {
			//Instantiate the class by passing the properties file
			apiClient = new APIClient(props);
			
			String deviceTypeToBeAdded = "{\"id\": \"" + DEVICE_TYPE + "\",\"description\": "
					+ "\"TestDT\",\"deviceInfo\": {\"fwVersion\": \"1.0.0\",\"hwVersion\": \"1.0\"},\"metadata\": {}}";
			
			JsonElement type = new JsonParser().parse(deviceTypeToBeAdded);
			apiClient.addGatewayDeviceType(type);
			apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID, "password", null,null,null);
			
		} catch (Exception e) {
			// looks like the application.properties file is not updated properly
			apiClient = null;
		}
		
		
	    setUpIsDone = true;
	}
	
	public void test01RegisterDevicesUnderGateway() throws IoTFCReSTException {
		System.out.println("Registering devices under the gateway --> " + DEVICE_ID);
		
		// Let us add the device type first
		apiClient.addDeviceType(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_TYPE, null, null);
		// then add the device
		apiClient.registerDeviceUnderGateway(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, DEVICE_TYPE, DEVICE_ID);
		
		// check if the device exists
		assertTrue("Not able to register attached devices", apiClient.isDeviceExist(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID));
		
		// Use the other constructor to register other device
		JsonElement device = new JsonParser().parse(ATTACHED_DEVICE_TO_BE_ADDED);
		
		apiClient.registerDeviceUnderGateway(ATTACHED_DEVICE_TYPE, DEVICE_ID, DEVICE_TYPE, device);
		
		// check if the device exists
		assertTrue("Not able to register attached devices", apiClient.isDeviceExist(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID2));
				
	}
	
	
	/**
	 * This sample showcases how to Delete a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test03deleteDevices() throws IoTFCReSTException {
		try {
			System.out.println("Deleting device --> "+ATTACHED_DEVICE_ID);
			boolean status = apiClient.deleteDevice(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID);
			assertFalse("Device is not deleted successfully", apiClient.isDeviceExist(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID));
			
			System.out.println("Deleting devices --> "+ATTACHED_DEVICE_ID2);
			status = apiClient.deleteDevice(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID2);
			assertFalse("Device is not deleted successfully", apiClient.isDeviceExist(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID2));
			
			System.out.println("Deleting Gateway --> "+DEVICE_ID);
			status = apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
			assertFalse("Device is not deleted successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID));
			
			//Detete device types
			apiClient.deleteDeviceType(ATTACHED_DEVICE_TYPE);
			assertFalse("Device is not deleted successfully", apiClient.isDeviceTypeExist(ATTACHED_DEVICE_TYPE));
			
			//Detete device types
			apiClient.deleteDeviceType(DEVICE_TYPE);
			assertFalse("Device is not deleted successfully", apiClient.isDeviceTypeExist(DEVICE_TYPE));
			
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
	public void test02getDevicesConnectedThroughGateway() throws IoTFCReSTException {
		try {
			System.out.println("get Devices Connected Through Gateway --> "+DEVICE_ID);
			JsonObject response = this.apiClient.getDevicesConnectedThroughGateway(DEVICE_TYPE, DEVICE_ID);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	
}