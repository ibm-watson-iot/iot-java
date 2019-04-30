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
package com.ibm.iotf.devicemgmt;

/**
 * <p>If a Gateway (and devices behind Gateway) and directly connected device, supports firmware update, this abstract class <code>DeviceFirmwareHandler</code>
 * should be extended by the Gateway/Device code.</p> 
 * 
 * <p>The <code>downloadFirmware</code> and <code>updateFirmware</code>
 * must be implemented to handle</p>
 *
 */
public abstract class DeviceFirmwareHandler {
	
	/**
	 * <p>Subclass must implement this method.</p>  
	 * <p>If the Gateway or Device supports firmware download, subclass must add logic to
	 *  download the firmware to the device. When done, set the state and status accordingly.</p>
	 *  
	 *  <p>Gateway must use the class {@link com.ibm.iotf.devicemgmt.DeviceFirmware} to retrieve the 
	 *  DeviceType and DeviceId for which the firmware download request is received and act accordingly.</p> 
	 *
	 * @param deviceFirmware DeviceFirmware where the device code can set the Firmware Download progress
	 * @see DeviceFirmware
	 */
	public abstract void downloadFirmware(DeviceFirmware deviceFirmware);
	
	/**
	 * <p> Subclass must implement this method. </p>
	 * <p>If the device supports firmware update, subclass should start updating the
	 * firmware on the device.  When done, set the update status accordingly.</p>
	 * 
	 * <p>Gateway must use the class {@link com.ibm.iotf.devicemgmt.DeviceFirmware} to retrieve the 
	 *  DeviceType and DeviceId for which the firmware update request is received and act accordingly.</p>
	 *  
	 * @param deviceFirmware DeviceFirmware where the device code can set the Firmware Update progress
	 * @see DeviceFirmware
	 */
	public abstract void updateFirmware(DeviceFirmware deviceFirmware);
}
