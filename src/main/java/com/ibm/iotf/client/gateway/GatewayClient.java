/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iotf.client.gateway;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.AbstractClient;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.util.LoggerUtility;

/**
 * A client, used by Gateway, that simplifies the Gateway interactions with IBM Watson IoT Platform. <br>
 * 
 * <p>Gateways are a specialized class of devices in Watson IoT Platform which serve as access points to the 
 * Watson IoT Platform for other devices. Gateway devices have additional permission when compared to 
 * regular devices and can perform the following  functions:</p>
 * 
 * <ul class="simple">
 * <li>Register new devices to Watson IoT Platform
 * <li>Send and receive its own sensor data like a directly connected device,
 * <li>Send and receive data on behalf of the devices connected to it
 * <li>Run a device management agent, so that it can be managed, also manage the devices connected to it
 * </ul>
 * 
 * <p>Refer to the <a href="https://docs.internetofthings.ibmcloud.com/gateways/mqtt.html">documentation</a> for more information about the 
 * Gateway support in Watson IoT Platform.</p>
 * 
 * This is a derived class from AbstractClient.
 */
public class GatewayClient extends AbstractClient implements MqttCallbackExtended{
	
	private static final String CLASS_NAME = GatewayClient.class.getName();
	
	private static final Pattern GATEWAY_NOTIFICATION_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/notify");
	private static final Pattern GATEWAY_COMMAND_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/cmd/(.+)/fmt/(.+)");
	
	private GatewayCallback gwCommandCallback = null;
	
	private HashMap<String, Integer> subscriptions = new HashMap<String, Integer>();
	
	private APIClient apiClient = null;
	
	/**
	 * <p>Create a Gateway client for the IBM Watson IoT Platform using the properties file passed. The
	 * properties must have the following definitions,</p>
	 * 
	 * <ul class="simple">
	 * <li>org - Your organization ID.
	 * <li>type - The type of your Gateway device.
	 * <li>id - The ID of your Gateway.
	 * <li>auth-method - Method of authentication (The only value currently supported is "token").
	 * <li>auth-token - API key token.
	 * </ul>
	 * @param options The Properties object creates definitions which are used to interact 
	 * with the Watson Internet of Things Platform module.
	 * 
	 * @throws Exception Failure in parsing the properties passed 
	 */
	public GatewayClient(Properties options) throws Exception {
		super(options);
		if(getOrgId()==null){
			
			throw new Exception("Invalid Auth Key");
		} else if(getOrgId().equalsIgnoreCase("quickstart")) {
			throw new Exception("There is no quickstart support for Gateways");
		}
		
		this.clientId = "g" + CLIENT_ID_DELIMITER + getOrgId() + 
				CLIENT_ID_DELIMITER + this.getGWDeviceType()  
				+ CLIENT_ID_DELIMITER + getGWDeviceId();
		
		if (getAuthMethod() == null) {
			this.clientUsername = null;
			this.clientPassword = null;
		}
		else if (!getAuthMethod().equals("token")) {
			throw new Exception("Unsupported Authentication Method: " + getAuthMethod());
		}
		else {
			// use-token-auth is the only authentication method currently supported
			this.clientUsername = "use-token-auth";
			this.clientPassword = getAuthToken();
		}
		createClient(this);
		options.setProperty("auth-method", "gateway");
		
		this.apiClient = new APIClient(options);
	}
	
	/**
	 * This constructor allows external user to pass the existing MqttAsyncClient 
	 * @param mqttAsyncClient MqttAsyncClient with the Watson IoT Platform connectivity details
	 */
	protected GatewayClient(MqttAsyncClient mqttAsyncClient) {
		super(mqttAsyncClient);
	}

