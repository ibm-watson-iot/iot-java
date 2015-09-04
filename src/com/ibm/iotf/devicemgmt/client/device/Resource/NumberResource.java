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
package com.ibm.iotf.devicemgmt.client.device.Resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class NumberResource extends Resource<Number> {

	public NumberResource(String name, Number value) {
		super(name, value);
	}
	
	@Override
	public JsonElement toJsonObject() {
		return (JsonElement) new JsonPrimitive(getValue());
	}

	@Override
	public int update(JsonElement json, boolean fireEvent) {
		this.setValue(json.getAsNumber(), fireEvent);
		return this.getRC();
	}
}
