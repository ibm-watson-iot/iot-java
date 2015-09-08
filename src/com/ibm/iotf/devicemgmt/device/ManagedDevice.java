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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.devicemgmt.device.DeviceData;
import com.ibm.iotf.devicemgmt.device.handler.DMRequestHandler;
import com.ibm.iotf.devicemgmt.device.listener.DMListener;
import com.ibm.iotf.util.LoggerUtility;

/**
 * A managed device class, used by device, that connects the device as managed device to IBM IoT Foundation and
 * enables devices to perform one or more Device Management operations,
 * 
 * The device management feature enhances the Internet of Things Foundation service with new capabilities for 
 * managing devices. 
 * 
 * What does Device Management add?
 * 
 * Control and management of device lifecycles for both individual and batches of devices.
 * Device metadata and status information, enabling the creation of device dashboards and other tools.
 * Diagnostic information, both for connectivity to the Internet of Things Foundation service, and device diagnostics.
 * Device management commands, like firmware update, and device reboot.
 * 
 * This is a derived class from DeviceClient and can be used by embedded devices to perform both device and Management operations,
 * 
 * i.e, the devices can use this class to do the following,
 * 
 * 1. Publish device events
 * 2. Subscribe to commands from application
 * 3. Perform Device management operations like, manage, unmanage, firmware update, reboot, 
 *    update location, Diagnostics informations, Factory Reset and etc.. 
 * 
 */

public class ManagedDevice extends DeviceClient implements IMqttMessageListener{
	
	private static final String CLASS_NAME = ManagedDevice.class.getName();
	
	private static final int REGISTER_TIMEOUT_VALUE = 60 * 1000; // wait for 1 minute
	
	
		
	private final SynchronousQueue<JsonObject> queue = new SynchronousQueue<JsonObject>();
	
	//Map to handle duplicate responses
	private Map<String, MqttMessage> requests = new HashMap<String, MqttMessage>();
	
	//Device specific information
	private DeviceData deviceData = null;
	
	private boolean supportsDeviceActions = false;
	private boolean supportsFirmwareActions = false;
	private boolean bManaged = false;
	private Date dormantTime;
	private ServerTopic responseSubscription = null;
	
    
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
		
