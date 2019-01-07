package com.ibm.iotf.client.gateway;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.util.LoggerUtility;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayManagementTest2 {
	private static final String CLASS_NAME = GatewayManagementTest2.class.getName();
	private static final String APP_ID = "GWMgmtTest2App1";

	private static APIClient apiClient = null;


	private static final String GW_DEVICE_TYPE = "GwMgmtType2";
	private static final String GW_DEVICE_ID = "GwMgmtDev2";

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
		
		// Add gateway type and register gateway device
		JsonObject jsonGWType = new JsonObject();
		jsonGWType.addProperty("id", GW_DEVICE_TYPE);
		jsonGWType.addProperty("classId", "Gateway");
		try {
			apiClient.addGatewayDeviceType(jsonGWType);
			LoggerUtility.info(CLASS_NAME, METHOD, "Gateway type " + GW_DEVICE_TYPE + " created.");
		} catch (IoTFCReSTException e) {
			String failMsg = "addGatewayDeviceType HTTP code " + e.getHttpCode() + " response " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
		}
		
		try {
			apiClient.registerDevice(GW_DEVICE_TYPE, GW_DEVICE_ID, TestEnv.getGatewayToken(), null, null, null);
			LoggerUtility.info(CLASS_NAME, METHOD, "Gateway device " + GW_DEVICE_ID + " created.");
		} catch (IoTFCReSTException e) {
			String failMsg = "registerDevice HTTP code " + e.getHttpCode() + " response " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
		}
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		final String METHOD = "oneTimeTearDown";
		LoggerUtility.info(CLASS_NAME, METHOD, "Cleaning up...");
		
		if (apiClient != null) {

    		try {
				apiClient.deleteDevice(GW_DEVICE_TYPE, GW_DEVICE_ID);
			} catch (IoTFCReSTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		try {
				apiClient.deleteDeviceType(GW_DEVICE_TYPE);
			} catch (IoTFCReSTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}
	
	@Test
	public void test01ManageRequest() {
		final String METHOD = "test01ManageRequest";
		ManagedGateway gwClient = null;
		boolean status = false;
		try {
			final DeviceData deviceData = new DeviceData.Builder()
			        .typeId(GW_DEVICE_TYPE).deviceId(GW_DEVICE_ID).build();
			
			Properties gwProps = TestEnv.getGatewayProperties(GW_DEVICE_TYPE, GW_DEVICE_ID);
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
			LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing sendGatewayManageRequest(0, false, true)");

			status = gwClient.sendGatewayManageRequest(0, false, false);
			assertTrue("Gateway Manage request is unsuccessfull", status);
			
			status = gwClient.sendGatewayUnmanageRequet();
			assertTrue("Gateway Un-manage request is unsuccessfull", status);
			
			gwClient.disconnect();
			LoggerUtility.info(CLASS_NAME, METHOD, "connected (" + gwClient.isConnected() + ")");
			
		} catch (MqttException e) {
			String failMsg = "Unexpected MQTT Exception : " + e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);
		}
	}

}
