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
package com.ibm.iotf.client;

import com.google.gson.JsonElement;

public class IoTFCReSTException extends Exception {
	
	public static final String HTTP_ERR_UNEXPECTED = "Unexpected error code";
	public static final String HTTP_ERR_500 = "Unexpected error";
	public static final String HTTP_ADD_DEVICE_ERR_400 = 
			"Invalid request (No body, invalid JSON, unexpected key, bad value)";
	public static final String HTTP_ADD_DEVICE_ERR_401 = 
			"The authentication token is empty or invalid";
	public static final String HTTP_ADD_DEVICE_ERR_403 = 
			"The authentication method is invalid or the API key used does not exist";
	public static final String HTTP_ADD_DEVICE_ERR_409 =
			"The device already exists";
	public static final String HTTP_ADD_DEVICE_ERR_500 = HTTP_ERR_500;
	
	public static final String HTTP_ADD_DM_EXTENSION_ERR_400 =
			"Invalid request";
	public static final String HTTP_ADD_DM_EXTENSION_ERR_401 =
			"Unauthorized";
	public static final String HTTP_ADD_DM_EXTENSION_ERR_403 =
			"Forbidden";
	public static final String HTTP_ADD_DM_EXTENSION_ERR_409 =
			"Conflict";
	public static final String HTTP_ADD_DM_EXTENSION_ERR_500 =
			"Internal server error";
	public static final String HTTP_GET_DM_REQUEST_ERR_500 = HTTP_ERR_500;
	public static final String HTTP_GET_DM_REQUEST_ERR_404 = "Requested status not found";

	public static final String HTTP_INITIATE_DM_REQUEST_ERR_500 = HTTP_ERR_500;
	public static final String HTTP_INITIATE_DM_REQUEST_ERR_400 = HTTP_ADD_DEVICE_ERR_400;
	public static final String HTTP_INITIATE_DM_REQUEST_ERR_401 = HTTP_ADD_DEVICE_ERR_401;
	public static final String HTTP_INITIATE_DM_REQUEST_ERR_403 = "One or more of the devices does not support the requested action";
	public static final String HTTP_INITIATE_DM_REQUEST_ERR_404 = "One or more of the devices does not exist";
	
	public static final String HTTP_ADD_LOGICAL_INTERFACE_ERR_400 = HTTP_ADD_DEVICE_ERR_400;
	public static final String HTTP_ADD_LOGICAL_INTERFACE_ERR_401 = HTTP_ADD_DEVICE_ERR_401;
	public static final String HTTP_ADD_LOGICAL_INTERFACE_ERR_403 = "The authentication method is invalid or the API key used does not exist";
	public static final String HTTP_ADD_LOGICAL_INTERFACE_ERR_500 = HTTP_ERR_500;
		
	public static final String HTTP_ADD_PHYSICAL_INTERFACE_ERR_400 = HTTP_ADD_DEVICE_ERR_400;
	public static final String HTTP_ADD_PHYSICAL_INTERFACE_ERR_401 = HTTP_ADD_DEVICE_ERR_401;
	public static final String HTTP_ADD_PHYSICAL_INTERFACE_ERR_403 = "The authentication method is invalid or the API key used does not exist";
	public static final String HTTP_ADD_PHYSICAL_INTERFACE_ERR_500 = HTTP_ERR_500;

	public static final String HTTP_ADD_DRAFT_EVENT_TYPE_ERR_400 = HTTP_ADD_DEVICE_ERR_400;
	public static final String HTTP_ADD_DRAFT_EVENT_TYPE_ERR_401 = HTTP_ADD_DEVICE_ERR_401;
	public static final String HTTP_ADD_DRAFT_EVENT_TYPE_ERR_403 = "The authentication method is invalid or the API key used does not exist";
	public static final String HTTP_ADD_DRAFT_EVENT_TYPE_ERR_500 = HTTP_ERR_500;
	
