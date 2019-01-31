package com.ibm.iotf.test.common;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.util.LoggerUtility;

public class TestApplicationHelper {
	static final String CLASS_NAME = TestApplicationHelper.class.getName();
	Properties appProps = null;
	ApplicationClient mqttAppClient = null;
	TestEventCallback eventCallback = null;
	
	
	public TestApplicationHelper(Properties appProperties) throws Exception {
		this.appProps = appProperties;
		
		if (this.appProps != null) {
			mqttAppClient = new ApplicationClient(this.appProps);
		}
	}
	
	/**
	 * Delete a device
	 * 
	 * @param devType Device Type
	 * @param devId   Device Id
	 * @throws IoTFCReSTException 
	 */
	public static void deleteDevice(APIClient apiClient, String devType, String devId) throws IoTFCReSTException {
		final String METHOD = "deleteDevice";
		boolean exist = false;
		try {
			exist = apiClient.isDeviceExist(devType, devId);
		} catch (IoTFCReSTException e1) {
			e1.printStackTrace();
		}
		
		boolean deleted = false;
		int maxAttempts = 5;
		if (exist) {
			for (int i=1; i<maxAttempts; i++) {
				try {
					deleted = apiClient.deleteDevice(devType, devId);
				} catch (IoTFCReSTException e) {
					LoggerUtility.severe(CLASS_NAME, METHOD, 
							"Failed delete device, Type (" + devType + ") ID(" + devId 
							+ ")  HTTP error code : " + e.getHttpCode() 
							+ " Response: " + e.getResponse());
					if (i == maxAttempts) {
						throw e;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// Ignore
					}
				}				
			}
			LoggerUtility.info(CLASS_NAME, METHOD, "Device Type (" + devType + ") ID (" + devId 
					+ ") deleted ? " + deleted);
		}
	}
	
	/**
	 * Delete device type.
	 * 
	 * @param apiClient
	 * @param devType
	 * @throws IoTFCReSTException 
	 */
	public static void deleteDeviceType(APIClient apiClient, String devType) throws IoTFCReSTException {
		final String METHOD = "deleteDeviceType";
		boolean exist = false;
		try {
			exist = apiClient.isDeviceTypeExist(devType);
		} catch (IoTFCReSTException e1) {
			e1.printStackTrace();
		}
		
		boolean deleted = false;
		int maxAttempts = 5;
		if (exist) {
			for (int i=1; i<maxAttempts; i++) {
				try {
					deleted = apiClient.deleteDeviceType(devType);
				} catch (IoTFCReSTException e) {
					LoggerUtility.severe(CLASS_NAME, METHOD, 
							"Failed delete device type (" + devType
							+ ")  HTTP error code : " + e.getHttpCode() 
							+ " Response: " + e.getResponse());
					if (i == maxAttempts) {
						throw e;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// Ignore
					}
				}				
			}
			
			LoggerUtility.info(CLASS_NAME, METHOD, "Device Type (" + devType + ") deleted ? " + deleted);
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
		try {
			apiClient.registerDevice(deviceType, deviceId, authToken, null, null, null);
		} catch (IoTFCReSTException e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Failed to register Device Type(" + deviceType + ")  ID(" + deviceId + ") HTTP Error Code: " 
					+ e.getHttpCode() + " Resonse: " + e.getResponse() );
			throw e;
		}
		LoggerUtility.info(CLASS_NAME, METHOD, "Device Type(" + deviceType + ")  ID(" + deviceId + ") created.");
	}
	
	/**
	 * Add a device type.
	 * 
	 * @param apiClient
	 * @param devType
	 * @throws IoTFCReSTException
	 */
	public static void addDeviceType(APIClient apiClient, String devType) throws IoTFCReSTException {
		final String METHOD = "addDeviceType";
		apiClient.addDeviceType(devType, null, null, null);
		LoggerUtility.info(CLASS_NAME, METHOD, "Device Type(" + devType + ") created.");
	}
	
