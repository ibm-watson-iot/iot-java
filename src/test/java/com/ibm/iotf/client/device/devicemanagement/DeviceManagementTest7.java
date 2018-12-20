package com.ibm.iotf.client.device.devicemanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.DeviceMetadata;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.test.common.TestDeviceActionHandler;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestHelper;
import com.ibm.iotf.util.LoggerUtility;;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceManagementTest7 {
	private static final String CLASS_NAME = DeviceManagementTest7.class.getName();
	
	private static APIClient apiClient = null;
	private static ManagedDevice dmClient = null;
	private static final String DEVICE_TYPE = "DevMgmtType6";
	private static final String DEVICE_ID = "DevMgmtDev6";
	private static final String APP_ID = "DevMgmtApp6";
	
	
	private static final String	factoryRequestToBeInitiated = "{\"action\": \"device/factoryReset\","
				+ "\"devices\": [ {\"typeId\": \"" + DEVICE_TYPE +"\","
				+ "\"deviceId\": \"" + DEVICE_ID + "\"}]}";
		

	/**
	 * This method builds the device objects required to create the
	 * ManagedClient
	 * 
	 * @param propertiesFile
	 * @throws Exception
	 */
	private static void createManagedClient(Properties deviceProps) throws Exception {
		DeviceFirmware firmware = new DeviceFirmware.Builder().
				version("1.0.1").
				name("iot-arm.deb").
				url("").
				verifier("12345").
				state(FirmwareState.IDLE).				
				build();
		
		DeviceInfo deviceInfo = new DeviceInfo.Builder().
				serialNumber("1234errrrrr").
				manufacturer("IBM").
				model("T450").
				deviceClass("A+").
				description("Lenovo ThinkPad").
				fwVersion("Windows10").
				hwVersion("1.2.3").
				descriptiveLocation("ELGC-6F-C138").
				build();
		
		/**
		 * Create a DeviceMetadata object
		 */
		JsonObject data = new JsonObject();
		data.addProperty("customField", "customValue");
		DeviceMetadata metadata = new DeviceMetadata(data);
		metadata.setMetadata(new JsonObject());
		
		DeviceData deviceData = new DeviceData.Builder().
						 deviceInfo(deviceInfo).
						 deviceFirmware(firmware).
						 metadata(metadata).
						 build();
		
		dmClient = new ManagedDevice(deviceProps, deviceData);
	}
	
	
	@BeforeClass
	public static void oneTimeSetUp() {
		
		final String METHOD = "oneTimeSetUp";
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, null, null);
		try {
			apiClient = new APIClient(appProps);
		} catch (KeyManagementException e) {
			e.printStackTrace();
			return;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return;
		}
		
		boolean exist = false;
		
		try {
			exist = apiClient.isDeviceTypeExist(DEVICE_TYPE);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		
		if (!exist) {
			try {
				TestHelper.addDeviceType(apiClient, DEVICE_TYPE);
				LoggerUtility.info(CLASS_NAME, METHOD, "Device type " + DEVICE_TYPE + " has been created.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}
		
		try {
			TestHelper.deleteDevice(apiClient, DEVICE_TYPE, DEVICE_ID);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		
		try {
			TestHelper.registerDevice(apiClient, DEVICE_TYPE, DEVICE_ID, TestEnv.getDeviceToken());
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		
		Properties deviceProps = TestEnv.getDeviceProperties(DEVICE_TYPE, DEVICE_ID);

		try {
			createManagedClient(deviceProps);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {
		final String METHOD = "oneTimeCleanup";
		
		if (apiClient != null) {
			TestHelper.deleteDevice(apiClient, DEVICE_TYPE, DEVICE_ID);
		}
		LoggerUtility.info(CLASS_NAME, METHOD, "completed."); 
	}	
	
	@Test
	public void test01FactoryResetRequest() {
		
		final String METHOD = "test01FactoryResetRequest";
		
		try {
			dmClient.connect();
			boolean status = dmClient.sendManageRequest(0, true, true);
			LoggerUtility.info(CLASS_NAME, METHOD, "send manage request, success = " + status); 
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		TestDeviceActionHandler handler = new TestDeviceActionHandler(dmClient);
		
		try {
			dmClient.addDeviceActionHandler(handler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject factory = (JsonObject) new JsonParser().parse(factoryRequestToBeInitiated);
		System.out.println(factory);
		JsonObject response = null;
		
		try {
			response = apiClient.initiateDMRequest(factory);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		if (response != null) {
			if (response.has("reqId")) {
				LoggerUtility.info(CLASS_NAME, METHOD, "initiated reboot request ID = " + response.get("reqId").getAsString());
			} else {
				LoggerUtility.info(CLASS_NAME, METHOD, "initiated reboot request, response " + response);
			}
		}
		
		int counter = 0;
		// wait for sometime
		while(counter <= 20) {
			// For some reason the volatile doesn't work, so using the lock
			if (handler.factoryReset()) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			counter++;
		}

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JsonObject status = null;
		try {
			status = apiClient.getDeviceManagementRequestStatusByDevice(response.get("reqId").getAsString(), DEVICE_TYPE, DEVICE_ID);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		if (status != null) {
			assertEquals(0, status.get("status").getAsInt());
			assertTrue(status.get("complete").getAsBoolean());
		} else {
			fail("Failed to get status of DM request");
		}
		assertTrue("The device factory reset request is not received", handler.factoryReset());
	}
	
	
}
