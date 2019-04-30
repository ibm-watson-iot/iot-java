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
package com.ibm.iotf.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.net.util.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
//import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.util.LoggerUtility;

/**
 * A client that handles connections with the IBM Watson IoT Platform. <br>
 * This is an abstract class which has to be extended
 */
public abstract class AbstractClient {
	
	private static final String CLASS_NAME = AbstractClient.class.getName();
	private static final String QUICK_START = "quickstart";
	private static final int DEFAULT_MAX_CONNECT_ATTEMPTS = 10;
	private static final long DEFAULT_ACTION_TIMEOUT = 5 * 1000L;
	private static final int DEFAULT_MAX_INFLIGHT_MESSAGES = 100;
	private static final int DEFAULT_MESSAGING_QOS = 1;
	
	private static final String SERVER_MESSAGING_PEM = "messaging.pem";
	
	protected static final String CLIENT_ID_DELIMITER = ":";
	
	//protected static final String DOMAIN = "messaging.staging.internetofthings.ibmcloud.com";
	public static final String DEFAULT_DOMAIN = "internetofthings.ibmcloud.com";
	protected static final String MESSAGING = "messaging";
	protected static final int MQTTS_PORT = 8883;
	protected static final int WSS_PORT = 443;
	
	protected static final int MQTT_PORT = 1883;
	protected static final int WS_PORT = 1883;

	protected volatile boolean disconnectRequested = false;
	
	/* Wait for 1 second after each attempt for the first 10 attempts*/
	private static final long RATE_0 = TimeUnit.SECONDS.toMillis(1);
	
	/* After 5 attempts throttle the rate of connection attempts to 1 per 10 second */
	private static final int THROTTLE_1 = 5;
	private static final long RATE_1 = TimeUnit.SECONDS.toMillis(10);
	
	/* After 10 attempts throttle the rate of connection attempts to 1 per minute */
	private static final int THROTTLE_2 = 10;
	private static final long RATE_2 = TimeUnit.MINUTES.toMillis(1);
	
	/* After 20 attempts throttle the rate of connection attempts to 1 per 5 minutes */
	private static final int THROTTLE_3 = 20;
	private static final long RATE_3 = TimeUnit.MINUTES.toMillis(5);
	
	protected static final Gson gson = new Gson();
	
	/**
	 * A formatter for ISO 8601 compliant timestamps.
	 */
	protected static final DateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	protected Properties options;
	protected String clientId;
	protected String clientUsername;
	protected String clientPassword;
	protected String serverURI;
	
	protected int messageCount = 0;
	
	protected MqttAsyncClient mqttAsyncClient = null;
	//private static final MemoryPersistence DATA_STORE = new MemoryPersistence();
	protected MqttConnectOptions mqttClientOptions;
	protected MqttCallback mqttCallback;
	protected int keepAliveInterval = -1;  // default
	
	// Supported only for DM ManagedClient
	protected MqttClient mqttClient = null;
	protected MemoryPersistence persistence = null;
	
	protected static final boolean newFormat;
	
	static {
		newFormat = Boolean.parseBoolean(System.getProperty("com.ibm.iotf.enableCustomFormat", "true"));
	}

	/**
	 * Note that this class does not have a default constructor <br>
	 * @param options
	 * 			Properties object which contains different artifacts such as auth-key
	 * 
	 */		
	
	public AbstractClient(Properties options) {
		this.options = options;
	}
	
	/**
	 * This constructor allows external user to pass the existing MqttAsyncClient 
	 * @param mqttAsyncClient the MQTTAsyncClient that has the connectivity parameters set
	 */
	protected AbstractClient(MqttAsyncClient mqttAsyncClient) {
		this.mqttAsyncClient = mqttAsyncClient;
	}

	/**
	 * This constructor allows external user to pass the existing MqttClient 
	 * @param mqttClient the MQTTClient that has the connectivity parameters set
	 */
	protected AbstractClient(MqttClient mqttClient) {
		this.mqttClient = mqttClient;
	}
	
	/**
	 * Create the Paho MQTT Client that will underpin the Device client.
	 * @param callback
	 * 			MqttCallback 
	 * @see <a href="http://www.eclipse.org/paho/files/javadoc/index.html">Paho Client Library</a> 
	 * 
	 */	

	protected void createClient(MqttCallback callback) {
		LoggerUtility.info(CLASS_NAME, "createClient", "Org ID    = " + getOrgId() +
				"\n         Client ID    = " + clientId);
		this.mqttAsyncClient = null;
		this.mqttClientOptions = new MqttConnectOptions();
		this.mqttCallback = callback;
	}
	