	/**
	 * Add a new gateway type.
	 * 
	 * @param apiClient
	 * @param gwType
	 * @throws IoTFCReSTException
	 */
	public static void addGatewayType(APIClient apiClient, String gwType) throws IoTFCReSTException {
		final String METHOD = "addGatewayType";
		JsonObject jsonGW = new JsonObject();
		jsonGW.addProperty("id", gwType);
		apiClient.addGatewayDeviceType(jsonGW);
		LoggerUtility.info(CLASS_NAME, METHOD, "Gateway Type(" + gwType + ") created.");
	}
	
	/**
	 * Create a API Key with role PD_STANDARD_APP
	 * 
	 * @param apiClient
	 * @param comment
	 * @return Properties which can be used to instantiate application client.
	 * Note that the caller should update Application ID before using the returned properties.
	 */
	public static Properties createAPIKey(APIClient apiClient, String comment) {
		ArrayList<String> roles = new ArrayList<String>();
		roles.add("PD_STANDARD_APP");
		return createAPIKey(apiClient, comment, roles);
	}
	
	/**
	 * Create a API Key and return properties which can be used to instantiate application client.
	 * Note that the caller should update Application ID before using the returned properties.
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
			LoggerUtility.info(CLASS_NAME, METHOD, "API Key (" + jsonResult + ") created.");
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
	 * @param resourceGroups
	 * @return
	 */
	public static JsonObject updateAPIKeyRole(APIClient apiClient, String apiKey, String roleId, JsonArray resourceGroups) {
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
		
		for (int i=0; i<resourceGroups.size(); i++) {
			String groupId = resourceGroups.get(i).getAsString();
			jarrayRoleToGroups.add(groupId); /* e.g ["group-uuid-abcdef-12345"] */
		}
		jsonRolesToGroups.add(roleId, jarrayRoleToGroups); /* "CUSTOM_ROLE_ACME": ["group-uuid-abcdef-12345"] */
				
		aRole.addProperty("roleId", roleId);
		aRole.addProperty("roleStatus", 1);
		jarrayRoles.add(aRole);
		
		jsonBody.add("roles", jarrayRoles);
		jsonBody.add("rolesToGroups", jsonRolesToGroups);
		
		LoggerUtility.info(CLASS_NAME, METHOD, "Updating " + jsonBody.toString());
		
		try {
			jsonResult = apiClient.updateAPIKeyRoles(apiKey, jsonBody);
			LoggerUtility.info(CLASS_NAME, METHOD, "API key (" + apiKey + ") roles updated.");
		} catch (UnsupportedEncodingException | IoTFCReSTException e) {
			e.printStackTrace();
		}
		
		return jsonResult;
	}
	
