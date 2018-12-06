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
import java.util.Properties;
import java.util.logging.Level;

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
public class GatewayCommandSubscriptionTest {
	
	private static final String CLASS_NAME = GatewayCommandSubscriptionTest.class.getName();
	private static final String APP_ID = "GwCmdSubApp1";
	
	private final static String DEVICE_TYPE = "SubType1";
	private final static String DEVICE_ID = "SubDev1";
	private final static String GW_DEVICE_TYPE = "GwCmdSubType1";
	private final static String GW_DEVICE_ID = "GwCmdSubDev1";
	private static GatewayClient gwClient = null;
	private static APIClient apiClient = null;
	private static ApplicationClient mqttAppClient = null;
	
	@BeforeClass
	public static void oneTimeSetup() {
		
		final String METHOD = "oneTimeSetup";
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, DEVICE_ID);
		
		try {
			apiClient = new APIClient(appProps);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		
		boolean exist = false;

		try {
			exist = apiClient.isDeviceExist(GW_DEVICE_TYPE, GW_DEVICE_ID);
		} catch (IoTFCReSTException e1) {
			e1.printStackTrace();
		}
		
		if (exist) {
			try {
				apiClient.deleteDevice(GW_DEVICE_TYPE, GW_DEVICE_ID);
				LoggerUtility.info(CLASS_NAME, METHOD, "Gateway device ID " + GW_DEVICE_ID + " has been deleted.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}
		
		try {
			exist = apiClient.isDeviceTypeExist(GW_DEVICE_TYPE);
		} catch (IoTFCReSTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (!exist) {
			JsonObject jsonGW = new JsonObject();
			jsonGW.addProperty("id", GW_DEVICE_TYPE);
			try {
				apiClient.addGatewayDeviceType(jsonGW);
			} catch (IoTFCReSTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Register GW device
		try {
			apiClient.registerDevice(GW_DEVICE_TYPE, GW_DEVICE_ID, TestEnv.getGatewayToken(), null, null, null);
			LoggerUtility.info(CLASS_NAME, METHOD, "Gateway device ID " + GW_DEVICE_ID + " has been created.");
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
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
		
		try {
			exist = apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		
		if (exist) {
			try {
				apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
				LoggerUtility.info(CLASS_NAME, METHOD, "Device ID " + DEVICE_ID + " has been deleted.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}

		try {
			apiClient.registerDeviceUnderGateway(DEVICE_TYPE, DEVICE_ID, GW_DEVICE_TYPE, GW_DEVICE_ID);
			LoggerUtility.info(CLASS_NAME, METHOD, "Device ID " + DEVICE_ID + " has been created.");
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		
		Properties gwProps = TestEnv.getDeviceProperties(GW_DEVICE_TYPE, GW_DEVICE_ID);
		try {
			gwClient = new GatewayClient(gwProps);
			LoggerUtility.info(CLASS_NAME, METHOD, "Gateway client id " + gwClient.getClientID());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, METHOD + " setup is complete");
		
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		final String METHOD = "oneTimeTearDown";
		
		if (gwClient != null && gwClient.isConnected()) {
			try {
				gwClient.disconnect();
				gwClient.close();
				gwClient = null;
			} catch (Exception e) {
				LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Exception", e);
				e.printStackTrace();
			}
		}
		
		if (apiClient != null) {
			try {
				apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
			} catch (IoTFCReSTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		try {
				apiClient.deleteDeviceType(DEVICE_TYPE);
			} catch (IoTFCReSTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		try {
				apiClient.deleteDevice(GW_DEVICE_TYPE, GW_DEVICE_ID);
			} catch (IoTFCReSTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		try {
				apiClient.deleteDeviceType(GW_DEVICE_TYPE);
			} catch (IoTFCReSTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		
	}
	
	
	@Test
	public void testGatewayCommandReception() {
		
		final String METHOD = "testGatewayCommandReception";

		if (gwClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		
		//Pass the above implemented CommandCallback as an argument to this device client
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setGatewayCallback(callback);

		try {
			gwClient.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, gwClient.getClientID() + " is connected ? " + gwClient.isConnected());
		
		// Ask application to publish the command to this gateway now
		publishCommand(true, null);
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		gwClient.disconnect();
		assertTrue("The command is not received by gateway", callback.commandReceived);
		
	}
	
	@Test
	public void testDeviceCommandReception() {
		
		final String METHOD = "testDeviceCommandReception";

		if (gwClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setGatewayCallback(callback);
		
		try {
			gwClient.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, gwClient.getClientID() + " is connected ? " + gwClient.isConnected());
		
		gwClient.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID);
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, null);
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		gwClient.disconnect();
		
		assertTrue("The command is not received by gateway", callback.commandReceived);
	}
	
	@Test
	public void test02DeviceCommandReception() throws MqttException{
		
		final String METHOD = "test02DeviceCommandReception";

		if (gwClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setGatewayCallback(callback);
		
		try {
			gwClient.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, gwClient.getClientID() + " is connected ? " + gwClient.isConnected());
		
		gwClient.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID, "stop", "json");
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, null);
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		gwClient.disconnect();
		
		assertTrue("The command is not received by gateway", callback.commandReceived);
	}
	
	@Test
	public void test03DeviceCommandReception() throws MqttException{
		
		final String METHOD = "test03DeviceCommandReception";

		if (gwClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setGatewayCallback(callback);
		
		try {
			gwClient.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, gwClient.getClientID() + " is connected ? " + gwClient.isConnected());
		
		gwClient.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID, "stop", "json", 2);
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, null);
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		gwClient.disconnect();
		
		assertTrue("The command is not received by gateway", callback.commandReceived);
	}

	
	@Test
	public void testNotification() throws MqttException{
		
		final String METHOD = "testNotification";

		if (gwClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setGatewayCallback(callback);
		
		try {
			gwClient.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, gwClient.getClientID() + " is connected ? " + gwClient.isConnected());
		
		gwClient.subscribeToGatewayNotification();
		
		gwClient.disconnect();
		
	}
	
	@Test
	public void testDeviceSpecificCommandReception() throws MqttException{
		
		//Pass the above implemented CommandCallback as an argument to this device client
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setGatewayCallback(callback);
		gwClient.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID, "start");
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, "start");
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("The command is not received by gateway", callback.commandReceived);
	}

	@Test
	public void testDeviceSpecificCommandReceptionWithQoS() throws MqttException{
		
		final String METHOD = "testDeviceSpecificCommandReceptionWithQoS";

		if (gwClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setGatewayCallback(callback);
		
		try {
			gwClient.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, gwClient.getClientID() + " is connected ? " + gwClient.isConnected());

		gwClient.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID, "start", 2);
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, "start");
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		gwClient.disconnect();
		assertTrue("The command is not received by gateway", callback.commandReceived);
	}
	
	@Test
	public void testDeviceSpecificCommandReceptionWithFormat() throws MqttException{
		
		final String METHOD = "testDeviceSpecificCommandReceptionWithFormat";

		if (gwClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setGatewayCallback(callback);
		
		try {
			gwClient.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, gwClient.getClientID() + " is connected ? " + gwClient.isConnected());

		gwClient.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID, "start", "json", 2);
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, "start");
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		gwClient.disconnect();
		assertTrue("The command is not received by gateway", callback.commandReceived);
	}

	@Test
	public void testDeviceCommandUnsubscription() throws MqttException{
		
		final String METHOD = "testDeviceCommandUnsubscription";

		if (gwClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setGatewayCallback(callback);
		
		try {
			gwClient.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, gwClient.getClientID() + " is connected ? " + gwClient.isConnected());

		
		gwClient.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID);
		
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, null);
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("The command is not received by gateway", callback.commandReceived);
		
		count = 0;
		
		callback.clear();
		
		gwClient.unsubscribeFromDeviceCommands(DEVICE_TYPE, DEVICE_ID);
		
		publishCommand(false, null);
		
		
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		gwClient.disconnect();
		
		assertFalse("The command should not be received by gateway", callback.commandReceived);
				
	}
	
	@Test
	public void test02DeviceCommandUnsubscription() throws MqttException{
		
		final String METHOD = "test02DeviceCommandUnsubscription";

		if (gwClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setGatewayCallback(callback);
		
		try {
			gwClient.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, gwClient.getClientID() + " is connected ? " + gwClient.isConnected());
		
		gwClient.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID, "stop");
		
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, null);
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("The command is not received by gateway", callback.commandReceived);
		
		count = 0;

		callback.clear();
		
		gwClient.unsubscribeFromDeviceCommands(DEVICE_TYPE, DEVICE_ID, "stop");
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, null);
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		gwClient.disconnect();
		assertFalse("The command should not be received by gateway", callback.commandReceived);
				
	}
	
	@Test
	public void test03DeviceCommandUnsubscription() throws MqttException{
		
		final String METHOD = "test03DeviceCommandUnsubscription";

		if (gwClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setGatewayCallback(callback);
		
		try {
			gwClient.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, gwClient.getClientID() + " is connected ? " + gwClient.isConnected());
		
		gwClient.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID, "stop", "json");
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, null);
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("The command is not received by gateway", callback.commandReceived);
		
		count = 0;
		
		callback.clear();
		
		gwClient.unsubscribeFromDeviceCommands(DEVICE_TYPE, DEVICE_ID, "stop", "json");
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, null);
		
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertFalse("The command should not be received by gateway", callback.commandReceived);
				
	}
	
	@Test
	public void test04DeviceCommandUnsubscription() throws MqttException{
		
		final String METHOD = "test04DeviceCommandUnsubscription";

		if (gwClient == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Skipping test " + METHOD);
			fail("Setup was not completed for test method " + METHOD);
			return;
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setGatewayCallback(callback);
		
		try {
			gwClient.connect();
		} catch (MqttException e) {
			String failMsg = METHOD + " connect MqttException: " +  e.getMessage();
			LoggerUtility.info(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
			e.printStackTrace();
			return;
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, gwClient.getClientID() + " is connected ? " + gwClient.isConnected());
		
		gwClient.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID, "stop", "json", 2);
		
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, null);
		
		int count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("The command is not received by gateway", callback.commandReceived);
		
		count = 0;
		callback.clear();
		
		
		gwClient.unsubscribeFromDeviceCommands(DEVICE_TYPE, DEVICE_ID, "stop", "json");
		
		// Ask application to publish the command to this gateway now
		publishCommand(false, null);
		
		count = 0;
		// wait for sometime before checking
		while(callback.commandReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertFalse("The command should not be received by gateway", callback.commandReceived);
				
	}
	
	/**
	 * Publish command to gateway device or attached device
	 * @param gateway Send command to gateway if true
	 * @param cmdName Name of command
	 */
	private void publishCommand(boolean gateway, String cmdName) {
		
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
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JsonObject data = new JsonObject();
		data.addProperty("name", "stop-rotation");
		data.addProperty("delay",  0);
		
		if (cmdName == null) {
			// use default command name 
			cmdName = "stop";
		}
		
		if (gateway) {
			//Registered flow allows 0, 1 and 2 QoS
			mqttAppClient.publishCommand(GW_DEVICE_TYPE, GW_DEVICE_ID, cmdName, data);
		} else {
			mqttAppClient.publishCommand(DEVICE_TYPE, DEVICE_ID, cmdName, data);
		}
		
		myAppClient.disconnect();
		LoggerUtility.info(CLASS_NAME, METHOD, myAppClient.getClientID() + " Connected ? " + myAppClient.isConnected());
		
		
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
}
