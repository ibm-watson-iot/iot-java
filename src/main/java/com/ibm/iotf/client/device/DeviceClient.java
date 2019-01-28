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
package com.ibm.iotf.client.device;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.AbstractClient;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.util.LoggerUtility;


/**
 * A client, used by device, that handles connections with the IBM Watson IoT Platform. <br>
 * 
 * This is a derived class from AbstractClient and can be used by embedded devices to handle connections with IBM Watson IoT Platform.
 */
public class DeviceClient extends AbstractClient implements MqttCallbackExtended {
	
	private static final String CLASS_NAME = DeviceClient.class.getName();
	
	private static final Pattern COMMAND_PATTERN = Pattern.compile("iot-2/cmd/(.+)/fmt/(.+)");
	
	private CommandCallback commandCallback = null;

	private APIClient apiClient;
	
	/**
	 * This constructor allows external user to pass the existing MqttAsyncClient 
	 * @param mqttAsyncClient The MQTTAsyncClient with connectivity details
	 */
	protected DeviceClient(MqttAsyncClient mqttAsyncClient) {
		super(mqttAsyncClient);
	}

	/**
	 * This constructor allows external user to pass the existing MqttClient 
	 * @param mqttClient The MQTTClient with Watson IoT Platform connectivity details
	 */
	protected DeviceClient(MqttClient mqttClient) {
		super(mqttClient);
	}
	/**
	 * Create a device client for the IBM Watson IoT Platform. <br>
	 * 
	 * @param options the list of options containing the device registration details
	 * @throws Exception When there is a failure in parsing the properties passed 
	 */
	public DeviceClient(Properties options) throws Exception {
		super(options);
		LoggerUtility.fine(CLASS_NAME, "DeviceClient", "options   = " + options);
		this.clientId = "d" + CLIENT_ID_DELIMITER + getOrgId() + CLIENT_ID_DELIMITER + getDeviceType() + CLIENT_ID_DELIMITER + getDeviceId();
		//this.clientId = "d:f71n0g:devicetype:device1";
		
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
		
		options.setProperty("auth-method", "device");
		
		this.apiClient = new APIClient(options);
	}
	
	/*
	 * old style - type
	 * new style - Device-Type
	 */
	public String getDeviceType() {
		String type;
		type = options.getProperty("type");
		if(type == null) {
			type = options.getProperty("Device-Type");
		}
		return trimedValue(type);
	}

