/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Modified to include Resource Model
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt.device.internal;

import java.util.Date;

import com.google.gson.JsonElement;
import com.ibm.iotf.devicemgmt.device.DiagnosticErrorCode;
import com.ibm.iotf.devicemgmt.device.DiagnosticLog;
import com.ibm.iotf.devicemgmt.device.DiagnosticLog.LogSeverity;
import com.ibm.iotf.devicemgmt.device.resource.Resource;

/**
 * <p>This class represents the errorcode & log information of a device.  
 * When <code>append</code> or <code>send</code> method is invoked, 
 * the IBM Internet of Things Foundation will be notified.</p>
 * 
 * </p>The device diagnostics operations are intended to provide information 
 * on device errors, and does not provide diagnostic information relating 
 * to the devices connection to the Internet of Things Foundation.</p>
 * 
 * This is a just a placeholder object to represent the log and errorcode object 
 * in the correct tree format, i.e. diag.errorcode and diag.log
 */
public class DeviceDiagnostic extends Resource {
	
	private static final String RESOURCE_NAME = "diag";
	
	private DiagnosticErrorCode errorCode;
	private DiagnosticLog log;
	
	public DeviceDiagnostic(DiagnosticErrorCode errorCode) {
		super(RESOURCE_NAME);
		this.errorCode = errorCode;
		this.add(this.errorCode);
	}
	
	public DeviceDiagnostic(DiagnosticLog log) {
		super(RESOURCE_NAME);
		this.log = log;
		this.add(this.log);
	}
	
	public DeviceDiagnostic(DiagnosticErrorCode errorCode, DiagnosticLog log) {
		super(RESOURCE_NAME);
		this.errorCode = errorCode;
		this.log = log;
		if(this.errorCode != null) {
			this.add(this.errorCode);
		}
		if(this.log != null) {
			this.add(this.log);
		}
	}
		
	@Override
	public JsonElement toJsonObject() {
		throw new RuntimeException("Not Supported");
	}

	@Override
	public int update(JsonElement json) {
		throw new RuntimeException("Not Supported");
	}
	
	@Override
	public int update(JsonElement json, boolean fireEvent) {
		throw new RuntimeException("Not Supported");
	}

	public DiagnosticErrorCode getErrorCode() {
		return this.errorCode;
	}
	
	public DiagnosticLog getLog() {
		return this.log;
	}

}
