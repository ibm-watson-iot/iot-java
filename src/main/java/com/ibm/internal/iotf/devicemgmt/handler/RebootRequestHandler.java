/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
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
package com.ibm.internal.iotf.devicemgmt.handler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.internal.iotf.devicemgmt.ConcreteDeviceAction;
import com.ibm.internal.iotf.devicemgmt.ManagedClient;
import com.ibm.internal.iotf.devicemgmt.DMServerTopic;
import com.ibm.iotf.devicemgmt.DeviceAction;
import com.ibm.iotf.devicemgmt.DeviceActionHandler;
import com.ibm.internal.iotf.devicemgmt.ResponseCode;
import com.ibm.iotf.devicemgmt.resource.Resource;
import com.ibm.iotf.devicemgmt.resource.Resource.ChangeListenerType;
import com.ibm.iotf.util.LoggerUtility;

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
	protected void handleRequest(JsonObject jsonRequest) {
		final String METHOD = "handleRequest";
		
		DeviceAction action = getDMClient().getDeviceData().getDeviceAction();
		if (action == null || getDMClient().getActionHandler() == null) {
			// this should never happen
			JsonObject response = new JsonObject();
			response.add("reqId", jsonRequest.get("reqId"));
			response.add("rc", new JsonPrimitive(ResponseCode.DM_FUNCTION_NOT_IMPLEMENTED.getCode()));
			respond(response);
		} else {
			LoggerUtility.fine(CLASS_NAME, METHOD, " start reboot action ");
			// remove any other listener that are listening for the status update
			((ConcreteDeviceAction)action).clearListener();
			((ConcreteDeviceAction)action).addPropertyChangeListener(this);
			this.reqId = jsonRequest.get("reqId");
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
				response.add("reqId", reqId);
				respond(response);
			} catch(Exception e) {
				LoggerUtility.warn(CLASS_NAME, "propertyChange", e.getMessage());
			}
		}
	}

}
