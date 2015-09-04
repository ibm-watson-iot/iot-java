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
package com.ibm.iotf.sample.devicemgmt.client.device;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Random;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.client.device.DeviceLocation;
import com.ibm.iotf.devicemgmt.client.device.ManagedClient;
import com.ibm.iotf.devicemgmt.client.device.DeviceData;
import com.ibm.iotf.devicemgmt.client.device.DeviceInfo;

/**
 * A sample device code that updates the location for every 5 seconds to IoT Foundation
 * while sending device events.
 * 
 * This sample takes a properties file where the device informations and location
 * informations are present. There is a default properties file in the sample folder, this
 * class takes the default properties file if one not specified by user.
 */
public class LocationUpdateSample {
	private final static String PROPERTIES_FILE_NAME = "DMDeviceSample.properties";
	private final static String DEFAULT_PATH = "samples/iotfmanagedclient/src";
	
	private DeviceData deviceData;
	private ManagedClient dmClient;
	private LocUpdater locUpdaterThread;
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting DM Java Client sample Location Update test...");

		String fileName = null;
		if (args.length == 1) {
			fileName = args[0];
		} else {
			fileName = getDefaultFilePath();
		}

		LocationUpdateSample sample = new LocationUpdateSample();
		try {
			sample.createManagedClient(fileName);
			sample.connect();
			sample.startLocationUpdaterThread();
			sample.publishDeviceEvents();
			sample.disConnect();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println(" Exiting...");
	}
	
	private void startLocationUpdaterThread() {
		this.locUpdaterThread.start();
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

		/**
		 * Create a DeviceLocation object
		 */
		DeviceLocation location = new DeviceLocation.Builder(30.28565, -97.73921).
												elevation(10).build();
		
		this.deviceData = new DeviceData.Builder().
						 deviceInfo(deviceInfo).
						 deviceLocation(location).
						 metadata(new JsonObject()).
						 build();
		
		// create the location updater thread 
		this.locUpdaterThread = new LocUpdater();
		this.locUpdaterThread.deviceLocation = location;
		
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
	 */
	private void connect() throws Exception {		
		dmClient.connect();
	}
	
	private void disConnect() throws Exception {
		dmClient.disconnect();
	}
	
	
	/**
	 * Thread class that updates the device location to IoT Foundation 
	 * for every 5 seconds
	 */
	private static class LocUpdater extends Thread {
		private DeviceLocation deviceLocation;
		private Random random = new Random();
		
		public void run() {
			for (int i=0; i<1000; i++) {
				// ...update location
				int rc = deviceLocation.update(random.nextDouble() + 30,   // latitude
									  random.nextDouble() - 98,	  // longitude
									  (double)random.nextInt(100));		  // elevation
				if(rc == 200) {
					System.out.println("Current location (" + deviceLocation.toString() + ")");
				} else {
					System.err.println("Failed to update the location");
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * This method publishes a sample device event for every 5 seconds
	 * for 1000 times.
	 * 
	 *  This sample shows that one can publish events while carrying out
	 *  the device management operations.
	 */
	private void publishDeviceEvents() {
		
		Random random = new Random();
		//Lets publish an event for every 5 seconds for 1000 times
		for(int i = 0; i < 1000; i++) {
			//Generate a JSON object of the event to be published
			JsonObject event = new JsonObject();
			event.addProperty("name", "foo");
			event.addProperty("cpu",  random.nextInt(100));
			event.addProperty("mem",  random.nextInt(100));
		
			System.out.println("Publishing device event:: "+event);
			//Registered flow allows 0, 1 and 2 QoS	
			dmClient.publishEvent("status", event);
			
			try {
				Thread.sleep(5000 * 2);
			} catch(InterruptedException ie) {
				
			}
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
}
