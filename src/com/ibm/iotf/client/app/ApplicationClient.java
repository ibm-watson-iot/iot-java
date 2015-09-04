package com.ibm.iotf.client.app;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.AbstractClient;
import com.ibm.iotf.util.LoggerUtility;

/**
 * A client, used by application, that handles connections with the IBM Internet of Things Foundation. <br>
 * 
 * This is a derived class from AbstractClient and can be used by end-applications to handle connections with IBM Internet of Things Foundation.
 */
public class ApplicationClient extends AbstractClient implements MqttCallback{
	
	private static final String CLASS_NAME = ApplicationClient.class.getName();
	
	private static final Pattern DEVICE_EVENT_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/evt/(.+)/fmt/(.+)");
	private static final Pattern DEVICE_STATUS_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/mon");
	private static final Pattern APP_STATUS_PATTERN = Pattern.compile("iot-2/app/(.+)/mon");
	private static final Pattern DEVICE_COMMAND_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/cmd/(.+)/fmt/(.+)");
	
	
	private EventCallback eventCallback = null;
	private StatusCallback statusCallback = null;
	
	private HashMap<String, Integer> subscriptions = new HashMap<String, Integer>();
	
	/**
	 * Create an application client for the IBM Internet of Things Foundation. 
	 * Connecting to specific org on IBM Internet of Things Foundation
	 * @param options
	 * 					An object of the class Properties
	 * @throws Exception 
	 */
	public ApplicationClient(Properties options) throws Exception {
		super(options);
		if(getOrgId()==null){
			
			throw new Exception("Invalid Auth Key");
		}
		this.clientId = "a" + CLIENT_ID_DELIMITER + getOrgId() + CLIENT_ID_DELIMITER + getAppId();
		
		if (getAuthMethod() == null) {
			this.clientUsername = null;
			this.clientPassword = null;
		}
		else if (!getAuthMethod().equals("apikey")) {
			throw new Exception("Unsupported Authentication Method: " + getAuthMethod());
		}
		else {
			// use-token-auth is the only authentication method currently supported
			this.clientUsername = getAuthKey();
			this.clientPassword = getAuthToken();
		}
		createClient(this);
	}
	
	/**
	 * Accessor method to retrieve app id
	 * @return appId
	 * 					String appId
	 */
	public String getAppId() {
		return options.getProperty("id");
	}

	/**
	 * Accessor method to retrieve auth method
	 * @return authMethod
	 * 					String authMethod
	 */
	public String getAuthMethod() {
		return options.getProperty("auth-method");
	}
	
	/**
	 * Accessor method to retrieve auth key
	 * @return authKey
	 * 					String authKey
	 */
	public String getAuthKey() {
		return options.getProperty("auth-key");
	}

	/**
	 * Accessor method to retrieve auth token
	 * @return authToken
	 * 					String authToken
	 */
	public String getAuthToken() {
		return options.getProperty("auth-token");
	}

	
	@Override
	public void connect() {
		super.connect();
	}
	
	/**
	 * Publish event, on the behalf of a device, to the IBM Internet of Things Foundation. <br> 
	 * Note that data is published
	 * at Quality of Service (QoS) 0, which means that a successful send does not guarantee
	 * receipt even if the publish is successful.
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param data
	 *            Payload data
	 * @return Whether the send was successful.
	 */
	public boolean publishEvent(String deviceType, String deviceId, String event, Object data) {
		return publishEvent(deviceType, deviceId, event, data, 0);
	}
	
