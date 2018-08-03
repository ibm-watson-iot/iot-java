package com.ibm.iotf.sample.dme.application;

import java.io.IOException;
import java.util.Properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;

public class SampleDMEApplication {
	
	private static String BUNDLE_TO_BE_ADDED = "{\"bundleId\": \"example-dme-actions-v1\",\"displayName\": "
			+ "{\"en_US\": \"example-dme Actions v1\"},\"version\": \"1.0\",\"actions\": "
			+ "{\"updatePublishInterval\": {\"actionDisplayName\": {\"en_US\": \"Update Pubslish Interval\"},"
			+ "\"parameters\": [{\"name\": \"publishInvertval\",\"value\": 5,"
			+ "\"required\": \"false\"}]}}}";
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	private APIClient apiClient = null;
	private String deviceId = "Device01";
	private String deviceType = "iotsample-deviceType";
	
	SampleDMEApplication(String filePath) {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(SampleDMEApplication.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		try {
			//Instantiate the class by passing the properties file
			this.apiClient = new APIClient(props);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) throws Exception {
		SampleDMEApplication sample = new SampleDMEApplication(PROPERTIES_FILE_NAME);
		sample.createDeviceManagementExtensionPkg();
		sample.initiateDMERequest();
	}
	
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
	
	private void initiateDMERequest() {
		String req = "{\"action\": \"example-dme-actions-v1/updatePublishInterval\", \"parameters\": [{\"name\": \"PublishInterval\", \"value\": 5}],\"devices\": [{" +
					"\"typeId\":\"" + deviceType + "\",\"deviceId\":\"" + deviceId + "\"}]}";
		System.out.println(req);
		JsonParser parser = new JsonParser();
		JsonObject jsonReq = (JsonObject) parser.parse(req);
		
		try {
			this.apiClient.initiateDeviceManagementRequest(jsonReq);
		} catch (IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
}
