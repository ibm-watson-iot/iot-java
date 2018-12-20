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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.app.ApplicationClient;
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
public class ApplicationCommandStatusSubscriptionTest1 {
	
	static Properties app1Props;
	static Properties app2Props;
	
	private final static String DEVICE_TYPE = "AppCmdSubTestType1";
	private final static String DEVICE_ID = "AppCmdSubTestDev1";
	private final static String APP_ID = "AppCmdSubTest";
	private final static String APP1_ID = "AppCmdSubTest1";
	private final static String APP2_ID = "AppCmdSubTest2";

	private static final String CLASS_NAME = ApplicationCommandStatusSubscriptionTest1.class.getName();
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
	
		app1Props = TestHelper.createAPIKey(apiClient, CLASS_NAME);
		
		if (app1Props != null) {
			app1Props.setProperty("id", APP1_ID);
		}

		app2Props = TestHelper.createAPIKey(apiClient, CLASS_NAME);
		
		if (app2Props != null) {
			app2Props.setProperty("id", APP2_ID);
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
	public void test01CommandSubscribe() {
		final String METHOD = "test01CommandSubscribe";

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		ApplicationClient app1Client = null;
		try {
			app1Client = new ApplicationClient(app1Props);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			app1Client.connect();
			LoggerUtility.info(CLASS_NAME, METHOD, app1Client.getClientID() + " connected ? " + app1Client.isConnected());
		} catch (Exception e) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Failed connect application " + e.getMessage());			
			fail(e.getMessage());
			return;
		}

		ApplicationClient app2Client = null;
		try {
			app2Client = new ApplicationClient(app2Props);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			app2Client.connect();
			LoggerUtility.info(CLASS_NAME, METHOD, app2Client.getClientID() + " connected ? " + app2Client.isConnected());
		} catch (Exception e) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Failed connect application " + e.getMessage());			
			fail(e.getMessage());
			app1Client.disconnect();
			return;
		}
		
		TestEventCallback evtCallback = new TestEventCallback();
		app1Client.setEventCallback(evtCallback);
		
		TestStatusCallback statusCallback = new TestStatusCallback();
		app1Client.setStatusCallback(statusCallback);
		
		app1Client.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID);
		
		JsonObject data = new JsonObject();
		data.addProperty("distance", 10);
		app2Client.publishCommand(DEVICE_TYPE, DEVICE_ID, "run", data);

		int count;
		Event event = evtCallback.getEvent();
		count = 0;
		while( event == null && count++ <= 10) {
			try {
				event = evtCallback.getEvent();
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		if (event != null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Command received by application: " + event);
		}
		assertTrue("Command is not received by application", (event != null));
				
		app2Client.disconnect();
		app1Client.disconnect();
	}	
	
}
