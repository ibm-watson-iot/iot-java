package com.ibm.iotf.devicemgmt.internal;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.DeviceData;

/**
 * A managed client interface that provides the contract what a managed device or Gateway
 * must implement, inorder to participate in DM activities. 
 *
 */
public interface ManagedClient {

	public void subscribe(String topic, int qos,
			IMqttMessageListener iMqttMessageListener) throws MqttException;

	public void unsubscribe(String topic) throws MqttException;

	public void publish(String response, JsonObject payload, int qos) throws MqttException;

	public DeviceData getDeviceData();

	public void subscribe(String[] topics, int[] qos,
			IMqttMessageListener[] listener) throws MqttException;

	public void unsubscribe(String[] topics) throws MqttException;

	public DMAgentTopic getDMAgentTopic();

	public DMServerTopic getDMServerTopic();

}
