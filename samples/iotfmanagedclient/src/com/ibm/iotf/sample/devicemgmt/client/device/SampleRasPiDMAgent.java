/**
 *****************************************************************************
szaaq` Copyright (c) 2015 IBM Corporation and other Contributors.
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
package com.ibm.iotf.sample.devicemgmt.client.device;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.client.device.DeviceDiagnostic;
import com.ibm.iotf.devicemgmt.client.device.DeviceLocation;
import com.ibm.iotf.devicemgmt.client.device.DiagnosticErrorCode;
import com.ibm.iotf.devicemgmt.client.device.DiagnosticLog;
import com.ibm.iotf.devicemgmt.client.device.ManagedClient;
import com.ibm.iotf.devicemgmt.client.device.DeviceData;
import com.ibm.iotf.devicemgmt.client.device.DeviceFirmware;
import com.ibm.iotf.devicemgmt.client.device.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.client.device.DeviceInfo;

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
 * This sample connects the device as manage device to IoT Foundation in the first step such
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
 * for more information about IBM IoT Foundation's DM capabilities 
 */
public class SampleRasPiDMAgent {
	private final static String PROPERTIES_FILE_NAME = "DMDeviceSample.properties";
	private final static String DEFAULT_PATH = "samples/iotfmanagedclient/src";
	private DeviceData deviceData;
	private ManagedClient dmClient;
	
	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
	private ScheduledFuture manageTask;
	private ScheduledFuture locationTask;
	private ScheduledFuture errorcodeTask;
	private ScheduledFuture logTask;
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting sample DM agent...");
		String fileName = null;
		if (args.length == 1) {
			fileName = args[0];
		} else {
			fileName = getDefaultFilePath();
		}
		
		SampleRasPiDMAgent sample = new SampleRasPiDMAgent();
		try {
			sample.createManagedClient(fileName);
			sample.connect();
			sample.scheduleDeviceEventPublishTask();
			sample.userAction();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			sample.terminate();
		}
		
