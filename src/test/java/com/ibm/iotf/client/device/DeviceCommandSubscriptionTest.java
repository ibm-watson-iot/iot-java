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
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.test.common.TestEnv;
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
	private final static String DEVICE_ID = "DevCmdSubID1";
	
	private static APIClient apiClient = null;
	private static DeviceClient devClient = null;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		
		final String METHOD = "oneTimeSetUp";
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, DEVICE_ID);
		try {
			apiClient = new APIClient(appProps);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Delete device if it was left from the last test run
		boolean exist = false;
		try {
			exist = apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID);
		} catch (IoTFCReSTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (exist) {
			try {
				apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
				LoggerUtility.info(CLASS_NAME, METHOD, METHOD + ": Device " + DEVICE_ID + " has been deleted.");
			} catch (IoTFCReSTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			exist = apiClient.isDeviceTypeExist(DEVICE_TYPE);
		} catch (IoTFCReSTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (!exist) {
			try {
				apiClient.addDeviceType(DEVICE_TYPE, null, null, null);
				LoggerUtility.info(CLASS_NAME, METHOD, METHOD + ": Device type " + DEVICE_TYPE + " has been created.");
			} catch (IoTFCReSTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			LoggerUtility.info(CLASS_NAME, METHOD, METHOD + ": Device type " + DEVICE_TYPE + " already existed.");
		}

		try {
			apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID, TestEnv.getDeviceToken(), null, null, null);
		} catch (IoTFCReSTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Properties devProps = TestEnv.getDeviceProperties(DEVICE_TYPE, DEVICE_ID);
		
		try {
			devClient = new DeviceClient(devProps);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Setup is complete.");
	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {
		final String METHOD = "oneTimeCleanup";
		if (apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
		}
		
		if (apiClient.isDeviceTypeExist(DEVICE_TYPE)) {
			apiClient.deleteDeviceType(DEVICE_TYPE);
		}
		LoggerUtility.info(CLASS_NAME, METHOD, "Cleanup is complete.");
	}	
	
	@Test
	public void testCommandReception() throws Exception {
		final String METHOD = "testCommandReception";
		
		if (devClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		MyCommandCallback callback = new MyCommandCallback();
		devClient.setCommandCallback(callback);
		//Connect to the IBM Watson IoT Platform	
		try {
			devClient.connect();
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			throw e1;
		} catch (java.lang.IllegalArgumentException e) {
			// looks like the property file is not modified, exit silently
			return;
		}
		
		// Ask application to publish the command to this device now
		publichCommand(1);
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		devClient.disconnect();
		
		assertTrue("Command is not received by the device", callback.commandReceived);
		LoggerUtility.info(CLASS_NAME, METHOD, "Command received : " + callback.commandReceived);
	}
	
	@Test
	public void testCustomCommandReception() throws Exception {
		
		final String METHOD = "testCustomCommandReception";
		if (devClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		MyCommandCallback callback = new MyCommandCallback();
		devClient.setCommandCallback(callback);
		//Connect to the IBM Watson IoT Platform	
		try {
			devClient.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		} catch (java.lang.IllegalArgumentException e) {
			String failMsg = METHOD + " connect IllegalArgumentException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		// Ask application to publish the command to this device now
		publichCommand(2);
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		devClient.disconnect();
		assertTrue("Device Event is not received by application", callback.commandReceived);
		LoggerUtility.info(CLASS_NAME, METHOD, "Command received : " + callback.commandReceived);
	}
	
	
	private void publichCommand(int type) {
		
		final String METHOD = "publichCommand";
		
		Properties props = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, DEVICE_ID);
		
		ApplicationClient myAppClient = null;
		try {
			myAppClient = new ApplicationClient(props);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			myAppClient.connect();
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (type == 1) { // JSON
			JsonObject data = new JsonObject();
			data.addProperty("name", "stop-rotation");
			data.addProperty("delay",  0);
			
			//Registered flow allows 0, 1 and 2 QoS
			myAppClient.publishCommand(DEVICE_TYPE, DEVICE_ID, "stop", data);
		} else {
			try {
				myAppClient.publishCommand(DEVICE_TYPE, DEVICE_ID, "stop", "rotation:80", "custom", 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		myAppClient.disconnect();
		LoggerUtility.info(CLASS_NAME, METHOD, "Connected " + myAppClient.isConnected());
	}

	
	//Implement the CommandCallback class to provide the way in which you want the command to be handled
	private static class MyCommandCallback implements CommandCallback {
		private boolean commandReceived = false;
		private static final String CLASS_NAME = MyCommandCallback.class.getName();
		
		/**
		 * This method is invoked by the library whenever there is command matching the subscription criteria
		 */
		@SuppressWarnings("deprecation")
		@Override
		public void processCommand(Command cmd) {
			final String METHOD = "processCommand";
			commandReceived = true;
			LoggerUtility.info(CLASS_NAME, METHOD, "Received command, name = "+cmd.getCommand() +
					", format = " + cmd.getFormat() + ", Payload = "+cmd.getPayload() + ", time = "+cmd.getTimestamp());
		}
	}
	
}
