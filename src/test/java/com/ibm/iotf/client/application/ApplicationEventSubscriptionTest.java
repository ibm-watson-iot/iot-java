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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.AbstractClient;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.ApplicationStatus;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.DeviceStatus;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.iotf.client.app.StatusCallback;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.test.common.TestDeviceHelper;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestHelper;
import com.ibm.iotf.util.LoggerUtility;

import junit.framework.TestCase;


/**
 * This test verifies that the event & device connectivity status are successfully received by the
 * application.
 *
 */
public class ApplicationEventSubscriptionTest {
	
	static Properties deviceProps;
	static Properties appProps;
	
	private final static String DEVICE_TYPE = "AppEvtSubTestDevType";
	private final static String DEVICE_ID = "AppEvtSubTestDevId";
	private final static String APP_ID = "AppEvtSubTest";

	static CommunicationProxyServer proxy;
	static final Class<?> cclass = ConnectionLossTest.class;
	private static final String className = cclass.getName();
	private static final Logger log = Logger.getLogger(className);
	
	private static final String CLASS_NAME = ApplicationEventSubscriptionTest.class.getName();
	//final String METHOD = "connect";
	
	private static String domainAddr;
	
	private static APIClient apiClient = null;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		final String METHOD = "oneTimeSetUp";
		LoggerUtility.info(CLASS_NAME, METHOD, "Setting up device type (" + DEVICE_TYPE + ") ID(" + DEVICE_ID + ")");
		deviceProps = TestEnv.getDeviceProperties(DEVICE_TYPE, DEVICE_ID);
		
		appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, DEVICE_ID);
		
		try {
			apiClient = new APIClient(appProps);
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
				LoggerUtility.info(CLASS_NAME, METHOD, "Device type " + DEVICE_TYPE + " has been created.");
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
		
	}
	
	@AfterClass
	public static void oneTimeCleanup() {
		if (apiClient != null) {
			try {
				TestDeviceHelper.deleteDevice(apiClient, DEVICE_TYPE, DEVICE_ID);
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}			
		}
	}
	
	private void proxyServerStart() {
		final String METHOD = "proxyServerStart";
		String orgId = TestEnv.getOrgId();
		
		domainAddr = orgId + ".messaging.internetofthings.ibmcloud.com";
					
		proxy = new CommunicationProxyServer(domainAddr, 8883, 0);
		proxy.startProxyServer();
		
		while (!proxy.isPortSet()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		log.log(Level.INFO, METHOD + ": Proxy Started, port set to: " + proxy.getlocalDevicePort());			
		
	}
		
	/**
	 * Test to ascertain network failure while the Client is connected to the Server, 
	 * has published a Blink event and is awaiting an acknowledgement from the Server.
	 * @throws Exception
	 */
	@Test
	public void testConnectionLossServerToClient()
		throws Exception
	{
		final String METHOD = "testConnectionLossServerToClient";
		
		proxyServerStart();
		
		/**
		 * Load device properties
		 */
		Properties props = new Properties(deviceProps);
		
		props.put("port", proxy.getlocalDevicePort()+"");
		props.put("mqtt-server", "localhost");
		props.put("Automatic-Reconnect", "false");

		DeviceClient myClient = null;
		try {
			myClient = new DeviceClient(props);
			myClient.setKeepAliveInterval(1000);
			myClient.connect();
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			return;
		}
		
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
		
		proxy.addDelayInServerResponse(100 * 1000);	
		
		boolean status = myClient.publishEvent("blink", event, 1);
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Completed the wait time before disconnecting");
		myClient.disconnect();
		assertFalse("Timed out waiting for a response from the server (32000)",status);
		proxy.stopProxyServer();
	}
	
	/**
	 * Test to ascertain network loss, while Client is connected to the Server and is trying to Publish a Blink event.
	 * @throws Exception
	 */
	@Test
	public void testConnectionLossClientToServer()
		throws Exception
	{
		final String METHOD = "testConnectionLossClientToServer";
		proxyServerStart();

		Properties props = new Properties(deviceProps);
		
		props.put("port", proxy.getlocalDevicePort()+"");
		props.put("mqtt-server", "localhost"); 
		props.put("Automatic-Reconnect", "false");

		DeviceClient myClient = null;
		try {
			myClient = new DeviceClient(props);
			myClient.setKeepAliveInterval(1000);
			myClient.connect();
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			return;
		}
		
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
					
		proxy.addDelayInClientPublish(100 * 1000);
		
		boolean status = myClient.publishEvent("blink", event, 1);
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Completed the wait time before disconnecting");
		if(status == true){
			LoggerUtility.info(CLASS_NAME, METHOD, "Successfully published Blink from Client to the server. ");	
		}
		
		myClient.disconnect();
		assertFalse("Timed out waiting for a response from the server (32000)",status);
		
		proxy.stopProxyServer();
	}

}
