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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.DeviceLocation;
import com.ibm.iotf.devicemgmt.DeviceMetadata;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.devicemgmt.resource.Resource;

/**
 * A sample device code that listens for the update message from IBM IoT Foundation. 
 * The library code updates these attributes in the corresponding objects and 
 * notifies the sample code if interested,
 * 
 * The IBM Internet of Things Foundation can send the following update request to a device to update 
 * values of one or more device attributes. 
 * 
 * iotdm-1/device/update
 * 
 * Attributes that can be updated by this operation are location, metadata, device information and firmware.
 * 
 * The "value" is the new value of the device attribute. It is a complex field matching the device model. 
 * Only writeable fields should be updated as a result of this operation. Values can be updated in:
 * 
 * location
 * metadata
 * deviceInfo
 * mgmt.firmware
 * 
 * 
 * This sample shows how one can listen for the incoming update message from IBM IoT Foundation
 * 
 * This sample takes a properties file where the device informations and location
 * informations are present. There is a default properties file in the sample folder, this
 * class takes the default properties file if one not specified by user.
 */
public class DeviceAttributesUpdateListenerSample implements PropertyChangeListener {
	private final static String PROPERTIES_FILE_NAME = "DMDeviceSample.properties";
	private final static String DEFAULT_PATH = "samples/iotfmanagedclient/src";
	
	private DeviceData deviceData;
	private ManagedDevice dmClient;
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting DM Java Client sample Location Update test...");

		String fileName = null;
		if (args.length == 1) {
			fileName = args[0];
		} else {
			fileName = getDefaultFilePath();
		}

		DeviceAttributesUpdateListenerSample sample = new DeviceAttributesUpdateListenerSample();
		try {
			sample.createManagedClient(fileName);
			sample.connect();
			sample.sendManageRequest();
			// wait for 10 minutes
			try {
				Thread.sleep(1000 * 60 * 10);
			} catch(InterruptedException e) {}
			sample.disConnect();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println(" Exiting...");
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getNewValue() == null) {
			return;
		}
		Object value = (Object) evt.getNewValue();
		
		switch(evt.getPropertyName()) {
			case "metadata":
				DeviceMetadata metadata = (DeviceMetadata) value;
				System.out.println("Received an updated metadata -- "+ metadata);
				break;
			
			case "location":
				DeviceLocation location = (DeviceLocation) value;
				System.out.println("received an updated location -- "+ location);
				break;
			
			case "deviceInfo":
				DeviceInfo info = (DeviceInfo) value;
				System.out.println("received an updated device info -- "+ info);
				break;
				
			case "mgmt.firmware":
				DeviceFirmware firmware = (DeviceFirmware) value;
				System.out.println("received an updated device firmware -- "+ firmware);
				break;		
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
		 *   - DeviceInfo (mandatory)
		 *   - DeviceLocation (optional)
		 *   - DeviceDiagnostic (optional)
		 *   - DeviceFirmware (optional)
		 *   - DeviceMetadata (mandatory)
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
		
		this.deviceData = new DeviceData.Builder().
						 deviceInfo(deviceInfo).
						 deviceFirmware(firmware).
						 metadata(metadata).
						 build();
		
		DeviceLocation location = new DeviceLocation();
		// Add a listener for all possible attribute changes
		location.addPropertyChangeListener(this);
		firmware.addPropertyChangeListener(this);
		deviceInfo.addPropertyChangeListener(this);
		metadata.addPropertyChangeListener(this);
		
		// Options to connect to IoT Foundation
		Properties options = new Properties();
		options.setProperty("Organization-ID", trimedValue(deviceProps.getProperty("Organization-ID")));
		options.setProperty("Device-Type", trimedValue(deviceProps.getProperty("Device-Type")));
		options.setProperty("Device-ID", trimedValue(deviceProps.getProperty("Device-ID")));
		options.setProperty("Authentication-Method", trimedValue(deviceProps.getProperty("Authentication-Method")));
		options.setProperty("Authentication-Token", trimedValue(deviceProps.getProperty("Authentication-Token")));
		

		dmClient = new ManagedDevice(options, deviceData);
	}
	
	/**
	 * This method connects the device to the IoT Foundation so that
	 * we can do one or more Device Management activities 
	 */
	private void connect() throws Exception {		
		dmClient.connect();
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
	 * @throws MqttException
	 */
	private void sendManageRequest() throws MqttException {
		if (dmClient.sendManageRequest(0, true, true)) {
			System.out.println("Device connected as Managed device now!");
		} else {
			System.err.println("Managed request failed!");
		}
	}
	
	private void disConnect() throws Exception {
		dmClient.disconnect();
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
