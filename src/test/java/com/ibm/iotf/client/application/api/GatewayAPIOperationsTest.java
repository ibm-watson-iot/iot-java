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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.util.LoggerUtility;

import junit.framework.TestCase;
/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/Gateway device(s)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayAPIOperationsTest {
	
	private final static String CLASS_NAME = GatewayAPIOperationsTest.class.getName();
	private static final String APP_ID = "GWApiApp1";
	private static final String GW_DEVICE_TYPE = "GWApiOpType1";
	private static final String GW_DEVICE_ID = "GWApiDev1";
	
	// Attached device 
	private final static String ATTACHED_DEVICE_TYPE = "GWApiOpAttType1";
	private final static String ATTACHED_DEVICE_ID = "GWApiOpAttDev1";
	private final static String ATTACHED_DEVICE_ID2 = "GWApiOpAttDev2";
	
	private static APIClient apiClient = null;
	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, GW_DEVICE_TYPE, null);
		apiClient = new APIClient(appProps);

		// Delete device if it was left from the last test run
		if (apiClient.isDeviceExist(GW_DEVICE_TYPE, GW_DEVICE_ID)) {
			apiClient.deleteDevice(GW_DEVICE_TYPE, GW_DEVICE_ID);
		}
		
		// If the device type does not exist, create it
		if (apiClient.isDeviceTypeExist(GW_DEVICE_TYPE) == false) {
			JsonObject jsonGWType = new JsonObject();
			jsonGWType.addProperty("classId", "Gateway");
			jsonGWType.addProperty("id", GW_DEVICE_TYPE);
			apiClient.addGatewayDeviceType(jsonGWType);
		}
		
		// Register the test device DEVICE_ID
		apiClient.registerDevice(GW_DEVICE_TYPE, GW_DEVICE_ID, TestEnv.getGatewayToken(), null, null, null);

	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {
		
		if (apiClient.isDeviceExist(GW_DEVICE_TYPE, GW_DEVICE_ID)) {
			apiClient.deleteDevice(GW_DEVICE_TYPE, GW_DEVICE_ID);
		}
		
		if (apiClient.isDeviceTypeExist(GW_DEVICE_TYPE)) {
			apiClient.deleteDeviceType(GW_DEVICE_TYPE);
		}
	}	
	
	public void test01RegisterDevicesUnderGateway() {
		final String METHOD = "test01RegisterDevicesUnderGateway";
		LoggerUtility.info(CLASS_NAME, METHOD, "Registering devices under the gateway --> " + GW_DEVICE_ID);
		
		try {
			// Let us add the device type first
			apiClient.addDeviceType(ATTACHED_DEVICE_TYPE, METHOD, null, null);
		} catch (Exception e) { 
			String failMsg = "Unexpected exception creating device type (" 
					+  ATTACHED_DEVICE_TYPE + ") : " + e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			return;
		}
		
		try {
			JsonObject response = apiClient.registerDeviceUnderGateway(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, GW_DEVICE_TYPE, GW_DEVICE_ID);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch (IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
		
		try {
			boolean created = apiClient.isDeviceExist(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID);
			assertTrue("Device (" + ATTACHED_DEVICE_ID + ") was not created.", created);
		} catch (IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
		
		// Use the other constructor to register other device
		JsonObject jsonDevice = new JsonObject();
		jsonDevice.addProperty("deviceId", ATTACHED_DEVICE_ID2);
		jsonDevice.addProperty("authToken", TestEnv.getDeviceToken());
		
		try {
			JsonObject response = apiClient.registerDeviceUnderGateway(ATTACHED_DEVICE_TYPE, GW_DEVICE_ID, GW_DEVICE_TYPE, jsonDevice);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch (IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
		
		try {
			boolean created = apiClient.isDeviceExist(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID2);
			assertTrue("Device (" + ATTACHED_DEVICE_ID + ") was not created.", created);
		} catch (IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
				
	}

	/**
	 * This sample showcases how to get device details using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test02getDevicesConnectedThroughGateway() throws IoTFCReSTException {
		final String METHOD = "test02getDevicesConnectedThroughGateway";
		LoggerUtility.info(CLASS_NAME, METHOD, "get Devices Connected Through Gateway --> "+ GW_DEVICE_ID);
		try {
			JsonObject response = apiClient.getDevicesConnectedThroughGateway(GW_DEVICE_TYPE, GW_DEVICE_ID);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
	}
	
	
	
	/**
	 * This sample showcases how to Delete a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	public void test03deleteDevices() {
		final String METHOD = "test03deleteDevices";
		
			LoggerUtility.info(CLASS_NAME, METHOD, "Deleting device " + ATTACHED_DEVICE_ID);
			
			try {
				boolean delete = apiClient.deleteDevice(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID);
				assertTrue("Delete attached device " + ATTACHED_DEVICE_ID + " failed.", delete);
			} catch (IoTFCReSTException e) {
				String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
				fail(failMsg);
			}
			
			try {
				boolean exist = apiClient.isDeviceExist(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID);
				assertFalse("Device " + ATTACHED_DEVICE_ID + " was not deleted successfully", exist);
			} catch (IoTFCReSTException e) {
				String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
				fail(failMsg);
			}
			

			LoggerUtility.info(CLASS_NAME, METHOD, "Deleting device " + ATTACHED_DEVICE_ID2);
			try {
				boolean delete = apiClient.deleteDevice(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID2);
				assertTrue("Delete attached device " + ATTACHED_DEVICE_ID2 + " failed.", delete);
			} catch (IoTFCReSTException e) {
				String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
				fail(failMsg);
			}
			
			try {
				boolean exist = apiClient.isDeviceExist(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID2);
				assertFalse("Device " + ATTACHED_DEVICE_ID2 + " was not deleted successfully", exist);
			} catch (IoTFCReSTException e) {
				String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
				fail(failMsg);
			}
			
			
			// Delete attached device type
			try {
				boolean delete = apiClient.deleteDeviceType(ATTACHED_DEVICE_TYPE);
				assertTrue("Delete attached device type " + ATTACHED_DEVICE_TYPE + " failed.", delete);
			} catch (IoTFCReSTException e) {
				String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
				fail(failMsg);
			}
			
			try {
				boolean exist = apiClient.isDeviceTypeExist(ATTACHED_DEVICE_TYPE);
				assertFalse("Device type " + ATTACHED_DEVICE_TYPE + " was not deleted successfully", exist);
			} catch (IoTFCReSTException e) {
				String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
				fail(failMsg);
			}
		
	}

	
}