/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt;

/**
 * <p>If a Gateway or Device supports device actions like reboot and factory reset, 
 * this abstract class <code>DeviceActionHandler</code>
 * should be extended by the Gateway or Device code.</p>  
 * 
 * <p>The {@link com.ibm.iotf.devicemgmt.DeviceActionHandler#handleReboot} and 
 * {@link com.ibm.iotf.devicemgmt.DeviceActionHandler#handleFactoryReset}
 *  must be implemented by the subclass to handle the actions sent by the IBM Watson IoT Platform.</p>
 *
 */
public abstract class DeviceActionHandler {
	
	/**
	 * Subclass must implement this method.  
	 * <p>If the device supports reboot, subclass must add logic to reboot the 
	 * device.  
	 *<br>
	 *<br>
	 * If reboot attempt fails, the "rc" is set to 500 and the "message" 
	 * field should be set accordingly, if the reboot is not supported, 
	 * set "rc" to 501 and optionally set "message" accordingly</p>
	 * 
	 * <p>Gateway must use the class {@link com.ibm.iotf.devicemgmt.DeviceAction} to retrieve the 
	 *  DeviceType and DeviceId for which the reboot request is received and act accordingly.</p>
	 * 
	 * @param action DeviceAction where the device code can set the failure status and message
	 * @see DeviceAction
	 */
	public abstract void handleReboot(DeviceAction action);
	
	/**
	 * Subclass must implement this method.  
	 * <p>If the device supports factory reset, subclass must add logic to reset the 
	 * device to factory settings
	 *<br>
	 *<br>
	 * If the factory reset attempt fails, the "rc" should be 500 and the "message" 
	 * field should be set accordingly, if the factory reset action is not supported, 
	 * set "rc" to 501 and optionally set "message" accordingly.</p>
	 * 
	 * <p>Gateway must use the class {@link com.ibm.iotf.devicemgmt.DeviceAction} to retrieve the 
	 *  DeviceType and DeviceId for which the Factory reset request is received and act accordingly.</p>
	 *  
 	 * @param action DeviceAction where the device code can set the failure status and message
	 * @see DeviceAction
	 */
	public abstract void handleFactoryReset(DeviceAction action);

}
