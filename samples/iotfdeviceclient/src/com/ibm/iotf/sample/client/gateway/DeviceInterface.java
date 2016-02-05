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

import com.google.gson.JsonElement;

/**
 * This class defines a command interface to push the command to the device
 * connected to Gateway.
 * 
 * Every device interface must implement this class.
 *
 */
public interface DeviceInterface {
	public void sendCommand(String cmd);

}
