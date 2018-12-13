package com.ibm.iotf.test.common;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.gateway.GatewayClient;
import com.ibm.iotf.test.common.TestGatewayCallback;
import com.ibm.iotf.util.LoggerUtility;

public class TestHelper {
	static final String CLASS_NAME = TestHelper.class.getName();
	String gwDevType = null;
	String gwDevId = null;
	String devType = null;
	String devId = null;
	
	GatewayClient gwClient = null;
	TestGatewayCallback callback = null;
	
	/**
	 * Delete a device
	 * 
	 * @param devType Device Type
	 * @param devId   Device Id
	 */
	public static void deleteDevice(APIClient apiClient, String devType, String devId) {
		final String METHOD = "deleteDevice";
		boolean exist = false;
		try {
			exist = apiClient.isDeviceExist(devType, devId);
		} catch (IoTFCReSTException e1) {
			e1.printStackTrace();
		}
		
		if (exist) {
			try {
				apiClient.deleteDevice(devType, devId);
				LoggerUtility.info(CLASS_NAME, METHOD, "Device ID (" + devId + ") deleted.");
			} catch (IoTFCReSTException e) {
				e.printStackTrace();
			}
		}			
	}
	
	/**
	 * Create a API Key and return properties which can be used to instantiate application client.
	 * Note that the caller should update Application ID before us
	 * 
	 * @param apiClient
	 * @param comment
	 * @param roles
	 * @return
	 */
	public static Properties createAPIKey(APIClient apiClient, String comment, ArrayList<String> roles) {
		final String METHOD = "createAPIKey";
		Properties props = null;
		
		if (comment == null || comment.isEmpty()) {
			LoggerUtility.warn(CLASS_NAME, METHOD, "comment can not be null or empty.");
			return null;
		}
		
		JsonObject apiKeyDetails = new JsonObject();
		apiKeyDetails.addProperty("comment", comment);
		JsonArray jarrayRoles = new JsonArray();
		for (String role : roles) {
			jarrayRoles.add(role);
		}
		apiKeyDetails.add("roles", jarrayRoles);
		
		try {
			JsonObject jsonResult = apiClient.createAPIKey(apiKeyDetails);
			if (jsonResult.has("key") && jsonResult.has("token")) {
				props = new Properties(apiClient.getProperties());
				props.setProperty("API-Key", jsonResult.get("key").getAsString());
				props.setProperty("Authentication-Token", jsonResult.get("token").getAsString());
			}
			LoggerUtility.info(CLASS_NAME, METHOD, "API Key (" + props.getProperty("API-Key") + ") created.");
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
		return props;
	}
	
	/**
	 * Delete API keys that were created in a test run.
	 * A comment is used to determine if a API key was created by a test.
	 * @param apiClient
	 * @param comment
	 */
	public static void deleteAPIKeys(APIClient apiClient, String comment) {
		final String METHOD = "deleteAPIKeys";
		try {
			JsonArray jarrayResult = apiClient.getAllAPIKeys();
			
			if (jarrayResult != null) {
				int size = jarrayResult.size();
				for (int i=0; i<size; i++) {
					JsonObject jsonObj = jarrayResult.get(i).getAsJsonObject();
					if (jsonObj.has("comment")) {
						if (comment.equals(jsonObj.get("comment").getAsString())) {
							
							if (jsonObj.has("key")) {
								try {
									String apiKey = jsonObj.get("key").getAsString();
									LoggerUtility.info(CLASS_NAME, METHOD, "Deleting API Key " + apiKey);
									apiClient.deleteAPIKey(apiKey);
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
							}
							
						}
					}
				}
			}
			
		} catch (IoTFCReSTException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Update API Key's roles.
	 * 
	 * @param apiClient
	 * @param apiKey
	 * @param roleId
	 * @param resourceGroup
	 * @return
	 */
	public static JsonObject updateAPIKeyRole(APIClient apiClient, String apiKey, String roleId, String resourceGroup) {
		final String METHOD = "updateAPIKeyRole";
		JsonObject jsonResult = null;
		JsonObject jsonRolesToGroups = null;
		JsonArray jarrayRoleToGroups = null;
		
		try {
			jsonResult = apiClient.getGetAPIKeyRoles((String)null);
			if (jsonResult != null && jsonResult.has("results")) {
				LoggerUtility.info(CLASS_NAME, METHOD, "This API Key roles : " + jsonResult);
			}
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		} catch (IoTFCReSTException e2) {
			e2.printStackTrace();
		}
		
		if (jsonResult == null) {
			LoggerUtility.info(CLASS_NAME, METHOD, "API Key (" + apiKey + ") does not have any roles");
			return null;
		}
		
		if (jsonResult.has("rolesToGroups")) {
			jsonRolesToGroups = jsonResult.get("rolesToGroups").getAsJsonObject();
			
			if (jsonRolesToGroups.has(roleId)) {
				jarrayRoleToGroups = jsonRolesToGroups.get(roleId).getAsJsonArray();
				jsonRolesToGroups.remove(roleId);
			}
		}
		
		
		/*
			{
			  "roles": [
			    {
			      "roleId": "CUSTOM_ROLE_ACME",
			      "roleStatus": 1
			    }
			  ],
			  "rolesToGroups": {
			    "CUSTOM_ROLE_ACME": [
			      "group-uuid-abcdef-12345"
			    ]
			  }
			}
		*/
		
		JsonObject jsonBody = new JsonObject();
		JsonArray jarrayRoles = new JsonArray();
		JsonObject aRole = new JsonObject();
		
		if (jsonRolesToGroups == null) {
			jsonRolesToGroups = new JsonObject();
		}
		
		if (jarrayRoleToGroups == null) {
			jarrayRoleToGroups = new JsonArray();
		}
		
		jarrayRoleToGroups.add(resourceGroup); /* e.g ["group-uuid-abcdef-12345"] */
		jsonRolesToGroups.add(roleId, jarrayRoleToGroups); /* "CUSTOM_ROLE_ACME": ["group-uuid-abcdef-12345"] */
				
		aRole.addProperty("roleId", roleId);
		aRole.addProperty("roleStatus", 1);
		jarrayRoles.add(aRole);
		
		jsonBody.add("roles", jarrayRoles);
		jsonBody.add("rolesToGroups", jsonRolesToGroups);
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Updating " + jsonBody.toString());
		
		try {
			jsonResult = apiClient.updateAPIKeyRoles(apiKey, jsonBody);
			LoggerUtility.info(CLASS_NAME, METHOD, "API key (" + apiKey + ") roles updated");
		} catch (UnsupportedEncodingException | IoTFCReSTException e) {
			e.printStackTrace();
		}
		
		return jsonResult;
	}
	
	
	
	public TestHelper(String gwDevType, String gwDevId, String devType, String devId) throws Exception {
		this.gwDevType = gwDevType;
		this.gwDevId = gwDevId;
		this.devType = devType;
		this.devId = devId;
		Properties props = TestEnv.getGatewayProperties(gwDevType, gwDevId);
		gwClient = new GatewayClient(props);
		callback = new TestGatewayCallback();
		gwClient.setGatewayCallback(callback);
	}
	
	public String getGatewayDeviceType() { return gwDevType; }
	public String getGatewayDeviceId() { return gwDevId; }
	public String getAttachedDeviceType() { return devType; }
	public String getAttachedDeviceId() { return devId; }
	
	public void connectGateway() throws MqttException {
		final String METHOD = "connectGateway";
		gwClient.connect();
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " connected " + gwClient.isConnected());
	}
	
	public void disconnectGateway() {
		final String METHOD = "disconnectGateway";
		gwClient.disconnect();
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " connected " + gwClient.isConnected());
	}
	
	public String getClientID() {
		return gwClient.getClientID();
	}
	
	public boolean commandReceived() {
		return callback.commandReceived();
	}
	
	public boolean notificationReceived() {
		return callback.notificationReceived();
	}
	
	public void clear() {
		callback.clear();
	}
	
	public void subscribeNotification() {
		final String METHOD = "subscribeNotification";
		gwClient.subscribeToGatewayNotification();
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " subscribed to notification");
	}
	
	public void subscribeCommands() {
		final String METHOD = "subscribeCommands";
		gwClient.subscribeToDeviceCommands(devType, devId);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " subscribed to all commands.");
	}
	
