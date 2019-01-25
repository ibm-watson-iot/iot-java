/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.gateway;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestEventCallback;
import com.ibm.iotf.test.common.TestGatewayHelper;
import com.ibm.iotf.test.common.TestHelper;
import com.ibm.iotf.test.common.TestStatusCallback;
import com.ibm.iotf.util.LoggerUtility;

/**
 * This test verifies that the device receives the command published by the application
 * successfully.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayRegisterDeviceTest2 {
	
	private static final String CLASS_NAME = GatewayRegisterDeviceTest2.class.getName();
	private static final String APP_ID = "GwDevRegApp2";
	
	private final static String DEVICE_TYPE = "RegType2";
	private final static String DEVICE_ID_PREFIX = "RegDev2";
	private final static String GW_DEVICE_TYPE = "GwDevRegType2";
	private final static String GW_DEVICE_ID_PREFIX = "GwDevRegDev2";

	private static APIClient apiClient = null;
	private static int testNum = 1;
	private final static int totalTests = 1;
	
	private static HashMap<Integer,TestGatewayHelper> testMap = new HashMap<Integer,TestGatewayHelper>();
	
	private synchronized int getTestNumber() {
		int number = testNum;
		testNum++;
		return number;
	}
	
	@BeforeClass
	public static void oneTimeSetup() {
		
		final String METHOD = "oneTimeSetup";
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, null, null);
		
		try {
			apiClient = new APIClient(appProps);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		boolean exist = false;
		try {
			exist = apiClient.isDeviceTypeExist(GW_DEVICE_TYPE);
		} catch (IoTFCReSTException e1) {
			e1.printStackTrace();
		}
		
		if (!exist) {
			try {
				TestHelper.addGatewayType(apiClient, GW_DEVICE_TYPE);
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
				return;
			}
		}

		// Delete devices that were left in previous test run
		// Register gateway and attached devices ...
		for (int i=1; i<= totalTests; i++) {			
			String devId = new String(DEVICE_ID_PREFIX + "_" + i);
			String gwDevId = new String(GW_DEVICE_ID_PREFIX + "_" + i);
			
			try {
				TestGatewayHelper.deleteDevice(apiClient, GW_DEVICE_TYPE, devId);
			} catch (IoTFCReSTException e1) {
				e1.printStackTrace();
				continue; // Move to next test
			}
			try {
				TestGatewayHelper.deleteDevice(apiClient, GW_DEVICE_TYPE, gwDevId);
			} catch (IoTFCReSTException e1) {
				e1.printStackTrace();
				continue; //move to next test
			}
			
			// Register gateway device
			try {
				apiClient.registerDevice(GW_DEVICE_TYPE, gwDevId, TestEnv.getGatewayToken(), null, null, null);
				LoggerUtility.info(CLASS_NAME, METHOD, "Gateway device " + gwDevId + " has been created.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
			
			
			
			Integer iTest = new Integer(i);
			TestGatewayHelper gwHelper = null;
			//try {
				try {
					gwHelper = new TestGatewayHelper(GW_DEVICE_TYPE, gwDevId, DEVICE_TYPE, devId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				testMap.put(iTest, gwHelper);

				JsonArray arrayGroups = TestHelper.getResourceGroups(apiClient, gwHelper.getClientID());
				if (arrayGroups != null) {
					String sPrivilegedGW = "PD_PRIVILEGED_GW_DEVICE";
					JsonObject jsonRoles = new JsonObject();
					
					JsonArray arrayRoles = new JsonArray();
					JsonObject aRole = new JsonObject();
					aRole.addProperty("roleId", sPrivilegedGW);
					aRole.addProperty("roleStatus", 1);
					arrayRoles.add(aRole);
					jsonRoles.add("roles", arrayRoles);
					
					JsonObject rolesToGroups = new JsonObject();
					rolesToGroups.add(sPrivilegedGW, arrayGroups);
					jsonRoles.add("rolesToGroups", rolesToGroups);
					
					JsonObject updatedRoles = null;
					try {
						updatedRoles = apiClient.updateDeviceRoles(gwHelper.getClientID(), jsonRoles);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IoTFCReSTException e) {
						e.printStackTrace();
					}
					
					LoggerUtility.info(CLASS_NAME, METHOD, "Updated Roles : " + updatedRoles.toString());
					
					try {
						// Delay a few second before running tests
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			//} catch (Exception e) {
			//	e.printStackTrace();
			//}
		}
		
		for (int i=1; i <= totalTests; i++) {
			Integer iTest = new Integer(i);
			TestGatewayHelper testHelper = testMap.get(iTest);
			if (testHelper != null) {

				// Create test API key with comment = CLASS_NAME
				ArrayList<String> roles = new ArrayList<String>();
				String roleId = "PD_STANDARD_APP";
				roles.add(roleId);
				Properties newApiClientProps = TestHelper.createAPIKey(apiClient, CLASS_NAME, roles);

				try {
					testHelper.setAppProperties(newApiClientProps);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
		
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		final String METHOD = "oneTimeTearDown";
		
		if (apiClient != null) {
			
			TestGatewayHelper.deleteAPIKeys(apiClient, CLASS_NAME);
			
			for (int i=1; i<= totalTests; i++) {
				Integer iTest = new Integer(i);
				TestGatewayHelper testHelper = testMap.get(iTest);
				
				if (testHelper != null) {
					try {
						TestHelper.deleteDevice(apiClient, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId());
						TestHelper.deleteDevice(apiClient, testHelper.getGatewayDeviceType(), testHelper.getGatewayDeviceId());
					} catch (IoTFCReSTException e) {
						e.printStackTrace();
					}
				}
			}
			
    		try {
				apiClient.deleteDeviceType(GW_DEVICE_TYPE);
				LoggerUtility.info(CLASS_NAME, METHOD, "Gateway device type " + GW_DEVICE_TYPE + " deleted.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
    		try {
    			TestHelper.deleteDeviceType(apiClient, DEVICE_TYPE);
    			LoggerUtility.info(CLASS_NAME, METHOD, "Device type " + DEVICE_TYPE + " deleted.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}

		}
		
	}
	
	
	@Test
	public void test01GatewayAutoRegisterTest1() {
		
		final String METHOD = "test01GatewayAutoRegisterTest1";
		
		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);
		
		
		TestGatewayHelper testHelper = testMap.get(iTest);

		if (testHelper == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		ApplicationClient appClient = testHelper.getApplicationClient();
		if (appClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD + " : Application Client is null");
			return;
		}
		
		
		TestStatusCallback statusCallback = new TestStatusCallback();
		TestEventCallback eventCallback = new TestEventCallback();

		try {
			appClient.connect();
			appClient.setStatusCallback(statusCallback);
			appClient.setEventCallback(eventCallback);
		} catch (MqttException e) {
			String failMsg = "Application connect failed (" + e.getReasonCode()
					+ ") " + e.getMessage();
			LoggerUtility.warn(CLASS_NAME, METHOD, failMsg);
			e.printStackTrace();
			fail(failMsg);
			return;
		}

		String event = "register";
		
		JsonObject eventData = new JsonObject();
		eventData.addProperty("deviceId", testHelper.getAttachedDeviceId());
		
		appClient.subscribeToDeviceStatus(testHelper.getGatewayDeviceType(), testHelper.getGatewayDeviceId());
		appClient.subscribeToDeviceEvents(testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(), event);

		try {
			testHelper.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.warn(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			appClient.disconnect();
			return;
		}
		
		if (statusCallback.getDeviceStatus() != null) {
			LoggerUtility.info(CLASS_NAME, METHOD, statusCallback.getDeviceStatus().toString());
		}
		
		// Publish device event will auto register the type and device
		testHelper.publishEvent(DEVICE_TYPE, testHelper.getAttachedDeviceId(), event, eventData);		
		
		int count = 0;
		int max = 10;
		boolean registered = false;
		boolean eventReceived = false;
		// Check every second up to max seconds
		while((!registered || !eventReceived) && count++ <= max) {
			JsonObject response;
			try {
				Thread.sleep(1000); // Sleep 1 second
				if (eventCallback.getEvent() != null) {
					LoggerUtility.info(CLASS_NAME, METHOD, "Device last event: " + eventCallback.getEvent().toString());
					if (eventCallback.getEvent().getEvent().equals(event)) {
						eventReceived = true;
					}
				}
				if (statusCallback.getDeviceStatus() != null) {
					LoggerUtility.info(CLASS_NAME, METHOD, "Gateway last status: " + statusCallback.getDeviceStatus().toString());
				}

				response = apiClient.getDevicesConnectedThroughGateway(GW_DEVICE_TYPE, testHelper.getGatewayDeviceId());
				LoggerUtility.info(CLASS_NAME, METHOD, "Response: " + response.toString());
			} catch(InterruptedException e) {
				continue;
			} catch (IoTFCReSTException e) {
				if (count == max) {
					e.printStackTrace();
					String failMsg = "HTTP Code : " + e.getHttpCode() + " Response : "  + e.getResponse();
					fail(failMsg);
				}
				continue;
			}
			JsonArray devices = response.get("results").getAsJsonArray(); 

			for (Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject deviceJson = deviceElement.getAsJsonObject();
				if (deviceJson.get("typeId").getAsString().equals(testHelper.getAttachedDeviceType())) {
					if (deviceJson.get("deviceId").getAsString().equals(testHelper.getAttachedDeviceId())) {
						registered = true;
					}
				}
			}
			
		}
		
		testHelper.disconnect();
		appClient.disconnect();

		assertTrue("Device type(" + testHelper.getAttachedDeviceType() + ") (" + testHelper.getAttachedDeviceId() + ") was not registered", registered);
		assertTrue("Event from device type(" + testHelper.getAttachedDeviceType() + ") (" + testHelper.getAttachedDeviceId() + ") was not received", eventReceived);
		if (statusCallback.getDeviceStatus() != null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Gateway last status: " + statusCallback.getDeviceStatus().toString());
		}		
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting test #" + iTest);
	}
	
	
}
