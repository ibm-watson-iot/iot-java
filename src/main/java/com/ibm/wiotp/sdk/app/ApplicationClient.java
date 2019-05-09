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
package com.ibm.wiotp.sdk.app;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.ibm.wiotp.sdk.AbstractClient;
import com.ibm.wiotp.sdk.MessageInterface;
import com.ibm.wiotp.sdk.app.callbacks.CommandCallback;
import com.ibm.wiotp.sdk.app.callbacks.EventCallback;
import com.ibm.wiotp.sdk.app.callbacks.StatusCallback;
import com.ibm.wiotp.sdk.app.config.ApplicationConfig;
import com.ibm.wiotp.sdk.app.messages.ApplicationStatus;
import com.ibm.wiotp.sdk.app.messages.Command;
import com.ibm.wiotp.sdk.app.messages.DeviceStatus;
import com.ibm.wiotp.sdk.app.messages.Event;
import com.ibm.wiotp.sdk.codecs.MessageCodec;
import com.ibm.wiotp.sdk.util.LoggerUtility;

/**
 * A client, used by application, that handles connections with the IBM Watson IoT Platform. <br>
 * 
 * This is a derived class from AbstractClient and can be used by end-applications to handle connections with IBM Watson IoT Platform.
 */
public class ApplicationClient extends AbstractClient implements MqttCallbackExtended {
	
	private static final String CLASS_NAME = ApplicationClient.class.getName();
	
	private static final Pattern DEVICE_EVENT_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/evt/(.+)/fmt/(.+)");
	private static final Pattern DEVICE_STATUS_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/mon");
	private static final Pattern APP_STATUS_PATTERN = Pattern.compile("iot-2/app/(.+)/mon");
	private static final Pattern DEVICE_COMMAND_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/cmd/(.+)/fmt/(.+)");
	
	private StatusCallback statusCallback = null;
	
	private HashMap<String, Integer> subscriptions = new HashMap<String, Integer>();
	
	@SuppressWarnings("rawtypes")
	private Map<Class, MessageCodec> messageCodecs = new HashMap<Class, MessageCodec>();
	@SuppressWarnings("rawtypes")
	private Map<String, MessageCodec> messageCodecsByFormat = new HashMap<String, MessageCodec>();
	
	@SuppressWarnings("rawtypes")
	private Map<Class, CommandCallback> commandCallbacks = new HashMap<Class, CommandCallback>();
	@SuppressWarnings("rawtypes")
	private Map<Class, EventCallback> eventCallbacks = new HashMap<Class, EventCallback>();
	
	public ApplicationClient() throws Exception {
		this(ApplicationConfig.generateFromEnv());
	}
		
	/**
	 * Create an application client for the IBM Watson IoT Platform. 
	 * Connecting to specific org on IBM Watson IoT Platform
	 * 
	 * @param config Configuration object for the client
	 * 
	 * @throws Exception Failure in parsing the properties 
	 */
	public ApplicationClient(ApplicationConfig config) throws Exception {
		super(config);
		configureMqttClient(this);
	}
	
	
	/**
	 * Publish event, on the behalf of a device, to the IBM Watson IoT Platform. <br>
	 * This method will attempt to create a JSON obejct out of the payload
	 * 
	 * @param typeId
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param eventId
	 *            object of String which denotes event
	 * @param data
	 *            Payload data
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 *            
	 * @return Whether the send was successful.
	 */
	@SuppressWarnings("unchecked")
	public boolean publishEvent(String typeId, String deviceId, String eventId, Object data, int qos) {
		final String METHOD = "publishEvent";

		if (data == null) {
			throw new NullPointerException("Data object for event publish can not be null");
		}
		
		// Find the codec for the data class
		@SuppressWarnings("rawtypes")
		MessageCodec codec = messageCodecs.get(data.getClass());
		
		// Check that a codec is registered
		if (codec == null) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Unable to encode event data of class " + data.getClass().getName());
			return false;
		}
		