	/**
	 * Get resource groups.
	 * 
	 * @param apiClient
	 * @param clientID
	 * @return JsonArray e.g. ["gw_def_res_grp:abcdef:GatewayType1:GatewayDevID1"],
	 */
	public static JsonArray getResourceGroups(APIClient apiClient, String clientID) {
		final String METHOD = "getResourceGroups";
		JsonArray groups = null;
		try {
			boolean done = false;
			String bookmark = null;
			while (!done) {
				List <NameValuePair> parameters = null;
				if (bookmark != null) {
					parameters = new ArrayList<NameValuePair>();
					NameValuePair nvpBookmark = new BasicNameValuePair("_bookmark", bookmark);
					parameters.add(nvpBookmark);
				}
				JsonObject jsonResult = apiClient.getAccessControlProperties(clientID, parameters);
				if (jsonResult != null) {
					if (jsonResult.has("results")) {
						LoggerUtility.info(CLASS_NAME, METHOD, jsonResult.toString());
						JsonArray devicesArray = jsonResult.get("results").getAsJsonArray();
						for (Iterator<JsonElement> iterator = devicesArray.iterator(); iterator.hasNext(); ) {
							JsonElement deviceElement = iterator.next();
							JsonObject jsonDevice = deviceElement.getAsJsonObject();
							if (jsonDevice.has("resourceGroups")) {
								JsonArray innerGroups = jsonDevice.get("resourceGroups").getAsJsonArray();
								if (groups == null) {
									groups = innerGroups;
								} else {
									groups.addAll(innerGroups);
								}
							}
						}
						
						if (jsonResult.has("bookmark")) {
							bookmark = jsonResult.get("bookmark").getAsString();
						}
					}
					
					if (bookmark == null) {
						done = true;
					}					
				} else {
					done = true;
				}
			}
		} catch (UnsupportedEncodingException e) {
			LoggerUtility.warn(CLASS_NAME, METHOD, e.getMessage());
			e.printStackTrace();
			return null;
		} catch (IoTFCReSTException e) {
			LoggerUtility.warn(CLASS_NAME, METHOD, "HTTP Status Code (" + e.getHttpCode() + ") " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		return groups;
	}
	
	
	public void setAppProperties(Properties props) throws Exception {
		this.appProps = props;
		mqttAppClient = new ApplicationClient(this.appProps);
	}
	
	public Properties getAppProperties() {
		return appProps;
	}
	
	public ApplicationClient getApplicationClient() {
		return mqttAppClient;
	}
	
	public TestEventCallback getCallback() {
		return eventCallback;
	}
	
	
	public void connect() throws MqttException, TestException {
		final String METHOD = "connectApplication";
		if (mqttAppClient == null) {
			throw new TestException(TestException.MQTT_APP_CLIENT_NOT_INITIALIZED);
		}
		mqttAppClient.connect();
		LoggerUtility.info(CLASS_NAME, METHOD, mqttAppClient.getClientID() + " connected " + mqttAppClient.isConnected());
	}
	
	public void disconnect() throws TestException {
		final String METHOD = "disconnectApplication";
		if (mqttAppClient == null) {
			throw new TestException(TestException.MQTT_APP_CLIENT_NOT_INITIALIZED);
		}
		mqttAppClient.disconnect();
		LoggerUtility.info(CLASS_NAME, METHOD, mqttAppClient.getClientID() + " connected " + mqttAppClient.isConnected());
	}
	
	public JsonObject publishCommand(String devType, String devId, 
			String cmdName, JsonObject jsonCmd) throws TestException {
		return publishCommand(null, null, devType, devId, cmdName, jsonCmd);
	}
	
	public JsonObject publishCommand(
			String gwType, String gwDevId, 
			String devType, String devId, 
			String cmdName, JsonObject jsonCmd) throws TestException {
		
		final String METHOD = "appPublishCommand";
		
		if (mqttAppClient == null) {
			throw new TestException(TestException.MQTT_APP_CLIENT_NOT_INITIALIZED);
		}
		
		if (mqttAppClient.isConnected() == false) {
			try {
				mqttAppClient.connect();
			} catch (MqttException e) {
				e.printStackTrace();
				return null;
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
		
		return jsonCmd;
	}
	
	public void subscribeToDeviceEvents(String deviceType, String deviceId, String event, String format, int qos) throws TestException {
		final String METHOD = "subscribeEvents";
		if (mqttAppClient == null) {
			throw new TestException(TestException.MQTT_APP_CLIENT_NOT_INITIALIZED);
		}

		if (mqttAppClient.isConnected() == false) {
			try {
				connect();
			} catch (MqttException e) {
				e.printStackTrace();
				LoggerUtility.warn(CLASS_NAME, METHOD, e.getMessage());
				return;
			}
		}
		
		eventCallback = new TestEventCallback();
		
		mqttAppClient.setEventCallback(eventCallback);
		
		mqttAppClient.subscribeToDeviceEvents(deviceType, deviceId, event, format, qos);
		
	}

}