		System.out.println(" Exiting...");
	}
	
	/**
	 * Device Event publish Task  - publish an event every 1 minute,
	 * 
	 * this is to showcase that one can publish events while carrying 
	 * out DM activities
	 */
	private void scheduleDeviceEventPublishTask() {
		PublishDeviceEventTask task = this.new PublishDeviceEventTask();
		scheduledThreadPool.scheduleAtFixedRate(task, 0, 60, TimeUnit.SECONDS);
	}

	/**
	 * Location Update Task  - updates random location at every 30th second
	 */
	private void scheduleLocationTask() {
		if(locationTask == null) {
			LocationUpdateTask locTask = this.new LocationUpdateTask(this.deviceData.getDeviceLocation());
			this.locationTask = scheduledThreadPool.scheduleAtFixedRate(locTask, 0, 30, TimeUnit.SECONDS);
			System.out.println("Location Update Task started successfully");
		} else {
			System.out.println("Location task is already scheduled !!");
		}
	}
	
	/**
	 * ErrorCode Update Task - Appends/clears an errorcode at every 30th second
	 */
	private void scheduleErrorCodeTask() {
		if(errorcodeTask == null) {
			DiagnosticErrorCodeUpdateTask ecTask = this.new DiagnosticErrorCodeUpdateTask(this.deviceData.getDeviceDiag());
			this.errorcodeTask = scheduledThreadPool.scheduleAtFixedRate(ecTask, 0, 30, TimeUnit.SECONDS);
			System.out.println("ErrorCode Update Task started successfully");
		} else {
			System.out.println("ErrorCode update task is already running !!");
		}
	}
	
	/**
	 * Log Update Task - Appends/clears a log information at every 30th second
	 */
	
	private void scheduleLogTask() {
		
		if(this.logTask == null) {
			DiagnosticLogUpdateTask logTask = this.new DiagnosticLogUpdateTask(this.deviceData.getDeviceDiag());
			this.logTask = scheduledThreadPool.scheduleAtFixedRate(logTask, 0, 30, TimeUnit.SECONDS);
			System.out.println("Log Update Task started successfully");
		} else {
			System.out.println("Log update task is already running !!");
		}
	}

	/**
	 * This method builds the device objects required to create the
	 * ManagedClient
	 * 
	 * @param propertiesFile
	 * @throws Exception
	 */
	private void createManagedClient(String propertiesFile) throws Exception {
		/**
		 * Load device properties
		 */
		Properties deviceProps = loadPropertiesFile(propertiesFile);
		
		/**
		 * To create a DeviceData object, we will need the following objects:
		 *   - DeviceInfo
		 *   - DeviceLocation (optional)
		 *   - DeviceDiagnostic (optional)
		 *   - DeviceFirmware (optional)
		 */
		DeviceInfo deviceInfo = new DeviceInfo.Builder().
				serialNumber(deviceProps.getProperty("DeviceInfo.serialNumber")).
				manufacturer(deviceProps.getProperty("DeviceInfo.manufacturer")).
				model(deviceProps.getProperty("DeviceInfo.model")).
				deviceClass(deviceProps.getProperty("DeviceInfo.deviceClass")).
				description(deviceProps.getProperty("DeviceInfo.description")).
				fwVersion(deviceProps.getProperty("DeviceInfo.swVersion")).
				hwVersion(deviceProps.getProperty("DeviceInfo.hwVersion")).
				descriptiveLocation(deviceProps.getProperty("DeviceInfo.descriptiveLocation")).
				build();
		
		DeviceFirmware firmware = new DeviceFirmware.Builder().
				version(deviceProps.getProperty("DeviceFirmware.version")).
				name(deviceProps.getProperty("DeviceFirmware.name")).
				url(deviceProps.getProperty("DeviceFirmware.url")).
				verifier(deviceProps.getProperty("DeviceFirmware.verifier")).
				state(FirmwareState.IDLE).				
				build();
		
		/**
		 * Create a DeviceLocation object
		 */
		DeviceLocation location = new DeviceLocation.Builder(30.28565, -97.73921).
												elevation(10).build();
		
		/**
		 * Create a DeviceDiagnostic Object With default ErrorCode & Log
		 */
		
		DiagnosticErrorCode errorCode = new DiagnosticErrorCode(0);
		
		DiagnosticLog log = new DiagnosticLog(
				"Creating a Managed Client", 
				new Date(),
				DiagnosticLog.LogSeverity.informational);
		
		DeviceDiagnostic diag = new DeviceDiagnostic(errorCode, log);
		
		this.deviceData = new DeviceData.Builder().
						 deviceInfo(deviceInfo).
						 deviceFirmware(firmware).
						 deviceLocation(location).
						 deviceDiag(diag).
						 metadata(new JsonObject()).
						 build();
		
		// Options to connect to IoT Foundation
		Properties options = new Properties();
		options.setProperty("org", deviceProps.getProperty("org"));
		options.setProperty("type", deviceProps.getProperty("type"));
		options.setProperty("id", deviceProps.getProperty("id"));
		options.setProperty("auth-method", deviceProps.getProperty("auth-method"));
		options.setProperty("auth-token", deviceProps.getProperty("auth-token"));
				
		dmClient = new ManagedClient(options, deviceData);
	}
	
	/**
	 * This method connects the device to the IoT Foundation and sends
	 * a manage request, so that this device becomes a managed device.
	 * 
	 * Use the overloaded connect method that takes the lifetime parameter
	 */
	private void connect() throws Exception {
		dmClient.connect();
	}
	
	/**
	 * 
	 * Timer task that sends the manage command before the lifetime
	 * expires, otherwise the device will become dormant and can't 
	 * participate in device management actions
	 *
	 */
	private class ManageTask implements Runnable {
		private int lifetime;
		
		private ManageTask(int lifetime) {
			this.lifetime = lifetime;
		}
		
		@Override
		public void run() {
			try {
				boolean status = sendManageRequest(this.lifetime);
				System.out.println("Resent the manage request at time "+new Date() +
						" status("+status+")");
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private boolean sendManageRequest(int lifetime) throws MqttException {
		if(this.manageTask != null) {
			manageTask.cancel(false);
		}

		if(lifetime > 0) {
			ManageTask task = this.new ManageTask(lifetime);
			int twoMinutes =  60 * 2;
			scheduledThreadPool.scheduleAtFixedRate(task, 
											(lifetime - twoMinutes), 
											(lifetime - twoMinutes),
											TimeUnit.SECONDS);
		}
		if (dmClient.manage(lifetime)) {
			return true;
		} else {
			System.err.println("Managed request failed!");
		}
		return false;
	}

	
	/**
	 * 
	 * Timer task that updates the device location to IoT Foundation 
	 */
	private class LocationUpdateTask extends TimerTask {
		
		private DeviceLocation deviceLocation;
		private Random random = new Random();
		
		private LocationUpdateTask(DeviceLocation location) {
			this.deviceLocation = location;
		}
		
		@Override
		public void run() {
			// ...update location
			int rc = deviceLocation.update(random.nextDouble() + 30,   // latitude
								  random.nextDouble() - 98,	  // longitude
								  (double)random.nextInt(100));		  // elevation
			if(rc == 200) {
				System.out.println("Current location (" + deviceLocation.toString() + ")");
			} else {
				System.err.println("Failed to update the location");
			}
		}
		
	}
	
	/**
	 * Timer task that appends/clears Error code to IoT Foundation
	 * 
	 *  Clears the error at every 25th iteration
	 */
	private class DiagnosticErrorCodeUpdateTask extends TimerTask {
		
		private DeviceDiagnostic diag;
		private Random random = new Random();
		private int count = 0;
		
		private DiagnosticErrorCodeUpdateTask(DeviceDiagnostic diag) {
			this.diag = diag;
		}
		
		@Override
		public void run() {
			
			int rc = diag.append(random.nextInt(500));
			if(rc == 200) {
				System.out.println("Current Errorcode (" + diag.getErrorCode() + ")");
			} else {
				System.out.println("Errorcode addition failed");
			}
			
			if(count++ == 25) {
				rc = diag.clearErrorCode();
				if(rc == 200) {
					System.out.println("ErrorCodes are cleared successfully");
				} else {
					System.out.println("Failed to clear the ErrorCodes");
				}
				
				this.count = 0;
			}
		}
		
	}
	
	/**
	 * Timer task that appends/clears Log information to IoT Foundation
	 * 
	 *  Clears the error at every 25th iteration
	 */
	private class DiagnosticLogUpdateTask extends TimerTask {
		
		private DeviceDiagnostic diag;
		private int count = 0;
		
		private DiagnosticLogUpdateTask(DeviceDiagnostic diag) {
			this.diag = diag;
		}
		
		@Override
		public void run() {
			int rc = diag.append("Log event " + count++, new Date(), 
					DiagnosticLog.LogSeverity.informational);
				
			if(rc == 200) {
				System.out.println("Current Log (" + diag.getLog() + ")");
			} else {
				System.out.println("Log Addition failed");
			}
				
			if(count == 25) {
				rc = diag.clearLog();
				if(rc == 200) {
					System.out.println("Logs are cleared successfully");
				} else {
					System.out.println("Failed to clear the Logs");
				}	
			}
		}
	}
	
	
	private void terminate() throws Exception {
		scheduledThreadPool.shutdown();
		dmClient.disconnect();
		System.exit(-1);
	}
	
	/**
	 * This method does two things.
	 * 
	 * 1. Informs the Device management server that this device supports Firmware actions
	 * 
	 * 2. Adds a Firmware handler where the device agent will get notified
	 *    when there is a firmware action from the server. 
	 */
	private void addFirmwareHandler() throws Exception {
		if(this.dmClient != null) {
			RasPiFirmwareHandlerSample fwHandler = new RasPiFirmwareHandlerSample();
			deviceData.addFirmwareHandler(fwHandler);
			dmClient.supportsFirmwareActions(true);
			
			// Need to send another manage request as we need to
			// inform IoTF that this device supports firmware actions now
			this.sendManageRequest(0);
			
			System.out.println("Added Firmware Handler successfully !!");
		}
	}
	
	/**
	 * This method does two things.
	 * 
	 * 1. Informs the Device management server that this device supports Firmware actions
	 * 
	 * 2. Adds a Firmware handler where the device agent will get notified
	 *    when there is a firmware action from the server. 
	 */
	private void addDeviceActionHandler() throws Exception {
		if(this.dmClient != null) {
			DeviceActionHandlerSample actionHandler = new DeviceActionHandlerSample();
			deviceData.addDeviceActionHandler(actionHandler);
			dmClient.supportsDeviceActions(true);
			
			// Need to send another manage request as we need to
			// inform IoTF that this device supports device action now
			this.sendManageRequest(0);
			System.out.println("Added Device Action Handler successfully !!");
		}
	}
	
	
	/**
	 * This method publishes a sample device event
	 * 
	 *  This sample shows that one can publish events while carrying out
	 *  the device management operations.
	 */
	private class PublishDeviceEventTask implements Runnable {
		Random random = new Random();
		public void run() {
			//Generate a JSON object of the event to be published
			JsonObject event = new JsonObject();
			event.addProperty("name", "foo");
			event.addProperty("cpu",  random.nextInt(100));
			event.addProperty("mem",  random.nextInt(100));
			
			System.out.println("Publishing device event:: "+event);
			//Registered flow allows 0, 1 and 2 QoS	
			dmClient.publishEvent("status", event);
		}
	}
	
	private void sendUnManageRequest() throws MqttException {
		dmClient.unmanage();
		
		System.out.println("Stopping Tasks !!");
		if(null != this.manageTask) {
			this.manageTask.cancel(false);
			manageTask = null;
		}
		
		if(this.locationTask != null) {
			this.locationTask.cancel(false);
			locationTask = null;
		}
		 
		if(this.errorcodeTask != null) {
			this.errorcodeTask.cancel(false);
			this.errorcodeTask = null;
		}
		
		if(logTask != null) {
			this.logTask.cancel(false);
			this.logTask = null;
		}
	}

	private static Properties loadPropertiesFile(String propertiesFilePath) {
		File propertiesFile = new File(propertiesFilePath);
		Properties clientProperties = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(propertiesFile);
			clientProperties.load(in);
			in.close();
		} catch (FileNotFoundException e) {
		
			InputStream stream =
					SampleRasPiDMAgent.class.getClass().getResourceAsStream(PROPERTIES_FILE_NAME);
			try {
				clientProperties.load(stream);
			} catch (IOException e1) {
				System.err.println("Could not find file "+ PROPERTIES_FILE_NAME+
						" Please run the application with file specified as an argument");
				System.exit(-1);
			}
			    
			return clientProperties;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not find file "+ PROPERTIES_FILE_NAME+
					" Please run the application with file specified as an argument");
			System.exit(-1);
		}
		return clientProperties;
	}
	
	private static String getDefaultFilePath() {
		System.out.println("Trying to look for the default properties file :: " + PROPERTIES_FILE_NAME);
		
		// look for the file in current directory
		File f = new File(DEFAULT_PATH + File.separatorChar + PROPERTIES_FILE_NAME);
		if(f.isFile()) {
			System.out.println("Found one in - "+ f.getAbsolutePath());
			return f.getAbsolutePath();
		}
		// Check whether its present in the bin folder
		f = new File("bin" + File.separatorChar + PROPERTIES_FILE_NAME);
		if(f.isFile()) {
			System.out.println("Found one in - "+ f.getAbsolutePath());
			return f.getAbsolutePath();
		} 
		System.out.println("Not found - try to load it using the classpath");
		return PROPERTIES_FILE_NAME;

	}
	
	private void userAction() {
    	Scanner in = new Scanner(System.in);
    	
    	System.out.println("List of available commands");
		System.out.println(" manage [lifetime in seconds] :: Request to make the device as Managed device in IoTF");
		System.out.println(" unmanage :: Request to make the device unmanaged ");
		System.out.println(" firmware :: Adds a Firmware Handler that listens for the firmware actions from IoTF");
		System.out.println(" reboot :: Adds a Device action Handler that listens for reboot from IoTF");
		System.out.println(" location :: Starts a task that updates a random location at every 30 seconds");
		System.out.println(" errorcode :: Starts a task that appends/clears a ErrorCode at every 30 seconds");
		System.out.println(" log :: Starts a task that appends/clears a Log message at every 30 seconds");
		System.out.println(" quit :: quit this program)");
		
    	while(true) {
    		try {
	    		System.out.println("Enter the command ");	
	            String input = in.nextLine();
	            
	            String[] parameters = input.split(" ");
	            
	            switch(parameters[0]) {
	            
	            	case "manage":
	            		boolean status = false;
	            		if(parameters.length == 2) {
	            			int lifetime = 0;
	            			try {
	            				lifetime = Integer.parseInt(parameters[1]);
	            				// The minimum lifetime should be 1 hour
	            				if(lifetime != 0 && lifetime < 3600) {
	            					System.err.println("Lifetime "+lifetime + " is less than minimum "
	            							+ "value (1 hour), so setting it to 1 hour");
	            					lifetime = 3600;
	            				}
	
	            			} catch(Exception e) {
	            				System.err.println("lifetime should be an integer");
	            				continue;
	            			}
	            			status = this.sendManageRequest(lifetime);
	            		} else {
	            			status = this.sendManageRequest(0);
	            		}
	            		if(status) {
	            			System.out.println("Device is connected as Managed device now !!");
	            		} 
	            		break;
	            		
	            	case "unmanage":
	            		this.sendUnManageRequest();
	            		break;
	            		
	            	case "firmware":
	            		this.addFirmwareHandler();;
	            		break;
	
	            	case "reboot":
	            		this.addDeviceActionHandler();
	            		break;
	
	            	case "location":
	            		this.scheduleLocationTask();
	            		break;
	
	            	case "errorcode":
	            		this.scheduleErrorCodeTask();
	            		break;
	            		
	            	case "log":
	            		this.scheduleLogTask();
	            		break;
	            		
	            	case "quit":
	            		this.terminate();
	            		break;
	
	            	default:
	            		System.out.println("Unknown command received :: "+input);
	            		System.out.println(" manage [lifetime in seconds] :: Request to make the device as Managed device in IoTF");
	            		System.out.println(" unmanage :: Request to make the device unmanaged ");
	            		System.out.println(" firmware :: Adds a Firmware Handler that listens for the firmware actions from IoTF)");
	            		System.out.println(" reboot :: Adds a Device action Handler that listens for reboot from IoTF");
	            		System.out.println(" location :: Starts a task that updates a random location at every 30 seconds");
	            		System.out.println(" errorcode :: Starts a task that appends/clears a ErrorCode at every 30 seconds");
	            		System.out.println(" log :: Starts a task that appends/clears a Log message at every 30 seconds");
	            		System.out.println(" quit :: quit this program");
	            }
    		} catch(Exception e) {
    			System.out.println("Operation failed with exception "+e.getMessage());
    			continue;
    		}
    	}
    	
    }

	
	
}
