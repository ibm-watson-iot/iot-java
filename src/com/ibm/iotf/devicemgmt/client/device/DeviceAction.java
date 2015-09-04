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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.codec.binary.Base64;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * This class represents the Diagnostic Log information of a device.  
 * When <code>update</code> or <code>send</code> method is invoked, 
 * the IBM Internet of Things Foundation will be notified.
 * 
 * Log entry includes a log messages, its timestamp and severity, as well as 
 * an optional base64-encoded binary diagnostic data.
 */
public class DeviceAction {
	
	public enum Status {
		FAILED(500), UNSUPPORTED(501);
		
		private final int rc;
		
		private Status(int rc) {
			this.rc = rc;
		}
		
		private int get() {
			return rc;
		}
	}

	public static final String DEVICE_REBOOT_START = "DeviceRebootStart";
	public static final String DEVICE_FACTORY_RESET_START = "DeviceFactoryResetStart";
	public static final String REBOOT_STOP = "DeviceRebootStop";
	public static final String FACTORY_RESET_STOP = "DeviceFactoryResetStop";
	
	private Status status;
	private String message;
		
		
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
		
		
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public void fireEvent(String event) {
		pcs.firePropertyChange(event, null, this);
	}
		
	/**
	 * Add a new listener to be notified when the location is changed.
	 * 
	 * @param listener
	 */
	public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	
	/**
	 * Remove the specified listener.
	 *  
	 * @param listener
	 */
	public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	
	/**
	 * Return the <code>JsonObject</code> representation of the <code>DeviceAction Response</code> object.
	 * @return JsonObject object
	 */
	public JsonObject toJsonObject() {
		JsonObject o = new JsonObject();
		o.add("rc", new JsonPrimitive(this.status.get()));
		if(this.message != null) {
			o.add("message", new JsonPrimitive(message));
		}
		return o;
	}
	
	/**
	 * Return the JSON string of the <code>DeviceAction Response</code> object.
	 */
	public String toString() {
		return toJsonObject().toString();
	}
}
