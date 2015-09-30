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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.codec.binary.Base64;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.device.internal.DeviceDiagnostic;
import com.ibm.iotf.devicemgmt.device.resource.Resource;

/**
 * <p>This class represents the Diagnostic Log information of a device.  
 * When <code>append</code> or <code>send</code> method is invoked, 
 * the IBM Internet of Things Foundation will be notified.</p>
 * 
 * <p>Log entry includes a log messages, its timestamp and severity, as well as 
 * an optional base64-encoded binary diagnostic data.</p>
 * 
 * </p>The device diagnostics operations are intended to provide information 
 * on device errors, and does not provide diagnostic information relating 
 * to the devices connection to the Internet of Things Foundation.</p>
 */
public class DiagnosticLog extends Resource {
	
	public static final String LOG_CHANGE_EVENT = "DiagnosticLog";
	public static final String LOG_CLEAR_EVENT = "ClearDiagnosticLog";
	
	/**
	 * 
	 * <p> Provides the possible Log severities </p>
	 */
	public enum LogSeverity {
		informational(0), warning(1), error(2);
		
		private final int severity;
		
		private LogSeverity(int severity) {
			this.severity = severity;
		}
		
		public int getSeverity() {
			return severity;
		}
	}

	private String message;
	private Date timestamp;
	private LogSeverity severity;
	private String data;
	
	private static final String RESOURCE_NAME = "log";
	
	/**
	 * @param message The Log message that needs to be added to the Internet of Things Foundation.
	 * @param timestamp The Log timestamp
	 * @param severity The Log severity
	 * 
	 **/
	public DiagnosticLog(String message, Date timestamp, LogSeverity severity) {
		this(message, timestamp, severity, null);
	}
	/**
	 * @param message The Log message that needs to be added to the Internet of Things Foundation.
	 * @param timestamp The Log timestamp
	 * @param severity The Log severity
	 * @param data The String data
	 * 	 
	 **/
	public DiagnosticLog(String message, Date timestamp, LogSeverity severity, String data) {
		super(RESOURCE_NAME);
		this.message = message;
		this.timestamp = timestamp;
		this.severity = severity;
		this.data = data;
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
		this.message = message;
		this.timestamp = timestamp;
		this.severity = severity;
		this.data = data;
		fireEvent(LOG_CHANGE_EVENT);
		return this.getRC();
	}
	
	/**
	 * This method appends the last log message to the Internet of Things Foundation.
	 * 
	 * @return code indicating whether the send is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int send() {
		this.timestamp = new Date();
		fireEvent(LOG_CHANGE_EVENT);
		return this.getRC();
	}
	
	/**
	 * Return the <code>JsonObject</code> representation of the <code>DeviceDiagnostic</code> object.
	 * @return JsonObject object
	 */
	public JsonObject toJsonObject() {
		JsonObject o = new JsonObject();
		o.add("message", new JsonPrimitive(this.message));
		o.add("severity", new JsonPrimitive(severity.getSeverity()));
		String utcTime = DateFormatUtils.formatUTC(timestamp, 
				DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
		o.add("timestamp", new JsonPrimitive(utcTime));

		if(this.data != null) {
			byte[] encodedBytes = Base64.encodeBase64(data.getBytes());
			o.add("data", new JsonPrimitive(new String(encodedBytes)));
		}
		return o;
	}
	
	/**
	 * Return the JSON string of the <code>DeviceDiagnostic</code> object.
	 */
	public String toString() {
		return toJsonObject().toString();
	}
	

	/**
	 * Clear the Logs from IBM IoT Foundation for this device
	 * @return code indicating whether the clear operation is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int clear() {
		fireEvent(LOG_CLEAR_EVENT);
		return this.getRC();
	}
	
	@Override
	public int update(JsonElement json, boolean fireEvent) {
		// Not required
		return 0;
	}
	
	@Override
	public int update(JsonElement json) {
		// Not required
		return 0;
	}

}
