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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware;
import com.ibm.wiotp.sdk.devicemgmt.internal.DMServerTopic;
import com.ibm.wiotp.sdk.devicemgmt.internal.ManagedClient;
import com.ibm.wiotp.sdk.devicemgmt.internal.ResponseCode;

/**
 * Request handler for <code>MMqttClient.SERVER_TOPIC_INITIATE_FIRMWARE_DOWNLOAD</code>
 * <br>Expected request message format
 * <blockquote>
 * {
 * 	"reqId": "string"
 * }
 * </blockquote>
 */	
public class FirmwareDownloadRequestHandler extends DMRequestHandler {

	private static final Logger LOG = LoggerFactory.getLogger(FirmwareDownloadRequestHandler.class);
	
	public FirmwareDownloadRequestHandler(ManagedClient dmClient) {
		setDMClient(dmClient);
	}
	
	/**
	 * Return initiate firmware download topic
	 */
	@Override
	protected String getTopic() {
		DMServerTopic topic = this.getDMClient().getDMServerTopic();
		return topic.getInitiateFirmwareDownload();
	}
	
	/**
	 * Following are actions that needs to be taken after receiving the command
	 * 
	 * If mgmt.firmware.state is not 0 ("Idle") an error should be reported with 
	 * response code 400, and an optional message text.
	 * 
	 * If the action can be initiated immediately, set rc to 202.
	 * 
	 * If mgmt.firmware.url is not set or is not a valid URL, set rc to 400.
	 * 
	 * If firmware download attempt fails, set rc to 500 and optionally set message accordingly.
	 * 
	 * If firmware download is not supported, set rc to 501 and optionally set message accordingly.
	 */
	@Override
	public void handleRequest(JsonObject jsonRequest, String topic) {
		ResponseCode rc;
		
		JsonObject response = new JsonObject();
		response.add("reqId", jsonRequest.get("reqId"));
		DeviceFirmware deviceFirmware = getDMClient().getDeviceData().getDeviceFirmware();
		if (deviceFirmware == null || getDMClient().getFirmwareHandler() == null) {
			rc = ResponseCode.DM_FUNCTION_NOT_IMPLEMENTED;
		} else if(deviceFirmware.getState() != DeviceFirmware.FirmwareState.IDLE.getState()) {
			rc = ResponseCode.DM_BAD_REQUEST;		
		} else {
			if (deviceFirmware.getUrl() != null) {
				rc = ResponseCode.DM_ACCEPTED;
			} else {
				rc = ResponseCode.DM_BAD_REQUEST;
				LOG.warn("No URL mentioned in the request");
			}
		} 
		response.add("rc", new JsonPrimitive(rc.getCode()));
		respond(response);
		if (rc == ResponseCode.DM_ACCEPTED) {
			LOG.debug("fire firmware download");
			getDMClient().getFirmwareHandler().downloadFirmware(deviceFirmware);			
		}
	}
}
