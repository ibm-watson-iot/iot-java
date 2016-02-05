/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.sample.devicemgmt.gateway;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;
import com.ibm.iotf.sample.client.SystemObject;
import com.ibm.iotf.sample.client.gateway.ArduinoSerialInterface;
import com.ibm.iotf.sample.client.gateway.GatewayCommandCallback;
import com.ibm.iotf.sample.devicemgmt.device.DeviceActionHandlerSample;
import com.ibm.iotf.sample.devicemgmt.device.RasPiFirmwareHandlerSample;
import com.ibm.iotf.sample.devicemgmt.gateway.task.DiagnosticErrorCodeUpdateTask;
import com.ibm.iotf.sample.devicemgmt.gateway.task.DiagnosticLogUpdateTask;
import com.ibm.iotf.sample.devicemgmt.gateway.task.ManageTask;
import com.ibm.iotf.sample.util.Utility;

/**
 * A sample device management agent code that shows the following core DM capabilities,
 * 
 * 1. Managed device
 * 2. Firmware update
 * 3. Device Reboot
 * 4. Location update 
 * 5. Diagnostic ErrorCode addition & clear
 * 6. Diagnostic Log addition & clear 
 * 7. unmanage
 * 
 * This sample connects the device as manage device to IBM Watson IoT Platform in the first step such
 * that this device can participate in DM activities,
 * 
 * And performs the following activities based on user input
 * 
 *
 * manage [lifetime in seconds] :: Request to make the device as Managed device in IoTF
 * unmanage :: Request to make the device unmanaged
 * firmware :: Adds a Firmware Handler that listens for the firmware actions from IoTF)
 * reboot :: Adds a Device action Handler that listens for reboot from IoTF)
 * location :: Starts a task that updates a random location at every 30 seconds)
 * errorcode :: Starts a task that appends/clears a ErrorCode at every 30 seconds)
 * log :: Starts a task that appends/clears a Log message at every 30 seconds)
 * quit :: quit this program)
	 
 * This sample takes a properties file where the device informations and Firmware
 * informations are present. There is a default properties file in the sample folder, this
 * class takes the default properties file if one not specified by user.
 * 
 * Refer to this link https://docs.internetofthings.ibmcloud.com/reference/device_mgmt.html
 * for more information about IBM IBM Watson IoT Platform's DM capabilities 
 */
