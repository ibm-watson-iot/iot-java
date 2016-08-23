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
 * This class encapsulates the device action like reboot and factory reset.
 * 
 */
public interface DeviceAction {
	/**
	 * <p>Status of the DeviceAction when there is a failure,</p>
	 * <ul class="simple">
	 * <li> 500 - if the operation fails for some reason</li>
	 * <li> 501 - if the operation is not supported</li>
	 * </ul>
	 */
	public enum Status {
		ACCEPTED(202), FAILED(500), UNSUPPORTED(501);
		
		private final int rc;
		
		private Status(int rc) {
			this.rc = rc;
		}
		
		public int get() {
			return rc;
		}
	}

	/**
	 * <p>Set the failure status of the current device action
	 * <br>
	 * The Device Action handler must use this method to report 
	 * the failure status back to IBM Watson IoT Platform whenever
	 * there is a failure.</p>
	 * 
	 * @param status Failure status of the current device action
	 */
	public void setStatus(Status status) ;
	
	/**
	 * <p>Set the failure status of the current device action
	 * <br>
	 * The Device Action handler must use this method to report 
	 * the failure status back to IBM Watson IoT Platform whenever
	 * there is a failure.</p>
	 * 
	 * @param status Failure status of the current device action
	 * @param message Failure message to be reported
	 */
	public void setStatus(Status status, String message);
	
	public String getTypeId();
	
	public String getDeviceId();

	public void setMessage(String message);
}
