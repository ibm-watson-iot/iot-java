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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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
 * Service.
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
	
	/* For 1 minute wait for 1 second after each attempt */
	private static final long RATE_0 = TimeUnit.SECONDS.toMillis(1);

	/* After 1 minute throttle the rate of connection attempts to 1 per 10 second*/
	private static final long THROTTLE_1 = TimeUnit.MINUTES.toMillis(1); 
	private static final long RATE_1 = TimeUnit.SECONDS.toMillis(10);
	
	/* After 10 minutes throttle the rate of connection attempts to 1 per minute*/
	private static final long THROTTLE_2 = TimeUnit.MINUTES.toMillis(10);
	private static final long RATE_2 = TimeUnit.MINUTES.toMillis(1);
	
	/* After 1 hour throttle the rate of connection attempts to 1 per 10 minutes*/
	private static final long THROTTLE_3 = TimeUnit.HOURS.toMillis(1);
	private static final long RATE_3 = TimeUnit.MINUTES.toMillis(10);

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

	
	/**
	 * Create a device client for the IBM Internet of Things Cloud service.  Connecting to 
	 * the QuickStart service.
	 * @param id The device ID for this device
	 * @throws MqttException
	 */
	public Device(String id) {
		deviceId = id;
		accountId = QUICKSTART_ACCOUNT;
		serverURI = "tcp://" + QUICKSTART_HOSTNAME + ":" + QUICKSTART_PORT;
		clientId =  accountId + CLIENT_ID_DELIMITER + deviceId;
		clientUsername = null;
		clientPassword = null;
		
		createClient();
	}

	
	/**
	 * Create a device client for the IBM Internet of Things Cloud service.  Connecting to 
	 * a specific account on the service.
	 *  
	 * @param id The device ID for this device
	 * @param account Your account ID on the IBM Internet of Things Cloud
	 * @param username Your device connection credentials' username
	 * @param password Your device connection credentials' password
	 */
	public Device(String id, String account, String username, String password) {
		deviceId = id;
		accountId = account;
		serverURI = "tcp://" + HOSTNAME + ":" + PORT;
		clientId =  accountId + CLIENT_ID_DELIMITER + deviceId;
		clientUsername = username;
		clientPassword = password;
		
		createClient();
	}
	
	
	/**
	 * Create the Paho MQTT Client that will underpin the Device client.
	 */
	private void createClient() {
		System.out.println("Client ID = " + clientId);
		client = null;
		try {
			client = new MqttClient(serverURI, clientId, null);
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
		if (client != null) {
			client.setCallback(new Callback());
			connect();
		}		
	}

	
	/**
	 * Connect to the IBM Internet of Things Cloud service.
	 */
	public void connect() {
		boolean tryAgain = true;
		int connectAttempts = 0;
		Date startedConnectAttempt = new Date();

		MqttConnectOptions connectOptions = new MqttConnectOptions();
		if (clientUsername != null) {
			connectOptions.setUserName(clientUsername);
		}
		if (clientPassword != null) {
			connectOptions.setPassword(clientPassword.toCharArray());
		}
		

		while (tryAgain) {
			connectAttempts ++;
			
			LOG.fine("Connecting to the IBM Internet of Things Cloud (attempt #" + connectAttempts + ")...");
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

			Date now = new Date();
			long timeElapsed = startedConnectAttempt.getTime() - now.getTime();
			
			if (client.isConnected()) {
				LOG.info("Successfully connected to the IBM Internet of Things Cloud");
				if (LOG.isLoggable(Level.FINEST)) {
					long second = (timeElapsed / 1000) % 60;
					long minute = (timeElapsed / (1000 * 60)) % 60;
					long hour = (timeElapsed / (1000 * 60 * 60)) % 24;

					String time = String.format("%02d:%02d:%02d", hour, minute, second);
					LOG.finest(" * Connection attempts: " + connectAttempts);
					LOG.finest(" * Time to connect: " + time);
				}
				tryAgain = false;
			} else {
				waitBeforeNextConnectAttempt(timeElapsed);
			}
		}
	}
	
	/**
	 * Sleep for a variable period of time between connect attempts.
	 * 
	 * @param timeElapsed How long (in milliseconds) since the first connection attempt was made.
	 */
	private void waitBeforeNextConnectAttempt(final long timeElapsed) {
		try {
			if (timeElapsed > THROTTLE_3) {
				Thread.sleep(RATE_3);
			}
			else if (timeElapsed > THROTTLE_2) {
				Thread.sleep(RATE_2);
			}
			else if (timeElapsed > THROTTLE_1) {
				Thread.sleep(RATE_1);
			}
			else {
				Thread.sleep(RATE_0);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Disconnect the device from the IBM Internet of Things Cloud Service
	 */
	public void disconnect() {
		LOG.fine("Disconnecting from the IBM Internet of Things Cloud ...");
		try {
			client.disconnect();
			LOG.info("Successfully disconnected from from the IBM Internet of Things Cloud");
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Publish data to the IBM Internet of Things Cloud service.  Note that data is published 
	 * at Quality of Service (QoS) 0, which means that a successful send does not guarantee
	 * receipt even if the publich is successful.
	 *  
	 * @param dataset Name of the dataset under which to publish the data
	 * @param data Object to be added to the payload as the dataset
	 * @return Whether the send was successful.
	 */
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
			return false;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	
	/**
	 * Determine whether this device is currently connected to the IBM Internet 
	 * of Things Cloud service.
	 * 
	 * @return Whether the device is connected to the IBM Internet of Things Cloud
	 */
	public boolean isConnected() {
		return client.isConnected();
	}

	
	/**
	 * Provides a human readable String representation of this Device, including the number 
	 * of messages sent and the current connect status.
	 * 
	 * @Return String representation of the Device.
	 */
	public String toString() {
		return "[" + clientId + "] " + messageCount + " messages sent - Connected = "
				+ String.valueOf(isConnected());
	}

	
	private class Callback implements MqttCallback {
		/**
		 * If we lose connection trigger the connect logic to attempt to 
		 * reconnect to the IBM Internet of Things Cloud.
		 */
		public void connectionLost(Throwable e) {
			LOG.info("Connection lost: " + e.getMessage());
			connect();
		}

		/**
		 * A completed deliver does not guarantee that the message is recieved by the service
		 * because devices send messages with Quality of Service (QoS) 0.  The message count 
		 * represents the number of messages that were sent by the device without an error on 
		 * from the perspective of the device.
		 */
		public void deliveryComplete(IMqttDeliveryToken token) {
			LOG.fine("Delivery Complete!");
			messageCount++;
		}
		
		/**
		 * The Device client does not currently support subscriptions.
		 */
		public void messageArrived(String topic, MqttMessage msg)
				throws Exception {
		}
	}

}
