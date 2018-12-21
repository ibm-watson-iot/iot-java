package com.ibm.iotf.client.application.api;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.util.LoggerUtility;


/**
 * This test verifies various Event Cache API operations that can be performed on Watson IoT Platform.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventCacheAPITests {
	
	private static final String CLASS_NAME = EventCacheAPITests.class.getName();
	private static final String APP_ID = "LECApp1";
	private static final String DEVICE_TYPE = "LECType1";
	private static final String DEVICE_ID = "LECDev1";
	
	private static APIClient apiClient = null;
	private static DeviceClient devClient = null;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, DEVICE_ID);
		apiClient = new APIClient(appProps);

		// Delete device if it was left from the last test run
		if (apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
		}
		
		// If the device type does not exist, create it
		if (apiClient.isDeviceTypeExist(DEVICE_TYPE) == false) {
			apiClient.addDeviceType(DEVICE_TYPE, null, null, null);
		}
		
		// Register the test device DEVICE_ID
		apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID, TestEnv.getDeviceToken(), null, null, null);

		Properties devProps = TestEnv.getDeviceProperties(DEVICE_TYPE, DEVICE_ID);
		
		devClient = new DeviceClient(devProps);
	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {
		
		if (apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
		}
		
		if (apiClient.isDeviceTypeExist(DEVICE_TYPE)) {
			apiClient.deleteDeviceType(DEVICE_TYPE);
		}
	}	

	/**
	 * This test verifies whether "getLastEvent" method call returns last event published by Device 
	 * @throws MqttException 
	 */
	@Test
	public void test01GetLastEvent() throws IoTFCReSTException, MqttException {
		final String METHOD = "testGetLastEvent";
		String eventID = METHOD;
		
		JsonObject event = new JsonObject();
		SystemObject obj = new SystemObject();
		event.addProperty("name", SystemObject.getName());
		event.addProperty("cpu", "33");
		event.addProperty("mem", obj.getMemoryUsed());
		
		devClient.connect();
		devClient.publishEvent( eventID, event);
		
		LoggerUtility.info(CLASS_NAME, METHOD,
				"Get event ID " + eventID);
		
		boolean sucess = false;
		JsonElement element = apiClient.getLastEvent(
				devClient.getDeviceType(),
				devClient.getDeviceId(),
				eventID);
		
		JsonObject jsonObject = element.getAsJsonObject();
		
		String tempEventID = jsonObject.get("eventId").getAsString();
		if (tempEventID.equals(eventID))
			sucess = true;
	
		devClient.disconnect();
		assertTrue("Expected device events are  not present for the device", sucess);

	}
	/**
	 * This test verifies whether "getLastEvents" method call returns last events published by Device 
	 * @throws MqttException 
	 */
	@Test
	public void test02GetLastEvents() throws IoTFCReSTException, MqttException {
		
		final String METHOD = "testGetLastEvents";
		int numEvents = 3;
		String eventPrefix = METHOD;
		ArrayList<String> events = new ArrayList<String>();

		for (int i=0; i<numEvents; i++) {
			JsonObject event = new JsonObject();
			SystemObject obj = new SystemObject();
			event.addProperty("name", SystemObject.getName());
			event.addProperty("cpu", "33");
			event.addProperty("mem", obj.getMemoryUsed());
			devClient.connect();
			String eventID = new String(eventPrefix + i);
			devClient.publishEvent(eventID, event);
			events.add(eventID);
		}
		
		int returnEvents = 0;
		JsonElement element = apiClient.getLastEvents(devClient.getDeviceType(), 
				devClient.getDeviceId());
		JsonArray jsonArray = element.getAsJsonArray();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonObject = (JsonObject) jsonArray.get(i);
			String eventID = jsonObject.get("eventId").getAsString().trim();
			if (events.contains(eventID)) {
				LoggerUtility.info(CLASS_NAME, METHOD, "Found event " + eventID);
				returnEvents++;
			}
		}
		
		devClient.disconnect();
		assertTrue("Expected device events for the device", numEvents == returnEvents);
	}
	
	@Test
	public void test03VerifyExceptionMessage() throws IoTFCReSTException {
		final String METHOD = "testVerifyExceptionMessage";

		String deviceType = "sample";
		String deviceId = "myid";
		String eventId = "my^%^evt";
		try {
			JsonElement response = apiClient.getLastEvent(deviceType, deviceId, eventId);
			LoggerUtility.severe(CLASS_NAME, METHOD,
					"Should not get this reponse: " + response.getAsJsonObject().toString());
		} catch (IoTFCReSTException e) {
			assertTrue("Expected exception message is not thrown", e.getMessage().contains("Illegal character"));
			return;
		}
		fail("IoTFCReSTException was expected");
	}
}
