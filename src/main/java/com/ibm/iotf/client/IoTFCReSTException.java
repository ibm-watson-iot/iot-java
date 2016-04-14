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
package com.ibm.iotf.client;

import com.google.gson.JsonElement;

public class IoTFCReSTException extends Exception {
	
	private int httpCode;
	private JsonElement response;
	
	public IoTFCReSTException(int httpCode, String message, 
			JsonElement response) {
		super(message);
		this.httpCode = httpCode;
		this.response = response;
	}

	public IoTFCReSTException(String message) {
		super(message);
	}

	public IoTFCReSTException(int code, String message) {
		super(message);
		this.httpCode = code;
	}

	public int getHttpCode() {
		return httpCode;
	}

	public JsonElement getResponse() {
		return response;
	}
	
}
