/**
 *****************************************************************************
 * Copyright (c) 2015 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */
package com.ibm.iotf.sample.client.application;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.ArrayList;

import com.cloudant.client.api.*; // .CloudantClient;
import com.cloudant.client.api.model.ChangesResult;
import com.cloudant.client.api.model.ChangesResult.Row;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;


/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * initiate/get/delete one or more device management operations.
 */
public class ApplicationFirmwareRequestSample {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";

	// Define a sequence to help obtaining the change set
	private String firmwareDBSequence;  
	private Database firmwareDB;
	private String username = null;
	private String password = null;
	private String documentId = null;
	private String attachmentName = null;
	private String deviceFwVersion = "1.0.2";
	private String dbName;
	private String deviceType;
	private String deviceId;
	
	
	// document Id containing the latest firmware image
	private String currentFirmware = "iot_1.0-2_armhf.deb";
	private static String latestFirmware;
	private String latestFirmwareVersion;
	
	private static URL url = null;

	ManagedDevice dmClient;
	
	protected final static JsonParser JSON_PARSER = new JsonParser();

	private APIClient apiClient = null;
	
	public ApplicationFirmwareRequestSample() {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(ApplicationFirmwareRequestSample.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		deviceType = trimedValue(props.getProperty("Device-Type"));
		deviceId = trimedValue(props.getProperty("Device-ID"));
		username = trimedValue(props.getProperty("User-Name"));
		password = trimedValue(props.getProperty("Password"));
		dbName = trimedValue(props.getProperty("Repository-DB"));
		
		try {
			//Instantiate the class by passing the properties file
			this.apiClient = new APIClient(props);
			
			StringBuilder sb = new StringBuilder();
			sb.append("https://")
			.append(username)
			.append(":")
			.append(password)
			.append("@")
			.append(username)
			.append(".cloudant.com");
				
			System.out.println(sb);
			
			CloudantClient client = new CloudantClient(sb.toString(), username, password);
				
			System.out.println("Connected to Cloudant");
			System.out.println("Server Version: " + client.serverVersion());
	
			firmwareDB = client.database(dbName, false);
			System.out.println("Value of Cloudant DB is "+firmwareDB);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) throws IoTFCReSTException {
		
		ApplicationFirmwareRequestSample sample = new ApplicationFirmwareRequestSample();
		sample.getDeviceFwVersion();
		while(true) {
			try {
				// Check on Cloudant DB to see if any latest firmware is available
				// If yes, then, Initiate Firmware Download, else, wait and check again after SLEEP time elapses
				if(sample.checkIfNewFirmwareImage()) {
					if(sample.initiateFirmwareDownloadRequest()) {
						// Wait for the Download to complete and receive an acknowledgement from the Device
						sample.waitForRequestToFinish();
						// Once the acknowledgement on the completion of Firmware Donwload comes in, initiate Firmware Update
						if(sample.initiateFirmwareUpdateRequest()) {
							// Wait for the Firmware Update action to complete and receive an acknowledgement from the Device
							sample.waitForRequestToFinish();
							// call getDeviceFwVersion to get the latest firmware version details from the device
							sample.getDeviceFwVersion();
						}
					}
				}
				// SLEEP for the defined time, if the Cloudant DB has the Firmware of same version, as available on the Device
				Thread.sleep(1000 * 60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * The following method helps initiated Device Management Request
	 * Firmware Update, being triggered from Application and initiated 
	 * through Watson IoT Platform
	 * @return
	 */
	private boolean initiateFirmwareUpdateRequest() {
		
		try {
			clearAllDMRequests();
		} catch (IoTFCReSTException e1) {
			System.out.println("Cleared all the Device Management Requests, to start clean");
			e1.printStackTrace();
		}
		
		// Prepare the Device Action as Firmware Update
		String updateRequest = "{\"action\": \"firmware/update\", \"devices\": [{\"typeId\": \"" + 
						deviceType + "\",\"deviceId\": \"" + deviceId + "\"}]}";
		
		JsonObject update = (JsonObject) new JsonParser().parse(updateRequest);
		System.out.println(update);
		boolean response = false;
		try {
			response = apiClient.initiateDeviceManagementRequest(update);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		System.out.println(response);
		
		return response;
		
	}
	
	
	private void clearAllDMRequests() throws IoTFCReSTException {
		JsonElement response = this.apiClient.getAllDeviceManagementRequests();
		if(response.getAsJsonObject().get("results") != null) {
			JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
			for(int i = 0; i < requests.size(); i++) {
				JsonElement request = requests.get(i);
				String requestId = request.getAsJsonObject().get("id").getAsString();
				this.apiClient.deleteDeviceManagementRequest(requestId);
			}
		}
		
	}

	/**
	 * The following method showcases steps to construct the URL to download the
	 * latest Firmware from the Cloudant NoSQL DB and helps create the Device Management
	 * request 'Firmware Download'.
	 * @return
	 */
	public boolean initiateFirmwareDownloadRequest() {
		try {
			clearAllDMRequests();
		} catch (IoTFCReSTException e1) {
			System.out.println("Cleared all the Device Management Requests, to start clean");
			e1.printStackTrace();
		}
		
		// Construct the URL to download the Debian Package from the Cloudant NoSQL DB 
		String buildURL = ("https://" +username +".cloudant.com/" + dbName + "/" + documentId + "/" + attachmentName);
		
		try {
			System.out.println("Building the URL ...");
			url = new URL(buildURL);
			System.out.println("Value of url is "+url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		// Prepare the Device Action as Firmware Download, passing the constructed URL
		
		String downloadRequest = "{\"action\": \"firmware/download\", \"parameters\": ["
				+ "{\"name\": \"version\", \"value\": \"" + latestFirmwareVersion + "\" }," + 
				"{\"name\": \"name\", \"value\": \"" + attachmentName +"\"}," +
		"{\"name\": \"uri\",\"value\": \"" + url + "\"}" +
		"],\"devices\": [{\"typeId\": \"" + deviceType + "\",\"deviceId\": \"" + deviceId + "\"}]}";
		
		JsonObject download = (JsonObject) new JsonParser().parse(downloadRequest);
		System.out.println(download);
		boolean response = false;
		try {
			
			response = apiClient.initiateDeviceManagementRequest(download);
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		System.out.println(response);
		
		return response;
					
	}
	
	/**
	 * The following method sends an acknowledgement from Device to Application,
	 * mentioning the status of the execution of Device Action, as triggered
	 * by the Application through Watson IoT Platform
	 * @throws IoTFCReSTException
	 */
	private void waitForRequestToFinish() throws IoTFCReSTException {
		JsonElement response = this.apiClient.getAllDeviceManagementRequests();
		JsonArray requests = response.getAsJsonObject().get("results").getAsJsonArray();
		JsonElement request = requests.get(0);
		String requestId = request.getAsJsonObject().get("id").getAsString();
		
		
		int count = 0;
		while(count++ <= 3600) { // wait for an hour, before giving it up
			JsonObject details = this.apiClient.getDeviceManagementRequest(requestId);
			System.out.println(details);
			if(details.get("complete").getAsBoolean() == true) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}
	
	/**
	 * The following method snoops the Cloudant NoSQL DB to see, if there's a newer version
	 * of Firmware, whose version is greater than that of the Firmware, that is currently
	 * active on the Device. If yes, it triggers Firmware Download action. Else, it waits
	 * till the Sleep time elapses and does the check again.
	 * @return
	 */
	public boolean checkIfNewFirmwareImage() {
			
		ChangesResult changes = null;
		if(this.firmwareDBSequence == null) {
			changes = firmwareDB.changes()
					.includeDocs(true)
					.getChanges();
		} else {
			changes = firmwareDB.changes()
					.includeDocs(true)
					.since(firmwareDBSequence)
					.getChanges();
		}
		firmwareDBSequence = firmwareDB.info().getUpdateSeq();
		String version = deviceFwVersion;
		boolean firmwareAvailable = false;
		if(changes.getResults().size() != 0) {
			List<ChangesResult.Row> rows = changes.getResults();
			
			for (Row row : rows) {
				JsonObject attachment = row.getDoc();
				
				if (attachment != null) {
					
					if(attachment.get("_deleted") != null && attachment.get("_deleted").getAsBoolean() == true) {
						continue;
					}
					
					if(attachment.get("version") != null && attachment.get("_attachments") != null) {
						System.out.println(attachment);
						String retrievedVersion = attachment.get("version").getAsString();
						System.out.println(retrievedVersion);
						if(isVersionGreater(version, retrievedVersion)) {
		                	firmwareAvailable = true;
		                	String docId = row.getId();
		                	documentId = docId.toString();
		//                	// System.out.println("Value of docId is " +docId);
		                	latestFirmwareVersion = retrievedVersion;
		                	version = retrievedVersion;
		                    
			                JsonObject obj = attachment.get("_attachments").getAsJsonObject();
							Set<Entry<String, JsonElement>> entrySet = obj.entrySet();
											
							Iterator<Entry<String, JsonElement>> itr = entrySet.iterator();
							if(itr.hasNext()) {
								Entry<String, JsonElement> entry = itr.next();
								setLatestFirmware(entry.getKey());
								attachmentName = entry.getKey();
								System.out.println("Setting latest firmware to "+entry.getKey());
								// attachmentName = entry.getKey().toString();
							}
						}
					}
				}
			}
		} 
		return firmwareAvailable;
	}
	
	/**
	 * This method checks whether the retrieved version is higher than
	 * the deviceVersion.
	 */
	private static boolean isVersionGreater(String deviceVersion, String retrievedVersion) {
		String[] retrieved = retrievedVersion.split("\\.");
		String[] device = deviceVersion.split("\\.");
		try {
			for(int i = 0; i < device.length; i++) {
				int retInt = Integer.parseInt(retrieved[i]);
				int deviceInt = Integer.parseInt(device[i]);
				if(retInt > deviceInt) {
					return true;
        		}
        	}
        } catch(Exception e) {
        }
		return false;
	}
	
	protected String getCurrentFirmware() {
		return currentFirmware;
	}
	
	protected void setCurrentFirmware(String currentFirmware) {
		this.currentFirmware = currentFirmware;
	}
	
	protected static String getLatestFirmware() {
		return latestFirmware;
	}
	
	protected void setLatestFirmware(String latestFirmware) {
		ApplicationFirmwareRequestSample.latestFirmware = latestFirmware;
	}
	
	/**
	 * This sample showcases how to get device details using the Java Client Library.
	 * @throws IoTFCReSTException
	 */
	private void getDeviceFwVersion() throws IoTFCReSTException {
		try {
			
			Properties props = new Properties();
			try {
				props.load(ApplicationFirmwareRequestSample.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			} catch (IOException e1) {
				System.err.println("Not able to read the properties file, exiting..");
				System.exit(-1);
			}	
			
			String deviceID = trimedValue(props.getProperty("Device-ID"));
			String deviceType = trimedValue(props.getProperty("Device-Type"));
			
			System.out.println("get device --> "+deviceID);
			JsonObject response = this.apiClient.getDevice(deviceType, deviceID);
			
			// JsonElement 
			
			if (response.get("deviceInfo") != null){
				JsonObject deviceInfo = response.get("deviceInfo").getAsJsonObject();
				System.out.println(deviceInfo);
				if(deviceInfo.get("fwVersion") != null) {
					deviceFwVersion = deviceInfo.get("fwVersion").getAsString();
				}
			} else {
				System.out.println("Device Info is Null, Considering the default criteria!");
			}
			System.out.println(response);
		} catch(IoTFCReSTException e) {
			System.out.println("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
}