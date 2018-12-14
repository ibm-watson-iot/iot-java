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
package com.ibm.iotf.client.device;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.test.common.TestDeviceHelper;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestHelper;
import com.ibm.iotf.util.LoggerUtility;

/**
 * This test verifies that the device receives the command published by the application
 * successfully.
 *
 */
public class DeviceCommandSubscriptionTest {
	
	private final static String CLASS_NAME = DeviceCommandSubscriptionTest.class.getName();
	private final static String APP_ID = "DevCmdSubApp1";
	private final static String DEVICE_TYPE = "DevCmdSubType1";
	private final static String DEVICE_ID_PREFIX = "DevCmdSubID1";
	
	private static APIClient apiClient = null;
	private static int testNum = 1;
	private final static int totalTests = 1;
	
	private static HashMap<Integer,TestDeviceHelper> testMap = new HashMap<Integer,TestDeviceHelper>();
	
	private synchronized int getTestNumber() {
		int number = testNum;
		testNum++;
		return number;
	}
	
	@BeforeClass
	public static void oneTimeSetUp() {
		
		final String METHOD = "oneTimeSetUp";
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, null, null);
		try {
			apiClient = new APIClient(appProps);
		} catch (KeyManagementException e) {
			e.printStackTrace();
			return;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return;
		}
		
		boolean exist = false;
		
		try {
			exist = apiClient.isDeviceTypeExist(DEVICE_TYPE);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		
		if (!exist) {
			try {
				TestHelper.addDeviceType(apiClient, DEVICE_TYPE);
				LoggerUtility.info(CLASS_NAME, METHOD, "Device type " + DEVICE_TYPE + " has been created.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}
		
		for (int i=1; i<= totalTests; i++) {
			
			String devId = new String(DEVICE_ID_PREFIX + "_" + i);
			try {
				TestDeviceHelper.deleteDevice(apiClient, DEVICE_TYPE, devId);
			} catch (IoTFCReSTException e2) {
				e2.printStackTrace();
				continue; // Move to next test
			}
			
			try {
				TestHelper.registerDevice(apiClient, DEVICE_TYPE, devId, TestEnv.getDeviceToken());
				Integer iTest = new Integer(i);
				try {
					TestDeviceHelper testHelper = new TestDeviceHelper(DEVICE_TYPE, devId);
					testMap.put(iTest, testHelper);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (IoTFCReSTException e1) {
				e1.printStackTrace();
			}
		}
		
		for (int i=1; i<= totalTests; i++) {
			Integer iTest = new Integer(i);
			TestDeviceHelper testHelper = testMap.get(iTest);
			
			if (testHelper != null) {
				// Create test API key with comment = CLASS_NAME
				ArrayList<String> roles = new ArrayList<String>();
				String roleId = "PD_STANDARD_APP";
				roles.add(roleId);
				Properties newApiClientProps = TestHelper.createAPIKey(apiClient, CLASS_NAME, roles);
				
				if (newApiClientProps != null) {
					newApiClientProps.setProperty("id", APP_ID + iTest);
					String apiKey = newApiClientProps.getProperty("API-Key");
					LoggerUtility.info(CLASS_NAME, METHOD, "New test API Key : " + apiKey);
					
					try {
						testHelper.setAppProperties(newApiClientProps);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}							
			}
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Setup is complete.");
	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {
		final String METHOD = "oneTimeCleanup";
		for (int i=1; i<= totalTests; i++) {
			Integer iTest = new Integer(i);
			TestDeviceHelper testHelper = testMap.get(iTest);
			
			if (testHelper != null) {
				TestHelper.deleteDevice(apiClient, testHelper.getDeviceType(), testHelper.getDeviceId());
			}
		}
		LoggerUtility.info(CLASS_NAME, METHOD, "Cleanup is complete.");
	}	
	
	@Test
	public void test01DeviceCommandSubscription() {
		final String METHOD = "test01DeviceCommandSubscription";

		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);
		
		TestDeviceHelper testHelper = testMap.get(iTest);
		
		if (testHelper == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);			
			return;
		}
		
		try {
			testHelper.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		testHelper.appPublishCommand(testHelper.getDeviceType(), testHelper.getDeviceId(), null, null);
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnect();
		
		assertTrue("Command is not received by the device", testHelper.commandReceived());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Command received : " + testHelper.commandReceived());
	}
	
}
