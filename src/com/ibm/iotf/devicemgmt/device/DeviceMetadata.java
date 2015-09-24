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
import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.device.resource.Resource;

/**
 * <p>This class represents the metadata information of a device.</p>
 * 
 */
public class DeviceMetadata extends Resource {
	
	private static final String METADATA = "metadata";
	
	public DeviceMetadata(JsonObject metadata) {
		super(METADATA);
		this.setValue(metadata);
	}

	
	/**
	 * Set the metadata to new value
	 * @param metadata new metadata to be set
	 */
	public void setMetadata(JsonObject metadata) {
		this.setValue(metadata);
	}
	
	/**
	 * Returns the value in Json Format
	 */
	@Override
	public JsonElement toJsonObject() {
		return (JsonElement) this.getValue();
	}
	
	/**
	 * Returns the value 
	 */
	public JsonObject getMetadata() {
		return (JsonObject) this.getValue();
	}
	
	/**
	 * Updates the value of this resource with the given Json value
	 */
	public int update(JsonElement json) {
		return update(json, true);
	}

	/**
	 * Updates the value of this resource with the given Json value
	 */
	public int update(JsonElement json, boolean fireEvent) {
		this.setValue((JsonObject)json, fireEvent);
		return this.getRC();
	}
	
	public String toString() {
		return toJsonObject().toString();
	}

}