/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.DateFormatUtils;
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
import com.ibm.iotf.devicemgmt.DeviceActionHandler;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmwareHandler;
import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.internal.iotf.devicemgmt.device.DeviceDMAgentTopic;
import com.ibm.internal.iotf.devicemgmt.device.DeviceDMServerTopic;
import com.ibm.internal.iotf.devicemgmt.handler.DMRequestHandler;
import com.ibm.internal.iotf.devicemgmt.DMAgentTopic;
import com.ibm.internal.iotf.devicemgmt.DMServerTopic;
import com.ibm.internal.iotf.devicemgmt.ManagedClient;
import com.ibm.internal.iotf.devicemgmt.ResponseCode;
import com.ibm.iotf.util.LoggerUtility;

/**
 * A managed device class, used by device, that connects the device as managed device to IBM Watson IoT Platform and
 * enables devices to perform one or more Device Management operations,
 *
 * <p>The device management feature enhances the Watson IoT Platform service with new capabilities
 * for managing devices.</p>
 *
 * <p> What does Device Management add? </p>
 * <ul class="simple">
 * <li>Control and management of device lifecycles for both individual and batches of devices.</li>
 * <li>Device metadata and status information, enabling the creation of device dashboards and other tools.</li>
 * <li>Diagnostic information, both for connectivity to the Watson IoT Platform service, and device diagnostics.</li>
 * <li>Device management commands, like firmware update, and device reboot.</li>
 * </ul>
 * <p> This is a derived class from DeviceClient and can be used by embedded devices to perform both <b>Device operations
 * and Device Management operations</b>, i.e, the devices can use this class to do the following, </p>
 *
 * <ul class="simple">
 * <li>Publish device events</li>
 * <li>Subscribe to commands from application</li>
 * <li>Perform Device management operations like, manage, unmanage, firmware update, reboot,
 *    update location, Diagnostics informations, Factory Reset and etc..</li>
 * </ul>
 *
 */

public class ManagedDevice extends DeviceClient implements IMqttMessageListener, Runnable {

	private static final String CLASS_NAME = ManagedDevice.class.getName();
	private static final int REGISTER_TIMEOUT_VALUE = 60 * 1000 * 2; // wait for 2 minute

	private final SynchronousQueue<JsonObject> queue = new SynchronousQueue<JsonObject>();

	private volatile boolean running = false;
	private BlockingQueue<JsonObject> publishQueue;
	JsonObject dummy = new JsonObject();

	private DeviceFirmwareHandler fwHandler = null;
	private DeviceActionHandler actionHandler = null;

	//Map to handle duplicate responses
	private Map<String, MqttMessage> requests = new HashMap<String, MqttMessage>();

	//Device specific information
	private DeviceData deviceData = null;

	private boolean supportDeviceActions = false;
	private boolean supportFirmwareActions = false;
	private boolean bManaged = false;
	private Date dormantTime;
	private String responseSubscription = null;
	private ManagedDeviceClient client;

    /**
     * Constructor that creates a ManagedDevice object, but does not connect to
     * IBM Watson IoT Platform connect yet
     *
     * @param options      List of options to connect to IBM Watson IoT Platform Connect
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
		this.client = new ManagedDeviceClient(this);
	}

	/**
	 * Constructs a ManagedDevice Object
	 *
	 * @param client      MqttClient which encapsulates the connection to IBM Watson IoT Platform connect
	 * @param deviceData  The Device Model
     * @throws Exception   If the essential parameters are not set
	 */
	public ManagedDevice(MqttClient client, DeviceData deviceData) throws Exception {
		super(client);
		this.client = new ManagedDeviceClient(this);
		setDeviceData(deviceData);
	}

	/**
	 * Constructs a ManagedDevice Object
	 *
	 * @param client      MqttAsyncClient which encapsulates the connection to IBM Watson IoT Platform connect
	 * @param deviceData  The Device Model
     * @throws Exception   If the essential parameters are not set
	 */
	public ManagedDevice(MqttAsyncClient client, DeviceData deviceData) throws Exception {
		super(client);
		this.client = new ManagedDeviceClient(this);
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
	 * <p>This method just connects to the IBM Watson IoT Platform,
	 * Device needs to make a call to manage() to participate in Device
	 * Management activities.</p>
	 *
	 * <p>This method does nothing if the device is already connected. Also, this 
	 * method does not retry when the following exceptions occur.</p>
	 * 
	 * <ul class="simple">
	 *  <li> MqttSecurityException - One or more credentials are wrong
	 * 	<li>UnKnownHostException - Host doesn't exist. For example, a wrong organization name is used to connect.
	 * </ul>
	 * 
	 * @throws MqttException see above
	 *
	 *
	 */
	@Override
    public void connect() throws MqttException {
		final String METHOD = "connect";
		if (this.isConnected()) {
			LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, "Device is already connected");
			return;
		}
		super.connect();
	}


