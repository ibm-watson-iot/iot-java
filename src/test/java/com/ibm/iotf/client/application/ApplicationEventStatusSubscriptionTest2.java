/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 Prasanna A Mathada - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.application;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.DeviceStatus;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.test.common.TestDeviceHelper;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestEventCallback;
import com.ibm.iotf.test.common.TestHelper;
import com.ibm.iotf.test.common.TestStatusCallback;
import com.ibm.iotf.util.LoggerUtility;


/**
 * This test verifies that the event & device connectivity status are successfully received by the
 * application.
 *
 */
public class ApplicationEventStatusSubscriptionTest2 {
	
	static Properties appProps;
	
	private final static String DEVICE_TYPE = "AppEvtSubTestType2";
	private final static String DEVICE_ID = "AppEvtSubTestDev2";
	private final static String APP_ID = "AppEvtSubTest";
	private final static String APP1_ID = "AppEvtSubTest2";

	private static final String CLASS_NAME = ApplicationEventStatusSubscriptionTest2.class.getName();
	private static APIClient apiClient = null;

	@BeforeClass
	public static void oneTimeSetUp() {
		final String METHOD = "oneTimeSetUp";
		LoggerUtility.info(CLASS_NAME, METHOD, "Setting up device type (" + DEVICE_TYPE + ") ID(" + DEVICE_ID + ")");

		Properties apiProps = TestEnv.getAppProperties(APP_ID, false, null, null);
		try {
			apiClient = new APIClient(apiProps);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}
		
		try {
			TestDeviceHelper.deleteDevice(apiClient, DEVICE_TYPE, DEVICE_ID);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		
		try {
			TestHelper.registerDevice(apiClient, DEVICE_TYPE, DEVICE_ID, TestEnv.getDeviceToken());
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
	
		appProps = TestHelper.createAPIKey(apiClient, CLASS_NAME);
		
		if (appProps != null) {
			appProps.setProperty("id", APP1_ID);
		}
		
	}
	
	@AfterClass
	public static void oneTimeCleanup() {
		if (apiClient != null) {
			try {
				TestDeviceHelper.deleteDevice(apiClient, DEVICE_TYPE, DEVICE_ID);
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
			
			TestHelper.deleteAPIKeys(apiClient, CLASS_NAME);
		}
	}
	
	@Test
	public void test01EventSubscribeDeviceType() {
		final String METHOD = "test01EventSubscribeDeviceType";

		TestDeviceHelper testHelper;
		try {
			testHelper = new TestDeviceHelper(DEVICE_TYPE, DEVICE_ID);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
		
		ApplicationClient appClient = null;
		try {
			appClient = new ApplicationClient(appProps);			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			appClient.connect();
			LoggerUtility.info(CLASS_NAME, METHOD, appClient.getClientID() + " connected ? " + appClient.isConnected());			
		} catch (Exception e) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Failed connect application " + e.getMessage());			
			fail(e.getMessage());
			return;
		}
		
		TestEventCallback evtCallback = new TestEventCallback();
		appClient.setEventCallback(evtCallback);
		
		TestStatusCallback statusCallback = new TestStatusCallback();
		appClient.setStatusCallback(statusCallback);
		
		appClient.subscribeToDeviceEvents(DEVICE_TYPE, DEVICE_ID);
		LoggerUtility.info(CLASS_NAME, METHOD, appClient.getClientID() + " subscribed to device events");
		
		appClient.subscribeToDeviceStatus(DEVICE_TYPE, DEVICE_ID);
		LoggerUtility.info(CLASS_NAME, METHOD, appClient.getClientID() + " subscribed to device status");
		
		
		try {
			testHelper.connect();
		} catch (MqttException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		int count = 0;
		DeviceStatus status = statusCallback.getDeviceStatus();
		count = 0;
		while( status == null && count++ <= 10) {
			try {
				status = statusCallback.getDeviceStatus();
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		if (status != null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Connect status received by application: " + status);
		}
		
		assertTrue("Device Status Connect is not received by application", (status != null));
		
		statusCallback.clear();
		
		JsonObject data = new JsonObject();
		data.addProperty("device", DEVICE_ID);
		testHelper.publishEvent("worker", null);
		
		Event event = evtCallback.getEvent();
		count = 0;
		while( event == null && count++ <= 10) {
			try {
				event = evtCallback.getEvent();
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		if (event != null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Event received by application: " + event);
		}
		assertTrue("Device Event is not received by application", (event != null));
		
		
		testHelper.disconnect();
		
		count = 0;
		while( status == null && count++ <= 10) {
			try {
				status = statusCallback.getDeviceStatus();
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		if (status != null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Disconnect status received by application: " + status);
		}
		
		assertTrue("Device Status Disconnect is not received by application", (status != null));
		
		appClient.disconnect();
		// Wait for a few second before deleting API keys in cleanup method.
		// If we don't wait, we might receive notification connectionLost and retry to connect this client. 
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}	
	
}
