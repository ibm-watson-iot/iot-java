/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 Prasanna Alur Mathada - Initial Contribution
 *****************************************************************************
 *
 */

package com.ibm.iotf.sample.devicemgmt.device;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.DeviceMetadata;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.sample.devicemgmt.device.SystemObject;


/**
 * A sample device management agent code that shows the following core DM capabilities,
 * 
 * 1. Firmware update
 * 2. Firmware Roll-back 
 * 3. Factory Reset 
 * 
 * This sample takes a properties file 'DMDeviceSample.properties' where the device informations 
 * and Firmware information are present. There is a default properties file in the sample folder, 
 * this class takes the default properties file if one not specified by user.
 * 
 * Refer to this link https://docs.internetofthings.ibmcloud.com/reference/device_mgmt.html
 * for more information about IBM Watson IoT Platform's DM capabilities 
 */

public class DeviceFirmwareSample {
	private final static String PROPERTIES_FILE_NAME = "/DMDeviceSample.properties";
	private ManagedDevice dmClient;
	
	/**
	 * This method builds the device objects required to create the ManagedClient
	 * 
	 * @param propertiesFile
	 * @throws Exception
	 */
	private void createManagedClient(String propertiesFile) throws Exception {
		/**
		 * Load device properties
		 */
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(DeviceFirmwareSample.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		/**
		 * To create a DeviceData object, we will need the following objects:
		 *   - DeviceInfo
		 *   - DeviceMetadata 
		 *   - DeviceFirmware
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
		
		DeviceFirmware firmware = new DeviceFirmware.Builder().
				version(trimedValue(deviceProps.getProperty("DeviceFirmware.version"))).
				name(trimedValue(deviceProps.getProperty("DeviceFirmware.name"))).
				url(trimedValue(deviceProps.getProperty("DeviceFirmware.url"))).
				verifier(trimedValue(deviceProps.getProperty("DeviceFirmware.verifier"))).
				state(FirmwareState.IDLE).				
				build();
		
		/**
		 * Create a DeviceMetadata object
		 */
		JsonObject data = new JsonObject();
		data.addProperty("customField", "customValue");
		DeviceMetadata metadata = new DeviceMetadata(data);
		
		DeviceData deviceData = new DeviceData.Builder().
						 deviceInfo(deviceInfo).
						 deviceFirmware(firmware).
						 metadata(metadata).
						 build();
		
		// Options to connect to Watson IoT Platform
		Properties options = new Properties();
		
		options.setProperty("Organization-ID", trimedValue(deviceProps.getProperty("Organization-ID")));
		options.setProperty("Device-Type", trimedValue(deviceProps.getProperty("Device-Type")));
		options.setProperty("Device-ID", trimedValue(deviceProps.getProperty("Device-ID")));
		options.setProperty("Authentication-Method", trimedValue(deviceProps.getProperty("Authentication-Method")));
		options.setProperty("Authentication-Token", trimedValue(deviceProps.getProperty("Authentication-Token")));
				
		dmClient = new ManagedDevice(options, deviceData);
		
	}
	
	private void disconnect() {
		// Disconnect cleanly
		dmClient.disconnect();
	}
	
	private void connect() throws MqttException {
		dmClient.connect();
	}

	public static void main(String[] args) throws Exception {
		DeviceFirmwareSample sample = new DeviceFirmwareSample();
		try{
			sample.createManagedClient(PROPERTIES_FILE_NAME);
			sample.connect();
				
			// can also not be managed
			sample.dmClient.sendManageRequest(0, true, true);
			sample.createFirmwareHandlerBasedonUserSelection();

			/** 
			 *  A method that updates a random location
			 */
			sample.updateDeviceLocation();
				
				
			/**
			 * The sample publishes a blink event every 5 seconds, that has the CPU and memory utilization of 
			 * this sample Gateway process.
			 */
			SystemObject obj = new SystemObject();
			while (true) {
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
						
				sample.dmClient.publishEvent("blink", event);
				System.out.println("<-- Device event :: "+event);
				Thread.sleep(5000);
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
	 * Load the Device properties file and read the user input on the source of
	 * Firmware Upgrade. i.e whether the Firmware Upgrade needs to be initiated
	 * by the Device or through the Platform. And if the choice is Platform, then
	 * should it be with Background Download & Update option or not.
	 * 
	 * Here, the user input is captured against the property file parameter 'option'.
	 */
	private void createFirmwareHandlerBasedonUserSelection() {
		
		Properties props = new Properties();
		try {
			props.load(DeviceInitiatedHandlerSample.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
			
		String option = trimedValue(props.getProperty("option"));
		
		Handler handler = Handler.createHandler(option, dmClient);
		handler.prepare(PROPERTIES_FILE_NAME);
	}

	/**
	 * Update a random Device Location. In reality, you may get the location of the 
	 * device using the GPS and update it to Watson IoT Platform.
	 */
	private void updateDeviceLocation() {
		Random random = new Random();
		// ...update location
		double latitude = random.nextDouble() + 30;
		double longitude = random.nextDouble() - 98;
		double elevation = (double)random.nextInt(100);
		int rc = dmClient.updateLocation(latitude, longitude, elevation);
		if(rc == 200) {
			System.out.println("Updated random location (" + latitude + " " + longitude +" " + elevation + ") for Device");
		} else {
			System.err.println("Failed to update the location (" + 
					latitude + " " + longitude +" " + elevation + "), rc ="+rc);
		}
	}
	
	/**
	 * Method that helps trim the output of the Device Properties File
	 * @param value
	 * @return
	 */
	
	private String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}

}
