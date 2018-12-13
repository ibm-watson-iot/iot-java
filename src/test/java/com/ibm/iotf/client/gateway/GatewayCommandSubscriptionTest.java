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

import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestHelper;
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
	private static int testNum = 1;
	private final static int totalTests = 10;
	
	private static HashMap<Integer,TestHelper> testMap = new HashMap<Integer,TestHelper>();
	
	private synchronized int getTestNumber() {
		int number = testNum;
		testNum++;
		return number;
	}
	
	@BeforeClass
	public static void oneTimeSetup() {
		
		final String METHOD = "oneTimeSetup";
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, null);
		
		String apiKey = appProps.getProperty("API-Key");
		String[] tokens = apiKey.split("-");
		for (String token : tokens) {
			LoggerUtility.info(CLASS_NAME, METHOD, "API Key token : " + token);
		}
		
		try {
			apiClient = new APIClient(appProps);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		try {
			JsonObject jsonResult = apiClient.getGetAPIKeyRoles((String)null);
			if (jsonResult != null && jsonResult.has("results")) {
				LoggerUtility.info(CLASS_NAME, METHOD, "This API Key roles : " + jsonResult);
			}
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		} catch (IoTFCReSTException e2) {
			e2.printStackTrace();
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
			
			TestHelper.deleteDevice(apiClient, DEVICE_TYPE, devId);
			TestHelper.deleteDevice(apiClient, GW_DEVICE_TYPE, gwDevId);
			
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
		
		for (int i=1; i <= totalTests; i++) {
			Integer iTest = new Integer(i);
			TestHelper testHelper = testMap.get(iTest);
			if (testHelper != null) {
				try {
					exist = apiClient.isDeviceExist(testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId());
					LoggerUtility.info(CLASS_NAME, METHOD, "Device " + testHelper.getAttachedDeviceId() + " does exist.");
					exist = apiClient.isDeviceExist(testHelper.getGatewayDeviceType(), testHelper.getGatewayDeviceId());
					LoggerUtility.info(CLASS_NAME, METHOD, "Gateway device " + testHelper.getGatewayDeviceId() + " does exist.");
					
					JsonObject jsonResult = null;
					
					// Get gateway device details
					/*
					jsonResult = apiClient.getDevice(testHelper.getGatewayDeviceType(), testHelper.getGatewayDeviceId());
					if (jsonResult != null) {
						LoggerUtility.info(CLASS_NAME, METHOD, testHelper.getGatewayDeviceId() + " details : " 
								+ jsonResult);
					}
					*/
					
					// Get Resource Group Info
					try {
						jsonResult = apiClient.getAccessControlProperties(testHelper.getClientID(), null);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					if (jsonResult != null && jsonResult.has("results")) {
						LoggerUtility.info(CLASS_NAME, METHOD, testHelper.getGatewayDeviceId() + " access control : " 
								+ jsonResult);
						String groupId = "gw_def_res_grp:" + TestEnv.getOrgId() 
								+ ":"+ testHelper.getGatewayDeviceType() + ":" + testHelper.getGatewayDeviceId();
						
						
						// Assign device to the resource group
						JsonArray jarrayDevices = new JsonArray();
						JsonObject aDevice = new JsonObject();
						aDevice.addProperty("typeId", testHelper.getAttachedDeviceType());
						aDevice.addProperty("deviceId", testHelper.getAttachedDeviceId());
						jarrayDevices.add(aDevice);
						
						try {
							apiClient.assignDevicesToResourceGroup(groupId, jarrayDevices);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						
						try {
							jsonResult = apiClient.getDevicesInResourceGroup(groupId, (String)null);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						if (jsonResult != null) {
							LoggerUtility.info(CLASS_NAME, METHOD, groupId + " has devices : " + jsonResult);
						}
						
						// Create test API key with comment = CLASS_NAME
						ArrayList<String> roles = new ArrayList<String>();
						String roleId = "PD_STANDARD_APP";
						roles.add(roleId);
						Properties newApiClientProps = TestHelper.createAPIKey(apiClient, CLASS_NAME, roles);
						
						if (newApiClientProps != null) {
							newApiClientProps.setProperty("id", APP_ID + iTest);
							apiKey = newApiClientProps.getProperty("API-Key");
							LoggerUtility.info(CLASS_NAME, METHOD, "New test API Key : " + apiKey);
							jsonResult = null;
							try {
								jsonResult = apiClient.getGetAPIKeyRoles((String)null);
							} catch (UnsupportedEncodingException | IoTFCReSTException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (jsonResult != null && jsonResult.has("results")) {
								LoggerUtility.info(CLASS_NAME, METHOD, "API Key (" + apiKey + ") roles : " + jsonResult);
								
								jsonResult = TestHelper.updateAPIKeyRole(apiClient, apiKey, roleId, groupId);
								if (jsonResult != null) {
									LoggerUtility.info(CLASS_NAME, METHOD, "API Key (" + apiKey + ") updated roles : " + jsonResult);
								}
							}
							
							try {
								testHelper.setAppProperties(newApiClientProps);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
					}	
					
				} catch (IoTFCReSTException e) { 
					e.printStackTrace();
				}
			}
		}
		LoggerUtility.info(CLASS_NAME, METHOD, METHOD + " setup is complete");
		
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		final String METHOD = "oneTimeTearDown";
		
		if (apiClient != null) {
			
			TestHelper.deleteAPIKeys(apiClient, CLASS_NAME);
			
			for (int i=1; i<= totalTests; i++) {
				Integer iTest = new Integer(i);
				TestHelper testHelper = testMap.get(iTest);
				
				if (testHelper != null) {
					TestHelper.deleteDevice(apiClient, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId());
					TestHelper.deleteDevice(apiClient, testHelper.getGatewayDeviceType(), testHelper.getGatewayDeviceId());
				}
			}
    		try {
				apiClient.deleteDeviceType(DEVICE_TYPE);
				LoggerUtility.info(CLASS_NAME, METHOD, "Device type " + DEVICE_TYPE + " deleted.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
    		try {
				apiClient.deleteDeviceType(GW_DEVICE_TYPE);
				LoggerUtility.info(CLASS_NAME, METHOD, "Gateway device type " + GW_DEVICE_TYPE + " deleted.");
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
			testHelper.connectGateway();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		// Ask application to publish the command to this gateway now
		/*
		publishCommand(iTest, testHelper.getGatewayDeviceType(), testHelper.getGatewayDeviceId(), 
				null, null, null, null);
				*/
		testHelper.appPublishCommand(testHelper.getGatewayDeviceType(), testHelper.getGatewayDeviceId(), 
				null, null, null, null);
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnectGateway();
		LoggerUtility.info(CLASS_NAME, METHOD, "Command received ? " + testHelper.commandReceived());
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
			testHelper.connectGateway();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		testHelper.subscribeCommands();
		
		/*
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				null, null);
				*/
		testHelper.appPublishCommand(null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				null, null);
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnectGateway();
		LoggerUtility.info(CLASS_NAME, METHOD, "Command received ? " + testHelper.commandReceived());
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
			testHelper.connectGateway();
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
		
		/*
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		*/
		testHelper.appPublishCommand(null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnectGateway();
		
		LoggerUtility.info(CLASS_NAME, METHOD, "The command received ? " + testHelper.commandReceived());
		
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
			testHelper.connectGateway();
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

		/*
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
				*/
		
		testHelper.appPublishCommand(null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnectGateway();
		
		LoggerUtility.info(CLASS_NAME, METHOD, "The command received ? " + testHelper.commandReceived());
		
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
			testHelper.connectGateway();
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
		
		testHelper.disconnectGateway();
		
		TestHelper.deleteDevice(apiClient, newGwType, newGwId);
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Notification received ? " + testHelper.notificationReceived());
		
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
			testHelper.connectGateway();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}

		String cmd = "start";
		testHelper.subscribeCommand(cmd);
		
		/*
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
				*/
		testHelper.appPublishCommand(null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnectGateway();
		LoggerUtility.info(CLASS_NAME, METHOD, "Command received ? " + testHelper.commandReceived());
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
			testHelper.connectGateway();
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
		
		/*
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
				*/
		testHelper.appPublishCommand(null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		
		int count = 0;
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnectGateway();
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Command received ? " + testHelper.commandReceived());
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
			testHelper.connectGateway();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}

		testHelper.subscribeCommands();
		
		/*
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				null, null);
				*/
		testHelper.appPublishCommand(null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
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
		
		/*
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				null, null);
				*/
		
		testHelper.appPublishCommand(null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				null, null);
		
		
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnectGateway();
		LoggerUtility.info(CLASS_NAME, METHOD, "Command received ? " + testHelper.commandReceived());
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
			testHelper.connectGateway();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}

		String cmd = "start";
		testHelper.subscribeCommand(cmd);
		
		/*
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
				*/
		testHelper.appPublishCommand(null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
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

		/*
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
				*/
		testHelper.appPublishCommand(null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnectGateway();
		LoggerUtility.info(CLASS_NAME, METHOD, "Command received ? " + testHelper.commandReceived());
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
			testHelper.connectGateway();
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
		
		/*
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		*/
		testHelper.appPublishCommand(null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
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

		/*
		publishCommand(iTest, null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		*/
		testHelper.appPublishCommand(null, null, testHelper.getAttachedDeviceType(), testHelper.getAttachedDeviceId(),
				cmd, null);
		
		// wait for sometime before checking
		while(testHelper.commandReceived() == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		testHelper.disconnectGateway();
		LoggerUtility.info(CLASS_NAME, METHOD, "Command received ? " + testHelper.commandReceived());
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
	private synchronized void publishCommand(
			Integer iTest,
			String gwType, String gwDevId, 
			String devType, String devId, 
			String cmdName, JsonObject jsonCmd) {
		
		final String METHOD = "publishCommand";
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);
		
		ApplicationClient mqttAppClient = null;
		Properties props = TestEnv.getAppProperties(APP_ID + iTest, false, null, null);
		try {
			mqttAppClient = new ApplicationClient(props);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			mqttAppClient.connect();
		} catch (MqttException ex) {
			ex.printStackTrace();
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
		
		LoggerUtility.info(CLASS_NAME, METHOD, mqttAppClient.getClientID() + " publish command " + cmdName);
		
		if (gwType != null && gwDevId != null) {
			mqttAppClient.publishCommand(gwType, gwDevId, cmdName, jsonCmd);
		} else {
			mqttAppClient.publishCommand(devType, devId, cmdName, jsonCmd);
		}
		
		mqttAppClient.disconnect();
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Exiting test #" + iTest);
	}

	
}