	/**
	 * Publish event, on the behalf of a device, to the IBM Internet of Things Foundation. <br>
	 * This method will attempt to create a JSON obejct out of the payload
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param data
	 *            Payload data
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @return Whether the send was successful.
	 */
	public boolean publishEvent(String deviceType, String deviceId, String event, Object data, int qos) {
		if (!isConnected()) {
			return false;
		}
		final String METHOD = "publishEvent(5)";
		JsonObject payload = new JsonObject();
		
		String timestamp = ISO8601_DATE_FORMAT.format(new Date());
		payload.addProperty("ts", timestamp);
		
		JsonElement dataElement = gson.toJsonTree(data);
		payload.add("d", dataElement);
		
		String topic = "iot-2/type/" + deviceType + "/id/" + deviceId + "/evt/" + event + "/fmt/json";
		
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic   = " + topic);
		LoggerUtility.fine(CLASS_NAME, METHOD, "Payload = " + payload.toString());
		
		MqttMessage msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		msg.setQos(0);
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

	/**
	 * Publish command to the IBM Internet of Things Foundation. <br>
	 * Note that data is published
	 * at Quality of Service (QoS) 0, which means that a successful send does not guarantee
	 * receipt even if the publish is successful.
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param data
	 *            Payload data
	 * @return Whether the send was successful.
	 */
	public boolean publishCommand(String deviceType, String deviceId, String command, Object data) {
		return publishCommand(deviceType, deviceId, command, data, 0);
	}

	/**
	 * Publish command to the IBM Internet of Things Foundation. <br>
	 * This method will attempt to create a JSON obejct out of the payload
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param data
	 *            Payload data
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 * @return Whether the send was successful.
	 */
	public boolean publishCommand(String deviceType, String deviceId, String command, Object data, int qos) {
		if (!isConnected()) {
			return false;
		}
		final String METHOD = "publishCommand(5)";
		JsonObject payload = new JsonObject();
		
		String timestamp = ISO8601_DATE_FORMAT.format(new Date());
		payload.addProperty("ts", timestamp);
		
		JsonElement dataElement = gson.toJsonTree(data);
		payload.add("d", dataElement);
		
		String topic = "iot-2/type/" + deviceType + "/id/" + deviceId + "/cmd/" + command + "/fmt/json";
		
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic   = " + topic);
		LoggerUtility.fine(CLASS_NAME, METHOD, "Payload = " + payload.toString());
		
		MqttMessage msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		msg.setQos(0);
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

	/**
	 * Subscribe to device events of the IBM Internet of Things Foundation. <br>
	 * Quality of Service is set to 0
	 * All events, for the given org are subscribed to
	 */
	public void subscribeToDeviceEvents() {
		subscribeToDeviceEvents("+", "+", "+", 0);
	}

	/**
	 * Subscribe to device events of the IBM Internet of Things Foundation. <br>
	 * Quality of Service is set to 0 <br>
	 * All events, for a given device type, are subscribed to
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 */
	public void subscribeToDeviceEvents(String deviceType) {
		subscribeToDeviceEvents(deviceType, "+", "+", 0);
	}

	/**
	 * Subscribe to device events of the IBM Internet of Things Foundation. <br>
	 * Quality of Service is set to 0 <br>
	 * All events, of a given device type and device id , are subscribed to
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 */
	public void subscribeToDeviceEvents(String deviceType, String deviceId) {
		subscribeToDeviceEvents(deviceType, deviceId, "+", 0);
	}

	/**
	 * Subscribe to device events of the IBM Internet of Things Foundation. <br>
	 * Quality of Service is set to 0
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 */
	public void subscribeToDeviceEvents(String deviceType, String deviceId, String event) {
		subscribeToDeviceEvents(deviceType, deviceId, event, 0);
	}

	/**
	 * Subscribe to device events of the IBM Internet of Things Foundation. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 */
	public void subscribeToDeviceEvents(String deviceType, String deviceId, String event, int qos) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/evt/"+event+"/fmt/json";
			subscriptions.put(newTopic, new Integer(qos));
			mqttAsyncClient.subscribe(newTopic, qos);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Subscribe to device events of the IBM Internet of Things Foundation. <br>
	 * Quality of Service is set to 0
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 */

	public void subscribeToDeviceEvents(String deviceType, String deviceId, String event, String format) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/evt/"+event+"/fmt/" + format;
			subscriptions.put(newTopic, new Integer(0));
			mqttAsyncClient.subscribe(newTopic, 0);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Subscribe to device events of the IBM Internet of Things Foundation. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 */
	
	public void subscribeToDeviceEvents(String deviceType, String deviceId, String event, String format, int qos) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/evt/"+event+"/fmt/" + format;
			subscriptions.put(newTopic, new Integer(qos));
			mqttAsyncClient.subscribe(newTopic, qos);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Unsubscribe from device events of the IBM Internet of Things Foundation. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param event
	 *            object of String which denotes event
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 */
	public void unsubscribeFromDeviceEvents(String deviceType, String deviceId, String event, String format, int qos) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/evt/"+event+"/fmt/" + format;
			subscriptions.remove(newTopic);
			mqttAsyncClient.unsubscribe(newTopic);

		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Subscribe to device commands, on the behalf of a device, from the IBM Internet of Things Foundation. <br>
	 * Quality of Service is set to 0
	 * All commands, for the given org are subscribed to
	 */
	public void subscribeToDeviceCommands() {
		subscribeToDeviceCommands("+", "+", "+", 0);
	}

	/**
	 * Subscribe to device commands, on the behalf of a device, from the IBM Internet of Things Foundation. <br>
	 * Quality of Service is set to 0 <br>
	 * All commands, for a given device type, are subscribed to
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 */
	public void subscribeToDeviceCommands(String deviceType) {
		subscribeToDeviceCommands(deviceType, "+", "+", 0);
	}

		
	/**
	 * Subscribe to device commands, on the behalf of a device, from the IBM Internet of Things Foundation. <br>
	 * Quality of Service is set to 0 <br>
	 * All commands, for a given device type and device id , are subscribed to
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 */
	public void subscribeToDeviceCommands(String deviceType, String deviceId) {
		subscribeToDeviceCommands(deviceType, deviceId, "+", 0);
	}
		
	/**
	 * Subscribe to device commands, on the behalf ofa device, for the IBM Internet of Things Foundation. <br>
	 * Quality of Service is set to 0
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 */
	public void subscribeToDeviceCommands(String deviceType, String deviceId, String command) {
		subscribeToDeviceCommands(deviceType, deviceId, command, 0);
	}
	
	/**
	 * Subscribe to device commands, on the behalf of a device, of the IBM Internet of Things Foundation. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 */
	public void subscribeToDeviceCommands(String deviceType, String deviceId, String command, int qos) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/cmd/" + command + "/fmt/json";
			subscriptions.put(newTopic, new Integer(qos));
			mqttAsyncClient.subscribe(newTopic, qos);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Subscribe to device commands, on the behalf of a device, for the IBM Internet of Things Foundation. <br>
	 * Quality of Service is set to 0
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 */

	public void subscribeToDeviceCommands(String deviceType, String deviceId, String command, String format) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/cmd/" + command + "/fmt/" + format;
			subscriptions.put(newTopic, new Integer(0));
			mqttAsyncClient.subscribe(newTopic, 0);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Subscribe to device commands, on the behalf of a device, of the IBM Internet of Things Foundation. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 * @param command
	 *            object of String which denotes command
	 * @param format
	 *            object of String which denotes format, typical example of format could be json
	 * @param qos
	 *            Quality of Service, in int - can have values 0,1,2
	 */
	public void subscribeToDeviceCommands(String deviceType, String deviceId, String command, String format, int qos) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/cmd/"+ command +"/fmt/" + format;
			subscriptions.put(newTopic, new Integer(qos));			
			mqttAsyncClient.subscribe(newTopic, qos);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Subscribe to device status of the IBM Internet of Things Foundation. <br>
	 * All the devices, for an org, are monitored
	 */
	public void subscribeToDeviceStatus() {
		subscribeToDeviceStatus("+", "+");
	}

	/**
	 * Subscribe to device status of the IBM Internet of Things Foundation. <br>
	 * All the devices of a given device type are subscribed to
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 */
	public void subscribeToDeviceStatus(String deviceType) {
		subscribeToDeviceStatus(deviceType, "+");
	}
	
	/**
	 * Subscribe to device status of the IBM Internet of Things Foundation. <br>
	 * 
	 * @param deviceType
	 *            object of String which denotes deviceType 
	 * @param deviceId
	 *            object of String which denotes deviceId
	 */
	public void subscribeToDeviceStatus(String deviceType, String deviceId) {
		try {
			String newTopic = "iot-2/type/"+deviceType+"/id/"+deviceId+"/mon";
			subscriptions.put(newTopic, new Integer(0));			
			mqttAsyncClient.subscribe(newTopic, 0);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * If we lose connection trigger the connect logic to attempt to
	 * reconnect to the IBM Internet of Things Foundation.
	 */
	public void connectionLost(Throwable e) {
		final String METHOD = "connectionLost";
		LoggerUtility.info(CLASS_NAME, METHOD, "Connection lost: " + e.getMessage());
		connect();
	    Iterator<Entry<String, Integer>> iterator = subscriptions.entrySet().iterator();
	    LoggerUtility.info(CLASS_NAME, METHOD, "Resubscribing....");
	    while (iterator.hasNext()) {
	        //Map.Entry pairs = (Map.Entry)iterator.next();
	        Entry<String, Integer> pairs = iterator.next();
	        LoggerUtility.info(CLASS_NAME, METHOD, pairs.getKey() + " = " + pairs.getValue());
	        try {
	        	mqttAsyncClient.subscribe(pairs.getKey().toString(), Integer.parseInt(pairs.getValue().toString()));
			} catch (NumberFormatException | MqttException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//	        iterator.remove(); // avoids a ConcurrentModificationException
	    }
	}
	
	/**
	 * A completed deliver does not guarantee that the message is recieved by the service
	 * because devices send messages with Quality of Service (QoS) 0. The message count
	 * represents the number of messages that were sent by the device without an error on
	 * from the perspective of the device.
	 */
	public void deliveryComplete(IMqttDeliveryToken token) {
		final String METHOD = "deliveryComplete";
		LoggerUtility.fine(CLASS_NAME, METHOD, "token = "+token.getMessageId());
		messageCount++;
	}
	
	/**
	 * The Application client does not currently support subscriptions.
	 */
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		final String METHOD = "messageArrived";
		if (eventCallback != null) {
			/* Only check whether the message is a device event if a callback 
			 * has been defined for events, otherwise it is a waste of time
			 * as without a callback there is nothing to process the generated
			 * event.
			 */
			Matcher matcher = DEVICE_EVENT_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String type = matcher.group(1);
				String id = matcher.group(2);
				String event = matcher.group(3);
				String format = matcher.group(4);
				Event evt = new Event(type, id, event, format, msg);

				if(evt.getTimestamp() != null) {
					LoggerUtility.fine(CLASS_NAME, METHOD, "Event received: " + evt.toString());
					eventCallback.processEvent(evt);					
				} else {
					LoggerUtility.warn(CLASS_NAME, METHOD, "Event is not formatted properly, so not processing");						
				}

				return;
		    }

			matcher = DEVICE_COMMAND_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String type = matcher.group(1);
				String id = matcher.group(2);
				String command = matcher.group(3);
				String format = matcher.group(4);
				Command cmd = new Command(type, id, command, format, msg);
			
				if(cmd.getTimestamp() != null ) {
					LoggerUtility.fine(CLASS_NAME, METHOD, "Command received: " + cmd.toString());	
					eventCallback.processCommand(cmd);					
				} else {
					LoggerUtility.warn(CLASS_NAME, METHOD, "Command is not formatted properly, so not processing");					
				}

				return;
		    }

		}
		
		if (statusCallback != null) {
			/* Only check whether the message is a status event if a callback 
			 * has been defined for status events, otherwise it is a waste of time
			 * as without a callback there is nothing to process the generated
			 * event.
			 */
			Matcher matcher = DEVICE_STATUS_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String type = matcher.group(1);
				String id = matcher.group(2);
				DeviceStatus status = new DeviceStatus(type, id, msg);
				LoggerUtility.fine(CLASS_NAME, METHOD, "Device status received: " + status.toString());
				statusCallback.processDeviceStatus(status);
		    }
			
			matcher = APP_STATUS_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String id = matcher.group(1);
				ApplicationStatus status = new ApplicationStatus(id, msg);
				LoggerUtility.fine(CLASS_NAME, METHOD, "Application status received: " + status.toString());
				statusCallback.processApplicationStatus(status);
		    }
		}
	}


	public void setEventCallback(EventCallback callback) {
		this.eventCallback  = callback;
	}

	public void setStatusCallback(StatusCallback callback) {
		this.statusCallback  = callback;
	}
}
