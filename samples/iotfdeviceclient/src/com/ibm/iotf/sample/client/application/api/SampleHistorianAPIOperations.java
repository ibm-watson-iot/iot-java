/**
 *****************************************************************************
 * Copyright (c) 2015 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */
package com.ibm.iotf.sample.client.application.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.sample.util.Utility;

/**
 * This sample showcases various ReST operations that can be performed on IoT Foundation to
 * retrieve the historical events.
 */

public class SampleHistorianAPIOperations {
	
	private final static String PROPERTIES_FILE_NAME = "application.prop";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";
	
	private static final String DEVICE_TYPE = "ManagedDT";
	private static final String DEVICE_ID = "RasPi01";
	
	private APIClient apiClient = null;
	
	SampleHistorianAPIOperations(String filePath) {
		
		/**
		 * Load properties file "application.prop"
		 */
		Properties props = Utility.loadPropertiesFile(PROPERTIES_FILE_NAME, filePath);
		try {
			//Instantiate the class by passing the properties file
			this.apiClient = new APIClient(props);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) throws Exception {
	
		String fileName = null;
		if (args.length == 1) {
			fileName = args[0];
		} else {
			fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);
		}
		
		// Make sure that the device is added to IBM IoT Foundation already
		SampleHistorianAPIOperations sample = new SampleHistorianAPIOperations(fileName);
		
		sample.getAllHistoricalEventsByDeviceType();
		sample.getAllHistoricalEventsByDeviceID();
		sample.getAllHistoricalEventsWithSampleQueryParameter();
	}

	/**
	 * This sample method retrieves historical events across all devices registered 
	 * in the organization with the supplied query parameters.
	 */
	private void getAllHistoricalEventsWithSampleQueryParameter() {
		// Get the historical events 
		try {
			ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("evt_type", "blink"));
			parameters.add(new BasicNameValuePair("start", "1445420849839"));
			JsonElement response = this.apiClient.getHistoricalEvents(parameters);

			// The response will contain more parameters that will be used to issue
			// the next request. The events element will contain the current list of devices
			JsonArray events = response.getAsJsonObject().get("events").getAsJsonArray(); 
			for(Iterator<JsonElement> iterator = events.iterator(); iterator.hasNext(); ) {
				JsonElement event = iterator.next();
				JsonObject responseJson = event.getAsJsonObject();
				System.out.println(responseJson);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method retrieves events across all devices of a particular device type but with a
	 * list of query parameters,
	 */
	private void getAllHistoricalEventsByDeviceType() {
		// Get the historical events 
		try {
			ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("evt_type", "blink"));
			parameters.add(new BasicNameValuePair("summarize", "{cpu,mem}"));
			parameters.add(new BasicNameValuePair("summarize_type", "avg"));
			
			JsonElement response = this.apiClient.getHistoricalEvents(DEVICE_TYPE, parameters);
			for(Iterator<JsonElement> iterator = response.getAsJsonArray().iterator(); iterator.hasNext(); ) {
				JsonElement event = iterator.next();
				JsonObject responseJson = event.getAsJsonObject();
				System.out.println(responseJson);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This sample retrieves events based on the device ID and with supplied query parameters.
	 */
	private void getAllHistoricalEventsByDeviceID() {
		// Get the historical events 
		try {
			ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("evt_type", "blink"));
			parameters.add(new BasicNameValuePair("summarize", "{cpu,mem}"));
			parameters.add(new BasicNameValuePair("summarize_type", "avg"));
			
			JsonElement response = this.apiClient.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID, parameters);
			for(Iterator<JsonElement> iterator = response.getAsJsonArray().iterator(); iterator.hasNext(); ) {
				JsonElement event = iterator.next();
				JsonObject responseJson = event.getAsJsonObject();
				System.out.println(responseJson);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}