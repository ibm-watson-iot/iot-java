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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * An internal class that represents a number attribute of the
 * Device
 */
public class NumberResource extends Resource<Number> {

	public NumberResource(String name, Number value) {
		super(name, value);
	}
	
	/**
	 * Returns the value in Json Format
	 */
	@Override
	public JsonElement toJsonObject() {
		return (JsonElement) new JsonPrimitive(getValue());
	}

	/**
	 * Updates the value of this resource with the given Json value
	 */
	@Override
	public int update(JsonElement json, boolean fireEvent) {
		this.setValue(json.getAsNumber(), fireEvent);
		return this.getRC();
	}
	
	/**
	 * Updates the value of this resource with the given Json value
	 */
	@Override
	public int update(JsonElement json) {
		return update(json, true);
	}
}
