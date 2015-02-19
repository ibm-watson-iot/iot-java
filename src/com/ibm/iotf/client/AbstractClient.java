package com.ibm.iotf.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import com.google.gson.Gson;

/**
 * A client that handles connections with the IBM Internet of Things Foundation.
 * This is an abstract class which has to be extended
 */
public abstract class AbstractClient {
	
	private static final String CLASS_NAME = AbstractClient.class.getName();
	private static final Logger LOG = Logger.getLogger(CLASS_NAME);
	
	protected static final String CLIENT_ID_DELIMITER = ":";
	
	protected static final String DOMAIN = "messaging.internetofthings.ibmcloud.com";
	protected static final int MQTT_PORT = 1883;
	protected static final int MQTTS_PORT = 8883;
	
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
	
	protected final Gson gson = new Gson();
	
	/**
	 * A formatter for ISO 8601 compliant timestamps.
	 */
	protected final DateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	protected Properties options;
	protected String clientId;
	protected String clientUsername;
	protected String clientPassword;
	
	protected int messageCount = 0;
	
	protected MqttClient mqttClient;
	protected MqttConnectOptions mqttClientOptions;
	protected MqttCallback mqttCallback;
	

	/**
	 * Note that this class does not have a default constructor <br>
	 * @param options
	 * 			Properties object which contains different artifacts such as auth-key
	 * 
	 */		
	
	public AbstractClient(Properties options) {
		this.options = options;
		LOG.fine(options.toString());
	}
	
	/**
	 * Create the Paho MQTT Client that will underpin the Device client.
	 * @param callback
	 * 			MqttCallback 
	 * @see <a href="Paho Client Library">http://www.eclipse.org/paho/files/javadoc/index.html</a> 
	 * 
	 */	

	protected void createClient(MqttCallback callback) {
		System.out.println("Org ID          = " + getOrgId());
		System.out.println("Client ID       = " + clientId);
		System.out.println("Client Username = " + clientUsername);
		System.out.println("Client Password = " + clientPassword);
		this.mqttClient = null;
		this.mqttClientOptions = new MqttConnectOptions();
		this.mqttCallback = callback;
	}
	
	/**
	 * Connect to the IBM Internet of Things Foundation
	 */
	public void connect() {
		boolean tryAgain = true;
		int connectAttempts = 0;

		if (getOrgId() == "quickstart") {
			configureMqtt();
		}
		else {
			configureMqtts();
		}
		while (tryAgain) {
			connectAttempts++;
			
			LOG.fine("Connecting to " + mqttClient.getServerURI() + " (attempt #" + connectAttempts + ")...");
			if (clientUsername != null) {
				LOG.fine(" * Username: " + mqttClientOptions.getUserName());
			}
			if (clientPassword != null) {
				LOG.fine(" * Passowrd: " + String.valueOf(mqttClientOptions.getPassword()));
			}
			try {
				mqttClient.connect(mqttClientOptions);
			} catch (MqttSecurityException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}
			
			if (mqttClient.isConnected()) {
				LOG.info("Successfully connected to the IBM Internet of Things Foundation");
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.finest(" * Connection attempts: " + connectAttempts);
				}
				tryAgain = false;
			} else {
				waitBeforeNextConnectAttempt(connectAttempts);
			}
		}
	}
	
	private void configureMqtt() {
		String serverURI = "tcp://" + getOrgId() + "." + DOMAIN + ":" + MQTT_PORT;
		try {
			mqttClient = new MqttClient(serverURI, clientId, null);
			mqttClient.setCallback(mqttCallback);
			mqttClientOptions = new MqttConnectOptions();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	private void configureMqtts() {
		String serverURI = "ssl://" + getOrgId() + "." + DOMAIN + ":" + MQTTS_PORT;
		try {
			mqttClient = new MqttClient(serverURI, clientId, null);
			mqttClient.setCallback(mqttCallback);
			
			mqttClientOptions = new MqttConnectOptions();
			mqttClientOptions.setUserName(clientUsername);
			mqttClientOptions.setPassword(clientPassword.toCharArray());
			
			/* This isn't needed as the production messaging.internetofthings.ibmcloud.com 
			 * certificate should already be in trust chain.
			 * 
			 * See: 
			 *   http://stackoverflow.com/questions/859111/how-do-i-accept-a-self-signed-certificate-with-a-java-httpsurlconnection
			 *   https://gerrydevstory.com/2014/05/01/trusting-x509-base64-pem-ssl-certificate-in-java/
			 *   http://stackoverflow.com/questions/12501117/programmatically-obtain-keystore-from-pem
			 *   https://gist.github.com/sharonbn/4104301
			 * 
			 * CertificateFactory cf = CertificateFactory.getInstance("X.509");
			 * InputStream certFile = AbstractClient.class.getResourceAsStream("messaging.pem");
			 * Certificate ca = cf.generateCertificate(certFile);
			 *
			 * KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			 * keyStore.load(null, null);
			 * keyStore.setCertificateEntry("ca", ca);
			 * TrustManager trustManager = TrustManagerUtils.getDefaultTrustManager(keyStore);
			 * SSLContext sslContext = SSLContextUtils.createSSLContext("TLSv1.2", null, trustManager);
			 * 
			 */
			 
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, null, null);
			mqttClientOptions.setSocketFactory(sslContext.getSocketFactory());
		} catch (MqttException | GeneralSecurityException e) {
			LOG.warning("Unable to configure TLSv1.2 connection: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Sleep for a variable period of time between connect attempts.
	 * 
	 * @param attempts
	 *               How many times have we tried (and failed) to connect
	 */
	private void waitBeforeNextConnectAttempt(final int attempts) {
		// Log when throttle boundaries are reached
		if (attempts == THROTTLE_3) {
			LOG.warning(String.valueOf(attempts) + " consecutive failed attempts to connect.  Retry delay increased to " + String.valueOf(RATE_3) + "ms");
		}
		else if (attempts == THROTTLE_2) {
			LOG.warning(String.valueOf(attempts) + " consecutive failed attempts to connect.  Retry delay increased to " + String.valueOf(RATE_2) + "ms");
		}
		else if (attempts == THROTTLE_1) {
			LOG.info(String.valueOf(attempts) + " consecutive failed attempts to connect.  Retry delay set to " + String.valueOf(RATE_1) + "ms");
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
	 * Disconnect the device from the IBM Internet of Things Foundation
	 */
	public void disconnect() {
		LOG.fine("Disconnecting from the IBM Internet of Things Foundation ...");
		try {
			mqttClient.disconnect();
			LOG.info("Successfully disconnected from from the IBM Internet of Things Foundation");
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Determine whether this device is currently connected to the IBM Internet
	 * of Things Foundation.
	 * 
	 * @return Whether the device is connected to the IBM Internet of Things Foundation
	 */
	public boolean isConnected() {
		return mqttClient.isConnected();
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
	 * Returns the orgid for this client
	 * 
	 * @return orgid
	 * 						String orgid
	 */
	public String getOrgId() {
//		return options.getProperty("org");
		String authKeyPassed = options.getProperty("auth-key");
		if(authKeyPassed != null && ! authKeyPassed.trim().equals("") && ! authKeyPassed.equals("quickstart")) {
			return authKeyPassed.substring(2, 8);			
		} else {
			return "quickstart";
		}

	}


}