	public static final String HTTP_ADD_DRAFT_LOGICAL_INTERFACE_ERR_400 = HTTP_ADD_DEVICE_ERR_400;
	public static final String HTTP_ADD_DRAFT_LOGICAL_INTERFACE_ERR_401 = HTTP_ADD_DEVICE_ERR_401;
	public static final String HTTP_ADD_DRAFT_LOGICAL_INTERFACE_ERR_403 = "The authentication method is invalid or the API key used does not exist";
	public static final String HTTP_ADD_DRAFT_LOGICAL_INTERFACE_ERR_404 = "A device type with the specified id does not exist";
	public static final String HTTP_ADD_DRAFT_LOGICAL_INTERFACE_ERR_500 = HTTP_ERR_500;
	
	public static final String HTTP_ADD_DRAFT_PROPERTY_MAPPINGS_ERR_400 = HTTP_ADD_DEVICE_ERR_400;
	public static final String HTTP_ADD_DRAFT_PROPERTY_MAPPINGS_ERR_401 = HTTP_ADD_DEVICE_ERR_401;
	public static final String HTTP_ADD_DRAFT_PROPERTY_MAPPINGS_ERR_403 = "The authentication method is invalid or the API key used does not exist";
	public static final String HTTP_ADD_DRAFT_PROPERTY_MAPPINGS_ERR_404 = "A device type with the specified id does not exist";
	public static final String HTTP_ADD_DRAFT_PROPERTY_MAPPINGS_ERR_500 = HTTP_ERR_500;

	public static final String HTTP_ADD_DRAFT_PHYSICAL_INTERFACE_ERR_400 = HTTP_ADD_DEVICE_ERR_400;
	public static final String HTTP_ADD_DRAFT_PHYSICAL_INTERFACE_ERR_401 = HTTP_ADD_DEVICE_ERR_401;
	public static final String HTTP_ADD_DRAFT_PHYSICAL_INTERFACE_ERR_403 = "The authentication method is invalid or the API key used does not exist";
	public static final String HTTP_ADD_DRAFT_PHYSICAL_INTERFACE_ERR_404 = "A device type with the specified id does not exist";
	public static final String HTTP_ADD_DRAFT_PHYSICAL_INTERFACE_ERR_500 = HTTP_ERR_500;
	
	public static final String HTTP_ADD_SCHEMA_DEFINITION_ERR_400 = HTTP_ADD_DEVICE_ERR_400;
	public static final String HTTP_ADD_SCHEMA_DEFINITION_ERR_401 = HTTP_ADD_DEVICE_ERR_401;
	public static final String HTTP_ADD_SCHEMA_DEFINITION_ERR_403 = "The authentication method is invalid or the API key used does not exist";
	public static final String HTTP_ADD_SCHEMA_DEFINITION_ERR_404 = "A device type with the specified id does not exist";
	public static final String HTTP_ADD_SCHEMA_DEFINITION_ERR_500 = HTTP_ERR_500;
	

	private String method = null;
	private String url = null;
	private int httpCode;
	private JsonElement response = null;
	private String request = null;
	
	/**
	 * 
	 * @param method One of the HTTP methods ("get","post", "put", "delete")
	 * @param url URL of the ReST call
	 * @param request Requested parameters or NULL
	 * @param httpCode Returned code from Watson IoT Platform
	 * @param reason Reason for the exception
	 * @param response Response from Watson IoT Platform or NULL
	 */
	public IoTFCReSTException(String method, String url, String request, 
			int httpCode, String reason, JsonElement response) {
		super(reason);
		this.method = method;
		this.url = url;
		this.request = request;
		this.httpCode = httpCode;
		this.response = response;
	}
	
	public IoTFCReSTException(int httpCode, String reason, JsonElement response) {
		super(reason);
		this.httpCode = httpCode;
		this.response = response;
	}
	
	public IoTFCReSTException(int httpCode, String reason) {
		super(reason);
		this.httpCode = httpCode;
	}
	
	public IoTFCReSTException(String reason, JsonElement response) {
		super(reason);
		this.response = response;
	}
	
	public IoTFCReSTException(String reason) {
		super(reason);
	}
	
	public String getMethodL() {
		return method;
	}
	
	public String getURL() {
		return url;
	}
	
	public String getRequest() {
		return request;
	}

	public int getHttpCode() {
		return httpCode;
	}

	/**
	 * Return the response from Watson IoT or null.
	 * @return JsonElement or null
	 */
	public JsonElement getResponse() {
		return response;
	}
	
}