	/**
	 * <p>Connects the device to IBM Watson IoT Platform and retries when there is an exception 
	 * based on the value set in retry parameter. <br>
	 * 
	 * This method does not retry when the following exceptions occur.</p>
	 * 
	 * <ul class="simple">
	 *  <li> MqttSecurityException - One or more credentials are wrong
	 * 	<li>UnKnownHostException - Host doesn't exist. For example, a wrong organization name is used to connect.
	 * </ul>
	 * 
	 * @param numberOfRetryAttempts - How many number of times to retry when there is a failure in connecting to Watson
	 * IoT Platform.
	 * @throws MqttException see above
	 **/
	public void connect(int numberOfRetryAttempts) throws MqttException {
		final String METHOD = "connect";
		// return if its already connected
		if(mqttAsyncClient != null && mqttAsyncClient.isConnected()) {
			LoggerUtility.log(Level.WARNING, CLASS_NAME, METHOD, "Client " + this.clientId + " is already connected.");
			return;
		}
		boolean tryAgain = true;
		int connectAttempts = 0;
		// clear the disconnect state when the user connects the client to Watson IoT Platform
		disconnectRequested = false;  
		String userCertificate = trimedValue(options.getProperty("Use-Secure-Certificate"));
		
		if (getOrgId() == QUICK_START) {
			configureMqtt();
		}else if ((getOrgId() != QUICK_START) && (userCertificate != null && userCertificate.equalsIgnoreCase("True"))){
				LoggerUtility.info(CLASS_NAME, METHOD, "Initiating Certificate based authentication");
				connectUsingCertificate();
				if (isAutomaticReconnect()) {
					DisconnectedBufferOptions disconnectedOpts = new DisconnectedBufferOptions();
					disconnectedOpts.setBufferEnabled(true);
					disconnectedOpts.setBufferSize(getDisconnectedBufferSize());
					mqttAsyncClient.setBufferOpts(disconnectedOpts);
				}
		} else {
				LoggerUtility.info(CLASS_NAME, METHOD, "Initiating Token based authentication");
				connectUsingToken();
			if (isAutomaticReconnect()) {
				DisconnectedBufferOptions disconnectedOpts = new DisconnectedBufferOptions();
				disconnectedOpts.setBufferEnabled(true);
				disconnectedOpts.setBufferSize(getDisconnectedBufferSize());
				mqttAsyncClient.setBufferOpts(disconnectedOpts);
			}
		}
		
		while (tryAgain && disconnectRequested == false) {
			connectAttempts++;
			
			LoggerUtility.info(CLASS_NAME, METHOD, "Connecting client "+ this.clientId + " to " + mqttAsyncClient.getServerURI() + 
					" (attempt #" + connectAttempts + ")...");
			
			try {
				mqttAsyncClient.connect(mqttClientOptions).waitForCompletion(getActionTimeout());
			} catch (MqttSecurityException e) {
				System.err.println("Looks like one or more connection parameters are wrong !!!");
				LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Connecting to Watson IoT Platform failed - " +
						"one or more connection parameters are wrong !!!", e);
				if (connectAttempts > numberOfRetryAttempts) {
					throw e;
				}
				
			} catch (MqttException e) {
				if(connectAttempts > numberOfRetryAttempts) {
					LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Connecting to Watson IoT Platform failed", e);
	                // We must give up as the host doesn't exist.
	                throw e;
	            }
				e.printStackTrace();
			}
			
			if (mqttAsyncClient.isConnected()) {
				LoggerUtility.info(CLASS_NAME, METHOD, mqttAsyncClient.getClientId() 
						+ " successfully connected to the IBM Watson IoT Platform");
				
				if (LoggerUtility.isLoggable(Level.FINEST)) {
					LoggerUtility.log(Level.FINEST, CLASS_NAME, METHOD, 
							" * Connection attempts: " + connectAttempts);
				}
				
				tryAgain = false;
			} else {
				waitBeforeNextConnectAttempt(connectAttempts);
			}
		}
	}
	
	/**
	 * <p>Connects the application to IBM Watson IoT Platform and retries when there is an exception 
	 * based on the value set in retry parameter. <br>
	 * 
	 * This method does not retry when the following exceptions occur.</p>
	 * 
	 * <ul class="simple">
	 *  <li> MqttSecurityException - One or more credentials are wrong
	 * 	<li>UnKnownHostException - Host doesn't exist. For example, a wrong organization name is used to connect.
	 * </ul>
	 * 
	 * @param autoRetry - tells whether to retry the connection when the connection attempt fails.
	 * @throws MqttException refer above
	 **/
	public void connect(boolean autoRetry) throws MqttException {
		if(autoRetry == false) {
			connect(0);
		} else {
			connect(getMaxConnectAttempts());
		}
	}
	
	/**
	 * configureMqtt() is called when the User does not provide an Organization value and intends
	 * to connect to Watson IoT Platform using the QUICKSTART mode. This type of connection is 
	 * In-secure in nature and is usually done over the 1883 Port Number.
	 */
	private void configureMqtt() {
		String protocol = null;
		int port = getPortNumber();
		if (isWebSocket()) {
			protocol = "ws://";
			// If there is no port specified use default
			if(port == -1) {
				port = WS_PORT;
			}
		} else {
			protocol = "tcp://";
			// If there is no port specified use default
			if(port == -1) {
				port = MQTT_PORT;
			}
		}
		
		String mqttServer = getMQTTServer();
		if(mqttServer != null){
			serverURI = protocol + mqttServer + ":" + port;
		} else {
			serverURI = protocol + getOrgId() + "." + MESSAGING + "." + this.getDomain() + ":" + port;
		}
		
		try {
			persistence = new MemoryPersistence();
			mqttAsyncClient = new MqttAsyncClient(serverURI, clientId, persistence);
			mqttAsyncClient.setCallback(mqttCallback);
			mqttClientOptions = new MqttConnectOptions();
			if (clientUsername != null) {
				mqttClientOptions.setUserName(clientUsername);
			}
			if (clientPassword != null) {
				mqttClientOptions.setPassword(clientPassword.toCharArray());
			}
			mqttClientOptions.setCleanSession(this.isCleanSession());
			if(this.keepAliveInterval != -1) {
				mqttClientOptions.setKeepAliveInterval(this.keepAliveInterval);
			}
			mqttClientOptions.setMaxInflight(getMaxInflight());
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 
	 * @return the port number specified by the user
	 */
	private int getPortNumber() {
		String port = options.getProperty("port", "-1");
		port = trimedValue(port);
		return Integer.parseInt(port);
	}
	
	/**
	 * @return the MQTT Server specified by the user
	 */
	public String getMQTTServer() {
		String mqttServer;
		mqttServer = options.getProperty("mqtt-server");
		if(mqttServer == null) {
			return null;
		}
		return trimedValue(mqttServer);
	}
	
	/**
	 * 
	 * @return the MQTT Client ID
	 */
	public String getClientID() {
		return this.clientId;
	}
	
	/**
	 * Call to the configureConnOptionsWithToken() method is made, when the User chooses to connect to the
	 * Watson IoT Platform using Device Token as the preferred Authentication mechanism. The Device Properties
	 * file allows you enable either Token based or Certificate based or both mechanisms to authenticate.
	 * However, setting the value to either 'True' or 'False' against the parameter 'Use-Secure-Certificate',
	 * facilitates usage of Certificates for authentication or not, respectively.
	 * Setting the value of parameter 'Use-Secure-Certificate' to 'False' in the Device.Properties file will
	 * make a call to the following method. 
	 */
	
	private void connectUsingToken() {
		String protocol = null;
		int port = getPortNumber();
		if (isWebSocket()) {
			protocol = "wss://";
			// If there is no port specified use default
			if(port == -1) {
				port = WSS_PORT;
			}
		} else {
			protocol = "ssl://";
			// If there is no port specified use default
			if(port == -1) {
				port = MQTTS_PORT;
			}
		} 

		String mqttServer = getMQTTServer();
		if(mqttServer != null){
			serverURI = protocol + mqttServer + ":" + port;
		} else {
			serverURI = protocol + getOrgId() + "." + MESSAGING + "." + this.getDomain() + ":" + port;
		}
		try {
			mqttAsyncClient = new MqttAsyncClient(serverURI, clientId, null);
			mqttAsyncClient.setCallback(mqttCallback);
			mqttClientOptions = new MqttConnectOptions();
			if (clientUsername != null) {
				mqttClientOptions.setUserName(clientUsername);
			}
			if (clientPassword != null) {
				mqttClientOptions.setPassword(clientPassword.toCharArray());
			}
			mqttClientOptions.setCleanSession(this.isCleanSession());
			if(this.keepAliveInterval != -1) {
				mqttClientOptions.setKeepAliveInterval(this.keepAliveInterval);
			}
			mqttClientOptions.setMaxInflight(getMaxInflight());
			mqttClientOptions.setAutomaticReconnect(isAutomaticReconnect());
			
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

			sslContext.init(null, null, null);
			
			mqttClientOptions.setSocketFactory(sslContext.getSocketFactory());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Call to the connectUsingCertificate() method is made, when the User chooses to connect to the Watson
	 * IoT Platform using Client Certificate as the preferred Authentication mechanism. The Device Properties
	 * file allows you enable either Token based or Certificate based or both mechanisms to authenticate.
	 * However, setting the value to either 'True' or 'False' against the parameter 'Use-Secure-Certificate',
	 * facilitates usage of Certificates for authentication or not, respectively.
	 * Setting the value of parameter 'Use-Secure-Certificate' to 'True' in the Device.Properties file will
	 * make a call to the following method. 
	 */
	
	private void connectUsingCertificate() {
		final String METHOD = "connectUsingCertificate";

		/**
		 * Add BouncyCastleProvider to the security provider list
		 * Note: the position is returned if it was added
		 *       or -1 if it was not added because it is already installed.
		 */
		int position = Security.addProvider(new BouncyCastleProvider());
	    LoggerUtility.info(CLASS_NAME, METHOD, "Security provider position " + position);
		
		String protocol = null;
		int port = getPortNumber();
		if (isWebSocket()) {
			protocol = "wss://";
			// If there is no port specified use default
			if(port == -1) {
				port = WSS_PORT;
			}
		} else {
			protocol = "ssl://";
			// If there is no port specified use default
			if(port == -1) {
				port = MQTTS_PORT;
			}
		} 

		String mqttServer = getMQTTServer();
		
		if(mqttServer != null){
			serverURI = protocol + mqttServer + ":" + port;
		} else {
			serverURI = protocol + getOrgId() + "." + MESSAGING + "." + this.getDomain() + ":" + port;
		}
		
		try {
			mqttAsyncClient = new MqttAsyncClient(serverURI, clientId, null);
		} catch (MqttException e) {
			e.printStackTrace();
			throw new RuntimeException("Create MqttAsynClient failed, exception: " + e.getMessage());
		}
		mqttAsyncClient.setCallback(mqttCallback);
		mqttClientOptions = new MqttConnectOptions();
		if (clientUsername != null) {
			mqttClientOptions.setUserName(clientUsername);
		}
		if (clientPassword != null) {
			mqttClientOptions.setPassword(clientPassword.toCharArray());
		}
		mqttClientOptions.setCleanSession(this.isCleanSession());
		if(this.keepAliveInterval != -1) {
			mqttClientOptions.setKeepAliveInterval(this.keepAliveInterval);
		}
		
		mqttClientOptions.setMaxInflight(getMaxInflight());
		mqttClientOptions.setAutomaticReconnect(isAutomaticReconnect());
		
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLSv1.2");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get SSLContext object. Exception: " + e.getMessage());
		}
		try {
			sslContext.init(null, null, null);
		} catch (KeyManagementException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed initializing SSLContext object. Exception: " + e.getMessage());
		}
		
		String serverCert = null;
		String clientCert = null;
		String clientCertKey = null;
		String certPassword = null;
		
		String trustStorePath = options.getProperty("TrustStorePath");
		String trustStorePassword = options.getProperty("TrustStorePassword");
		String keyStorePath = options.getProperty("KeyStorePath");
		String keyStorePassword = options.getProperty("KeyStorePassword");
		
		if (trustStorePath == null) {
			//Validate the availability of Server Certificate
			if (trimedValue(options.getProperty("Server-Certificate")) != null){
				if (trimedValue(options.getProperty("Server-Certificate")).contains(".pem")||trimedValue(options.getProperty("Server-Certificate")).contains(".der")||trimedValue(options.getProperty("Server-Certificate")).contains(".cer")){
					serverCert = trimedValue(options.getProperty("Server-Certificate"));
				} else{
					LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Only PEM, DER & CER certificate formats are supported at this point of time");
					throw new RuntimeException("Only PEM, DER & CER certificate formats are supported at this point of time");
				}
			} else{
				LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Value for Server Certificate is missing, using default one");
			}
		}
		
		if (keyStorePath == null) {
			//Validate the availability of Client Certificate
			if (trimedValue(options.getProperty("Client-Certificate")) != null){
				if (trimedValue(options.getProperty("Client-Certificate")).contains(".pem")||trimedValue(options.getProperty("Client-Certificate")).contains(".der")||trimedValue(options.getProperty("Client-Certificate")).contains(".cer")){
					clientCert = trimedValue(options.getProperty("Client-Certificate"));
				} else {
					LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Only PEM, DER & CER certificate formats are supported at this point of time");
					throw new RuntimeException("Only PEM, DER & CER certificate formats are supported at this point of time");
				}
			} 

			//Validate the availability of Client Certificate Key
			if (trimedValue(options.getProperty("Client-Key")) != null) {
				if (trimedValue(options.getProperty("Client-Key")).contains(".key")){
					clientCertKey = trimedValue(options.getProperty("Client-Key"));
				} else {
					LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Only Certificate key in .key format is supported at this point of time");
					return;
				}
			}
			//Validate the availability of Certificate Password
			try {
				if (trimedValue(options.getProperty("Certificate-Password")) != null){
					certPassword = trimedValue(options.getProperty("Certificate-Password"));
				} else {
					certPassword = "";
				}
			} catch (RuntimeException e) {
					LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Value for Certificate Password is missing", e);
					throw e;
			}				
		}
		
		
		SSLSocketFactory sslSocketFactory = null;
		
		// Using both trust store and client key store 
		if (trustStorePath != null && trustStorePassword != null && keyStorePath != null && keyStorePassword != null) {
			try {
				sslSocketFactory = getSocketFactoryFromTrustStoreKeyStore(trustStorePath, trustStorePassword, keyStorePath, keyStorePassword);
			} catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException
					| KeyStoreException | CertificateException | IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Error getting SocketFactory. Exception: " + e.getMessage());
			}
		}
		
		// Using only trust store
		if (sslSocketFactory == null  && trustStorePath != null && trustStorePassword != null 
				&& keyStorePath == null && keyStorePassword == null) {
			try {
				sslSocketFactory = getSocketFactoryFromTrustStore(trustStorePath, trustStorePassword);
			} catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException
					| KeyStoreException | CertificateException | IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Error getting SocketFactory. Exception: " + e.getMessage());
			}
		}
		
		if (sslSocketFactory == null) {
			// Using certificate PEM files
			try {
				sslSocketFactory = getSocketFactory(serverCert, clientCert, clientCertKey, certPassword);
			} catch (UnrecoverableKeyException | KeyManagementException | CertificateException | KeyStoreException
					| NoSuchAlgorithmException | IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Error getting SocketFactory. Exception: " + e.getMessage());
			}
		}
		
		mqttClientOptions.setSocketFactory(sslSocketFactory);
			
	}
	
	public void setKeepAliveInterval(int keepAliveInterval) {
		this.keepAliveInterval = keepAliveInterval;
	}
	
	/**
	 * 
	 * Check whether the clean session is disabled
	 * 
	 * @return boolean value containing whether its a clean session or not.
	 */
	public boolean isCleanSession() {
		boolean enabled = true;
		String value = options.getProperty("Clean-Session");
		if(value == null) {
			value = options.getProperty("clean-session");
		}
		if(value != null) {
			enabled = Boolean.parseBoolean(trimedValue(value));
		} 
		return enabled;
	}
	
	public boolean isWebSocket() {
		boolean enabled = false;
		String value = options.getProperty("WebSocket");
		if (value != null) {
			enabled = Boolean.parseBoolean(trimedValue(value));
		}
		return enabled;
	}
	
	private String getKeystoreType() {
		String type = options.getProperty("Keystore-Type");
		if (type == null) {
			type = KeyStore.getDefaultType();
		}
		return type;
	}
	
	public boolean isAutomaticReconnect() {
		boolean enabled = true;
		String value = options.getProperty("Automatic-Reconnect");
		if (value != null) {
			enabled = Boolean.parseBoolean(trimedValue(value));
		}
		return enabled;
	}
	
	public int getDisconnectedBufferSize() {
		int size = 5000;
		String value = options.getProperty("Disconnected-Buffer-Size");
		if (value != null) {
			size = Integer.parseInt(value);
		}
		return size;
	}
	
	public long getActionTimeout() {
		long timeout = DEFAULT_ACTION_TIMEOUT;
		String value = options.getProperty("ActionTimeout");
		if (value != null) {
			timeout = Integer.parseInt(trimedValue(value));
		}
		return timeout;
	}
	
	public int getMaxConnectAttempts() {
		int attempts = DEFAULT_MAX_CONNECT_ATTEMPTS;
		String value = options.getProperty("MaxConnectAttempts");
		if (value != null) {
			attempts = Integer.parseInt(trimedValue(value));
		}
		return attempts;
	}
	
	public int getMaxInflight() {
		int maxInflight = DEFAULT_MAX_INFLIGHT_MESSAGES;
		String value = options.getProperty("MaxInflightMessages");
		if (value != null) {
			maxInflight = Integer.parseInt(trimedValue(value));
		}
		return maxInflight;
	}
	
	public int getMessagingQoS() {
		int qos = DEFAULT_MESSAGING_QOS;
		String value = options.getProperty("MessagingQoS");
		if (value != null) {
			qos = Integer.parseInt(trimedValue(value));
			if (qos < 0 || qos > 2) {
				qos = DEFAULT_MESSAGING_QOS;
			}
		}
		return qos;
	}

	/**
	 * Sleep for a variable period of time between connect attempts.
	 * 
	 * @param attempts
	 *               How many times have we tried (and failed) to connect
	 */
	private void waitBeforeNextConnectAttempt(final int attempts) {
		final String METHOD = "waitBeforeNextConnectAttempt";
		// Log when throttle boundaries are reached
		if (attempts == THROTTLE_3) {
			LoggerUtility.warn(CLASS_NAME, METHOD, String.valueOf(attempts) + 
					" consecutive failed attempts to connect.  Retry delay increased to " + String.valueOf(RATE_3) + "ms");
		}
		else if (attempts == THROTTLE_2) {
			LoggerUtility.warn(CLASS_NAME, METHOD, String.valueOf(attempts) + 
					" consecutive failed attempts to connect.  Retry delay increased to " + String.valueOf(RATE_2) + "ms");
		}
		else if (attempts == THROTTLE_1) {
			LoggerUtility.info(CLASS_NAME, METHOD, String.valueOf(attempts) + 
					" consecutive failed attempts to connect.  Retry delay set to " + String.valueOf(RATE_1) + "ms");
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
		final String METHOD = "disconnect";
		
		try {
			this.disconnectRequested = true;
			if (mqttAsyncClient != null) {
				LoggerUtility.info(CLASS_NAME, METHOD, mqttAsyncClient.getClientId() 
						+ " is disconnecting from the IBM Watson IoT Platform ...");
				mqttAsyncClient.disconnect().waitForCompletion(getActionTimeout());
				LoggerUtility.info(CLASS_NAME, METHOD, mqttAsyncClient.getClientId()
					+ " successfully disconnected from the IBM Watson IoT Platform");
			}
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Close and free all MQTT client resources
	 * @throws MqttException Thrown if an error occurs
	 */
	public void close() throws MqttException {
		final String METHOD = "close";
		LoggerUtility.info(CLASS_NAME, METHOD, "Closing MQTT client (" + clientId + ")");
		if (mqttAsyncClient != null) {
			mqttAsyncClient.close(true);
			mqttAsyncClient = null;
			LoggerUtility.info(CLASS_NAME, METHOD, "Closed MQTT client (" + clientId + ")");
		}
	}
	
	
	/**
	 * Determine whether this device is currently connected to the IBM Watson Internet
	 * of Things Platform.
	 * 
	 * @return Whether the device is connected to the IBM Watson IoT Platform
	 */
	public boolean isConnected() {
		final String METHOD = "isConnected";
		boolean connected = false;
		if (mqttAsyncClient != null) {
			connected = mqttAsyncClient.isConnected();
		} else if (mqttClient != null) {
			connected = mqttClient.isConnected();
		}
		LoggerUtility.log(Level.FINEST, CLASS_NAME, METHOD, "Connected(" + connected + ")");
		return connected;
	}
	
	/**
	 * Provides a human readable String representation of this Device, including the number
	 * of messages sent and the current connect status.
	 * 
	 * @return String representation of the Device.
	 */
	public String toString() {
		return "[" + clientId + "] " + messageCount + " messages sent - Connected = " + String.valueOf(isConnected());
	}

	/**
	 * Parses properties file and returns back an object of Properties class
	 * 
	 * @param propertiesFile
	 * 						File object
	 * @return properties
	 * 						Properties object
	 */	
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

	/**
	 * 
	 * @return the domain
	 */
	protected String getDomain() {
		String domain;
		domain = options.getProperty("domain");
		
		if(domain == null) {
			domain = options.getProperty("Domain");
		}
		domain = trimedValue(domain);
		
		if(domain != null && !("".equals(domain))) {
			return domain;
		} else {
			return DEFAULT_DOMAIN;
		}
	}
	
	/**
	 * 
	 * @return the organization id
	 */
	public String getOrgId() {
		String org;
		org = options.getProperty("org");
		
		if(org == null) {
			org = options.getProperty("Organization-ID");
		}
		return trimedValue(org);
	}
	
	/*
	 * old style - id
	 * new style - Device-ID
	 */
	public String getDeviceId() {
		String id;
		id = options.getProperty("id");
		if(id == null) {
			id = options.getProperty("Device-ID");
		}
		return trimedValue(id);
	}
	
	public static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}

	/**
	 * Accessor method to retrieve Authendication Method
	 * old style - auth-method
	 * new style - Authentication-Method
	 * 
	 * @return The Authentication method
	 */	
	public String getAuthMethod() {
		String method = options.getProperty("auth-method");
		if(method == null) {
			method = options.getProperty("Authentication-Method");
		}
		return trimedValue(method);
	}

	/*
	 * old style - auth-token
	 * new style - Authentication-Token
	 */
	public String getAuthToken() {
		String token = options.getProperty("auth-token");
		if(token == null) {
			token = options.getProperty("Authentication-Token");
		}
		return trimedValue(token);
	}

	
	private static void validateNull(String property, String value) throws Exception {
		if(value == null || value == "") {
			throw new Exception(property +" cannot be null or empty !");
		}
	}
	
	/**
	 * @param organization  Organization ID (Either "quickstart" or the registered organization ID)
	 * @param domain		Domain of the Watson IoT Platform, for example internetofthings.ibmcloud.com
	 * @param deviceType	Device Type
	 * @param deviceId		Device ID
	 * @param eventName		Name of the Event
	 * @param device 		Boolean value indicating whether the request is originated from device or application
	 * @param authKey		Authentication Method
	 * @param authToken		Authentication Token to securely post this event (Can be null or empty if its quickstart)
	 * @param payload		The message to be published
	 * @return int			HTTP code indicating the status of the HTTP request
	 * @throws Exception	throws exception when http post fails
	 */
	protected static int publishEventsThroughHttps(String organization,
			String domain,
			String deviceType,
			String deviceId,
			String eventName,
			boolean device,
			String authKey,
			String authToken,
			Object payload) throws Exception {

		final String METHOD = "publishEventsThroughHttps";

		validateNull("Organization ID", organization);
		validateNull("Domain", domain);
		validateNull("Device Type", deviceType);
		validateNull("Device ID", deviceId);
		validateNull("Event Name", eventName);
		if(QUICK_START.equalsIgnoreCase(organization) == false) {
			validateNull("Authentication Method", authKey);
			validateNull("Authentication Token", authToken);
		}

		StringBuilder sb = new StringBuilder();
		
		// Form the URL
		if(QUICK_START.equalsIgnoreCase(organization)) {
			sb.append("http://");
		} else {
			sb.append("https://");
		}
		sb.append(organization)
			.append(".messaging.internetofthings.ibmcloud.com/api/v0002");
			
		if(device == true) {
			sb.append("/device");
		} else {
			sb.append("/application");
		}
		sb.append("/types/")
			.append(deviceType)
			.append("/devices/")
			.append(deviceId)
			.append("/events/")
			.append(eventName);
		
		LoggerUtility.fine(CLASS_NAME, METHOD, "ReST URL::"+sb.toString());
		BufferedReader br = null;
	
		// Create the payload message in Json format
		JsonObject message = (JsonObject) gson.toJsonTree(payload);		
		StringEntity input = new StringEntity(message.toString(), StandardCharsets.UTF_8);
		
		// Create the Http post request
		HttpPost post = new HttpPost(sb.toString());
		post.setEntity(input);
		post.addHeader("Content-Type", "application/json");
		post.addHeader("Accept", "application/json");
		
		if(QUICK_START.equalsIgnoreCase(organization) == false) {
			byte[] encoding = Base64.encodeBase64(new String(authKey + ":" + authToken).getBytes() );			
			String encodedString = new String(encoding);
			post.addHeader("Authorization", "Basic " + encodedString);
		}
		
		try {
			
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, null, null);
			
			HttpClient client = HttpClientBuilder.create().setSSLContext(sslContext).build();
			
			HttpResponse response = client.execute(post);
			
			int httpCode = response.getStatusLine().getStatusCode();
			if(httpCode >= 200 && httpCode < 300) {
				return httpCode;
			}
			
			/**
			 * Looks like some error so log the header and response
			 */
			System.out.println("Looks like some error, so log the header and response");
			StringBuilder log = new StringBuilder("HTTP Code: "+httpCode);
			log.append("\nURL: ")
				.append(sb.toString())
				.append("\nHeader:\n");
			Header[] headers = response.getAllHeaders();
			for(int i = 0; i < headers.length; i++) {
				log.append(headers[i].getName())
					.append(' ')
					.append(headers[i].getValue())
					.append('\n');
			}
			log.append("\nResponse \n");
			br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
			log.append(br.readLine());
			LoggerUtility.severe(CLASS_NAME, METHOD, log.toString());
			
			return httpCode;
		} catch (IOException e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, e.getMessage());
			throw e;
		} finally {
			if(br != null) {
				br.close();
			}
		}
	}

	/**
	 * Get SSLSocketFactory object from CA certificate file, client Certificate file and Key file.
	 * 
	 * @param caCrtFile Path to CA certificate, can be null (default messaging.pem will be used)
	 * @param clientCertFile Path to client certificate
	 * @param clientKeyFile Path to client key file
	 * @param password Password to access client key
	 * @return SSLSocketFactory
	 * @throws IOException
	 * @throws CertificateException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 */
	private SSLSocketFactory getSocketFactory (String caCrtFile, String clientCertFile, 
			String clientKeyFile, String password) throws IOException, CertificateException, UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException { 

		final String METHOD = "getSocketFactory";
		
		X509Certificate caCert = null;

		if (clientCertFile == null) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Value for Client Certificate is missing");
			throw new RuntimeException("Value for Client Certificate is missing");
		}
		
		if (clientKeyFile == null) {
			LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, "Value for Client Key is missing");
			throw new RuntimeException("Value for Client Key is missing");
		}

		if (caCrtFile != null) {
		    // load CA certificate
		    PEMReader reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(caCrtFile)))));
		    caCert = (X509Certificate)reader.readObject();
		    reader.close();
	    } else {
	    	// Use the default messaging.pem
	    	 ClassLoader classLoader = AbstractClient.class.getClassLoader();
	    	 PEMReader reader = new PEMReader(new InputStreamReader(classLoader.getResource(SERVER_MESSAGING_PEM).openStream()));
			    caCert = (X509Certificate)reader.readObject();
			    reader.close();
	    }
	    
	    PEMReader reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(clientCertFile)))));
	    X509Certificate cert = (X509Certificate)reader.readObject();
	    reader.close();
	    
	    // load client private key
	    reader = new PEMReader(
	            new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(clientKeyFile))))
	    );
	    KeyPair key = (KeyPair)reader.readObject();
	    reader.close();
	    	    
	    TrustManagerFactory tmf = null;
	    if (caCert != null) {
		    // CA certificate is used to authenticate server
		    KeyStore caKs = KeyStore.getInstance(getKeystoreType());
			caKs.load(null, null);
		    KeyStore.TrustedCertificateEntry caEntry = new KeyStore.TrustedCertificateEntry(caCert);
		    caKs.setCertificateEntry("ca-certificate", caEntry.getTrustedCertificate());
		    try {
				tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			} catch (NoSuchAlgorithmException e) {
				if (TrustManagerFactory.getDefaultAlgorithm().equalsIgnoreCase("PKIX") == false) {
					tmf = TrustManagerFactory.getInstance("PKIX");
				} else {
					throw e;
				}
			}
		    tmf.init(caKs);
	    } 
	    
	    // client key and certificates are sent to server so it can authenticate us
	    KeyStore ks = KeyStore.getInstance(getKeystoreType());
		ks.load(null, null);
	    ks.setCertificateEntry("certificate", cert);
	    ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{cert});
	    KeyManagerFactory kmf;
		try {
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		} catch (NoSuchAlgorithmException e) {
			if (KeyManagerFactory.getDefaultAlgorithm().equalsIgnoreCase("PKIX") == false) {
				kmf = KeyManagerFactory.getInstance("PKIX");
			} else {
				throw e;
			}
		}
	    
	    kmf.init(ks, password.toCharArray());

	    // finally, create SSL socket factory
	    SSLContext context = SSLContext.getInstance("TLSv1.2");
	    LoggerUtility.info(CLASS_NAME, METHOD, "Using security provider: " + context.getProvider().getName());
	    if (tmf != null) {
	    	context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	    } else {
	    	context.init(kmf.getKeyManagers(), null, null);
	    }

	    return context.getSocketFactory();
	}

	/**
	 * Load and return the KeyStore object from the specified trust store or client key store
	 * @param path Path to the key store file
	 * @param password Password to access the key store file
	 * @return KeyStore
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	private KeyStore getKeyStore(String path, String password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		
		KeyStore ks = KeyStore.getInstance(getKeystoreType());

	    java.io.FileInputStream fis = null;
	    try {
	        fis = new java.io.FileInputStream(path);
	        ks.load(fis, password.toCharArray());
	    } finally {
	        if (fis != null) {
	            fis.close();
	        }
	    }
	    return ks;
	}

	/**
	 * Use this method to obtain a SSLSocketFactory object when client certificate is used.
	 * 
	 * @param trustStorePath Path to trust store
	 * @param trustStorePassword Password to access the trust store
	 * @param keyStorePath Path to client key store
	 * @param keyStorePassword Password to access the client key store
	 * @return SSLSocketFactory
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 */
	private SSLSocketFactory getSocketFactoryFromTrustStoreKeyStore(
			String trustStorePath, String trustStorePassword,
			String keyStorePath, String keyStorePassword) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		final String METHOD = "getSocketFactoryFromTrustStoreKeyStore";

		TrustManagerFactory tmf = null;
		KeyManagerFactory kmf = null;
	    
		tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		KeyStore trustStore = getKeyStore(trustStorePath, trustStorePassword);
		tmf.init(trustStore);	    
		
		kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		KeyStore keyStore = getKeyStore(keyStorePath, keyStorePassword);
		kmf.init(keyStore, keyStorePassword.toCharArray());

		SSLContext context = SSLContext.getInstance("TLSv1.2");
	    LoggerUtility.info(CLASS_NAME, METHOD, "Using security provider: " + context.getProvider().getName());
		context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		return context.getSocketFactory();
	}

	/**
	 * Use this method to obtain a SSLSocketFactory object when client certificate is not used.
	 * 
	 * @param trustStorePath Path to trust store
	 * @param trustStorePassword Password to access the trust store
	 * @return SSLSocketFactory
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 */
	private SSLSocketFactory getSocketFactoryFromTrustStore(
			String trustStorePath, String trustStorePassword) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		final String METHOD = "getSocketFactoryFromTrustStore";

		TrustManagerFactory tmf = null;
	    
		tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		KeyStore trustStore = getKeyStore(trustStorePath, trustStorePassword);
		tmf.init(trustStore);	    

		// Get a SSLContext that implements the specified secure socket protocol (TLSv1.2)
		SSLContext context = SSLContext.getInstance("TLSv1.2");
	    LoggerUtility.info(CLASS_NAME, METHOD, "Using security provider: " + context.getProvider().getName());
		context.init(null, tmf.getTrustManagers(), null);
		return context.getSocketFactory();
	}
	
}
