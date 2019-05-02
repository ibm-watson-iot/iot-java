/**
 *****************************************************************************
 Copyright (c) 2015-19 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 *
 */
package com.ibm.wiotp.sdk.device;

import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.AbstractClient;
import com.ibm.wiotp.sdk.device.config.DeviceConfig;
import com.ibm.wiotp.sdk.util.LoggerUtility;


/**
 * A client, used by device, that handles connections with the IBM Watson IoT Platform. <br>
 * 
 * This is a derived class from AbstractClient and can be used by embedded devices to handle connections with IBM Watson IoT Platform.
 */
public class DeviceClient extends AbstractClient implements MqttCallbackExtended {
	
	private static final String CLASS_NAME = DeviceClient.class.getName();
	private static final Pattern COMMAND_PATTERN = Pattern.compile("iot-2/cmd/(.+)/fmt/(.+)");
	
	protected CommandCallback commandCallback = null;
	
	public DeviceClient() throws Exception {
		this(DeviceConfig.generateFromEnv());
	}
	
	/**
	 * Create a device client for the IBM Watson IoT Platform. <br>
	 * 
	 * @param config Configuration object for the client
	 * @throws Exception When there is a failure in parsing the properties passed 
	 */
	public DeviceClient(DeviceConfig config) throws Exception {
		super(config);
		configureMqttClient(this);
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
	 * @throws MqttException One or more credentials are wrong
	 * @throws NoSuchAlgorithmException TLS issues
	 * @throws KeyManagementException TLS issues
	 **/
	public void connect() throws MqttException, KeyManagementException, NoSuchAlgorithmException {
		super.connect();
		if (!config.getOrgId().equals("quickstart")) {
			subscribeToCommands();
		}
	}
	
	
	/*
	 * This method reconnects when the connection is lost due to n/w interruption
	 */
	protected void reconnect() throws MqttException, KeyManagementException, NoSuchAlgorithmException {
		super.connect();
		if (!config.getOrgId().equals("quickstart") && config.isCleanSession()) {
			subscribeToCommands();
		}
	}
	
	private void subscribeToCommands() throws MqttException{
		try {
			mqttAsyncClient.subscribe("iot-2/cmd/+/fmt/+", 2).waitForCompletion(DEFAULT_ACTION_TIMEOUT);
		} catch (MqttException e) {
			e.printStackTrace();
			throw e;
		}
	}


	/**
	 * Publish data to the IBM Watson IoT Platform.<br>
	 * 
	 * @param eventId
	 *            object of String which denotes event
	 * @param data
	 *            Payload data
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @return Whether the send was successful.
	 * @throws Exception when the publish operation fails
	 */	
	public boolean publishEvent(String eventId, Object data, int qos) throws Exception {
		final String METHOD = "publishEvent";
		String topic = "iot-2/evt/" + eventId + "/fmt/json";
		Object payload = null;
		MqttMessage msg = null;
		// Handle null object
		if(data == null) {
			data = new JsonObject();
		}
		payload = gson.toJsonTree(data);
		msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic   = " + topic);
		LoggerUtility.fine(CLASS_NAME, METHOD, "Payload = " + payload.toString());
		
		msg.setQos(qos);
		msg.setRetained(false);
		
		try {
			mqttAsyncClient.publish(topic, msg);
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
	 * Simply log error when connection is lost
	 */
	public void connectionLost(Throwable e) {
		final String METHOD = "connectionLost";
		LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Lost connection client (" + config.getClientId() + ") : " + e.getMessage());
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
		LoggerUtility.info(CLASS_NAME, METHOD, config.getClientId() + " reconnected (" + reconnect + ") URI: " + serverURI);
		if (reconnect) {
			if (!config.getOrgId().equals("quickstart")) {
				try {
					subscribeToCommands();
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
