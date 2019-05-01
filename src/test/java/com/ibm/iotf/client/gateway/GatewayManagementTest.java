package com.ibm.iotf.client.gateway;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.wiotp.sdk.IoTFCReSTException;
import com.ibm.wiotp.sdk.api.APIClient;
import com.ibm.wiotp.sdk.devicemgmt.DeviceAction;
import com.ibm.wiotp.sdk.devicemgmt.DeviceActionHandler;
import com.ibm.wiotp.sdk.devicemgmt.DeviceData;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmwareHandler;
import com.ibm.wiotp.sdk.devicemgmt.LogSeverity;
import com.ibm.wiotp.sdk.devicemgmt.DeviceAction.Status;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.wiotp.sdk.devicemgmt.gateway.ManagedGateway;
import com.ibm.wiotp.sdk.util.LoggerUtility;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayManagementTest {
	private static final String CLASS_NAME = GatewayManagementTest.class.getName();
	private static final String APP_ID = "GWMgmtApp1";
	
	private final static long DEFAULT_ACTION_TIMEOUT = 5000;
	
	private static Random random = new Random();
	private static ManagedGateway gwClient;
	private static APIClient apiClient = null;

	private static int count = 0;

	// Attached device 
	private final static String ATTACHED_DEVICE_TYPE = "iotsampleType";
	private final static String ATTACHED_DEVICE_ID = "Arduino02";

	private static final String GW_DEVICE_TYPE = "GwMgmtType1";
	private static final String GW_DEVICE_ID = "GwMgmtDev1";
	private static final String GATEWAY_REBOOT_REQUEST;
	private static final String GATEWAY_FACTORYRESET_REQUEST;
	private static final String GATEWAY_FIRMWARE_DOWNLOAD_REQUEST;
	private static final String GATEWAY_FIRMWARE_UPDATE_REQUEST;
	
	private static final String ATTACHED_DEVICE_REBOOT_REQUEST;
	private static final String ATTACHED_DEVICE_FACTORYRESET_REQUEST;
	private static final String ATTACHED_DEVICE_FIRMWARE_DOWNLOAD_REQUEST;
	private static final String ATTACHED_DEVICE_FIRMWARE_UPDATE_REQUEST;

	private static DeviceHandlerSample actionHandler = new DeviceHandlerSample();
	private static FirmwareHandlerSample firmwareHandler = new FirmwareHandlerSample();

	
	static {
		
		GATEWAY_REBOOT_REQUEST = "{\"action\": \"device/reboot\","
				+ "\"devices\": [ {\"typeId\": \"" + GW_DEVICE_TYPE +"\","
				+ "\"deviceId\": \"" + GW_DEVICE_ID + "\"}]}";
		
		GATEWAY_FACTORYRESET_REQUEST = "{\"action\": \"device/factoryReset\","
				+ "\"devices\": [ {\"typeId\": \"" + GW_DEVICE_TYPE +"\","
				+ "\"deviceId\": \"" + GW_DEVICE_ID + "\"}]}";
		
		
		GATEWAY_FIRMWARE_DOWNLOAD_REQUEST = "{\"action\": \"firmware/download\", \"parameters\": [{\"name\": \"version\", \"value\": \"0.1.10\" }," +
				"{\"name\": \"name\", \"value\": \"RasPi01 firmware\"}, {\"name\": \"verifier\", \"value\": \"123df\"}," +
				"{\"name\": \"uri\",\"value\": \"https://github.com/ibm-messaging/iot-raspberrypi/releases/download/1.0.2.1/iot_1.0-2_armhf.deb\"}" +
				"],\"devices\": [{\"typeId\": \"" + GW_DEVICE_TYPE + "\",\"deviceId\": \"" + GW_DEVICE_ID + "\"}]}";
		
		GATEWAY_FIRMWARE_UPDATE_REQUEST = "{\"action\": \"firmware/update\", \"devices\": [{\"typeId\": \"" + GW_DEVICE_TYPE + "\",\"deviceId\": \"" + GW_DEVICE_ID + "\"}]}";
		
		ATTACHED_DEVICE_REBOOT_REQUEST = "{\"action\": \"device/reboot\","
				+ "\"devices\": [ {\"typeId\": \"" + ATTACHED_DEVICE_TYPE +"\","
				+ "\"deviceId\": \"" + ATTACHED_DEVICE_ID + "\"}]}";
		
		ATTACHED_DEVICE_FACTORYRESET_REQUEST = "{\"action\": \"device/factoryReset\","
				+ "\"devices\": [ {\"typeId\": \"" + ATTACHED_DEVICE_TYPE +"\","
				+ "\"deviceId\": \"" + ATTACHED_DEVICE_ID + "\"}]}";
		
		
		ATTACHED_DEVICE_FIRMWARE_DOWNLOAD_REQUEST = "{\"action\": \"firmware/download\", \"parameters\": [{\"name\": \"version\", \"value\": \"0.1.10\" }," +
				"{\"name\": \"name\", \"value\": \"RasPi01 firmware\"}, {\"name\": \"verifier\", \"value\": \"123df\"}," +
				"{\"name\": \"uri\",\"value\": \"https://github.com/ibm-messaging/iot-raspberrypi/releases/download/1.0.2.1/iot_1.0-2_armhf.deb\"}" +
				"],\"devices\": [{\"typeId\": \"" + ATTACHED_DEVICE_TYPE + "\",\"deviceId\": \"" + ATTACHED_DEVICE_ID + "\"}]}";
		
		ATTACHED_DEVICE_FIRMWARE_UPDATE_REQUEST = "{\"action\": \"firmware/update\", \"devices\": [{\"typeId\": \"" + 
												ATTACHED_DEVICE_TYPE + "\",\"deviceId\": \"" + ATTACHED_DEVICE_ID + "\"}]}";
	}
	
	/**
	 * This sample adds a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	private void addDeviceType(String deviceType) throws IoTFCReSTException {
		final String METHOD = "addDeviceType";
		if (apiClient == null) {
			return;
		}
		try {
			//System.out.println("<-- Checking if device type "+deviceType +" already created in Watson IoT Platform");
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Checking device type (" + deviceType + ")");
			boolean exist = apiClient.isDeviceTypeExist(deviceType);
			if (!exist) {
				//System.out.println("<-- Adding device type "+ deviceType + " now..");
				LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Adding device type (" + deviceType + ")");
				// device type to be created in WIoTP
				apiClient.addDeviceType(deviceType, deviceType, null, null);
			}
		} catch(IoTFCReSTException e) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "IoTFCReSTException HTTP code(" + e.getHttpCode() + ") Response(" + e.getResponse() + ")");
			//System.err.println("ERROR: unable to add manually device type " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a device under the given gateway using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void addDevice(String deviceType, String deviceId) throws IoTFCReSTException {
		final String METHOD = "addDevice";
		if (apiClient == null) {
			return;
		}
		try {
			//System.out.println("<-- Checking if device " + deviceId +" with deviceType " +
			//		deviceType +" exists in Watson IoT Platform");
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Checking device ID (" + deviceId + ")");
			boolean exist = apiClient.isDeviceExist(deviceType, deviceId);
			if (!exist) {
				LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Adding device ID (" + deviceId + ")");
				apiClient.registerDeviceUnderGateway(deviceType, deviceId,
						gwClient.getGWDeviceType(), 
						gwClient.getGWDeviceId());
			}
		} catch (IoTFCReSTException e) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "IoTFCReSTException HTTP code(" + e.getHttpCode() + ") Response(" + e.getResponse() + ")");
			//System.out.println("ERROR: unable to add manually device " + deviceId);
		}
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
		
		// Register attached device type and attached device
		JsonObject jsonType = new JsonObject();
		jsonType.addProperty("id", ATTACHED_DEVICE_TYPE);
		try {
			apiClient.addDeviceType(jsonType);
			LoggerUtility.info(CLASS_NAME, METHOD, "Type " + ATTACHED_DEVICE_TYPE + " created.");
		} catch (IoTFCReSTException e) {
			String failMsg = "addDeviceType HTTP code " + e.getHttpCode() + " response " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
		}
		
		try {
			apiClient.registerDeviceUnderGateway(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, GW_DEVICE_TYPE, GW_DEVICE_ID);
			LoggerUtility.info(CLASS_NAME, METHOD, "Device " + ATTACHED_DEVICE_ID + " created.");
		} catch (IoTFCReSTException e) {
			String failMsg = "registerDeviceUnderGateway HTTP code " + e.getHttpCode() + " response " + e.getResponse();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);			
		}
		
		// Create managed gateway client
		DeviceFirmware firmware = new DeviceFirmware.Builder().
				version("1.0.1").
				name("iot-arm.deb").
				url("").
				verifier("12345").
				state(FirmwareState.IDLE).				
				build();
		
		/**
		 * Create a DeviceMetadata object
		 */
		JsonObject data = new JsonObject();
		data.addProperty("customField", "customValue");
		//DeviceMetadata metadata = new DeviceMetadata(data);
		
		DeviceData deviceData = new DeviceData.Builder().
						 //deviceInfo(deviceInfo).
						 deviceFirmware(firmware).
						 //metadata(metadata).
						 build();
		
		Properties gwProps = TestEnv.getDeviceProperties(ATTACHED_DEVICE_TYPE, GW_DEVICE_ID);
		gwProps.setProperty("Gateway-Type", GW_DEVICE_TYPE);
		gwProps.setProperty("Gateway-ID", GW_DEVICE_ID);
		gwProps.setProperty("Authentication-Token", TestEnv.getGatewayToken());
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
		
		if (gwClient.isConnected()) {
			gwClient.subscribeToGatewayNotification(DEFAULT_ACTION_TIMEOUT);
		}
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		final String METHOD = "oneTimeTearDown";
		LoggerUtility.info(CLASS_NAME, METHOD, "Cleaning up...");
		if (gwClient != null && gwClient.isConnected()) {
			try {
				gwClient.disconnect();
				gwClient.close();
				gwClient = null;
			} catch (Exception e) {
				LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Exception", e);
				e.printStackTrace();
			}
		}
		
		if (apiClient != null) {
			try {
				apiClient.deleteDevice(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID);
			} catch (IoTFCReSTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		try {
				apiClient.deleteDeviceType(ATTACHED_DEVICE_TYPE);
			} catch (IoTFCReSTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	
	private static class FirmwareHandlerSample extends DeviceFirmwareHandler {
		
		private volatile String name;
		private volatile String version;
		private volatile String url;
		private volatile String verifier;
		
		private volatile boolean firmwareUpdateCalled = false;
		
		@Override
		public void downloadFirmware(final DeviceFirmware deviceFirmware) {
			System.out.println("Firmware download request received");
			new Thread() {
				public void run() {
				
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
					deviceFirmware.setState(FirmwareState.DOWNLOADED);
					
					name = deviceFirmware.getName();
					version = deviceFirmware.getVersion();
					url = deviceFirmware.getUrl();
					verifier = deviceFirmware.getVerifier();
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// Fake completion
					//deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
					deviceFirmware.setState(FirmwareState.DOWNLOADED);
				}
			}.start();
		}

		@Override
		public void updateFirmware(final DeviceFirmware deviceFirmware) {
			System.out.println("Firmware update request received");
			new Thread() {
				public void run() {
				
					firmwareUpdateCalled = true;
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// Pretend that the update is successful
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
					deviceFirmware.setState(FirmwareState.IDLE);
				}
			}.start();
		}

		private void clear() {
			this.firmwareUpdateCalled = false;
			this.name = null;
			this.url = null;
			this.verifier = null;
			this.version = null;
		}
	}
	
	private static class DeviceHandlerSample extends DeviceActionHandler {
		
		private volatile boolean reboot = false;
		private volatile boolean factoryReset = false;
		private ManagedGateway gwClient;
		private boolean isGateway = true;
		
		@Override
		public void handleReboot(final DeviceAction action) {
			System.out.println("Reboot request initiated");
			action.setStatus(Status.ACCEPTED);
			new Thread() {
				public void run() {
					try {
						Thread.sleep(5000);
					
						boolean status = false;
						if(isGateway) {
							status = gwClient.sendGatewayManageRequest(0,  true, true);
						} else {
							status = gwClient.sendDeviceManageRequest(action.getTypeId(), action.getDeviceId(), 0,  true, true);
						}
						System.out.println("sent a manage request : " + status);
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					reboot = true;
				}
			}.start();
			
		}
		
		@Override
		public void handleFactoryReset(final DeviceAction action) {
			System.out.println("Factory reset initiated");
			action.setStatus(Status.ACCEPTED);
			new Thread() {
				public void run() {
					try {
						Thread.sleep(5000);
						boolean status = false;
						if(isGateway) {
							status = gwClient.sendGatewayManageRequest(0,  true, true);
						} else {
							status = gwClient.sendDeviceManageRequest(action.getTypeId(), action.getDeviceId(), 0,  true, true);
						}
						System.out.println("sent a manage request : " + status);
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					factoryReset = true;
				}

			}.start();
		}
		
		private void clear() {
			this.factoryReset = false;
			this.reboot = false;
			this.isGateway = false;
		}
	}
	

	@Test
	public void test08RebootRequest() throws Exception {
		actionHandler.isGateway = true;
		
		actionHandler.clear();
		try {
			//gwClient.connect();
			gwClient.sendGatewayManageRequest(0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		actionHandler.gwClient = gwClient;
		try {
			gwClient.addDeviceActionHandler(actionHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject download = (JsonObject) new JsonParser().parse(GATEWAY_REBOOT_REQUEST);
		System.out.println(download);
		boolean response = apiClient.initiateDeviceManagementRequest(download);

		System.out.println(response);
		int counter = 0;
		// wait for sometime
		while(counter <= 20) {
			if(actionHandler.reboot) {
				break;
			}
			Thread.sleep(1000);
			counter++;
		}
		
		assertTrue("The device reboot request is not received", actionHandler.reboot);
	}

	@Test
	public void test09FactoryResetRequest() throws Exception {
		actionHandler.clear();
		actionHandler.isGateway = true;
		
		try {
			//gwClient.connect();
			gwClient.sendGatewayManageRequest(0, false, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			gwClient.addDeviceActionHandler(actionHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject factory = (JsonObject) new JsonParser().parse(GATEWAY_FACTORYRESET_REQUEST);
		System.out.println(factory);
		boolean response = apiClient.initiateDeviceManagementRequest(factory);

		System.out.println(response);
		int counter = 0;
		// wait for sometime
		while(counter <= 20) {
			if(actionHandler.factoryReset) {
				break;
			}
			Thread.sleep(1000);
			counter++;
		}
		
		assertTrue("The device factory reset request is not received", actionHandler.factoryReset);
	}
	
	@Test
	public void test081RebootRequest() throws Exception {
		
		actionHandler.clear();
		actionHandler.isGateway = false;
		try {
			//gwClient.connect();
			gwClient.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, 0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		actionHandler.gwClient = gwClient;
		try {
			gwClient.addDeviceActionHandler(actionHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject download = (JsonObject) new JsonParser().parse(ATTACHED_DEVICE_REBOOT_REQUEST);
		System.out.println(download);
		boolean response = apiClient.initiateDeviceManagementRequest(download);

		System.out.println(response);
		int counter = 0;
		// wait for sometime
		while(counter <= 20) {
			if(actionHandler.reboot) {
				break;
			}
			Thread.sleep(1000);
			counter++;
		}
		
		assertTrue("The device reboot request is not received", actionHandler.reboot);
	}

	@Test
	public void test091FactoryResetRequest() throws Exception {
		actionHandler.clear();
		actionHandler.isGateway = false;
		
		try {
			//gwClient.connect();
			gwClient.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, 0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			gwClient.addDeviceActionHandler(actionHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject factory = (JsonObject) new JsonParser().parse(ATTACHED_DEVICE_FACTORYRESET_REQUEST);
		System.out.println(factory);
		boolean response = apiClient.initiateDeviceManagementRequest(factory);

		System.out.println(response);
		int counter = 0;
		// wait for sometime
		while(counter <= 20) {
			if(actionHandler.factoryReset) {
				break;
			}
			Thread.sleep(1000);
			counter++;
		}
		
		assertTrue("The device factory reset request is not received", actionHandler.factoryReset);
	}


	@Test
	public void test06FirmwareDownload() throws Exception {
		
		try {
			//gwClient.connect();
			gwClient.sendGatewayManageRequest(0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			gwClient.addFirmwareHandler(firmwareHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject download = (JsonObject) new JsonParser().parse(GATEWAY_FIRMWARE_DOWNLOAD_REQUEST);
		System.out.println(download);
		boolean response = apiClient.initiateDeviceManagementRequest(download);

		System.out.println(response);
		// wait for sometime
		Thread.sleep(1000 * 20);
		
		assertTrue("The firmware request/parameters not received", "123df".equals(firmwareHandler.verifier));
		assertTrue("The firmware request/parameters not received", "RasPi01 firmware".equals(firmwareHandler.name));
		assertTrue("The firmware request/parameters not received", "0.1.10".equals(firmwareHandler.version));
		assertTrue("The firmware request/parameters not received", 
				"https://github.com/ibm-messaging/iot-raspberrypi/releases/download/1.0.2.1/iot_1.0-2_armhf.deb".equals(firmwareHandler.url));
	}


	@Test
	public void test07FirmwareUpdate() throws Exception {
		
		firmwareHandler.clear();
		
		try {
			//gwClient.connect();
			gwClient.sendGatewayManageRequest(0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			gwClient.addFirmwareHandler(firmwareHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject update = (JsonObject) new JsonParser().parse(GATEWAY_FIRMWARE_UPDATE_REQUEST);
		System.out.println(update);
		boolean response = apiClient.initiateDeviceManagementRequest(update);

		System.out.println(response);
		// wait for sometime
		Thread.sleep(1000 * 20);
		
		assertTrue("The firmware request/parameters not received", firmwareHandler.firmwareUpdateCalled);
	}
	
	@Test
	public void test061FirmwareDownload() throws Exception {
		
		firmwareHandler.clear();
		try {
			//gwClient.connect();
			gwClient.sendGatewayManageRequest(0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			gwClient.addFirmwareHandler(firmwareHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject download = (JsonObject) new JsonParser().parse(ATTACHED_DEVICE_FIRMWARE_DOWNLOAD_REQUEST);
		System.out.println(download);
		boolean response = apiClient.initiateDeviceManagementRequest(download);

		System.out.println(response);
		// wait for sometime
		Thread.sleep(1000 * 20);
		
		assertTrue("The firmware request/parameters not received for attached device", "123df".equals(firmwareHandler.verifier));
		assertTrue("The firmware request/parameters not received for attached device", "RasPi01 firmware".equals(firmwareHandler.name));
		assertTrue("The firmware request/parameters not received for attached device", "0.1.10".equals(firmwareHandler.version));
		assertTrue("The firmware request/parameters not received for attached device", 
				"https://github.com/ibm-messaging/iot-raspberrypi/releases/download/1.0.2.1/iot_1.0-2_armhf.deb".equals(firmwareHandler.url));
	}


	@Test
	public void test071FirmwareUpdate() throws Exception {
		
		firmwareHandler.clear();
		
		try {
			//gwClient.connect();
			gwClient.sendGatewayManageRequest(0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			gwClient.addFirmwareHandler(firmwareHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject update = (JsonObject) new JsonParser().parse(ATTACHED_DEVICE_FIRMWARE_UPDATE_REQUEST);
		System.out.println(update);
		boolean response = apiClient.initiateDeviceManagementRequest(update);

		System.out.println(response);
		// wait for sometime
		Thread.sleep(1000 * 20);
		
		assertTrue("The firmware request/parameters not received for attacheddevice", firmwareHandler.firmwareUpdateCalled);
	}


	@Test
	public void test01ManageRequest() {
		final String METHOD = "test01ManageRequest";
		boolean status = false;
		try {
			// Gateway manage request
			LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing sendGatewayManageRequest(0, false, true)");
			status = gwClient.sendGatewayManageRequest(0, false, true);
			assertTrue("Gateway Manage request is unsuccessfull", status);
			
			LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing sendDeviceManageRequest(" + ATTACHED_DEVICE_TYPE + "," + ATTACHED_DEVICE_ID + ",0,true,true)");
			status = gwClient.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, 0, true, true);
			assertTrue("Device Manage request is unsuccessfull", status);
			
			// test overloaded method
			/**
			 * Create a DeviceMetadata object
			 */
			JsonObject data = new JsonObject();
			data.addProperty("customField", "customValue");
			//DeviceMetadata metadata = new DeviceMetadata(data);
			
			DeviceData deviceData = new DeviceData.Builder().
							 //deviceInfo(deviceInfo).
							 deviceFirmware(null).
							 //metadata(metadata).
							 build();
			
			LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing sendDeviceManageRequest(" + ATTACHED_DEVICE_TYPE + "," + ATTACHED_DEVICE_ID + "," + deviceData + ",0,true,true)");
			status = gwClient.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, deviceData, 0, true, true);
			assertTrue("Device Manage request is unsuccessfull", status);
			
			DeviceData devicedata = gwClient.getGatewayDeviceData();
			if(devicedata == null) {
				fail("Device data must not be null");
			}
			
			devicedata = gwClient.getAttachedDeviceData(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID);
			if(devicedata == null) {
				fail("Device data must not be null");
			}

		} catch (MqttException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void test02UnManageRequest() {
		
		final String METHOD = "test02UnManageRequest";
		
		boolean status = false;
		try {
			LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing sendGatewayManageRequest(0, false, true)");
			status = gwClient.sendGatewayManageRequest(0, false, true);
			LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing sendGatewayUnmanageRequet()");
			status = gwClient.sendGatewayUnmanageRequet();
			assertTrue("Gateway UnManage request is unsuccessfull", status);
			
			LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing sendDeviceManageRequest(" + ATTACHED_DEVICE_TYPE + "," + ATTACHED_DEVICE_ID + ",0,true,true)");
			status = gwClient.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, 0, true, true);
			LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing sendDeviceUnmanageRequet(" + ATTACHED_DEVICE_TYPE + "," + ATTACHED_DEVICE_ID + ")");
			status = gwClient.sendDeviceUnmanageRequet(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID);
			assertTrue("Device UnManage request is unsuccessfull", status);
			
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void test03LocationUpdate() throws MqttException {
		
		final String METHOD = "test03LocationUpdate";
		
		LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing sendGatewayManageRequest(0, false, true)");
		gwClient.sendGatewayManageRequest(0, false, true);
			
		double latitude = random.nextDouble() + 30;
		double longitude = random.nextDouble() - 98;
		double elevation = (double)random.nextInt(100);
		
		LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing updateGatewayLocation");
		int rc = gwClient.updateGatewayLocation(latitude, longitude, elevation);
		assertTrue("Gateway Location update is unsuccessfull", rc==200);
		
		// user overloaded method
		LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing updateGatewayLocation datetime");
		rc = gwClient.updateGatewayLocation(latitude, longitude, elevation, new Date());
		assertTrue("Gateway location update is unsuccessfull", rc==200);
		
		// Test device's
		LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing sendDeviceManageRequest(" + ATTACHED_DEVICE_TYPE + "," + ATTACHED_DEVICE_ID + ",0,true,true)");
		gwClient.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, 0, true, true);
		
		LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing updateDeviceLocation");
		rc = gwClient.updateDeviceLocation(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, latitude, longitude, elevation);
		assertTrue("device location update is unsuccessfull", rc==200);
		
		LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Testing updateDeviceLocation datetime");
		rc = gwClient.updateDeviceLocation(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, latitude, longitude, elevation, new Date());
		assertTrue("device location update is unsuccessfull", rc==200);
	}
	
	@Test
	public void test04Errorcodes() throws MqttException {
		gwClient.sendGatewayManageRequest(0, false, true);
		
		int errorCode = random.nextInt(500);
		int rc = gwClient.addGatewayErrorCode(errorCode);
		assertTrue("Gateway Errorcode addition unsuccessfull", rc==200);
		
		// Let us clear the errorcode now
		rc = gwClient.clearGatewayErrorCodes();
		assertTrue("clear Gateway Errorcode operation is unsuccessfull", rc==200);
		
		// Test device's
		gwClient.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, 0, true, true);
		rc = gwClient.addDeviceErrorCode(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, 9);
		assertTrue("Device Errorcode addition unsuccessfull", rc==200);
		
		// Let us clear the errorcode now
		rc = gwClient.clearDeviceErrorCodes(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID);
		assertTrue("clear Device Errorcode operation is unsuccessfull", rc==200);
		

	}

	@Test
	public void test05LogMessages() throws MqttException {
		gwClient.sendGatewayManageRequest(0, false, true);
		
		String message = "Log event 1";
		Date timestamp = new Date();
		LogSeverity severity = LogSeverity.informational;
		int rc = gwClient.addGatewayLog(message, timestamp, severity);
		assertTrue("Gateway Log addition unsuccessfull", rc==200);
		
		// Use overloaded methods
		rc = gwClient.addGatewayLog("Log event with data", timestamp, severity, "Sample data");
		assertTrue("Gateway Log addition unsuccessfull", rc==200);

		// Let us clear the errorcode now
		rc = gwClient.clearGatewayLogs();
		assertTrue("clear Log operation is unsuccessfull", rc==200);
		
		// Test device's
		gwClient.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, 0, true, true);
		rc = gwClient.addDeviceLog(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, message, timestamp, severity);
		assertTrue("Gateway Log addition unsuccessfull", rc==200);
		
		// Use overloaded methods
		rc = gwClient.addDeviceLog(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, "Log event with data", timestamp, severity, "Sample data");
		assertTrue("Gateway Log addition unsuccessfull", rc==200);

		// Let us clear the errorcode now
		rc = gwClient.clearDeviceLogs(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID);
		assertTrue("clear Log operation is unsuccessfull", rc==200);

	}

	
	/**
	 * This method builds the device objects required to create the
	 * ManagedClient
	 * 
	 * @param propertiesFile
	 * @throws Exception
	 */
	private void createManagedClient(String propertiesFile) throws Exception {
		final String METHOD = "createManagedClient";
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(GatewayManagementTest.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		DeviceFirmware firmware = new DeviceFirmware.Builder().
				version("1.0.1").
				name("iot-arm.deb").
				url("").
				verifier("12345").
				state(FirmwareState.IDLE).				
				build();
		
		/**
		 * Create a DeviceMetadata object
		 */
		JsonObject data = new JsonObject();
		data.addProperty("customField", "customValue");
		//DeviceMetadata metadata = new DeviceMetadata(data);
		
		DeviceData deviceData = new DeviceData.Builder().
						 //deviceInfo(deviceInfo).
						 deviceFirmware(firmware).
						 //metadata(metadata).
						 build();
		
		gwClient = new ManagedGateway(props, deviceData);
		gwClient.setGatewayCallback(new GatewayCallbackTest(props));
		gwClient.connect();
		LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "connected (" + gwClient.isConnected() + ")");
		if (gwClient.isConnected()) {
			gwClient.subscribeToGatewayNotification(DEFAULT_ACTION_TIMEOUT);
		}
		
		
		/**
		 * We need APIClient to register the devicetype in Watson IoT Platform 
		 */
		apiClient = gwClient.api();
		
	}

}
