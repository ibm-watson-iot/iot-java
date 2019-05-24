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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.wiotp.sdk.devicemgmt.DeviceAction;
import com.ibm.wiotp.sdk.devicemgmt.DeviceActionHandler;
import com.ibm.wiotp.sdk.devicemgmt.internal.ConcreteDeviceAction;
import com.ibm.wiotp.sdk.devicemgmt.internal.DMServerTopic;
import com.ibm.wiotp.sdk.devicemgmt.internal.ManagedClient;
import com.ibm.wiotp.sdk.devicemgmt.internal.ResponseCode;


/**
 * Request handler for <code>MMqttClient.SERVER_TOPIC_INITIATE_FACTORY_RESET</code>
 * <br>Expected request message format
 * <blockquote>
 * {
 * 	"reqId": "string"
 * }
 * </blockquote>
 */	
public class FactoryResetRequestHandler extends DMRequestHandler implements PropertyChangeListener {

	private static final Logger LOG = LoggerFactory.getLogger(FactoryResetRequestHandler.class);
	private static final String REQ_ID = "reqId";
	
	private JsonElement reqId;

	public FactoryResetRequestHandler(ManagedClient dmClient) {
		setDMClient(dmClient);
		
	}
	
	/**
	 * return initiate factory reset topic
	 */
	@Override
	protected String getTopic() {
		DMServerTopic topic = this.getDMClient().getDMServerTopic();
		return topic.getInitiateFactoryReset();
	}
	
	/**
	 * Handle the initiate factory reset messages from IBM Watson IoT Platform 
	 */
	@Override
	protected void handleRequest(JsonObject jsonRequest, String topic) {
		DeviceAction action = getDMClient().getDeviceData().getDeviceAction();
		if (action == null || getDMClient().getActionHandler() == null) {
			JsonObject response = new JsonObject();
			response.add(REQ_ID, jsonRequest.get(REQ_ID));
			response.add("rc", new JsonPrimitive(ResponseCode.DM_FUNCTION_NOT_IMPLEMENTED.getCode()));
			respond(response);
		} else {
			LOG.debug("Start Factory Reset action ");
			// remove any other listener that are listening for the status update
			((ConcreteDeviceAction)action).clearListener();
			((ConcreteDeviceAction)action).addPropertyChangeListener(this);
			this.reqId = jsonRequest.get(REQ_ID);
			DeviceActionHandler handler = getDMClient().getActionHandler();
			handler.handleFactoryReset(action);
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
				LOG.warn(e.getMessage());
			}
		}
	}
}
