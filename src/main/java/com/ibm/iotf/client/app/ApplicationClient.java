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
package com.ibm.iotf.client.app;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.AbstractClient;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.util.LoggerUtility;

/**
 * A client, used by application, that handles connections with the IBM Watson IoT Platform. <br>
 * 
 * This is a derived class from AbstractClient and can be used by end-applications to handle connections with IBM Watson IoT Platform.
 */
public class ApplicationClient extends AbstractClient implements MqttCallback{
	
	private static final String CLASS_NAME = ApplicationClient.class.getName();
	
	private static final Pattern DEVICE_EVENT_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/evt/(.+)/fmt/(.+)");
	private static final Pattern DEVICE_STATUS_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/mon");
	private static final Pattern APP_STATUS_PATTERN = Pattern.compile("iot-2/app/(.+)/mon");
	private static final Pattern DEVICE_COMMAND_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/cmd/(.+)/fmt/(.+)");
	
	private EventCallback eventCallback = null;
	private StatusCallback statusCallback = null;
	
	private APIClient apiClient = null;
	
	private HashMap<String, Integer> subscriptions = new HashMap<String, Integer>();
	
	
	/**
	 * Create an application client for the IBM Watson IoT Platform. 
	 * Connecting to specific org on IBM Watson IoT Platform
	 * @param options
	 * 					An object of the class Properties
	 * @throws Exception Failure in parsing the properties 
	 */
	public ApplicationClient(Properties options) throws Exception {
		super(options);
		if(getOrgId()==null){
			
			throw new Exception("Invalid Auth Key");
		}
		if(isSharedSubscriptionEnabled()) {
			this.clientId = "A" + CLIENT_ID_DELIMITER + getOrgId() + CLIENT_ID_DELIMITER + getAppId();
		} else {
			this.clientId = "a" + CLIENT_ID_DELIMITER + getOrgId() + CLIENT_ID_DELIMITER + getAppId();
		}
		
		if (getAuthMethod() == null) {
			this.clientUsername = null;
			this.clientPassword = null;
		}
		else if (!getAuthMethod().equals("apikey")) {
			throw new Exception("Unsupported Authentication Method: " + getAuthMethod());
		}
		else {
			// use-token-auth is the only authentication method currently supported
			this.clientUsername = getAuthKey();
			this.clientPassword = getAuthToken();
		}
		createClient(this);
		
		this.apiClient = new APIClient(options);
	}
	
	/**
	 * Returns the {@link com.ibm.iotf.client.api.APIClient} that allows the users to interact with 
	 * Watson IoT Platform API's to perform one or more operations like, registering a device, 
	 * getting the list of devices connected through the Gateway and etc..
	 * 
	 * @return APIClient
	 */
	public APIClient api() {
		return this.apiClient;
	}
	
	private boolean isSharedSubscriptionEnabled() {
		boolean enabled = false;
		String value = options.getProperty("Shared-Subscription");
		if(value == null) {
			value = options.getProperty("shared-subscription");
		}
		if(value != null) {
			enabled = Boolean.parseBoolean(trimedValue(value));
		}
		return enabled;
	}
	
	/**
	 * Returns the orgid for this client
	 * 
	 * @return orgid
	 * 						String orgid
	 */
	public String getOrgId() {
		// Check if org id is provided by the user
		String orgid = super.getOrgId();
		if(orgid == null || orgid.equals("")) {
			String authKeyPassed = getAuthKey();
			if(authKeyPassed != null && ! authKeyPassed.trim().equals("") && ! authKeyPassed.equals("quickstart")) {
				if(authKeyPassed.length() >=8){
					return authKeyPassed.substring(2, 8);}
				else {
					return null;
				}
			} else {
				return "quickstart";
			}
		}
		return orgid;

	}
	
	/**
	 * Accessor method to retrieve app id
	 * @return appId
	 * 					String appId
	 */
	public String getAppId() {
		return trimedValue(options.getProperty("id"));
	}

	/**
	 * Accessor method to retrieve auth key
	 * @return authKey
	 * 					String authKey
	 */
	public String getAuthKey() {
		String authKeyPassed = options.getProperty("auth-key");
		if(authKeyPassed == null) {
			authKeyPassed = options.getProperty("API-Key");
		}
		return trimedValue(authKeyPassed);
	}
	
