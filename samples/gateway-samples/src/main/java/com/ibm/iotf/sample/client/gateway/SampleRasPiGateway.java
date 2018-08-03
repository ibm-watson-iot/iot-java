/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Patrizia Gufler1 - Initial Contribution
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.gateway;

import java.io.IOException;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.gateway.GatewayClient;
import com.ibm.iotf.sample.client.gateway.device.ArduinoInterface;
import com.ibm.iotf.sample.client.gateway.device.DeviceInterface;

/**
 * <p>The Gateway sample uses the com.ibm.iotf.client.gateway.GatewayClient class from the Watson IoT Platform Java Client Library 
 * that simplifies the Gateway interactions with IBM Watson IoT Platform. </p>
 * 
 * <p>The Gateway support is demonstrated in this sample by connecting the Arduino Uno to Raspberry Pi 
 * where the Raspberry Pi act as a Gateway and publishes events/receives commands on behalf of Arduino Uno 
 * to IBM Watson IoT Platform.</p> 
 * 
 * The following configuration is used in this Sample,<br>
 * 
 * <ul class="simple">
 * <li>Raspberry Pi as Gateway (this sample)
 * <li>Arduino Uno as device (Arduino sketch is available here)
 * <li>PIR Motion sensor as sensor connected to Ardunio Uno
 * <li>Internal temperature sensor as sensor connected to Ardunio Uno
 * <li>LED as actuator connected to Arduino Uno
 * </ul>
 * 
 * <p>The Arduino Uno reads the signal from PIR sensor along with the internal temperature sensor 
 * every moment and sends these readings to Raspberry Pi, upon receiving the sensor readings, 
 * Raspberry Pi gateway publishes the same to Watson IoT Platform through MQTT.</p>
 * 
 * <p>The Arduino Uno Sketch code is present here - refer to ArduinoSketch.c in this package.</p>
 * 
 * <p>A sample application running outside of Raspberry Pi and Arduino Uno, written in java, 
 * processes these sensor readings by subscribing to IBM Watson IoT Platform. 
 * And when it receives a movement detection, sends a command back to Arduino through 
 * the Gateway to blink the LED. </p>
 * 
 * <p>Also, the sample application allows one to blink the LED for a specified number of times 
 * by sending the command at any moment. For example, one can send a command to blink the 
 * LED for 10 times regardless of whether the movement is detected by the PIR sensor or not.</p>
 * 
 * <p>The sample application code is present here - refer to SampleApplication.java in this package.</p>
 * 
 */
public class SampleRasPiGateway {
	
	// Properties file containing the Registration details of the gateway, to connect to Watson IoT Platform
	// Present in the maven resource directory
	private final static String PROPERTIES_FILE_NAME = "/gateway.properties";
	
	private final static String DEVICE_TYPE = "iotsample-deviceType";
	private final static String ARDUINO_DEVICE_ID = "Arduino01";
	/** The port where Arduino Uno normally connects to RaspberryPi. */
	private final static String DEFAULT_SERIAL_PORT = "/dev/ttyACM0";
	
	private GatewayClient gwClient = null;
	SystemObject obj = new SystemObject();
	private String port;
	private APIClient apiClient;
	private boolean bManualRegistrationMode;
	
	public SampleRasPiGateway() {
		
	}
	
