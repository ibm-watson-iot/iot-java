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
package com.ibm.iotf.devicemgmt.device.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.device.DeviceFirmware;
import com.ibm.iotf.devicemgmt.device.resource.Resource;

/**
 * An internal class that encapsulated the Firmware Object
 */
public class DeviceMgmt extends Resource {

	public static final String RESOURCE_NAME = "mgmt";
	private DeviceFirmware firmware;
	
	public DeviceMgmt(DeviceFirmware firmware) {
		super(RESOURCE_NAME);
		this.firmware = firmware;
		this.add(firmware);
	}
	
	@Override
	public JsonObject toJsonObject() {
		return this.firmware.toJsonObject();
	}

	@Override
	public int update(JsonElement json) {
		throw new RuntimeException("Not Supported");
	}

	@Override
	public int update(JsonElement json, boolean fireEvent) {
		throw new RuntimeException("Not Supported");
	}
	public DeviceFirmware getDeviceFirmware() {
		return this.firmware;
	}

}
