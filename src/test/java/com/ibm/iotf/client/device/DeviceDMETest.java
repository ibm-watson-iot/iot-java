package com.ibm.iotf.client.device;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.CustomAction;
import com.ibm.iotf.client.CustomAction.Status;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.CustomActionHandler;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.DeviceMetadata;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceDMETest {
	private static ManagedDevice dmClient;
	private static APIClient apiClient = null;

	private static boolean setupDone = false;
	private static int count = 0;
	private final static String DEVICE_PROPERTIES_FILE = "/device.properties";
	private final static String APPLICATION_PROPERTIES_FILE = "/application.properties";
	
	private static String BUNDLE_TO_BE_ADDED = "{\"bundleId\": \"example-dme-actions-v1\",\"displayName\": "
			+ "{\"en_US\": \"example-dme Actions v1\"},\"version\": \"1.0\",\"actions\": "
			+ "{\"updatePublishInterval\": {\"actionDisplayName\": {\"en_US\": \"Update Pubslish Interval\"},"
			+ "\"parameters\": [{\"name\": \"publishInvertval\",\"value\": 5,"
			+ "\"required\": \"false\"}]}}}";
	private static String DME_REQUEST;
	
	private static final String DEVICE_TYPE;
	private static final String DEVICE_ID;	
	
	static {
		createAPIClient();
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(DeviceDMETest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		
		DEVICE_TYPE = props.getProperty("Device-Type");
		DEVICE_ID = props.getProperty("Device-ID");
		DME_REQUEST = "{\"action\": \"example-dme-actions-v1/updatePublishInterval\", \"parameters\": [{\"name\": \"PublishInterval\", \"value\": 5}],\"devices\": [{" +
				"\"typeId\":\"" + DEVICE_TYPE + "\",\"deviceId\":\"" + DEVICE_ID + "\"}]}";
	}
	
	private static CustomActionHandlerSample actionHandler = new CustomActionHandlerSample();

	private void createDeviceManagementExtensionPkg() {
		System.out.println("Add DME Extension package");
		try {
			JsonObject response = this.apiClient.addDeviceManagementExtension(BUNDLE_TO_BE_ADDED);
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
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
		if(count == 3) {
			dmClient.disconnect();
		}
	}
	
	private static void createAPIClient() {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(DeviceDMETest.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
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
	
	private static class CustomActionHandlerSample extends CustomActionHandler {
		
		private volatile boolean customAction = false;
		
		@Override
		public void handleCustomAction(CustomAction action) {
			System.out.println("Custom Action initiated "+action.getActionId());
			customAction = true;
			action.setStatus(Status.OK);
		}
	}
	

	public void test03CustomActionRequest() throws Exception {
		try {
			dmClient.connect(5);
			dmClient.sendManageRequest(0, false, false, "example-dme-actions-v1");
			// add the package if its not added already
			createDeviceManagementExtensionPkg();
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			dmClient.addCustomActionHandler(actionHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonParser parser = new JsonParser();
		JsonObject jsonReq = (JsonObject) parser.parse(DME_REQUEST);
		System.out.println(jsonReq);
		boolean status = false;
		
		try {
			status = this.apiClient.initiateDeviceManagementRequest(jsonReq);
		} catch (IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		System.out.println(status);
		int counter = 0;
		// wait for sometime
		while(counter <= 20) {
			if(actionHandler.customAction) {
				break;
			}
			Thread.sleep(1000);
			counter++;
		}
		
		JsonObject response = apiClient.getAllDeviceManagementRequests();
		JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
		JsonElement request = requests.get(requests.size() - 1);
		System.out.println("get status of the DM request .. "+request.getAsJsonObject().get("id").getAsString());
		System.out.println(apiClient.getDeviceManagementRequestStatus(request.getAsJsonObject().get("id").getAsString()));
		
		assertTrue("The device DME request is not received", actionHandler.customAction);
	}

	public void test01ManageRequest() {
		boolean status = false;
		try {
			status = dmClient.sendManageRequest(0, false, false, "example-dme-actions-v1");
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
			status = dmClient.sendManageRequest(0, true, true, "example-dme-actions-v1");
			status = dmClient.sendUnmanageRequest();
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		assertTrue("UnManage request is unsuccessfull", status);
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
			deviceProps.load(DeviceDMETest.class.getResourceAsStream(propertiesFile));
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
