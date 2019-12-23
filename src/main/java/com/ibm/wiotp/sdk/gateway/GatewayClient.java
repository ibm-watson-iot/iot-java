/**
 *****************************************************************************
 * Copyright (c) 2016-19 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 */

package com.ibm.wiotp.sdk.gateway;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.MessageInterface;
import com.ibm.wiotp.sdk.codecs.MessageCodec;
import com.ibm.wiotp.sdk.device.Command;
import com.ibm.wiotp.sdk.device.CommandCallback;
import com.ibm.wiotp.sdk.device.DeviceClient;
import com.ibm.wiotp.sdk.gateway.config.GatewayConfig;

/**
 * A client, used by Gateway, that simplifies the Gateway interactions with IBM
 * Watson IoT Platform. <br>
 * 
 * <p>
 * Gateways are a specialized class of devices in Watson IoT Platform which
 * serve as access points to the Watson IoT Platform for other devices. Gateway
 * devices have additional permission when compared to regular devices and can
 * perform the following functions:
 * </p>
 * 
 * <ul class="simple">
 * <li>Register new devices to Watson IoT Platform
 * <li>Send and receive its own sensor data like a directly connected device,
 * <li>Send and receive data on behalf of the devices connected to it
 * <li>Run a device management agent, so that it can be managed, also manage the
 * devices connected to it
 * </ul>
 * 
 * <p>
 * Refer to the <a href=
 * "https://docs.internetofthings.ibmcloud.com/gateways/mqtt.html">documentation</a>
 * for more information about the Gateway support in Watson IoT Platform.
 * </p>
 * 
 * This is a derived class from AbstractClient.
 */
public class GatewayClient extends DeviceClient implements MqttCallbackExtended {
	private static final Logger LOG = LoggerFactory.getLogger(GatewayClient.class);
	private static final Pattern GATEWAY_COMMAND_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/cmd/(.+)/fmt/(.+)");

	/**
	 * <p>
	 * Create a Gateway client for the IBM Watson IoT Platform using the properties
	 * file passed. The properties must have the following definitions,
	 * </p>
	 * 
	 * <ul class="simple">
	 * <li>org - Your organization ID.
	 * <li>type - The type of your Gateway device.
	 * <li>id - The ID of your Gateway.
	 * <li>auth-method - Method of authentication (The only value currently
	 * supported is "token").
	 * <li>auth-token - API key token.
	 * </ul>
	 * 
	 * @param config Configuration object for the gateway client with the Watson
	 *               Internet of Things Platform module.
	 * 
	 * @throws Exception Failure in parsing the properties passed
	 */
	public GatewayClient(GatewayConfig config) throws Exception {
		super(config);
	}

	public boolean publishDeviceEvent(String deviceType, String deviceId, String event, Object data) {
		return publishDeviceEvent(deviceType, deviceId, event, data, 0);
	}

	/**
	 * Publish an event on the behalf of a device to the IBM Watson IoT Platform.
	 * <br>
	 * 
	 * @param typeId   object of String which denotes deviceType
	 * @param deviceId object of String which denotes deviceId
	 * @param eventId  object of String which denotes event
	 * @param data     Payload data
	 * @param qos      Quality of Service, in int - can have values 0,1,2
	 * 
	 * @return Whether the send was successful.
	 */
	public boolean publishDeviceEvent(String typeId, String deviceId, String eventId, Object data, int qos) {
		Object payload = null;
		String topic = "iot-2/type/" + typeId + "/id/" + deviceId + "/evt/" + eventId + "/fmt/json";
		// Handle null object
		if (data == null) {
			data = new JsonObject();
		}
		payload = gson.toJsonTree(data);

		LOG.debug("Topic   = " + topic);
		LOG.debug("Payload = " + payload.toString());

		MqttMessage msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
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

	/**
	 * Subscribe to device commands, on the behalf of a device, to the IBM Watson
	 * IoT Platform. <br>
	 * 
	 * @param tpyeId    object of String which denotes deviceType
	 * @param deviceId  object of String which denotes deviceId
	 * @param commandId object of String which denotes command
	 * @param format    object of String which denotes format, typical example of
	 *                  format could be json
	 * @param qos       Quality of Service, in int - can have values 0,1,2
	 */
	public void subscribeToDeviceCommands(String tpyeId, String deviceId, String commandId, String format, int qos) {
		try {
			String newTopic = "iot-2/type/" + tpyeId + "/id/" + deviceId + "/cmd/" + commandId + "/fmt/" + format;
			mqttAsyncClient.subscribe(newTopic, qos).waitForCompletion(DEFAULT_ACTION_TIMEOUT);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Unsubscribe from device commands, on the behalf of a device, from the IBM
	 * Watson IoT Platform.
	 * 
	 * @param typeId    object of String which denotes deviceType
	 * @param deviceId  object of String which denotes deviceId
	 * @param commandId object of String which denotes command name
	 */
	public void unsubscribeFromDeviceCommands(String typeId, String deviceId, String commandId) {
		try {
			String newTopic = "iot-2/type/" + typeId + "/id/" + deviceId + "/cmd/" + commandId + "/fmt/json";
			mqttAsyncClient.unsubscribe(newTopic).waitForCompletion(DEFAULT_ACTION_TIMEOUT);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * This method is called by the MQTT library when a message(command) is sent by
	 * the IBM Watson IoT Platform.
	 * </p>
	 * The message(command) will be processed by this class and corresponding
	 * callback method will be called if registered.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void messageArrived(String topic, MqttMessage msg) {
		if (!commandCallbacks.isEmpty()) {
			/*
			 * Only check whether the message is a command if a callback has been defined,
			 * otherwise it is a waste of time as without a callback there is nothing to
			 * process the generated command.
			 */
			Matcher matcher = GATEWAY_COMMAND_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String command = matcher.group(1);
				String format = matcher.group(2);

				MessageCodec codec = messageCodecsByFormat.get(format);
				// Check that a codec is registered
				if (codec == null) {
					LOG.warn("Unable to decode command from format " + format);
				}
				MessageInterface message = codec.decode(msg);
				Command cmd = new Command(command, format, message);

				LOG.debug("Command received: " + cmd.toString());

				CommandCallback callback = commandCallbacks.get(codec.getMessageClass());
				if (callback != null) {
					callback.processCommand(cmd);
				}
			}
		}
	}

}
