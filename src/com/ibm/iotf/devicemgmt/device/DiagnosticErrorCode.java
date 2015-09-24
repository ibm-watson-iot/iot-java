/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
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
package com.ibm.iotf.devicemgmt.device;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.device.internal.DeviceDiagnostic;
import com.ibm.iotf.devicemgmt.device.resource.Resource;

/**
 * This class represents the error code information of a device.  
 * When <code>append</code> or <code>send</code> method is invoked, 
 * the IBM Internet of Things Foundation will be notified.
 */
public class DiagnosticErrorCode extends Resource {
	
	private static final String RESOURCE_NAME = "errorCodes";
	public static final String ERRORCODE_CHANGE_EVENT = "DiagnosticErrorCode";
	public static final String ERRORCODE_CLEAR_EVENT = "ClearDiagnosticErrorCode";
	
	private int errorCode = 0;

	public DiagnosticErrorCode(int errorCode) {
		super(RESOURCE_NAME);
		this.errorCode = errorCode;
	}
	
	/**
	 * Appends the current errorcode to IBM IoT Foundation.
	 * 
	 * @param errorCode The "errorCode" is a current device error code that 
	 * needs to be added to the Internet of Things Foundation.
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int append(int errorcode) {
		
		this.errorCode = errorcode;
		fireEvent(ERRORCODE_CHANGE_EVENT);
		return this.getRC();
	}
	
	/**
	 * Sends the current device error code to IBM Internet of Things Foundation.
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int send() {
		fireEvent(ERRORCODE_CHANGE_EVENT);
		return this.getRC();
	}
	
	/**
	 * Return the <code>JsonObject</code> representation of the <code>DeviceDiagnostic</code> object.
	 * @return JsonObject object
	 */
	public JsonObject toJsonObject() {
		JsonObject o = new JsonObject();
		o.add("errorCode", new JsonPrimitive(errorCode));
		return o;
	}
	
	/**
	 * Return the JSON string of the <code>DeviceDiagnostic</code> object.
	 */
	public String toString() {
		return toJsonObject().toString();
	}


	/**
	 * Clear the Error Codes from IBM IoT Foundation for this device
	 * @return code indicating whether the clear operation is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int clear() {
		fireEvent(ERRORCODE_CLEAR_EVENT);
		return this.getRC();
	}
	
	@Override
	public int update(JsonElement json) {
		return this.getRC();
	}

	@Override
	public int update(JsonElement json, boolean fireEvent) {
		return this.getRC();
	}
}
