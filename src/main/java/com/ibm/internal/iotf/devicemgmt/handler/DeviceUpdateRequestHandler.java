/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Added resource model to update based on resource
 *****************************************************************************
 *
 */
package com.ibm.internal.iotf.devicemgmt.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.internal.iotf.devicemgmt.ManagedClient;
import com.ibm.internal.iotf.devicemgmt.DMServerTopic;
import com.ibm.internal.iotf.devicemgmt.ResponseCode;
import com.ibm.iotf.devicemgmt.resource.Resource;
import com.ibm.iotf.util.LoggerUtility;

/**
 * 
 * <br>Update device attributes
 * <br>Watson IoT Platform can send this request to a device to update values of one or more device attributes. 
 * Supported update targets are location, metadata, device information and firmware.
 * <p>Topic
 *	<br>iotdm-1/device/update
 * <br><br>Message format
 * <br>Request:
 * <br>{
 * <br>  "d": {
 * <br>      "value": { }
 * <br>  },
 * <br>  "reqId": "string"
 * <br>}
 * <br><br>"value" is the new value of the device attribute. 
 * <br>It is a complex field matching the device model. 
 * <br>Only writeable fields should be updated as a result of this operation.
 * <br>Values can be updated in:
 * <ul class="simple">
 *   <li>location		(see Update location section for details)
 *   <li>metadata		(Optional)
 *   <li>deviceInfo		(Optional)
 *   <li>mgmt.firmware	(see Firmware update process for details)
 * </ul>
 * <br><br>Response:
 * <br>{
 *    <br>"rc": number,
 *    <br>"message": "string",
 *    <br>"d": {
 *       <br>"fields": [
 *          <br> "string"
 *       <br>]
 *   <br>},
 *   <br>"reqId": "string"
 * <br>}
 * <br><br>"message" field can be specified if "rc" is not 200.
 * <br>If any field value could not be retrieved, 
 * <br>"rc" should be set to 404 (if not found) or 500 (any other reason). 
 * <br>"fields" array should contain the name of each field that could not be updated.
 *
 */
public class DeviceUpdateRequestHandler extends DMRequestHandler {
	private static final String CLASS_NAME = DeviceUpdateRequestHandler.class.getName();
	
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	
	public DeviceUpdateRequestHandler(ManagedClient dmClient) {
		setDMClient(dmClient);
	}
	
	/**
	 * Returns the update topic
	 */
	@Override
	protected String getTopic() {
		DMServerTopic topic = this.getDMClient().getDMServerTopic();
		return topic.getDeviceUpdateTopic();
	}

	/**
	 * This method handles all the update requests from IBM Watson IoT Platform
	 */
	@Override
	public void handleRequest(JsonObject jsonRequest) {
		final String METHOD = "handleRequest";
		List<Resource> fireRequiredResources = new ArrayList<Resource>();
		JsonArray fields;
		ResponseCode rc = ResponseCode.DM_UPDATE_SUCCESS;
		JsonObject response = new JsonObject();
		JsonObject d = (JsonObject)jsonRequest.get("d");
		
		if (d != null) {
			fields = (JsonArray)d.get("fields");
			if (fields != null) {
				
				/**
				 * update the fields in actual device object
				 */
				JsonArray resFields = new JsonArray();
				for (int i=0; i < fields.size(); i++) {
					JsonObject obj = (JsonObject)fields.get(i);
					if (obj.get("field") != null) {
						String key = obj.get("field").getAsString();
						JsonObject value = (JsonObject)obj.get("value");
						boolean success = false;
						try {
							Resource resource = getDMClient().getDeviceData().getResource(key);
							if(resource != null) {
								success = updateField(resource, value);
								fireRequiredResources.add(resource);
							}
						} catch(Exception e) {
							LoggerUtility.log(Level.SEVERE, CLASS_NAME, METHOD, 
									"Exception in updating field "+key +
									" value "+value, e);
							
							if(e.getMessage() != null)
								response.add("message", new JsonPrimitive(e.getMessage()));
						}
						if(success == false) {
							resFields.add(new JsonPrimitive(key));
							rc = ResponseCode.DM_NOT_FOUND;
						}
					}
				}
				
				if(resFields.size() != 0) {
					JsonObject json = new JsonObject();
					json.add("fields", resFields);
					response.add("d",  json);
				}
			}
		}
		
		response.add("rc", new JsonPrimitive(rc.getCode()) );
		response.add("reqId", jsonRequest.get("reqId"));
		respond(response);	
		
		// Lets fire the property change event now - this will notify the
		// device code if they are listening to
		Task task = this.new Task(fireRequiredResources);
		executor.execute(task);
	}
	
	/**
	 * Update resource with new value
	 * 
	 * @param resource resource to be updated
	 * @param value - the new value
	 * @return - true if the update is successful, false if not
	 */
	private boolean updateField(Resource resource, JsonObject value) {
		if(resource != null) {
			// Update the properties but do not fire the change event
			resource.update(value, false);
			return true;
		}
		return false;
	}
	
	/**
	 * A task that fires the modified event on all the
	 * resources that are updated
	 */
	private class Task implements Runnable {
		private List<Resource> fireRequiredResources;
		
		private Task(List<Resource> fireRequiredResources) {
			this.fireRequiredResources = fireRequiredResources;
		}

		@Override
		public void run() {
			for(int i = 0; i < fireRequiredResources.size(); i++) {
				Resource resource = fireRequiredResources.get(i);
				resource.notifyExternalListeners();
			}
		}
		
	}
}