public class ManagedRasPiGateway {
	private static final int gatewayLifetime = 36000;
	private static final  int deviceLifetime = 42000;
	
	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2);
	
	private final static String PROPERTIES_FILE_NAME = "DMDeviceSample.properties";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";
	
	private final static String DEVICE_TYPE = "iotsample-deviceType";
	private final static String ARDUINO_DEVICE_ID = "Arduino01";
	/** The port where Arduino Uno normally connects to RaspberryPi. */
	private final static String DEFAULT_SERIAL_PORT = "/dev/ttyACM0";
	
	private ManagedGateway mgdGateway;
	SystemObject obj = new SystemObject();
	private String port;

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
		mgdGateway.setCommandCallback(callback);
		mgdGateway.subscribeToDeviceCommands(DEVICE_TYPE, ARDUINO_DEVICE_ID);
		try {
			ArduinoSerialInterface arduino = new ArduinoSerialInterface(
										ARDUINO_DEVICE_ID, 
										DEVICE_TYPE, 
										this.port, 
										this.mgdGateway);
			arduino.initialize();
		
			callback.addDeviceInterface(ARDUINO_DEVICE_ID, arduino);
			Thread t = new Thread(callback);
			t.start();
		} catch(Exception | Error e) {
			e.printStackTrace();
		}
		
	}
	

	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting the Managed Gateway...");
		String fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);
		
		ManagedRasPiGateway sample = new ManagedRasPiGateway();
		try {
			sample.createManagedClient(fileName);
			sample.sendGatewayManageRequest();
			
			sample.sendDeviceManageRequest();
			sample.addCommandCallback();
			
			sample.updateGatewayLocation();
			sample.updateDeviceLocation();
			
			// Start Errorcode tasks
			sample.scheduleGatewayErrorCodeTask();
			sample.scheduleDeviceErrorCodeTask();
			
			// Start Diagnostic Logs task
			sample.scheduleGatewayLogTask();
			sample.scheduleDeviceLogTask();
			
			// Add the handlers
			sample.addDeviceActionHandler();
			sample.addFirmwareHandler();
			
			
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

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.err.flush();
		} finally {
			sample.disconnect();
		}
		
		System.out.println(" Exiting...");
	}
	
	/**
	 * A method that updates a random location for the Gateway
	 */
	private void updateGatewayLocation() {
		Random random = new Random();
		// ...update location
		double latitude = random.nextDouble() + 30;
		double longitude = random.nextDouble() - 98;
		double elevation = (double)random.nextInt(100);
		int rc = mgdGateway.updateGatewayLocation(latitude, longitude, elevation);
		if(rc == 200) {
			System.out.println("Updated random location (" + latitude + " " + longitude +" " + elevation + ") for Gateway");
		} else {
			System.err.println("Failed to update the location (" + 
					latitude + " " + longitude +" " + elevation + "), rc ="+rc);
		}
	}
	
	/**
	 * A method that updates a random location for the Arduino Uno
	 */
	private void updateDeviceLocation() {
		Random random = new Random();
		// ...update location
		double latitude = random.nextDouble() + 30;
		double longitude = random.nextDouble() - 98;
		double elevation = (double)random.nextInt(100);
		int rc = mgdGateway.updateDeviceLocation(DEVICE_TYPE, ARDUINO_DEVICE_ID, latitude, longitude, elevation);
		if(rc == 200) {
			System.out.println("Updated random location (" + latitude + " " + longitude +" " + elevation + ") "
					+ "for device "+ARDUINO_DEVICE_ID);
		} else {
			System.err.println("Failed to update the location (" + 
					latitude + " " + longitude +" " + elevation + ") for device = "+ ARDUINO_DEVICE_ID +" reason ="+rc);
		}
	}
	
	/**
	 * ErrorCode Update Task - Appends a random errorcode at every 30th second
	 * 
	 * Also clears the same at every 25th interval
	 */
	private void scheduleGatewayErrorCodeTask() {
		DiagnosticErrorCodeUpdateTask ecTask = new DiagnosticErrorCodeUpdateTask(this.mgdGateway);
		scheduledThreadPool.scheduleAtFixedRate(ecTask, 0, 30, TimeUnit.SECONDS);
	}
	
	/**
	 * ErrorCode Update Task - Appends a random errorcode at every 30th second
	 * 
	 * Also clears the same at every 25th interval
	 */
	private void scheduleDeviceErrorCodeTask() {
		DiagnosticErrorCodeUpdateTask ecTask = new DiagnosticErrorCodeUpdateTask(this.mgdGateway, 
				DEVICE_TYPE, ARDUINO_DEVICE_ID);
		scheduledThreadPool.scheduleAtFixedRate(ecTask, 0, 30, TimeUnit.SECONDS);
	}
	
	
	/**
	 * Log Update Task - Appends a random log information at every 30th second
	 * Also clears the same at every 25th interval
	 */
	
	private void scheduleGatewayLogTask() {
		DiagnosticLogUpdateTask logTask = new DiagnosticLogUpdateTask(this.mgdGateway);
		scheduledThreadPool.scheduleAtFixedRate(logTask, 0, 30, TimeUnit.SECONDS);
	}
	
	/**
	 * Log Update Task - Appends a random log information at every 30th second
	 * Also clears the same at every 25th interval
	 */
	
	private void scheduleDeviceLogTask() {
		DiagnosticLogUpdateTask logTask = new DiagnosticLogUpdateTask(
				this.mgdGateway, DEVICE_TYPE, ARDUINO_DEVICE_ID);
		scheduledThreadPool.scheduleAtFixedRate(logTask, 0, 30, TimeUnit.SECONDS);
	}

	
	private String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}

	/**
	 * This method creates a ManagedGateway instance by passing the required properties 
	 * and connects the Gateway to the Watson IoT Platform by calling the connect function.
	 * 
	 * After the successful connection to the Watson IoT Platform, the Gateway can perform the following operations,
	 *   1. Publish events for itself and on behalf of devices connected behind the Gateway
	 *   2. Subscribe to commands for itself and on behalf of devices behind the Gateway
	 *   3. Send a manage request so that it can patricipate in the Device Management activities
	 */
	private void createManagedClient(String propertiesFile) throws Exception {
		
		/**
		 * Load device properties
		 */
		Properties deviceProps = Utility.loadPropertiesFile(PROPERTIES_FILE_NAME, propertiesFile);
		
		/**
		 * To create a DeviceData object, we will need the following objects:
		 *   - DeviceInfo
		 */
		DeviceInfo deviceInfo = new DeviceInfo.Builder().
				serialNumber(trimedValue(deviceProps.getProperty("DeviceInfo.serialNumber"))).
				manufacturer(trimedValue(deviceProps.getProperty("DeviceInfo.manufacturer"))).
				model(trimedValue(deviceProps.getProperty("DeviceInfo.model"))).
				deviceClass(trimedValue(deviceProps.getProperty("DeviceInfo.deviceClass"))).
				description(trimedValue(deviceProps.getProperty("DeviceInfo.description"))).
				fwVersion(trimedValue(deviceProps.getProperty("DeviceInfo.swVersion"))).
				hwVersion(trimedValue(deviceProps.getProperty("DeviceInfo.hwVersion"))).
				descriptiveLocation(trimedValue(deviceProps.getProperty("DeviceInfo.descriptiveLocation"))).
				build();
		
		DeviceData deviceData = new DeviceData.Builder().
						 		  deviceInfo(deviceInfo).
						 		  build();
		
		// Options to connect to IBM Watson IoT Platform
		Properties options = new Properties();
		
		options.setProperty("Organization-ID", trimedValue(deviceProps.getProperty("Organization-ID")));
		options.setProperty("Device-Type", trimedValue(deviceProps.getProperty("Device-Type")));
		options.setProperty("Device-ID", trimedValue(deviceProps.getProperty("Device-ID")));
		options.setProperty("Authentication-Method", trimedValue(deviceProps.getProperty("Authentication-Method")));
		options.setProperty("Authentication-Token", trimedValue(deviceProps.getProperty("Authentication-Token")));

		this.port = options.getProperty("port");
		if(this.port == null) {
			this.port = this.DEFAULT_SERIAL_PORT;
		}
		mgdGateway = new ManagedGateway(options, deviceData);
		
		// Connect to Watson IoT Platform
		mgdGateway.connect();
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
			
		mgdGateway.publishGatewayEvent("blink", event, 2);
	}
	
	private void disconnect() {
		//Disconnect cleanly
		mgdGateway.disconnect();
	}
	
	/**
	 * This method sends the manage request for the Gateway such that this Gateway
	 * can participate in the DM activities
	 * 
	 * @return status of the manage request
	 * @throws MqttException
	 */
	private boolean sendGatewayManageRequest() throws MqttException {
		boolean status = mgdGateway.sendGatewayManageRequest(gatewayLifetime, true, true);
		System.out.println("Status of Gateway manage request = "+ status);
		
		return status;
	}
	
	/**
	 * This method sends the manage request for the Arduino Uno Device connected to the 
	 * Raspberry Pi gateway
	 * 
	 * @return status of the manage request
	 * @throws MqttException
	 */
	private boolean sendDeviceManageRequest() throws MqttException {
		boolean status = mgdGateway.sendDeviceManageRequest(DEVICE_TYPE, ARDUINO_DEVICE_ID, deviceLifetime, true, true);
		System.out.println("Status of device manage request = "+ status);
		return status;
	}
	
	/**
	 * This method adds a Firmware handler where the device agent will get notified
	 *    when there is a firmware action from the server. 
	 */
	private void addFirmwareHandler() throws Exception {
		if(this.mgdGateway != null) {
			GatewayFirmwareHandlerSample fwHandler = new GatewayFirmwareHandlerSample();
			mgdGateway.addFirmwareHandler(fwHandler);
			System.out.println("Added Firmware Handler successfully !!");
		}
	}
	
	/**
	 * This method adds a device action handler where the device agent will get notified
	 * when there is a device action from the server. 
	 */
	private void addDeviceActionHandler() throws Exception {
		if(this.mgdGateway != null) {
			GatewayActionHandlerSample actionHandler = new GatewayActionHandlerSample();
			mgdGateway.addDeviceActionHandler(actionHandler);
			System.out.println("Added Device Action Handler successfully !!");
		}
	}
	
	private void sendUnManageRequest() throws MqttException {
		mgdGateway.sendGatewayUnmanageRequet();
		
		System.out.println("Stopping Tasks !!");
		
		scheduledThreadPool.shutdownNow();
		
		System.out.println("Tasks Stopped!!");
	}

}
