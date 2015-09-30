/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Extended from DeviceClient
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt.device;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.devicemgmt.device.DeviceData;
import com.ibm.iotf.devicemgmt.device.handler.DMRequestHandler;
import com.ibm.iotf.devicemgmt.device.internal.DeviceTopic;
import com.ibm.iotf.devicemgmt.device.internal.ResponseCode;
import com.ibm.iotf.devicemgmt.device.internal.ServerTopic;
import com.ibm.iotf.devicemgmt.device.listener.DMListener;
import com.ibm.iotf.util.LoggerUtility;

/**
 * A managed device class, used by device, that connects the device as managed device to IBM IoT Foundation and
 * enables devices to perform one or more Device Management operations,
 * 
 * <p>The device management feature enhances the Internet of Things Foundation service with new capabilities 
 * for managing devices.</p>
 * 
 * <p> What does Device Management add? </p>
 * <ul class="simple">
 * <li>Control and management of device lifecycles for both individual and batches of devices.</li>
 * <li>Device metadata and status information, enabling the creation of device dashboards and other tools.</li>
 * <li>Diagnostic information, both for connectivity to the Internet of Things Foundation service, and device diagnostics.</li>
 * <li>Device management commands, like firmware update, and device reboot.</li>
 * </ul> 
 * <p> This is a derived class from DeviceClient and can be used by embedded devices to perform both <b>Device operations 
 * and Device Management operations</b>, i.e, the devices can use this class to do the following, <p>
 * 
 * <ul class="simple">
 * <li>Publish device events</li>
 * <li>Subscribe to commands from application</li>
 * <li>Perform Device management operations like, manage, unmanage, firmware update, reboot, 
 *    update location, Diagnostics informations, Factory Reset and etc..</li> 
 * 
 */

public class ManagedDevice extends DeviceClient implements IMqttMessageListener, Runnable {
	
	private static final String CLASS_NAME = ManagedDevice.class.getName();
	private static final int REGISTER_TIMEOUT_VALUE = 60 * 1000 * 2; // wait for 2 minute
	
	private final SynchronousQueue<JsonObject> queue = new SynchronousQueue<JsonObject>();
	
	private boolean running = false;
	private BlockingQueue<JsonObject> publishQueue;
	JsonObject dummy = new JsonObject();
	
	//Map to handle duplicate responses
	private Map<String, MqttMessage> requests = new HashMap<String, MqttMessage>();
	
	//Device specific information
	private DeviceData deviceData = null;
	
	private boolean supportsDeviceActions = false;
	private boolean supportsFirmwareActions = false;
	private boolean bManaged = false;
	private Date dormantTime;
	private ServerTopic responseSubscription = null;
	
    /**
     * Constructor that creates a ManagedDevice object, but does not connect to 
     * IBM IoT Foundation connect yet
     * 
     * @param options      List of options to connect to IBM IoT Foundation Connect
     * @param deviceData   The Device Model
     * @throws Exception   If the essential parameters are not set
     */
	public ManagedDevice(Properties options, DeviceData deviceData) throws Exception {
		super(options);
		final String METHOD = "constructor";
		if(deviceData == null) {
			LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Could not create Managed Client "
					+ "without DeviceInformations !");
			throw new Exception("Could not create Managed Client without DeviceInformations !");
		}
		String typeId = this.getDeviceType();
		String deviceId = this.getDeviceId();
		
