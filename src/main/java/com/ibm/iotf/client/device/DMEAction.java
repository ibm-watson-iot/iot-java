/**
 *****************************************************************************
 Copyright (c) 2017 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.device;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.Message;

/**
 * A Class that represents the Device Management Extension Action
 *
 */

public class DMEAction {

	private String bundleId;
	private String actionId;
	private JsonObject payload;

	/**
	 * @param bundleId
	 * 			Unique identifier for a device management extension
	 * @param actionId
	 * 			Unique identifier to refer an action within the bundle
	 * @param payload
	 * 			 Actual payload containing the parameters to complete the action
	 * 
	 * 
	 */	
	public DMEAction(String bundleId, String actionId, JsonObject payload) {
		this.bundleId = bundleId;
		this.actionId = actionId;
		this.payload = payload;
	}
	
	/**
	 * Returns the DME bundleId
	 * @return bundleId
	 */
	public String getBundleId() {
		return this.bundleId;
	}

	/**
	 * Returns the DME Action Id
	 * @return actionId
	 */
	public String getActionId() {
		return this.actionId;
	}

	/**
	 * Returns the DME action payload
	 * @return payload
	 */
	public JsonObject getPayload() {
		return this.payload;
	}
	
	/**
	 * Returns the reqId of the action request from the server
	 * @return reqId
	 */
	public String getRequestId() {
		try {
			String fields = payload.get("reqId").getAsString();
			return fields;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
