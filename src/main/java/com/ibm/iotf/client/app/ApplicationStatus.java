/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.app;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Class that handles application status, of applications using IBM Watson IoT Platform
 */
public class ApplicationStatus extends Status {

	private String id;
	
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

	public String getId() {
		return id;
	}

}
