package com.ibm.iotf.client.device;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import junit.framework.TestCase;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceAction;
import com.ibm.iotf.devicemgmt.DeviceActionHandler;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceFirmwareHandler;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.DeviceMetadata;
import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.DeviceAction.Status;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceManagementTest extends TestCase {
	private static Random random = new Random();
	private static ManagedDevice dmClient;
	private static APIClient apiClient = null;

	private static boolean setupDone = false;
	private static int count = 0;
	private final static String DEVICE_PROPERTIES_FILE = "/device.properties";
	private final static String APPLICATION_PROPERTIES_FILE = "/application.properties";
	
	
	private static final String DEVICE_TYPE;
	private static final String DEVICE_ID;
	private static final String rebootRequestToBeInitiated;
	private static final String factoryRequestToBeInitiated;
	private static final String downloadRequest;
	private static final String updateRequest;
	
	
	static {
		createAPIClient();
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(DeviceManagementTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		DEVICE_TYPE = props.getProperty("Device-Type");
		DEVICE_ID = props.getProperty("Device-ID");
		rebootRequestToBeInitiated = "{\"action\": \"device/reboot\","
				+ "\"devices\": [ {\"typeId\": \"" + DEVICE_TYPE +"\","
				+ "\"deviceId\": \"" + DEVICE_ID + "\"}]}";
		
		factoryRequestToBeInitiated = "{\"action\": \"device/factoryReset\","
				+ "\"devices\": [ {\"typeId\": \"" + DEVICE_TYPE +"\","
				+ "\"deviceId\": \"" + DEVICE_ID + "\"}]}";
		
		
		downloadRequest = "{\"action\": \"firmware/download\", \"parameters\": [{\"name\": \"version\", \"value\": \"0.1.10\" }," +
				"{\"name\": \"name\", \"value\": \"RasPi01 firmware\"}, {\"name\": \"verifier\", \"value\": \"123df\"}," +
				"{\"name\": \"uri\",\"value\": \"https://github.com/ibm-messaging/iot-raspberrypi/releases/download/1.0.2.1/iot_1.0-2_armhf.deb\"}" +
				"],\"devices\": [{\"typeId\": \"" + DEVICE_TYPE + "\",\"deviceId\": \"" + DEVICE_ID + "\"}]}";
		
		updateRequest = "{\"action\": \"firmware/update\", \"devices\": [{\"typeId\": \"" + DEVICE_TYPE + "\",\"deviceId\": \"" + DEVICE_ID + "\"}]}";
	}
	
	private static DeviceHandlerSample actionHandler = new DeviceHandlerSample();
	private static FirmwareHandlerSample firmwareHandler = new FirmwareHandlerSample();

	public void setUp() {
		if(setupDone == true && dmClient.isConnected() == true) {
			return;
		}
	    // do the setup
		try {
			createManagedClient(DEVICE_PROPERTIES_FILE);
			setupDone = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void tearDown() {
		count++;
		if(count == 9) {
			dmClient.disconnect();
		}
	}
	
	private static void createAPIClient() {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(DeviceManagementTest.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
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
		private volatile boolean firmwaredownloaded = false;
		
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
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// Fake completion
					//deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
					deviceFirmware.setState(FirmwareState.DOWNLOADED);
					firmwaredownloaded = true;
				}
			}.start();
		}

		@Override
		public void updateFirmware(final DeviceFirmware deviceFirmware) {
			System.out.println("Firmware update request received");
			new Thread() {
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// Pretend that the update is successful
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
					deviceFirmware.setState(FirmwareState.IDLE);
					firmwareUpdateCalled = true;
				}
			}.start();
		}
	}
	
	private static class DeviceHandlerSample extends DeviceActionHandler {
		
		private volatile boolean reboot = false;
		private volatile boolean factoryReset = false;
		public ManagedDevice dmClient;
		
		@Override
		public void handleReboot(DeviceAction action) {
			System.out.println("Reboot request initiated");
			action.setStatus(Status.ACCEPTED);
			new Thread() {
				public void run() {
					try {
						Thread.sleep(2000);
						boolean status = dmClient.sendManageRequest(0,  true, true);
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
		public void handleFactoryReset(DeviceAction action) {
			System.out.println("Factory reset initiated");
			action.setStatus(Status.ACCEPTED);
			new Thread() {
				public void run() {
					try {
						Thread.sleep(2000);
						boolean status = dmClient.sendManageRequest(0,  true, true);
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
	
	}
	

	public void test08RebootRequest() throws Exception {
		
		try {
			dmClient.connect(5);
			dmClient.sendManageRequest(0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			dmClient.addDeviceActionHandler(actionHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		actionHandler.dmClient = dmClient;
		
		JsonObject jsonReboot = (JsonObject) new JsonParser().parse(rebootRequestToBeInitiated);
		System.out.println(jsonReboot);
		JsonObject response = apiClient.initiateDMRequest(jsonReboot);

		int counter = 0;
		// wait for sometime
		while(counter <= 20) {
			if(actionHandler.reboot) {
				break;
			}
			Thread.sleep(1000);
			counter++;
		}
		
		JsonObject status = apiClient.getDeviceManagementRequestStatusByDevice(response.get("reqId").getAsString(), DEVICE_TYPE, DEVICE_ID);
		System.out.println(status);
		assertEquals(0, status.get("status").getAsInt());
		assertTrue(status.get("complete").getAsBoolean());
		assertTrue("The device reboot request is not received", actionHandler.reboot);
	}

	public void test09FactoryResetRequest() throws Exception {
		
		try {
			dmClient.connect();
			dmClient.sendManageRequest(0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			dmClient.addDeviceActionHandler(actionHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		actionHandler.dmClient = dmClient;
		
		JsonObject factory = (JsonObject) new JsonParser().parse(factoryRequestToBeInitiated);
		System.out.println(factory);
		JsonObject response = apiClient.initiateDMRequest(factory);

		System.out.println(response);
		int counter = 0;
		// wait for sometime
		while(counter <= 20) {
			// For some reason the volatile doesn't work, so using the lock
			if(actionHandler.factoryReset) {
				break;
			}
			Thread.sleep(1000);
			counter++;
		}

		Thread.sleep(2000);
		JsonObject status = apiClient.getDeviceManagementRequestStatusByDevice(response.get("reqId").getAsString(), DEVICE_TYPE, DEVICE_ID);
		System.out.println(status);
		assertEquals(0, status.get("status").getAsInt());
		assertTrue(status.get("complete").getAsBoolean());
		assertTrue("The device factory reset request is not received", actionHandler.factoryReset);
	}

	public void test06FirmwareDownload() throws Exception {
		
		try {
			dmClient.connect(true);
			dmClient.sendManageRequest(0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			dmClient.addFirmwareHandler(firmwareHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject download = (JsonObject) new JsonParser().parse(downloadRequest);
		System.out.println(download);
		JsonObject response = apiClient.initiateDMRequest(download);

		System.out.println(response);
		int counter = 0;
		// wait for sometime
		while(counter <= 20) {
			// For some reason the volatile doesn't work, so using the lock
			if(firmwareHandler.firmwaredownloaded) {
				break;
			}
			Thread.sleep(1000);
			counter++;
		}

		Thread.sleep(2000);
		
		JsonObject status = apiClient.getDeviceManagementRequestStatusByDevice(response.get("reqId").getAsString(), DEVICE_TYPE, DEVICE_ID);
		System.out.println(status);
		assertEquals(0, status.get("status").getAsInt());
		assertTrue(status.get("complete").getAsBoolean());
		
		assertTrue("The firmware request/parameters not received", "123df".equals(firmwareHandler.verifier));
		assertTrue("The firmware request/parameters not received", "RasPi01 firmware".equals(firmwareHandler.name));
		assertTrue("The firmware request/parameters not received", "0.1.10".equals(firmwareHandler.version));
		assertTrue("The firmware request/parameters not received", 
				"https://github.com/ibm-messaging/iot-raspberrypi/releases/download/1.0.2.1/iot_1.0-2_armhf.deb".equals(firmwareHandler.url));
	}


	public void test07FirmwareUpdate() throws Exception {
		
		try {
			dmClient.connect();
			dmClient.sendManageRequest(0, true, true);
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			dmClient.addFirmwareHandler(firmwareHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonObject update = (JsonObject) new JsonParser().parse(updateRequest);
		System.out.println(update);
		JsonObject response = apiClient.initiateDMRequest(update);
		
		System.out.println(response);
		int counter = 0;
		// wait for sometime
		while(counter <= 20) {
			// For some reason the volatile doesn't work, so using the lock
			if(firmwareHandler.firmwareUpdateCalled) {
				break;
			}
			Thread.sleep(1000);
			counter++;
		}

		Thread.sleep(2000);
		
		JsonObject status = apiClient.getDeviceManagementRequestStatusByDevice(response.get("reqId").getAsString(), DEVICE_TYPE, DEVICE_ID);
		System.out.println(status);
		assertEquals(0, status.get("status").getAsInt());
		assertTrue(status.get("complete").getAsBoolean());
		assertTrue("The firmware request/parameters not received", firmwareHandler.firmwareUpdateCalled);
	}

	public void test01ManageRequest() {
		boolean status = false;
		try {
			status = dmClient.sendManageRequest(0, true, false);
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
		boolean status = false;
		try {
			status = dmClient.sendManageRequest(0, true, false);
			status = dmClient.sendUnmanageRequest();
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		assertTrue("UnManage request is unsuccessfull", status);
	}
	
	public void test03LocationUpdate() throws MqttException {
		dmClient.sendManageRequest(0, true, false);
		
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
	
	public void test04Errorcodes() throws MqttException {
		dmClient.sendManageRequest(0, true, false);
		
		int errorCode = random.nextInt(500);
		int rc = dmClient.addErrorCode(errorCode);
		assertTrue("Errorcode addition unsuccessfull", rc==200);
		
		// Let us clear the errorcode now
		rc = dmClient.clearErrorCodes();
		assertTrue("clear Errorcode operation is unsuccessfull", rc==200);
	}

	public void test05LogMessages() throws MqttException {
		dmClient.sendManageRequest(0, true, false);
		
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
			deviceProps.load(DeviceManagementTest.class.getResourceAsStream(propertiesFile));
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
	
	/**
	 * This method connects the device to the Watson IoT Platform
	 */
	private void connect() throws Exception {		
		dmClient.connect();
	}


}
