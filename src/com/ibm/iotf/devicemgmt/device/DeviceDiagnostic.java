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
package com.ibm.iotf.devicemgmt.device;

import java.util.Date;

import com.google.gson.JsonElement;
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
 */
public class DeviceDiagnostic extends Resource {
	public static final String ERRORCODE_CHANGE_EVENT = "DiagnosticErrorCode";
	public static final String LOG_CHANGE_EVENT = "DiagnosticLog";
	public static final String ERRORCODE_CLEAR_EVENT = "ClearDiagnosticErrorCode";
	public static final String LOG_CLEAR_EVENT = "ClearDiagnosticLog";
	
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
		this.add(this.errorCode);
		this.add(this.log);
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
	public int append(int errorCode) {
		if(this.errorCode != null) {
			return this.errorCode.update(errorCode);
		}
		return 0;
	}
	
	/**
	 * Sends the last device error code to IBM Internet of Things Foundation.
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int sendErrorCode() {
		if(errorCode != null) {
			return errorCode.send();
		}
		return 0;
	}
	
	/**
	 * Appends a Log message to the Internet of Things Foundation.
	 * @param message The Log message that needs to be added to the Internet of Things Foundation.
	 * @param timestamp The Log timestamp
	 * @param severity the Log severity
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int append(String message, Date timestamp, LogSeverity severity) {
		return append(message, timestamp, severity, null);
	}
	
	/**
	 * The Log message that needs to be added to the Internet of Things Foundation.
	 * 
	 * @param message The Log message that needs to be added to the Internet of Things Foundation.
	 * @param timestamp The Log timestamp
	 * @param severity The Log severity
	 * @param data The String data
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int append(String message, Date timestamp, LogSeverity severity, String data) {
		if(null != log) {
			return log.update(message, timestamp, severity, data);
		}
		return 0;
	}
	
	/**
	 * The last Log message that needs to be added to the Internet of Things Foundation.
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int sendLog() {
		if(log != null) {
			return log.send();
		}
		return 0;
	}

	/**
	 * @return DiagnosticErrorCode Returns the DiagnosticsErrorCode object
	 * @see DiagnosticErrorCode
	 */
	public DiagnosticErrorCode getErrorCode() {
		return this.errorCode;
	}

	/**
	 * @return DiagnosticLog Returns the DiagnosticsLog
	 * @see DiagnosticLog
	 */
	public DiagnosticLog getLog() {
		return this.log;
	}

	/**
	 * Clear the Error Codes from IBM IoT Foundation for this device
	 * @return code indicating whether the clear operation is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int clearErrorCode() {
		if(this.errorCode != null) {
			return this.errorCode.clear();
		}
		return 0;
		
	}
	
	/**
	 * Clear the Logs from IBM IoT Foundation for this device
	 * @return code indicating whether the clear operation is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	
	public int clearLog() {
		if(this.log != null) {
			return this.log.clear();
		}
		return 0;
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
}
