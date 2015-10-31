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

import java.util.Properties;

import com.google.gson.JsonElement;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.sample.util.Utility;

/**
 * This sample showcases various ReST operations that can be performed on IoT Foundation to
 * retrieve various data usage and service status.
 *
 */
public class SampleUsageManagementAPIOperations {
	
	private final static String PROPERTIES_FILE_NAME = "application.prop";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";
	
	private APIClient apiClient = null;
	
	SampleUsageManagementAPIOperations(String filePath) {
		
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
	
	public static void main(String[] args) throws IoTFCReSTException {
	
		String fileName = null;
		if (args.length == 1) {
			fileName = args[0];
		} else {
			fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);
		}
		
		SampleUsageManagementAPIOperations sample = new SampleUsageManagementAPIOperations(fileName);
		sample.getActiveDevices();
		sample.getHistoricalDataUsage();
		sample.getDataTraffic();
		
		sample.getServiceStatus();
	}


	/**
	 * Retrieve the number of active devices over a period of time, 
	 * this sample calls the APIClient and retrieves the value
	 * which accepts the following parameters.
	 * 
	 * <p>startDate Start date in one of the following formats: YYYY (last day of the year), 
	 * YYYY-MM (last day of the month), YYYY-MM-DD (specific day)</p>
	 * 
	 * <p> endDate End date in one of the following formats: YYYY (last day of the year), 
	 * YYYY-MM (last day of the month), YYYY-MM-DD (specific day)</p>
	 * 
	 * <p> detail Indicates whether a daily breakdown will be included in the resultset</p>
	 * 
	 * <p>Response - JSON response containing the active devices over a period of time</p>
	 *  
	 */
	private void getActiveDevices() throws IoTFCReSTException {
		try {
			String start = "2015-09-01";
			String end = "2015-10-01";
			JsonElement response = this.apiClient.getActiveDevices(start, end, true);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * Retrieve the amount of storage being used by historical event data, 
	 * this sample calls the APIClient and retrieves the value
	 * which accepts the following parameters.
	 * 
	 * <p>startDate Start date in one of the following formats: YYYY (last day of the year), 
	 * YYYY-MM (last day of the month), YYYY-MM-DD (specific day)</p>
	 * 
	 * <p> endDate End date in one of the following formats: YYYY (last day of the year), 
	 * YYYY-MM (last day of the month), YYYY-MM-DD (specific day)</p>
	 * 
	 * <p> detail Indicates whether a daily breakdown will be included in the resultset</p>
	 * 
	 * <p>Response - JSON response containing the active devices over a period of time</p>
	 *  
	 */
	private void getHistoricalDataUsage() throws IoTFCReSTException {
		try {
			String start = "2015-09-01";
			String end = "2015-10-01";
			JsonElement response = this.apiClient.getHistoricalDataUsage(start, end, false);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * Retrieve the amount of data used, this sample calls the APIClient and retrieves the value
	 * which accepts the following parameters.
	 * 
	 * <p>startDate Start date in one of the following formats: YYYY (last day of the year), 
	 * YYYY-MM (last day of the month), YYYY-MM-DD (specific day)</p>
	 * 
	 * <p> endDate End date in one of the following formats: YYYY (last day of the year), 
	 * YYYY-MM (last day of the month), YYYY-MM-DD (specific day)</p>
	 * 
	 * <p> detail Indicates whether a daily breakdown will be included in the resultset</p>
	 * 
	 * <p>Response - JSON response containing the active devices over a period of time</p>
	 *  
	 */
	private void getDataTraffic() throws IoTFCReSTException {
		try {
			String start = "2015-09-01";
			String end = "2015-10-01";
			JsonElement response = this.apiClient.getDataTraffic(start, end, false);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * Retrieve the status of services for an organization. This sample method calls the 
	 * APIClient and retrieves the status.
	 * 
	 * <p>JSON response containing the status of services for an organization.</p>
	 *  
	 * @throws IoTFCReSTException 
	 */
	private void getServiceStatus() throws IoTFCReSTException {
		try {
			JsonElement response = this.apiClient.getServiceStatus();
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
}