	/**
	 * This constructor allows external user to pass the existing MqttClient 
	 * @param mqttClient MqttClient with the Watson IoT Platform connectivity details
	 */
	protected GatewayClient(MqttClient mqttClient) {
		super(mqttClient);
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
	
	/**
	 * Returns the IBM Watson IoT Platform Organization ID for this client.
	 * 
	 * @return orgid Organization ID
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
	
	/*
	 * old style - id
	 * new style - Device-ID
	 */
	public String getGWDeviceId() {
		String id;
		id = options.getProperty("Gateway-ID");
		if(id == null) {
			return super.getDeviceId();
		}
		return trimedValue(id);
	}
	
	/**
	 * Returns the Gateway ID
	 */
	public String getDeviceId() {
		return this.getGWDeviceId();
	}
	
	public String getGWDeviceType() {
		String type;
		type = options.getProperty("Gateway-Type");
		if(type == null) {
			type = options.getProperty("type");
		}
		if(type == null) {
			type = options.getProperty("Device-Type");
		}
		return trimedValue(type);
	}
	
	/**
	 * Accessor method to retrieve auth key
	 * @return authKey
	 * 					String authKey
	 */
	private String getAuthKey() {
		String authKeyPassed = options.getProperty("auth-key");
		if(authKeyPassed == null) {
			authKeyPassed = options.getProperty("API-Key");
		}
		return trimedValue(authKeyPassed);
	}
	
	
	/**
	 * <p>Connects the Gateway to IBM Watson Internet of Things Platform. 
	 * After the successful connection to the IBM Watson IoT Platform, 
	 * the Gateway client can perform the following operations,</p>
	 * 
	 * <ul class="simple">
	 * <li>Publish events for itself and on behalf of devices connected behind the Gateway.
	 * <li>Subscribe to commands for itself and on behalf of devices behind the Gateway.
	 * </ul>
	 * 
	 * 
	 * <p>The GatewayClient retries when there is a connect exception.<br>
	 * 
	 * This method does not retry when the following exceptions occur.</p>
	 * 
	 * <ul class="simple">
	 *  <li> MqttSecurityException - One or more credentials are wrong
	 * 	<li>UnKnownHostException - Host doesn't exist. For example, a wrong organization name is used to connect.
	 * </ul>
	 * 
	 * @throws MqttException see above
	 **/
	public void connect() throws MqttException {
		super.connect(true);
		subscribeToGatewayCommands();
	}
	
	/**
	 * <p>Connects the Gateway to IBM Watson Internet of Things Platform. 
	 * After the successful connection to the IBM Watson IoT Platform, 
	 * the Gateway client can perform the following operations,</p>
	 * 
	 * <ul class="simple">
	 * <li>Publish events for itself and on behalf of devices connected behind the Gateway.
	 * <li>Subscribe to commands for itself and on behalf of devices behind the Gateway.
	 * </ul>
	 * 
	 * <p>The GatewayClient retries when there is a connect exception based on the 
	 * value set in retry parameter. <br>
	 * 
	 * This method does not retry when the following exceptions occur.</p>
	 * 
	 * <ul class="simple">
	 *  <li> MqttSecurityException - One or more credentials are wrong
	 * 	<li>UnKnownHostException - Host doesn't exist. For example, a wrong organization name is used to connect.
	 * </ul>
	 * 
	 * @param autoRetry - tells whether to retry the connection when the connection attempt fails.
	 * @throws MqttException see above
	 **/
	public void connect(boolean autoRetry) throws MqttException {
		super.connect(autoRetry);
		subscribeToGatewayCommands();
	}
	
	/**
	 * <p>Connects the Gateway to IBM Watson Internet of Things Platform. 
	 * After the successful connection to the IBM Watson IoT Platform, 
	 * the Gateway client can perform the following operations,</p>
	 * 
	 * <ul class="simple">
	 * <li>Publish events for itself and on behalf of devices connected behind the Gateway.
	 * <li>Subscribe to commands for itself and on behalf of devices behind the Gateway.
	 * </ul>
	 * 
	 * <p>The GatewayClient retries when there is a connect exception based on the 
	 * value set in retry parameter. <br>
	 * 
	 * This method does not retry when the following exceptions occur.</p>
	 * 
	 * <ul class="simple">
	 *  <li> MqttSecurityException - One or more credentials are wrong
	 * 	<li>UnKnownHostException - Host doesn't exist. For example, a wrong organization name is used to connect.
	 * </ul>
	 * 
	 * @param numberOfRetryAttempts - How many number of times to retry when there is a failure in connecting to Watson
	 * IoT Platform.
	 * @throws MqttException see above
	 **/
	@Override
	public void connect(int numberOfRetryAttempts) throws MqttException {
		super.connect(numberOfRetryAttempts);
		subscribeToGatewayCommands();
	}
	
	/**
	 * <p>While Gateway publishes events on behalf of the devices connected behind, 
	 * the Gateway can publish its own events as well. This method publishes the event with the 
	 * specified name and specified QOS.</p>
	 * 
	 * <p>Note that data is published at Quality of Service (QoS) 0, which means that 
	 * a successful send does not guarantee receipt even if the publish has been successful.</p>
	 * 
	 * @param event
	 *            Name of the dataset under which to publish the data
	 * @param data
	 *            Object to be added to the payload as the dataset
	 * @return Whether the send was successful.
	 */
	public boolean publishGatewayEvent(String event, Object data) {
		return publishDeviceEvent(this.getGWDeviceType(), this.getGWDeviceId(), event, data, 0);
	}

	/**
	 * <p>While Gateway publishes events on behalf of the devices connected to it, 
	 * the Gateway can publish its own events as well. This method publishes event with the 
	 * specified name and specified QOS.</p>
	 * 
	 * This method allows QoS to be passed as an argument
	 * 
	 * @param event
	 *            Name of the dataset under which to publish the data
	 * @param data
	 *            Object to be added to the payload as the dataset
	 * @param qos
	 *            Quality of Service - should be 0, 1 or 2
	 * @return Whether the send was successful.
	 */	
	public boolean publishGatewayEvent(String event, Object data, int qos) {
		return publishDeviceEvent(this.getGWDeviceType(), this.getGWDeviceId(), event, data, qos);
	}
	
	/**
	 * <p>While Gateway publishes events on behalf of the devices connected to it, 
	 * the Gateway can publish its own events as well. This method publishes event with the 
	 * specified name and specified QOS.</p>
	 * 
	 * This method allows QoS to be passed as an argument
	 * 
	 * @param event
	 *            Name of the dataset under which to publish the data
	 * @param data
	 *            Object to be added to the payload as the dataset
	 * @param format the format of the event
	 * @param qos
	 *            Quality of Service - should be 0, 1 or 2
	 * @return Whether the send was successful.
	 * @throws Exception when the publish operation fails
	 */	
	public boolean publishGatewayEvent(String event, Object data, String format, int qos) throws Exception {
		return publishDeviceEvent(this.getGWDeviceType(), this.getGWDeviceId(), event, data, format, qos);
	}
	
	/**
	 * <p>Publish the event on behalf of a device to the IBM Watson IoT Platform. </p> 
	 * Note that data is published at Quality of Service (QoS) 0, which means that a successful send does not guarantee
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
	public boolean publishDeviceEvent(String deviceType, String deviceId, String event, Object data) {
		return publishDeviceEvent(deviceType, deviceId, event, data, 0);
	}
	
	/**
	 * Publish an event on the behalf of a device to the IBM Watson IoT Platform. <br>
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
	public boolean publishDeviceEvent(String deviceType, String deviceId, String event, Object data, int qos) {
		if (!isConnected() && !isAutomaticReconnect()) {
			return false;
		}
		final String METHOD = "publishEvent(5)";
		Object payload = null;
		String topic = "iot-2/type/" + deviceType + "/id/" + deviceId + "/evt/" + event + "/fmt/json";
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
			if (isConnected() && !isAutomaticReconnect()) {
				mqttAsyncClient.publish(topic, msg).waitForCompletion();
			} else {
				mqttAsyncClient.publish(topic, msg);
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
	public boolean publishDeviceEvent(String deviceType, String deviceId, String event, Object data, String format, int qos) throws Exception {
		if (!isConnected()) {
			return false;
		}
		final String METHOD = "publishEvent(6)";
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
			if (isConnected() && !isAutomaticReconnect()) {
				mqttAsyncClient.publish(topic, msg).waitForCompletion();
			} else {
				mqttAsyncClient.publish(topic, msg);
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


	/*
	 * This method reconnects when the connection is lost due to n/w interruption
	 */
	protected void reconnect() {
		try {
			super.connect(true);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		subscribeToGatewayCommands();
	}
	
	private void subscribeToGatewayCommands() {
		subscribeToDeviceCommands(this.getGWDeviceType(), this.getGWDeviceId());
	}
	
	/**
	 * <p>Subscribe to device commands, on the behalf of a device, to the IBM Watson IoT Platform. <br>
	 * Note that the, Quality of Service is set to 0. </p>
	 * This method subscribes to all commands, for a given device type and device id.
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 */
	public void subscribeToDeviceCommands(String deviceType, String deviceId) {
		subscribeToDeviceCommands(deviceType, deviceId, "+", 0);
	}
	
	/**
	 * Unsubscribe from device commands, on the behalf of a device, from the IBM Watson IoT Platform.
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
	 * Subscribe to device commands, on the behalf of a device, to the IBM Watson IoT Platform. <br>
	 * Quality of Service is set to 0.
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
	 * Subscribe to device commands, on the behalf of a device, to the IBM Watson IoT Platform. <br>
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
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/cmd/" + command + "/fmt/+";
			subscriptions.put(newTopic, new Integer(qos));
			mqttAsyncClient.subscribe(newTopic, qos);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Unsubscribe from device commands, on the behalf of a device, from the IBM Watson IoT Platform.
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes the command name
	 */
	public void unsubscribeFromDeviceCommands(String deviceType, String deviceId, String command) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/cmd/" + command + "/fmt/+";
			subscriptions.remove(newTopic);
			mqttAsyncClient.unsubscribe(newTopic);

		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public void subscribeToGatewayNotification() {
		String newTopic = "iot-2/type/"+this.getGWDeviceType() +"/id/" +this.getGWDeviceId() + "/notify";
		subscriptions.put(newTopic, 0);
		try {
			mqttAsyncClient.subscribe(newTopic, 0);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Subscribe to device commands, on the behalf of a device, to the IBM Watson IoT Platform. <br>
	 * Quality of Service is set to 0
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 */

	public void subscribeToDeviceCommands(String deviceType, String deviceId, String command, String format) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/cmd/" + command + "/fmt/" + format;
			subscriptions.put(newTopic, new Integer(0));
			mqttAsyncClient.subscribe(newTopic, 0);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Subscribe to device commands, on the behalf of a device, to the IBM Watson IoT Platform. <br>
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
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/cmd/"+ command +"/fmt/" + format;
			subscriptions.put(newTopic, new Integer(qos));			
			mqttAsyncClient.subscribe(newTopic, qos);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Unsubscribe from device commands, on the behalf of a device, from the IBM Watson IoT Platform.
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
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/cmd/"+ command +"/fmt/" + format;
			subscriptions.remove(newTopic);
			mqttAsyncClient.unsubscribe(newTopic);

		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>This method is called by the MQTT library when the connection to the 
	 * IBM Watson Platform is lost. </p> 
	 * 
	 * This Watson IoT library will start the reconnect process, and application doesn't need to worry 
	 */
	public void connectionLost(Throwable e) {
		final String METHOD = "connectionLost";
		LoggerUtility.info(CLASS_NAME, METHOD, "Connection lost: " + e.getMessage());
		try {
			if (this.isAutomaticReconnect() == false) {
				connect();
			}
			if (this.isCleanSession() == true) {
			    Iterator<Entry<String, Integer>> iterator = subscriptions.entrySet().iterator();
			    LoggerUtility.info(CLASS_NAME, METHOD, "Resubscribing....");
			    while (iterator.hasNext()) {
			        //Map.Entry pairs = (Map.Entry)iterator.next();
			        Entry<String, Integer> pairs = iterator.next();
			        LoggerUtility.info(CLASS_NAME, METHOD, pairs.getKey() + " = " + pairs.getValue());
			        try {
			        	mqttAsyncClient.subscribe(pairs.getKey().toString(), Integer.parseInt(pairs.getValue().toString()));
					} catch (NumberFormatException | MqttException e1) {
						e1.printStackTrace();
					}
	//		        iterator.remove(); // avoids a ConcurrentModificationException
			    }
			}
		} catch (MqttException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
	/**
	 * <p>This method is called by the MQTT library when a message is delivered successfully.</p> 
	 * 
	 * A completed delivery does not guarantee that the message is received by the service
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
	 * <p>This method is called by the MQTT library when a message(command) is sent by the IBM Watson IoT Platform.
	 * </p>
	 * The message(command) will be processed by this class and corresponding callback method will be called if
	 * registered.
	 */
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		final String METHOD = "messageArrived";
		if (gwCommandCallback != null) {
			/* Only check whether the message is a application command if a callback 
			 * has been defined for commands, otherwise it is a waste of time
			 * as without a callback there is nothing to process the generated
			 * event.
			 */
			Matcher matcher = GATEWAY_COMMAND_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String type = matcher.group(1);
				String id = matcher.group(2);
				String command = matcher.group(3);
				String format = matcher.group(4);
				Command cmd = new Command(type, id, command, format, msg);
			
				if(cmd.getTimestamp() != null ) {
					LoggerUtility.fine(CLASS_NAME, METHOD, "Command received: " + cmd.toString());	
					gwCommandCallback.processCommand(cmd);					
				} else {
					LoggerUtility.warn(CLASS_NAME, METHOD, "Command is not formatted properly, so not processing");					
				}

				return;
		    }

			matcher = GATEWAY_NOTIFICATION_PATTERN.matcher(topic);
			if(matcher.matches()) {
				String type = matcher.group(1);
				String id = matcher.group(2);	
			}

		}
	}

	/**
	 * <p>Register the {@link com.ibm.iotf.client.gateway.GatewayCallback} class to the Gateway, so that the 
	 * {@link com.ibm.iotf.client.gateway.GatewayCallback#processCommand(com.ibm.iotf.client.gateway.Command)} method gets called when 
	 * command is received for the given subscription.</p> 
	 * 
	 * <p>Also, the 
	 * {@link com.ibm.iotf.client.gateway.GatewayCallback#processNotification(com.ibm.iotf.client.gateway.Notification)} method gets called when 
	 * any notification is received. Note that you one must have called subscribeToGatewayNotification inorder
	 * to get the notification.</p> 
	 * 
	 * The messages are returned as an instance of the {@link com.ibm.iotf.client.gateway.Command}. 
	 * 
	 * @param callback an instance of {@link com.ibm.iotf.client.gateway.GatewayCallback}
	 */
	public void setGatewayCallback(GatewayCallback callback) {
		this.gwCommandCallback  = callback;
	}

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		final String METHOD = "connectComplete";
		if (reconnect) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Reconnected to " + serverURI );
		}
	}

}
