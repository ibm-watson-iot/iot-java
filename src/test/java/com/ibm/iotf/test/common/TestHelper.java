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
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.test.common.TestGatewayCallback;
import com.ibm.iotf.util.LoggerUtility;

public class TestHelper {
	static final String CLASS_NAME = TestHelper.class.getName();
	Properties appProps = null;
	ApplicationClient mqttAppClient = null;
	
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
	 * Register a new device
	 * 
	 * @param apiClient
	 * @param deviceType
	 * @param deviceId
	 * @param authToken
	 * @throws IoTFCReSTException 
	 */
	public static void registerDevice(APIClient apiClient, String deviceType, String deviceId, String authToken) throws IoTFCReSTException {
		final String METHOD = "deleteDevice";
		apiClient.registerDevice(deviceType, deviceId, authToken, null, null, null);
		LoggerUtility.info(CLASS_NAME, METHOD, "Device Type(" + deviceType + ")  ID(" + deviceId + ") created.");
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
	
	public TestHelper(Properties appProperties) throws Exception {
		this.appProps = appProperties;
	}
	
	public void setAppProperties(Properties props) throws Exception {
		this.appProps = props;
		mqttAppClient = new ApplicationClient(this.appProps);
	}
	
	
	public void connectApplication() throws MqttException {
		final String METHOD = "connectApplication";
		mqttAppClient.connect();
		LoggerUtility.info(CLASS_NAME, METHOD, mqttAppClient.getClientID() + " connected " + mqttAppClient.isConnected());
	}
	
	public void disconnectApplication() {
		final String METHOD = "disconnectApplication";
		mqttAppClient.disconnect();
		LoggerUtility.info(CLASS_NAME, METHOD, mqttAppClient.getClientID() + " connected " + mqttAppClient.isConnected());
	}
	
	public void appPublishCommand(String devType, String devId, 
			String cmdName, JsonObject jsonCmd) {
		appPublishCommand(null, null, devType, devId, cmdName, jsonCmd);
	}
	
	public void appPublishCommand(
			String gwType, String gwDevId, 
			String devType, String devId, 
			String cmdName, JsonObject jsonCmd) {
		
		final String METHOD = "appPublishCommand";
		
		if (mqttAppClient == null) {
			return;
		}
		
		if (mqttAppClient.isConnected() == false) {
			try {
				mqttAppClient.connect();
			} catch (MqttException e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (cmdName == null) {
			// use default command name 
			cmdName = "stop";
		}
		
		if (jsonCmd == null) {
			jsonCmd = new JsonObject();
			jsonCmd.addProperty("name", "stop-rotation");
			jsonCmd.addProperty("delay", 0);
		}
		
		LoggerUtility.info(CLASS_NAME, METHOD, mqttAppClient.getClientID() + " publish command " + cmdName);
		
		if (gwType != null && gwDevId != null) {
			mqttAppClient.publishCommand(gwType, gwDevId, cmdName, jsonCmd);
		} else {
			mqttAppClient.publishCommand(devType, devId, cmdName, jsonCmd);
		}
		
		mqttAppClient.disconnect();
		
	}
	

}
