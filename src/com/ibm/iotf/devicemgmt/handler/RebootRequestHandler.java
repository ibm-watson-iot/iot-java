/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.internal.ManagedClient;
import com.ibm.iotf.devicemgmt.internal.DMServerTopic;
import com.ibm.iotf.devicemgmt.DeviceAction;
import com.ibm.iotf.devicemgmt.internal.ResponseCode;
import com.ibm.iotf.util.LoggerUtility;

/**
 * Request handler for <code>MMqttClient.SERVER_TOPIC_INITIATE_REBOOT</code>
 * <br>Expected request message format
 * <blockquote>
 * {
 * 	"reqId": "string"
 * }
 */
public class RebootRequestHandler extends DMRequestHandler {

	public RebootRequestHandler(ManagedClient dmClient) {
		setDMClient(dmClient);
	}
	
	/**
	 * Return Initiate reboot topic
	 */
	@Override
	protected String getTopic() {
		DMServerTopic topic = this.getDMClient().getDMServerTopic();
		return topic.getInitiateRebootTopic();
	}
	
	/**
	 * Subscribe to Initiate reboot topic
	 */
	@Override
	protected void subscribe() {
		DMServerTopic topic = this.getDMClient().getDMServerTopic();
		subscribe(topic.getInitiateRebootTopic());
	}

	/**
	 * unsubscribe Initiate reboot topic
	 */
	@Override
	protected void unsubscribe() {
		DMServerTopic topic = this.getDMClient().getDMServerTopic();
		unsubscribe(topic.getInitiateRebootTopic());
	}

	/**
	 * Handle initiate reboot request from IBM IoT Foundation
	 */
	@Override
	protected void handleRequest(JsonObject jsonRequest) {
		final String METHOD = "handleRequest";
		ResponseCode rc = ResponseCode.DM_ACCEPTED;
		
		JsonObject response = new JsonObject();
		response.add("reqId", jsonRequest.get("reqId"));
		DeviceAction action = getDMClient().getDeviceData().getDeviceAction();
		if (action == null) {
			rc = ResponseCode.DM_FUNCTION_NOT_IMPLEMENTED;
		} else {
			LoggerUtility.fine(CLASS_NAME, METHOD, " fire event(" 
					+ DeviceAction.DEVICE_REBOOT_START + ")" );
				
			action.fireEvent(DeviceAction.DEVICE_REBOOT_START);
			rc = ResponseCode.DM_ACCEPTED;
		} 
		response.add("rc", new JsonPrimitive(rc.getCode()));
		respond(response);
	}

}
