/*
 * IBM Confidential
 * OCO Source Materials
 * 5725-Q07
 * (c) Copyright IBM Corp. 2014
 * The source code for this program is not published or otherwise divested of
 * its trade secrets, irrespective of what has been deposited with the U.S.
 * Copyright Office.
 */
package com.ibm.iotcloud.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import com.google.gson.Gson;

/**
 * A client that handles connections with the IBM Internet of Things Cloud
 * Service.
 */
public abstract class AbstractClient {
	
	private static final String CLASS_NAME = AbstractClient.class.getName();
	private static final Logger LOG = Logger.getLogger(CLASS_NAME);
	
	protected static final String CLIENT_ID_DELIMITER = ":";
	
	protected static final String QUICKSTART_HOSTNAME = "messaging.quickstart.internetofthings.ibmcloud.com";
	protected static final int QUICKSTART_PORT = 1883;
	protected static final String QUICKSTART_ACCOUNT = "quickstart";
	
	protected static final String HOSTNAME = "messaging.internetofthings.ibmcloud.com";
	protected static final int PORT = 1883;
	
	/* For 1 minute wait for 1 second after each attempt */
	private static final long RATE_0 = TimeUnit.SECONDS.toMillis(1);
	
	/* After 1 minute throttle the rate of connection attempts to 1 per 10 second */
	private static final long THROTTLE_1 = TimeUnit.MINUTES.toMillis(1);
	private static final long RATE_1 = TimeUnit.SECONDS.toMillis(10);
	
	/* After 10 minutes throttle the rate of connection attempts to 1 per minute */
	private static final long THROTTLE_2 = TimeUnit.MINUTES.toMillis(10);
	private static final long RATE_2 = TimeUnit.MINUTES.toMillis(1);
	
	/* After 1 hour throttle the rate of connection attempts to 1 per 10 minutes */
	private static final long THROTTLE_3 = TimeUnit.HOURS.toMillis(1);
	private static final long RATE_3 = TimeUnit.MINUTES.toMillis(10);
	
	protected final Gson gson = new Gson();
	
	/**
	 * A formatter for ISO 8601 compliant timestamps.
	 */
	protected final DateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	protected String serverURI;
	protected String clientId;
	protected String clientUsername;
	protected String clientPassword;
	
	protected int messageCount = 0;
	
	protected MqttClient client;
	
	
	/**
	 * Create the Paho MQTT Client that will underpin the Device client.
	 */
	protected void createClient(MqttCallback callback) {
		System.out.println("Server URI      = " + serverURI);
		System.out.println("Client ID       = " + clientId);
		System.out.println("Client Username = " + clientUsername);
		System.out.println("Client Password = " + clientPassword);
		
		client = null;
		try {
			client = new MqttClient(serverURI, clientId, null);
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
		if (client != null) {
			client.setCallback(callback);
			//connect();
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
			connectAttempts++;
			
			LOG.fine("Connecting to " + serverURI + " (attempt #" + connectAttempts + ")...");
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
	 * @param timeElapsed
	 *            How long (in milliseconds) since the first connection attempt was made.
	 */
	private void waitBeforeNextConnectAttempt(final long timeElapsed) {
		try {
			if (timeElapsed > THROTTLE_3) {
				Thread.sleep(RATE_3);
			} else if (timeElapsed > THROTTLE_2) {
				Thread.sleep(RATE_2);
			} else if (timeElapsed > THROTTLE_1) {
				Thread.sleep(RATE_1);
			} else {
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
		return "[" + clientId + "] " + messageCount + " messages sent - Connected = " + String.valueOf(isConnected());
	}
	
	public static Properties parsePropertiesFile(File propertiesFile) {
		Properties clientProperties = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(propertiesFile);
			clientProperties.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return clientProperties;
		} catch (IOException e) {
			e.printStackTrace();
			return clientProperties;
		}
		return clientProperties;
	}

	
}
