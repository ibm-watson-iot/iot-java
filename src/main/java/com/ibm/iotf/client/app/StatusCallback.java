/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.app;


/**
 * This interface holds the callbacks for processing status
 *
 */
public interface StatusCallback {

	
	/**
	 * This method processes the application status
	 * @param status
	 * 			an object of ApplicationStatus
	 */
	public void processApplicationStatus(ApplicationStatus status);
	
	/**
	 * This method processes device status
	 * @param status
	 * 			an object of DeviceStatus
	 */
	public void processDeviceStatus(DeviceStatus status);
}