	/**
	 * This method creates a GatewayClient instance by passing the required properties 
	 * and connects the Gateway to the Watson IoT Platform by calling the connect function.
	 * 
	 * After the successful connection to the Watson IoT Platform, the Gateway can perform the following operations,
	 *   1. Publish events for itself and on behalf of devices connected behind the Gateway
	 *   2. Subscribe to commands for itself and on behalf of devices behind the Gateway
	 */
	private void createGatewayClient(String fileName) {
		 /**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(SampleRasPiGateway.class.getResourceAsStream(fileName));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		try {
			//Instantiate & connect the Gateway by passing the properties file
			gwClient = new GatewayClient(props);
			String mode = props.getProperty("Registration-Mode");
			this.port = props.getProperty("port");
			if(this.port == null) {
				this.port = this.DEFAULT_SERIAL_PORT;
			}
			gwClient.connect();
			
			// Check if its a manual registration, then we need to create APIClient
			
			if("MANUAL".equalsIgnoreCase(mode)) {
				this.bManualRegistrationMode = true;
				/**
				 * We need APIClient to register the devicetype in Watson IoT Platform 
				 */
				Properties options = new Properties();
				options.put("Organization-ID", props.getProperty("Organization-ID"));
				options.put("id", "app" + (Math.random() * 10000));		
				options.put("Authentication-Method","apikey");
				options.put("API-Key", props.getProperty("API-Key"));		
				options.put("Authentication-Token", props.getProperty("API-Token"));
				
				this.apiClient = new APIClient(options);
			} else {
				this.bManualRegistrationMode = false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * This sample adds a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	private void addDeviceType(String deviceType) throws IoTFCReSTException {
		try {
			System.out.println("<-- Checking if device type "+deviceType +" already created in Watson IoT Platform");
			boolean exist = false;
			
			try {
				exist = apiClient.isDeviceTypeExist(deviceType);
			} catch(Exception e) {}
			if (!exist) {
				System.out.println("<-- Adding device type "+deviceType + " now..");
				// device type to be created in WIoTP
				apiClient.addDeviceType(deviceType, deviceType, null, null);
			}
		} catch(IoTFCReSTException e) {
			System.err.println("ERROR: unable to add manually device type " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a device under the given gateway using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void addDevice(String deviceType, String deviceId) throws IoTFCReSTException {
		try {
			System.out.println("<-- Checking if device " + deviceId +" with deviceType " +
					deviceType +" exists in Watson IoT Platform");
			boolean exist = false;
			try {
				exist = this.gwClient.api().isDeviceExist(deviceType, deviceId);
			} catch(Exception e) {}
			if(!exist) {
				System.out.println("<-- Creating device " + deviceId +" with deviceType " +
						deviceType +" now..");
				gwClient.api().registerDeviceUnderGateway(deviceType, deviceId,
						this.gwClient.getGWDeviceType(), 
						this.gwClient.getGWDeviceId());
			}
		} catch (IoTFCReSTException ex) {
			
			System.out.println("ERROR: unable to add manually device " + deviceId);
		}
	}	

	/**
	 * While Raspberry Pi Gateway publishes events on behalf of the Arduino, the Raspberry Pi Gateway 
	 * can publish its own events as well. 
	 * 
	 * The sample publishes a blink event every second, that has the CPU and memory utilization of 
	 * this sample Gateway process.
	 */
	private void publishGatewayEvent() {
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", SystemObject.getName());
		try {
			event.addProperty("cpu",  obj.getProcessCpuLoad());
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		}
		event.addProperty("mem",  obj.getMemoryUsed());
			
		gwClient.publishGatewayEvent("blink", event, 2);
		System.out.println("<--(GW) Gateway event :: "+event);
	}
	
	private void disconnect() {
		//Disconnect cleanly
		gwClient.disconnect();
	}

	/**
	 * When the GatewayClient connects, it automatically subscribes to any commands for this Gateway. 
	 * But to subscribe to commands for the devices connected to the Gateway, one needs to use the 
	 * subscribeToDeviceCommands() method from the GatewayClient class. 
	 * 
	 * To receive and process the Arduino Uno commands, the Gateway sample does the following,
	 *   1. Adds a command callback method
	 *   2. Subscribes to commands for Arduino Uno
	 *   
	 * The callback method processCommand() is invoked by the GatewayClient when it receives any command 
	 * for Arduino Uno from Watson IoT Platform. The Gateway CommandCallback defines a BlockingQueue 
	 * to store and process the commands (in separate thread) for smooth handling of MQTT publish message.
	 */
	private void addCommandCallback() {
		GatewayCommandCallback callback = new GatewayCommandCallback(gwClient);
		gwClient.setGatewayCallback(callback);
		gwClient.subscribeToDeviceCommands(DEVICE_TYPE, ARDUINO_DEVICE_ID);
				
		DeviceInterface arduino = ArduinoInterface.createDevice(
										ARDUINO_DEVICE_ID, 
										DEVICE_TYPE, 
										this.port, 
										this.gwClient);
		
		arduino.toggleDisplay(); // activate the console display
		
		// Create the WIoTP client Id to uniquely identify the device
		String key = new StringBuilder("d:")
			.append(gwClient.getOrgId())
			.append(':')
			.append(DEVICE_TYPE)
			.append(':')
			.append(ARDUINO_DEVICE_ID).toString();
		
		callback.addDeviceInterface(key, arduino);
		Thread t = new Thread(callback);
		t.start();
		
	}
	
	public static void main(String[] args) throws IoTFCReSTException {
		
		SampleRasPiGateway sample = new SampleRasPiGateway();
		
		sample.createGatewayClient(PROPERTIES_FILE_NAME);

		/**
		 * There are different ways to register the device in Watson IoT Platform that are behind the Gateway.
		 * 
		 * 1. Auto registration: The Device gets added automatically
		 * 2. API: Using the Watson IoT Platform API
		 * 
		 * Register the device, based on user settings. 
		 */
		if(sample.bManualRegistrationMode) {
			sample.addDeviceType(DEVICE_TYPE);
			sample.addDevice(DEVICE_TYPE, ARDUINO_DEVICE_ID);
		} 
		sample.addCommandCallback();

		System.out.println("Gateway Started");
		
		/**
		 * Try to publish a Gateway Event for every second. As like devices, the Gateway
		 * also can have attached sensors and publish events.
		 */
		while(true) {
			sample.publishGatewayEvent();			
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ie) {}
		}
		
		//sample.disconnect();
		
	}

}
