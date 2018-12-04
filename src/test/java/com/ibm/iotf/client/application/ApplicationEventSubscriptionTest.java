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

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.AbstractClient;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.ApplicationStatus;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.DeviceStatus;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.iotf.client.app.StatusCallback;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.util.LoggerUtility;

import junit.framework.TestCase;


/**
 * This test verifies that the event & device connectivity status are successfully received by the
 * application.
 *
 */
public class ApplicationEventSubscriptionTest extends TestCase{
	
	//private final static String DEVICE_PROPERTIES_FILE = "/device.properties";
	//private final static String APPLICATION_PROPERTIES_FILE = "/application.properties";
	
	static Properties deviceProps;
	static Properties appProps;
	
	private final static String DEVICE_TYPE = "AppEvtSubTestDevType";
	private final static String DEVICE_ID = "AppEvtSubTestDevId1";
	private final static String APP_ID = "AppEvtSubTest1";

	static CommunicationProxyServer proxy;
	static final Class<?> cclass = ConnectionLossTest.class;
	private static final String className = cclass.getName();
	private static final Logger log = Logger.getLogger(className);
	
	private static final String CLASS_NAME = ApplicationEventSubscriptionTest.class.getName();
	//final String METHOD = "connect";
	
	private static String domainAddr;
	
	
//	private String  message  = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
//	private MqttConnectOptions options;
//
//	private static final MqttDefaultFilePersistence DATA_STORE = new MqttDefaultFilePersistence("C:\temp");
//	

	/**
	 * This method publishes a device event such that the application will receive the same
	 * and verifies that the event is same.
	 */
	private void deviceEventPublish() {

		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(deviceProps);
			myClient.connect();
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
			
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
					
		myClient.publishEvent("blink", event);
		myClient.disconnect();
	}
	
