package com.ibm.iotf.client.device;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.AbstractClient;
import com.ibm.iotf.util.LoggerUtility;


/**
 * A client, used by device, that handles connections with the IBM Internet of Things Foundation. <br>
 * 
 * This is a derived class from AbstractClient and can be used by embedded devices to handle connections with IBM Internet of Things Foundation.
 */
public class DeviceClient extends AbstractClient {
	
	private static final String CLASS_NAME = DeviceClient.class.getName();
	
	private static final Pattern COMMAND_PATTERN = Pattern.compile("iot-2/cmd/(.+)/fmt/(.+)");
	
	private CommandCallback commandCallback = null;
	
	/**
	 * This constructor allows external user to pass the existing MqttAsyncClient 
	 * @param mqttAsyncClient
	 */
	protected DeviceClient(MqttAsyncClient mqttAsyncClient) {
		super(mqttAsyncClient);
	}

	/**
	 * This constructor allows external user to pass the existing MqttClient 
	 * @param mqttClient
	 */
	protected DeviceClient(MqttClient mqttClient) {
		super(mqttClient);
	}
	/**
	 * Create a device client for the IBM Internet of Things Foundation. <br>
	 * 
	 * Connecting to a specific account on the IoTF.
	 * @throws Exception 
	 */
	public DeviceClient(Properties options) throws Exception {
		super(options);
		System.out.println(options.toString());
		this.clientId = "d" + CLIENT_ID_DELIMITER + getOrgId() + CLIENT_ID_DELIMITER + getDeviceType() + CLIENT_ID_DELIMITER + getDeviceId();
		
		if (getAuthMethod() == null) {
			this.clientUsername = null;
			this.clientPassword = null;
		}
		else if (!getAuthMethod().equals("token")) {
			throw new Exception("Unsupported Authentication Method: " + getAuthMethod());
		}
		else {
			// use-token-auth is the only authentication method currently supported
			this.clientUsername = "use-token-auth";
			this.clientPassword = getAuthToken();
		}
		createClient(this.new MqttDeviceCallBack());
	}
	
	/*
	 * old style - org
	 * new style - Organization-ID
	 */
	public String getOrgId() {
		String org = null;
		org = options.getProperty("org");
		
		if(org == null) {
			org = options.getProperty("Organization-ID");
		}
		return org;
	}

	/*
	 * old style - id
	 * new style - Device-ID
	 */
	public String getDeviceId() {
		String id = null;
		id = options.getProperty("id");
		if(id == null) {
			id = options.getProperty("Device-ID");
		}
		return id;
	}

	/*
	 * old style - type
	 * new style - Device-Type
	 */
	public String getDeviceType() {
		String type = null;
		type = options.getProperty("type");
		if(type == null) {
			type = options.getProperty("Device-Type");
		}
		return type;
	}

	/*
	 * old style - auth-method
	 * new style - Authentication-Method
	 */	
	public String getAuthMethod() {
		String method = options.getProperty("auth-method");
		if(method == null) {
			method = options.getProperty("Authentication-Method");
		}
		return method;
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
		return token;
	}


	public String getFormat() {
		String format = options.getProperty("format");
		if(format != null && ! format.equals(""))
			return format;
		else
			return "json";
		
	}
	
	/**
	 * Connect to the IBM Internet of Things Foundation
	 * 
	 */	
	@Override
	public void connect() {
		super.connect();
		if (!getOrgId().equals("quickstart")) {
			subscribeToCommands();
		}
	}
	
	/*
	 * This method reconnects when the connection is lost due to n/w interruption
	 */
	protected void reconnect() {
		super.connect();
		if (!getOrgId().equals("quickstart")) {
			subscribeToCommands();
		}
	}
	
	private void subscribeToCommands() {
		try {
			mqttAsyncClient.subscribe("iot-2/cmd/+/fmt/" + getFormat(), 2);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Publish data to the IBM Internet of Things Foundation.<br>
	 * Note that data is published
	 * at Quality of Service (QoS) 0, which means that a successful send does not guarantee
	 * receipt even if the publish has been successful.
	 * 
	 * @param event
	 *            Name of the dataset under which to publish the data
	 * @param data
	 *            Object to be added to the payload as the dataset
	 * @return Whether the send was successful.
	 */
	public boolean publishEvent(String event, Object data) {
		return publishEvent(event, data, 0);
	}

	/**
	 * Publish data to the IBM Internet of Things Foundation.<br>
	 * 
	 * This method allows QoS to be passed as an argument
	 * 
	 * @param event
	 *            Name of the dataset under which to publish the data
	 * @param data
	 *            Object to be added to the payload as the dataset
	 * @param qos
	 *            Quality of Service - should be 0, 1 or 2
	 * @return Whether the send was successful.
	 */	
	public boolean publishEvent(String event, Object data, int qos) {
		if (!isConnected()) {
			return false;
		}
		final String METHOD = "publishEvent(2)";
		JsonObject payload = new JsonObject();
		
		String timestamp = ISO8601_DATE_FORMAT.format(new Date());
		payload.addProperty("ts", timestamp);
		
		JsonElement dataElement = gson.toJsonTree(data);
		payload.add("d", dataElement);
		
		String topic = "iot-2/evt/" + event + "/fmt/json";
		
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic   = " + topic);
		LoggerUtility.fine(CLASS_NAME, METHOD, "Payload = " + payload.toString());
		
		MqttMessage msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		msg.setQos(qos);
		msg.setRetained(false);
		
		try {
			mqttAsyncClient.publish(topic, msg).waitForCompletion();
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
			return false;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	

	
	private class MqttDeviceCallBack implements MqttCallback {
	
		/**
		 * If we lose connection trigger the connect logic to attempt to
		 * reconnect to the IBM Internet of Things Foundation.
		 * 
		 * @param exception
		 *            Throwable which caused the connection to get lost
		 */
		public void connectionLost(Throwable exception) {
			final String METHOD = "connectionLost";
			LoggerUtility.info(CLASS_NAME, METHOD, exception.getMessage());
			reconnect();
		}
		
		/**
		 * A completed deliver does not guarantee that the message is received by the service
		 * because devices send messages with Quality of Service (QoS) 0. <br>
		 * 
		 * The message count
		 * represents the number of messages that were sent by the device without an error on
		 * from the perspective of the device.
		 * @param token
		 *            MQTT delivery token
		 */
		public void deliveryComplete(IMqttDeliveryToken token) {
			final String METHOD = "deliveryComplete";
			LoggerUtility.fine(CLASS_NAME, METHOD, "token " + token.getMessageId());
			messageCount++;
		}
		
		/**
		 * The Device client does not currently support subscriptions.
		 */
		public void messageArrived(String topic, MqttMessage msg) throws Exception {
			final String METHOD = "messageArrived";
			if (commandCallback != null) {
				/* Only check whether the message is a command if a callback 
				 * has been defined, otherwise it is a waste of time
				 * as without a callback there is nothing to process the generated
				 * command.
				 */
				Matcher matcher = COMMAND_PATTERN.matcher(topic);
				if (matcher.matches()) {
					String command = matcher.group(1);
					String format = matcher.group(2);
					Command cmd = new Command(command, format, msg);
					LoggerUtility.fine(CLASS_NAME, METHOD, "Event received: " + cmd.toString());
					commandCallback.processCommand(cmd);
			    }
			}
		}

	}
	
	public void setCommandCallback(CommandCallback callback) {
		this.commandCallback  = callback;
	}
	
}
