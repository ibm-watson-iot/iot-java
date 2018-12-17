package com.ibm.iotf.client.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.iotf.devicemgmt.DeviceFirmwareHandler;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.DeviceMetadata;
import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.test.common.TestDeviceActionHandler;
import com.ibm.iotf.test.common.TestDeviceFirmwareHandler;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestHelper;
import com.ibm.iotf.util.LoggerUtility;;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceManagementTest {
	private static final String CLASS_NAME = DeviceManagementTest.class.getName();
	
	private static Random random = new Random();
	private static APIClient apiClient = null;
	private static ManagedDevice dmClient = null;
	private static final String DEVICE_TYPE = "DevMgmtType1";
	private static final String DEVICE_ID = "DevMgmtDev1";
	private static final String APP_ID = "DevMgmtApp1";
	
	
	private static final String rebootRequestToBeInitiated = "{\"action\": \"device/reboot\","
				+ "\"devices\": [ {\"typeId\": \"" + DEVICE_TYPE +"\","
				+ "\"deviceId\": \"" + DEVICE_ID + "\"}]}";
		
	private static final String	factoryRequestToBeInitiated = "{\"action\": \"device/factoryReset\","
				+ "\"devices\": [ {\"typeId\": \"" + DEVICE_TYPE +"\","
				+ "\"deviceId\": \"" + DEVICE_ID + "\"}]}";
		
		
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
	
	public void test01ManageRequest() {
		final String METHOD = "test01ManageRequest";
		boolean status = false;
		try {
			status = dmClient.sendManageRequest(0, true, false);
			LoggerUtility.info(CLASS_NAME, METHOD, "send manage request, success = " + status); 
			DeviceData devicedata = dmClient.getDeviceData();
			if(devicedata == null) {
				fail("Device data must not be null");
			}
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		assertTrue("Manage request is unsuccessfull", status);
	}
	
	public void test02UnManageRequest() {
		final String METHOD = "test02UnManageRequest";
		boolean status = false;
		try {
			status = dmClient.sendManageRequest(0, true, false);
			LoggerUtility.info(CLASS_NAME, METHOD, "send manage request, success = " + status); 
			status = dmClient.sendUnmanageRequest();
			LoggerUtility.info(CLASS_NAME, METHOD, "send unmanage request, success = " + status); 
		} catch (MqttException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertTrue("UnManage request is unsuccessfull", status);
	}
	
	public void test03LocationUpdate() {
		final String METHOD = "test03LocationUpdate";
		boolean status = false;
		try {
			status = dmClient.sendManageRequest(0, true, false);
			LoggerUtility.info(CLASS_NAME, METHOD, "send manage request, success = " + status); 
		} catch (MqttException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		double latitude = random.nextDouble() + 30;
		double longitude = random.nextDouble() - 98;
		double elevation = (double)random.nextInt(100);
		
		int rc = dmClient.updateLocation(latitude, longitude, elevation);
		assertTrue("Location update is unsuccessfull", rc==200);
		
		// user overloaded method
		rc = dmClient.updateLocation(latitude, longitude, elevation, new Date());
		assertTrue("Location update is unsuccessfull", rc==200);
		
		// user overloaded method
		rc = dmClient.updateLocation(latitude, longitude, elevation, new Date(), 1d);
		assertTrue("Location update is unsuccessfull", rc==200);
	}
	
	public void test04Errorcodes() {
		final String METHOD = "test04Errorcodes";
		boolean status = false;
		try {
			status = dmClient.sendManageRequest(0, true, false);
			LoggerUtility.info(CLASS_NAME, METHOD, "send manage request, success = " + status); 
		} catch (MqttException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		int errorCode = random.nextInt(500);
		int rc = dmClient.addErrorCode(errorCode);
		assertTrue("Errorcode addition unsuccessfull", rc==200);
		
		// Let us clear the errorcode now
		rc = dmClient.clearErrorCodes();
		assertTrue("clear Errorcode operation is unsuccessfull", rc==200);
	}

	public void test05LogMessages() {
		
		final String METHOD = "test04Errorcodes";
		boolean status = false;
		try {
			status = dmClient.sendManageRequest(0, true, false);
			LoggerUtility.info(CLASS_NAME, METHOD, "send manage request, success = " + status); 
		} catch (MqttException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		String message = "Log event 1";
		Date timestamp = new Date();
		LogSeverity severity = LogSeverity.informational;
		int rc = dmClient.addLog(message, timestamp, severity);
		assertTrue("Log addition unsuccessfull", rc==200);
		
		// Use overloaded methods
		rc = dmClient.addLog("Log event with data", timestamp, severity, "Sample data");
		assertTrue("Log addition unsuccessfull", rc==200);

		// Let us clear the errorcode now
		rc = dmClient.clearLogs();
		assertTrue("clear Log operation is unsuccessfull", rc==200);
	}

	public void test06FirmwareDownload() {
		final String METHOD = "test06FirmwareDownload";
		try {
			dmClient.connect(true);
			dmClient.sendManageRequest(0, true, true);
		} catch (MqttException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		TestDeviceFirmwareHandler handler = new TestDeviceFirmwareHandler(dmClient);
		
		try {
			dmClient.addFirmwareHandler(handler);
		} catch(Exception e) {
			fail(e.getMessage());
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
			LoggerUtility.info(CLASS_NAME, METHOD, "DM request status, response " + response);
			assertEquals(0, status.get("status").getAsInt());
			assertTrue(status.get("complete").getAsBoolean());
			assertTrue("The firmware request/parameters not received", "123df".equals(handler.getDeviceFirmwareVerifier()));
			assertTrue("The firmware request/parameters not received", "RasPi01 firmware".equals(handler.getDeviceFirmwareName()));
			assertTrue("The firmware request/parameters not received", "0.1.10".equals(handler.getDeviceFirmwareVersion()));
			assertTrue("The firmware request/parameters not received", 
					"https://github.com/ibm-messaging/iot-raspberrypi/releases/download/1.0.2.1/iot_1.0-2_armhf.deb".equals(handler.getDeviceFirmwareURL()));
			
		} else {
			fail("Failed to get status of DM request");
		}		
	}


	public void test07FirmwareUpdate() {
		
		final String METHOD = "test07FirmwareUpdate";
		
		try {
			dmClient.connect();
			dmClient.sendManageRequest(0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		TestDeviceFirmwareHandler handler = new TestDeviceFirmwareHandler(dmClient);
		
		try {
			dmClient.addFirmwareHandler(handler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject update = (JsonObject) new JsonParser().parse(updateRequest);
		
		JsonObject response = null;
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
		
		
		int counter = 0;
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

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		JsonObject status = null;
		try {
			status = apiClient.getDeviceManagementRequestStatusByDevice(response.get("reqId").getAsString(), DEVICE_TYPE, DEVICE_ID);
		} catch (IoTFCReSTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue("The firmware request/parameters not received", handler.firmwareUpdated());	
		
		if (status != null) {
			assertEquals(0, status.get("status").getAsInt());
			assertTrue(status.get("complete").getAsBoolean());
		} else {
			fail("Failed to get status of DM request");
		}
	}

	public void test08RebootRequest() {
		final String METHOD = "test08RebootRequest";
		
		try {
			dmClient.connect(5);
			dmClient.sendManageRequest(0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		TestDeviceActionHandler handler = new TestDeviceActionHandler(dmClient);
		try {
			dmClient.addDeviceActionHandler(handler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject jsonReboot = (JsonObject) new JsonParser().parse(rebootRequestToBeInitiated);
		//System.out.println(jsonReboot);
		JsonObject response = null;
		try {
			response = apiClient.initiateDMRequest(jsonReboot);
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
			if(handler.rebooted()) {
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
			LoggerUtility.info(CLASS_NAME, METHOD, "reboot request status, response " + response);
			assertEquals(0, status.get("status").getAsInt());
			assertTrue(status.get("complete").getAsBoolean());
		} else {
			fail("Failed to get status of DM request");
		}
		assertTrue("The device reboot request is not received", handler.rebooted());
	}

	public void test09FactoryResetRequest() {
		
		final String METHOD = "test09FactoryResetRequest";
		
		try {
			dmClient.connect();
			dmClient.sendManageRequest(0, true, true);
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
