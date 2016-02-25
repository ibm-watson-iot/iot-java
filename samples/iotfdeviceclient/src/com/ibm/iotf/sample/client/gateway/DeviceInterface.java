/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.gateway;

import java.util.Date;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.devicemgmt.DeviceAction;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.LogSeverity;

/**
 * This class defines a command interface to push the command to the device
 * connected to Gateway.
 * 
 * Every device interface must implement this class.
 *
 */
public interface DeviceInterface {
	final static JsonParser JSON_PARSER = new JsonParser();
	
	public void sendCommand(String cmd);

	public void setFirmwareName(String downloadedFirmwareName);

	public void updateFirmware(DeviceFirmware deviceFirmware);

	public void reboot(DeviceAction action);

	public void toggleDisplay();

	public void sendLog(LogSeverity severity, String message, String data, Date date);

}
