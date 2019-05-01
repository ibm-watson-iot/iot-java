package com.ibm.iotf.client.gateway;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.wiotp.sdk.CustomAction;
import com.ibm.wiotp.sdk.IoTFCReSTException;
import com.ibm.wiotp.sdk.CustomAction.Status;
import com.ibm.wiotp.sdk.api.APIClient;
import com.ibm.wiotp.sdk.devicemgmt.CustomActionHandler;
import com.ibm.wiotp.sdk.devicemgmt.DeviceData;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.wiotp.sdk.devicemgmt.gateway.ManagedGateway;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayDMETest {
	private static APIClient apiClient = null;
	private static boolean setupDone = false;
	private static int count = 0;
	private static ManagedGateway gwClient;
		private final static String GATEWAY_PROPERTIES_FILE = "/gateway.properties";
	private final static String APPLICATION_PROPERTIES_FILE = "/application.properties";

	// Attached device 
	private final static String ATTACHED_DEVICE_TYPE = "iotsampleType";
	private final static String ATTACHED_DEVICE_ID = "Arduino02";

	private static String BUNDLE_TO_BE_ADDED = "{\"bundleId\": \"example-dme-actions-v1\",\"displayName\": "
			+ "{\"en_US\": \"example-dme Actions v1\"},\"version\": \"1.0\",\"actions\": "
			+ "{\"updatePublishInterval\": {\"actionDisplayName\": {\"en_US\": \"Update Pubslish Interval\"},"
			+ "\"parameters\": [{\"name\": \"publishInvertval\",\"value\": 5,"
			+ "\"required\": \"false\"}]}}}";

	private static String GATEWAY_DME_REQUEST;
	private static String ATTACHED_DEVICE_DME_REQUEST;
	
	private static final String DEVICE_TYPE;
	private static final String DEVICE_ID;	
	
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
		
		GATEWAY_DME_REQUEST = "{\"action\": \"example-dme-actions-v1/updatePublishInterval\", \"parameters\": [{\"name\": \"PublishInterval\", \"value\": 5}],\"devices\": [{" +
				"\"typeId\":\"" + DEVICE_TYPE + "\",\"deviceId\":\"" + DEVICE_ID + "\"}]}";
		
		ATTACHED_DEVICE_DME_REQUEST = "{\"action\": \"example-dme-actions-v1/updatePublishInterval\", \"parameters\": [{\"name\": \"PublishInterval\", \"value\": 5}],\"devices\": [{" +
				"\"typeId\":\"" + ATTACHED_DEVICE_TYPE + "\",\"deviceId\":\"" + ATTACHED_DEVICE_ID + "\"}]}";
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
		if(count == 4) {
			gwClient.disconnect();
		}
	}
	
	private static void createAPIClient() {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(GatewayDMETest.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
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
		
		private AtomicBoolean customAction = new AtomicBoolean(false);
		private volatile String deviceId = "";
		
		@Override
		public void handleCustomAction(CustomAction action) {
			System.out.println("Custom Action initiated "+action.getActionId() + " "+action.getDeviceId());
			// check the deviceID and then set the status accordingly
			if(action.getDeviceId().equals(deviceId)) {
				customAction.set(true);
				action.setStatus(Status.OK);
			}
		}
	}
	

	public void test03GatewayCustomActionRequest() throws Exception {
		try {
			List<String> bundleIds = new ArrayList<String>();
			bundleIds.add("example-dme-actions-v1");
			gwClient.connect(5);
			gwClient.sendGatewayManageRequest(0, false, false, bundleIds);
			// add the package if its not added already
			createDeviceManagementExtensionPkg();
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			actionHandler.deviceId = gwClient.getGWDeviceId();
			actionHandler.customAction.set(false);
			gwClient.addCustomActionHandler(actionHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonParser parser = new JsonParser();
		JsonObject jsonReq = (JsonObject) parser.parse(GATEWAY_DME_REQUEST);
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
			if(actionHandler.customAction.get()) {
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
		
		assertTrue("The Gateway DME request is not received", actionHandler.customAction.get());
	}
	
	public void test04DeviceCustomActionRequest() throws Exception {
		try {
			List<String> bundleIds = new ArrayList<String>();
			bundleIds.add("example-dme-actions-v1");
			gwClient.connect(5);
			gwClient.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, 0, false, false, bundleIds);
			// add the package if its not added already
			createDeviceManagementExtensionPkg();
		} catch (MqttException e) {
			fail(e.getMessage());
		}
		
		try {
			actionHandler.deviceId = ATTACHED_DEVICE_ID;
			actionHandler.customAction.set(false);
			gwClient.addCustomActionHandler(actionHandler);
		} catch(Exception e) {
			// ignore the error
		}
		
		JsonParser parser = new JsonParser();
		JsonObject jsonReq = (JsonObject) parser.parse(ATTACHED_DEVICE_DME_REQUEST);
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
			if(actionHandler.customAction.get()) {
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
		
		assertTrue("The device DME request is not received", actionHandler.customAction.get());
	}


	public void test01ManageRequest() {
		boolean status = false;
		try {
			List<String> bundleIds = new ArrayList<String>();
			bundleIds.add("example-dme-actions-v1");
			gwClient.connect(5);
			status = gwClient.sendGatewayManageRequest(0, false, false, bundleIds);
			DeviceData devicedata = gwClient.getGatewayDeviceData();
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
			List<String> bundleIds = new ArrayList<String>();
			bundleIds.add("example-dme-actions-v1");
			gwClient.connect(5);
			status = gwClient.sendGatewayManageRequest(0, false, false, bundleIds);
			status = gwClient.sendGatewayUnmanageRequet();
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

}
