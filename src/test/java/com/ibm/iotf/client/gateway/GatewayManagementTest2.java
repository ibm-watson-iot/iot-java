package com.ibm.iotf.client.gateway;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceMetadata;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;
import com.ibm.iotf.test.common.TestApplicationHelper;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestGatewayHelper;
import com.ibm.iotf.test.common.TestPropertyChangeListener;
import com.ibm.iotf.util.LoggerUtility;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayManagementTest2 {
	private static final String CLASS_NAME = GatewayManagementTest2.class.getName();
	private static final String APP_ID = "GWMgmtTest2App1";

	private static APIClient apiClient = null;


	private static final String GW_DEVICE_TYPE = "GwMgmtType2";
	private static final String GW_DEVICE_ID_PREFIX = "GwMgmtDev2";

	private static int testNum = 1;
	private static int totalTests = 2;
	
	private static HashMap<Integer,String> testMap = new HashMap<Integer,String>();
	
	private synchronized int getTestNumber() {
		int number = testNum;
		testNum++;
		return number;
	}

	@BeforeClass
	public static void oneTimeSetup() {
		final String METHOD = "oneTimeSetup";
		LoggerUtility.info(CLASS_NAME, METHOD, "Using org " + TestEnv.getOrgId());
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, null, null);
		
		try {
			apiClient = new APIClient(appProps);
		} catch (Exception e) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Exception ", e);
			e.printStackTrace();
			return;
		}
		
		boolean exist = false;
		try {
			exist = apiClient.isDeviceTypeExist(GW_DEVICE_TYPE);
		} catch (IoTFCReSTException e1) {
			e1.printStackTrace();
		}
		
		if (!exist) {
			try {
				TestApplicationHelper.addGatewayType(apiClient, GW_DEVICE_TYPE);
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
				return;
			}
		}
		// Delete devices that were left in previous test run
		// Register gateway and attached devices ...
		for (int i=1; i<= totalTests; i++) {			
			String gwDevId = new String(GW_DEVICE_ID_PREFIX + "_" + i);
			try {
				TestGatewayHelper.deleteDevice(apiClient, GW_DEVICE_TYPE, gwDevId);
			} catch (IoTFCReSTException e1) {
				e1.printStackTrace();
				continue; //move to next test
			}
			
			// Register gateway device
			try {
				apiClient.registerDevice(GW_DEVICE_TYPE, gwDevId, TestEnv.getGatewayToken(), null, null, null);
				LoggerUtility.info(CLASS_NAME, METHOD, "Gateway device " + gwDevId + " has been created.");
				testMap.put(new Integer(i), gwDevId);
				
				LoggerUtility.info(CLASS_NAME, METHOD, apiClient.getDevice(GW_DEVICE_TYPE, gwDevId).toString());
				

				/* Set metadata
				JsonObject jsonProps = new JsonObject();
				JsonObject jsonMetadata = new JsonObject();
				jsonMetadata.addProperty("setup", i);
				jsonProps.add("metadata", jsonMetadata);
				
				try {
					apiClient.updateDevice(GW_DEVICE_TYPE, gwDevId, jsonProps);
				} catch (IoTFCReSTException e) {
					String failMsg = "Update Device failed, Exception: " + e.getMessage();
					LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
				}

				LoggerUtility.info(CLASS_NAME, METHOD, apiClient.getDevice(GW_DEVICE_TYPE, gwDevId).toString());
				
				*/
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		final String METHOD = "oneTimeTearDown";
		LoggerUtility.info(CLASS_NAME, METHOD, "Cleaning up...");
		
		if (apiClient != null) {
			for (int i=1; i<= totalTests; i++) {
				Integer iTest = new Integer(i);
				String gwDevId = testMap.get(iTest);
				try {
					apiClient.deleteDevice(GW_DEVICE_TYPE, gwDevId);
				} catch (IoTFCReSTException e) {
					e.printStackTrace();
				}
			}
    	}
	}
	
	@Test
	public void test01ManageRequest() {
		final String METHOD = "test01ManageRequest";
		ManagedGateway gwClient = null;
		
		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);
		String gwDevId = testMap.get(iTest);
		
		boolean status = false;
		try {
			// Current Watson IoT Device Info
			JsonObject curMetadata = null;
			DeviceMetadata curDeviceMetadata = null;

			try {
				JsonObject curDevice = apiClient.getDevice(GW_DEVICE_TYPE, gwDevId);
				if (curDevice.has("metadata") && 
						curDevice.get("metadata").getAsJsonObject().entrySet() != null &&
						curDevice.get("metadata").getAsJsonObject().entrySet().size() > 0) {
					curMetadata = curDevice.get("metadata").getAsJsonObject();
					LoggerUtility.info(CLASS_NAME, METHOD, "Current Metadata : "  + curMetadata.toString());
					curDeviceMetadata = new DeviceMetadata(curMetadata);
				}
				if (curMetadata == null) {
					curDeviceMetadata = new DeviceMetadata(new JsonObject());
					LoggerUtility.info(CLASS_NAME, METHOD, "New Metadata : "  + curDeviceMetadata.getMetadata().toString());
				}
			} catch (IoTFCReSTException e) {
				String failMsg = "Get Device failed, Exception: " + e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
				e.printStackTrace();
			}

			// Build device data with current metadata
			final DeviceData deviceData = new DeviceData.Builder()
			        .typeId(GW_DEVICE_TYPE)
			        .deviceId(gwDevId)
			        .metadata(curDeviceMetadata).build();
			
			Properties gwProps = TestEnv.getGatewayProperties(GW_DEVICE_TYPE, gwDevId);
			try {
				gwClient = new ManagedGateway(gwProps, deviceData);
				LoggerUtility.info(CLASS_NAME, METHOD, "Created managed gateway client");
			} catch (Exception e) {
				String failMsg = "ManagedGateway Exception: " + e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			}
			gwClient.setGatewayCallback(new GatewayCallbackTest(gwProps));
			try {
				gwClient.connect();
				LoggerUtility.info(CLASS_NAME, METHOD, "connected (" + gwClient.isConnected() + ")");
			} catch (MqttException e) {
				String failMsg = "Connect failed, Exception: " + e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			}

			// Gateway manage request
			status = gwClient.sendGatewayManageRequest(0, false, false);
			assertTrue("Gateway Manage request is unsuccessfull", status);

			// Add properties change listener
			TestPropertyChangeListener listener = new TestPropertyChangeListener();
			gwClient.getGatewayDeviceData().getMetadata().addPropertyChangeListener(listener);
			
			// Current Watson IoT device details
			try {
				LoggerUtility.info(CLASS_NAME, METHOD, "Watson IoT Platform Device details after manage request : " + apiClient.getDevice(GW_DEVICE_TYPE, gwDevId).toString());
			} catch (IoTFCReSTException e) {
				String failMsg = "Get Device failed, Exception: " + e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
				e.printStackTrace();
			}
			
			// Current metadata
			JsonObject currentDeviceMetadata = gwClient.getGatewayDeviceData().getMetadata().getMetadata();
			LoggerUtility.info(CLASS_NAME, METHOD, "Current Gateway Device Metadata : " + currentDeviceMetadata.toString());
			
			// Update metadata
			JsonObject jsonProps = new JsonObject();
			JsonObject jsonMetadata = new JsonObject();
			jsonMetadata.addProperty("test", iTest.intValue());
			jsonProps.add("metadata", jsonMetadata);
			
			try {
				apiClient.updateDevice(GW_DEVICE_TYPE, gwDevId, jsonProps);
			} catch (IoTFCReSTException e) {
				String failMsg = "Update Device failed, Exception: " + e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			}
			
			// Check for property change listener
			int count = 10;
			boolean notify = false;
			while (--count > 0) {
				String propertyName = listener.getPropertyName();
				if (propertyName != null && propertyName.equals("metadata")) {
					Object newValue = listener.getNewValue();
					if (newValue != null) {
						notify = true;
						LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "New Value : " + newValue);
						break;
					}
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			
			assertTrue("Property Listener has not been notified", notify);
			
			// Verify metadata update
			try {
				JsonObject jsonDevice = apiClient.getDevice(GW_DEVICE_TYPE, gwDevId);
				JsonObject updateDeviceMetadata = jsonDevice.get("metadata").getAsJsonObject();
				LoggerUtility.info(CLASS_NAME, METHOD, "Metadata from Watson IoT Platform API : " + updateDeviceMetadata.toString());
				assertTrue("Device Metadata was not updated.", jsonMetadata.equals(updateDeviceMetadata));
				
				JsonObject deviceMetadata = gwClient.getGatewayDeviceData().getMetadata().getMetadata();
				LoggerUtility.info(CLASS_NAME, METHOD, "Metadata from Gateway Client : " + deviceMetadata.toString());
				assertTrue("Device Metadata was not updated.", jsonMetadata.equals(deviceMetadata));				
			} catch (IoTFCReSTException e) {
				String failMsg = "Get Device failed, Exception: " + e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			}
			
			status = gwClient.sendGatewayUnmanageRequet();
			assertTrue("Gateway Un-manage request is unsuccessfull", status);			
			
			gwClient.disconnect();
			
		} catch (MqttException e) {
			String failMsg = "Unexpected MQTT Exception : " + e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
	}

	@Test
	public void test02ManageRequest() {
		final String METHOD = "test02ManageRequest";
		ManagedGateway gwClient = null;
		
		Integer iTest = new Integer(getTestNumber());
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Running test #" + iTest);
		
		String gwDevId = testMap.get(iTest);
		
		JsonObject newData = new JsonObject();
		newData.addProperty("start", true);
		DeviceMetadata newMetadata = new DeviceMetadata(newData);
		
		boolean status = false;
		try {
			// Build device data with new metadata
			final DeviceData deviceData = new DeviceData.Builder()
			        .typeId(GW_DEVICE_TYPE).deviceId(gwDevId).metadata(newMetadata).build();
			
			Properties gwProps = TestEnv.getGatewayProperties(GW_DEVICE_TYPE, gwDevId);
			try {
				gwClient = new ManagedGateway(gwProps, deviceData);
				LoggerUtility.info(CLASS_NAME, METHOD, "Created managed gateway client");
			} catch (Exception e) {
				String failMsg = "ManagedGateway Exception: " + e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			}
			gwClient.setGatewayCallback(new GatewayCallbackTest(gwProps));
			try {
				gwClient.connect();
				LoggerUtility.info(CLASS_NAME, METHOD, "connected (" + gwClient.isConnected() + ")");
			} catch (MqttException e) {
				String failMsg = "Connect failed, Exception: " + e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			}
			
			// Gateway manage request
			status = gwClient.sendGatewayManageRequest(0, false, false);
			assertTrue("Gateway Manage request is unsuccessfull", status);

			// Add properties change listener
			TestPropertyChangeListener listener = new TestPropertyChangeListener();
			gwClient.getGatewayDeviceData().getMetadata().addPropertyChangeListener(listener);
			
			// Current Watson IoT device details
			try {
				LoggerUtility.info(CLASS_NAME, METHOD, "Watson IoT Platform Device details after manage request : " + apiClient.getDevice(GW_DEVICE_TYPE, gwDevId).toString());
			} catch (IoTFCReSTException e) {
				String failMsg = "Get Device failed, Exception: " + e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
				e.printStackTrace();
			}
			
			// Current metadata
			JsonObject currentDeviceMetadata = gwClient.getGatewayDeviceData().getMetadata().getMetadata();
			LoggerUtility.info(CLASS_NAME, METHOD, "Current Gateway Device Metadata : " + currentDeviceMetadata.toString());
			
			// Update metadata
			JsonObject jsonProps = new JsonObject();
			JsonObject jsonMetadata = new JsonObject();
			jsonMetadata.addProperty("test", iTest.intValue());
			jsonProps.add("metadata", jsonMetadata);
			
			try {
				apiClient.updateDevice(GW_DEVICE_TYPE, gwDevId, jsonProps);
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
			
			// Check for property change listener
			int count = 10;
			boolean notify = false;
			while (--count > 0) {
				String propertyName = listener.getPropertyName();
				if (propertyName != null && propertyName.equals("metadata")) {
					Object newValue = listener.getNewValue();
					if (newValue != null) {
						notify = true;
						LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "New Value : " + newValue);
						break;
					}
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			assertTrue("Property Listener has not been notified", notify);
			
			// Verify metadata update
			try {
				JsonObject jsonDevice = apiClient.getDevice(GW_DEVICE_TYPE, gwDevId);
				JsonObject updateDeviceMetadata = jsonDevice.get("metadata").getAsJsonObject();
				LoggerUtility.info(CLASS_NAME, METHOD, "Metadata from Watson IoT Platform API : " + updateDeviceMetadata.toString());
				assertTrue("Device Metadata was not updated.", jsonMetadata.equals(updateDeviceMetadata));
				
				JsonObject deviceMetadata = gwClient.getGatewayDeviceData().getMetadata().getMetadata();
				LoggerUtility.info(CLASS_NAME, METHOD, "Metadata from Gateway Client : " + deviceMetadata.toString());
				assertTrue("Device Metadata was not updated.", jsonMetadata.equals(deviceMetadata));				
			} catch (IoTFCReSTException e) {
				String failMsg = "Get Device failed, Exception: " + e.getMessage();
				LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			}
			
			status = gwClient.sendGatewayUnmanageRequet();
			assertTrue("Gateway Un-manage request is unsuccessfull", status);			
			
			gwClient.disconnect();
			
		} catch (MqttException e) {
			String failMsg = "Unexpected MQTT Exception : " + e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
	}
	
}
