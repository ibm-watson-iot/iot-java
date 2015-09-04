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
package com.ibm.iotf.devicemgmt.client.device;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.client.device.Resource.Resource;

/**
 * This class represents the error code information of a device.  
 * When <code>append</code> or <code>send</code> method is invoked, 
 * the IBM Internet of Things Foundation will be notified.
 */
public class DiagnosticErrorCode extends Resource {
	
	private static final String RESOURCE_NAME = "errorCodes";
	
	private int errorCode = 0;

	public DiagnosticErrorCode(int errorCode) {
		super(RESOURCE_NAME);
		this.errorCode = errorCode;
	}
	
	int update(int errorcode) {
		this.errorCode = errorcode;
		pcs.firePropertyChange(DeviceDiagnostic.ERRORCODE_CHANGE_EVENT, null, this);
		return this.getRC();
	}
	
	int send() {
		pcs.firePropertyChange(DeviceDiagnostic.ERRORCODE_CHANGE_EVENT, null, this);
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

	public int clear() {
		pcs.firePropertyChange(DeviceDiagnostic.ERRORCODE_CLEAR_EVENT, null, this);
		return this.getRC();
	}

	@Override
	public int update(JsonElement json, boolean fireEvent) {
		return this.getRC();
	}

}