	/**
	 * <p>Send a device manage request to Watson IoT Platform</p>
	 *
	 * <p>A Device uses this request to become a managed device.
	 * It should be the first device management request sent by the
	 * Device after connecting to the IBM Watson IoT Platform.
	 * It would be usual for a device management agent to send this
	 * whenever is starts or restarts.</p>
	 *
	 * <p>This method connects the device to Watson IoT Platform connect if its not connected already</p>
	 *
	 * @param lifetime The length of time in seconds within
	 *        which the device must send another Manage device request.
	 *        if set to 0, the managed device will not become dormant.
	 *        When set, the minimum supported setting is 3600 (1 hour).
	 *
	 * @param supportFirmwareActions Tells whether the device supports firmware actions or not.
	 *        The device must add a firmware handler to handle the firmware requests.
	 *
	 * @param supportDeviceActions Tells whether the device supports Device actions or not.
	 *        The device must add a Device action handler to handle the reboot and factory reset requests.
	 *
	 * @return boolean response containing the status of the manage request
	 * @throws MqttException When there is a failure
	 */
	public boolean sendManageRequest(long lifetime, boolean supportFirmwareActions,
			boolean supportDeviceActions) throws MqttException {

		final String METHOD = "manage";

		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "lifetime(" + lifetime + "), " +
				"supportFirmwareActions("+ supportFirmwareActions + "), "+
				"supportDeviceActions("+supportDeviceActions+")");

		boolean success = false;
		String topic = client.getDMAgentTopic().getManageTopic();

		if (!this.isConnected()) {
			this.connect();
		}

		this.supportDeviceActions = supportDeviceActions;
		this.supportFirmwareActions = supportFirmwareActions;
		JsonObject jsonPayload = new JsonObject();
		JsonObject supports = new JsonObject();
		supports.add("deviceActions", new JsonPrimitive(supportDeviceActions));
		supports.add("firmwareActions", new JsonPrimitive(supportFirmwareActions));

		JsonObject data = new JsonObject();
		data.add("supports", supports);
		if (deviceData.getDeviceInfo() != null) {
			data.add("deviceInfo", deviceData.getDeviceInfo().toJsonObject());
		}
		if (deviceData.getMetadata() != null) {
			data.add("metadata", deviceData.getMetadata().getMetadata());
		}
		if(lifetime > 0) {
			data.add("lifetime", new JsonPrimitive(lifetime));
		}
		jsonPayload.add("d", data);


