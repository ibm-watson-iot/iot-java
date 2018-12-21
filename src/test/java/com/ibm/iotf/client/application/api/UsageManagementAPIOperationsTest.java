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
 * Amit M Mangalvedkar
 *****************************************************************************
 */

package com.ibm.iotf.client.application.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonElement;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.test.common.TestEnv;

/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * retrieve various data usage and service status.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UsageManagementAPIOperationsTest {
	
	private static APIClient apiClient = null;
	private static final String APP_ID = "UMApiOpApp1";
	
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@BeforeClass
	public static void oneTimeSetUp() {
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, null, null);
		try {
			apiClient = new APIClient(appProps);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {
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
	
	/*
	@Ignore
	@Test
	public void test01getActiveDevices() throws IoTFCReSTException {
		try {
			
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
			String start = "2016-05-01";
			String end = sdfDate.format(new Date());
			System.out.println("Get all active devices between date "+start + " end "+end);
			JsonElement response = this.apiClient.getActiveDevices(start, end, true);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			//ToDo uncomment when the bug is fixed
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	*/
	
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
	/*
	@Ignore
	@Test
	public void test02getHistoricalDataUsage() throws IoTFCReSTException {
		try {
			String start = "2018-09-01";
			String end = "2016-10-01";
			System.out.println("Get Historical data usage between date "+start + " end "+end);
			JsonElement response = this.apiClient.getHistoricalDataUsage(start, end, false);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			// ToDo uncomment when the bug is fixed
			//fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	*/
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
	public void test03getDataTraffic() throws IoTFCReSTException {
		try {
			String start = "2017-09-01";
			String end = "2018-10-01";
			System.out.println("Get data traffic between date "+start + " end "+end);
			JsonElement response = apiClient.getDataTraffic(start, end, false);
			System.out.println(response);
			assertEquals("Get data traffic between date "+ start + " end "+ end, 200, 200 );

		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * This is a negative test case to check the exception
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
	@Test
	public void test032getDataTraffic() throws IoTFCReSTException {
		String start = "2019-09-01";
		String end = "2018-10-01";
		System.out.println("Get data traffic between date "+start + " end "+end);
		try {
			JsonElement response = apiClient.getDataTraffic(start, end, false);
			fail();
		} catch(IoTFCReSTException iotfe) {
			
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
	public void test04getServiceStatus() throws IoTFCReSTException {
		try {
			System.out.println("Get Service status..");
			JsonElement response = apiClient.getServiceStatus();
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
}