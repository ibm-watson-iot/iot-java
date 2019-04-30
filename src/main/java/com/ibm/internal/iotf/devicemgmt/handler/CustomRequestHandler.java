/**
 *****************************************************************************
 Copyright (c) 2017-19 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 *
 */
package com.ibm.internal.iotf.devicemgmt.handler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


//import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.internal.iotf.devicemgmt.ConcreteCustomAction;
import com.ibm.internal.iotf.devicemgmt.ConcreteDeviceAction;
import com.ibm.internal.iotf.devicemgmt.ManagedClient;
import com.ibm.internal.iotf.devicemgmt.DMServerTopic;
import com.ibm.iotf.client.CustomAction;
import com.ibm.iotf.devicemgmt.CustomActionHandler;
import com.ibm.internal.iotf.devicemgmt.ResponseCode;
import com.ibm.iotf.util.LoggerUtility;

/**
 * Request handler for <code>MMqttClient.SERVER_TOPIC_INITIATE_CUSTOM</code>
 * <br>Expected request message format
 * <blockquote>
 * {
 * 	"reqId": "string"
 * }
 * </blockquote>
 */
public class CustomRequestHandler extends DMRequestHandler implements PropertyChangeListener {

	private static final String REQ_ID = "reqId";
	//private JsonElement reqId;

	public CustomRequestHandler(ManagedClient dmClient) {
		setDMClient(dmClient);
	}
	
	/**
	 * Return Initiate custom action topic
	 */
	@Override
	protected String getTopic() {
		DMServerTopic topic = this.getDMClient().getDMServerTopic();
		return topic.getInitiateCustomAction();
	}
	
	/**
	 * Handle initiate custom action request from IBM Watson IoT Platform
	 */
	@Override
	protected void handleRequest(JsonObject jsonRequest, String topic) {
		final String METHOD = "handleRequest";
		
		ConcreteCustomAction action = (ConcreteCustomAction) getDMClient().getDeviceData().getCustomAction();
		if (action == null || getDMClient().getCustomActionHandler() == null) {
			// this should never happen
			JsonObject response = new JsonObject();
			response.add(REQ_ID, jsonRequest.get(REQ_ID));
			response.add("rc", new JsonPrimitive(ResponseCode.DM_FUNCTION_NOT_IMPLEMENTED.getCode()));
			respond(response);
		} else {
			LoggerUtility.fine(CLASS_NAME, METHOD, " start custom action ");
			// remove any other listener that are listening for the status update
			
			// iotdm-1/mgmt/custom/bundleId/actionId
			// iotdm-1/type/typeId/id/deviceId/mgmt/custom/bundleId/actionId
			String customActionSubstring = topic.substring(topic.indexOf("mgmt/custom/"));
			String[] customActionParts = customActionSubstring.split("/");
			String bundleId = customActionParts[2];
			String actionId = customActionParts[3];
			action.setBundleId(bundleId);
			action.setActionId(actionId);
			action.setReqId(jsonRequest.get(REQ_ID).getAsString());
			action.setPayload(jsonRequest);

			((ConcreteCustomAction)action).clearListener();
			((ConcreteCustomAction)action).addPropertyChangeListener(this);
			CustomActionHandler handler = getDMClient().getCustomActionHandler();
			handler.handleCustomAction(action);
		} 
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(ConcreteDeviceAction.DEVICE_ACTION_STATUS_UPDATE.equals(evt.getPropertyName())) {
			try {
				ConcreteCustomAction action = (ConcreteCustomAction) evt.getNewValue();
				JsonObject response = action.toJsonObject();
				//response.add(REQ_ID, reqId);
				respond(response);
			} catch(Exception e) {
				LoggerUtility.warn(CLASS_NAME, "propertyChange", e.getMessage());
			}
		}
	}

}