	/**
	 * This method publishes a device event such that the application will receive the same
	 * and verifies that the event is same.
	 */
	private void stringEventPublish() {

		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(deviceProps);
			myClient.connect();
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
			
		String s = "cpu:10";
		try {
			myClient.publishEvent("blink", s, "string", 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		myClient.disconnect();
	}
	
	/**
	 * This method publishes a device event such that the application will receive the same
	 * and verifies that the event is same.
	 */
	private void binaryEventPublish() {
			
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(deviceProps);
			myClient.connect();
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
			
		byte[] payload = {1, 4, 5, 6, 7, 9, 10};
		try {
			myClient.publishEvent("blink", payload, "binary", 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		myClient.disconnect();
	}
	
	@Test
	public void test01EventSubscribe() {
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(5);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		//Add status callback
		MyStatusCallback statusbk = new MyStatusCallback();
		myClient.setStatusCallback(statusbk);
		
		// Subscribe to device events and device connectivity status
		//myClient.subscribeToDeviceStatus();
		myClient.subscribeToDeviceEvents(deviceType, deviceId);
		
		this.deviceEventPublish();
		
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Event is not received by application", eventbk.eventReceived);
		//assertTrue("Device connectivity status is not received by application", statusbk.statusReceived);
	}
	
	@Test
	public void test10CustomEventSubscribe() {
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(5);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceEvents(deviceType, deviceId);
		this.binaryEventPublish();
		
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Event is not received by application", eventbk.eventReceived);
		//assertTrue("Device connectivity status is not received by application", statusbk.statusReceived);
	}

	@Test
	public void test11CustomEventSubscribe() {

		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(5);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceEvents(deviceType, deviceId);
		this.stringEventPublish();
		
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Event is not received by application", eventbk.eventReceived);
		//assertTrue("Device connectivity status is not received by application", statusbk.statusReceived);
	}

	@Test
	public void test11CommandSubscribe() {
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(5);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceCommands();
		
		myClient.publishCommand(deviceType, deviceId, "start", null);
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Command is not received by application", eventbk.cmdReceived);
	}


	@Test
	public void test12CommandSubscribe() {
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(5);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceCommands(deviceType, deviceId);
		
		myClient.publishCommand(deviceType, deviceId, "start", null);
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Command is not received by application", eventbk.cmdReceived);
	}

	@Test
	public void test13CommandSubscribe() {
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(5);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceCommands(deviceType, deviceId, "start");
		
		myClient.publishCommand(deviceType, deviceId, "start", null);
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Command is not received by application", eventbk.cmdReceived);
	}
	
	@Test
	public void test14CommandSubscribe() {

		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(5);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceCommands(deviceType, deviceId, "hello");
		
		myClient.publishCommand(deviceType, deviceId, "start", null);
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertFalse("Device Command is not supposed to be received by application", eventbk.cmdReceived);
	}
	
	@Test
	public void test15CommandSubscribe() {

		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(5);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceCommands(deviceType, deviceId, "start");
		myClient.unsubscribeFromDeviceCommands(deviceType, deviceId, "start");
		
		myClient.publishCommand(deviceType, deviceId, "start", null);
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertFalse("Device Command is not supposed to be received by application", eventbk.cmdReceived);
	}
	
	@Test
	public void test16CommandSubscribe() {

		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(5);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceCommands(deviceType, deviceId, "start", "json");
		
		myClient.publishCommand(deviceType, deviceId, "start", null);
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Command is not received by application", eventbk.cmdReceived);
	}
	
	@Test
	public void test17CommandSubscribe() {

		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(5);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceCommands(deviceType, deviceId, "start", "json");
		myClient.unsubscribeFromDeviceCommands(deviceType, deviceId, "start", "json");
		
		myClient.publishCommand(deviceType, deviceId, "start", null);
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertFalse("Device Command is not supposed to be received by application", eventbk.cmdReceived);
	}
	
	@Test
	public void test18CommandSubscribe() {
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(5);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceCommands(deviceType);
		
		myClient.publishCommand(deviceType, deviceId, "start", null);
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Command is not received by application", eventbk.cmdReceived);
	}
	
	@Test
	public void test19CommandSubscribe() {

		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(5);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceCommands(deviceType, deviceId, "start", "json", 1);
		
		myClient.publishCommand(deviceType, deviceId, "start", null);
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Command is not received by application", eventbk.cmdReceived);
	}
	
	
	@Test
	public void test02Subscribe() {
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(true);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceEvents(deviceType, deviceId, "blink");
		
		this.deviceEventPublish();
		
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Event is not received by application", eventbk.eventReceived);
	}
	
	/**
	 * Application subscribing to all the device events
	 */
	@Test
	public void test05Subscribe() {
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(true);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceEvents();
		
		this.deviceEventPublish();
		
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Event is not received by application", eventbk.eventReceived);
	}
	
	/**
	 * Application subscribing to all the device events of a particular device type
	 */
	@Test
	public void test06Subscribe() {

		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(true);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceEvents(deviceType);
		
		this.deviceEventPublish();
		
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Event is not received by application", eventbk.eventReceived);
	}
	
	/**
	 * Application subscribing to all the device events of a particular device type, device Id and JSON format
	 */
	@Test
	public void test07Subscribe() {

		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(true);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		myClient.subscribeToDeviceEvents(deviceType, deviceId, "blink", "json");
		this.deviceEventPublish();
		
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 10) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Event is not received by application", eventbk.eventReceived);
	}
	
	/**
	 * Application subscribing to all the device events of a particular device type, device Id, JSON format and Qos
	 */
	@Test
	public void test08SubscribewithQos() {
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(true);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		//Add status callback
		MyStatusCallback statusbk = new MyStatusCallback();
		myClient.setStatusCallback(statusbk);
		
		myClient.subscribeToDeviceEvents();
		this.deviceEventPublish();
		
		myClient.subscribeToDeviceEvents(deviceType, deviceId, "blink", "json", 0);
		
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Event is not received by application", eventbk.eventReceived);
	}
	
	/**
	 * Negative test
	 */
	@Test
	public void test03Subscribe() {
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(true);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		//Add status callback
		MyStatusCallback statusbk = new MyStatusCallback();
		myClient.setStatusCallback(statusbk);
		
		// Subscribe to some different event
		myClient.subscribeToDeviceEvents(deviceType, deviceId, "Nonevent");
		//myClient.subscribeToDeviceStatus(deviceType);
		this.deviceEventPublish();
		
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertFalse("Device Event is not supposed to be received by application", eventbk.eventReceived);
		//assertTrue("Device connectivity status is not received by application", statusbk.statusReceived);
	}
	
	/**
	 * Negative test
	 */
	@Test
	public void test08UnSubscribe() {
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(true);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		//Add status callback
		MyStatusCallback statusbk = new MyStatusCallback();
		myClient.setStatusCallback(statusbk);
		
		// Subscribe to some different event
		myClient.subscribeToDeviceEvents(deviceType, deviceId, "blink");
		myClient.unsubscribeFromDeviceEvents(deviceType, deviceId, "blink");
		
		this.deviceEventPublish();
		
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertFalse("Device Event is not supposed to be received by application", eventbk.eventReceived);
	}
	
	/**
	 * Negative test
	 */
	@Test
	public void test09UnSubscribe() {

		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(true);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		//Add status callback
		MyStatusCallback statusbk = new MyStatusCallback();
		myClient.setStatusCallback(statusbk);
		
		// Subscribe to some different event
		myClient.subscribeToDeviceEvents(deviceType, deviceId, "blink", "json");
		myClient.unsubscribeFromDeviceEvents(deviceType, deviceId, "blink", "json");
		
		this.deviceEventPublish();
		
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertFalse("Device Event is not supposed to be received by application", eventbk.eventReceived);
	}
	
	/**
	 * Negative test
	 */
	@Test
	public void test10UnSubscribe() {

		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(appProps);
			myClient.connect(true);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = DEVICE_TYPE;
		String deviceId = DEVICE_ID;
		
		//Add status callback
		MyStatusCallback statusbk = new MyStatusCallback();
		myClient.setStatusCallback(statusbk);
		
		// Subscribe to some different event
		myClient.subscribeToDeviceStatus(deviceType, deviceId);
		
		// Wait 10 seconds, then unsubscribe
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		myClient.unSubscribeFromDeviceStatus(deviceType, deviceId);

		// Wait 10 seconds, then publish
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		this.deviceEventPublish();
		
		int count = 0;
		// wait for sometime before checking
		while(statusbk.statusReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		// ToDo uncomment when the bug is fixed
		assertFalse("Device status is not supposed to be received by application", statusbk.statusReceived);
	}
	
	public void test04QuickstartEventPublish() {
		Properties props = new Properties();
		
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(props);
			myClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = "iotsample-ardunio";
		String deviceId = "00112233aabb";

		
		//Quickstart flow allows only QoS = 0
		boolean status = myClient.publishEvent(deviceType, deviceId, "blink", null);
		assertTrue("Failed to publish the event in quickstart", status);

	}

	private static class MyEventCallback implements EventCallback {
		private volatile boolean eventReceived = false;
		private volatile boolean cmdReceived = false;

		@Override
		public void processEvent(Event e) {
			eventReceived = true;
			System.out.println("Received Event, name = "+e.getEvent() +
					", format = " + e.getFormat() + ", Payload = "+e.getPayload() + ", time = "+e.getTimestamp()
					 + ",Raw Payload : " + e.getRawPayload() + ",data = "+ e.getData());
		}

		@Override
		public void processCommand(Command cmd) {
			cmdReceived = true;
			System.out.println("Received command, name = "+cmd.getCommand() +
					", format = " + cmd.getFormat() + ", Payload = "+cmd.getPayload() + ", time = "+cmd.getTimestamp()
					+ ",Raw Payload : " + cmd.getRawPayload() + ",data = "+ cmd.getData());		
		}		
	}
	
	private static class MyStatusCallback implements StatusCallback {
		private volatile boolean statusReceived = false;

		@Override
		public void processApplicationStatus(ApplicationStatus status) {
			System.out.println("Application Status = " + status.getAction()
					+ " " + status.getClientAddr() + status.getClientId() + status.getConnectTime() +
					status.getId() + status.getPayload() + status.getPort() + status.getProtocol() +
			status.getReadBytes() + status.getReadMsg() + status.getReason());
		}

		@Override
		public void processDeviceStatus(DeviceStatus status) {
			statusReceived = true;
			System.out.println("Device Status = " + status.getPayload());
			
			System.out.println("Device Status = " + status.getAction()
					+ " " + status.getClientAddr() + status.getClientId() + status.getConnectTime() +
					 status.getPayload() + status.getPort() + status.getProtocol() +
			status.getReadBytes() + status.getReadMsg() + status.getReason());

		}
	}

	
	@BeforeClass
	public static void oneTimeSetUp() throws Exception{
		final String METHOD = "oneTimeSetUp";
		try {
			deviceProps = TestEnv.getDeviceProperties(DEVICE_TYPE, DEVICE_ID);
			
			appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, DEVICE_ID);
			
			APIClient apiClient = new APIClient(appProps);
			
			//Create a test device type
			apiClient.addDeviceType(DEVICE_TYPE, null, null, null);
			apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID, TestEnv.getDeviceToken(), null, null, null);
			
		
		} catch (Exception ex) {
		      log.log(Level.SEVERE, METHOD + ": caught exception:", ex);
		      throw ex;
		}
	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {
		final String METHOD = "oneTimeCleanup";
		try {
			APIClient apiClient = new APIClient(appProps);
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
			apiClient.deleteDeviceType(DEVICE_TYPE);
		} catch (Exception ex) {
			log.log(Level.SEVERE, METHOD + " caught exception:", ex);
			throw ex;
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
