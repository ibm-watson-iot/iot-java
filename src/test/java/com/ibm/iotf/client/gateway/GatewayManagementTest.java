package com.ibm.iotf.client.gateway;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import junit.framework.TestCase;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceAction;
import com.ibm.iotf.devicemgmt.DeviceActionHandler;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceFirmwareHandler;
import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.DeviceAction.Status;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayManagementTest extends TestCase {
	private static Random random = new Random();
	private static ManagedGateway gwClient;
	private static APIClient apiClient = null;

	private static boolean setupDone = false;
	private static int count = 0;
	private final static String GATEWAY_PROPERTIES_FILE = "/gateway.properties";
	private final static String APPLICATION_PROPERTIES_FILE = "/application.properties";

	// Attached device 
	private final static String ATTACHED_DEVICE_TYPE = "iotsampleType";
	private final static String ATTACHED_DEVICE_ID = "Arduino02";

	private static final String DEVICE_TYPE;
	private static final String DEVICE_ID;
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
		createAPIClient();
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(GatewayManagementTest.class.getResourceAsStream(GATEWAY_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		DEVICE_TYPE = props.getProperty("Gateway-Type");
		DEVICE_ID = props.getProperty("Gateway-ID");
		GATEWAY_REBOOT_REQUEST = "{\"action\": \"device/reboot\","
				+ "\"devices\": [ {\"typeId\": \"" + DEVICE_TYPE +"\","
				+ "\"deviceId\": \"" + DEVICE_ID + "\"}]}";
		
		GATEWAY_FACTORYRESET_REQUEST = "{\"action\": \"device/factoryReset\","
				+ "\"devices\": [ {\"typeId\": \"" + DEVICE_TYPE +"\","
				+ "\"deviceId\": \"" + DEVICE_ID + "\"}]}";
		
		
		GATEWAY_FIRMWARE_DOWNLOAD_REQUEST = "{\"action\": \"firmware/download\", \"parameters\": [{\"name\": \"version\", \"value\": \"0.1.10\" }," +
				"{\"name\": \"name\", \"value\": \"RasPi01 firmware\"}, {\"name\": \"verifier\", \"value\": \"123df\"}," +
				"{\"name\": \"uri\",\"value\": \"https://github.com/ibm-messaging/iot-raspberrypi/releases/download/1.0.2.1/iot_1.0-2_armhf.deb\"}" +
				"],\"devices\": [{\"typeId\": \"" + DEVICE_TYPE + "\",\"deviceId\": \"" + DEVICE_ID + "\"}]}";
		
		GATEWAY_FIRMWARE_UPDATE_REQUEST = "{\"action\": \"firmware/update\", \"devices\": [{\"typeId\": \"" + DEVICE_TYPE + "\",\"deviceId\": \"" + DEVICE_ID + "\"}]}";
		
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
	

	public void setUp() {
		if(setupDone == true && gwClient.isConnected() == true) {
			return;
		}
	    // do the setup
		try {
			createManagedClient(GATEWAY_PROPERTIES_FILE);
			setupDone = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void tearDown() {
		count++;
		if(count == 13) {
			gwClient.disconnect();
		}
	}
	
	private static void createAPIClient() {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(GatewayManagementTest.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		try {
			//Instantiate the class by passing the properties file
			apiClient = new APIClient(props);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
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
	

	public void test08RebootRequest() throws Exception {
		actionHandler.isGateway = true;
		
		actionHandler.clear();
		try {
			gwClient.connect();
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

	public void test09FactoryResetRequest() throws Exception {
		actionHandler.clear();
		actionHandler.isGateway = true;
		
		try {
			gwClient.connect();
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
	
	public void test081RebootRequest() throws Exception {
		
		actionHandler.clear();
		actionHandler.isGateway = false;
		try {
			gwClient.connect();
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

	public void test091FactoryResetRequest() throws Exception {
		actionHandler.clear();
		actionHandler.isGateway = false;
		
		try {
			gwClient.connect();
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


	public void test06FirmwareDownload() throws Exception {
		
		try {
			gwClient.connect();
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


	public void test07FirmwareUpdate() throws Exception {
		
		firmwareHandler.clear();
		
		try {
			gwClient.connect();
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
	
	public void test061FirmwareDownload() throws Exception {
		
		firmwareHandler.clear();
		try {
			gwClient.connect();
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


	public void test071FirmwareUpdate() throws Exception {
		
		firmwareHandler.clear();
		
		try {
			gwClient.connect();
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


	public void test01ManageRequest() {
		boolean status = false;
		try {
			// Gateway manage request
			status = gwClient.sendGatewayManageRequest(0, false, true);
			assertTrue("Gateway Manage request is unsuccessfull", status);
			
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
			
			status = gwClient.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, deviceData, 0, true, true);
			assertTrue("Device Manage request is unsuccessfull", status);

		} catch (MqttException e) {
			fail(e.getMessage());
		}
	}
	
	public void test02UnManageRequest() {
		
		boolean status = false;
		try {
			status = gwClient.sendGatewayManageRequest(0, false, true);
			status = gwClient.sendGatewayUnmanageRequet();
			assertTrue("Gateway UnManage request is unsuccessfull", status);
			
			status = gwClient.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, 0, true, true);
			status = gwClient.sendDeviceUnmanageRequet(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID);
			assertTrue("Device UnManage request is unsuccessfull", status);
			
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
	}
	
	public void test03LocationUpdate() throws MqttException {
		gwClient.sendGatewayManageRequest(0, false, true);
			
		double latitude = random.nextDouble() + 30;
		double longitude = random.nextDouble() - 98;
		double elevation = (double)random.nextInt(100);
		
		int rc = gwClient.updateGatewayLocation(latitude, longitude, elevation);
		assertTrue("Gateway Location update is unsuccessfull", rc==200);
		
		// user overloaded method
		rc = gwClient.updateGatewayLocation(latitude, longitude, elevation, new Date());
		assertTrue("Gateway location update is unsuccessfull", rc==200);
		
		// Test device's
		gwClient.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, 0, true, true);
		rc = gwClient.updateDeviceLocation(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, latitude, longitude, elevation);
		assertTrue("device location update is unsuccessfull", rc==200);
		
		rc = gwClient.updateDeviceLocation(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, latitude, longitude, elevation, new Date());
		assertTrue("device location update is unsuccessfull", rc==200);
	}
	
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
		/**
		 * Load device properties
		 */
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(GatewayManagementTest.class.getResourceAsStream(propertiesFile));
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
		
		gwClient = new ManagedGateway(deviceProps, deviceData);
		gwClient.connect();
	}
	
	/**
	 * This method connects the device to the Watson IoT Platform
	 */
	private void connect() throws Exception {		
		gwClient.connect();
	}


}
