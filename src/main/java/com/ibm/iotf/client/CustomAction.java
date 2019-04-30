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

package com.ibm.iotf.client;

import com.google.gson.JsonObject;

/**
 * This class encapsulates the custom actions.
 * 
 */
public interface CustomAction {
	/**
	 * <p>Status of the CustomAction when there is a failure,</p>
	 * <ul class="simple">
	 * <li> 500 - if the operation fails for some reason</li>
	 * <li> 501 - if the operation is not supported</li>
	 * </ul>
	 */
	public enum Status {
		OK(200), FAILED(500), UNSUPPORTED(501);
		
		private final int rc;
		
		private Status(int rc) {
			this.rc = rc;
		}
		
		public int get() {
			return rc;
		}
	}

	/**
	 * <p>Set the failure status of the current custom action
	 * <br>
	 * The Custom Action handler must use this method to report 
	 * the failure status back to IBM Watson IoT Platform whenever
	 * there is a failure.</p>
	 * 
	 * @param status Failure status of the current custom action
	 */
	public void setStatus(Status status) ;
	
	/**
	 * <p>Set the failure status of the current custom action
	 * <br>
	 * The Custom Action handler must use this method to report 
	 * the failure status back to IBM Watson IoT Platform whenever
	 * there is a failure.</p>
	 * 
	 * @param status Failure status of the current custom action
	 * @param message Failure message to be reported
	 */
	public void setStatus(Status status, String message);
	
	public String getTypeId();
			
	public String getDeviceId();
	
	public String getBundleId();
	
	public JsonObject getPayload();
	
	public String getActionId();

	public String getReqId();
}