/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Added resource model
 *****************************************************************************
 *
 */
package com.ibm.internal.iotf.devicemgmt.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.internal.iotf.devicemgmt.ManagedClient;
import com.ibm.internal.iotf.devicemgmt.DMServerTopic;

import com.ibm.internal.iotf.devicemgmt.ResponseCode;

/**
 * Request handler for <code>MMqttClient.SERVER_TOPIC_CANCEL</code>
 * <br>Expected request message format
 * <blockquote>
 * 
 * {
 * "d": {
 *      "data": [
 *          {
 *       	"field": "mgmt.firmware"
 *       }
 *       ]
 *  }
 * }
 * </blockquote>
 */
public class CancelRequestHandler extends DMRequestHandler {

	public CancelRequestHandler(ManagedClient dmClient) {
		setDMClient(dmClient);
		
	}

	/**
	 * This method handles the cancel request messages from IBM Watson IoT Platform
	 */
	@Override
	protected void handleRequest(JsonObject jsonRequest) {
		JsonArray fields;
		JsonObject d = (JsonObject)jsonRequest.get("d");
		if (d != null) {
			fields = (JsonArray)d.get("data");
			if (fields != null) {
				ObserveRequestHandler observe = getObserveRequestHandler(getDMClient());
				if (observe != null) {
					observe.cancel(fields);
				}
			}
		}
		
		JsonObject response = new JsonObject();
		response.add("reqId", jsonRequest.get("reqId"));
		response.add("rc", new JsonPrimitive(ResponseCode.DM_SUCCESS.getCode()));
		respond(response);
	}
	
	/**
	 * Return the cancel topic
	 */
	@Override
	protected String getTopic() {
		DMServerTopic topic = this.getDMClient().getDMServerTopic();
		return topic.getCancelTopic();
	}

}
