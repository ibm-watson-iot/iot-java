/**
 *****************************************************************************
 Copyright (c) 2015-19 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 *
 */
package com.ibm.wiotp.sdk.devicemgmt.internal.handler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.wiotp.sdk.devicemgmt.DeviceAction;
import com.ibm.wiotp.sdk.devicemgmt.DeviceActionHandler;
import com.ibm.wiotp.sdk.devicemgmt.internal.ConcreteDeviceAction;
import com.ibm.wiotp.sdk.devicemgmt.internal.DMServerTopic;
import com.ibm.wiotp.sdk.devicemgmt.internal.ManagedClient;
import com.ibm.wiotp.sdk.devicemgmt.internal.ResponseCode;
import com.ibm.wiotp.sdk.util.LoggerUtility;

/**
 * Request handler for <code>MMqttClient.SERVER_TOPIC_INITIATE_REBOOT</code>
 * <br>Expected request message format
 * <blockquote>
 * {
 * 	"reqId": "string"
 * }
 * </blockquote>
 */
public class RebootRequestHandler extends DMRequestHandler implements PropertyChangeListener {

	private static final String REQ_ID = "reqId";
	private JsonElement reqId;

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
	 * Handle initiate reboot request from IBM Watson IoT Platform
	 */
	@Override
	protected void handleRequest(JsonObject jsonRequest, String topic) {
		final String METHOD = "handleRequest";
		
		DeviceAction action = getDMClient().getDeviceData().getDeviceAction();
		if (action == null || getDMClient().getActionHandler() == null) {
			// this should never happen
			JsonObject response = new JsonObject();
			response.add(REQ_ID, jsonRequest.get(REQ_ID));
			response.add("rc", new JsonPrimitive(ResponseCode.DM_FUNCTION_NOT_IMPLEMENTED.getCode()));
			respond(response);
		} else {
			LoggerUtility.fine(CLASS_NAME, METHOD, " start reboot action ");
			// remove any other listener that are listening for the status update
			((ConcreteDeviceAction)action).clearListener();
			((ConcreteDeviceAction)action).addPropertyChangeListener(this);
			this.reqId = jsonRequest.get(REQ_ID);
			DeviceActionHandler handler = getDMClient().getActionHandler();
			handler.handleReboot(action);
		} 
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(ConcreteDeviceAction.DEVICE_ACTION_STATUS_UPDATE.equals(evt.getPropertyName())) {
			try {
				ConcreteDeviceAction action = (ConcreteDeviceAction) evt.getNewValue();
				JsonObject response = action.toJsonObject();
				response.add(REQ_ID, reqId);
				respond(response);
			} catch(Exception e) {
				LoggerUtility.warn(CLASS_NAME, "propertyChange", e.getMessage());
			}
		}
	}

}
