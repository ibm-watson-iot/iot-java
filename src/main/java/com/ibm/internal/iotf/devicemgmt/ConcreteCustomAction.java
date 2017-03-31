/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
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

package com.ibm.internal.iotf.devicemgmt;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.client.CustomAction;

/**
 * This class encapsulates the custom device actions like reboot and factory reset.
 * 
 */
public class ConcreteCustomAction implements CustomAction {
	
	public static final String DEVICE_ACTION_STATUS_UPDATE = "ListenerStatusUpdate";
	
	private Status status;
	private String message;
	private JsonObject payload;
	private String bundleId;
	private String actionId;
	private String reqId;
	private String typeId;
	private String deviceId;

	public ConcreteCustomAction(String typeId, String deviceId) {
		this.typeId = typeId;
		this.deviceId = deviceId;
	}
	
	public String getBundleId() {
		return bundleId;
	}
	
	public void setBundleId(String bundleId) {
		this.bundleId = bundleId;
	}
	
	public String getActionId() {
		return actionId;
	}
	
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	
	public String getReqId() {
		return reqId;
	}
	
	public void setReqId(String reqId) {
		this.reqId = reqId;
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
	 * the failure status back to IBM Watson IoT Platform whenever
	 * there is a failure.</p>
	 * 
	 * @param status Failure status of the current device action
	 */
	public void setStatus(Status status) {
		this.status = status;
		this.fireEvent(DEVICE_ACTION_STATUS_UPDATE);
	}
	
	/**
	 * <p>Set the failure status of the current device action
	 * <br>
	 * The Device Action handler must use this method to report 
	 * the failure status back to IBM Watson IoT Platform whenever
	 * there is a failure.</p>
	 * 
	 * @param status Failure status of the current device action
	 */
	public void setStatus(Status status, String message) {
		this.status = status;
		this.message = message;
		this.fireEvent(DEVICE_ACTION_STATUS_UPDATE);
	}
	
	@Override
	public JsonObject getPayload() {
		return this.payload;
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
	 * 
	 * @param listener PropertyChangeListener
	 */
	public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(DEVICE_ACTION_STATUS_UPDATE, listener);
	}
	
	public synchronized void clearListener() {
		PropertyChangeListener[] listener = pcs.getPropertyChangeListeners(DEVICE_ACTION_STATUS_UPDATE);
		for(int i = 0; i < listener.length; i++) {
			pcs.removePropertyChangeListener(listener[i]);
		}
	}
	
	/**
	 * Return the <code>JsonObject</code> representation of the <code>Listener Response</code> object.
	 * @return JsonObject object
	 */
	public JsonObject toJsonObject() {
		JsonObject o = new JsonObject();
		if (this.status != null) {
			o.add("rc", new JsonPrimitive(this.status.get()));
		}
		if (this.message != null) {
			o.add("message", new JsonPrimitive(message));
		}
		if (this.reqId != null) {
			o.add("reqId", new JsonPrimitive(reqId));
		}
		return o;
	}
	
	/**
	 * Return the JSON string of the <code>Listener Response</code> object.
	 */
	public String toString() {
		return toJsonObject().toString();
	}

	public int getStatus() {
		return this.status.get();
	}

	public void setPayload(JsonObject payload) {
		this.payload = payload;
	}
	
	
}