		if(typeId == null || deviceId == null) {
			LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Could not create Managed Client "
					+ "without Device Type or Device ID !");
			throw new Exception("Could not create Managed Client without Device Type or Device ID!, "
					+ "Please specify the same in properties");
		}
		deviceData.setTypeId(typeId);
		deviceData.setDeviceId(deviceId);
		this.deviceData = deviceData;
	}
	
	/**
	 * Constructs a ManagedDevice Object
	 * 	
	 * @param client      MqttClient which encapsulates the connection to IBM IoT Foundation connect 
	 * @param deviceData  The Device Model
     * @throws Exception   If the essential parameters are not set
	 */
	public ManagedDevice(MqttClient client, DeviceData deviceData) throws Exception {
		super(client);
		setDeviceData(deviceData);
	}
	
	/**
	 * Constructs a ManagedDevice Object
	 * 	
	 * @param client      MqttAsyncClient which encapsulates the connection to IBM IoT Foundation connect 
	 * @param deviceData  The Device Model
     * @throws Exception   If the essential parameters are not set
	 */
	public ManagedDevice(MqttAsyncClient client, DeviceData deviceData) throws Exception {
		super(client);
		setDeviceData(deviceData);
	}
	
	private void setDeviceData(DeviceData deviceData) throws Exception {
		final String METHOD = "setDeviceData";
		if(deviceData == null) {
			LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Could not create Managed Client "
					+ "without DeviceInformations !");
			throw new Exception("Could not create Managed Client without DeviceInformations !");
		}
		String typeId = deviceData.getTypeId();
		String deviceId = deviceData.getDeviceId();
		
		if(typeId == null || deviceId == null) {
			LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Could not create Managed Client "
					+ "without Device Type or Device ID!");
			throw new Exception("Could not create Managed Client without Device Type or Device ID!, "
					+ "Please specify the same in DeviceData");
		}
		this.deviceData = deviceData;
	}
	
	public DeviceData getDeviceData() {
		return deviceData;
	}
	
	/**
	 * Sets whether the device can participate in Reboot & Factory reset activities
	 * 
	 * @param supportsDeviceActions boolean value indicating whether the device
	 * supports device action or not
	 */
	public void supportsDeviceActions(boolean supportsDeviceActions) {
		this.supportsDeviceActions = supportsDeviceActions;
	}
	
	/**
	 * Sets whether the device can participate in Firmware activities
	 * 
	 * @param supportsFirmwareActions boolean value indicating whether the device
	 * supports Firmware action or not
	 */

	public void supportsFirmwareActions(boolean supportsFirmwareActions) {
		this.supportsFirmwareActions = supportsFirmwareActions;
	}

	
	private boolean registerDevice(long lifetime) {
		final String METHOD = "registerDevice";
		boolean success = false;
		String organization = getOrgId();
		if (organization == null || ("quickstart").equals(organization)) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Unable to create ManagedClient instance.  "
					+ "QuickStart devices do not support device management");
			
			throw new RuntimeException("Unable to create ManagedClient instance.  "
					+ "QuickStart devices do not support device management");
		}
		try {
			success = this.manage(lifetime);
			if(success) {
				LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Device is connected as managed device");
			} else {
				LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, "Device is failed to connect as managed device");
			}
		} catch (MqttException ex) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Connecting the device as managed device "
					+ "operation is Failed, Exception: "+ex.getMessage());
			
			RuntimeException e = new RuntimeException("Connecting the device as managed device "
					+ "operation is Failed, Exception: "+ex.getMessage());
			e.initCause(ex);
			throw e;
		}
		return success;
	}
	
	/**
	 * <p>This method just connects to the IBM Internet of Things Foundation,
	 * Device needs to make a call to manage() to participate in Device
	 * Management activities.<p> 
	 * 
	 * This method does nothing if the device is already connected
	 * 
	 * 
	 */	
	public void connect() {
		final String METHOD = "connect";
		if (this.isConnected()) {
			LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, "Device is already connected");
			return;
		}
		super.connect();
	}
	
	
	/**
	 * <p>Send a device manage request to IoT Foundation</p>
	 * 
	 * <p>A device uses this request to become a managed device. 
	 * It should be the first device management request sent by the 
	 * device after connecting to the Internet of Things Foundation. 
	 * It would be usual for a device management agent to send this 
	 * whenever is starts or restarts.</p>
	 * 
	 * <p>This method connects the device to IoT Foundation connect if its not connected already</p>
	 * 
	 * <p>This method sends a manage request with lifetime 0 and hence
	 * the device will never become dormant. </p>
	 * 
	 * <p>Use overloaded method manage(long) to specify lifetime</p>
	 * 
	 * @return
	 * @throws MqttException
	 */
	public boolean manage() throws MqttException {
		return this.manage(0);
	}
	
	/**
	 * <p>Send a device manage request to IoT Foundation</p>
	 * 
	 * <p>A device uses this request to become a managed device. 
	 * It should be the first device management request sent by the 
	 * device after connecting to the Internet of Things Foundation. 
	 * It would be usual for a device management agent to send this 
	 * whenever is starts or restarts.</p>
	 * 
	 * <p>This method connects the device to IoT Foundation connect if 
	 * its not connected already</p>
	 * 
	 * @param lifetime The length of time in seconds within 
	 *        which the device must send another Manage device request.
	 *        if set to 0, the managed device will not become dormant. 
	 *        When set, the minimum supported setting is 3600 (1 hour).
	 *        
	 * @return True if successful
	 * @throws MqttException
	 */
	public boolean manage(long lifetime) throws MqttException {
		
		final String METHOD = "manage";
		
		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "lifetime value (" + lifetime + ")");
		
		boolean success = false;
		DeviceTopic topic = DeviceTopic.MANAGE;
		
		if (!this.isConnected()) {
			this.connect();
		}
		
		JsonObject jsonPayload = new JsonObject();
		if (deviceData.getDeviceInfo() != null  || deviceData.getMetadata() != null) {
			
			JsonObject supports = new JsonObject();
			supports.add("deviceActions", new JsonPrimitive(this.supportsDeviceActions));
			supports.add("firmwareActions", new JsonPrimitive(this.supportsFirmwareActions));
			
			JsonObject data = new JsonObject();
			data.add("supports", supports);
			if (deviceData.getDeviceInfo() != null) {
				data.add("deviceInfo", deviceData.getDeviceInfo().toJsonObject());
			}
			if (deviceData.getMetadata() != null) {
				data.add("metadata", deviceData.getMetadata().getMetadata());
			}
			data.add("lifetime", new JsonPrimitive(lifetime));
			jsonPayload.add("d", data);
		} else {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Cannot send manage request "
					+ "as either deviceInfo or metadata is not set !!");
			
			return false;
		}

		JsonObject jsonResponse = sendAndWait(topic, jsonPayload, REGISTER_TIMEOUT_VALUE);
		if (jsonResponse != null && jsonResponse.get("rc").getAsInt() == 
				ResponseCode.DM_SUCCESS.getCode()) {
			DMListener.start(this);
			DMRequestHandler.setRequestHandlers(this);
			publishQueue = new LinkedBlockingQueue<JsonObject>();
			Thread t = new Thread(this);
			t.start();
			/*
			 * set the dormant time to a local variable, in case if the connection is
			 * lost due to n/w interruption, we need to send another manage request
			 * with the dormant time as the lifetime
			 */
			if(lifetime > 0) {
				Date currentTime = new Date();
				dormantTime = new Date(currentTime.getTime() + (lifetime * 1000));
			}
			success = true;			
		}
		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Success (" + success + ")");
		
		bManaged = success;
		return success;
	}
	
	/**
	 * Moves the device from managed state to unmanaged state
	 * 
	 * A device uses this request when it no longer needs to be managed. 
	 * This means IoTF will no longer send new device management requests 
	 * to this device and device management requests from this device will 
	 * be rejected apart from a Manage device request
	 * 
	 * @return
	 * 		True if the unmanage command is successful
	 * @throws MqttException
	 */
	public boolean unmanage() throws MqttException {
		
		final String METHOD = "unmanage";
		boolean success = false;
		DeviceTopic topic = DeviceTopic.UNMANAGE;

		JsonObject jsonPayload = new JsonObject();
		JsonObject jsonResponse = sendAndWait(topic, jsonPayload, REGISTER_TIMEOUT_VALUE);
		if (jsonResponse != null && jsonResponse.get("rc").getAsInt() == 
				ResponseCode.DM_SUCCESS.getCode()) {
			success = true;	
		}

		terminate();
		DMListener.stop(this);
		DMRequestHandler.clearRequestHandlers(this);
		this.deviceData.terminateHandlers();
		this.supportsDeviceActions = false;
		this.supportsFirmwareActions = false;
		
		if (responseSubscription != null) {
			this.unsubscribe(this.responseSubscription);
			responseSubscription = null;
		}

		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Success (" + success + ")");
		if(success) {
			bManaged = false;
		}
		return success;
	}
	
	/**
	 * <p>Subscribe the given listener to the given topic</p>
	 * 
	 * <p> This method is used by the library to subscribe to each of the topic 
	 * where IBM IoT Foundation will send the DM requests</p>
	 *  
	 * @param topic topic to be subscribed
	 * @param qos Quality of Service for the subscription
	 * @param listener The IMqttMessageListener for the given topic
	 * @throws MqttException
	 */
	public void subscribe(ServerTopic topic, int qos, IMqttMessageListener listener) throws MqttException {
		final String METHOD = "subscribe";
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic(" + topic + ")");
		if (isConnected()) {
			if (mqttAsyncClient != null) {
				mqttAsyncClient.subscribe(topic.getName(), qos, listener);
			} else if(mqttClient != null) {
				mqttClient.subscribe(topic.getName(), qos, listener);
			}
		} else {
			LoggerUtility.warn(CLASS_NAME, METHOD, "Will not subscribe to topic(" + topic +
					") because MQTT client is not connected.");
		}
	}
	
	/**
	 * <p>Subscribe the given listeners to the given topics</p>
	 * 
	 * <p> This method is used by the library to subscribe to each of the topic 
	 * where IBM IoT Foundation will send the DM requests</p>
	 *  
	 * @param topics List of topics to be subscribed
	 * @param qos Quality of Service for the subscription
	 * @param listeners The list of IMqttMessageListeners for the given topics
	 * @throws MqttException
	 */
	public void subscribe(String[] topics, int[] qos, IMqttMessageListener[] listeners) throws MqttException {
		final String METHOD = "subscribe#2";
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topics(" + topics + ")");
		if (isConnected()) {
			if (mqttAsyncClient != null) {
				mqttAsyncClient.subscribe(topics, qos, listeners);
			} else if(mqttClient != null) {
				mqttClient.subscribe(topics, qos, listeners);
			}
		} else {
			LoggerUtility.warn(CLASS_NAME, METHOD, "Will not subscribe to topics(" + topics +
					") because MQTT client is not connected.");
		}
	}
	
	/**
	 * <p>UnSubscribe the library from the given topic</p>
	 * 
	 * <p> This method is used by the library to unsubscribe each of the topic 
	 * where IBM IoT Foundation will send the DM requests</p>
	 *  
	 * @param topic topic to be unsubscribed
	 * @throws MqttException
	 */
	public void unsubscribe(ServerTopic topic) throws MqttException {
		final String METHOD = "unsubscribe";
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic(" + topic + ")");
		if (isConnected()) {
			if (mqttAsyncClient != null) {
				mqttAsyncClient.unsubscribe(topic.getName());
			} else if (mqttClient != null) {
				mqttClient.unsubscribe(topic.getName());
			}
		} else {
			LoggerUtility.warn(CLASS_NAME, METHOD, "Will not unsubscribe from topic(" + 
										topic + ") because MQTT client is not connected.");
		}
	}
	
	/**
	 * <p>UnSubscribe the library from the given topics</p>
	 * 
	 * <p> This method is used by the library to unsubscribe each of the topic 
	 * where IBM IoT Foundation will send the DM requests</p>
	 *  
	 * @param topics topics to be unsubscribed
	 * @throws MqttException
	 */
	public void unsubscribe(String[] topics) throws MqttException {
		final String METHOD = "unsubscribe#2";
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topics(" + topics + ")");
		if (isConnected()) {
			if (mqttAsyncClient != null) {
				mqttAsyncClient.unsubscribe(topics);
			} else if (mqttClient != null) {
				mqttClient.unsubscribe(topics);
			}
		} else {
			LoggerUtility.warn(CLASS_NAME, METHOD, "Will not unsubscribe from topics(" + 
										topics + ") because MQTT client is not connected.");
		}
	}
	
	protected IMqttDeliveryToken publish(DeviceTopic topic, MqttMessage message) throws MqttException {
		final String METHOD = "publish";
		IMqttDeliveryToken token = null;
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic(" + topic + ")");
		while(true) {
			if (isConnected()) {
				try {
					if (this.mqttAsyncClient != null) {
						token = mqttAsyncClient.publish(topic.getName(), message);
					} else if (mqttClient != null) {
						mqttClient.publish(topic.getName(), message);
					}
				} catch(MqttException ex) {
					String payload = null;
					try {
						payload = new String(message.getPayload(), "UTF-8");
					} catch (UnsupportedEncodingException e1) {	}
					if(this.mqttAsyncClient.isConnected() == false) {
						LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, " Connection Lost retrying to publish MSG :"+
								payload +" on topic "+topic+" every 5 seconds");
					
						// 	wait for 5 seconds and retry
						try {
							Thread.sleep(5 * 1000);
							continue;
						} catch (InterruptedException e) {}
					} else {
						throw ex;
					}
				}
			
				if (isConnected() == false) {
					LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, "MQTT got disconnected "
							+ "after publish to Topic(" + topic + ")");
				}
				return token;
			} else {
				LoggerUtility.warn(CLASS_NAME, METHOD, ": Will not publish to topic(" + 
									topic + ") because MQTT client is not connected.");
				try {
					Thread.sleep(5 * 1000);
					continue;
				} catch (InterruptedException e) {}
			}
		}
		
	}
	
	/**
	 * <p>Publish the Device management response to IBm IoT Foundation </p>
	 *  
	 * <p>This method is used by the library to respond to each of the Device Management commands from
	 *  IBM IoT Foundation</p>
	 * 
	 * @param topic Topic where the response to be published
	 * @param payload the Payload
	 * @param qos The Quality Of Service
	 * @throws MqttException
	 */
	public void publish(DeviceTopic topic, JsonObject payload, int qos) throws MqttException {
		final String METHOD = "publish3";
		JsonObject jsonPubMsg = new JsonObject();
		jsonPubMsg.addProperty("topic", topic.getName());
		jsonPubMsg.add("qos", new JsonPrimitive(qos));
		jsonPubMsg.add("payload", payload);		
		publishQueue.add(jsonPubMsg);
		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, ": Queued Topic(" + topic + ") qos=" + 
													qos + " payload (" + payload.toString() + ")");
	}
	
	private void publish(JsonObject jsonPubMsg) throws MqttException, UnsupportedEncodingException {
		final String METHOD = "publish1";
		String topic = jsonPubMsg.get("topic").getAsString();
		int qos = jsonPubMsg.get("qos").getAsInt();
		JsonObject payload = jsonPubMsg.getAsJsonObject("payload");
		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, ": Topic(" + topic + ") qos=" + 
												qos + " payload (" + payload.toString() + ")");
		MqttMessage message = new MqttMessage();
		message.setPayload(payload.toString().getBytes("UTF-8"));
		message.setQos(qos);
		publish(DeviceTopic.get(topic), message);
	}
	
	/**
	 * <p>Send the message and waits for the response from IBM IoT Foundation<p>
	 *  
	 * <p>This method is used by the library to send following messages to
	 *  IBM IoT Foundation</p>
	 *  
	 *  <ul class="simple">
	 * <li>Manage 
	 * <li>Unmanage
	 * <li>Location update
	 * <li>Diagnostic update/clear
	 * </ul>
	 * 
	 * @param topic Topic where the message to be sent 
	 * @param jsonPayload The message
	 * @param timeout How long to wait for the resonse
	 * @return response in Json format
	 * @throws MqttException
	 */
	public JsonObject sendAndWait(DeviceTopic topic, JsonObject jsonPayload, long timeout) throws MqttException {
		
		final String METHOD = "sendAndWait";
		
		String uuid = UUID.randomUUID().toString();
		jsonPayload.add("reqId", new JsonPrimitive(uuid));

		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic (" + topic + 
				") payload (" + jsonPayload.toString() + ") reqId (" + uuid + ")" );
		
		if (responseSubscription == null) {
			responseSubscription = ServerTopic.RESPONSE;
			subscribe(responseSubscription, 1, this);
		}
	
		MqttMessage message = new MqttMessage();
		try {
			message.setPayload(jsonPayload.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Error setting payload for topic: " + topic, e);
			return null;
		}
		
		message.setQos(1);
		
		requests.put(uuid, message);
		
		publish(topic, message);

		JsonObject jsonResponse = null;
		while (jsonResponse == null) {
			try {
				jsonResponse = queue.poll(timeout, TimeUnit.MILLISECONDS);
				if (jsonResponse == null) {
					break;
				}
				if (jsonResponse.get("reqId").getAsString().equals(uuid)) {
					LoggerUtility.fine(CLASS_NAME, METHOD, ""
							+ "This response is for me reqId:" + jsonResponse.toString() );
					break;
				} else {
					// This response is not for our request, put it back to the queue.
					LoggerUtility.warn(CLASS_NAME, METHOD, "This response is NOT for me reqId:" + jsonResponse.toString() );
					queue.add(jsonResponse);
					jsonResponse = null;
				}
			} catch (InterruptedException e) {
				break;
			}
		}
		if (jsonResponse == null) {
			LoggerUtility.warn(CLASS_NAME, METHOD, "NO RESPONSE from IoTF for request: " + jsonPayload.toString());
			LoggerUtility.warn(CLASS_NAME, METHOD, "Connected(" + isConnected() + ")");
		}
		return jsonResponse;
	}
	
	
	/**
	 * Disconnects from IBM IoT Foundation
	 */
	@Override
	public void disconnect() {
		if(this.bManaged == true) {
			try {
				unmanage();
			} catch (MqttException e) {
			}
			this.bManaged = false;
		}
		super.disconnect();
	}

	/**
	 * This method reconnects when the connection is lost due to n/w interruption and this method 
	 * is called only when the connection is established originally by the library code.
	 * 
	 * This method does the following activities,
	 * 1. Checks whether the device was in a managed state before disconnecting
	 * 2. Calculates the lifetime that we need to send in the
	 * manage request,
	 */
	@Override
	protected void reconnect() {
		String METHOD = "reconnect";
		
		IMqttDeliveryToken[] tokens = this.mqttAsyncClient.getPendingDeliveryTokens();
		super.connect();
		
		responseSubscription = null;
		if(this.isConnected() && this.bManaged == true) {
			long lifetime = 0;
			if(dormantTime != null) {
				Date currentTime = new Date();
				lifetime = (dormantTime.getTime() - currentTime.getTime()) / 1000;
				if(lifetime < 0) {
					lifetime = 0;
				}
			}
			try {
				
				LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "lifetime (" + lifetime + ")");
				this.manage(lifetime);
				
				if(tokens != null) {
					LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Republishing messages start");
					for(int i = 0; i < tokens.length; i++) {
						try {
							MqttMessage msg = tokens[i].getMessage();
							this.mqttAsyncClient.publish(tokens[i].getTopics()[0] , msg);
						} catch (MqttException e) {
							e.printStackTrace();
						}
					}
					LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Republishing messages End");
				}
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		final String METHOD = "messageArrived";
		if (topic.equals(ServerTopic.RESPONSE.getName())) {
			LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, 
					"Received response from IoT Foundation, topic (" + topic + ")");
			
			String responsePayload = new String (message.getPayload(), "UTF-8");
			JsonObject jsonResponse = new JsonParser().parse(responsePayload).getAsJsonObject();
			try {
				String reqId = jsonResponse.get("reqId").getAsString();
				LoggerUtility.fine(CLASS_NAME, METHOD, "reqId (" + reqId + "): " + jsonResponse.toString() );
				MqttMessage sentMsg = requests.remove(reqId);
				if (sentMsg != null) {
					queue.put(jsonResponse);
				} 
			} catch (Exception e) {
				if (jsonResponse.get("reqId") == null) {
					LoggerUtility.warn(CLASS_NAME, METHOD, "The response "
							+ "does not contain 'reqId' field (" + responsePayload + ")");
				} else {
					LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Unexpected exception", e);
				}
			}
		} else {
			LoggerUtility.warn(CLASS_NAME, METHOD, "Unknown topic (" + topic + ")");
		}
	}

	@Override
	public void run() {
		final String METHOD = "run";
		running = true;
		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Running...");
		while (running) {
			try {
				JsonObject o = publishQueue.take();
				if (o.equals(dummy)) {
					LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "It is time to quit.");
				} else {
					publish(o);
				}
			} catch (Exception e) {
				LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, e.toString());
				e.printStackTrace();
				running = false;
			}
		}
		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Exiting...");
	}
	
	private void terminate() {
		running = false;
		try {
			publishQueue.put(dummy);
		} catch (InterruptedException e) {
		}
	}

}
