package com.ibm.iotf.client.application.api;

/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jose Paul - Initial Contribution
 *****************************************************************************
 */

import java.io.IOException;
import java.util.Properties;

import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.device.DeviceClient;


public class Connection {
	
	private final static String DEVICE_PROPERTIES_FILE = "/device.properties";
	private final static String APPLICATION_PROPERTIES_FILE = "/application.properties";
	
    

	public Connection() {
		// TODO Auto-generated constructor stub
	}
	public static  ApplicationClient getApplicationClient(){
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(Connection.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		ApplicationClient applicationClient = null;
		try {
			//Instantiate the class by passing the properties file
			applicationClient = new ApplicationClient(props);
			applicationClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return applicationClient;
	}

	public static DeviceClient getDeviceClient(){
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(Connection.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		
		
		DeviceClient deviceClient = null;
		try {
			//Instantiate the class by passing the properties file			
			deviceClient = new DeviceClient(props);
			deviceClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return deviceClient ;
	}
	public static  APIClient getApiClient(){
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(Connection.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		APIClient apiClient = null;
		try {
			//Instantiate the class by passing the properties file
			apiClient = new APIClient(props);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return apiClient;
	}
	
	
}
