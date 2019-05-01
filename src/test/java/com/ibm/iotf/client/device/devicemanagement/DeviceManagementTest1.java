package com.ibm.iotf.client.device.devicemanagement;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonObject;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestApplicationHelper;
import com.ibm.wiotp.sdk.IoTFCReSTException;
import com.ibm.wiotp.sdk.api.APIClient;
import com.ibm.wiotp.sdk.devicemgmt.DeviceData;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware;
import com.ibm.wiotp.sdk.devicemgmt.DeviceInfo;
import com.ibm.wiotp.sdk.devicemgmt.DeviceMetadata;
import com.ibm.wiotp.sdk.devicemgmt.LogSeverity;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.wiotp.sdk.devicemgmt.device.ManagedDevice;
import com.ibm.wiotp.sdk.util.LoggerUtility;;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceManagementTest1 {
	private static final String CLASS_NAME = DeviceManagementTest1.class.getName();
	
	private static APIClient apiClient = null;
	private static ManagedDevice dmClient = null;
	private static final String DEVICE_TYPE = "DevMgmtType1";
	private static final String DEVICE_ID = "DevMgmtDev1";
	private static final String APP_ID = "DevMgmtApp1";
	
	

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
	public void test01LogMessages() {
		
		final String METHOD = "test01LogMessages";
		boolean status = false;
		try {
			dmClient.connect();
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
		
		dmClient.disconnect();
	}

	
}