	/**
	 * <p>Connects the application to IBM Watson IoT Platform and retries when there is an exception.<br>
	 * 
	 * This method does not retry when the following exceptions occur.</p>
	 * 
	 * <ul class="simple">
	 *  <li> MqttSecurityException - One or more credentials are wrong
	 * 	<li>UnKnownHostException - Host doesn't exist. For example, a wrong organization name is used to connect.
	 * </ul>
	 * 
	 * @throws MqttException when the security credentials are wrong
	 **/
	public void connect() throws MqttException {
		super.connect(true);
	}
	
	/**
	 * Publish event, on the behalf of a device, to the IBM Watson IoT Platform. <br> 
	 * Note that data is published
	 * at Quality of Service (QoS) 0, which means that a successful send does not guarantee
	 * receipt even if the publish is successful.
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param data
	 *            Payload data
	 * @return Whether the send was successful.
	 */
	public boolean publishEvent(String deviceType, String deviceId, String event, Object data) {
		return publishEvent(deviceType, deviceId, event, data, 0, getActionTimeout());
	}

	/**
	 * Publish event, on the behalf of a device, to the IBM Watson IoT Platform. <br>
	 * This method will attempt to create a JSON obejct out of the payload
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param data
	 *            Payload data
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @return Whether the send was successful.
	 */
	public boolean publishEvent(String deviceType, String deviceId, String event, Object data, int qos) {
		return publishEvent(deviceType, deviceId, event, data, qos, getActionTimeout());
	}
	
