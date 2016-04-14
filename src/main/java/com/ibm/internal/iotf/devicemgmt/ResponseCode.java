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
package com.ibm.internal.iotf.devicemgmt;

/**
 * List of available response codes that device needs to respond to
 * the server request
 */
public enum ResponseCode {
	DM_SUCCESS(200),
	DM_ACCEPTED(202),
	DM_UPDATE_SUCCESS(204),
	DM_BAD_REQUEST(400),
	DM_NOT_FOUND(404),
	DM_INTERNAL_ERROR(500),
	DM_FUNCTION_NOT_IMPLEMENTED(501);
	
	private int code;
	
	private ResponseCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}

	public static ResponseCode get(int rc) {
		switch(rc) {
			case 200: return DM_SUCCESS;
			case 202: return DM_ACCEPTED;
			case 204: return DM_UPDATE_SUCCESS;
			case 400: return DM_BAD_REQUEST;
			case 404: return DM_NOT_FOUND;
			case 500: return DM_INTERNAL_ERROR;
			case 501: return DM_FUNCTION_NOT_IMPLEMENTED;
		}
		return DM_SUCCESS;
	}
}
