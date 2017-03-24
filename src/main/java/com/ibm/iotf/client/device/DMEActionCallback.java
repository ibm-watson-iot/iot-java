/**
 *****************************************************************************
 Copyright (c) 2017 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.device;

/**
 * Interface to provide callback methods for DME actions  <br>
 * This can be used by devices to listen for various DME actions. <br>
 * Device Management Extension is a JSON document which defines a set of device management actions. 
 * The actions can be initiated against one or more devices which support those actions.<br> 
 * The actions are initiated in the same way as the default device management actions by using 
 * either the IoT Platform dashboard or the device management REST APIs.
 */
public interface DMEActionCallback {

	
	/**
	 * process the action received
	 * 
	 * @param action
	 *               The action to be performed 
	 */	
	public void processAction(DMEAction action);
}
