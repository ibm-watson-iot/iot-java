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
package com.ibm.iotf.sample.devicemgmt.gateway;

import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;
import com.ibm.iotf.sample.client.gateway.ArduinoInterface;
import com.ibm.iotf.sample.client.gateway.DeviceInterface;
import com.ibm.iotf.sample.client.gateway.GatewayCommandCallback;
import com.ibm.iotf.sample.util.Utility;

/**
 * <p>The following Gateway Device Management(DM) capabilities are demonstrated in this sample
 * by managing the Arduino Uno device through the Raspberry Pi Gateway.</p>
 * 
 * <ul class="simple">
 * <li>Manage - Manage the Gateway and the attached Arduino Uno device
 * <li>Firmware update - Update the sketch code of Arduino Uno via the Gateway
 * <li>Device Reboot - Reboot both the Raspberry Pi Gateway and Arduino Uno device
 * <li>Location Update - Update the location of Gateway and Arduino Uno device
 * <li>Diagnostic update - Updates the Errorcode and Log information of Gateway and Arduino Uno to Watson IoT platform
 * <li>Unmanage - Move the Gateway and Arduino Uno from managed state to unmanage state
 * </ul>
 *  
 *  <p>This sample adds a gateway management agent on top of the sample presented in the 
 *  Gateway recipe to demonstrate the end to end capabilities of the Gateway. 
 *  i.e, perform DM operations while sending and receiving sensor events and commands 
 *  for the Gateway and attached devices. The management agent running on the Raspberry Pi 
 *  Gateway will connect the Gateway and Arduino Uno device as managed devices to 
 *  IBM Watson IoT Platform, in the first step, such that they can participate in the 
 *  DM activities. The agent then listen for the DM requests from the Watson IoT Platform 
 *  DM server, upon receiving the request, it completes the action and responds to the server 
 *  about the completion status. Also, the agent can update the diagnostic information 
 *  whenever there is an error and update the location of the Gateway/attached devices 
 *  frequently to Watson IoT Platform.</p> 
 * 
 * Performs the following activities based on user input<br>
 * 
 *
 * manage [gateway|device] [lifetime] :: Request to make the gateway/device as Managed device in WIoTP<br>
 * unmanage [gateway|device]          :: Request to make the gateway/device unmanaged<br>
 * location [gateway|device]          :: updates a random location of the device/gateway<br>
 * errorcode [gateway|device]         :: appends/clears a simulated ErrorCode<br>
 * log [gateway|device]               :: appends/clears a simulated Log message<br>
 * display                            :: Toggle the Arduino Uno event display on the console<br>
 * quit                               :: quit this sample<br>
 * 
 * 	 
 * <p>This sample takes a properties file where the device informations and Firmware
 * informations are present. There is a default properties file in the sample folder, this
 * class takes the default properties file if one not specified by user.</p>
 * 
 * Refer to this link https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/index.html
 * for more information about IBM IBM Watson IoT Platform's DM capabilities 
 */
public class ManagedRasPiGateway {
	private final static String PROPERTIES_FILE_NAME = "DMGatewaySample.properties";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";
	
	private final static String DEVICE_TYPE = "iotsample-deviceType";
	private final static String ARDUINO_DEVICE_ID = "Arduino01";
	/** The port where Arduino Uno normally connects to RaspberryPi. */
	private final static String DEFAULT_SERIAL_PORT = "/dev/ttyACM0";
	
