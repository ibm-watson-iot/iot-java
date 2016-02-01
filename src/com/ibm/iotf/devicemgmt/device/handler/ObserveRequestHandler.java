/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
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
package com.ibm.iotf.devicemgmt.device.handler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.devicemgmt.device.internal.ResponseCode;
import com.ibm.iotf.devicemgmt.device.internal.ServerTopic;
import com.ibm.iotf.devicemgmt.device.resource.Resource;
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
 */
public class ObserveRequestHandler extends DMRequestHandler implements PropertyChangeListener {

	private ConcurrentHashMap<String, Resource> fieldsMap = new ConcurrentHashMap<String, Resource>();
	private ConcurrentHashMap<String, JsonElement> responseMap = new ConcurrentHashMap<String, JsonElement>();
	
	public ObserveRequestHandler(ManagedDevice dmClient) {
		setDMClient(dmClient);
	}
	
	/**
	 * Return the observe topic
	 */
	@Override
	protected ServerTopic getTopic() {
		return ServerTopic.OBSERVE;
	}
	
	/**
	 * subscribe to observe topic
	 */
	@Override
	protected void subscribe() {
		subscribe(ServerTopic.OBSERVE);
	}

	/**
	 * unsubscribe to observe topic
	 */
	@Override
	protected void unsubscribe() {
		unsubscribe(ServerTopic.OBSERVE);
	}

	/**
	 * Handles the observe request from IBM IoT Foundation
	 */
	@Override
	protected void handleRequest(JsonObject jsonRequest) {
		JsonObject response = new JsonObject();
		JsonArray responseArray = new JsonArray();
		JsonObject d = (JsonObject) jsonRequest.get("d");
		JsonArray fields = (JsonArray) d.get("fields");
		for (int i=0; i < fields.size(); i++) {
			JsonObject field = fields.get(i).getAsJsonObject();
			String name = field.get("field").getAsString();
			JsonObject fieldResponse = new JsonObject();
			Resource resource = getDMClient().getDeviceData().getResource(name);
			if(resource != null) {
				resource.addPropertyChangeListener(Resource.ChangeListenerType.INTERNAL, this);
				fieldsMap.put(name, resource);
			}
			fieldResponse.addProperty("field", name);
			JsonElement value = resource.toJsonObject();
			fieldResponse.add("value", value);
			responseMap.put(name, value);
			responseArray.add(fieldResponse);
		}
	
		JsonObject responseFileds = new JsonObject();
		responseFileds.add("fields", responseArray);
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
			field.add("field", new JsonPrimitive(resource.getCanonicalName()));
			JsonElement value = resource.toJsonObject();
			JsonElement trimedValue = trimResponse(evt.getPropertyName(), value);
			if(null == trimedValue) {
				return;
			}
			field.add("value", trimedValue);
			JsonObject fields = new JsonObject();
			JsonArray fieldsArray = new JsonArray();
			fieldsArray.add(field);
			fields.add("fields", fieldsArray);
			response.add("d", fields);
			notify(response);
		}
	}

	void cancel(JsonArray fields) {
		final String METHOD = "cancel";
		LoggerUtility.fine(CLASS_NAME, METHOD,  "Cancel observation for " + fields);
		for (int i=0; i < fields.size(); i++) {
			JsonObject obj = (JsonObject)fields.get(i);
			String name = obj.get("field").getAsString();
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
