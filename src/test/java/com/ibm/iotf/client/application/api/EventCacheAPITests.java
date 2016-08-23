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
import junit.framework.TestCase;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.device.DeviceClient;


/**
 * This test verifies various Event Cache API operations that can be performed on Watson IoT Platform.
 *
 */
public class EventCacheAPITests extends TestCase{
	
	
	
    private static boolean setUpIsDone = false;	
	private static APIClient apiClient = null;
	
	
	public synchronized void setUp() {
	    if (setUpIsDone) {
	        return;
	    }
	    
	    apiClient =Connection.getApiClient();
		
	    setUpIsDone = true;
	}
	/**
	 * This test verifies whether "getLastEvent" method call returns last event published by Device 
	 */
	public void testGetLastEvent() throws IoTFCReSTException {
		String eventID="TestCacheEvent";
		DeviceClient deviceClient = Connection.getDeviceClient();
		
		if(apiClient == null || deviceClient==null) {
			return;
		}
		
		
		JsonObject event = new JsonObject();
		SystemObject obj = new SystemObject();
		event.addProperty("name", SystemObject.getName());
		event.addProperty("cpu", "33");
		event.addProperty("mem", obj.getMemoryUsed());
		deviceClient.publishEvent( "TestCacheEvent",event);
		
		boolean sucess = false;
		JsonElement element = apiClient.getLastEvent(
				trimedValue(deviceClient.getDeviceType()),
				trimedValue(deviceClient.getDeviceId()),eventID);
		JsonObject jsonObject =element.getAsJsonObject();
		String tempEventID= jsonObject.get("eventId").getAsString();
			if (tempEventID.equals(eventID))
				sucess = true;
	
		deviceClient.disconnect();
		assertTrue("Expected device events are  not present for the device",sucess);
		

	}
	/**
	 * This test verifies whether "getLastEvents" method call returns last events published by Device 
	 */
	public void testGetLastEvents() throws IoTFCReSTException {
		String eventID="TestCacheEvent";
		DeviceClient deviceClient = Connection.getDeviceClient();
		if(apiClient == null || deviceClient==null) {
			return;
		}
		
		JsonObject event = new JsonObject();
		SystemObject obj = new SystemObject();
		event.addProperty("name", SystemObject.getName());
		event.addProperty("cpu", "33");
		event.addProperty("mem", obj.getMemoryUsed());
		deviceClient.publishEvent( "TestCacheEvent",event);
		
		boolean sucess = false;
		JsonElement element = apiClient.getLastEvents(
				trimedValue(deviceClient.getDeviceType()),
				trimedValue(deviceClient.getDeviceId()));
		JsonArray jsonArray = element.getAsJsonArray();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonObject = (JsonObject) jsonArray.get(i);
			String tempEventID = jsonObject.get("eventId").getAsString().trim();
			if (tempEventID.equals(eventID))
				sucess = true;
		}
		deviceClient.disconnect();
		assertTrue("Expected device events are  not present for the device",sucess);
	}
	
	public void testVerifyExceptionMessage() throws IoTFCReSTException {

		String deviceType = "sample";
		String deviceId = "myid";
		String eventId = "my^%^evt";
		try {
			JsonElement response = apiClient.getLastEvent(deviceType, deviceId, eventId);
			System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(response));
		} catch (IoTFCReSTException e) {
			assertTrue("Expected exception message is not thrown", e.getMessage().contains("Illegal character"));
			return;
		}
		fail("Must trhow an exception");
	}
	private static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}
}
