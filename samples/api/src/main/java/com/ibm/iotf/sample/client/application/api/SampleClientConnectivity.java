/**
 *****************************************************************************
 * Copyright (c) 2019 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.application.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;

/**
 * This sample showcases various client connectivity status operations that can be performed
 */
public class SampleClientConnectivity {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";

	private final static String clientIdToQuery = "client:id:here";
	
	private APIClient apiClient = null;
	
	SampleClientConnectivity(String filePath) {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(SampleBulkAPIOperations.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		try {
			//Instantiate the class by passing the properties file
			this.apiClient = new APIClient(props);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) throws Exception {
	
		SampleClientConnectivity sample = new SampleClientConnectivity(PROPERTIES_FILE_NAME);
		sample.getConnectionStates();
		sample.getConnectionState();
		sample.getConnectedConnectionStates();
		sample.getRecentConnectionStates();
		sample.getCustomConnectionState();
	}

	/**
	 * This sample showcases how to call the connection states api for all client ids, 25 at a time.
	 * 
	 * @throws Exception 
	 */
	private void getConnectionStates() throws IoTFCReSTException {
		System.out.println("Getting connection states");

		try {
			JsonObject responseJson = this.apiClient.getConnectionStates();
			System.out.println(responseJson);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to call the connection states api for a single client id
	 * 
	 * @throws Exception 
	 */
	private void getConnectionState() throws IoTFCReSTException {
		System.out.println("Getting connection state for a single client id " + clientIdToQuery);

		try {
			JsonObject responseJson = this.apiClient.getConnectionState(clientIdToQuery);
			System.out.println(responseJson);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to call the connection states api all connected clients
	 * 
	 * @throws Exception 
	 */
	private void getConnectedConnectionStates() throws IoTFCReSTException {
		System.out.println("Getting connected connection states");

		try {
			JsonObject responseJson = this.apiClient.getConnectedConnectionStates();
			System.out.println(responseJson);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to call the connection states api for clients connected within the last two days
	 * 
	 * @throws Exception 
	 */
	private void getRecentConnectionStates() throws IoTFCReSTException {
		System.out.println("Getting recent connection states");
		
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, -2);
		Date dateTwoDaysAgo = cal.getTime();
		
		String utcTime = DateFormatUtils.formatUTC(dateTwoDaysAgo, 
				DateFormatUtils.ISO_DATETIME_FORMAT.getPattern());

		try {
			JsonObject responseJson = this.apiClient.getActiveInRecentDaysConnectionStates(utcTime);
			System.out.println(responseJson);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to call a custom query for the connection state api
	 * defaults to {orgId}.internetofthings.ibmcloud.com/api/v0002/clientconnectionstates when empty
	 * 
	 * @throws Exception 
	 */
	private void getCustomConnectionState() throws IoTFCReSTException {
		
	    String query = "?connectionStatus=disconnected";

		try {
			JsonObject responseJson = this.apiClient.getCustomConnectionState(query);
			System.out.println(responseJson);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}

	
}