package com.ibm.iotf.client.application.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestHelper;
import com.ibm.iotf.util.LoggerUtility;

/**
 * This test-case tests various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/delete device(s)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceAPIOperationsTest {
	
	private static final String CLASS_NAME = DeviceAPIOperationsTest.class.getName();
	
	private static final String APP_ID = "DevApiOpApp1";
	
	//private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private static final String DEVICE_TYPE = "DevApiOpType";
	private static final String DEVICE_ID = "DevApiOpId1";
	private static final String DEVICE_ID2 = "DevApiOpId2";
	private static final String MANAGED_DEV_ID = "manDev1";

	/**
	 * Split the elements into multiple lines, so that we can showcase the use of multiple constructors
	 */
	private final static String locationToBeAdded = "{\"longitude\": 0, \"latitude\": 0, \"elevation\": "
			+ "0,\"measuredDateTime\": \"2015-23-07T11:23:23+00:00\"}";
	
	private final static String newlocationToBeAdded = "{\"longitude\": 10, \"latitude\": 20, \"elevation\": 0}";
	
	
	private final static String deviceInfoToBeAdded = "{\"serialNumber\": "
			+ "\"10087\",\"manufacturer\": \"IBM\",\"model\": \"7865\",\"deviceClass\": "
			+ "\"A\",\"description\": \"My DEVICE_ID2 Device\",\"fwVersion\": \"1.0.0\","
			+ "\"hwVersion\": \"1.0\",\"descriptiveLocation\": \"EGL C\"}";
	
	private final static String deviceToBeAdded = "{\"deviceId\": "
			+ "\"" + DEVICE_ID + "\",\"authToken\": \"password\"," + 
			"\"location\": " + locationToBeAdded + "," 
			+ "\"deviceInfo\": " + deviceInfoToBeAdded + "," 
			+ "\"metadata\": {}}";

	private static APIClient apiClient = null;
	
	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, null);
		apiClient = new APIClient(appProps);
		
		boolean exist = false;
		
		try {
			exist = apiClient.isDeviceTypeExist(DEVICE_TYPE);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		
		if (!exist) {
			TestHelper.addDeviceType(apiClient, DEVICE_TYPE);
		}

		TestHelper.deleteDevice(apiClient, DEVICE_TYPE, DEVICE_ID);
		TestHelper.deleteDevice(apiClient, DEVICE_TYPE, DEVICE_ID2);
		TestHelper.deleteDevice(apiClient, DEVICE_TYPE, MANAGED_DEV_ID);

		
	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {	
		TestHelper.deleteDevice(apiClient, DEVICE_TYPE, DEVICE_ID);
		TestHelper.deleteDevice(apiClient, DEVICE_TYPE, DEVICE_ID2);
		TestHelper.deleteDevice(apiClient, DEVICE_TYPE, MANAGED_DEV_ID);
		apiClient.deleteDeviceType(DEVICE_TYPE);
	}
	
	/**
	 * This test-case tests how to add a device using the Java Client Library.
	 */
	@Test
	public void test01addDevice() {
		
		JsonParser parser = new JsonParser();
		JsonElement input = parser.parse(deviceToBeAdded);
		try {
			JsonObject response = apiClient.registerDevice(DEVICE_TYPE, input);
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID));
			assertFalse("Response must not be null", response.isJsonNull());
		} catch (IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
		try {
			
			// Lets add device with different API that accepts more args,
			
			JsonElement deviceInfo = parser.parse(deviceInfoToBeAdded);
			JsonElement location = parser.parse(locationToBeAdded);
			
			JsonObject response = apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID2, "Password", 
					deviceInfo, location, null);
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID2));
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			
		}
	}
	
	/**
	 * This test-case tests how to get device details using the Java Client Library.
	 */
	@Test
	public void test02getDevice() throws IoTFCReSTException {
		try {
			JsonObject response = apiClient.getDevice(DEVICE_TYPE, DEVICE_ID);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}
	
	/**
	 * This test-case tests how to get device details using the Java Client Library.
	 * 
	 * Negative test - Specify an invalid device type
	 * @throws IoTFCReSTException
	 */
	//FIXME
	public void test021getDevice() throws IoTFCReSTException {
		final String METHOD = "test021getDevice";
		try {
			JsonObject response = apiClient.getDevice("Non-Exist", DEVICE_ID);
			fail("Must throw an exception, but received a response: " + response.getAsString());
		} catch (IoTFCReSTException e) {
			LoggerUtility.info(CLASS_NAME, METHOD, DEVICE_TYPE + "HTTP Code " + e.getHttpCode() 
				+ " Response: " + e.getResponse() );
			assertTrue("HTTP error code must be 404", e.getHttpCode() == 404);
		}
	}
	
	/**
	 * This test-case tests how to update a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	@Test
	public void test03updateDeviceLocation() throws IoTFCReSTException {
		
		JsonElement newLocation = new JsonParser().parse(newlocationToBeAdded);
		try {
			JsonObject response = apiClient.updateDeviceLocation(DEVICE_TYPE, DEVICE_ID, newLocation);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * This test-case tests how to get a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	@Test
	public void test04getDeviceLocation() throws IoTFCReSTException {
		try {
			JsonObject response = apiClient.getDeviceLocation(DEVICE_TYPE, DEVICE_ID);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
	}
	
	/**
	 * This test-case tests how to get a device location weather using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	//FIXME
	public void test05getDeviceLocationWeather() throws IoTFCReSTException {
		try {
			JsonObject response = apiClient.getDeviceLocationWeather(DEVICE_TYPE, DEVICE_ID);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());	
		}
	}
	
	
	/**
	 * This test-case tests how to get a management information of a device using the Java Client Library.
	 * @throws Exception 
	 */
	@Test
	public void test06getDeviceManagementInformation() {
		final String METHOD = "test06getDeviceManagementInformation";

		try {
			TestHelper.registerDevice(apiClient, DEVICE_TYPE, MANAGED_DEV_ID, TestEnv.getDeviceToken());
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
			String failMsg = "Register device failed. HTTP code " + e.getHttpCode()
								+ " Response: " + e.getResponse();
			fail(failMsg);
			return;
		}
		

		Properties props = TestEnv.getDeviceProperties(DEVICE_TYPE, MANAGED_DEV_ID);
		
		ManagedDevice dmClient = null;
		try {
			DeviceData data = new DeviceData.Builder().build();
			dmClient = new ManagedDevice(props, data);
			dmClient.connect();
			dmClient.sendManageRequest(0, true, true);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		try {
			JsonObject response = apiClient.getDeviceManagementInformation(DEVICE_TYPE, MANAGED_DEV_ID);
			assertFalse("Response must not be null", response.isJsonNull());
			LoggerUtility.info(CLASS_NAME, METHOD, response.toString());
		} catch(IoTFCReSTException e) {
			e.printStackTrace();
			String failMsg = METHOD + " HTTP Code :" + e.getHttpCode() +" Reponse :: "+ e.getResponse();
			fail(failMsg);
		} finally {
			dmClient.disconnect();
		}
		
	}

	/**
	 * This test-case tests how to update a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	@Test
	public void test07updateDevice() throws IoTFCReSTException {
		
		JsonObject updatedMetadata = new JsonObject();
		
		try {
			
			JsonObject metadata = new JsonObject();
			metadata.addProperty("Hi", "Hello, I'm updated metadata");
		
			updatedMetadata.add("metadata", metadata);
			JsonObject response = apiClient.updateDevice(DEVICE_TYPE, DEVICE_ID, updatedMetadata);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}

	/**
	 * This test-case tests how to retrieve all the devices in an organization using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	@Test
	public void test08getAllDevicesOfAType() throws IoTFCReSTException {
		
		// Get all the devices of type TestDT
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		try {
			/**
			 * The Java ibmiotf client library provides an one argument constructor
			 * which can be used to control the output, for example, lets try to retrieve
			 * the devices in a sorted order based on device ID.
			 */
			parameters.add(new BasicNameValuePair("_sort","deviceId"));
			
			JsonObject response = apiClient.retrieveDevices(DEVICE_TYPE, parameters);
			
			// The response will contain more parameters that will be used to issue
			// the next request. The result element will contain the current list of devices
			JsonArray devices = response.get("results").getAsJsonArray(); 
			for(Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				assertFalse("Response must not be null", responseJson.isJsonNull());
			}
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}

	/**
	 * This test-case tests how to retrieve all the devices in an organization using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	@Test
	public void test08getAllDevices() throws IoTFCReSTException {

		try {
			
			JsonObject response = apiClient.getAllDevices();
			
			// The response will contain more parameters that will be used to issue
			// the next request. The result element will contain the current list of devices
			JsonArray devices = response.get("results").getAsJsonArray(); 
			for(Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject responseJson = deviceElement.getAsJsonObject();
				assertFalse("Response must not be null", responseJson.isJsonNull());
			}
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}
	
	/**
	 * This test-case tests how to Delete a device using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	@Test
	public void test08deleteDevice() throws IoTFCReSTException {
		try {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
			assertFalse("Device is not deleted successfully", apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID));
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
	}
	
	/**
	 * This test-case tests how to add a device, registered under a gateway, using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	@Test
	public void test09addDeviceUnderGateway() {
		
		String gwType = "gwType1";
		String gwDevId = "gwId1";
		String newDevId = gwDevId + "_1";
		
		JsonElement eGwType = new JsonParser().parse("{\"id\": \"" + gwType + "\"}");
		
		//Add gateway type
		try {
			JsonObject response = apiClient.addGatewayDeviceType(eGwType);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch (IoTFCReSTException e) {
			fail("Add gateway type failed for  " + gwType + " HTTP Error " + e.getHttpCode() );
			return;
		}
		
		// Register gateway device
		try {
			apiClient.registerDevice(gwType, gwDevId, TestEnv.getGatewayToken(), null, null, null);
		} catch (IoTFCReSTException e) {
			fail("Register gateway device failed for " + gwDevId + " HTTP Error " + e.getHttpCode() );
			return;
		}
		
		// Register device under gateway
		try {
			JsonObject response = apiClient.registerDeviceUnderGateway(
					DEVICE_TYPE, newDevId, gwType, gwDevId);
			assertTrue("Device is not registered successfully", apiClient.isDeviceExist(DEVICE_TYPE, newDevId));
			assertFalse("Response must not be null", response.isJsonNull());
		} catch (IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		}
		
		// Verify we can get information
		try {
			JsonObject response = apiClient.getDevicesConnectedThroughGateway(gwType, gwDevId);
			assertFalse("Response must not be null", response.isJsonNull());
			
			JsonArray devices = response.get("results").getAsJsonArray(); 
			assertTrue("The results size must be 1", devices.size() == 1);
			for (Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
				JsonElement deviceElement = iterator.next();
				JsonObject deviceJson = deviceElement.getAsJsonObject();
				assertTrue("The type must be " + DEVICE_TYPE, 
						deviceJson.get("typeId").getAsString().equals(DEVICE_TYPE));
				assertTrue("The device ID must be " + newDevId, 
						deviceJson.get("deviceId").getAsString().equals(newDevId));
			}
		} catch (IoTFCReSTException e) {
			fail("Get devices under gateway failed. HTTP error: " + e.getHttpCode());
		}
		
		try {
			apiClient.deleteDevice(DEVICE_TYPE, newDevId);
		} catch (IoTFCReSTException e) {
			fail("Delete device under gateway failed. HTTP error: " + e.getHttpCode());
		}
		
		try {
			apiClient.deleteDevice(gwType, gwDevId);
		} catch (IoTFCReSTException e) {
			fail("Delete gateway device failed. HTTP error: " + e.getHttpCode());
		}
		
		try {
			apiClient.deleteDeviceType(gwType);
		} catch (IoTFCReSTException e) {
			fail("Delete gateway type failed. HTTP error: " + e.getHttpCode());
		}
		
	}
	
}