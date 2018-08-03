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
package com.ibm.iotf.sample.client.gateway.devicemgmt;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;
import com.ibm.iotf.sample.client.gateway.GatewayCommandCallback;
import com.ibm.iotf.sample.client.gateway.SystemObject;
import com.ibm.iotf.sample.client.gateway.device.ArduinoInterface;
import com.ibm.iotf.sample.client.gateway.device.DeviceInterface;
/**
 * <p>The following Gateway Device Management(DM) capabilities are demonstrated in this sample
 * by managing the Arduino Uno device through the Raspberry Pi Gateway.</p>
 * 
 * <ul class="simple">
 * <li>	Manage - Manage the Gateway and the attached 
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
	private final static String PROPERTIES_FILE_NAME = "/DMGatewaySample.properties";
	
	private final static String DEVICE_TYPE = "iotsample-deviceType";
	private final static String ARDUINO_DEVICE_ID = "Arduino01";
	/** The port where Arduino Uno normally connects to RaspberryPi. */
	private final static String DEFAULT_SERIAL_PORT = "/dev/ttyACM0";
	
	// Create a threadpool that can handle the firmware/device action requests from the Watson IoT Platform
	// in bulk, for example, if a user wants to reboot all the devices connected to the gateway in one go,
	// gateway should be able to handle the load if there are 1000 or more devices connected to it. In this sample
	// we are creating a pool with one thread and set it to Firmware and device action handler as it manages
	// only one device. But increase, in case if you want to manage more devices. Please refer to
	// HomeGatewaySample that manages ~10 devices and provides a framework to add more devices through the gateway
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private ManagedGateway mgdGateway;
	// Represents either Arduino Uno simulator or hardware
	private DeviceInterface arduino;      
	// The port in which Arduino Uno and Rasperry Pi is connected - required onlu when hardwares are present
	private String port;
	private SystemObject obj = new SystemObject();

	private GatewayActionHandlerSample actionHandler;
	private GatewayFirmwareHandlerSample fwHandler;
	private GatewayCommandCallback callback;
	/**
	 * There are different ways to register the device in Watson IoT Platform that are behind the Gateway.
	 * 
	 * 1. Auto registration: The Device gets added automatically
	 * 2. API: Using the Watson IoT Platform API
	 * 
	 * This field carries the user selection. 
	 */
	private boolean bManualRegistrationMode;
	
	// Required if the user wants to add the device using manual registration
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
		callback = new GatewayCommandCallback(this.mgdGateway);
		mgdGateway.setGatewayCallback(callback);
		try {
			Thread t = new Thread(callback);
			t.start();
		} catch(Exception | Error e) {
			e.printStackTrace();
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
			}catch(Exception e) {}
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
				exist = mgdGateway.api().isDeviceExist(deviceType, deviceId);
			}catch(Exception e){}
			if(!exist) {
				System.out.println("<-- Creating device " + deviceId +" with deviceType " +
						deviceType +" now..");
				mgdGateway.api().registerDeviceUnderGateway(deviceType, deviceId,
						this.mgdGateway.getGWDeviceType(), 
						this.mgdGateway.getGWDeviceId());
			}
		} catch (IoTFCReSTException ex) {
			
			System.out.println("ERROR: unable to add manually device " + deviceId);
		}
	}	
	

	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting the Managed Gateway...");
		final ManagedRasPiGateway sample = new ManagedRasPiGateway();
		try {
			sample.createManagedClient(PROPERTIES_FILE_NAME);
			final boolean isSimulatorRequired = isSimulatorRequired(PROPERTIES_FILE_NAME);
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
			System.out.println("Sending a manage request for the Gateway..");
			sample.sendGatewayManageRequest(0);
			System.out.println("Updating the location of the Gateway..");
			sample.updateGatewayLocation();

			// Thread to wait for Arduino connectivity
			Thread t = new Thread() {
				public void run() {
					boolean arduinoConnected = false;
					while(!arduinoConnected) {
						arduinoConnected = sample.createArduinoDeviceInterface(isSimulatorRequired);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			};
			t.start();
			
			/**
			 * Try to publish a Gateway Event for every 5 second. As like devices, the Gateway
			 * also can have attached sensors and publish events.
			 */
			while(true) {
				sample.publishGatewayEvent();			
				try {
					Thread.sleep(5000);
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
			
		this.mgdGateway.publishGatewayEvent("blink", event, 2);
		System.out.println("<--(GW) Gateway event :: "+event);
	}
	
	private static double getLatitude(String propertiesFile) {
		 /**
		  * Load device properties
		  */
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(ManagedRasPiGateway.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		String val = deviceProps.getProperty("latitude", "-1");
		return Double.parseDouble(val);
	}
	
	private static double getLongitude(String propertiesFile) {
		 /**
		  * Load device properties
		  */
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(ManagedRasPiGateway.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		String val = deviceProps.getProperty("longitude", "-1");
		return Double.parseDouble(val);
	}

	private static double getElevation(String propertiesFile) {
		 /**
		  * Load device properties
		  */
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(ManagedRasPiGateway.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		String val = deviceProps.getProperty("elevation", "-1");
		return Double.parseDouble(val);
	}
	
	/**
	 * A method that updates a random location for the Gateway
	 */
	private void updateGatewayLocation() {
		// ...update location
		double latitude = getLatitude(PROPERTIES_FILE_NAME);
		double longitude = getLongitude(PROPERTIES_FILE_NAME);
		double elevation = getElevation(PROPERTIES_FILE_NAME);
		
		if(latitude !=-1 && longitude != -1 && elevation != -1) {
			int rc = mgdGateway.updateGatewayLocation(latitude, longitude, elevation);
			if(rc == 200) {
				System.out.println("Updated location (" + latitude + " " + longitude +" " + elevation + ") for Gateway");
			} else {
				System.err.println("Failed to update the location (" + 
						latitude + " " + longitude +" " + elevation + "), rc ="+rc);
			}
		}
	}
	
	/**
	 * A method that updates a random location for the Gateway
	 */
	private void updateDeviceLocation() {
		// ...update location
		double latitude = getLatitude(PROPERTIES_FILE_NAME);
		double longitude = getLongitude(PROPERTIES_FILE_NAME);
		double elevation = getElevation(PROPERTIES_FILE_NAME);
		
		if(latitude !=-1 && longitude != -1 && elevation != -1) {
			int rc = mgdGateway.updateDeviceLocation(DEVICE_TYPE, ARDUINO_DEVICE_ID, latitude, longitude, elevation);
			if(rc == 200) {
				System.out.println("Updated location (" + latitude + " " + longitude +" " + elevation + ") for Device");
			} else {
				System.err.println("Failed to update the location (" + 
						latitude + " " + longitude +" " + elevation + "), rc ="+rc);
			}
		}
	}
	
	private static String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}
	
	private static DeviceData getDeviceData(String propertiesFile) {
		 /**
		  * Load device properties
		  */
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(ManagedRasPiGateway.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		

		/**
		 * Let us create the DeviceData object with the DeviceInfo object
		 */
		DeviceInfo deviceInfo = new DeviceInfo.Builder().
				serialNumber(trimedValue(deviceProps.getProperty("DeviceInfo.serialNumber"))).
				manufacturer(trimedValue(deviceProps.getProperty("DeviceInfo.manufacturer"))).
				model(trimedValue(deviceProps.getProperty("DeviceInfo.model"))).
				deviceClass(trimedValue(deviceProps.getProperty("DeviceInfo.deviceClass"))).
				description(trimedValue(deviceProps.getProperty("DeviceInfo.description"))).
				fwVersion(trimedValue(deviceProps.getProperty("DeviceInfo.fwVersion"))).
				hwVersion(trimedValue(deviceProps.getProperty("DeviceInfo.hwVersion"))).
				descriptiveLocation(trimedValue(deviceProps.getProperty("DeviceInfo.descriptiveLocation"))).
				build();
		
		DeviceFirmware firmware = new DeviceFirmware.Builder().version(deviceProps.getProperty("DeviceFirmware.version")).build();
		
		DeviceData deviceData = new DeviceData.Builder().
						 		  deviceInfo(deviceInfo).
						 		  deviceFirmware(firmware).
						 		  build();
		
		return deviceData;
	}
	
	private static String getPort(String propertiesFile) {
		 /**
		  * Load device properties
		  */
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(ManagedRasPiGateway.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		String port = deviceProps.getProperty("port");
		if(port == null) {
			port = DEFAULT_SERIAL_PORT;
		}
		return port;
	}
	
	private static boolean isSimulatorRequired(String propertiesFile) {
		 /**
		  * Load device properties
		  */
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(ManagedRasPiGateway.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		String sim = deviceProps.getProperty("simulator","false");
		return Boolean.parseBoolean(sim);
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
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(ManagedRasPiGateway.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		

		DeviceData deviceData = getDeviceData(propertiesFile);
		this.port = getPort(propertiesFile);
		
		mgdGateway = new ManagedGateway(deviceProps, deviceData);
		
		// Connect to Watson IoT Platform
		mgdGateway.connect();
		
		String mode = deviceProps.getProperty("Registration-Mode");
		
		if("MANUAL".equalsIgnoreCase(mode)) {
			this.bManualRegistrationMode = true;
			// We need to create APIclint to register the device type of Arduino Uno device, if its not registered already
			Properties options = new Properties();
			options.put("Organization-ID", deviceProps.getProperty("Organization-ID"));
			options.put("id", "app" + (Math.random() * 10000));		
			options.put("Authentication-Method","apikey");
			options.put("API-Key", deviceProps.getProperty("API-Key"));		
			options.put("Authentication-Token", deviceProps.getProperty("API-Token"));
			this.apiClient = new APIClient(options);
		} else {
			this.bManualRegistrationMode = false;
		}

	}
	
	/**
	 * Create the Arduino device interface object used to interact with Arduino Uno device
	 */
	private boolean createArduinoDeviceInterface(boolean simulator) {
		this.arduino = ArduinoInterface.createDevice(
				ARDUINO_DEVICE_ID, 
				DEVICE_TYPE, 
				this.port, 
				this.mgdGateway,
				simulator);
		
		if(this.arduino != null) {
			System.out.println("Detected Arduino Uno, Provisioning to Watson IoT Platform!");
			callback.addDeviceInterface(getKey(), arduino);
			mgdGateway.subscribeToDeviceCommands(DEVICE_TYPE, ARDUINO_DEVICE_ID);
			actionHandler.addDeviceInterface(getKey(), arduino);
			fwHandler.addDeviceInterface(getKey(), arduino);
			try {
				sendDeviceManageRequest(0);
				this.updateDeviceLocation();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			arduino.toggleDisplay();
			Date timestamp = new Date();
			LogSeverity severity = LogSeverity.informational;
			mgdGateway.addGatewayLog("Arduino connected to Watson IoT", timestamp, severity);
			return true;
		} else {
			return false;
		}
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
		
		/**
		 *  Add the handlers inorder to receive the Firmware action/Device action.
		 *  The library supports a handler for the gateway and all the attached devices.
		 */
		
		this.addDeviceActionHandler();
		this.addFirmwareHandler();

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
		DeviceData deviceData = getDeviceData(PROPERTIES_FILE_NAME);
		deviceData.setDeviceId(ARDUINO_DEVICE_ID);
		deviceData.setTypeId(DEVICE_TYPE);
		boolean status = mgdGateway.sendDeviceManageRequest(DEVICE_TYPE, ARDUINO_DEVICE_ID, deviceData, lifetime, true, true);
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
			fwHandler = new GatewayFirmwareHandlerSample();
			fwHandler.setGateway(this.mgdGateway);
			mgdGateway.addFirmwareHandler(fwHandler);
			fwHandler.setExecutor(executor);
			System.out.println("Added Firmware Handler successfully !!");
		}
	}
	
	/**
	 * This method adds a device action handler where the device agent will get notified
	 * when there is a device action from the server. 
	 */
	private void addDeviceActionHandler() throws Exception {
		if(this.mgdGateway != null) {
			actionHandler = new GatewayActionHandlerSample();
			actionHandler.setGateway(mgdGateway);
			mgdGateway.addDeviceActionHandler(actionHandler);
			actionHandler.setExecutor(executor);

			System.out.println("Added Device Action Handler successfully !!");
		}
	}
	
	private void terminate() throws Exception {
		if(this.mgdGateway != null) {
			mgdGateway.disconnect();
			System.out.println("Bye !!");
			System.exit(-1);
		}
	}
	
	private String getKey() {
		// Create the WIoTP client Id to uniquely identify the device
		return new StringBuilder("d:")
			.append(mgdGateway.getOrgId())
			.append(':')
			.append(DEVICE_TYPE)
			.append(':')
			.append(ARDUINO_DEVICE_ID).toString();

	}

}
