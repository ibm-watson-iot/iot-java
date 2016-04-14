/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.net.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.util.LoggerUtility;

/**
 * Class to register, delete and retrieve information about devices <br>
 * This class can also be used to retrieve historian information
 */

public class DeviceFactory {

	private static final String CLASS_NAME = DeviceClient.class.getName();
	
	private static final String SUCCESSFULLY_DELETED = "SUCCESSFULLY DELETED";
	private static final String RESOURCE_NOT_FOUND = "RESOURCE NOT FOUND";
	private static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

	private String authKey = null;
	private String authToken = null;
	
	public DeviceFactory(Properties opt) {
		authKey = opt.getProperty("authKey");
		authToken = opt.getProperty("authToken");
	}
	
	private String connect(String httpOperation, String url, String jsonPacket) {
		final String METHOD = "connect";
		BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(System.in));
		
		StringEntity input = null;
		try {
			if(jsonPacket != null) {
				input = new StringEntity(jsonPacket);
			}
		} catch (UnsupportedEncodingException e) {
			LoggerUtility.warn(CLASS_NAME, METHOD, "Unable to carry out the ReST request");
			return null;
		}
		byte[] encoding = Base64.encodeBase64(new String(authKey + ":" + authToken).getBytes() );			
		String encodedString = new String(encoding);
		switch(httpOperation) {
			case "post":
				HttpPost post = new HttpPost(url);
				post.setEntity(input);
				post.addHeader("Content-Type", "application/json");
				post.addHeader("Accept", "application/json");
				post.addHeader("Authorization", "Basic " + encodedString);
				try {
					HttpClient client = HttpClientBuilder.create().build();					
					HttpResponse response = client.execute(post);
					br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));			
				} catch (IOException e) {
					LoggerUtility.warn(CLASS_NAME, METHOD, e.getMessage());
					return null;
				} finally {

				}
				break;
			case "get":

				HttpGet get = new HttpGet(url);
				get.addHeader("Content-Type", "application/json");
				get.addHeader("Accept", "application/json");
				get.addHeader("Authorization", "Basic " + encodedString);
				try {
					HttpClient client = HttpClientBuilder.create().build();					
					HttpResponse response = client.execute(get);
					br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));			
				} catch (IOException e) {
					LoggerUtility.warn(CLASS_NAME, METHOD, e.getMessage());
					return null;
				}			
				break;
			case "delete":
				HttpDelete delete = new HttpDelete(url);
				delete.addHeader("Content-Type", "application/json");
				delete.addHeader("Accept", "application/json");
				delete.addHeader("Authorization", "Basic " + encodedString);
				try {
					HttpClient client = HttpClientBuilder.create().build();					
					HttpResponse response = client.execute(delete);
					int httpCode = response.getStatusLine().getStatusCode();
					if(httpCode == 202 || httpCode == 200 || httpCode == 204) {
						return SUCCESSFULLY_DELETED;
					} else if(httpCode == 400 || httpCode == 404) {
						return RESOURCE_NOT_FOUND;
					}
						
				} catch (IOException e) {
					LoggerUtility.warn(CLASS_NAME, METHOD, e.getMessage());
					return UNKNOWN_ERROR;
				} finally {

				}
				break;				
		}
			
		String line = null;
		try {
			line = br.readLine();
		} catch (IOException e) {
			LoggerUtility.warn(CLASS_NAME, METHOD, e.getMessage());
			return null;
		}
		LoggerUtility.info(CLASS_NAME, METHOD, line);
		try {
			if(br != null)
				br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return line;
	}

	/**
	 * This method retrieves a device in the org whose authkey and token were passed previously
	 * @param deviceType
	 * 				an object of String which contains device type
	 * @param deviceId
	 * 				an object of String which contains device id
	 * @return Device
	 */
	public Device getDevice(String deviceType, String deviceId) {
		String orgid = this.authKey.substring(2, 8);
		String url = "https://"+ orgid +".internetofthings.ibmcloud.com/api/v0001" + "/devices/" + deviceType + "/" + deviceId;
		String jsonPacket = "{\"type\": \"" + deviceType + "\",\"id\": \"" + deviceId+ "\"}";

		String response = connect("get", url, jsonPacket);
		JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
		
		Device device = new Device();
		device.setUuid(responseJson.get("uuid").toString());
		device.setType(responseJson.get("type").toString());
		String metadata = responseJson.get("metadata") != null ? responseJson.get("metadata").toString() : null;
		device.setMetadata(metadata);
		device.setId(responseJson.get("id").toString());
		device.setRegistration(responseJson.get("registration").toString());

		return device;		
	}

	
	/**
	 * This method returns an array of all the devices belonging to the organization
	 * @return devices
	 * 			An array of devices
	 */
	public Device[] getDevices() {
		String orgid = this.authKey.substring(2, 8);
		String url = "https://"+ orgid +".internetofthings.ibmcloud.com/api/v0001/devices";

		String response = connect("get", url, null);
		JsonArray responseJsonArray = new JsonParser().parse(response).getAsJsonArray();
		
		List<Device> deviceList = new ArrayList<Device>();
		for(Iterator<JsonElement> iterator = responseJsonArray.iterator(); iterator.hasNext(); ) {
			JsonElement deviceElement = iterator.next();
			JsonObject responseJson = deviceElement.getAsJsonObject();
			
			Device device = new Device();
			device.setUuid(responseJson.get("uuid").toString());
			device.setType(responseJson.get("type").toString());
			String metadata = responseJson.get("metadata") != null ? responseJson.get("metadata").toString() : null;
			device.setMetadata(metadata);
			device.setId(responseJson.get("id").toString());
			device.setRegistration(responseJson.get("registration").toString());
			
			deviceList.add(device);
		}
		return deviceList.toArray(new Device[0]);
	}

	
	/**
	 * This method accepts variable number of arguments and returns a list of historical events <br>
	 * If only one argument is passed, it contains the device type <br>
	 * If 2 arguments are passed the 2nd argument represents deviceId
	 * @param deviceInfo
	 * 			
	 * @return array of historical events
	 */
	
	public HistoricalEvent[] getHistoricalEvents(String ... deviceInfo) {
		String orgid = this.authKey.substring(2, 8);
		String deviceType = deviceInfo.length > 0 ? deviceInfo[0] : null;
		String deviceId = deviceInfo.length > 1 ? deviceInfo[1] : null;
		
		String url = new String();
		if(deviceType != null && deviceId != null) {
			url = "https://"+ orgid +".internetofthings.ibmcloud.com/api/v0001/historian/" + deviceType + "/" + deviceId;
		} else if( deviceType != null ) {
			url = "https://"+ orgid +".internetofthings.ibmcloud.com/api/v0001/historian/" + deviceType;
		} else {
			url = "https://"+ orgid +".internetofthings.ibmcloud.com/api/v0001/historian";
		}
		
		String response = connect("get", url, null);		
		JsonArray responseJsonArray = new JsonParser().parse(response).getAsJsonArray();
		
		List<HistoricalEvent> messageList = new ArrayList<HistoricalEvent>();
		for(Iterator<JsonElement> iterator = responseJsonArray.iterator(); iterator.hasNext(); ) {
			JsonElement messageElement = iterator.next();
			
//			messageList.add(new HistoricalEvent(messageElement));

			JsonObject responseJson = messageElement.getAsJsonObject();

			HistoricalEvent hEvent = new HistoricalEvent();
			
			hEvent.setEvt_type(responseJson.get("evt_type").toString());
			hEvent.setTimestamp(responseJson.get("timestamp").toString());
			hEvent.setEvt(responseJson.get("evt").toString());
			
			messageList.add(hEvent);
		}
		return messageList.toArray(new HistoricalEvent[0]);
	}
	
	/**
	 * This method registers a device
	 * @param deviceType
	 * 				object of String representing device type
	 * @param deviceId
	 * 				object of String representing device id
	 * @param metadata
	 * 				object of String representing metadata. This value can be null
	 * @return device
	 */
	public Device registerDevice(String deviceType, String deviceId, String metadata) {
		final String METHOD = "registerDevice";
		String orgid = authKey.substring(2, 8);
		String url = "https://"+ orgid +".internetofthings.ibmcloud.com/api/v0001" + "/devices";
		String jsonPacket = "{\"type\": \"" + deviceType + "\",\"id\": \"" + deviceId+ "\"}";

		String response = connect("post", url, jsonPacket);
		JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
		
		if(responseJson.get("message") != null) {
			LoggerUtility.warn(CLASS_NAME, METHOD, "Device already exists ");
			return null;
		}
		
		Device device = new Device();
		device.setUuid(responseJson.get("uuid").toString());
		device.setType(responseJson.get("type").toString());
		metadata = responseJson.get("metadata") != null ? responseJson.get("metadata").toString() : null;
		device.setMetadata(metadata);
		device.setId(responseJson.get("id").toString());
		device.setPassword(responseJson.get("password").toString());
		device.setRegistration(responseJson.get("registration").toString());

		return device;		
	}
	
	
	/**
	 * This method deletes the device which matches the device id and type of the organization
	 * @param deviceType
	 * 				object of String which represents device Type
	 * @param deviceId
	 * 				object of String which represents device id
	 * @return boolean to denote success or failure of operation
	 */
	public boolean deleteDevice(String deviceType, String deviceId) {

		final String METHOD = "deleteDevice";
		String orgid = authKey.substring(2, 8);
		String url = "https://"+ orgid +".internetofthings.ibmcloud.com/api/v0001/devices/" + deviceType + "/" + deviceId;
		String jsonPacket = "{\"type\": \"" + deviceType + "\",\"id\": \"" + deviceId+ "\"}";

		String response = connect("delete", url, jsonPacket);
		
		if(response.equals(RESOURCE_NOT_FOUND)) {
			LoggerUtility.warn(CLASS_NAME, METHOD, "Device didn't exist ");
			return false;
		} else if(response.equals(SUCCESSFULLY_DELETED)) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Device deregistered");
			return true;
		} else {
			LoggerUtility.warn(CLASS_NAME, METHOD, UNKNOWN_ERROR);
			return false;			
		}
	}

