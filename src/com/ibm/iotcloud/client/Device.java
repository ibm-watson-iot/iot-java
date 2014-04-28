/*
 * Licensed Materials - Property of IBM  
 * NNNN-NNN
 * © Copyright IBM Corp. 2014
 * US Government Users Restricted Rights - Use, duplication, or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 */
package com.ibm.iotcloud.client;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A client that handles connections with the IBM Internet of Things Cloud
 * Service and allows the sending and receiving of messages.
 */
public class Device {

	private static final String CLASS_NAME = Device.class.getName();
	private static final Logger LOG = Logger.getLogger(CLASS_NAME);

	
	
	private static final String CLIENT_ID_DELIMITER = ":";
	
	private static final String QUICKSTART_HOSTNAME = "messaging.quickstart.internetofthings.ibmcloud.com";
	private static final int QUICKSTART_PORT = 1883;
	private static final String QUICKSTART_ACCOUNT = "quickstart";

	private static final String HOSTNAME = "messaging.internetofthings.ibmcloud.com";
	private static final int PORT = 1883;

	private final Gson gson = new Gson();
	
	/**
	 * A formatter for ISO 8601 compliant timestamps.
	 */
	private final DateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private final String serverURI;
	private final String deviceId;
	private final String accountId;
	private final String clientId;
	private final String clientUsername;
	private final String clientPassword;
	
	private int messageCount = 0;

	private MqttClient client;

	public Device(String id) throws MqttException {
		deviceId = id;
		accountId = QUICKSTART_ACCOUNT;
		serverURI = "tcp://" + QUICKSTART_HOSTNAME + ":" + QUICKSTART_PORT;
		clientId =  accountId + CLIENT_ID_DELIMITER + deviceId;
		clientUsername = null;
		clientPassword = null;
		
		client = new MqttClient(serverURI, clientId, null);
		client.setCallback(new Callback());
		
		connect();
	}

	public Device(String id, String account, String username, String password) {
		deviceId = id;
		accountId = account;
		serverURI = "tcp://" + HOSTNAME + ":" + PORT;
		clientId =  accountId + CLIENT_ID_DELIMITER + deviceId;
		clientUsername = username;
		clientPassword = password;

		System.out.println("Client ID = " + clientId);
		client = null;
		try {
			client = new MqttClient(serverURI, clientId, null);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (client != null) {
			client.setCallback(new Callback());
			connect();
		}
	}

	public void connect() {
		MqttConnectOptions connectOptions = new MqttConnectOptions();
		if (clientUsername != null) {
			connectOptions.setUserName(clientUsername);
		}
		if (clientPassword != null) {
			connectOptions.setPassword(clientPassword.toCharArray());
		}
		
		LOG.fine("Connecting to the IBM Internet of Things Cloud ...");
		if (clientUsername != null) {
			LOG.fine(" * Username: " + connectOptions.getUserName());
		}
		if (clientPassword != null) {
			LOG.fine(" * Passowrd: " + String.valueOf(connectOptions.getPassword()));
		}
		try {
			client.connect(connectOptions);
		} catch (MqttSecurityException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		LOG.info("Successfully connected to the IBM Internet of Things Cloud");
	}
	
	public void disconnect() {
		LOG.fine("Disconnecting from the IBM Internet of Things Cloud ...");
		try {
			client.disconnect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		LOG.info("Successfully disconnected from from the IBM Internet of Things Cloud");
	}

	public boolean send(String dataset, Object data) {
		if (!isConnected()) {
			return false;
		}
		JsonObject payload = new JsonObject();

		String timestamp = ISO8601_DATE_FORMAT.format(new Date());
		payload.addProperty("ts", timestamp);
 
		JsonElement dataElement = gson.toJsonTree(data);
		payload.add("d", dataElement);

		String topic = "iot-1/d/" + deviceId + "/evt/" + dataset + "/json";

		LOG.fine("Topic   = " + topic);
		LOG.fine("Payload = " + payload.toString());

		MqttMessage msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		msg.setQos(0);
		msg.setRetained(false);
		
		try {
			client.publish(topic, msg);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean isConnected() {
		return client.isConnected();
	}

	public String toString() {
		return "[" + clientId + "] " + messageCount + " messages sent - Connected = "
				+ String.valueOf(isConnected());
	}

	private class Callback implements MqttCallback {

		public void connectionLost(Throwable e) {
			LOG.info("Connection lost: " + e.getMessage());
		}

		public void deliveryComplete(IMqttDeliveryToken token) {
			LOG.fine("Delivery Complete!");
			messageCount++;
		}
		
		public void messageArrived(String topic, MqttMessage msg)
				throws Exception {
		}
	}

}
