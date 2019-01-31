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
import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.test.common.TestApplicationHelper;
import com.ibm.iotf.test.common.TestDeviceHelper;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestException;
import com.ibm.iotf.util.LoggerUtility;


/**
 * This test verifies that the event & device connectivity status are successfully received by the
 * application.
 *
 */
public class ApplicationNonDurableSubscriptionTest1 {
		
	private final static String DEVICE_TYPE = "NonDurableType1";
	private final static String DEVICE_ID_PREFIX = "NonDurableDev1";
	private final static String APP_ID = "NonDurableApp1";
	private final static int numApps = 2;
	private final static int numDevices = 10;
	private final static int numEvents = 10;
	private static ArrayList<TestApplicationHelper> appHelpers = null;
	private static ArrayList<TestDeviceHelper> deviceHelpers = null;

	private static final String CLASS_NAME = ApplicationNonDurableSubscriptionTest1.class.getName();
	private static APIClient apiClient = null;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {

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
				TestApplicationHelper.addDeviceType(apiClient, DEVICE_TYPE);
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}
		
		for (int i=1; i<=numDevices; i++) {
			String devId = DEVICE_ID_PREFIX + "_" + i;
			try {
				TestDeviceHelper.deleteDevice(apiClient, DEVICE_TYPE, devId);
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
			try {
				TestApplicationHelper.registerDevice(apiClient, DEVICE_TYPE, devId, TestEnv.getDeviceToken());
				if (deviceHelpers == null) {
					deviceHelpers = new ArrayList<TestDeviceHelper>();
				}
				deviceHelpers.add(new TestDeviceHelper(DEVICE_TYPE, devId));
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}
		
	
		for (int i=1; i<=numApps; i++) {
			Properties appProps = TestApplicationHelper.createAPIKey(apiClient, CLASS_NAME);
			if (appProps != null) {
				appProps.setProperty("id", APP_ID);
				appProps.setProperty("Clean-Session", "true");
				appProps.setProperty("Shared-Subscription", "true");
			}
			if (appHelpers == null) {
				appHelpers = new ArrayList<TestApplicationHelper>();
			}
			appHelpers.add(new TestApplicationHelper(appProps));
		}
		
		
	}
	
	@AfterClass
	public static void oneTimeCleanup() {
		if (apiClient != null) {
			
			if (deviceHelpers != null) {
				for (TestDeviceHelper deviceHelper : deviceHelpers) {
					try {
						TestDeviceHelper.deleteDevice(apiClient, deviceHelper.getDeviceType(), deviceHelper.getDeviceId());
					} catch (IoTFCReSTException e) {
						e.printStackTrace();
					}
				}
			}
			TestApplicationHelper.deleteAPIKeys(apiClient, CLASS_NAME);
		}
	}
	
	@Test
	public void test01EventNonDurableSubscription() {
		final String METHOD = "test01EventNonDurableSubscription";
		
		if (appHelpers == null || deviceHelpers == null) {
			fail("Test has not been intialized");
			return;
		}

		for (TestApplicationHelper appHelper : appHelpers) {
			try {
				appHelper.connect();
			} catch (MqttException e) {
				String failMsg = "Nondurable application failed to connect: " + e.getMessage();
				e.printStackTrace();
				fail(failMsg);
				return;
			} catch (TestException e) {
				e.printStackTrace();
			}
			LoggerUtility.info(CLASS_NAME, METHOD, "Application connected ? " + appHelper.getApplicationClient().isConnected());
			try {
				appHelper.subscribeToDeviceEvents(DEVICE_TYPE, "+", "+", "+", 1);
			} catch (TestException e) {
				e.printStackTrace();
			}
		}
		
		for (TestDeviceHelper deviceHelper : deviceHelpers) {
			try {
				deviceHelper.connect();
			} catch (MqttException e) {
				String failMsg = "Device failed to connect: " + e.getMessage();
				e.printStackTrace();
				fail(failMsg);
				return;
			}
		}

		int eventsSent = 0;
		for (int count=1; count <= numEvents; count++) {
			for (TestDeviceHelper deviceHelper : deviceHelpers) {
				JsonObject event = new JsonObject();
				event.addProperty("deviceId", deviceHelper.getDeviceId());
				event.addProperty("count", count);
				deviceHelper.publishEvent("nondurable", event, 1);
				eventsSent++;
			}			
		}
		
		int count = 0;
		int maxWait = 15;
		int eventsReceived = 0;
		while (count++ <= maxWait) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
			
			int appIndex = 0;
			eventsReceived = 0;
			for (TestApplicationHelper appHelper : appHelpers) {
				if (appHelper.getCallback() != null && appHelper.getCallback().getAllEvents() != null) {
					appIndex++;
					eventsReceived += appHelper.getCallback().getAllEvents().size();
					LoggerUtility.info(CLASS_NAME, METHOD, "Application " + appIndex + " received " + 
							appHelper.getCallback().getAllEvents().size() + " events");
				} else {
					LoggerUtility.warn(CLASS_NAME, METHOD, "Event callback is null for application #" + appIndex);
				}
			}
			if (eventsReceived == eventsSent) {
				break;
			}
		}
		
		for (TestDeviceHelper deviceHelper : deviceHelpers) {
			deviceHelper.disconnect();
		}
				
		int appIndex = 0;
		eventsReceived = 0;
		for (TestApplicationHelper appHelper : appHelpers) {
			appIndex++;
			if (appHelper.getCallback() != null && appHelper.getCallback().getAllEvents() != null) {
				eventsReceived += appHelper.getCallback().getAllEvents().size();
				LoggerUtility.info(CLASS_NAME, METHOD, "Application " + appIndex + " received " + 
						appHelper.getCallback().getAllEvents().size() + " events");
			}
			try {
				appHelper.disconnect();
			} catch (TestException e) {
				e.printStackTrace();
			}
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Sent " + eventsSent + " events,  Received " + eventsReceived + " events");
		
		
		// Wait for a few second before deleting API keys in cleanup method.
		// If we don't wait, we might receive notification connectionLost and retry to connect this client. 
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue("Incorrect count of received events by nondurable subscription", (eventsReceived == eventsSent));
		
	}	
	
}
