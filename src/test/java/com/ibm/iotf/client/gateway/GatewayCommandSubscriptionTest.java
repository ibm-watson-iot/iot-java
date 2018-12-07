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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.*;

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
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayCommandSubscriptionTest {
	
	private static final String CLASS_NAME = GatewayCommandSubscriptionTest.class.getName();
	private static final String APP_ID = "GwCmdSubApp1";
	
	private final static String DEVICE_TYPE = "SubType1";
	private final static String DEVICE_ID_PREFIX = "SubDev";
	private final static String GW_DEVICE_TYPE = "GwCmdSubType1";
	private final static String GW_DEVICE_ID_PREFIX = "GwCmdSubDev";

	private static APIClient apiClient = null;
	private static ApplicationClient mqttAppClient = null;
	
	private static int testNum = 1;
	private final static int totalTests = 10;
	
	private static HashMap<Integer,TestHelper> testMap = new HashMap<Integer,TestHelper>();
	
	private synchronized int getTestNumber() {
		int number = testNum;
		testNum++;
		return number;
	}
	
	private static void deleteDevice(String devType, String devId) {
		final String METHOD = "deleteDevices";
		boolean exist = false;
		try {
			exist = apiClient.isDeviceExist(devType, devId);
		} catch (IoTFCReSTException e1) {
			e1.printStackTrace();
		}
		
		if (exist) {
			try {
				apiClient.deleteDevice(devType, devId);
				LoggerUtility.info(CLASS_NAME, METHOD, "Device ID " + devId + " has been deleted.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}			
	}
	
	@BeforeClass
	public static void oneTimeSetup() {
		
		final String METHOD = "oneTimeSetup";
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, null);
		
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
			JsonObject jsonGW = new JsonObject();
			jsonGW.addProperty("id", GW_DEVICE_TYPE);
			try {
				apiClient.addGatewayDeviceType(jsonGW);
				LoggerUtility.info(CLASS_NAME, METHOD, "Gateway device type " + GW_DEVICE_TYPE + " has been created.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}

		try {
			exist = apiClient.isDeviceTypeExist(DEVICE_TYPE);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		
		if (!exist) {
			try {
				apiClient.addDeviceType(DEVICE_TYPE, null, null, null);
				LoggerUtility.info(CLASS_NAME, METHOD, "Device type " + DEVICE_TYPE + " has been created.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}
		
		for (int i=1; i<= totalTests; i++) {			
			String devId = new String(DEVICE_ID_PREFIX + "_" + i);
			String gwDevId = new String(GW_DEVICE_ID_PREFIX + "_" + i);
			
			deleteDevice(DEVICE_TYPE, devId);
			deleteDevice(GW_DEVICE_TYPE, gwDevId);
			
			// Register gateway device
			try {
				apiClient.registerDevice(GW_DEVICE_TYPE, gwDevId, TestEnv.getGatewayToken(), null, null, null);
				LoggerUtility.info(CLASS_NAME, METHOD, "Gateway device " + gwDevId + " has been created.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
			
			// Register device under gateway
			try {
				apiClient.registerDeviceUnderGateway(DEVICE_TYPE, devId, GW_DEVICE_TYPE, gwDevId);
				LoggerUtility.info(CLASS_NAME, METHOD, "Device " + devId + " has been created.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
			
			Integer iTest = new Integer(i);
			TestHelper testHelper = null;
			try {
				testHelper = new TestHelper(GW_DEVICE_TYPE, gwDevId, DEVICE_TYPE, devId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			testMap.put(iTest, testHelper);
		}
		LoggerUtility.info(CLASS_NAME, METHOD, METHOD + " setup is complete");
		
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		final String METHOD = "oneTimeTearDown";
		
		if (apiClient != null) {
			for (int i=1; i<= totalTests; i++) {
				Integer iTest = new Integer(i);
				TestHelper testHelper = testMap.get(iTest);
				
				if (testHelper != null) {
					deleteDevice(testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId());
					deleteDevice(testHelper.getGatewayDeviceType(), testHelper.getGatewayDeviceId());
				}
			}
    		try {
				apiClient.deleteDeviceType(DEVICE_TYPE);
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
    		try {
				apiClient.deleteDeviceType(GW_DEVICE_TYPE);
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
    	}
		
	}
	
	
	@Test
	public void test01GatewayCommandSubscription() {
		
		final String METHOD = "test01GatewayCommandSubscription";
		
		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);
		
		TestHelper testHelper = testMap.get(iTest);
		
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
		
		// Ask application to publish the command to this gateway now
		publishCommand(iTest, testHelper.getGatewayDeviceType(), testHelper.getGatewayDeviceId(), 
				null, null, null, null);
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnect();
		assertTrue("The command is not received by gateway", testHelper.commandReceived());
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting test #" + iTest);
	}
	
	@Test
	public void test02DeviceAllCommandsSubscription() {
		
		final String METHOD = "test02DeviceAllCommandsSubscription";
		
		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);

		TestHelper testHelper = testMap.get(iTest);
		
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
		
		testHelper.subscribeCommands();
		
		// Ask application to publish the command to attached device
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				null, null);
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnect();
		
		assertTrue("The command is not received by gateway", testHelper.commandReceived());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting test #" + iTest);
	}
	
	@Test
	public void test03DeviceCommandSubscriptionWithFormat() throws MqttException{
		
		final String METHOD = "test03DeviceCommandSubscriptionWithFormat";

		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);

		TestHelper testHelper = testMap.get(iTest);
		
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
		
		String cmd = "stop";
		String format = "json";
		testHelper.subscribeCommand(cmd, format);
		
		// Ask application to publish the command to attached device
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnect();
		
		assertTrue("The command is not received by gateway", testHelper.commandReceived());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting test #" + iTest);
	}
	
	@Test
	public void test04DeviceCommandSubscriptionWithFormatAndQOS() throws MqttException{
		
		final String METHOD = "test04DeviceCommandSubscriptionWithFormatAndQOS";
		
		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);

		TestHelper testHelper = testMap.get(iTest);
		
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
		
		String cmd = "stop";
		String format = "json";
		int qos = 2;
		testHelper.subscribeCommand(cmd, format, qos);

		// Ask application to publish the command to attached device
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnect();
		
		assertTrue("The command is not received by gateway", testHelper.commandReceived());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting test #" + iTest);
	}

	
	@Test
	public void test05GatewayNotification() throws MqttException{
		
		final String METHOD = "test05GatewayNotification";
		
		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);

		TestHelper testHelper = testMap.get(iTest);
		
		if (testHelper == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}

		String newGwType = "GwNotifyType";
		boolean exist = false;
		try {
			exist = apiClient.isDeviceTypeExist(newGwType);
			
		} catch (IoTFCReSTException e1) {
			e1.printStackTrace();
		}
		
		if (!exist) {
			JsonObject jsonGW = new JsonObject();
			jsonGW.addProperty("id", newGwType);
			try {
				apiClient.addGatewayDeviceType(jsonGW);
				LoggerUtility.info(CLASS_NAME, METHOD, "Gateway device type " + newGwType + " has been created.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}
		
		String newGwId = "GwNotifyId1";
		try {
			apiClient.registerDevice(newGwType, newGwId, TestEnv.getGatewayToken(), null, null, null);
		} catch (IoTFCReSTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		
		testHelper.subscribeNotification();
		
		
		JsonObject jsonData = new JsonObject();
		jsonData.addProperty("test", iTest.intValue());
		
		// Publish event to on behalf of new gateway should not be authorized
		testHelper.publishEvent(newGwType, newGwId, "test", jsonData);
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.notificationReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnect();
		
		deleteDevice(newGwType, newGwId);
		
		assertTrue("Notification was not received by gateway", testHelper.notificationReceived());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting test #" + iTest);
		
	}
	
	@Test
	public void test06DeviceSpecificCommandSubscription() throws MqttException{
		
		final String METHOD = "test06DeviceSpecificCommandSubscription";
		
		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);

		TestHelper testHelper = testMap.get(iTest);
		
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

		String cmd = "start";
		testHelper.subscribeCommand(cmd);
		
		// Ask application to publish the command to attached device
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnect();
		
		assertTrue("The command is not received by gateway", testHelper.commandReceived());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting test #" + iTest);
	}

	@Test
	public void test07DeviceSpecificCommandSubscriptionAndQOS() throws MqttException{
		
		final String METHOD = "test07DeviceSpecificCommandSubscriptionAndQOS";
		
		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);

		TestHelper testHelper = testMap.get(iTest);
		
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

		String cmd = "start";
		int qos = 2;
		testHelper.subscribeCommand(cmd, qos);
		
		// Ask application to publish the command to attached device
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("The command is not received by gateway", testHelper.commandReceived());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting test #" + iTest);
	}
	

	@Test
	public void test08DeviceAllCommandsUnsubscription() throws MqttException{
		
		final String METHOD = "test08DeviceAllCommandsUnsubscription";
		
		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);

		TestHelper testHelper = testMap.get(iTest);
		
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

		testHelper.subscribeCommands();
		
		// Ask application to publish the command to attached device
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				null, null);
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("The command is not received by gateway", testHelper.commandReceived());
		
		count = 0;
		
		testHelper.clear();
		
		testHelper.unsubscribeCommands();
		
		// Ask application to publish the command to attached device
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				null, null);
		
		
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnect();
		
		assertFalse("The command should not be received by gateway", testHelper.commandReceived());
				
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting test #" + iTest);
	}
	
	@Test
	public void test09DeviceSpecificCommandUnsubscription() throws MqttException{
		
		final String METHOD = "test09DeviceSpecificCommandUnsubscription";
		
		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);

		TestHelper testHelper = testMap.get(iTest);
		
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

		String cmd = "start";
		testHelper.subscribeCommand(cmd);
		
		// Ask application to publish the command to attached device
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("The command is not received by gateway", testHelper.commandReceived());
		
		count = 0;

		testHelper.clear();
		
		testHelper.unsubscribeCommand(cmd);

		// Ask application to publish the command to attached device
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnect();
		assertFalse("The command should not be received by gateway", testHelper.commandReceived());
				
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting test #" + iTest);
	}
	
	@Test
	public void test10DeviceCommandUnsubscriptionWithFormat() throws MqttException{
		
		final String METHOD = "test10DeviceCommandUnsubscriptionWithFormat";
		
		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);

		TestHelper testHelper = testMap.get(iTest);
		
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
		
		String cmd = "stop";
		String format = "json";
		testHelper.subscribeCommand(cmd, format);
		
		// Ask application to publish the command to attached device
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("The command is not received by gateway", testHelper.commandReceived());
		
		count = 0;
		
		testHelper.clear();
		testHelper.unsubscribeCommand(cmd, format);

		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnect();
		
		assertFalse("The command should not be received by gateway", testHelper.commandReceived());
				
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting test #" + iTest);
	}
	
	
	/**
	 * Publish a command to gateway or attached device
	 * 
	 * @param gwType Gateway device type, can be null
	 * @param gwDevId Gateway device ID, can be null
	 * @param devType Device type, can be null
	 * @param devId Devid ID, can be null
	 * @param cmdName Command to send, can be null
	 * @param jsonCmd Json object for the command, can be null
	 */
	private void publishCommand(
			Integer iTest,
			String gwType, String gwDevId, 
			String devType, String devId, 
			String cmdName, JsonObject jsonCmd) {
		
		final String METHOD = "publichCommand";
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running " + METHOD);
		
		Properties props = TestEnv.getAppProperties(APP_ID + iTest , false, null, null);
		
		ApplicationClient myAppClient = null;
		
		try {
			myAppClient = new ApplicationClient(props);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			myAppClient.connect();
		} catch (MqttException e) {
			e.printStackTrace();
		}

		if (cmdName == null) {
			// use default command name 
			cmdName = "stop";
		}
		
		if (jsonCmd == null) {
			jsonCmd = new JsonObject();
			jsonCmd.addProperty("name", "stop-rotation");
			jsonCmd.addProperty("delay", 0);
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, myAppClient.getClientID() + " publish command " + cmdName);
		
		if (gwType != null && gwDevId != null) {
			mqttAppClient.publishCommand(gwType, gwDevId, cmdName, jsonCmd);
		} else {
			mqttAppClient.publishCommand(devType, devId, cmdName, jsonCmd);
		}
		
		myAppClient.disconnect();
		LoggerUtility.info(CLASS_NAME, METHOD, myAppClient.getClientID() + " Connected ? " + myAppClient.isConnected());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting " + METHOD);
	}

	
	//Implement the CommandCallback class to provide the way in which you want the command to be handled
	private static class GatewayCommandCallback implements GatewayCallback{
		private boolean commandReceived = false;
		private boolean notificationReceived = false;
		private static final String CLASS_NAME = GatewayCommandCallback.class.getName();
		
		/**
		 * This method is invoked by the library whenever there is command matching the subscription criteria
		 */
		@Override
		public void processCommand(com.ibm.iotf.client.gateway.Command cmd) {
			final String METHOD = "processCommand";
			commandReceived = true;
			LoggerUtility.info(CLASS_NAME, METHOD, "Received command, name = "+cmd.getCommand() +
					", format = " + cmd.getFormat() + ", Payload = "+cmd.getPayload() + ", time = "+cmd.getTimestamp());
		}

		@Override
		public void processNotification(Notification notification) {
			final String METHOD = "processNotification";
			notificationReceived = true;
			LoggerUtility.info(CLASS_NAME, METHOD, "Received notification, Device Type (" 
					+ notification.getDeviceType() + ") Device ID ("
					+ notification.getDeviceId() + ")");
		}
		
		private void clear() {
			commandReceived = false;
			notificationReceived = false;
		}
	}
	
	private static class TestHelper {
		String gwDevType = null;
		String gwDevId = null;
		String devType = null;
		String devId = null;
		
		GatewayClient gwClient = null;
		GatewayCommandCallback callback = null;
		
		
		public TestHelper(String gwDevType, String gwDevId, String devType, String devId) throws Exception {
			this.gwDevType = gwDevType;
			this.gwDevId = gwDevId;
			this.devType = devType;
			this.devId = devId;
			Properties props = TestEnv.getGatewayProperties(gwDevType, gwDevId);
			gwClient = new GatewayClient(props);
			callback = new GatewayCommandCallback();
			gwClient.setGatewayCallback(callback);
		}
		
		public String getGatewayDeviceType() { return gwDevType; }
		public String getGatewayDeviceId() { return gwDevId; }
		public String getAttachedDeviceType() { return devType; }
		public String getAttachedDeviceId() { return devId; }
		
		public void connect() throws MqttException {
			gwClient.connect();
		}
		
		public void disconnect() {
			gwClient.disconnect();
		}
		
		public boolean commandReceived() {
			return callback.commandReceived;
		}
		
		public boolean notificationReceived() {
			return callback.notificationReceived;
		}
		
		public void clear() {
			callback.clear();
		}
		
		public void subscribeNotification() {
			gwClient.subscribeToGatewayNotification();
		}
		
		public void subscribeCommands() {
			gwClient.subscribeToDeviceCommands(devType, devId);
		}
		
		public void subscribeCommand(String command) {
			gwClient.subscribeToDeviceCommands(devType, devId, command);
		}

		public void subscribeCommand(String command, int qos) {
			gwClient.subscribeToDeviceCommands(devType, devId, command, qos);
		}
		
		public void subscribeCommand(String command, String format) {
			gwClient.subscribeToDeviceCommands(devType, devId, command, format);
		}

		public void subscribeCommand(String command, String format, int qos) {
			gwClient.subscribeToDeviceCommands(devType, devId, command, format, qos);
		}

		public void publishEvent(String devType, String devId, String event, JsonObject jsonData) {
			gwClient.publishDeviceEvent(devId, devId, event, jsonData);
		}

		public void unsubscribeCommands() {
			gwClient.unsubscribeFromDeviceCommands(devType, devId);
		}

		public void unsubscribeCommand(String command) {
			gwClient.unsubscribeFromDeviceCommands(devType, devId, command);
		}

		public void unsubscribeCommand(String command, String format) {
			gwClient.unsubscribeFromDeviceCommands(devType, devId, command, format);
		}

	}
}