/*
	public static void main(String[] args) {

		String deviceType = "iotsample-arduino";
		String deviceId = "00aaaaaaaa02";

		Properties opt = new Properties();
		opt.setProperty("authKey", "xxx");
		opt.setProperty("authToken", "yyy");
		DeviceFactory factory = new DeviceFactory(opt);
		
		String metadata = "";
		Device device = factory.registerDevice(deviceType, deviceId, metadata);
		if(device != null)
			System.out.println("Device created and has " + device);
		else 
			System.out.println("Device not created");
		
		System.out.println("Now retrieving the device.....");
		
		device = factory.getDevice(deviceType, deviceId);
		
		if(device != null)
			System.out.println("Device retrieved and has " + device);
		else 
			System.out.println("Device not retrieved");
		
		Device [] listDevices = factory.getDevices();
		System.out.println("Devices obtained = " + listDevices.length);		

//		Historian retrieval		
		
		HistoricalEvent [] listHistory = factory.getHistoricalEvents(deviceType, "00aabbccde03");
		System.out.println("Events obtained 00 = " + listHistory.length);
		
		HistoricalEvent [] listHistory1 = factory.getHistoricalEvents(deviceType, deviceId);
		System.out.println("Events obtained 00 = " + listHistory1.length);
		
		HistoricalEvent [] listHistory2 = factory.getHistoricalEvents("mbed", "bed01");
		System.out.println("Events obtained 11 = " + listHistory2.length);
		
		HistoricalEvent [] listHistory3 = factory.getHistoricalEvents("mbed");
		System.out.println("Events obtained 12 = " + listHistory3.length);
		
		listHistory = factory.getHistoricalEvents(deviceType);
		System.out.println("Events obtained 00= " + listHistory.length);
		
		listHistory = factory.getHistoricalEvents();
		System.out.println("Events obtained 22 = " + listHistory.length);
		
		System.out.println("Devices obtained = " + listDevices.length);
		boolean deletion = factory.deleteDevice(deviceType, deviceId);
		System.out.println("Operation was successful? " + deletion);
		
		Device [] listDevices1 = factory.getDevices();
		System.out.println("Devices obtained = " + listDevices1.length);	
	}
*/
}