		JsonObject jsonResponse = sendAndWait(topic, jsonPayload, REGISTER_TIMEOUT_VALUE);
		if (jsonResponse != null && jsonResponse.get("rc").getAsInt() ==
				ResponseCode.DM_SUCCESS.getCode()) {
			DMRequestHandler.setRequestHandlers(this.client);
			if(!running) {
				publishQueue = new LinkedBlockingQueue<JsonObject>();
				Thread t = new Thread(this);
				t.start();
				running = true;
			}
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
	 * Update the location.
	 *
	 * @param latitude	Latitude in decimal degrees using WGS84
	 * @param longitude Longitude in decimal degrees using WGS84
	 * @param elevation	Elevation in meters using WGS84
	 *
	 * @return code indicating whether the update is successful or not
	 *        (200 means success, otherwise unsuccessful)

	 */
	public int updateLocation(Double latitude, Double longitude, Double elevation) {
		return updateLocation(latitude, longitude, elevation, new Date());
	}

	/**
	 * Update the location of the device. This method converts the
	 * date in the required format. The caller just need to pass the date in java.util.Date format
	 *
	 * @param latitude	Latitude in decimal degrees using WGS84
	 * @param longitude Longitude in decimal degrees using WGS84
	 * @param elevation	Elevation in meters using WGS84
	 * @param measuredDateTime When the location information is retrieved
	 *
	 * @return code indicating whether the update is successful or not
	 *        (200 means success, otherwise unsuccessful)

	 */
	public int updateLocation(Double latitude, Double longitude, Double elevation, Date measuredDateTime) {
		return updateLocation(latitude, longitude, elevation, measuredDateTime, null);
	}

	/**
	 * Update the location of the device. This method converts the
	 * date in the required format. The caller just need to pass the date in java.util.Date format
	 *
	 * @param latitude	Latitude in decimal degrees using WGS84
	 * @param longitude Longitude in decimal degrees using WGS84
	 * @param elevation	Elevation in meters using WGS84
	 * @param measuredDateTime When the location information is retrieved
	 * @param accuracy	Accuracy of the position in meters
	 *
	 * @return code indicating whether the update is successful or not
	 *        (200 means success, otherwise unsuccessful)

	 */
	public int updateLocation(Double latitude, Double longitude, Double elevation, Date measuredDateTime, Double accuracy) {
		final String METHOD = "updateLocation";
		JsonObject jsonData = new JsonObject();

		JsonObject json = new JsonObject();
		json.addProperty("longitude", longitude);
		json.addProperty("latitude", latitude);
		if(elevation != null) {
			json.addProperty("elevation", elevation);
		}
		String utcTime = DateFormatUtils.formatUTC(measuredDateTime,
				DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
		json.addProperty("measuredDateTime", utcTime);

		if(accuracy != null) {
			json.addProperty("accuracy", accuracy);
		}

		jsonData.add("d", json);

		try {
			JsonObject response = sendAndWait(client.getDMAgentTopic().getUpdateLocationTopic(),
					jsonData, REGISTER_TIMEOUT_VALUE);
			if (response != null ) {
				return response.get("rc").getAsInt();
			}
		} catch (MqttException e) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, e.toString());
		}

		return 0;
	}

