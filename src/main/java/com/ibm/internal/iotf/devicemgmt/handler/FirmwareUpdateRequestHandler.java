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
package com.ibm.internal.iotf.devicemgmt.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.internal.iotf.devicemgmt.ManagedClient;
import com.ibm.internal.iotf.devicemgmt.DMServerTopic;
import com.ibm.iotf.devicemgmt.DeviceFirmware;

import com.ibm.internal.iotf.devicemgmt.ResponseCode;
import com.ibm.iotf.util.LoggerUtility;

/**
 * Request handler for <code>MMqttClient.SERVER_TOPIC_INITIATE_FIRMWARE_UPDATE</code>
 * <br>Expected request message format
 * <blockquote>
 * {
 * 	"reqId": "string"
 * }
 * </blockquote>
 */	
public class FirmwareUpdateRequestHandler extends DMRequestHandler {

	public FirmwareUpdateRequestHandler(ManagedClient dmClient) {
		setDMClient(dmClient);
	}
	
	/**
	 * Return Initiate firmware update
	 */
	@Override
	protected String getTopic() {
		DMServerTopic topic = this.getDMClient().getDMServerTopic();
		return topic.getInitiateFirmwareUpdate();
	}
	
	/**
	 * If this operation can be initiated immediately, rc should be set to 202.
	 * 
	 * If firmware was not previously downloaded successfully, rc should be set to 400.
	 * 
	 * If firmware update attempt fails, rc should be set to 500 
	 * and the message field can optionally be set to contain relevant information.
	 * 
	 * If firmware update is not supported rc should be set to 501 and the message 
	 * field can optionally be set to contain relevant information.
	 * 
	 * If mgmt.firmware.state is not 2 (Downloaded), an error should be reported 
	 * with rc set to 400 and an optional message text. Otherwise, 
	 * mgmt.firmware.updateStatus should be set to 1 (In Progress) and firmware 
	 * installation should start.
	 * 
	 *  If firmware installation fails, mgmt.firmware.updateStatus should be set to either:
	 *  2 (Out of Memory)
	 *  5 (Unsupported Image)
	 *  
	 *  Once firmware update is complete, mgmt.firmware.updateStatus 
	 *  should be set to 0 (Success), mgmt.firmware.state should be set to 0 (Idle), 
	 *  downloaded firmware image can be deleted from the device and deviceInfo.fwVersion 
	 *  should be set to the value of mgmt.firmware.version.
	 */
	@Override
	public void handleRequest(JsonObject jsonRequest, String topic) {
		final String METHOD = "handleRequest";
		ResponseCode rc;
		
		JsonObject response = new JsonObject();
		response.add("reqId", jsonRequest.get("reqId"));
		
		// handle the error conditions
		DeviceFirmware firmware = getDMClient().getDeviceData().getDeviceFirmware();
		if(firmware == null || getDMClient().getFirmwareHandler() == null) {
			rc = ResponseCode.DM_FUNCTION_NOT_IMPLEMENTED;
		//} else if(firmware.getState() == DeviceFirmware.FirmwareState.IDLE.getState()) {
		//	rc = ResponseCode.DM_BAD_REQUEST;
		} else {
			// Normal condition
			
			if (firmware.getUrl() != null) {
				rc = ResponseCode.DM_ACCEPTED;
			} else {
				rc = ResponseCode.DM_BAD_REQUEST;
				response.add("message", new JsonPrimitive("The value of the firmware URL is not set or null"));
			}
		}

		response.add("rc", new JsonPrimitive(rc.getCode()));
		respond(response);
		if (rc == ResponseCode.DM_ACCEPTED) {
			LoggerUtility.fine(CLASS_NAME, METHOD, "Fire Firmware update ");
			getDMClient().getFirmwareHandler().updateFirmware(firmware);			
		}
	}

}
