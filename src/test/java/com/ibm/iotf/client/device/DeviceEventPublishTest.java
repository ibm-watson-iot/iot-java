/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Surbhi Agarwal - Initial contribution
 * Sathiskumar P - Added quickstart flow
 *****************************************************************************
 */

package com.ibm.iotf.client.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.api.APIClient.ContentType;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.test.common.TestDeviceHelper;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestHelper;
import com.ibm.iotf.util.LoggerUtility;

import junit.framework.TestCase;

/**
 * This Test verifies whether one can publish event successfully to the quickstart 
 * and registered service (if property file is provided).
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceEventPublishTest {
	
	private final static String CLASS_NAME = DeviceEventPublishTest.class.getName();
	private final static String APP_ID = "DevEvPubApp1";
	private final static String DEVICE_TYPE = "DevEvPubType1";
	private final static String DEVICE_ID_PREFIX = "DevEvPubDev1";
	
	private static APIClient apiClient = null;
	private static int testNum = 1;
	private final static int totalTests = 9;
	
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
	
	
	public void testQuickstartPublish(){

		//Provide the device specific data using Properties class
		Properties options = new Properties();
		options.setProperty("org", "quickstart");
		options.setProperty("type", "iotsample-arduino");
		options.setProperty("id", "00aabbccde03");
				
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		//Connect to the IBM Watson IoT Platform
		try {
			myClient.connect(true);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
		
		//Quickstart flow allows only QoS = 0
		myClient.publishEvent("status", event, 0);
		System.out.println("SUCCESSFULLY POSTED......");

		//Disconnect cleanly
		myClient.disconnect();
	}

	@Test
	public void test01RegisteredPublish() {
		final String METHOD = "test01RegisteredPublish";

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
			
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
		
		boolean code = testHelper.publishEvent("blink", event);
		
		testHelper.disconnect();
		assertTrue("Failed to publish the event......", code);
	}
	
	@Test
	public void test02RegisteredPublishWithEmptyObject(){			
		final String METHOD = "test02RegisteredPublishWithEmptyObject";

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
		
		boolean code = testHelper.publishEvent("blink", null);
		
		testHelper.disconnect();
		assertTrue("Failed to publish the event......", code);
	}
	
	/**
	 * NegativeTest - try to publish after disconnect, it should return immediately 
	 */
	@Test
	public void test03PublishAfterDisconnect(){			
		final String METHOD = "test03PublishAfterDisconnect";

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
		
		testHelper.disconnect();
		
		boolean code = testHelper.publishEvent("blink", null);

		assertFalse("Successfully publish the event even after disconnect......", code);
	}
	
	/**
	 * This test publishes the event in QoS 0
	 */
	@Test
	public void test04RegisteredPublishQos0(){			
		final String METHOD = "test04RegisteredPublishQos0";

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

		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
		
		boolean code = testHelper.publishEvent("blink", event, 0);
		
		testHelper.disconnect();
		
		assertTrue("Failed to publish the event......", code);
	}
	
	/**
	 * This test publishes the event in QoS 2
	 * @throws Exception 
	 */
	@Test
	public void test05CustomPublishQos2() {			
		final String METHOD = "test05CustomPublishQos2";

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

		
		boolean code = false;
		try {
			code = testHelper.publishEvent("blink", new byte[]{1, 2, 3, 4, 54}, "binary", 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		testHelper.disconnect();
		assertTrue("Failed to publish the event......", code);
	}
	
	/**
	 * 
	 * This test publishes the event in QoS 2
	 * @throws Exception 
	 */
	@Test
	public void test06CustomPublishString() {			
		final String METHOD = "test06CustomPublishString";

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
			
		boolean code = false;
		try {
			code = testHelper.publishEvent("blink", "cpu:90", "binary", 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		testHelper.disconnect();
		assertTrue("Failed to publish the event......", code);
	}
	
	/**
	 * Test application can publish event on behalf of a device.
	 */
	public void testApplicationEventPublishOnBehalfOfDevice() {			

		/*
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
					
		boolean code = myClient.publishEvent(deviceType, deviceId, "blink", event, 2);
		myClient.disconnect();
		assertTrue("Failed to publish the event......", code);
		*/
	}
	
	/**
	 * Test device client can publish event over HTTP protocol.
	 */
	@Test
	public void test07RegisteredPublishHttp(){			
		final String METHOD = "test07RegisteredPublishHttp";

		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);
		
		TestDeviceHelper testHelper = testMap.get(iTest);
		if (testHelper == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);			
			return;
		}
		
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
					
		boolean code = false;
		try {
			code = testHelper.publishDeviceEventOverHTTP("blink", event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals("Failed to publish the event......", true, code);
	}

	/**
	 * Test that device can publish event over HTTP protocol
	 */
	@Test
	public void test08RegisteredPublishHttp_stringnonJSON(){			
		final String METHOD = "test08RegisteredPublishHttp_stringnonJSON";

		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);
		
		TestDeviceHelper testHelper = testMap.get(iTest);
		if (testHelper == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);			
			return;
		}
		

		//Publish nonJSON string
		String simpleString = "Hello World";
		boolean code = false;
		try {
			code = testHelper.publishDeviceEventOverHTTP("blink", simpleString);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals("Failed to publish the event......", true, code);
	}
	
	/**
	 * This test expects a properties file containing the device registration details. Failing to
	 * provide the same, the test will return immediately and show as passed.
	 */
	@Test
	public void test09RegisteredPublishHttp_new(){			
		final String METHOD = "test09RegisteredPublishHttp_new";

		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);
		
		TestDeviceHelper testHelper = testMap.get(iTest);
		if (testHelper == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);			
			return;
		}

		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
					
		boolean code = false;
		try {
			code = testHelper.publishDeviceEventOverHTTP("blink", event, ContentType.json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals("Failed to publish the event......", true, code);
	}
	
	public void testApplicationEventPublishOnBehalfOfDeviceOverHTTP(){			

		/*
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
					
		boolean code = false;
		try {
			code = myClient.api().publishApplicationEventforDeviceOverHTTP(deviceId, deviceType, "blink", event);
		} catch (java.lang.IllegalArgumentException e) {
			// looks like the proerties file is not edited, just ignore
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals("Failed to publish the event......", true, code);
		*/
	}
	
	public void testApplicationEventPublishOnBehalfOfDeviceOverHTTP_new(){			
			
		/*
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
					
		boolean code = false;
		try {
			code = myClient.api().publishApplicationEventforDeviceOverHTTP(deviceType, deviceId, "blink", event);
		} catch (java.lang.IllegalArgumentException e) {
			// looks like the proerties file is not edited, just ignore
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals("Failed to publish the event......", true, code);
		*/
	}
	
	private static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}

}
