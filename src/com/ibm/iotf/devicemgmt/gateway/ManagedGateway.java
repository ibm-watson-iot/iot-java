/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
 Sathiskumar Palaniappan - Initial implementation
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt.gateway;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.ibm.iotf.client.gateway.GatewayClient;
import com.ibm.iotf.devicemgmt.internal.ManagedClient;
import com.ibm.iotf.devicemgmt.internal.DMAgentTopic;
import com.ibm.iotf.devicemgmt.internal.DMServerTopic;
import com.ibm.iotf.devicemgmt.DeviceActionHandler;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmwareHandler;
import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.handler.DMRequestHandler;
import com.ibm.iotf.devicemgmt.internal.ResponseCode;
import com.ibm.iotf.devicemgmt.resource.Resource;
import com.ibm.iotf.devicemgmt.gateway.internal.GatewayDMAgentTopic;
import com.ibm.iotf.devicemgmt.gateway.internal.GatewayDMServerTopic;
import com.ibm.iotf.util.LoggerUtility;

/**
 * A managed Gateway class, used by Gateway, to connect the Gateway and devices behind the Gateway as managed devices 
 * to IBM Watson IoT Platform and enables the Gateway to perform one or more Device Management operations.
 * 
 * <p>The device management feature enhances the IBM Watson IoT Platform service with new capabilities 
 * for managing devices and Gateways.</p>
 * 
 * <p> This is a derived class from {@link com.ibm.iotf.client.gateway.GatewayClient} and can be used by 
 * Gateway devices to perform both <b>Device operations and Device Management operations</b>, 
 * i.e, the Gateways can use this class to do the following, <p>
 * 
 * <ul class="simple">
 * <li>Connect devices behind the Gateway to IBM Watson IoT Platform
 * <li>Send and receive its own sensor data like a directly connected device,
 * <li>Send and receive data on behalf of the devices connected to it
 * <li>Perform Device management operations like, manage, unmanage, firmware update, reboot, 
 *    update location, Diagnostics informations, Factory Reset and etc.. 
 *    for both Gateway and devices connected to the Gateway</li> 
 * </ul>
 */

public class ManagedGateway extends GatewayClient implements IMqttMessageListener, Runnable {
	
	private static final String CLASS_NAME = ManagedGateway.class.getName();
	private static final int REGISTER_TIMEOUT_VALUE = 60 * 1000 * 2; // wait for 2 minute
	
	private final SynchronousQueue<JsonObject> queue = new SynchronousQueue<JsonObject>();
	private final Map<String, ManagedClient> devicesMap = new HashMap<String, ManagedClient>();
	private final BlockingQueue<JsonObject> publishQueue = new LinkedBlockingQueue<JsonObject>();
	
	private static final Pattern GATEWAY_RESPONSE_PATTERN = Pattern.compile("iotdm-1/type/(.+)/id/(.+)/response");
	private static final String GATEWAY_RESPONSE_TOPIC = "iotdm-1/type/+/id/+/response";
	
	private volatile boolean running = false;
	private JsonObject dummy = new JsonObject();
	private ManagedGatewayDevice gateway;
	private final String gatewayKey;
	
	private DeviceFirmwareHandler fwHandler = null;
	private DeviceActionHandler actionHandler = null;
	
	//Map to handle duplicate responses
	private Map<String, MqttMessage> requests = new HashMap<String, MqttMessage>();
	private boolean reponseSubscription;
	
    /**
     * <p>Constructor that creates a ManagedGateway object using the properties file, but does not connect to 
     * IBM Watson IoT Platform yet. The properties must have the following definitions,</p>
	 * 
	 * <ul class="simple">
	 * <li>org - Your organization ID.
	 * <li>type - The type of your Gateway device.
	 * <li>id - The ID of your Gateway.
	 * <li>auth-method - Method of authentication (The only value currently supported is "token").
	 * <li>auth-token - API key token.
	 * </ul>
     * 
     * @param options      List of options to connect to IBM Watson IoT Platform
     * @param deviceData   The Device Model
     * @throws Exception   If the essential parameters are not set
     */
	public ManagedGateway(Properties options, DeviceData deviceData) throws Exception {
		super(options);
		final String METHOD = "constructor";
		if(deviceData == null) {
			LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Could not create Managed Client "
					+ "without DeviceInformations !");
			throw new Exception("Could not create Managed Client without DeviceInformations !");
		}
		String typeId = this.getGWDeviceType();
		String deviceId = this.getGWDeviceId();
		