	public String getFormat() {
		String format = options.getProperty("format");
		if(format != null && ! format.equals(""))
			return format;
		else
			return "json";
		
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
	 * @throws MqttException see above
	 **/
	public void connect() throws MqttException {
		super.connect(true);
		if (!getOrgId().equals("quickstart")) {
			subscribeToCommands();
		}
	}
	
	/**
	 * <p>Connects the device to IBM Watson IoT Platform and retries when there is an exception 
	 * based on the value set in retry parameter. <br>
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
	@Override
	public void connect(boolean autoRetry) throws MqttException {
		super.connect(autoRetry);
		if (!getOrgId().equals("quickstart")) {
			subscribeToCommands();
		}
	}
	
	/**
	 * <p>Connects the device to IBM Watson IoT Platform and retries when there is an exception 
	 * based on the value set in retry parameter. <br>
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
		if (!getOrgId().equals("quickstart")) {
			subscribeToCommands();
		}
	}
	
	/*
	 * This method reconnects when the connection is lost due to n/w interruption
	 */
	protected void reconnect() throws MqttException {
		if (this.isAutomaticReconnect() == false) {
			super.connect(true);
			if (!getOrgId().equals("quickstart") && this.isCleanSession()) {
				subscribeToCommands();
			}
		}
	}
	
	private void subscribeToCommands() throws MqttException{
		try {
			mqttAsyncClient.subscribe("iot-2/cmd/+/fmt/+", 2).waitForCompletion(getActionTimeout());
		} catch (MqttException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Returns the {@link com.ibm.iotf.client.api.APIClient} that allows the users to interact with 
	 * Watson IoT Platform API's to publish device events using HTTPS.
	 * 
	 * @return APIClient
	 */
	public APIClient api() {
		return this.apiClient;
	}

	/**
	 * Publish data to the IBM Watson IoT Platform.<br>
	 * Note that data is published
	 * at Quality of Service (QoS) 0, which means that a successful send does not guarantee
	 * receipt even if the publish has been successful.
	 * 
	 * @param event
	 *            Name of the dataset under which to publish the data
	 * @param data
	 *            Object to be added to the payload as the dataset
	 * @return Whether the send was successful.
	 */
	public boolean publishEvent(String event, Object data) {
		return publishEvent(event, data, 0, getActionTimeout());
	}
	
	/**
	 * Publish data to the IBM Watson IoT Platform.<br>
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
	public boolean publishEvent(String event, Object data, int qos) {
		return publishEvent(event, data, qos, getActionTimeout());
	}

	/**
	 * Publish data to the IBM Watson IoT Platform.<br>
	 * 
	 * This method allows QoS to be passed as an argument
	 * 
	 * @param event
	 *            Name of the dataset under which to publish the data
	 * @param data
	 *            Object to be added to the payload as the dataset
	 * @param qos
	 *            Quality of Service - should be 0, 1 or 2
	 * @param timeout
	 * 		The maximum amount of time to wait for the action, in milliseconds, to complete
	 * @return Whether the send was successful.
	 */	
	public boolean publishEvent(String event, Object data, int qos, long timeout) {
		if (!isConnected() && !isAutomaticReconnect()) {
			return false;
		}
		
		final String METHOD = "publishEvent(2)";
		Object payload = null;
		String topic = "iot-2/evt/" + event + "/fmt/json";
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
				mqttAsyncClient.publish(topic, msg).waitForCompletion(timeout);
			} else {
				mqttAsyncClient.publish(topic, msg);
			}
		} catch (MqttPersistenceException e) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, e.getMessage(), e);
			return false;
		} catch (MqttException e) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, e.getMessage(), e);
			return false;
		}
		return true;
	}

	/**
	 * Publish data to the IBM Watson IoT Platform.<br>
	 * 
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
	public boolean publishEvent(String event, Object data, String format, int qos) throws Exception {
		return publishEvent(event, data, format, qos, getActionTimeout());
	}
	
	/**
	 * Publish data to the IBM Watson IoT Platform.<br>
	 * 
	 * @param event
	 *            object of String which denotes event
	 * @param data
	 *            Payload data
	 * @param format
	 * 			The message format
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @param timeout
	 * 			The maximum amount of time to wait for the action, in milliseconds, to complete
	 * @return Whether the send was successful.
	 * @throws Exception when the publish operation fails
	 */
	public boolean publishEvent(String event, Object data, String format, int qos, long timeout) throws Exception {
		if (!isConnected() && !isAutomaticReconnect()) {
			return false;
		}
		final String METHOD = "publishEvent(4)";
		String topic = "iot-2/evt/" + event + "/fmt/" + format;
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
			payload = Arrays.toString((byte[]) data);
			msg = new MqttMessage((byte[]) data);
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
				mqttAsyncClient.publish(topic, msg).waitForCompletion(timeout);
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

	
	public void setCommandCallback(CommandCallback callback) {
		this.commandCallback  = callback;
	}
	
	/**
	 * @deprecated
	 * <br> Use this {@link com.ibm.iotf.client.api.APIClient#publishDeviceEventOverHTTP(String eventId, JsonObject payload, ContentType contenttype)} method instead 
	 * Publish an event to the IBM Watson IoT Platform using HTTP(S)<br>
	 * 
	 * @param eventName  Name of the dataset under which to publish the data
	 * @param payload Object to be added to the payload as the dataset
	 * @return httpcode the return code
	 * @throws Exception if the operation is not successful
	 */
	public int publishEventOverHTTP(String eventName, Object payload) throws Exception {
		String authKey = "use-token-auth";
		return publishEventsThroughHttps(this.getOrgId(), this.getDomain(), this.getDeviceType(), this.getDeviceId(), 
				eventName, true, authKey, this.getAuthToken(), payload);
	}	

	/**
	 * Simply log error when connection is lost
	 */
	public void connectionLost(Throwable e) {
		final String METHOD = "connectionLost";
		LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Lost connection client (" + clientId + ") : " + e.getMessage());
		if (e instanceof MqttException) {
			MqttException e2 = (MqttException) e;
			LoggerUtility.info(CLASS_NAME, METHOD, "Connection lost: Reason Code: " 
					+ e2.getReasonCode() + " Cause: " + ExceptionUtils.getRootCauseMessage(e2));
		} else {
			LoggerUtility.info(CLASS_NAME, METHOD, "Connection lost: " + e.getMessage());
		}
		
	}
	
	/**
	 * A completed deliver does not guarantee that the message is received by the service
	 * because devices send messages with Quality of Service (QoS) 0. <br>
	 * 
	 * The message count
	 * represents the number of messages that were sent by the device without an error on
	 * from the perspective of the device.
	 * @param token
	 *            MQTT delivery token
	 */
	public void deliveryComplete(IMqttDeliveryToken token) {
		final String METHOD = "deliveryComplete";
		LoggerUtility.fine(CLASS_NAME, METHOD, "token " + token.getMessageId());
		messageCount++;
	}
	
	/**
	 * The Device client does not currently support subscriptions.
	 */
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		final String METHOD = "messageArrived";
		if (commandCallback != null) {
			/* Only check whether the message is a command if a callback 
			 * has been defined, otherwise it is a waste of time
			 * as without a callback there is nothing to process the generated
			 * command.
			 */
			Matcher matcher = COMMAND_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String command = matcher.group(1);
				String format = matcher.group(2);
				Command cmd = new Command(command, format, msg);
				LoggerUtility.fine(CLASS_NAME, METHOD, "Event received: " + cmd.toString());
				commandCallback.processCommand(cmd);
		    }
		}
	}

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		final String METHOD = "connectComplete";
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " reconnected (" + reconnect + ") URI: " + serverURI);
		if (reconnect) {
			if (!getOrgId().equals("quickstart")) {
				try {
					subscribeToCommands();
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