		if(typeId == null) {
			LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Could not create Managed Client "
					+ "without Device Type!");
			throw new Exception("Could not create Managed Client without Device Type!, "
					+ "Please specify the same in properties");
		}
		deviceData.setTypeId(typeId);
		
		if(deviceId == null) {
			LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Could not create Managed Client "
					+ "without Device ID!");
			throw new Exception("Could not create Managed Client without Device ID!, "
					+ "Please specify the same in properties");
		}
		
		deviceData.setDeviceId(deviceId);
		this.deviceData = deviceData;
	}
	
	public DeviceData getDeviceData() {
		return deviceData;
	}
	
	public void supportsDeviceActions(boolean supportsDeviceActions) {
		this.supportsDeviceActions = supportsDeviceActions;
	}
	
	public void supportsFirmwareActions(boolean supportsFirmwareActions) {
		this.supportsFirmwareActions = supportsFirmwareActions;
	}

	
	/**
	 * Connect to the IBM Internet of Things Foundation and 
	 * send a device manage request such that this device
	 * can receive a device management commands from IoT
	 * Foundation DM server 
	 * 
	 * @param lifetime The length of time in seconds within 
	 *        which the device must send another Manage device request 
	 * 
	 */	
	public void connect(long lifetime) {
		final String METHOD = "connect(lifetime)";
		super.connect();
		
		String organization = getOrgId();
		if (organization == null || ("quickstart").equals(organization)) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Unable to create ManagedClient instance.  "
					+ "QuickStart devices do not support device management");
			
			throw new RuntimeException("Unable to create ManagedClient instance.  "
					+ "QuickStart devices do not support device management");
		}
		try {
			boolean success = this.manage(lifetime);
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
	}
	
	/**
	 * Connect to the IBM Internet of Things Foundation and 
	 * send a device manage request such that this device
	 * can receive a device management commands from IoT
	 * Foundation DM server 
	 * 
	 * This method connects with lifetime 0 - the device will
	 * never become dormant
	 * 
	 */	
	public void connect() {
		final String METHOD = "connect";
		super.connect();
		
		String organization = getOrgId();
		if (organization == null || ("quickstart").equals(organization)) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Unable to create ManagedClient instance.  "
					+ "QuickStart devices do not support device management");
			
			throw new RuntimeException("Unable to create ManagedClient instance.  "
					+ "QuickStart devices do not support device management");
		}
		try {
			boolean success = this.manage(0);
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
	}
	
	
	/**
	 * Send a device manage request to IoT Foundation
	 * 
	 * A device uses this request to become a managed device. 
	 * It should be the first device management request sent by the 
	 * device after connecting to the Internet of Things Foundation. 
	 * It would be usual for a device management agent to send this 
	 * whenever is starts or restarts.
	 * 
	 * @param lifetime The length of time in seconds within 
	 *        which the device must send another Manage device request 
	 * @return True if successful
	 * @throws MqttException
	 */
	public boolean manage(long lifetime) throws MqttException {
		
		final String METHOD = "manage";
		
		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "lifetime value (" + lifetime + ")");
		
		boolean success = false;
		DeviceTopic topic = DeviceTopic.MANAGE;
		
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
				data.add("metadata", deviceData.getMetadata());
			}
			data.add("lifetime", new JsonPrimitive(lifetime));
			jsonPayload.add("d", data);
		}

		JsonObject jsonResponse = sendAndWait(topic, jsonPayload, REGISTER_TIMEOUT_VALUE);
		if (jsonResponse != null && jsonResponse.get("rc").getAsInt() == 
				ResponseCode.DM_SUCCESS.getCode()) {
			DMListener.start(this);
			DMRequestHandler.setRequestHandlers(this);
			
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
		DeviceTopic topic = DeviceTopic.UNMANGE;

		JsonObject jsonPayload = new JsonObject();
		JsonObject jsonResponse = sendAndWait(topic, jsonPayload, REGISTER_TIMEOUT_VALUE);
		if (jsonResponse != null && jsonResponse.get("rc").getAsInt() == 
				ResponseCode.DM_SUCCESS.getCode()) {
			success = true;	
		}

		DMListener.stop(this);
		DMRequestHandler.clearRequestHandlers(this);
		this.deviceData.terminateHandlers();
		this.supportsDeviceActions = false;
		this.supportsFirmwareActions = false;
		
		if (responseSubscription != null) {
			if (mqttAsyncClient != null) {
				mqttAsyncClient.unsubscribe(responseSubscription.getName());
			}
			responseSubscription = null;
		}

		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Success (" + success + ")");
		if(success) {
			bManaged = false;
		}
		return success;
	}
	
	public boolean isConnected() {
		final String METHOD = "isConnected";
		boolean connected = false;
		if (mqttAsyncClient != null) {
			connected = mqttAsyncClient.isConnected();
		}
		LoggerUtility.log(Level.FINEST, CLASS_NAME, METHOD, "Connected(" + connected + ")");
		return connected;
	}
	
	public void subscribe(ServerTopic topic, int qos, IMqttMessageListener listener) throws MqttException {
		final String METHOD = "subscribe";
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic(" + topic + ")");
		if (isConnected()) {
			if (mqttAsyncClient != null) {
				mqttAsyncClient.subscribe(topic.getName(), qos, listener);
			}
		} else {
			LoggerUtility.warn(CLASS_NAME, METHOD, "Will not subscribe to topic(" + topic +
					") because MQTT client is not connected.");
		}
	}
	
	public void unsubscribe(String topic) throws MqttException {
		final String METHOD = "unsubscribe";
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic(" + topic + ")");
		if (isConnected()) {
			if (mqttAsyncClient != null) {
				mqttAsyncClient.unsubscribe(topic);
			}
		} else {
			LoggerUtility.warn(CLASS_NAME, METHOD, "Will not unsubscribe from topic(" + 
										topic + ") because MQTT client is not connected.");
		}
	}
	
	protected IMqttDeliveryToken publish(DeviceTopic topic, MqttMessage message) throws MqttException {
		final String METHOD = "publish";
		IMqttDeliveryToken token = null;
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic(" + topic + ")");
		while(true) {
			if (isConnected()) {
				try {
					token = mqttAsyncClient.publish(topic.getName(), message);
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
	
	public IMqttDeliveryToken publish(DeviceTopic topic, JsonObject payload, int qos) throws MqttException {
		final String METHOD = "publish";
		IMqttDeliveryToken token = null;
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic(" + topic + 
				") qos=" + qos + " payload (" + payload.toString() + ")");
		MqttMessage message = new MqttMessage();
		try {
			message.setPayload(payload.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Error setting payload for topic: " + topic, e);
			return null;
		}
		token = publish(topic, message);
		return token;
	}
	
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
	 * This method reconnects when the connection is lost due to n/w interruption
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
}