	/**
	 * Publish event, on the behalf of a device, to the IBM Watson IoT Platform. <br>
	 * This method will attempt to create a JSON obejct out of the payload
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param data
	 *            Payload data
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @return Whether the send was successful.
	 */
	public boolean publishEvent(String deviceType, String deviceId, String event, Object data, int qos, long timeout) {
		if (!isConnected() && !isAutomaticReconnect()) {
			return false;
		}
		final String METHOD = "publishEvent(5)";
		String topic = "iot-2/type/" + deviceType + "/id/" + deviceId + "/evt/" + event + "/fmt/json";
		JsonObject payload = null;
		// Handle null object
		if(data == null) {
			data = new JsonObject();
		}
		if(newFormat == false) {
			payload = new JsonObject();
			String timestamp = ISO8601_DATE_FORMAT.format(new Date());
			payload.addProperty("ts", timestamp);
			JsonElement dataElement = gson.toJsonTree(data);
			payload.add("d", dataElement);
		} else {
			payload = (JsonObject) gson.toJsonTree(data);
		}
		
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic   = " + topic);
		LoggerUtility.fine(CLASS_NAME, METHOD, "Payload = " + payload.toString());
		
		MqttMessage msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		msg.setQos(qos);
		msg.setRetained(false);
		
		try {
			if (!isConnected() && isAutomaticReconnect()) {
				// Buffer message
				mqttAsyncClient.publish(topic, msg);
			} else {
				mqttAsyncClient.publish(topic, msg).waitForCompletion(timeout);
			}
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
			return false;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Publish event, on the behalf of a device, to the IBM Watson IoT Platform. <br>
	 * This method will attempt to create a JSON obejct out of the payload
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param data
	 *            Payload data
	 * @param format
	 * 			The message format
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @return Whether the send was successful.
	 * @throws Exception when the publish operation fails
	 */	
	public boolean publishEvent(String deviceType, String deviceId, String event, Object data, String format, int qos) throws Exception {
		return publishEvent(deviceType, deviceId, event, data, format, qos, getActionTimeout());
	}
	
	/**
	 * Publish event, on the behalf of a device, to the IBM Watson IoT Platform. <br>
	 * This method will attempt to create a JSON obejct out of the payload
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param data
	 *            Payload data
	 * @param format
	 * 			The message format
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 * @return Whether the send was successful.
	 * @throws Exception when the publish operation fails
	 */
	public boolean publishEvent(String deviceType, String deviceId, String event, Object data, String format, int qos, long timeout) throws Exception {
		if (!isConnected() && !isAutomaticReconnect()) {
			return false;
		}
		final String METHOD = "publishEvent(5)";
		String topic = "iot-2/type/" + deviceType + "/id/" + deviceId + "/evt/" + event + "/fmt/" + format;
		Object payload = null;
		MqttMessage msg = null;
		// Handle null object
		if(data == null) {
			data = new JsonObject();
		}
		if(data.getClass() == String.class) {
			payload = data;
			msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		} else if(data.getClass().getName().equals("[B")) { // checking for byte array
			msg = new MqttMessage((byte[]) data);
			payload = Arrays.toString((byte[]) data);
		} else {
			payload = gson.toJsonTree(data);
			msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		} 
		
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic   = " + topic);
		LoggerUtility.fine(CLASS_NAME, METHOD, "Payload = " + payload.toString());
		
		msg.setQos(qos);
		msg.setRetained(false);
		
		try {
			if (!isConnected() && isAutomaticReconnect()) {
				// Buffer message
				mqttAsyncClient.publish(topic, msg);
			} else {
				mqttAsyncClient.publish(topic, msg).waitForCompletion(timeout);
			}
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
			return false;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Publish command to the IBM Watson IoT Platform. <br>
	 * Note that data is published
	 * at Quality of Service (QoS) 0, which means that a successful send does not guarantee
	 * receipt even if the publish is successful.
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param data
	 *            Payload data
	 * @return Whether the send was successful.
	 */
	public boolean publishCommand(String deviceType, String deviceId, String command, Object data) {
		return publishCommand(deviceType, deviceId, command, data, 0);
	}

	/**
	 * Publish command to the IBM Watson IoT Platform. <br>
	 * This method will attempt to create a JSON obejct out of the payload
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param data
	 *            Payload data
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @return Whether the send was successful.
	 */	
	public boolean publishCommand(String deviceType, String deviceId, String command, Object data, int qos) {
		return publishCommand(deviceType, deviceId, command, data, qos, getActionTimeout());
	}

	/**
	 * Publish command to the IBM Watson IoT Platform. <br>
	 * This method will attempt to create a JSON obejct out of the payload
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param data
	 *            Payload data
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 * @return Whether the send was successful.
	 */
	public boolean publishCommand(String deviceType, String deviceId, String command, Object data, int qos, long timeout) {
		if (!isConnected() && !isAutomaticReconnect()) {
			return false;
		}
		final String METHOD = "publishCommand(5)";
		Object payload = null;
		String topic = "iot-2/type/" + deviceType + "/id/" + deviceId + "/cmd/" + command + "/fmt/json";
		// Handle null object
		if(data == null) {
			data = new JsonObject();
		}
		if(newFormat == false) {
			payload = new JsonObject();
			String timestamp = ISO8601_DATE_FORMAT.format(new Date());
			((JsonObject) payload).addProperty("ts", timestamp);
			JsonElement dataElement = gson.toJsonTree(data);
			((JsonObject) payload).add("d", dataElement);
		} else {
			payload = gson.toJsonTree(data);
		}
		
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic   = " + topic);
		LoggerUtility.fine(CLASS_NAME, METHOD, "Payload = " + payload.toString());
		
		MqttMessage msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		msg.setQos(qos);
		msg.setRetained(false);
		
		try {
			if (!isConnected() && isAutomaticReconnect()) {
				// Buffer message
				mqttAsyncClient.publish(topic, msg);
			} else {
				mqttAsyncClient.publish(topic, msg).waitForCompletion(timeout);
			}
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
			return false;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Publish event, on the behalf of a device, to the IBM Watson IoT Platform. <br>
	 * This method will attempt to create a JSON obejct out of the payload
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes the command
	 * @param data
	 *            Payload data
	 * @param format
	 * 			The message format
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @return Whether the send was successful.
	 * @throws Exception when the publish operation fails
	 */	
	public boolean publishCommand(String deviceType, String deviceId, String command, Object data, String format, int qos) throws Exception {
		return publishCommand(deviceType, deviceId, command, data, format, qos, getActionTimeout());
	}
	
	/**
	 * Publish event, on the behalf of a device, to the IBM Watson IoT Platform. <br>
	 * This method will attempt to create a JSON obejct out of the payload
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes the command
	 * @param data
	 *            Payload data
	 * @param format
	 * 			The message format
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 * @return Whether the send was successful.
	 * @throws Exception when the publish operation fails
	 */
	public boolean publishCommand(String deviceType, String deviceId, String command, Object data, String format, int qos, long timeout) throws Exception {
		if (!isConnected() && !isAutomaticReconnect()) {
			return false;
		}
		final String METHOD = "publishCommand(6)";
		String topic = "iot-2/type/" + deviceType + "/id/" + deviceId + "/cmd/" + command + "/fmt/"+ format;
		Object payload = null;
		MqttMessage msg = null;
		// Handle null object
		if(data == null) {
			data = new JsonObject();
		}
		if(data.getClass() == String.class) {
			payload = data;
			msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		} else if(data.getClass().getName().equals("[B")) { // checking for byte array
			msg = new MqttMessage((byte[]) data);
			payload = Arrays.toString((byte[]) data); // for debug
		} else {
			payload = (JsonObject) gson.toJsonTree(data);
			msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		}
		
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic   = " + topic);
		LoggerUtility.fine(CLASS_NAME, METHOD, "Payload = " + payload.toString());
		
		msg.setQos(qos);
		msg.setRetained(false);
		
		try {
			if (!isConnected() && isAutomaticReconnect()) {
				// Buffer message
				mqttAsyncClient.publish(topic, msg);
			} else {
				mqttAsyncClient.publish(topic, msg).waitForCompletion(timeout);
			}
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
			return false;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Subscribe to device events of the IBM Watson IoT Platform. <br>
	 * Quality of Service is set to 0
	 * All events, for the given org are subscribed to
	 */
	public void subscribeToDeviceEvents() {
		subscribeToDeviceEvents("+", "+", "+", 0);
	}

	/**
	 * Subscribe to device events of the IBM Watson IoT Platform. <br>
	 * Quality of Service is set to 0 <br>
	 * All events, for a given device type, are subscribed to
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 */
	public void subscribeToDeviceEvents(String deviceType) {
		subscribeToDeviceEvents(deviceType, "+", "+", 0);
	}

	/**
	 * Subscribe to device events of the IBM Watson IoT Platform. <br>
	 * Quality of Service is set to 0 <br>
	 * All events, of a given device type and device id , are subscribed to
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 */
	public void subscribeToDeviceEvents(String deviceType, String deviceId) {
		subscribeToDeviceEvents(deviceType, deviceId, "+", 0);
	}
	
	/**
	 * Unsubscribe from device events of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 */
	public void unsubscribeFromDeviceEvents(String deviceType, String deviceId) {
		unsubscribeFromDeviceEvents(deviceType, deviceId, "+");
	}

	/**
	 * Subscribe to device events of the IBM Watson IoT Platform. <br>
	 * Quality of Service is set to 0
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 */
	public void subscribeToDeviceEvents(String deviceType, String deviceId, String event) {
		subscribeToDeviceEvents(deviceType, deviceId, event, 0);
	}
	
	/**
	 * Subscribe to device events of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 */
	public void subscribeToDeviceEvents(String deviceType, String deviceId, String event, int qos) {
		subscribeToDeviceEvents(deviceType, deviceId, event, qos, getActionTimeout());
	}

	/**
	 * Subscribe to device events of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 */
	public void subscribeToDeviceEvents(String deviceType, String deviceId, String event, int qos, long timeout) {
		subscribeToDeviceEvents(deviceType, deviceId, event, "+", qos, timeout);
	}
	
	/**
	 * Subscribe to device events of the IBM Watson IoT Platform. <br>
	 * Quality of Service is set to 0
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 */
	public void subscribeToDeviceEvents(String deviceType, String deviceId, String event, String format) {
		subscribeToDeviceEvents(deviceType, deviceId, event, format, 0);
	}
	
	/**
	 * Subscribe to device events of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 */
	public void subscribeToDeviceEvents(String deviceType, String deviceId, String event, String format, int qos) {
		subscribeToDeviceEvents(deviceType, deviceId, event, format, qos, getActionTimeout());
	}
	
	/**
	 * Subscribe to device events of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 */
	public void subscribeToDeviceEvents(String deviceType, String deviceId, String event, String format, int qos, long timeout) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/evt/"+event+"/fmt/" + format;
			subscriptions.put(newTopic, new Integer(qos));
			mqttAsyncClient.subscribe(newTopic, qos).waitForCompletion(timeout);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Unsubscribe from device events of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 */
	public void unsubscribeFromDeviceEvents(String deviceType, String deviceId, String event) {
		unsubscribeFromDeviceEvents(deviceType, deviceId, event, "+");
	}

	/**
	 * Unsubscribe from device events of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 */
	public void unsubscribeFromDeviceEvents(String deviceType, String deviceId, String event, String format) {
		unsubscribeFromDeviceEvents(deviceType, deviceId, event, format, getActionTimeout());
	}
	
	/**
	 * Unsubscribe from device events of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 */
	public void unsubscribeFromDeviceEvents(String deviceType, String deviceId, String event, String format, long timeout) {
		try {
			String topic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/evt/"+event+"/fmt/" + format;
			subscriptions.remove(topic);
			mqttAsyncClient.unsubscribe(topic).waitForCompletion(timeout);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Unsubscribe from device commands, from the IBM Watson IoT Platform.
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 */
	public void unsubscribeFromDeviceCommands(String deviceType, String deviceId) {
		unsubscribeFromDeviceCommands(deviceType, deviceId, "+");
	}
	
	/**
	 * Unsubscribe from device commands of the IBM Watson IoT Platform.
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command name
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 */
	public void unsubscribeFromDeviceCommands(String deviceType, String deviceId, String command, String format) {
		unsubscribeFromDeviceCommands(deviceType, deviceId, command, format, getActionTimeout());
	}
	
	/**
	 * Unsubscribe from device commands of the IBM Watson IoT Platform.
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command name
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 */
	public void unsubscribeFromDeviceCommands(String deviceType, String deviceId, String command, String format, long timeout) {
		try {
			String topic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/cmd/" + command + "/fmt/" + format;
			subscriptions.remove(topic);
			mqttAsyncClient.unsubscribe(topic).waitForCompletion(timeout);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Unsubscribe from device commands, from the IBM Watson IoT Platform.
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes the command name
	 */
	public void unsubscribeFromDeviceCommands(String deviceType, String deviceId, String command) {
		unsubscribeFromDeviceCommands(deviceType, deviceId, command, "+");
	}
	
	/**
	 * Subscribe to device commands, on the behalf of a device, from the IBM Watson IoT Platform. <br>
	 * Quality of Service is set to 0
	 * All commands, for the given org are subscribed to
	 */
	public void subscribeToDeviceCommands() {
		subscribeToDeviceCommands("+", "+", "+", 0);
	}

	/**
	 * Subscribe to device commands, on the behalf of a device, from the IBM Watson IoT Platform. <br>
	 * Quality of Service is set to 0 <br>
	 * All commands, for a given device type, are subscribed to
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 */
	public void subscribeToDeviceCommands(String deviceType) {
		subscribeToDeviceCommands(deviceType, "+", "+", 0);
	}

		
	/**
	 * Subscribe to device commands, on the behalf of a device, from the IBM Watson IoT Platform. <br>
	 * Quality of Service is set to 0 <br>
	 * All commands, for a given device type and device id , are subscribed to
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 */
	public void subscribeToDeviceCommands(String deviceType, String deviceId) {
		subscribeToDeviceCommands(deviceType, deviceId, "+", 0);
	}
		
	/**
	 * Subscribe to device commands, on the behalf ofa device, for the IBM Watson IoT Platform. <br>
	 * Quality of Service is set to 0
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 */
	public void subscribeToDeviceCommands(String deviceType, String deviceId, String command) {
		subscribeToDeviceCommands(deviceType, deviceId, command, 0);
	}

	/**
	 * Subscribe to device commands, on the behalf of a device, of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 */	
	public void subscribeToDeviceCommands(String deviceType, String deviceId, String command, int qos) {
		subscribeToDeviceCommands(deviceType, deviceId, command, qos, getActionTimeout());
	}
	
	/**
	 * Subscribe to device commands, on the behalf of a device, of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @param timeout
	 *		The maximum amount of time to wait for the action, in milliseconds, to complete
	 */
	public void subscribeToDeviceCommands(String deviceType, String deviceId, String command, int qos, long timeout) {
		subscribeToDeviceCommands(deviceType, deviceId, command, "+", qos, timeout);
	}
	
	public void subscribeToDeviceCommands(String deviceType, String deviceId, String command, String format) {
		subscribeToDeviceCommands(deviceType, deviceId, command, format, getActionTimeout());
	}

	/**
	 * Subscribe to device commands, on the behalf of a device, for the IBM Watson IoT Platform. <br>
	 * Quality of Service is set to 0
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 */
	public void subscribeToDeviceCommands(String deviceType, String deviceId, String command, String format, long timeout) {
		subscribeToDeviceCommands(deviceType, deviceId, command, format, 0, timeout);
	}

	/**
	 * Subscribe to device commands, on the behalf of a device, of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 */
	public void subscribeToDeviceCommands(String deviceType, String deviceId, String command, String format, int qos) {
		subscribeToDeviceCommands(deviceType, deviceId, command, format, qos, getActionTimeout());
	}
	
	/**
	 * Subscribe to device commands, on the behalf of a device, of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 */
	public void subscribeToDeviceCommands(String deviceType, String deviceId, String command, String format, int qos, long timeout) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/cmd/"+ command +"/fmt/" + format;
			subscriptions.put(newTopic, new Integer(qos));			
			mqttAsyncClient.subscribe(newTopic, qos).waitForCompletion(timeout);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Subscribe to device status of the IBM Watson IoT Platform. <br>
	 * All the devices, for an org, are monitored
	 */
	public void subscribeToDeviceStatus() {
		subscribeToDeviceStatus("+", "+");
	}

	/**
	 * Subscribe to device status of the IBM Watson IoT Platform. <br>
	 * All the devices of a given device type are subscribed to
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 */
	public void subscribeToDeviceStatus(String deviceType) {
		subscribeToDeviceStatus(deviceType, "+");
	}
	
	/**
	 * Subscribe to device status of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 */
	public void subscribeToDeviceStatus(String deviceType, String deviceId) {
		subscribeToDeviceStatus(deviceType, deviceId, getActionTimeout());
	}
	
	/**
	 * Subscribe to device status of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 */
	public void subscribeToDeviceStatus(String deviceType, String deviceId, long timeout) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/mon";
			subscriptions.put(newTopic, new Integer(0));			
			mqttAsyncClient.subscribe(newTopic, 0).waitForCompletion(timeout);;
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Unsubscribe from device status of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 */
	public void unSubscribeFromDeviceStatus(String deviceType, String deviceId) {
		unSubscribeFromDeviceStatus(deviceType, deviceId, getActionTimeout());
	}
	
	/**
	 * Unsubscribe from device status of the IBM Watson IoT Platform. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 */
	public void unSubscribeFromDeviceStatus(String deviceType, String deviceId, long timeout) {
		try {
			String topic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/mon";
			mqttAsyncClient.unsubscribe(topic).waitForCompletion(timeout);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Subscribe to application status of the IBM Watson IoT Platform. <br>
	 */
	public void subscribeToApplicationStatus() {
		subscribeToApplicationStatus("+");
	}

	/**
	 * Subscribe to application status of the IBM Watson IoT Platform. <br>
	 * 
	 * @param appId 
	 * 		object of String which denotes the application uniquely in the organization
	 */
	public void subscribeToApplicationStatus(String appId) {
		subscribeToApplicationStatus(appId, getActionTimeout());
	}
	
	/**
	 * Subscribe to application status of the IBM Watson IoT Platform. <br>
	 * 
	 * @param appId 
	 * 		object of String which denotes the application uniquely in the organization
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 */
	public void subscribeToApplicationStatus(String appId, long timeout) {
		try {
			String newTopic = "iot-2/app/"+appId+"/mon";
			subscriptions.put(newTopic, new Integer(0));			
			mqttAsyncClient.subscribe(newTopic, 0).waitForCompletion(timeout);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Unsubscribe from application status of the IBM Watson IoT Platform. <br>
	 * 
	 * @param appId 
	 * 		object of String which denotes the application uniquely in the organization
	 */
	public void unSubscribeFromApplicationStatus(String appId) {
		unSubscribeFromApplicationStatus(appId, getActionTimeout());
	}
	/**
	 * Unsubscribe from application status of the IBM Watson IoT Platform. <br>
	 * 
	 * @param appId 
	 * 		object of String which denotes the application uniquely in the organization
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 */
	public void unSubscribeFromApplicationStatus(String appId, long timeout) {
		try {
			String topic = "iot-2/app/"+appId+"/mon";
			mqttAsyncClient.unsubscribe(topic).waitForCompletion(timeout);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * If we lose connection trigger the connect logic to attempt to
	 * reconnect to the IBM Watson IoT Platform.
	 */
	public void connectionLost(Throwable e) {
		final String METHOD = "connectionLost";
		LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, e.getMessage(), e);
		try {
			connect();
			Iterator<Entry<String, Integer>> iterator = subscriptions.entrySet().iterator();
		    LoggerUtility.info(CLASS_NAME, METHOD, "Resubscribing....");
		    while (iterator.hasNext() && this.isConnected()) {
		        //Map.Entry pairs = (Map.Entry)iterator.next();
		        Entry<String, Integer> pairs = iterator.next();
		        LoggerUtility.info(CLASS_NAME, METHOD, pairs.getKey() + " = " + pairs.getValue());
		        try {
		        	mqttAsyncClient.subscribe(pairs.getKey().toString(), Integer.parseInt(pairs.getValue().toString())).waitForCompletion(getActionTimeout());
				} catch (NumberFormatException | MqttException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
//		        iterator.remove(); // avoids a ConcurrentModificationException
		    }
		} catch (MqttException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	    
	}
	
	/**
	 * A completed deliver does not guarantee that the message is recieved by the service
	 * because devices send messages with Quality of Service (QoS) 0. The message count
	 * represents the number of messages that were sent by the device without an error on
	 * from the perspective of the device.
	 */
	public void deliveryComplete(IMqttDeliveryToken token) {
		final String METHOD = "deliveryComplete";
		LoggerUtility.fine(CLASS_NAME, METHOD, "token = "+token.getMessageId());
		messageCount++;
	}
	
	/**
	 * The Application client does not currently support subscriptions.
	 */
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		final String METHOD = "messageArrived";
		if (eventCallback != null) {
			/* Only check whether the message is a device event if a callback 
			 * has been defined for events, otherwise it is a waste of time
			 * as without a callback there is nothing to process the generated
			 * event.
			 */
			Matcher matcher = DEVICE_EVENT_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String type = matcher.group(1);
				String id = matcher.group(2);
				String event = matcher.group(3);
				String format = matcher.group(4);
				Event evt = new Event(type, id, event, format, msg);

				if(evt.getTimestamp() != null) {
					LoggerUtility.fine(CLASS_NAME, METHOD, "Event received: " + evt.toString());
					eventCallback.processEvent(evt);					
				} else {
					LoggerUtility.warn(CLASS_NAME, METHOD, "Event is not formatted properly, so not processing");						
				}

				return;
		    }

			matcher = DEVICE_COMMAND_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String type = matcher.group(1);
				String id = matcher.group(2);
				String command = matcher.group(3);
				String format = matcher.group(4);
				Command cmd = new Command(type, id, command, format, msg);
			
				if(cmd.getTimestamp() != null ) {
					LoggerUtility.fine(CLASS_NAME, METHOD, "Command received: " + cmd.toString());	
					eventCallback.processCommand(cmd);					
				} else {
					LoggerUtility.warn(CLASS_NAME, METHOD, "Command is not formatted properly, so not processing");					
				}

				return;
		    }

		}
		
		if (statusCallback != null) {
			/* Only check whether the message is a status event if a callback 
			 * has been defined for status events, otherwise it is a waste of time
			 * as without a callback there is nothing to process the generated
			 * event.
			 */
			Matcher matcher = DEVICE_STATUS_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String type = matcher.group(1);
				String id = matcher.group(2);
				DeviceStatus status = new DeviceStatus(type, id, msg);
				LoggerUtility.fine(CLASS_NAME, METHOD, "Device status received: " + status.toString());
				statusCallback.processDeviceStatus(status);
		    }
			
			matcher = APP_STATUS_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String id = matcher.group(1);
				ApplicationStatus status = new ApplicationStatus(id, msg);
				LoggerUtility.fine(CLASS_NAME, METHOD, "Application status received: " + status.toString());
				statusCallback.processApplicationStatus(status);
		    }
		}
	}


	public void setEventCallback(EventCallback callback) {
		this.eventCallback  = callback;
	}

	public void setStatusCallback(StatusCallback callback) {
		this.statusCallback  = callback;
	}
	
	/**
	 * Publish an event to the IBM Watson IoT Platform using HTTP(S) <br>
	 * @deprecated This API is exposed in APIClient
	 * <br> Use this {@link com.ibm.iotf.client.api.APIClient#publishApplicationEventforDeviceOverHTTP}
 	 * @param deviceType	Device Type
	 * @param deviceId		Device ID
	 * @param eventName  Name of the dataset under which to publish the data
	 * @param payload Object to be added to the payload as the dataset
	 * @return httpcode the return code
	 * @throws Exception if the operation is not successful
	 */
	@Deprecated public int publishEventOverHTTP(String deviceType,
									String deviceId,
									String eventName, 
									Object payload) throws Exception {
		return publishEventsThroughHttps(this.getOrgId(), this.getDomain(), deviceType, deviceId, 
				eventName, false, this.getAuthKey(), this.getAuthToken(), payload);
	}

}
