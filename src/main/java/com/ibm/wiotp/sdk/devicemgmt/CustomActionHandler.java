/**
 *****************************************************************************
 Copyright (c) 2017-19 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 *
 */
package com.ibm.wiotp.sdk.devicemgmt;

import com.ibm.wiotp.sdk.CustomAction;

/**
 * <p>If a Gateway or Device supports custom actions, 
 * this abstract class <code>CustomActionHandler</code>
 * should be extended by the Gateway or Device code.</p>  
 * 
 * <p>The {@link com.ibm.wiotp.sdk.devicemgmt.CustomActionHandler#handleCustomAction}
 *  must be implemented by the subclass to handle the actions sent by the IBM Watson IoT Platform.</p>
 *
 */
public abstract class CustomActionHandler {
	
	/**
	 * Subclass must implement this method.  
	 * <p>If the device supports custom actions, subclass must add logic to take
	 * the necessary action.  
	 *<br>
	 *<br>
	 * If the action attempt fails, the "rc" is set to 500 and the "message" 
	 * field should be set accordingly, if the action is not supported, 
	 * set "rc" to 501 and optionally set "message" accordingly</p>
	 * 
	 * <p>Gateway must use the class {@link com.ibm.wiotp.sdk.CustomAction} to retrieve the 
	 *  DeviceType and DeviceId for which the custom action request is received and act accordingly.</p>
	 * 
	 * @param action CustomAction where the device code can set the failure status and message
	 * @see CustomAction
	 */
	public abstract void handleCustomAction(CustomAction action);

}
