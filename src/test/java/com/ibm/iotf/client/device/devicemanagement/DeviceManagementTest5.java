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
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.handlers.TestDeviceFirmwareHandler;
import com.ibm.iotf.test.common.TestApplicationHelper;
import com.ibm.wiotp.sdk.IoTFCReSTException;
import com.ibm.wiotp.sdk.api.APIClient;
import com.ibm.wiotp.sdk.devicemgmt.DeviceData;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware;
import com.ibm.wiotp.sdk.devicemgmt.DeviceInfo;
import com.ibm.wiotp.sdk.devicemgmt.DeviceMetadata;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.wiotp.sdk.devicemgmt.device.ManagedDevice;
import com.ibm.wiotp.sdk.util.LoggerUtility;;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceManagementTest5 {
	private static final String CLASS_NAME = DeviceManagementTest5.class.getName();
	
	private static APIClient apiClient = null;
	private static ManagedDevice dmClient = null;
	private static final String DEVICE_TYPE = "DevMgmtType5";
	private static final String DEVICE_ID = "DevMgmtDevXX5"; // @FIXME remove X
	private static final String APP_ID = "DevMgmtApp5";
	
	private static final String	downloadRequest = "{\"action\": \"firmware/download\", \"parameters\": [{\"name\": \"version\", \"value\": \"0.1.10\" }," +
			"{\"name\": \"name\", \"value\": \"RasPi01 firmware\"}, {\"name\": \"verifier\", \"value\": \"123df\"}," +
			"{\"name\": \"uri\",\"value\": \"https://github.com/ibm-messaging/iot-raspberrypi/releases/download/1.0.2.1/iot_1.0-2_armhf.deb\"}" +
			"],\"devices\": [{\"typeId\": \"" + DEVICE_TYPE + "\",\"deviceId\": \"" + DEVICE_ID + "\"}]}";
	
	private static final String	updateRequest = "{\"action\": \"firmware/update\", \"devices\": [{\"typeId\": \"" + DEVICE_TYPE + "\",\"deviceId\": \"" + DEVICE_ID + "\"}]}";
	

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
				TestApplicationHelper.addDeviceType(apiClient, DEVICE_TYPE);
				LoggerUtility.info(CLASS_NAME, METHOD, "Device type " + DEVICE_TYPE + " has been created.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}
		
		try {
			TestApplicationHelper.deleteDevice(apiClient, DEVICE_TYPE, DEVICE_ID);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		
		try {
			TestApplicationHelper.registerDevice(apiClient, DEVICE_TYPE, DEVICE_ID, TestEnv.getDeviceToken());
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
			TestApplicationHelper.deleteDevice(apiClient, DEVICE_TYPE, DEVICE_ID);
		}
		LoggerUtility.info(CLASS_NAME, METHOD, "completed."); 
	}	
	
	@Test
	public void test01FirmwareUpdate() {
		
		final String METHOD = "test01FirmwareUpdate";
		
		try {
			dmClient.connect();
			boolean status = dmClient.sendManageRequest(0, true, true);
			LoggerUtility.info(CLASS_NAME, METHOD, "send manage request, success = " + status); 
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		TestDeviceFirmwareHandler handler = new TestDeviceFirmwareHandler(dmClient);
		
		try {
			dmClient.addFirmwareHandler(handler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject download = (JsonObject) new JsonParser().parse(downloadRequest);
		
		JsonObject response = null;
		try {
			response = apiClient.initiateDMRequest(download);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		if (response != null) {
			if (response.has("reqId")) {
				LoggerUtility.info(CLASS_NAME, METHOD, "initiated firmware download request ID = " + response.get("reqId").getAsString());
			} else {
				LoggerUtility.info(CLASS_NAME, METHOD, "initiated firmware download, response " + response);
			}
		}

		int counter = 0;
		// wait for sometime
		while(counter <= 20) {
			// For some reason the volatile doesn't work, so using the lock
			if (handler.firmwareDownloaded()) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			counter++;
		}
		
		JsonObject update = (JsonObject) new JsonParser().parse(updateRequest);
		
		response = null;
		try {
			response = apiClient.initiateDMRequest(update);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		if (response != null) {
			if (response.has("reqId")) {
				LoggerUtility.info(CLASS_NAME, METHOD, "initiated firmware update request ID = " + response.get("reqId").getAsString());
			} else {
				LoggerUtility.info(CLASS_NAME, METHOD, "initiated firmware update, response " + response);
			}
		}
		
		
		counter = 0;
		// wait for sometime
		while(counter <= 20) {
			// For some reason the volatile doesn't work, so using the lock
			if(handler.firmwareUpdated()) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			counter++;
		}

		JsonObject status = null;
		
		if (response != null && response.has("reqId")) {
			try {
				status = apiClient.getDeviceManagementRequestStatusByDevice(response.get("reqId").getAsString(), DEVICE_TYPE, DEVICE_ID);
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}
		
		assertTrue("The firmware request/parameters not received", handler.firmwareUpdated());	
		
		if (status != null) {
			assertEquals(0, status.get("status").getAsInt());
			assertTrue(status.get("complete").getAsBoolean());
		} else {
			fail("Failed to get status of DM request");
		}
		
		dmClient.disconnect();
		
	}

	
}
