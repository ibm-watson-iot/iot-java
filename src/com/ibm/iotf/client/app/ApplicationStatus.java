package com.ibm.iotf.client.app;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Class that handles application status, of applications using IBM Internet of Things Foundation
 */
public class ApplicationStatus extends Status{

	public String id;
	
	/**
	 * Maintains the status of application
	 * @param id
	 * 					An object of the class String which denotes the appId
	 * @param msg
	 * 					An object of the class MqttMessage
	 * @see <a href="Paho Client Library">http://www.eclipse.org/paho/files/javadoc/index.html</a> 
	 * @throws UnsupportedEncodingException 
	 */	
	public ApplicationStatus(String id, MqttMessage msg) throws UnsupportedEncodingException {
		super(msg);
		this.id = id;
	}

}
