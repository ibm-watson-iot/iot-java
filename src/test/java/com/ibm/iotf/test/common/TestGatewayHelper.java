package com.ibm.iotf.test.common;

import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.gateway.GatewayClient;
import com.ibm.iotf.util.LoggerUtility;

public class TestGatewayHelper extends TestApplicationHelper {

	String gwDevType = null;
	String gwDevId = null;
	String devType = null;
	String devId = null;
	
	GatewayClient gwClient = null;
	TestGatewayCallback callback = null;
	
	public TestGatewayHelper(String gwDevType, String gwDevId, String devType, String devId) throws Exception {
		super(null);
		this.gwDevType = gwDevType;
		this.gwDevId = gwDevId;
		this.devType = devType;
		this.devId = devId;
		Properties props = TestEnv.getGatewayProperties(gwDevType, gwDevId);
		gwClient = new GatewayClient(props);
		callback = new TestGatewayCallback();
		gwClient.setGatewayCallback(callback);
	}
	
	public String getGatewayDeviceType() { return gwDevType; }
	public String getGatewayDeviceId() { return gwDevId; }
	public String getAttachedDeviceType() { return devType; }
	public String getAttachedDeviceId() { return devId; }
	
	public void connect() throws MqttException {
		final String METHOD = "connectGateway";
		gwClient.connect();
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " connected ? " + gwClient.isConnected());
	}
	
	public void disconnect() {
		final String METHOD = "disconnectGateway";
		gwClient.disconnect();
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " connected ? " + gwClient.isConnected());
	}
	
	public String getClientID() {
		return gwClient.getClientID();
	}
	
	public boolean commandReceived() {
		return callback.commandReceived();
	}
	
	public boolean notificationReceived() {
		return callback.notificationReceived();
	}
	
	public void clear() {
		callback.clear();
	}
	
	public void subscribeNotification() {
		final String METHOD = "subscribeNotification";
		gwClient.subscribeToGatewayNotification();
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " subscribed to notification");
	}
	
	public void subscribeCommands() {
		final String METHOD = "subscribeCommands";
		gwClient.subscribeToDeviceCommands(devType, devId);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " subscribed to all commands.");
	}
	
	public void subscribeCommand(String command) {
		final String METHOD = "subscribeCommands";
		gwClient.subscribeToDeviceCommands(devType, devId, command);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " subscribed to command " + command);
	}

	public void subscribeCommand(String command, int qos) {
		final String METHOD = "subscribeCommand";
		gwClient.subscribeToDeviceCommands(devType, devId, command, qos);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " subscribed to command " + command + " and QoS " + qos);
	}
	
	public void subscribeCommand(String command, String format) {
		final String METHOD = "subscribeCommand";
		gwClient.subscribeToDeviceCommands(devType, devId, command, format);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " subscribed to command " + command + " and format " + format);
	}

	public void subscribeCommand(String command, String format, int qos) {
		final String METHOD = "subscribeCommand";
		gwClient.subscribeToDeviceCommands(devType, devId, command, format, qos);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " subscribed to command " + command + " and format " + format + " and QoS " + qos);
	}

	public void publishEvent(String devType, String devId, String event, JsonObject jsonData) {
		final String METHOD = "publishEvent";
		gwClient.publishDeviceEvent(devType, devId, event, jsonData);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " publish event to device type " + devType + " device ID " + devId + " Event " + event);
	}

	public void unsubscribeCommands() {
		final String METHOD = "unsubscribeCommands";
		gwClient.unsubscribeFromDeviceCommands(devType, devId);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " unsubscribed from all commands");
	}

	public void unsubscribeCommand(String command) {
		final String METHOD = "unsubscribeCommand";
		gwClient.unsubscribeFromDeviceCommands(devType, devId, command);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " unsubscribed from command " + command);
	}

	public void unsubscribeCommand(String command, String format) {
		final String METHOD = "unsubscribeCommand";
		gwClient.unsubscribeFromDeviceCommands(devType, devId, command, format);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " unsubscribed from command " + command + " and format " + format);
	}


}
