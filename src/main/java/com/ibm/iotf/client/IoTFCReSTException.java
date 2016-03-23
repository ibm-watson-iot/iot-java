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
