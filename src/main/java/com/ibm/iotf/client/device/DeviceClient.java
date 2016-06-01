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
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.AbstractClient;
import com.ibm.iotf.util.LoggerUtility;


/**
 * A client, used by device, that handles connections with the IBM Watson IoT Platform. <br>
 * 
 * This is a derived class from AbstractClient and can be used by embedded devices to handle connections with IBM Watson IoT Platform.
 */
public class DeviceClient extends AbstractClient {
	
	private static final String CLASS_NAME = DeviceClient.class.getName();
	
	private static final Pattern COMMAND_PATTERN = Pattern.compile("iot-2/cmd/(.+)/fmt/(.+)");
	
	private CommandCallback commandCallback = null;
	
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
	 * Connecting to a specific account on the IoTF.
	 * @throws Exception When there is a failure in parsing the properties passed 
	 */
	public DeviceClient(Properties options) throws Exception {
		super(options);
		LoggerUtility.fine(CLASS_NAME, "DeviceClient", "options   = " + options);
		this.clientId = "d" + CLIENT_ID_DELIMITER + getOrgId() + CLIENT_ID_DELIMITER + getDeviceType() + CLIENT_ID_DELIMITER + getDeviceId();
		
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
		createClient(this.new MqttDeviceCallBack());
	}
	
	/*
	 * old style - type
	 * new style - Device-Type
	 */
	public String getDeviceType() {
		String type = null;
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
		super.connect(true);
		if (!getOrgId().equals("quickstart")) {
			subscribeToCommands();
		}
	}
	
	private void subscribeToCommands() {
		try {
			mqttAsyncClient.subscribe("iot-2/cmd/+/fmt/" + getFormat(), 2);
		} catch (MqttException e) {
			e.printStackTrace();
		}
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
		return publishEvent(event, data, 0);
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
		if (!isConnected()) {
			return false;
		}
		final String METHOD = "publishEvent(2)";
		JsonObject payload = new JsonObject();
		
		String timestamp = ISO8601_DATE_FORMAT.format(new Date());
		payload.addProperty("ts", timestamp);
		
		// Handle null object
		if(data == null) {
			data = new JsonObject();
		}
		
		JsonElement dataElement = gson.toJsonTree(data);
		payload.add("d", dataElement);
		
		String topic = "iot-2/evt/" + event + "/fmt/json";
		
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic   = " + topic);
		LoggerUtility.fine(CLASS_NAME, METHOD, "Payload = " + payload.toString());
		
		MqttMessage msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		msg.setQos(qos);
		msg.setRetained(false);
		
		try {
			mqttAsyncClient.publish(topic, msg).waitForCompletion();
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
			return false;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	

	
	private class MqttDeviceCallBack implements MqttCallback {
	
		/**
		 * If we lose connection trigger the connect logic to attempt to
		 * reconnect to the IBM Watson IoT Platform.
		 * 
		 * @param exception
		 *            Throwable which caused the connection to get lost
		 */
		public void connectionLost(Throwable exception) {
			final String METHOD = "connectionLost";
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, exception.getMessage(), exception);
			try {
				reconnect();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	}
	
	public void setCommandCallback(CommandCallback callback) {
		this.commandCallback  = callback;
	}
	
	/**
	 * Publish an event to the IBM Watson IoT Platform using HTTP(S)<br>
	 * 
	 * @param eventName  Name of the dataset under which to publish the data
	 * @param payload Object to be added to the payload as the dataset
	 * @return httpcode the return code
	 * @throws Exception if the operation is not successful
	 */
	public int publishEventOverHTTP(String eventName, Object payload) throws Exception {
		String authKey = "use-token-auth";
		return publishEventsThroughHttps(this.getOrgId(), this.getDeviceType(), this.getDeviceId(), 
				eventName, true, authKey, this.getAuthToken(), payload);
	}
	
}
