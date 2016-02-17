/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.gateway;

import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.gateway.GatewayClient;
import com.ibm.iotf.sample.client.SystemObject;
import com.ibm.iotf.sample.util.Utility;

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
	
	private final static String PROPERTIES_FILE_NAME = "device.prop";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";
	
	private final static String DEVICE_TYPE = "iotsample-deviceType";
	private final static String ARDUINO_DEVICE_ID = "Arduino01";
	/** The port where Arduino Uno normally connects to RaspberryPi. */
	private final static String DEFAULT_SERIAL_PORT = "/dev/ttyACM0";
	
	private GatewayClient gwClient = null;
	SystemObject obj = new SystemObject();
	private String gwDeviceId;
	private String gwDeviceType;
	private String port;
	private APIClient apiClient;
	
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
		 * Load properties file "device.prop"
		 */
		Properties props = Utility.loadPropertiesFile(PROPERTIES_FILE_NAME, fileName);
		
		try {
			//Instantiate & connect the Gateway by passing the properties file
			gwClient = new GatewayClient(props);
			this.gwDeviceId = props.getProperty("Device-ID");
			this.gwDeviceType = props.getProperty("Device-Type");
			this.port = props.getProperty("port");
			if(this.port == null) {
				this.port = this.DEFAULT_SERIAL_PORT;
			}
			gwClient.connect();
			
			Properties options = new Properties();
			options.put("Organization-ID", props.getProperty("Organization-ID"));
			options.put("id", "app" + (Math.random() * 10000));		
			options.put("Authentication-Method","apikey");
			options.put("API-Key", props.getProperty("API-Key"));		
			options.put("Authentication-Token", props.getProperty("API-Token"));
			
			this.apiClient = new APIClient(options);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * This sample showcases how to Create a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	private void addDeviceType() throws IoTFCReSTException {
		try {
			JsonObject response = this.apiClient.addDeviceType(DEVICE_TYPE, null, null, null);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * Add a device under the given gateway using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void addDevice(String deviceId) throws IoTFCReSTException {
		try{
			
			String deviceToBeAdded = "{\"deviceId\": \"" + deviceId +
						"\",\"authToken\": \"qwert123\"}";

			System.out.println(deviceToBeAdded);
			JsonParser parser = new JsonParser();
			JsonElement input = parser.parse(deviceToBeAdded);
			JsonObject response = this.apiClient.
					registerDeviceUnderGateway(DEVICE_TYPE, this.gwDeviceId, this.gwDeviceType, input);
			System.out.println(response);
			
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
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
		GatewayCommandCallback callback = new GatewayCommandCallback();
		gwClient.setCommandCallback(callback);
		gwClient.subscribeToDeviceCommands(DEVICE_TYPE, ARDUINO_DEVICE_ID);
				
		ArduinoSerialInterface arduino = new ArduinoSerialInterface(
										ARDUINO_DEVICE_ID, 
										DEVICE_TYPE, 
										this.port, 
										this.gwClient);
		try {
			arduino.initialize();
		} catch(Error e) {
			e.printStackTrace();
		}
		callback.setGatewayId(this.gwDeviceId);
		callback.addDeviceInterface(ARDUINO_DEVICE_ID, arduino);
		Thread t = new Thread(callback);
		t.start();
		
	}
	
	public static void main(String[] args) throws IoTFCReSTException {
		
		SampleRasPiGateway sample = new SampleRasPiGateway();
		
		String fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);
		sample.createGatewayClient(fileName);

		/**
		 * There are different ways to register the device in Watson IoT Platform that are behind the Gateway.
		 * 
		 * 1. Auto registration: The Device gets added automatically
		 * 2. API: Using the Watson IoT Platform API
		 * 
		 * In this case we will add the device using the Watson Platform API.
		 */
		sample.addDeviceType();
		sample.addDevice(ARDUINO_DEVICE_ID);
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
