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
package com.ibm.wiotp.sdk;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.util.Debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * A client that handles connections with the IBM Watson IoT Platform. <br>
 * This is an abstract class which has to be extended
 */
public abstract class AbstractClient {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractClient.class);

	public static final int DEFAULT_MAX_CONNECT_ATTEMPTS = 10;
	public static final long DEFAULT_ACTION_TIMEOUT = 10 * 1000L;
	public static final int DEFAULT_MAX_INFLIGHT_MESSAGES = 100;
	public static final int DEFAULT_MESSAGING_QOS = 1;
	public static final int DEFAULT_DISCONNECTED_BUFFER_SIZE = 5000;

	protected static final String CLIENT_ID_DELIMITER = ":";

	protected volatile boolean disconnectRequested = false;

	/* Wait for 1 second after each attempt for the first 10 attempts */
	private static final long RATE_0 = TimeUnit.SECONDS.toMillis(1);

	/*
	 * After 5 attempts throttle the rate of connection attempts to 1 per 10 second
	 */
	private static final int THROTTLE_1 = 5;
	private static final long RATE_1 = TimeUnit.SECONDS.toMillis(10);

	/* After 10 attempts throttle the rate of connection attempts to 1 per minute */
	private static final int THROTTLE_2 = 10;
	private static final long RATE_2 = TimeUnit.MINUTES.toMillis(1);

	/*
	 * After 20 attempts throttle the rate of connection attempts to 1 per 5 minutes
	 */
	private static final int THROTTLE_3 = 20;
	private static final long RATE_3 = TimeUnit.MINUTES.toMillis(5);

	protected static final Gson gson = new Gson();

	protected AbstractConfig config;

	protected MqttAsyncClient mqttAsyncClient = null;
	protected MqttConnectOptions mqttClientOptions;
	protected MqttCallback mqttCallback;

	// Supported only for DM ManagedClient
	protected MqttClient mqttClient = null;
	protected MemoryPersistence persistence = null;

	/**
	 * Note that this class does not have a default constructor <br>
	 * 
	 * @param config Configuration object for the client
	 * 
	 */

	public AbstractClient(AbstractConfig config) {
		this.config = config;
	}

	/**
	 * <p>
	 * Connects the device to IBM Watson IoT Platform and retries when there is an
	 * exception based on the value set in retry parameter. <br>
	 * 
	 * This method does not retry when the following exceptions occur.
	 * </p>
	 * 
	 * <ul class="simple">
	 * <li>MqttSecurityException - One or more credentials are wrong
	 * <li>UnKnownHostException - Host doesn't exist. For example, a wrong
	 * organization name is used to connect.
	 * </ul>
	 * 
	 * @throws MqttException            One or more credentials are wrong
	 * @throws NoSuchAlgorithmException Problems with TLS
	 * @throws KeyManagementException   Problems with TLS
	 **/
	public void connect() throws MqttException, KeyManagementException, NoSuchAlgorithmException {
		// return if its already connected
		if (mqttAsyncClient != null && mqttAsyncClient.isConnected()) {
			LOG.warn("Client " + config.getClientId() + " is already connected.");
			return;
		}
		boolean tryAgain = true;
		int connectAttempts = 0;
		// clear the disconnect state when the user connects the client to Watson IoT
		// Platform
		disconnectRequested = false;

		while (tryAgain && disconnectRequested == false) {
			connectAttempts++;
			LOG.debug("Connecting client " + config.getClientId() + " to " + mqttAsyncClient.getServerURI()
					+ " (attempt #" + connectAttempts + ")");

			try {
				MqttConnectOptions options = config.getMqttConnectOptions();
				mqttAsyncClient.connect(options).waitForCompletion(DEFAULT_ACTION_TIMEOUT);
			} catch (MqttSecurityException e) {
				LOG.warn("Connecting to Watson IoT Platform failed - one or more connection parameters are wrong !!!",
						e);
				if (connectAttempts > DEFAULT_MAX_CONNECT_ATTEMPTS) {
					throw e;
				}
			} catch (MqttException e) {
				if (connectAttempts > DEFAULT_MAX_CONNECT_ATTEMPTS) {
					LOG.warn("Connecting to Watson IoT Platform failed", e);
					// We must give up as the host doesn't exist.
					throw e;
				}
				e.printStackTrace();
				/*
				 * workaround eclipse mqtt paho connect loop problem (resets connection state in
				 * paho client)
				 */
				mqttAsyncClient.disconnectForcibly(0, 0, false);
			}

			if (mqttAsyncClient.isConnected()) {
				LOG.info("Successfully connected to IBM Watson IoT Platform");
				tryAgain = false;
			} else {
				waitBeforeNextConnectAttempt(connectAttempts);
			}
		}
	}

	/**
	 * configureMqtt() is called when the User does not provide an Organization
	 * value and intends to connect to Watson IoT Platform using the QUICKSTART
	 * mode. This type of connection is In-secure in nature and is usually done over
	 * the 1883 Port Number.
	 * 
	 * @param callback The handler for MQTT callbacks
	 * 
	 * @throws NoSuchAlgorithmException Problems with TLS
	 * @throws KeyManagementException   Problems with TLS
	 */
	protected void configureMqttClient(MqttCallbackExtended callback)
			throws KeyManagementException, NoSuchAlgorithmException {
		mqttAsyncClient = null;
		mqttCallback = callback;

		try {
			persistence = new MemoryPersistence();
			mqttAsyncClient = new MqttAsyncClient(config.getMqttServerURI(), config.getClientId(), persistence);
			mqttAsyncClient.setCallback(mqttCallback);
			DisconnectedBufferOptions disconnectedOpts = new DisconnectedBufferOptions();
			disconnectedOpts.setBufferEnabled(true);
			disconnectedOpts.setBufferSize(DEFAULT_DISCONNECTED_BUFFER_SIZE);
			mqttAsyncClient.setBufferOpts(disconnectedOpts);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sleep for a variable period of time between connect attempts.
	 * 
	 * @param attempts How many times have we tried (and failed) to connect
	 */
	private void waitBeforeNextConnectAttempt(final int attempts) {
		// Log when throttle boundaries are reached
		if (attempts == THROTTLE_3) {
			LOG.warn(String.valueOf(attempts) + " consecutive failed attempts to connect.  Retry delay increased to "
					+ String.valueOf(RATE_3) + "ms");
		} else if (attempts == THROTTLE_2) {
			LOG.warn(String.valueOf(attempts) + " consecutive failed attempts to connect.  Retry delay increased to "
					+ String.valueOf(RATE_2) + "ms");
		} else if (attempts == THROTTLE_1) {
			LOG.info(String.valueOf(attempts) + " consecutive failed attempts to connect.  Retry delay set to "
					+ String.valueOf(RATE_1) + "ms");
		}

		try {
			long delay = RATE_0;
			if (attempts >= THROTTLE_3) {
				delay = RATE_3;
			} else if (attempts >= THROTTLE_2) {
				delay = RATE_2;
			} else if (attempts >= THROTTLE_1) {
				delay = RATE_1;
			}
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Disconnect the device from the IBM Watson IoT Platform
	 */
	public void disconnect() {
		try {
			this.disconnectRequested = true;
			if (mqttAsyncClient != null) {
				LOG.debug("Disconnecting from IBM Watson IoT Platform");
				mqttAsyncClient.disconnect().waitForCompletion(DEFAULT_ACTION_TIMEOUT);
				LOG.info("Successfully disconnected from IBM Watson IoT Platform");
			}
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close and free all MQTT client resources
	 * 
	 * @throws MqttException Thrown if an error occurs
	 */
	public void close() throws MqttException {
		LOG.debug("Closing MQTT client (" + config.getClientId() + ")");
		if (mqttAsyncClient != null) {
			mqttAsyncClient.close(true);
			mqttAsyncClient = null;
			LOG.info("Closed MQTT client (" + config.getClientId() + ")");
		}
	}

	/**
	 * Determine whether this device is currently connected to the IBM Watson
	 * Internet of Things Platform.
	 * 
	 * @return Whether the device is connected to the IBM Watson IoT Platform
	 */
	public boolean isConnected() {
		boolean connected = false;
		if (mqttAsyncClient != null) {
			connected = mqttAsyncClient.isConnected();
		} else if (mqttClient != null) {
			connected = mqttClient.isConnected();
		}
		LOG.debug("Connected(" + connected + ")");
		return connected;
	}

	public AbstractConfig getConfig() {
		return config;
	}

	/**
	 * Provides a human readable String representation of this Device, including the
	 * number of messages sent and the current connect status.
	 * 
	 * @return String representation of the Device.
	 */
	public String toString() {
		return "[" + config.getClientId() + "] Connected = " + String.valueOf(isConnected());
	}

	public Debug getPahoDebug() {
		return this.mqttAsyncClient.getDebug();
	}

}
