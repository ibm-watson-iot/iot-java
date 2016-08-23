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
package com.ibm.iotf.client.application.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;

/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * retrieve the historical events.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HistorianAPIOperationsTest extends TestCase {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private static final String DEVICE_TYPE = "TestDT";
	private static final String DEVICE_ID = "RasPi01";
	
	private static APIClient apiClient = null;
	private static APIClient apiClientWithWrongToken = null;
	private static APIClient apiClientWithWrongKey = null;
	private static APIClient apiClientWithWrongOrg= null;
	private static boolean setUpIsDone = false;
	
	public synchronized void setUp() {
	    if (setUpIsDone) {
	        return;
	    }
	    
	    /**
		  * Load device properties
		  */
		Properties props = new Properties();
		Properties propsWrongToken = new Properties();
		Properties propsWrongMethod = new Properties();
		Properties propsWrongOrg = new Properties();
		try {
			props.load(HistorianAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			propsWrongToken.load(HistorianAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			propsWrongMethod.load(HistorianAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			propsWrongOrg.load(HistorianAPIOperationsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}					
		try {
			//Instantiate the class by passing the properties file
			apiClient = new APIClient(props);
			
			propsWrongToken.setProperty("Authentication-Token", "Wrong");
			apiClientWithWrongToken = new APIClient(propsWrongToken);
			
			propsWrongMethod.setProperty("API-Key", "Wrong");
			apiClientWithWrongKey = new APIClient(propsWrongMethod);
			
			propsWrongOrg.setProperty("Organization-ID", "Wrong");
			apiClientWithWrongOrg = new APIClient(propsWrongOrg);
		} catch (Exception e) {
			// looks like the application.properties file is not updated properly
			apiClient = null;
		}
	    setUpIsDone = true;
	}
	
	/**
	 * This sample method retrieves historical events across all devices registered 
	 * in the organization with the supplied query parameters.
	 */
	public void test01getAllHistoricalEventsWithSampleQueryParameter() {
		System.out.println("Get all blink events from start date "+ new Date(1445420849839L));
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		// Get the historical events 
		try {
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
			fail(e.getMessage());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getHistoricalEvents(parameters);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.getHistoricalEvents(parameters);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.getHistoricalEvents(parameters);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}

	}
	
	/**
	 * This sample method retrieves historical events across all devices registered 
	 * in the organization.
	 */
	public void test02getAllHistoricalEvents() {
		// Get the historical events 
		try {
			JsonElement response = this.apiClient.getHistoricalEvents();

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
			fail(e.getMessage());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getHistoricalEvents();
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.getHistoricalEvents();
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.getHistoricalEvents();
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
	
	/**
	 * This method retrieves events across all devices of a particular device type but with a
	 * list of query parameters,
	 */
	public void test03getAllHistoricalEventsByDeviceType() {
		System.out.println("Get all blink events under device type "+ DEVICE_TYPE +" and summarize the datapoints cpu and mem ");
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		// Get the historical events 
		try {
			
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
			fail(e.getMessage());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getHistoricalEvents(DEVICE_TYPE, parameters);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Method
		try {
			apiClientWithWrongKey.getHistoricalEvents(DEVICE_TYPE, parameters);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrrong Org
		try {
			apiClientWithWrongOrg.getHistoricalEvents(DEVICE_TYPE, parameters);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
	
	/**
	 * This method retrieves events across all devices of a particular device type but without a
	 * list of query parameters,
	 */
	public void test04getAllHistoricalEventsByDeviceType() {
		System.out.println("Get all blink events under device type "+ DEVICE_TYPE +" and summarize the datapoints cpu and mem ");
		// Get the historical events 
		try {
			
			JsonElement response = this.apiClient.getHistoricalEvents(DEVICE_TYPE);
			System.out.println(response);
			for(Iterator<JsonElement> iterator = response.getAsJsonObject().get("events").getAsJsonArray().iterator(); iterator.hasNext(); ) {
				JsonElement event = iterator.next();
				JsonObject responseJson = event.getAsJsonObject();
				System.out.println(responseJson);
			}
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getHistoricalEvents(DEVICE_TYPE);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.getHistoricalEvents(DEVICE_TYPE);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.getHistoricalEvents(DEVICE_TYPE);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
	
	/**
	 * This sample retrieves events based on the device ID and with supplied query parameters.
	 */
	public void test05getAllHistoricalEventsByDeviceID() {
		System.out.println("Get all blink events under device "+ DEVICE_ID +" of Type "+ DEVICE_TYPE +" and summarize the datapoints cpu and mem ");
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		// Get the historical events 
		try {
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
			fail(e.getMessage());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID, parameters);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID, parameters);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID, parameters);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
	
	/**
	 * This sample retrieves events based on the device ID and without supplied query parameters.
	 */
	public void test06getAllHistoricalEventsByDeviceID() {
		System.out.println("Get all blink events under device "+ DEVICE_ID +" of Type "+ DEVICE_TYPE);
		// Get the historical events 
		try {
			JsonElement response = this.apiClient.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID);
			for(Iterator<JsonElement> iterator = response.getAsJsonObject().get("events").getAsJsonArray().iterator(); iterator.hasNext(); ) {
				JsonElement event = iterator.next();
				JsonObject responseJson = event.getAsJsonObject();
				System.out.println(responseJson);
			}
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID);
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
	
	/**
	 * This sample retrieves events based on the device ID and without supplied query parameters.
	 */
	public void test07getAllHistoricalEvents() {
		System.out.println("Get all blink events under device "+ DEVICE_ID +" of Type "+ DEVICE_TYPE);
		String endTime = Long.toString(new Date().getTime());
		// Get the historical events 
		try {
			JsonElement response = this.apiClient.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID, null, 
					"blink", "1445420849839", endTime, 100, "{cpu,mem}", "avg");
			for(Iterator<JsonElement> iterator = response.getAsJsonArray().iterator(); iterator.hasNext(); ) {
				JsonElement event = iterator.next();
				JsonObject responseJson = event.getAsJsonObject();
				System.out.println(responseJson);
			}
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// negative test, it should fail
		try {
			apiClientWithWrongToken.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID, null, 
					null, null, null, 100, "{cpu,mem}", "avg");
			fail("Doesn't throw invild Auth token exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrong Method
		try {
			apiClientWithWrongKey.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID, null, 
					null, null, null, 100, "{cpu,mem}", "avg");
			fail("Doesn't throw invild API Key exception");
		} catch(IoTFCReSTException e) {	}
		
		// Wrong Org
		try {
			apiClientWithWrongOrg.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID, null, 
					null, null, null, 100, "{cpu,mem}", "avg");
			fail("Doesn't throw invild ORG exception");
		} catch(IoTFCReSTException e) {	}
	}
}