	/**
	 * Clear the Error Codes from IBM Watson IoT Platform for this device
	 * @return code indicating whether the clear operation is successful or not
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int clearErrorCodes() {
		final String METHOD = "clearErrorCodes";
		JsonObject jsonData = new JsonObject();
		try {
			JsonObject response = sendAndWait(client.getDMAgentTopic().getClearDiagErrorCodesTopic(),
					jsonData, REGISTER_TIMEOUT_VALUE);
			if (response != null ) {
				return response.get("rc").getAsInt();
			}
		} catch (MqttException e) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, e.toString());
		}
		return 0;
	}

	/**
	 * Clear the Logs from IBM Watson IoT Platform for this device
	 * @return code indicating whether the clear operation is successful or not
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int clearLogs() {
		final String METHOD = "clearLogs";
		JsonObject jsonData = new JsonObject();
		try {
			JsonObject response = sendAndWait(client.getDMAgentTopic().getClearDiagLogsTopic(),
					jsonData, REGISTER_TIMEOUT_VALUE);
			if (response != null ) {
				return response.get("rc").getAsInt();
			}
		} catch (MqttException e) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, e.toString());
		}
		return 0;
	}

	/**
	 * Adds the current errorcode to IBM Watson IoT Platform.
	 *
	 * @param errorCode The "errorCode" is a current device error code that
	 * needs to be added to the Watson IoT Platform.
	 *
	 * @return code indicating whether the update is successful or not
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int addErrorCode(int errorCode) {
		final String METHOD = "addErrorCode";
		JsonObject jsonData = new JsonObject();
		JsonObject errorObj = new JsonObject();
		errorObj.addProperty("errorCode", errorCode);
		jsonData.add("d", errorObj);

		try {
			JsonObject response = sendAndWait(client.getDMAgentTopic().getAddErrorCodesTopic(),
					jsonData, REGISTER_TIMEOUT_VALUE);
			if (response != null ) {
				return response.get("rc").getAsInt();
			}
		} catch (MqttException e) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, e.toString());
		}
		return 0;
	}

	/**
	 * Appends a Log message to the Watson IoT Platform.
	 * @param message The Log message that needs to be added to the Watson IoT Platform.
	 * @param timestamp The Log timestamp
	 * @param severity the Log severity
	 *
	 * @return code indicating whether the update is successful or not
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int addLog(String message, Date timestamp, LogSeverity severity) {
		return addLog(message, timestamp, severity, null);
	}

	/**
	 * The Log message that needs to be added to the Watson IoT Platform.
	 *
	 * @param message The Log message that needs to be added to the Watson IoT Platform.
	 * @param timestamp The Log timestamp
	 * @param severity The Log severity
	 * @param data The optional diagnostic string data -
	 *             The library will encode the data in base64 format as required by the Platform
	 * @return code indicating whether the update is successful or not
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int addLog(String message, Date timestamp, LogSeverity severity, String data) {
		final String METHOD = "addLog";
		JsonObject jsonData = new JsonObject();
		JsonObject log = new JsonObject();
		log.add("message", new JsonPrimitive(message));
		log.add("severity", new JsonPrimitive(severity.getSeverity()));
		String utcTime = DateFormatUtils.formatUTC(timestamp,
				DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
		log.add("timestamp", new JsonPrimitive(utcTime));

		if(data != null) {
			byte[] encodedBytes = Base64.encodeBase64(data.getBytes());
			log.add("data", new JsonPrimitive(new String(encodedBytes)));
		}
		jsonData.add("d", log);

		try {
			JsonObject response = sendAndWait(client.getDMAgentTopic().getAddDiagLogTopic(),
					jsonData, REGISTER_TIMEOUT_VALUE);
			if (response != null ) {
				return response.get("rc").getAsInt();
			}
		} catch (MqttException e) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, e.toString());
		}
		return 0;
	}

	/**
	 * Moves the device from managed state to unmanaged state
	 *
	 * A device uses this request when it no longer needs to be managed.
	 * This means Watson IoT Platform will no longer send new device management requests
	 * to this device and device management requests from this device will
	 * be rejected apart from a Manage device request
	 *
	 * @return
	 * 		True if the unmanage command is successful

	 * @throws MqttException When failure
	 */
	public boolean sendUnmanageRequest() throws MqttException {

		final String METHOD = "unmanage";
		boolean success = false;
		String topic = client.getDMAgentTopic().getUnmanageTopic();

		JsonObject jsonPayload = new JsonObject();
		JsonObject jsonResponse = sendAndWait(topic, jsonPayload, REGISTER_TIMEOUT_VALUE);
		if (jsonResponse != null && jsonResponse.get("rc").getAsInt() ==
				ResponseCode.DM_SUCCESS.getCode()) {
			success = true;
		}

		terminate();
		DMRequestHandler.clearRequestHandlers(this.client);
		this.terminateHandlers();

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
	 * where IBM Watson IoT Platform will send the DM requests</p>
	 *
	 * @param topic topic to be subscribed
	 * @param qos Quality of Service for the subscription
	 * @param listener The IMqttMessageListener for the given topic

	 * @throws MqttException When subscription fails
	 */
	public void subscribe(String topic, int qos, IMqttMessageListener listener) throws MqttException {
		final String METHOD = "subscribe";
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic(" + topic + ")");
		if (isConnected()) {
			if (mqttAsyncClient != null) {
				mqttAsyncClient.subscribe(topic, qos, listener).waitForCompletion();
			} else if(mqttClient != null) {
				mqttClient.subscribe(topic, qos, listener);
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
	 * where IBM Watson IoT Platform will send the DM requests</p>
	 *
	 * @param topics List of topics to be subscribed
	 * @param qos Quality of Service for the subscription
	 * @param listeners The list of IMqttMessageListeners for the given topics
	 * @throws MqttException When subscription fails
	 */
	public void subscribe(String[] topics, int[] qos, IMqttMessageListener[] listeners) throws MqttException {
		final String METHOD = "subscribe#2";
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topics(" + topics + ")");
		if (isConnected()) {
			if (mqttAsyncClient != null) {
				mqttAsyncClient.subscribe(topics, qos, listeners).waitForCompletion();
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
	 * where IBM Watson IoT Platform will send the DM requests</p>
	 *
	 * @param topic topic to be unsubscribed
	 * @throws MqttException when unsubscribe fails
	 */
	public void unsubscribe(String topic) throws MqttException {
		final String METHOD = "unsubscribe";
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic(" + topic + ")");
		if (isConnected()) {
			if (mqttAsyncClient != null) {
				mqttAsyncClient.unsubscribe(topic);
			} else if (mqttClient != null) {
				mqttClient.unsubscribe(topic);
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
	 * where IBM Watson IoT Platform will send the DM requests</p>
	 *
	 * @param topics topics to be unsubscribed
	 * @throws MqttException when unsubscribe fails
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

	protected IMqttDeliveryToken publish(String topic, MqttMessage message) throws MqttException {
		final String METHOD = "publish";
		IMqttDeliveryToken token = null;
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic(" + topic + ")");
		while(true) {
			if (isConnected()) {
				try {
					if (this.mqttAsyncClient != null) {
						token = mqttAsyncClient.publish(topic, message);
					} else if (mqttClient != null) {
						mqttClient.publish(topic, message);
					}
				} catch(MqttException ex) {
					long wait;
					switch (ex.getReasonCode()) {
					case MqttException.REASON_CODE_CLIENT_NOT_CONNECTED:
					case MqttException.REASON_CODE_CLIENT_DISCONNECTING:
						try {
							LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, " Connection Lost retrying to publish MSG :"+
									new String(message.getPayload(), "UTF-8") + " on topic "+topic+" every 5 seconds");
						} catch (UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}
						wait = 5 * 1000;
						break;
					case MqttException.REASON_CODE_MAX_INFLIGHT:
						wait = 50;
						break;
					default:
						throw ex;
					}
					// Retry
					try {
						Thread.sleep(wait);
						continue;
					} catch (InterruptedException e) {}
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
	 * <p>Publish the Device management response to IBm Watson IoT Platform </p>
	 *
	 * <p>This method is used by the library to respond to each of the Device Management commands from
	 *  IBM Watson IoT Platform</p>
	 *
	 * @param topic Topic where the response to be published
	 * @param payload the Payload
	 * @param qos The Quality Of Service
	 * @throws MqttException When MQTT operation fails
	 */
	public void publish(String topic, JsonObject payload, int qos) throws MqttException {
		final String METHOD = "publish3";
		JsonObject jsonPubMsg = new JsonObject();
		jsonPubMsg.addProperty("topic", topic);
		jsonPubMsg.add("qos", new JsonPrimitive(qos));
		jsonPubMsg.add("payload", payload);
		publishQueue.add(jsonPubMsg);
		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, ": Queued Topic(" + topic + ") qos=" +
													qos + " payload (" + payload.toString() + ")");
	}
	
	public void publish(String topic, JsonObject payload) throws MqttException {
		publish(topic, payload, this.getMessagingQoS());
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
		publish(topic, message);
	}

	/**
	 * <p>Send the message and waits for the response from IBM Watson IoT Platform</p>
	 *
	 * <p>This method is used by the library to send following messages to
	 *  IBM Watson IoT Platform</p>
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
	 * @param timeout How long to wait for the response
	 * @return response in Json format
	 * @throws MqttException when MQTT operation fails
	 */
	public JsonObject sendAndWait(String topic, JsonObject jsonPayload, long timeout) throws MqttException {

		final String METHOD = "sendAndWait";

		String uuid = UUID.randomUUID().toString();
		jsonPayload.add("reqId", new JsonPrimitive(uuid));

		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic (" + topic +
				") payload (" + jsonPayload.toString() + ") reqId (" + uuid + ")" );

		if (responseSubscription == null) {
			responseSubscription = client.getDMServerTopic().getDMServerTopic();
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
			LoggerUtility.warn(CLASS_NAME, METHOD, "NO RESPONSE from Watson IoT Platform for request: " + jsonPayload.toString());
			LoggerUtility.warn(CLASS_NAME, METHOD, "Connected(" + isConnected() + ")");
		}
		return jsonResponse;
	}


	/**
	 * Disconnects from IBM Watson IoT Platform
	 */
	@Override
	public void disconnect() {
		if(this.bManaged == true) {
			try {
				sendUnmanageRequest();
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
		try {
			super.connect();
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

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
				sendManageRequest(lifetime, this.supportFirmwareActions, this.supportDeviceActions);

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
		if (topic.equals(this.client.getDMServerTopic().getDMServerTopic())) {
			LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD,
					"Received response from Watson IoT Platform, topic (" + topic + ")");

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

	private class ManagedDeviceClient implements ManagedClient {

		private ManagedDevice dmClient;
		private DMAgentTopic deviceDMAgentTopic;
		private DMServerTopic deviceDMServerTopic;

		private ManagedDeviceClient(ManagedDevice dmClient) {
			this.dmClient = dmClient;
			deviceDMAgentTopic = DeviceDMAgentTopic.getInstance();
			deviceDMServerTopic = DeviceDMServerTopic.getInstance();
		}


		@Override
		public void subscribe(String topic, int qos,
				IMqttMessageListener iMqttMessageListener) throws MqttException {
			dmClient.subscribe(topic, qos, iMqttMessageListener);
		}

		@Override
		public void unsubscribe(String topic) throws MqttException {
			dmClient.unsubscribe(topic);
		}

		@Override
		public void publish(String response, JsonObject payload)
				throws MqttException {
			dmClient.publish(response, payload);
		}
		
		@Override
		public void publish(String response, JsonObject payload, int qos)
				throws MqttException {
			dmClient.publish(response, payload, qos);
		}

		@Override
		public DeviceData getDeviceData() {
			return dmClient.getDeviceData();
		}

		@Override
		public void subscribe(String[] topics, int[] qos,
				IMqttMessageListener[] listener) throws MqttException {
			dmClient.subscribe(topics, qos, listener);
		}

		@Override
		public void unsubscribe(String[] topics) throws MqttException {
			dmClient.unsubscribe(topics);
		}

		@Override
		public DMAgentTopic getDMAgentTopic() {
			return this.deviceDMAgentTopic;
		}

		@Override
		public DMServerTopic getDMServerTopic() {
			return this.deviceDMServerTopic;
		}


		@Override
		public DeviceActionHandler getActionHandler() {
			return dmClient.actionHandler;
		}
		
		@Override
		public DeviceFirmwareHandler getFirmwareHandler() {
			return dmClient.fwHandler;
		}

	}

	/**
	 * <p>Adds a firmware handler for this device,
	 * that is of type {@link com.ibm.iotf.devicemgmt.DeviceFirmwareHandler}</p>
	 *
	 * <p>If the device supports firmware update, the abstract class
	 * {@link com.ibm.iotf.devicemgmt.DeviceFirmwareHandler} should be extended by the device code.
	 * The {@link com.ibm.iotf.devicemgmt.DeviceFirmwareHandler#downloadFirmware} and
	 * {@link com.ibm.iotf.devicemgmt.DeviceFirmwareHandler#updateFirmware}
	 * must be implemented to handle the firmware actions.</p>
	 *
	 * @param fwHandler {@link com.ibm.iotf.devicemgmt.DeviceFirmwareHandler} that handles the Firmware actions
	 * @throws Exception throws an exception if a handler is already added
	 *
	 */
	public void addFirmwareHandler(DeviceFirmwareHandler fwHandler) throws Exception {
		final String METHOD = "addFirmwareHandler";
		if(this.fwHandler != null) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Firmware Handler is already set, "
					+ "so can not add the new firmware handler !");

			throw new Exception("Firmware Handler is already set, "
					+ "so can not add the new firmware handler !");
		}
		this.fwHandler = fwHandler;
	}

	/**
	 * <p>Adds a device action handler which is of type {@link com.ibm.iotf.devicemgmt.DeviceActionHandler}</p>
	 *
	 * <p>If the device supports device actions like reboot and factory reset,
	 * the abstract class {@link com.ibm.iotf.devicemgmt.DeviceActionHandler}
	 * should be extended by the device code. The {@link com.ibm.iotf.devicemgmt.DeviceActionHandler#handleReboot} and
	 * {@link com.ibm.iotf.devicemgmt.DeviceActionHandler#handleFactoryReset}
	 * must be implemented to handle the actions.</p>
	 *
	 * @param actionHandler {@link com.ibm.iotf.devicemgmt.DeviceActionHandler} that handles the Reboot and Factory reset actions
	 * @throws Exception throws an exception if a handler is already added
	 */
	public void addDeviceActionHandler(DeviceActionHandler actionHandler) throws Exception {
		final String METHOD = "addDeviceActionHandler";
		if(this.actionHandler != null) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Action Handler is already set, "
					+ "so can not add the new Action handler !");

			throw new Exception("Action Handler is already set, "
					+ "so can not add the new Action handler !");
		}
		this.actionHandler = actionHandler;
	}

	private void terminateHandlers() {
		fwHandler = null;
		actionHandler = null;
	}
}
