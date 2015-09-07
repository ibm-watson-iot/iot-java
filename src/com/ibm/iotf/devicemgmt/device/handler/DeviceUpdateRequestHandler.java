/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
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
package com.ibm.iotf.devicemgmt.device.handler;

import java.util.logging.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.device.DeviceAction;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.devicemgmt.device.ResponseCode;
import com.ibm.iotf.devicemgmt.device.resource.Resource;
import com.ibm.iotf.util.LoggerUtility;

/**
 * 
 * <br>Update device attributes
 * <br>IoTF can send this request to a device to update values of one or more device attributes. 
 * Supported update targets are location, metadata, device information and firmware.
 * <p>Topic
 *	<li>iotdm-1/device/update
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
 *   <li>location		(see Update location section for details)
 *   <li>metadata		(Optional)
 *   <li>deviceInfo		(Optional)
 *   <li>mgmt.firmware	(see Firmware update process for details)
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
	
	public DeviceUpdateRequestHandler(ManagedDevice dmClient) {
		setDMClient(dmClient);
	}
	
	@Override
	public void handleRequest(JsonObject jsonRequest) {
		final String METHOD = "handleRequest";
		JsonArray fields = null;
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
							success = updateField(key, value);
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
	}
	
	private boolean updateField(String name, JsonObject value) {
		Resource resource = getDMClient().getDeviceData().getResource(name);
		if(resource != null) {
			// Don't generate a notify message for the update from IoT Foundation
			resource.update(value, false);
			return true;
		}
		return false;
	}

}
