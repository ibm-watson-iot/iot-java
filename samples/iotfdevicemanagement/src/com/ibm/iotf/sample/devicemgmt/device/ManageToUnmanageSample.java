/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.sample.devicemgmt.device;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.device.DeviceData;
import com.ibm.iotf.devicemgmt.device.DeviceFirmware;
import com.ibm.iotf.devicemgmt.device.DeviceInfo;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.devicemgmt.device.DeviceFirmware.FirmwareState;

/**
 * A sample device code that shows how to make a device managed and then unmanaged
 *  
 * This sample takes a properties file where the device informations and Firmware
 * informations are present. There is a default properties file in the sample folder, this
 * class takes the default properties file if one not specified by user.
 */
public class ManageToUnmanageSample {
	private final static String PROPERTIES_FILE_NAME = "DMDeviceSample.properties";
	private final static String DEFAULT_PATH = "samples/iotfmanagedclient/src";
	
	private DeviceData deviceData;
	private ManagedDevice dmClient;
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting Device Management sample test...");
		String fileName = null;
		if (args.length == 1) {
			fileName = args[0];
		} else {
			fileName = getDefaultFilePath();
		}
		
		ManageToUnmanageSample sample = new ManageToUnmanageSample();
		try {
			sample.createManagedClient(fileName);
			sample.connect();
			sample.publishDeviceEvent();
			sample.sendUnManageRequest();
			sample.publishDeviceEvent();
			sample.sendManageRequest();
			sample.publishDeviceEvent();
			sample.disConnect();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println(" Exiting...");
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
		
		this.deviceData = new DeviceData.Builder().
						 deviceInfo(deviceInfo).
						 metadata(new JsonObject()).
						 build();
		
		// Options to connect to IoT Foundation
		Properties options = new Properties();
		options.setProperty("org", deviceProps.getProperty("org"));
		options.setProperty("type", deviceProps.getProperty("type"));
		options.setProperty("id", deviceProps.getProperty("id"));
		options.setProperty("auth-method", deviceProps.getProperty("auth-method"));
		options.setProperty("auth-token", deviceProps.getProperty("auth-token"));

		dmClient = new ManagedDevice(options, deviceData);
	}
	
	/**
	 * This method connects the device to the IoT Foundation and sends
	 * a manage request, so that this device becomes a managed device.
	 */
	private void connect() throws Exception {		
		dmClient.connect();
	}
	
	private void disConnect() throws Exception {
		dmClient.disconnect();
	}
	
	/**
	 * Send a device manage request to IoT Foundation
	 * 
	 * A device uses this request to become a managed device. 
	 * It should be the first device management request sent by the 
	 * device after connecting to the Internet of Things Foundation. 
	 * It would be usual for a device management agent to send this 
	 * whenever is starts or restarts.
	 * 
	 * @param lifetime The length of time in seconds within 
	 *        which the device must send another Manage device request 
	 * @return True if successful
	 * @throws MqttException
	 */
	private void sendManageRequest() throws MqttException {
		if (dmClient.manage(0)) {
			System.out.println("Device connected as Managed device now!");
		} else {
			System.err.println("Managed request failed!");
		}
	}
	
	private void sendUnManageRequest() throws MqttException {
		dmClient.unmanage();
	}
	
	/**
	 * This method publishes a sample device event 
	 */
	private void publishDeviceEvent() {
		
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  80);
		event.addProperty("mem",  90);
		
		System.out.println("Publishing device event:: "+event);
		//Registered flow allows 0, 1 and 2 QoS	
		dmClient.publishEvent("status", event);
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
		File f = new File(PROPERTIES_FILE_NAME);
		if(f.isFile()) {
			System.out.println("Found one in - "+ f.getAbsolutePath());
			return f.getAbsolutePath();
		}
		
		// look for the file in default path
		f = new File(DEFAULT_PATH + File.separatorChar + PROPERTIES_FILE_NAME);
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
}
