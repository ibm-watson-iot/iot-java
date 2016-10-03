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

import java.io.IOException;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.device.DeviceClient;

import junit.framework.TestCase;

/**
 * This Test verifies whether one can publish event successfully to the quickstart 
 * and registered service (if property file is provided).
 * 
 */

public class DeviceEventPublishTest extends TestCase{
	
	
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

	private final static String DEVICE_PROPERTIES_FILE = "/device.properties";
	private final static String APPLICATION_PROPERTIES_FILE = "/application.properties";

	/**
	 * This test expects a properties file containing the device registration details. Failing to
	 * provide the same, the test will return immediately and show as passed.
	 */
	@Test
	public void testRegisteredPublish(){			
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceEventPublishTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(props);
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
					
		boolean code = myClient.publishEvent("blink", event);
		myClient.disconnect();
		assertTrue("Failed to publish the event......", code);
	}
	
	/**
	 * This test expects a properties file containing the device registration details. Failing to
	 * provide the same, the test will return immediately and show as passed.
	 */
	@Test
	public void testRegisteredPublishWithEmptyObject(){			
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceEventPublishTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(props);
			myClient.connect(true);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
			
		boolean code = myClient.publishEvent("blink", null);
		myClient.disconnect();
		assertTrue("Failed to publish the event......", code);
	}
	
	/**
	 * NegativeTest - try to publish after disconnect, it should return immediately 
	 */
	@Test
	public void testPublishAfterDisconnect(){			
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceEventPublishTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(props);
			myClient.connect(true);
			myClient.disconnect();
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
			
		boolean code = myClient.publishEvent("blink", null);
		assertFalse("Successfully publish the event even after disconnect......", code);
	}
	
	/**
	 * This test expects a properties file containing the device registration details. Failing to
	 * provide the same, the test will return immediately and show as passed.
	 * 
	 * This test publishes the event in QoS 0
	 */
	@Test
	public void testRegisteredPublishQos0(){			
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceEventPublishTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(props);
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
					
		boolean code = myClient.publishEvent("blink", event, 0);
		myClient.disconnect();
		assertTrue("Failed to publish the event......", code);
	}
	
	/**
	 * This test expects a properties file containing the device registration details. Failing to
	 * provide the same, the test will return immediately and show as passed.
	 * 
	 * This test publishes the event in QoS 2
	 * @throws Exception 
	 */
	@Test
	public void testCustomPublishQos2() throws Exception{			
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceEventPublishTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(props);
			myClient.connect();
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
			
		byte[] ss = new byte[]{1, 2, 3 ,4};
		System.out.println(ss.getClass().getName());
		
		boolean code = myClient.publishEvent("blink", new byte[]{1, 2, 3, 4, 54}, "binary", 2);
		myClient.disconnect();
		assertTrue("Failed to publish the event......", code);
	}
	
	/**
	 * This test expects a properties file containing the device registration details. Failing to
	 * provide the same, the test will return immediately and show as passed.
	 * 
	 * This test publishes the event in QoS 2
	 * @throws Exception 
	 */
	@Test
	public void testCustomPublishString() throws Exception{			
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceEventPublishTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(props);
			myClient.connect();
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
			
		boolean code = myClient.publishEvent("blink", "cpu:90", "binary", 2);
		myClient.disconnect();
		assertTrue("Failed to publish the event......", code);
	}
	
	/**
	 * This test expects a properties file containing the application details, also the device details. Failing to
	 * provide the same, the test will return immediately and show as passed.
	 * 
	 */
	@Test
	public void testApplicationEventPublishOnBehalfOfDevice(){			
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceEventPublishTest.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(props);
			myClient.connect();
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = trimedValue(props.getProperty("Device-Type"));
		String deviceId = trimedValue(props.getProperty("Device-ID"));
			
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
					
		boolean code = myClient.publishEvent(deviceType, deviceId, "blink", event, 2);
		myClient.disconnect();
		assertTrue("Failed to publish the event......", code);
	}
	
	/**
	 * This test expects a properties file containing the device registration details. Failing to
	 * provide the same, the test will return immediately and show as passed.
	 */
	@Test
	public void testRegisteredPublishHttp(){			
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceEventPublishTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(props);
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
					
		int code = 0;
		try {
			code = myClient.publishEventOverHTTP("blink", event);
		} catch (java.lang.IllegalArgumentException e) {
			// looks like the proerties file is not edited, just ignore
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals("Failed to publish the event......", 200, code);
	}
	
	/**
	 * This test expects a properties file containing the device registration details. Failing to
	 * provide the same, the test will return immediately and show as passed.
	 */
	@Test
	public void testRegisteredPublishHttp_new(){			
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceEventPublishTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(props);
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
					
		boolean code = false;
		try {
			code = myClient.api().publishDeviceEventOverHTTP("blink", event, "json");
		} catch (java.lang.IllegalArgumentException e) {
			// looks like the proerties file is not edited, just ignore
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals("Failed to publish the event......", true, code);
	}
	
	/**
	 * This test expects a properties file containing the application details, also the device details. Failing to
	 * provide the same, the test will return immediately and show as passed.
	 */
	@Test
	public void testApplicationEventPublishOnBehalfOfDeviceOverHTTP(){			
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceEventPublishTest.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(props);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = trimedValue(props.getProperty("Device-Type"));
		String deviceId = trimedValue(props.getProperty("Device-ID"));
		
			
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
					
		int code = 0;
		try {
			code = myClient.publishEventOverHTTP(deviceType, deviceId, "blink", event);
		} catch (java.lang.IllegalArgumentException e) {
			// looks like the proerties file is not edited, just ignore
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals("Failed to publish the event......", 200, code);
	}
	
	/**
	 * This test expects a properties file containing the application details, also the device details. Failing to
	 * provide the same, the test will return immediately and show as passed.
	 */
	@Test
	public void testApplicationEventPublishOnBehalfOfDeviceOverHTTP_new(){			
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceEventPublishTest.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(props);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = trimedValue(props.getProperty("Device-Type"));
		String deviceId = trimedValue(props.getProperty("Device-ID"));
		
			
		//Generate a JSON object of the event to be published
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
	}
	
	private static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}

}
