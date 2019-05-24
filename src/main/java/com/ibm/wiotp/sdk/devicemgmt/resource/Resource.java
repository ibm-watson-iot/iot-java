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
package com.ibm.wiotp.sdk.devicemgmt.resource;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;

import com.google.gson.JsonElement;

/**
 * 
 * An internal class that provides a a tree like resource model
 * to handle update or establish observe relation on fields/objects
 * requested by Watson IoT Platform 
 */
public abstract class Resource<T> {
	
	public final static String ROOT_RESOURCE_NAME = "root";
	
	public enum ChangeListenerType {
		INTERNAL("Internal"),
		EXTERNAL("External");
		
		private ChangeListenerType(String name) {
			this.name = name;
		}
		
		private final String name;
		
		public String getName() {
			return name;
		}
	}
	
	// Lets say 2 minutes is the default timeout period
	public final static int RESPONSE_TIMEOUT =  1000 * 60 * 2;
	
	// Internal listeners are the library classes that listens for the
	// attribute change that needs to be sent to the IBM Watson IoT Platform
	private PropertyChangeSupport pcsInternal = new PropertyChangeSupport(this);
	
	// Externals are those that will be added by the device code 
	private PropertyChangeSupport pcsExternal = new PropertyChangeSupport(this);
	
	/* The resource resource. */
	private String resourceName;
	private String canonicalName;
	
	private T value;
	
	@SuppressWarnings("rawtypes")
	private Resource parent;
	
	private boolean responseRequired = true;
	private int rc = 0;

	/* The child resources.
	 */
	@SuppressWarnings("rawtypes")
	private HashMap<String, Resource> children;
	
	public Resource(String resourceResourceName) {
		this(resourceResourceName, null);
	}
	
	@SuppressWarnings("rawtypes")
	public Resource(String resourceResourceName, T value) {
		this.resourceName = resourceResourceName;
		this.value = value;
		this.children = new HashMap<String, Resource>();
		this.canonicalName = resourceResourceName;
	}


	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		setValue(value, true);
	}
	
	public void setValue(T value, boolean fireEvent) {
		this.value = value;
		if(fireEvent) {
			pcsInternal.firePropertyChange(this.canonicalName, null, this);
		}
	}
	
	protected void fireEvent(boolean fire) {
		if(fire) {
			pcsInternal.firePropertyChange(this.canonicalName, null, this);
		}
	}
	
	public void fireEvent(String event) {
		pcsInternal.firePropertyChange(event, null, this);
	}
	
	public void notifyExternalListeners() {
		pcsExternal.firePropertyChange(this.canonicalName, null, this);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void add(Resource child) {
		if (child.resourceName == null)
			throw new NullPointerException("Child must have a resourceResourceName");
		if (child.getParent() != null)
			child.getParent().remove(child);
		children.put(child.resourceName, child);
		child.setParent(this);
		if(!this.resourceName.equals(ROOT_RESOURCE_NAME)) {
			child.canonicalName = this.canonicalName + "." +child.resourceName;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public boolean remove(Resource child) {
		Resource removed = remove(child.resourceName);
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
	@SuppressWarnings("rawtypes")
	public Resource remove(String resourceResourceName) {
		return children.remove(resourceResourceName);
	}
	
	/*
	 * 
	 * Returns the parent of this resource
	 */
	
	@SuppressWarnings("rawtypes")
	public Resource getParent() {
		return parent;
	}
	
	/*
	 * Sets the parent of this resource to the given value
	 */
	@SuppressWarnings("rawtypes")
	public void setParent(Resource parent) {
		this.parent = parent;
	}
	
	/* 
	 * Returns the child with the given resourceResourceName
	 */
	
	@SuppressWarnings("rawtypes")
	public Resource getChild(String resourceResourceName) {
		return children.get(resourceResourceName);
	}

	/* 
	 * Returns the resourceName of this resource
	 */
	
	public String getResourceName() {
		return resourceName;
	}
	
	public String getCanonicalName() {
		return canonicalName;
	}

	// should be used for read-only
	@SuppressWarnings("rawtypes")
	public Collection<Resource> getChildren() {
		return children.values();
	}
	
	public abstract JsonElement toJsonObject();

	
	/**
	 * Add a new listener to be notified when the value is changed.
	 * 
	 * @param listener PropertyChangeListener
	 */
	public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
		pcsExternal.addPropertyChangeListener(listener);
	}
	
	
	/**
	 * Add a new listener to be notified when the location is changed.
	 * 
	 * @param type The type of listener
	 * @param listener PropertyChangeListener
	 */
	public synchronized void addPropertyChangeListener(ChangeListenerType type, PropertyChangeListener listener) {
		if(type == ChangeListenerType.INTERNAL) {
			pcsInternal.addPropertyChangeListener(listener);
		} else {
			pcsExternal.addPropertyChangeListener(listener);
		}
	}
	
	/**
	 * Remove the specified listener.
	 *  
	 * @param listener PropertyChangeListener
	 */
	public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
		pcsInternal.removePropertyChangeListener(listener);
	}

	public abstract int update(JsonElement json);
	public abstract int update(JsonElement json, boolean fireEvent);
	
	/**
	 * Requests that Watson IoT Platform to respond when it receives update notification.
	 * This implies that the update call will be blocked until a response is received or timed out.
	 * 
	 * @param responseRequired boolean specifying whether response is required or not
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
	 * Used by the client library to set return code received in the response from Watson IoT Platform.

	 * @param rc return code to be set
	 */
	public void setRC(int rc) {
		this.rc = rc;
	}
	
	public boolean getResponseRequired() {
		return responseRequired;
	}

}