		byte[] payload = codec.encode(data, null);
		String topic = "iot-2/type/" + typeId + "/id/" + deviceId + "/evt/" + eventId + "/fmt/" + codec.getMessageFormat();

		LoggerUtility.info(CLASS_NAME, METHOD, "Publishing event to " + topic);
		
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
	
	public boolean publishEvent(String typeId, String deviceId, String eventId, Object data) {
		return publishEvent(typeId, deviceId, eventId, data, 0);
	}


	/**
	 * Publish command to the IBM Watson IoT Platform. <br>
	 * This method will attempt to create a JSON obejct out of the payload
	 * 
	 * @param typeId
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param commandId
	 *            object of String which denotes command
	 * @param data
	 *            Payload data
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 *            
	 * @return Whether the send was successful.
	 */
	@SuppressWarnings("unchecked")
	public boolean publishCommand(String typeId, String deviceId, String commandId, Object data, int qos) {
		final String METHOD = "publishCommand";

		if (data == null) {
			throw new NullPointerException("Data object for event publish can not be null");
		}
		
		// Find the codec for the data class
		@SuppressWarnings("rawtypes")
		MessageCodec codec = messageCodecs.get(data.getClass());
		
		// Check that a codec is registered
		if (codec == null) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Unable to encode command data of class " + data.getClass().getName());
			return false;
		}
		
		byte[] payload = codec.encode(data, null);

		String topic = "iot-2/type/" + typeId + "/id/" + deviceId + "/cmd/" + commandId + "/fmt/" + codec.getMessageFormat();
		LoggerUtility.info(CLASS_NAME, METHOD, "Publishing command to " + topic);

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
	
	public boolean publishCommand(String typeId, String deviceId, String commandId, Object data) {
		return publishCommand(typeId, deviceId, commandId, data, 1);
	}
	