		if(typeId == null || deviceId == null) {
			LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Could not create Managed Client "
					+ "without Device Type or Device ID !");
			throw new Exception("Could not create Managed Client without Device Type or Device ID!, "
					+ "Please specify the same in properties");
		}
		deviceData.setTypeId(typeId);
		deviceData.setDeviceId(deviceId);
		ManagedClient mc = new ManagedGatewayDevice(this, deviceData);
		this.gateway = (ManagedGatewayDevice) mc;
		this.gatewayKey = typeId + ':' + deviceId;
		this.devicesMap.put(gatewayKey, mc);
	}
	
	/**
	 * Constructs a ManagedGateway Object using the Synchronous MqttClient created externally by the Gateway
	 * 	
	 * @param client      MqttClient which encapsulates the connection to IBM Watson IoT Platform 
	 * @param deviceData  The Device Model
     * @throws Exception   If the essential parameters are not set
	 */
	public ManagedGateway(MqttClient client, DeviceData deviceData) throws Exception {
		super(client);
		ManagedClient mc = new ManagedGatewayDevice(this, deviceData);
		this.gatewayKey = deviceData.getTypeId() + ':' + deviceData.getDeviceId();
		this.devicesMap.put(gatewayKey, mc);
	}
	
	/**
	 * Constructs a ManagedGateway Object using the Asynchronous MqttClient created externally by the Gateway
	 * 	
	 * @param client      MqttAsyncClient which encapsulates the connection to IBM Watson IoT Platform 
	 * @param deviceData  The Device Model
     * @throws Exception   If the essential parameters are not set
	 */
	public ManagedGateway(MqttAsyncClient client, DeviceData deviceData) throws Exception {
		super(client);
		ManagedClient mc = new ManagedGatewayDevice(this, deviceData);
		this.gatewayKey = deviceData.getTypeId() + ':' + deviceData.getDeviceId();
		this.devicesMap.put(gatewayKey, mc);
	}
	
	private String getGWTypeId() {
		if(gateway != null) {
			return this.gateway.getTypeId();
		}
		return this.getGWDeviceType();
	}
	
	/**
	 * Returns the Device ID of the gateway.
	 */
	public String getGWDeviceId() {
		if(this.gateway != null) {
			return this.gateway.getDeviceId();
		} else {
			return super.getGWDeviceId();
		}
	}

	
	/**
	 * <p>This method connects the Gateway to the IBM IBM Watson IoT Platform.</p>
	 * <p>Note that the Gateway needs to make a call manage() to participate in Device
	 * Management activities.<p> 
	 * 
	 * This method does nothing if the Gateway is already connected.
	 * 
	 */	
	public void connect() {
		final String METHOD = "connect";
		if (this.isConnected()) {
			LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, "Gateway device is already connected");
			return;
		}
		super.connect();
	}
	
	private class ManagedGatewayDevice implements ManagedClient {
		
		public Date getDormantTime() {
			return dormantTime;
		}

		private ManagedGateway gwClient;
		public void setDormantTime(Date dormantTime) {
			this.dormantTime = dormantTime;
		}

		private DMAgentTopic gatewayDMAgentTopic;
		private DMServerTopic gatewayDMServerTopic;
		
		//Device specific information
		private DeviceData deviceData = null;
		private boolean deviceActions = false;
		private boolean firmwareActions = false;
		
		public boolean isDeviceActions() {
			return deviceActions;
		}

		public boolean isFirmwareActions() {
			return firmwareActions;
		}

		private boolean bManaged = false;
		private Date dormantTime;

		private ManagedGatewayDevice(ManagedGateway gwClient, DeviceData deviceData) {
			this.gwClient = gwClient;
			this.deviceData = deviceData;
			this.gatewayDMAgentTopic = new GatewayDMAgentTopic(deviceData.getTypeId(), deviceData.getDeviceId());
			this.gatewayDMServerTopic = new GatewayDMServerTopic(deviceData.getTypeId(), deviceData.getDeviceId());
		}

		public ManagedGatewayDevice(ManagedGateway gwClient,
				DeviceData deviceData, boolean supportsFirmwareActions,
				boolean supportDeviceActions) {
			this.gwClient = gwClient;
			this.deviceData = deviceData;
			this.gatewayDMAgentTopic = new GatewayDMAgentTopic(deviceData.getTypeId(), deviceData.getDeviceId());
			this.gatewayDMServerTopic = new GatewayDMServerTopic(deviceData.getTypeId(), deviceData.getDeviceId());
			this.firmwareActions = supportsFirmwareActions;
			this.deviceActions = supportDeviceActions;
		}

		@Override
		public void subscribe(String topic, int qos,
				IMqttMessageListener iMqttMessageListener) throws MqttException {
			gwClient.subscribe(topic, qos, iMqttMessageListener);
		}

		@Override
		public void unsubscribe(String topic) throws MqttException {
			gwClient.unsubscribe(topic);
		}

		@Override
		public void publish(String response, JsonObject payload, int qos)
				throws MqttException {
			gwClient.publish(response, payload, qos);
		}

		@Override
		public DeviceData getDeviceData() {
			return this.deviceData;
		}

		@Override
		public void subscribe(String[] topics, int[] qos,
				IMqttMessageListener[] listener) throws MqttException {
			gwClient.subscribe(topics, qos, listener);
		}

		@Override
		public void unsubscribe(String[] topics) throws MqttException {
			gwClient.unsubscribe(topics);
		}

		@Override
		public DMAgentTopic getDMAgentTopic() {
			return this.gatewayDMAgentTopic;
		}

		@Override
		public DMServerTopic getDMServerTopic() {
			return this.gatewayDMServerTopic;
		}

		public void setbManaged(boolean bManaged) {
			this.bManaged = bManaged;
		}

		public String getTypeId() {
			return this.deviceData.getTypeId();
		}
		
		public String getDeviceId() {
			return this.deviceData.getDeviceId();
		}
	}
	
	
	/**
	 * <p>Sends a manage request to IBM Watson IoT Platform</p>
	 * 
	 * <p>A Gateway uses this request to become a managed device. 
	 * It should be the first device management request sent by the 
	 * Gateway after connecting to the IBM Watson IoT Platform. 
	 * It would be usual for a device management agent to send this 
	 * whenever is starts or restarts.</p>
	 * 
	 * <p>This method connects the Gateway to IBM Watson IoT Platform if its not connected already</p>
	 * 
	 * @param lifetime The length of time in seconds within 
	 *        which the Gateway must send another Manage device request.
	 *        if set to 0, the managed Gateway will not become dormant. 
	 *        When set, the minimum supported setting is 3600 (1 hour).
	 * 
	 * @param supportFirmwareActions Tells whether the Gateway supports firmware actions or not.
	 *        The Gateway must add a firmware handler to handle the firmware requests.
	 * 
	 * @param supportDeviceActions Tells whether the Gateway supports Device actions or not.
	 *        The Gateway must add a Device action handler to handle the reboot and factory reset requests.
	 * 
	 * @return
	 * @throws MqttException
	 */
	public boolean sendGatewayManageRequest(long lifetime, boolean supportFirmwareActions, 
			boolean supportDeviceActions) throws MqttException {
		// Add the gateway to the map if its not added already
		if(!this.devicesMap.containsKey(this.gatewayKey)) {
			this.devicesMap.put(gatewayKey, this.gateway);
		}
		return this.sendDeviceManageRequest(getGWTypeId(), getGWDeviceId(), gateway.getDeviceData(),
				lifetime, supportFirmwareActions, supportDeviceActions);
	}
	
	
	/**
	 * <p>Sends a device manage request to IBM Watson IoT Platform</p>
	 * 
	 * <p>A Gateway uses this request to manage the device connected to it, 
	 * It should be the first device management request sent for the 
	 * device after connecting to the IBM Watson IoT Platform. 
	 * It would be usual for a device management agent to send this 
	 * whenever is starts or restarts.</p>
	 * 
	 * <p>This method connects the Gateway/Device to IBM Watson IoT Platform if 
	 * its not connected already</p>
	 * 
	 * @param typeId The typeId of the device connected to the gateway
	 * 
	 * @param deviceId The deviceId of the device connected to the gateway
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
	 * @return True if successful
	 * @throws MqttException
	 */
	public boolean sendDeviceManageRequest(String typeId, String deviceId, 
			long lifetime, boolean supportFirmwareActions, 
			boolean supportDeviceActions) throws MqttException {
		
		return this.sendDeviceManageRequest(typeId, deviceId, null, 
				lifetime, supportFirmwareActions, supportDeviceActions);
		
	}
	
	/**
	 * <p>Sends a device manage request to IBM Watson IoT Platform</p>
	 * 
	 * <p>A Gateway uses this request to manage the device connected to it, 
	 * It should be the first device management request sent for the 
	 * device after connecting to the IBM Watson IoT Platform. 
	 * It would be usual for a device management agent to send this 
	 * whenever is starts or restarts.</p>
	 * 
	 * <p>This method connects the Gateway/Device to IBM Watson IoT Platform if 
	 * its not connected already</p>
	 * 
	 * @param typeId The typeId of the device connected to the gateway
	 * 
	 * @param deviceId The deviceId of the device connected to the gateway
	 * 
	 * @param DeviceData The DeviceData containing the information about the device
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
       
	 * @return True if successful
	 * @throws MqttException
	 */
	public boolean sendDeviceManageRequest(String typeId, 
									String deviceId, 
									DeviceData deviceData,
									long lifetime,
									boolean supportsFirmwareActions, 
									boolean supportDeviceActions) throws MqttException {
		
		final String METHOD = "manage";
		
		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "typeId(" + typeId + "), "
				+ "deviceId ("+ deviceId + "), lifetime value(" + lifetime + ")");
		
		String key = typeId + ':' + deviceId;
		ManagedGatewayDevice mc = (ManagedGatewayDevice) this.devicesMap.get(key);
		if(mc == null) {
			if(deviceData == null) {
				deviceData = new DeviceData.Builder().typeId(typeId).deviceId(deviceId).build();
			}
			mc = new ManagedGatewayDevice(this, deviceData, supportsFirmwareActions, supportDeviceActions);
		}

		if(reponseSubscription == false) {
			subscribe(GATEWAY_RESPONSE_TOPIC, 1, this);
			reponseSubscription = true;
		}
		

		boolean success = false;
		String topic = mc.getDMAgentTopic().getManageTopic();
		
		if (!this.isConnected()) {
			this.connect();
		}
		
		JsonObject jsonPayload = new JsonObject();
		JsonObject supports = new JsonObject();
		supports.add("deviceActions", new JsonPrimitive(supportDeviceActions));
		supports.add("firmwareActions", new JsonPrimitive(supportsFirmwareActions));
			
		JsonObject data = new JsonObject();
		data.add("supports", supports);
		if (mc.deviceData.getDeviceInfo() != null) {
			data.add("deviceInfo", mc.deviceData.getDeviceInfo().toJsonObject());
		}
		if (mc.deviceData.getMetadata() != null) {
			data.add("metadata", mc.deviceData.getMetadata().getMetadata());
		}
		data.add("lifetime", new JsonPrimitive(lifetime));
		jsonPayload.add("d", data);
		
		JsonObject jsonResponse = sendAndWait(topic, jsonPayload, REGISTER_TIMEOUT_VALUE);
		if (jsonResponse != null && jsonResponse.get("rc").getAsInt() == 
				ResponseCode.DM_SUCCESS.getCode()) {
			DMRequestHandler.setRequestHandlers(mc);
			if(!running) {
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
				mc.setDormantTime(new Date(currentTime.getTime() + (lifetime * 1000)));
			}
			success = true;
			this.devicesMap.put(key, mc);
			if(this.fwHandler != null) {
				deviceData.getDeviceFirmware().addPropertyChangeListener(
						Resource.ChangeListenerType.INTERNAL, fwHandler);
			}
			if(this.actionHandler != null) {
				deviceData.getDeviceAction().addPropertyChangeListener(
						Resource.ChangeListenerType.INTERNAL, actionHandler);
			}
		}
		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Success (" + success + ")");
		
		mc.setbManaged(success);
		return success;
	}

	
	/**
	 * Update the location of the Gateway to IBM Watson IoT Platform.
	 * 
	 * @param latitude	Latitude in decimal degrees using WGS84
	 * @param longitude Longitude in decimal degrees using WGS84
	 * @param elevation	Elevation in meters using WGS84
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)

	 */
	public int updateGatewayLocation(Double latitude, Double longitude, Double elevation) {
		return updateDeviceLocation(getGWTypeId(), getGWDeviceId(), latitude, 
				longitude, elevation, new Date(), null, null);
	}
	
	/**
	 * Update the location of the Device connected via the Gateway to IBM Watson IoT Platform.
	 * 
  	 * @param typeId The device type of the device connected to the Gateway
	 * @param deviceId The deviceId of the device connected to the Gateway
	 * @param latitude	Latitude in decimal degrees using WGS84
	 * @param longitude Longitude in decimal degrees using WGS84
	 * @param elevation	Elevation in meters using WGS84
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)

	 */
	public int updateDeviceLocation(String typeId, String deviceId, 
							Double latitude, Double longitude, Double elevation) {
		return updateDeviceLocation(typeId, deviceId, latitude, 
				longitude, elevation, new Date(), null, null);
	}

	/**
	 * Update the location of the Gateway. This method converts the 
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
	public int updateGatewayLocation(Double latitude, Double longitude, Double elevation, Date measuredDateTime) {
		return updateDeviceLocation(getGWTypeId(), getGWDeviceId(), latitude, 
				longitude, elevation, measuredDateTime, null, null);
	}
	
	/**
	 * Update the location of the Device connected via the Gateway.This method converts the 
	 * date in the required format. The caller just need to pass the date in java.util.Date format
	 * 
  	 * @param typeId The device type of the device connected to the Gateway
	 * @param deviceId The deviceId of the device connected to the Gateway
	 * @param latitude	Latitude in decimal degrees using WGS84
	 * @param longitude Longitude in decimal degrees using WGS84
	 * @param elevation	Elevation in meters using WGS84
	 * @param measuredDateTime When the location information is retrieved
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)

	 */
	public int updateDeviceLocation(String typeId, String deviceId, Double latitude, 
							Double longitude, Double elevation, Date measuredDateTime) {
		
		return updateDeviceLocation(typeId, deviceId, latitude,	
				longitude, elevation, measuredDateTime, null, null);
	}
	
	/**
	 * Update the location of the Gateway. This method converts the 
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
	public int updateGatewayLocation(Double latitude, 
							 Double longitude, 
							 Double elevation, 
							 Date measuredDateTime,
							 Date updatedDateTime,
							 Double accuracy) {
		
		return updateDeviceLocation(getGWTypeId(), getGWDeviceId(), latitude, 
				longitude, elevation, measuredDateTime, updatedDateTime, accuracy);
	}
	
	/**
	 * Update the location of the device connected through the Gateway. This method converts the 
	 * date in the required format. The caller just need to pass the date in java.util.Date format.
	 * 
   	 * @param typeId The device type of the device connected to the Gateway
	 * @param deviceId The deviceId of the device connected to the Gateway
	 * @param latitude	Latitude in decimal degrees using WGS84
	 * @param longitude Longitude in decimal degrees using WGS84
	 * @param elevation	Elevation in meters using WGS84
	 * @param measuredDateTime Date of location measurement
	 * @param updatedDateTime Date of the update to the device information
	 * @param accuracy	Accuracy of the position in meters
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)

	 */
	public int updateDeviceLocation(String typeId,
							 String deviceId,
							 Double latitude, 
							 Double longitude, 
							 Double elevation, 
							 Date measuredDateTime,
							 Date updatedDateTime,
							 Double accuracy) {
		
		final String METHOD = "updateLocation"; 
		
		String key = typeId + ':' + deviceId;
		ManagedGatewayDevice mc = (ManagedGatewayDevice) this.devicesMap.get(key);
		if(mc == null) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, 
					"The device is not a managed device, so can not send the request");
			return -1;
		}

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
		
		if(updatedDateTime != null) {
			utcTime = DateFormatUtils.formatUTC(updatedDateTime, 
					DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
			json.addProperty("updatedDateTime", utcTime);
		}
		
		if(accuracy != null) {
			json.addProperty("accuracy", accuracy);
		}
		
		jsonData.add("d", json);
		
		try {
			JsonObject response = sendAndWait(mc.getDMAgentTopic().getUpdateLocationTopic(),
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
	 * Clear the Gateway ErrorCodes from IBM Watson IoT Platform.
	 * 
	 * @return code indicating whether the clear operation is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int clearGatewayErrorCodes() {
		return clearDeviceErrorCodes(getGWTypeId(), getGWDeviceId());
	}
	
	/**
	 * Clear the Device(connected via the Gateway) ErrorCodes from IBM Watson IoT Platform.
	 * 
 	 * @param typeId The device type of the device connected to the Gateway
	 * @param deviceId The deviceId of the device connected to the Gateway
	 * @return code indicating whether the clear operation is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int clearDeviceErrorCodes(String typeId, String deviceId) {
		final String METHOD = "clearErrorCodes";
		String key = typeId + ':' + deviceId;
		ManagedGatewayDevice mc = (ManagedGatewayDevice) this.devicesMap.get(key);
		if(mc == null) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, 
					"The device is not a managed device, so can not send the request");
			return -1;
		}
		JsonObject jsonData = new JsonObject();
		try {
			JsonObject response = sendAndWait(mc.getDMAgentTopic().getClearDiagErrorCodesTopic(), 
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
	 * Clear the Gateway Logs from IBM Watson IoT Platform.
	 * 
	 * @return code indicating whether the clear operation is successful or not 
	 *        (200 means success, otherwise unsuccessful).
	 */
	public int clearGatewayLogs() {
		return clearDeviceLogs(getGWTypeId(), getGWDeviceId());
	}
	
	/**
	 * Clear the Device(connected through the Gateway) Logs from IBM Watson IoT Platform.
	 * 
	 * @return code indicating whether the clear operation is successful or not 
	 *        (200 means success, otherwise unsuccessful).
	 */
	public int clearDeviceLogs(String typeId, String deviceId) {
		final String METHOD = "clearLogs"; 
		String key = typeId + ':' + deviceId;
		ManagedGatewayDevice mc = (ManagedGatewayDevice) this.devicesMap.get(key);
		if(mc == null) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, 
					"The device is not a managed device, so can not send the request");
			return -1;
		}
		JsonObject jsonData = new JsonObject();
		try {
			JsonObject response = sendAndWait(mc.getDMAgentTopic().getClearDiagLogsTopic(), 
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
	 * Adds the Gateway errorcode to IBM Watson IoT Platform.
	 * 
	 * @param errorCode The "errorCode" is a current device error code that 
	 * needs to be added to the IBM Watson IoT Platform.
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful).
	 */
	public int addGatewayErrorCode(int errorCode) {
		return addDeviceErrorCode(getGWTypeId(), getGWDeviceId(), errorCode);
	}
	
	/**
	 * Adds the Device errorcode to IBM Watson IoT Platform.
	 * 
 	 * @param typeId The device type of the device connected to the Gateway
	 * @param deviceId The deviceId of the device connected to the Gateway
	 * @param errorCode The "errorCode" is a current device error code that 
	 * needs to be added to the IBM Watson IoT Platform.
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int addDeviceErrorCode(String typeId, String deviceId, int errorCode) {
		final String METHOD = "addErrorCode"; 
		String key = typeId + ':' + deviceId;
		ManagedGatewayDevice mc = (ManagedGatewayDevice) this.devicesMap.get(key);
		if(mc == null) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, 
					"The device is not a managed device, so can not send the request");
			return -1;
		}
		
		JsonObject jsonData = new JsonObject();
		JsonObject errorObj = new JsonObject();
		errorObj.addProperty("errorCode", errorCode);
		jsonData.add("d", errorObj);
		
		try {
			JsonObject response = sendAndWait(mc.getDMAgentTopic().getAddErrorCodesTopic(), 
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
	 * Adds a Gateway Log message to the IBM Watson IoT Platform.
	 * 
	 * @param message The Log message that needs to be added to the IBM Watson IoT Platform.
	 * @param timestamp The Log timestamp.
	 * @param severity the {@link com.ibm.iotf.devicemgmt.LogSeverity}.
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful).
	 */
	public int addGatewayLog(String message, Date timestamp, LogSeverity severity) {
		return addDeviceLog(getGWTypeId(), getGWDeviceId(), message, timestamp, severity, null);
	}
	
	/**
	 * Adds a Gateway Log message to the IBM Watson IoT Platform.
	 * 
	 * @param message The Log message that needs to be added to the IBM Watson IoT Platform.
	 * @param timestamp The Log timestamp.
	 * @param severity The {@link com.ibm.iotf.devicemgmt.LogSeverity}.
	 * @param data The optional diagnostic string data - 
	 * 				The library will encode the data in base64 format as required by the Platform. 
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful).
	 */
	public int addGatewayLog(String message, Date timestamp, LogSeverity severity, String data) {
		return addDeviceLog(getGWTypeId(), getGWDeviceId(), message, timestamp, severity, data);
	}
	
	/**
	 * Adds a Device(connected via the Gateway) Log message to the IBM Watson IoT Platform.
	 * 
	 * @param typeId The device type of the device connected to the Gateway.
	 * @param deviceId The deviceId of the device connected to the Gateway.
	 * @param message The Log message that needs to be added to the IBM Watson IoT Platform.
	 * @param timestamp The Log timestamp.
	 * @param severity The {@link com.ibm.iotf.devicemgmt.LogSeverity}.

	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful).
	 */
	public int addDeviceLog(String typeId, 
							String deviceId, 
							String message, 
							Date timestamp, 
							LogSeverity severity) {
		return addDeviceLog(typeId, deviceId, message, timestamp, severity, null);
	}
	
	/**
	 * Adds a Device(connected via the Gateway) Log message to the IBM Watson IoT Platform.
	 * 
	 * This method converts the timestamp in the required format. 
	 * The caller just need to pass the timestamp in java.util.Date format.
	 * 
	 * @param typeId The device type of the device connected to the Gateway.
	 * @param deviceId The deviceId of the device connected to the Gateway.
	 * @param message The Log message that needs to be added to the IBM Watson IoT Platform.
	 * @param timestamp The Log timestamp
	 * @param severity The {@link com.ibm.iotf.devicemgmt.LogSeverity}
	 * @param data The optional diagnostic string data - 
	 * 				The library will encode the data in base64 format as required by the Platform .
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful).
	 */
	public int addDeviceLog(String typeId, 
							String deviceId, 
							String message, 
							Date timestamp, 
							LogSeverity severity, 
							String data) {
		
		final String METHOD = "addDeviceLog"; 
		String key = typeId + ':' + deviceId;
		ManagedGatewayDevice mc = (ManagedGatewayDevice) this.devicesMap.get(key);
		if(mc == null) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, 
					"The device is not a managed device, so can not send the request");
			return -1;
		}

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
			JsonObject response = sendAndWait(mc.getDMAgentTopic().getAddDiagLogTopic(), 
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
	 * Moves the Gateway from managed state to unmanaged state
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
	public boolean sendGatewayUnmanageRequet() throws MqttException {
		return sendDeviceUnmanageRequet(getGWTypeId(), getGWDeviceId());
	}
	
	/**
	 * Moves the device connected via the Gateway from managed state to unmanaged state
	 * 
	 * A device uses this request when it no longer needs to be managed. 
	 * This means IoTF will no longer send new device management requests 
	 * to this device and device management requests from this device will 
	 * be rejected apart from a Manage device request
	 * 
  	 * @param typeId The device type of the device connected to the Gateway
	 * @param deviceId The deviceId of the device connected to the Gateway
	 * 
	 * @return
	 * 		True if the unmanage command is successful
	 * @throws MqttException
	 */
	public boolean sendDeviceUnmanageRequet(String typeId, String deviceId) throws MqttException {
		
		final String METHOD = "unmanage";
		String key = typeId + ':' + deviceId;
		ManagedGatewayDevice mc = (ManagedGatewayDevice) this.devicesMap.remove(key);
		if(mc == null) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, 
					"The device is not a managed device, so can not send the request");
			return false;
		}
		
		boolean success = false;
		String topic = mc.gatewayDMAgentTopic.getUnmanageTopic();

		JsonObject jsonPayload = new JsonObject();
		JsonObject jsonResponse = sendAndWait(topic, jsonPayload, REGISTER_TIMEOUT_VALUE);
		if (jsonResponse != null && jsonResponse.get("rc").getAsInt() == 
				ResponseCode.DM_SUCCESS.getCode()) {
			success = true;	
		}

		DMRequestHandler.clearRequestHandlers(mc);
		
		if(devicesMap.size() == 0) {
			unsubscribe(GATEWAY_RESPONSE_TOPIC);
			terminate();
			terminateHandlers();
			this.reponseSubscription = false;
		}
		
		LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "Success (" + success + ")");
		if(success) {
			mc.setbManaged(false);
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
	 * @throws MqttException
	 */
	private void subscribe(String topic, int qos, IMqttMessageListener listener) throws MqttException {
		final String METHOD = "subscribe";
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic(" + topic + ")");
		if (isConnected()) {
			if (mqttAsyncClient != null) {
				mqttAsyncClient.subscribe(topic, qos, listener);
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
	 * @throws MqttException
	 */
	private void subscribe(String[] topics, int[] qos, IMqttMessageListener[] listeners) throws MqttException {
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
	 * where IBM Watson IoT Platform will send the DM requests</p>
	 *  
	 * @param topic topic to be unsubscribed
	 * @throws MqttException
	 */
	private void unsubscribe(String topic) throws MqttException {
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
	 * @throws MqttException
	 */
	private void unsubscribe(String[] topics) throws MqttException {
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
	 * <p>Publish the Device management response to IBm IBM Watson IoT Platform </p>
	 *  
	 * <p>This method is used by the library to respond to each of the Device Management commands from
	 *  IBM Watson IoT Platform</p>
	 * 
	 * @param topic Topic where the response to be published
	 * @param payload the Payload
	 * @param qos The Quality Of Service
	 * @throws MqttException
	 */
	private void publish(String topic, JsonObject payload, int qos) throws MqttException {
		final String METHOD = "publish3";
		JsonObject jsonPubMsg = new JsonObject();
		jsonPubMsg.addProperty("topic", topic);
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
		publish(topic, message);
	}
	
	/**
	 * <p>Send the message and waits for the response from IBM Watson IoT Platform<p>
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
	 * @param timeout How long to wait for the resonse
	 * @return response in Json format
	 * @throws MqttException
	 */
	private JsonObject sendAndWait(String topic, JsonObject jsonPayload, long timeout) throws MqttException {
		
		final String METHOD = "sendAndWait";
		
		String uuid = UUID.randomUUID().toString();
		jsonPayload.add("reqId", new JsonPrimitive(uuid));

		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic (" + topic + 
				") payload (" + jsonPayload.toString() + ") reqId (" + uuid + ")" );
		
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
					//LoggerUtility.warn(CLASS_NAME, METHOD, "This response is NOT for me reqId:" + jsonResponse.toString() );
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
	 * Disconnects the Gateway and all the devices connected, from IBM Watson IoT Platform
	 */
	@Override
	public void disconnect() {
		Set<String> devices = devicesMap.keySet();
		Iterator<String> itr = devices.iterator();
		while(itr.hasNext()) {
			ManagedGatewayDevice mc = (ManagedGatewayDevice) this.devicesMap.get(itr.next());
			if(mc != null && mc.bManaged == true) {
				try {
					this.sendDeviceUnmanageRequet(mc.getTypeId(), mc.getTypeId());
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
		}
		super.disconnect();
	}

	/**
	 * This method reconnects when the connection is lost due to n/w interruption and this method 
	 * is called only when the connection is established originally by the library code.
	 * 
	 * This method does the following activities,
	 * 1. Calculates the lifetime remaining
	 * 2. Sends the manage request for those devices/Gateway which are in managed state before disconnecting
	 *  
	 */
	@Override
	protected void reconnect() {
		String METHOD = "reconnect";
		
		IMqttDeliveryToken[] tokens = this.mqttAsyncClient.getPendingDeliveryTokens();
		super.connect();
		
		this.reponseSubscription = false;
		if(this.isConnected()) {
			// Iterate through all the devices and send a manage request again
			Set<String> devices = devicesMap.keySet();
			Iterator<String> itr = devices.iterator();
			while(itr.hasNext()) {
				ManagedGatewayDevice mc = (ManagedGatewayDevice) this.devicesMap.get(itr.next());
				if(mc != null && mc.bManaged == true) {
					try {
						long lifetime = 0;
						if(mc.getDormantTime() != null) {
							Date currentTime = new Date();
							lifetime = (mc.getDormantTime().getTime() - currentTime.getTime()) / 1000;
							if(lifetime < 0) {
								lifetime = 0;
							}
						}
						LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, "lifetime (" + lifetime + ")");
						this.sendDeviceManageRequest(mc.getTypeId(), mc.getDeviceId(), 
								lifetime, mc.isFirmwareActions(), mc.isDeviceActions());
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
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
		}
	}
	
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		final String METHOD = "messageArrived";
		
		Matcher matcher = GATEWAY_RESPONSE_PATTERN.matcher(topic);
		if (matcher.matches()) {
			LoggerUtility.log(Level.FINE, CLASS_NAME, METHOD, 
					"Received response from IBM Watson IoT Platform, topic (" + topic + ")");
			
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

	/**
	 * <p>Adds a firmware handler for both Gateway and devices connected through the gateway, 
	 * that is of type {@link com.ibm.iotf.devicemgmt.DeviceFirmwareHandler}</p>
	 * 
	 * <p>If the Gateway/device supports firmware update, the abstract class 
	 * {@link com.ibm.iotf.devicemgmt.DeviceFirmwareHandler} should be extended by the Gateway code.
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
		setFirmwareHandlerForAllDevices();
		fwHandler.start();
	}
	
	/**
	 * Add the same handler to all those devices which are already in managed state
	 */
	private void setFirmwareHandlerForAllDevices() {
		Set<String> devices = devicesMap.keySet();
		Iterator<String> itr = devices.iterator();
		while(itr.hasNext()) {
			ManagedGatewayDevice mc = (ManagedGatewayDevice) this.devicesMap.get(itr.next());
			DeviceData deviceData = mc.getDeviceData();
			deviceData.getDeviceFirmware().addPropertyChangeListener(Resource.ChangeListenerType.INTERNAL, fwHandler);
		}
	}

	/**
	 * <p>Adds a device action handler which is of type {@link com.ibm.iotf.devicemgmt.DeviceActionHandler}</p>
	 * 
	 * <p>If the gateway or device supports device actions like reboot and factory reset,
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
		setDeviceHandlerForAllDevices();
		actionHandler.start();
	}
	
	/**
	 * Add the same handler to all those devices which are already in managed state
	 */
	private void setDeviceHandlerForAllDevices() {
		Set<String> devices = devicesMap.keySet();
		Iterator<String> itr = devices.iterator();
		while(itr.hasNext()) {
			ManagedGatewayDevice mc = (ManagedGatewayDevice) this.devicesMap.get(itr.next());
			DeviceData deviceData = mc.getDeviceData();
			deviceData.getDeviceAction().addPropertyChangeListener(Resource.ChangeListenerType.INTERNAL, actionHandler);
		}
	}

	/**
	 * We are disconnecting, so terminate all handlers.
	 */
	private void terminateHandlers() {
		if(this.fwHandler != null) {
			fwHandler.terminate();
			fwHandler = null;
		}
		
		if(this.actionHandler != null) {
			actionHandler.terminate();
			actionHandler = null;
		}
		
	}

}
