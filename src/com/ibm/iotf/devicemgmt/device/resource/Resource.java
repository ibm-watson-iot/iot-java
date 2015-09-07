/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt.device.resource;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;

import com.google.gson.JsonElement;

/**
 * 
 * An internal class that provides a a tree like resource model
 * to handle update or establish observe relation on fields/objects
 * requested by IoT Foundation 
 */
public abstract class Resource<T> {
	
	public final static String ROOT_RESOURCE_NAME = "root";
	public final static int RESPONSE_TIMEOUT =  1000 * 60;
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	/* The resource resourceResourceName. */
	private String resourceResourceName;
	private String canonicalName;
	
	private T value;
	
	private Resource parent;
	
	private boolean responseRequired = true;
	private int rc = 0;

	/* The child resources.
	 */
	private HashMap<String, Resource> children;
	
	public Resource(String resourceResourceName) {
		this(resourceResourceName, null);
	}
	
	public Resource(String resourceResourceName, T value) {
		this.resourceResourceName = resourceResourceName;
		this.value = value;
		this.children = new HashMap<String, Resource>();
		this.canonicalName = resourceResourceName;
	}


	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
		pcs.firePropertyChange(this.canonicalName, null, this);
	}
	
	public void setValue(T value, boolean fireEvent) {
		this.value = value;
		if(fireEvent)
			pcs.firePropertyChange(this.canonicalName, null, this);
	}
	
	public void add(Resource child) {
		if (child.resourceResourceName == null)
			throw new NullPointerException("Child must have a resourceResourceName");
		if (child.getParent() != null)
			child.getParent().remove(child);
		children.put(child.resourceResourceName, child);
		child.setParent(this);
		if(!this.resourceResourceName.equals(ROOT_RESOURCE_NAME)) {
			child.canonicalName = this.canonicalName + "." +child.resourceResourceName;
		}
	}
	
	private void setCanonicalResourceName(String canonicalName) {
		this.canonicalName = canonicalName; 
		
	}


	public boolean remove(Resource child) {
		Resource removed = remove(child.resourceResourceName);
		if (removed == child) {
			child.setParent(null);
			return true;
		}
		return false;
	}
	
	/**
	 * Removes the child with the specified resourceResourceName and returns it. If no child
	 * with the specified resourceResourceName is found, the return value is null.
	 * 
	 * @param resourceResourceName the resourceResourceName
	 * @return the removed resource or null
	 */
	public Resource remove(String resourceResourceName) {
		return children.remove(resourceResourceName);
	}
	
	/*
	 * 
	 * Returns the parent of this resource
	 */
	
	public Resource getParent() {
		return parent;
	}
	
	/*
	 * Sets the parent of this resource to the given value
	 */
	public void setParent(Resource parent) {
		this.parent = parent;
	}
	
	/* 
	 * Returns the child with the given resourceResourceName
	 */
	
	public Resource getChild(String resourceResourceName) {
		return children.get(resourceResourceName);
	}

	/* 
	 * Returns the resourceResourceName of this resource
	 */
	
	public String getResourceName() {
		return resourceResourceName;
	}
	
	public String getCanonicalName() {
		return canonicalName;
	}

	// should be used for read-only
	public Collection<Resource> getChildren() {
		return children.values();
	}
	
	public abstract JsonElement toJsonObject();

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

	public abstract int update(JsonElement json, boolean fireEvent);
	
	/**
	 * Requests that IoT Foundation to respond when it receives update notification.
	 * This implies that the update call will be blocked until a response is received or timed out.
	 * @param responseRequired
	 */
	public void waitForResponse(boolean responseRequired) {
		this.responseRequired = responseRequired;
	}
	
	/**
	 * Returns the return code of the last location update notification.
	 * <br>Note 200 is successful.
	 * @return Return code
	 */
	public int getRC() {
		return rc;
	}
	
	/**
	 * Used by the client library to set return code received in the response from IoT Foundation.
	 * @param rc
	 */
	public void setRC(int rc) {
		this.rc = rc;
	}
	
	public boolean getResponseRequired() {
		return responseRequired;
	}

}
