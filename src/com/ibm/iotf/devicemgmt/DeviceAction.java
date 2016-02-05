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

package com.ibm.iotf.devicemgmt;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.resource.Resource.ChangeListenerType;

/**
 * <p>This class encapsulates the device action like reboot & factory reset.</p>
 * 
 */
public class DeviceAction {
	/**
	 * <p>Status of the DeviceAction when there is a failure,</p>
	 * <ul class="simple">
	 * <li> 500 - if the operation fails for some reason</li>
	 * <li> 501 - if the operation is not supported</li>
	 * </ul>
	 */
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
	public static final String DEVICE_REBOOT_STOP = "DeviceRebootStop";
	public static final String DEVICE_FACTORY_RESET_STOP = "DeviceFactoryResetStop";
	
	private Status status;
	private String message;
	private String typeId;
	private String deviceId;

	DeviceAction(String typeId, String deviceId) {
		this.typeId = typeId;
		this.deviceId = deviceId;
	}
	
	public String getTypeId() {
		return typeId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * <p>Set the failure status of the current device action
	 * <br>
	 * The Device Action handler must use this method to report 
	 * the failure status back to IBM IoT Foundation whenever
	 * there is a failure.</p>
	 * 
	 * @param status Failure status of the current device action
	 */
	public void setStatus(Status status) {
		this.status = status;
	}
	
	/**
	 * <p>Set the failure message of the current device action that needs to be 
	 * sent to the IBM IoT Foundation.
	 * <br>
	 * The Device Action handler must use this method to report 
	 * the failure message back to IBM IoT Foundation whenever
	 * there is a failure.</p>
	 * 
	 * @param message failure message that needs to be sent to IBM IoT Foundation
	 */
	public void setMessage(String message) {
		this.message = message;
	}
		
		
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	/**
	 * Trigger the notification message - This method should only be used by the library code
	 * @param event event to be fired
	 */
	public void fireEvent(String event) {
		pcs.firePropertyChange(event, null, this);
	}
		
	/**
	 * Add a new listener to be notified when device action status is changed.
	 * @param internal 
	 * 
	 * @param listener
	 */
	public synchronized void addPropertyChangeListener(ChangeListenerType internal, 
			PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	
	/**
	 * Remove the specified listener.
	 *  
	 * @param listener
	 */
	synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
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
