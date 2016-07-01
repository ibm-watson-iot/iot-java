/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Added Resource Model
 *****************************************************************************
 *
 */
package com.ibm.internal.iotf.devicemgmt.handler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.internal.iotf.devicemgmt.ManagedClient;
import com.ibm.internal.iotf.devicemgmt.DMServerTopic;

import com.ibm.internal.iotf.devicemgmt.ResponseCode;
import com.ibm.iotf.devicemgmt.resource.Resource;
import com.ibm.iotf.util.LoggerUtility;

/**
 * Request handler for <code>MMqttClient.SERVER_TOPIC_OBSERVE</code>
 * <br>Expected request message format
 * <blockquote>
 * {
 * 	"d": {
 * 		"fields": [ "string" ]
 * 	},
 * 	"reqId": "string"
 * }
 * </blockquote>
 */
public class ObserveRequestHandler extends DMRequestHandler implements PropertyChangeListener {

	private static final String FIELDS = "fields";
	private static final String FIELD = "field";
	
	private ConcurrentHashMap<String, Resource> fieldsMap = new ConcurrentHashMap<String, Resource>();
	private ConcurrentHashMap<String, JsonElement> responseMap = new ConcurrentHashMap<String, JsonElement>();
	
	public ObserveRequestHandler(ManagedClient dmClient) {
		setDMClient(dmClient);
	}
	
	/**
	 * Return the observe topic
	 */
	@Override
	protected String getTopic() {
		DMServerTopic topic = this.getDMClient().getDMServerTopic();
		return topic.getObserveTopic();
	}
	
	/**
	 * Handles the observe request from IBM Watson IoT Platform
	 */
	@Override
	protected void handleRequest(JsonObject jsonRequest) {
		JsonObject response = new JsonObject();
		JsonArray responseArray = new JsonArray();
		JsonObject d = (JsonObject) jsonRequest.get("d");
		JsonArray fields = (JsonArray) d.get(FIELDS);
		for (int i=0; i < fields.size(); i++) {
			JsonObject field = fields.get(i).getAsJsonObject();
			String name = field.get(FIELD).getAsString();
			JsonObject fieldResponse = new JsonObject();
			Resource resource = getDMClient().getDeviceData().getResource(name);
			if(resource != null) {
				resource.addPropertyChangeListener(Resource.ChangeListenerType.INTERNAL, this);
				fieldsMap.put(name, resource);
			}
			fieldResponse.addProperty(FIELD, name);
			JsonElement value = resource.toJsonObject();
			fieldResponse.add("value", value);
			responseMap.put(name, value);
			responseArray.add(fieldResponse);
		}
	
		JsonObject responseFileds = new JsonObject();
		responseFileds.add(FIELDS, responseArray);
		response.add("d", responseFileds);
		response.add("reqId", jsonRequest.get("reqId"));
		response.add("rc", new JsonPrimitive(ResponseCode.DM_SUCCESS.getCode()));
		respond(response);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		Resource resource = (Resource) evt.getNewValue();
		// check if there is an observe relation established
		resource = fieldsMap.get(evt.getPropertyName());
		if(resource != null) {
			JsonObject response = new JsonObject();
			JsonObject field = new JsonObject();
			field.add(FIELD, new JsonPrimitive(resource.getCanonicalName()));
			JsonElement value = resource.toJsonObject();
			JsonElement trimedValue = trimResponse(evt.getPropertyName(), value);
			if(null == trimedValue) {
				return;
			}
			field.add("value", trimedValue);
			JsonObject fields = new JsonObject();
			JsonArray fieldsArray = new JsonArray();
			fieldsArray.add(field);
			fields.add(FIELDS, fieldsArray);
			response.add("d", fields);
			notify(response);
		}
	}

	void cancel(JsonArray fields) {
		final String METHOD = "cancel";
		LoggerUtility.fine(CLASS_NAME, METHOD,  "Cancel observation for " + fields);
		for (int i=0; i < fields.size(); i++) {
			JsonObject obj = (JsonObject)fields.get(i);
			String name = obj.get(FIELD).getAsString();
			Resource resource = fieldsMap.remove(name);
			if(null != resource) {
				resource.removePropertyChangeListener(this);
				responseMap.remove(name);
			}
		}
	}
	
	private JsonElement trimResponse(String name, JsonElement newValue) {
		JsonElement previousValue = responseMap.get(name);
		if(previousValue == null) {
			return null;
		}
		
		if(previousValue.isJsonPrimitive()) {
			if(previousValue.equals(newValue)) {
				return null;
			} else {
				return newValue;
			}
		}

		boolean bModified = false;
		JsonObject response = new JsonObject();
		for (Map.Entry<String, JsonElement> en : ((JsonObject)previousValue).entrySet()) {
			String key = en.getKey();
			JsonElement value = en.getValue();
			JsonElement otherValue = ((JsonObject) newValue).get(key);
			if (otherValue == null || !otherValue.equals(value)) {
				response.add(key, otherValue);
				en.setValue(otherValue);
				bModified = true;
	        }
	    }
		
		if(bModified) {
			return response;
		} else {
			return null;
		}
	}
}