	public void subscribeCommand(String command) {
		final String METHOD = "subscribeCommands";
		gwClient.subscribeToDeviceCommands(devType, devId, command);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " subscribed to command " + command);
	}

	public void subscribeCommand(String command, int qos) {
		final String METHOD = "subscribeCommand";
		gwClient.subscribeToDeviceCommands(devType, devId, command, qos);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " subscribed to command " + command + " and QoS " + qos);
	}
	
	public void subscribeCommand(String command, String format) {
		final String METHOD = "subscribeCommand";
		gwClient.subscribeToDeviceCommands(devType, devId, command, format);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " subscribed to command " + command + " and format " + format);
	}

	public void subscribeCommand(String command, String format, int qos) {
		final String METHOD = "subscribeCommand";
		gwClient.subscribeToDeviceCommands(devType, devId, command, format, qos);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " subscribed to command " + command + " and format " + format + " and QoS " + qos);
	}

	public void publishEvent(String devType, String devId, String event, JsonObject jsonData) {
		final String METHOD = "publishEvent";
		gwClient.publishDeviceEvent(devId, devId, event, jsonData);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " publish event to device type " + devType + " device ID " + devId + " Event " + event);
	}

	public void unsubscribeCommands() {
		final String METHOD = "unsubscribeCommands";
		gwClient.unsubscribeFromDeviceCommands(devType, devId);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " unsubscribed from all commands");
	}

	public void unsubscribeCommand(String command) {
		final String METHOD = "unsubscribeCommand";
		gwClient.unsubscribeFromDeviceCommands(devType, devId, command);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " unsubscribed from command " + command);
	}

	public void unsubscribeCommand(String command, String format) {
		final String METHOD = "unsubscribeCommand";
		gwClient.unsubscribeFromDeviceCommands(devType, devId, command, format);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " unsubscribed from command " + command + " and format " + format);
	}


}