	/**
	 * Subscribe to device events of the IBM Watson IoT Platform. <br>
	 * 
	 * @param typeId
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param eventId
	 *            object of String which denotes event
	 * @param format
	 *            object of String which denotes format
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 */
	public void subscribeToDeviceEvents(String typeId, String deviceId, String eventId, String format, int qos) {
		String newTopic = "iot-2/type/" + typeId + "/id/" + deviceId + "/evt/" + eventId + "/fmt/" + format;
		LoggerUtility.info(CLASS_NAME, "subscribeToDeviceEvents", "Subscribing to " + newTopic);

		try {
			subscriptions.put(newTopic, new Integer(qos));
			mqttAsyncClient.subscribe(newTopic, qos).waitForCompletion(DEFAULT_ACTION_TIMEOUT);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public void subscribeToDeviceEvents(String typeId, String deviceId, String eventId, String format) {
		subscribeToDeviceEvents(typeId, deviceId, eventId, format, 0);
	}
	public void subscribeToDeviceEvents(String typeId, String deviceId, String eventId) {
		subscribeToDeviceEvents(typeId, deviceId, eventId, "+");
	}
	public void subscribeToDeviceEvents(String typeId, String deviceId) {
		subscribeToDeviceEvents(typeId, deviceId, "+");
	}
	public void subscribeToDeviceEvents(String typeId) {
		subscribeToDeviceEvents(typeId, "+");
	}
	public void subscribeToDeviceEvents() {
		subscribeToDeviceEvents("+");
	}
	
	
	/**
	 * Unsubscribe from device events of the IBM Watson IoT Platform. <br>
	 * 
	 * @param typeId
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param eventId
	 *            object of String which denotes event
	 */
	public void unsubscribeFromDeviceEvents(String typeId, String deviceId, String eventId) {
		try {
			String topic = "iot-2/type/"+typeId+"/id/"+deviceId+"/evt/"+eventId+"/fmt/json";
			subscriptions.remove(topic);
			mqttAsyncClient.unsubscribe(topic).waitForCompletion(DEFAULT_ACTION_TIMEOUT);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void unsubscribeFromDeviceEvents(String typeId, String deviceId) {
		unsubscribeFromDeviceEvents(typeId, deviceId, "+");
	}
	public void unsubscribeFromDeviceEvents(String typeId) {
		unsubscribeFromDeviceEvents(typeId, "+");
	}
	public void unsubscribeFromDeviceEvents() {
		unsubscribeFromDeviceEvents("+");
	}

	/**
	 * Subscribe to device commands, on the behalf of a device, of the IBM Watson IoT Platform. <br>
	 * 
	 * @param typeId
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param commandId
	 *            object of String which denotes command
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 */
	public void subscribeToDeviceCommands(String typeId, String deviceId, String commandId, int qos) {
		String newTopic = "iot-2/type/" + typeId + "/id/" + deviceId + "/cmd/" + commandId + "/fmt/json";
		LoggerUtility.info(CLASS_NAME, "subscribeToDeviceCommands", "Subscribing to " + newTopic);
		try {
			subscriptions.put(newTopic, new Integer(qos));
			mqttAsyncClient.subscribe(newTopic, qos).waitForCompletion(DEFAULT_ACTION_TIMEOUT);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public void subscribeToDeviceCommands(String typeId, String deviceId, String commandId) {
		subscribeToDeviceCommands(typeId, deviceId, commandId, 1);
	}
	public void subscribeToDeviceCommands(String typeId, String deviceId) {
		subscribeToDeviceCommands(typeId, deviceId, "+");
	}
	public void subscribeToDeviceCommands(String typeId) {
		subscribeToDeviceCommands(typeId, "+");
	}


	/**
	 * Unsubscribe from device commands of the IBM Watson IoT Platform. <br>
	 * 
	 * @param typeId
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param commandId
	 *            object of String which denotes command
	 */
	public void unsubscribeFromDeviceCommands(String typeId, String deviceId, String commandId) {
		try {
			String topic = "iot-2/type/"+typeId+"/id/"+deviceId+"/cmd/"+commandId+"/fmt/json";
			subscriptions.remove(topic);
			mqttAsyncClient.unsubscribe(topic).waitForCompletion(DEFAULT_ACTION_TIMEOUT);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void unsubscribeFromDeviceCommands(String typeId, String commandId) {
		unsubscribeFromDeviceCommands(typeId, commandId, "+");
	}
	public void unsubscribeFromDeviceCommands(String typeId) {
		unsubscribeFromDeviceCommands(typeId, "+");
	}
	public void unsubscribeFromDeviceCommands() {
		unsubscribeFromDeviceCommands("+");
	}

	/**
	 * Subscribe to device status of the IBM Watson IoT Platform. <br>
	 * 
	 * @param typeId
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 */
	public void subscribeToDeviceStatus(String typeId, String deviceId) {
		try {
			String newTopic = "iot-2/type/"+typeId+"/id/"+deviceId+"/mon";
			subscriptions.put(newTopic, new Integer(0));			
			mqttAsyncClient.subscribe(newTopic, 0).waitForCompletion(DEFAULT_ACTION_TIMEOUT);;
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Subscribe to application status of the IBM Watson IoT Platform. <br>
	 * 
	 * @param appId 
	 * 		object of String which denotes the application uniquely in the organization
	 */
	public void subscribeToApplicationStatus(String appId) {
		try {
			String newTopic = "iot-2/app/"+appId+"/mon";
			subscriptions.put(newTopic, new Integer(0));			
			mqttAsyncClient.subscribe(newTopic, 0).waitForCompletion(DEFAULT_ACTION_TIMEOUT);
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
		try {
			String topic = "iot-2/app/"+appId+"/mon";
			mqttAsyncClient.unsubscribe(topic).waitForCompletion(DEFAULT_ACTION_TIMEOUT);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Simply log error when connection is lost
	 */
	public void connectionLost(Throwable e) {
		final String METHOD = "connectionLost";
		LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Lost connection client (" + config.getClientId() + ") : " + e.getMessage(), e);
		if (e instanceof MqttException) {
			MqttException e2 = (MqttException) e;
			LoggerUtility.info(CLASS_NAME, METHOD, "Connection lost: Reason Code: " 
					+ e2.getReasonCode() + " Cause: " + ExceptionUtils.getRootCauseMessage(e2));
		} else {
			LoggerUtility.info(CLASS_NAME, METHOD, "Connection lost: " + e.getMessage());
		}
		
	}
	
	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		final String METHOD = "connectComplete";
		if (reconnect) {
			LoggerUtility.info(CLASS_NAME, METHOD, "Reconnected to " + serverURI );
			if (config.isCleanSession() == true) {
			    Iterator<Entry<String, Integer>> iterator = subscriptions.entrySet().iterator();
			    while (iterator.hasNext()) {
			        Entry<String, Integer> pairs = iterator.next();
			        String topic = pairs.getKey();
			        Integer qos = pairs.getValue();
			        LoggerUtility.info(CLASS_NAME, METHOD, "Resubscribing topic(" +topic + ") QoS:" + qos);
			        try {
			        	mqttAsyncClient.subscribe(topic, qos.intValue());
					} catch (NumberFormatException | MqttException e1) {
						e1.printStackTrace();
					}
			    }
			}
			
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void messageArrived(String topic, MqttMessage msg) {
		final String METHOD = "messageArrived";
		if (! eventCallbacks.isEmpty()) {
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
				
				MessageCodec codec = messageCodecsByFormat.get(format);
				if (codec == null) {
					LoggerUtility.severe(CLASS_NAME, METHOD, "Unable to decode event of format " + format);
					// We don't throw an exception, as doing so will cause the underlying MQTT Paho client to disconnect.
					return;
				}
				MessageInterface message = codec.decode(msg);
				Event evt = new Event(type, id, event, format, message);

				LoggerUtility.fine(CLASS_NAME, METHOD, "Event received: " + evt.toString());
				
				EventCallback callback = eventCallbacks.get(codec.getMessageClass());
				if (callback != null) {
					callback.processEvent(evt);
				}
				return;
		    }
		}
		
		if (! commandCallbacks.isEmpty()) {
			Matcher matcher = DEVICE_COMMAND_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String type = matcher.group(1);
				String id = matcher.group(2);
				String command = matcher.group(3);
				String format = matcher.group(4);
				
				MessageCodec codec = messageCodecsByFormat.get(format);
				if (codec == null) {
					LoggerUtility.severe(CLASS_NAME, METHOD, "Unable to decode command of format " + format);
					// We don't throw an exception, as doing so will cause the underlying MQTT Paho client to disconnect.
					return;
				}
				MessageInterface message = codec.decode(msg);
				Command cmd = new Command(type, id, command, format, message);

				LoggerUtility.fine(CLASS_NAME, METHOD, "Command received: " + cmd.toString());
				
				CommandCallback callback = commandCallbacks.get(codec.getMessageClass());
				if (callback != null) {
					callback.processCommand(cmd);
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
				try {
					DeviceStatus status = new DeviceStatus(type, id, msg);
					LoggerUtility.fine(CLASS_NAME, METHOD, "Device status received: " + status.toString());
					statusCallback.processDeviceStatus(status);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
			
			matcher = APP_STATUS_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String id = matcher.group(1);
				try {
					ApplicationStatus status = new ApplicationStatus(id, msg);
					LoggerUtility.fine(CLASS_NAME, METHOD, "Application status received: " + status.toString());
					statusCallback.processApplicationStatus(status);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
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

	@SuppressWarnings("rawtypes")
	public void registerEventCallback(EventCallback callback) {
		this.eventCallbacks.put(callback.getMessageClass(), callback);
	}


	public void setStatusCallback(StatusCallback callback) {
		this.statusCallback  = callback;
	}
	
}