	private ManagedGateway mgdGateway;
	private DeviceInterface arduino;      // Represents either Arduino Uno simulator or hardware
	private String port;
	private Random random = new Random();
	private APIClient apiClient;

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
			callback.addDeviceInterface(ARDUINO_DEVICE_ID, arduino);
			Thread t = new Thread(callback);
			t.start();
		} catch(Exception | Error e) {
			e.printStackTrace();
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
			if(e.getHttpCode() != 409) { // 409 : device type is already present
				System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
				// Print if there is a partial response
				System.out.println(e.getResponse());
			}
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
			JsonObject response = this.apiClient.registerDeviceUnderGateway(
							DEVICE_TYPE, 
							this.mgdGateway.getGWDeviceId(), 
							this.mgdGateway.getGWTypeId(), 
							input);
			System.out.println(response);
			
		} catch(IoTFCReSTException e) {
			if(e.getHttpCode() != 409) { // 409 : device is already present
				
				System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
				// Print if there is a partial response
				System.out.println(e.getResponse());
			}
		}
	}	
	

	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting the Managed Gateway...");
		String fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);
		
		ManagedRasPiGateway sample = new ManagedRasPiGateway();
		try {
			sample.createManagedClient(fileName);
			sample.createArduinoDeviceInterface();
			
			sample.addDeviceType();
			sample.addDevice(ARDUINO_DEVICE_ID);
			
			
			// Add the handlers
			sample.addDeviceActionHandler();
			sample.addFirmwareHandler();

			sample.addCommandCallback();
			sample.arduino.toggleDisplay(); // activate the console display 
			sample.userAction();
			

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
	 * Appends a random errorcode to the Gateway
	 * 
	 */
	private void appendGatewayErrorCode() {
		int errorCode = random.nextInt(500);
		int rc = this.mgdGateway.addGatewayErrorCode(errorCode);
		if(rc == 200) {
			System.out.println("Current Gateway Errorcode (" + errorCode + ")");
		} else {
			System.out.println("Errorcode addition failed for Gateway!, rc = "+rc);
		}
	}
	
	/**
	 * Appends a random errorcode for the Device
	 * 
	 */
	private void appendDeviceErrorCode() {
		int errorCode = random.nextInt(500);
		int rc = this.mgdGateway.addDeviceErrorCode(DEVICE_TYPE, ARDUINO_DEVICE_ID, errorCode);
		if(rc == 200) {
			System.out.println("Current Device Errorcode (" + errorCode + ")");
		} else {
			System.out.println("Errorcode addition failed for Device!, rc = "+rc);
		}
	}
	

	/**
	 * Appends a random Log message to the Gateway
	 * 
	 */
	private void appendGatewayLog() {
		String message = "Log event " + random.nextInt(500);
		Date timestamp = new Date();
		LogSeverity severity = LogSeverity.informational;
		int randomValue = this.random.nextInt();
		if(randomValue % 10 == 0) {
			severity = LogSeverity.error;
		} else if(randomValue % 10 >= 5) {
			severity = LogSeverity.warning;
		}
		int rc = mgdGateway.addGatewayLog(message, timestamp, severity);
			
		if(rc == 200) {
			System.out.println("Current Gateway Log (" + message + " " + timestamp + " " + severity + ")");
		} else {
			System.out.println("Gateway Log Addition failed!, rc = "+rc);
		}
	}
	
	/**
	 * Appends a random Log message for the Device
	 * 
	 */
	private void appendDeviceLog() {
		String message = "Log event " + random.nextInt(500);
		Date timestamp = new Date();
		LogSeverity severity = LogSeverity.informational;
		int randomValue = this.random.nextInt();
		if(randomValue % 10 == 0) {
			severity = LogSeverity.error;
		} else if(randomValue % 10 >= 5) {
			severity = LogSeverity.warning;
		}
		int rc = mgdGateway.addDeviceLog(DEVICE_TYPE, ARDUINO_DEVICE_ID, message, timestamp, severity);
			
		if(rc == 200) {
			System.out.println("Current device("+ARDUINO_DEVICE_ID+") Log (" + message + " " + timestamp + " " + severity + ")");
		} else {
			System.out.println("Device("+ARDUINO_DEVICE_ID+") Log Addition failed!, rc = "+rc);
		}
		
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
		 * Let us create the DeviceData object with the DeviceInfo object
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
		
		this.port = deviceProps.getProperty("port");
		if(this.port == null) {
			this.port = this.DEFAULT_SERIAL_PORT;
		}
		mgdGateway = new ManagedGateway(deviceProps, deviceData);
		
		// Connect to Watson IoT Platform
		mgdGateway.connect();
		
		// We need to create APIclint to register the Arduino Uno device if its not registered already
		Properties options = new Properties();
		options.put("Organization-ID", deviceProps.getProperty("Organization-ID"));
		options.put("id", "app" + (Math.random() * 10000));		
		options.put("Authentication-Method","apikey");
		options.put("API-Key", deviceProps.getProperty("API-Key"));		
		options.put("Authentication-Token", deviceProps.getProperty("API-Token"));
		this.apiClient = new APIClient(options);
	}
	
	/**
	 * Create the Arduino device interface object used to interact with Arduino Uno device
	 */
	private void createArduinoDeviceInterface() {
		this.arduino = ArduinoInterface.createDevice(
				ARDUINO_DEVICE_ID, 
				DEVICE_TYPE, 
				this.port, 
				this.mgdGateway);
		
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
	 * @throws Exception 
	 */
	private boolean sendGatewayManageRequest(int lifetime) throws Exception {
		boolean status = mgdGateway.sendGatewayManageRequest(lifetime, true, true);
		if(status == true) {
			System.out.println("Gateway is connected as managed device now !!");
		} else {
			System.out.println("Gateway manage request failed!!");
		}
		return status;
	}
	
	/**
	 * This method sends the manage request for the Arduino Uno Device connected to the 
	 * Raspberry Pi gateway
	 * 
	 * @return status of the manage request
	 * @throws Exception 
	 */
	private boolean sendDeviceManageRequest(int lifetime) throws Exception {
		boolean status = mgdGateway.sendDeviceManageRequest(DEVICE_TYPE, ARDUINO_DEVICE_ID, lifetime, true, true);
		if(status == true) {
			System.out.println("Arduino Uno device is connected as managed device now !!");
		} else {
			System.out.println("Arduino Uno manage request failed!!");
		}
		return status;
	}
	
	/**
	 * This method adds a Firmware handler where the device agent will get notified
	 *    when there is a firmware action from the server. 
	 */
	private void addFirmwareHandler() throws Exception {
		if(this.mgdGateway != null) {
			GatewayFirmwareHandlerSample fwHandler = new GatewayFirmwareHandlerSample();
			fwHandler.addDeviceInterface(ARDUINO_DEVICE_ID, arduino);
			fwHandler.setGatewayDeviceId(this.mgdGateway.getGWDeviceId());
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
			actionHandler.addDeviceInterface(ARDUINO_DEVICE_ID, arduino);
			actionHandler.setGatewayDeviceId(this.mgdGateway.getGWDeviceId());
			mgdGateway.addDeviceActionHandler(actionHandler);
			System.out.println("Added Device Action Handler successfully !!");
		}
	}
	
	/**
	 * Moves the Gateway from managed state to unmanaged state
	 * 
	 * @return status of the Unmanage request
	 * @throws MqttException
	 */
	private boolean sendGatewayUnmanageRequest() throws MqttException {
		boolean status = mgdGateway.sendGatewayUnmanageRequet();
		System.out.println("Status of Gateway Unmanage request = "+ status);
		
		return status;
	}
	
	/**
	 * Moves the Arduino Uno device from managed state to unmanaged state
	 * 
	 * @return status of the Unmanage request
	 * @throws MqttException
	 */
	private boolean sendDeviceUnmanageRequest() throws MqttException {
		boolean status = mgdGateway.sendDeviceUnmanageRequet(DEVICE_TYPE, ARDUINO_DEVICE_ID);
		System.out.println("Status of device unmanage request = "+ status);
		return status;
	}
	
	private void userAction() {
    	Scanner in = new Scanner(System.in);
    	final String DEVICE = "device";
    	final String GATEWAY = "gateway";
    	TYPE type = TYPE.GATEWAY_AND_DEVICE;
    	printOptions();
    	while(true) {
    		try {
	    		System.out.println("Enter the command ");
	    		type = TYPE.GATEWAY_AND_DEVICE;
	            String input = in.nextLine();
	            String[] parameters = input.split(" ");
	            if(parameters.length == 2) {
        			if(DEVICE.equalsIgnoreCase(parameters[1])) {
        				type = TYPE.DEVICE;
        			} else if(GATEWAY.equalsIgnoreCase(parameters[1])) {
        				type = TYPE.GATEWAY;
        			}
	            }
	            
	            switch(parameters[0]) {
	            
	            case "manage":
	            	int lifetime = 0;
	            	if(parameters.length == 2 && type == TYPE.GATEWAY_AND_DEVICE) {
	            		// User has entered a lifetime paramter
	            		try {
	            			lifetime = Integer.parseInt(parameters[1]);
	            		} catch(Exception e) {
	            			// Ignore any invalid numbers.
	            		}
	            	} else if(parameters.length == 3) {
	            		// User has entered a lifetime paramter
	            		try {
	            			lifetime = Integer.parseInt(parameters[2]);
	            		} catch(Exception e) {
	            			// Ignore any invalid numbers.
	            		}
	            	}
	            	
           			if(type == TYPE.GATEWAY || type == TYPE.GATEWAY_AND_DEVICE) {
           				this.sendGatewayManageRequest(lifetime);
           			} 
           			if(type == TYPE.DEVICE || type == TYPE.GATEWAY_AND_DEVICE) {
           				this.sendDeviceManageRequest(lifetime);
            		}
            		break;
	            
	            	case "unmanage":
	            		if(type == TYPE.GATEWAY || type == TYPE.GATEWAY_AND_DEVICE) {
	           				this.sendGatewayUnmanageRequest();
	            		}
	            		if(type == TYPE.DEVICE || type == TYPE.GATEWAY_AND_DEVICE) {
	           				this.sendDeviceUnmanageRequest();
	            		}
	            		break;
	            		
	            	case "location":
	            		if(type == TYPE.GATEWAY || type == TYPE.GATEWAY_AND_DEVICE) {
	            			updateGatewayLocation();
	            		}
	            		if(type == TYPE.DEVICE || type == TYPE.GATEWAY_AND_DEVICE) {
	           				updateDeviceLocation();
	            		}
	        			break;
	
	            	case "errorcode":
	            		if(type == TYPE.GATEWAY || type == TYPE.GATEWAY_AND_DEVICE) {
	            			appendGatewayErrorCode();
	            		}
	            		if(type == TYPE.DEVICE || type == TYPE.GATEWAY_AND_DEVICE) {
	           				appendDeviceErrorCode();
	            		}
	            		break;
	            		
	            	case "log":
	            		if(type == TYPE.GATEWAY || type == TYPE.GATEWAY_AND_DEVICE) {
	            			appendGatewayLog();
	            		}
	            		if(type == TYPE.DEVICE || type == TYPE.GATEWAY_AND_DEVICE) {
	           				appendDeviceLog();
	            		}
	            		break;
	            		
	            	case "quit":
	            		this.terminate();
	            		break;
	            		
	            	case "display":
	            		this.arduino.toggleDisplay();
	            		break;
	
	
	            	default:
	            		System.out.println("Unknown command received :: "+input);
	            		printOptions();
	            		
	            }
    		} catch(Exception e) {
    			System.out.println("Operation failed with exception "+e.getMessage());
    			e.printStackTrace();
    			printOptions();
    			continue;
    		}
    	}
    }
	
	private enum TYPE {
		GATEWAY_AND_DEVICE(0), GATEWAY(1), DEVICE(2); 
		
		private final int type;
		
		private TYPE(int type) {
			this.type = type;
		}
	}
	
	private static void printOptions() {
		System.out.println("List of device management operations that this agent can perform are:");
		System.out.println("manage [gateway|device][lifetime] :: Request to make the gateway/device as Managed device in WIoTP");
		System.out.println("unmanage [gateway|device]         :: Request to make the gateway/device unmanaged ");
		System.out.println("location [gateway|device]         :: updates a random location of the device/gateway");
		System.out.println("errorcode [gateway|device]        :: appends/clears a simulated ErrorCode");
		System.out.println("log [gateway|device]              :: appends/clears a simulated Log message");
		System.out.println("display                           :: Toggle the Arduino Uno event display on the console");
		System.out.println("quit                              :: quit this sample");
	}
	
	private void terminate() throws Exception {
		if(this.mgdGateway != null) {
			mgdGateway.disconnect();
			System.out.println("Bye !!");
			System.exit(-1);
		}
	}

}
