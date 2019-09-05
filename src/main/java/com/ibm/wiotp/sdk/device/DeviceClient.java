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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.wiotp.sdk.AbstractClient;
import com.ibm.wiotp.sdk.MessageInterface;
import com.ibm.wiotp.sdk.codecs.MessageCodec;
import com.ibm.wiotp.sdk.device.config.DeviceConfig;


/**
 * A client, used by device, that handles connections with the IBM Watson IoT Platform. <br>
 * 
 */
public class DeviceClient extends AbstractClient implements MqttCallbackExtended {
	private static final Logger LOG = LoggerFactory.getLogger(DeviceClient.class);
	private static final Pattern COMMAND_PATTERN = Pattern.compile("iot-2/cmd/(.+)/fmt/(.+)");
	
	@SuppressWarnings("rawtypes")
	protected Map<Class, MessageCodec> messageCodecs = new HashMap<Class, MessageCodec>();
	@SuppressWarnings("rawtypes")
	protected Map<String, MessageCodec> messageCodecsByFormat = new HashMap<String, MessageCodec>();
	
	@SuppressWarnings("rawtypes")
	protected Map<Class, CommandCallback> commandCallbacks = new HashMap<Class, CommandCallback>();

	
	public DeviceClient() throws Exception {
		this(DeviceConfig.generateFromEnv());
	}

	public DeviceClient(String fileName) throws Exception {
		this(DeviceConfig.generateFromConfig(fileName));
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
	 */	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean publishEvent(String eventId, Object data, int qos) {
		if (data == null) {
			throw new NullPointerException("Data object for event publish can not be null");
		}
		
		// Find the codec for the data class
		MessageCodec codec = messageCodecs.get(data.getClass());
		
		// Check that a codec is registered
		if (codec == null) {
			LOG.warn("Unable to encode event of class " + data.getClass().getName());
			return false;
		}
		byte[] payload = codec.encode(data, new DateTime());
		String topic = "iot-2/evt/" + eventId + "/fmt/" + codec.getMessageFormat();
		LOG.debug("Publishing event to " + topic);
		
		MqttMessage msg = new MqttMessage(payload);
		msg.setQos(qos);
		msg.setRetained(false);
		
		try {
			mqttAsyncClient.publish(topic, msg);
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean publishEvent(String eventId, Object data) {
		return publishEvent(eventId, data, 0);
	}
	

	/**
	 * Simply log error when connection is lost
	 */
	public void connectionLost(Throwable e) {
		if (e instanceof MqttException) {
			MqttException e2 = (MqttException) e;
			LOG.warn("Connection lost: Reason Code: " + e2.getReasonCode() + " Cause: " + ExceptionUtils.getRootCauseMessage(e2));
		} else {
			LOG.warn("Connection lost: " + e.getMessage());
		}
		
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void messageArrived(String topic, MqttMessage msg) {
		if (! commandCallbacks.isEmpty()) {
			/* Only check whether the message is a command if a callback 
			 * has been defined, otherwise it is a waste of time
			 * as without a callback there is nothing to process the generated
			 * command.
			 */
			Matcher matcher = COMMAND_PATTERN.matcher(topic);
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

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		if (reconnect) {
			LOG.info("Reconnected to " + serverURI);
			if (!config.getOrgId().equals("quickstart")) {
				try {
					subscribeToCommands();
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void registerCodec(MessageCodec codec) {
		this.messageCodecs.put(codec.getMessageClass(), codec);
		this.messageCodecsByFormat.put(codec.getMessageFormat(), codec);
	}
	
	@SuppressWarnings("rawtypes")
	public void registerCommandCallback(CommandCallback callback) {
		this.commandCallbacks.put(callback.getMessageClass(), callback);
	}
	
}
