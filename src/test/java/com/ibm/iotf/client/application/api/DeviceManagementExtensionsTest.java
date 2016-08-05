/**
 *****************************************************************************
 * Copyright (c) 2015 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */
package com.ibm.iotf.client.application.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.FixMethodOrder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.device.DeviceManagementTest;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

import org.junit.runners.MethodSorters;
import org.junit.FixMethodOrder;
/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/Gateway device(s)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceManagementExtensionsTest extends TestCase {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	

	private static boolean setUpIsDone = false;
	
	private static APIClient apiClient = null;

	String bundleId = "TEST_BUNDLE_01";
	String display_name_local = "en";
	String display_name_value = "TEST BUNDLE 01";
	String description_local = "en";
	String description_value = "TEST BUNDLE 01 DESCRIPTION";
	String version = "1.0";
	String provider = "Watson IoT Platform Test";
	String action_id_1 = "ActionID1";
	String action_id_1_display_name_local = "en";
	String action_id_1_display_name_value = "Action 1";
	String action_id_1_description_local = "en";
	String action_id_1_description_value = "Test Action 1";
	String parameter_1_name = "Param1";
	String parameter_1_value = "^(0|[1-9][0-9]*)$";
	boolean parameter_1_required = false;
	String parameter_1_default_value = "1000";
	
	public synchronized void setUp() {
	    if (setUpIsDone) {
	        return;
	    }
	    
	    /**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(DeviceManagementExtensionsTest.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		try {
			//Instantiate the class by passing the properties file
			apiClient = new APIClient(props);
			
		} catch (Exception e) {
			// looks like the application.properties file is not updated properly
			apiClient = null;
			return;
		}
		
	    setUpIsDone = true;
	}
	
	/*
	{
		"bundleId": "<unique identifier>",
		"displayName": {
			"<locale 0>": "<localized display name 0>"
		},
		"description": {
			"<locale 0>": "<localized description 0>"
		},
		"version": "<bundle version>",
		"provider": "<bundle provider>",
		"actions": {
			"<actionId 0>": {
				"actionDisplayName": {
					"<locale 0>": "<localized action display name 0>"
				},
				"description": {
					"<locale 0>": "<localized description>"
					},
				"parameters": [
					{
						"name": "<parameterId>",
						"value": "<regex pattern for value checking>",
						"required": false,
						"defaultValue": "<default>"
					}
				]
			}
		}
	}
	*/
	public void test01AddDeviceManagementExtension() {
		
		JsonObject jsonRequest = new JsonObject();
		//BundleId
		jsonRequest.add("bundleId", new JsonPrimitive(bundleId));
		//displayName
		JsonObject jsonDisplayName = new JsonObject();
		jsonDisplayName.add(display_name_local, new JsonPrimitive(display_name_value));
		jsonRequest.add("displayName", jsonDisplayName);
		//description
		JsonObject jsonDescription = new JsonObject();
		jsonDescription.add(description_local, new JsonPrimitive(description_value));
		jsonRequest.add("description", jsonDescription);
		//version
		jsonRequest.add("version", new JsonPrimitive(version));
		//provider
		jsonRequest.add("provider", new JsonPrimitive(provider));
		
		
		//Action 1 displayName
		JsonObject jsonAction1DisplayName = new JsonObject();
		jsonAction1DisplayName.add(action_id_1_display_name_local, new JsonPrimitive(action_id_1_display_name_value));
		
		//Action 1 description
		JsonObject jsonAction1Description = new JsonObject();
		jsonAction1Description.add(action_id_1_description_local, new JsonPrimitive(action_id_1_description_value));
		
		//parameter1
		JsonObject jsonParameter1 = new JsonObject();
		jsonParameter1.add("name", new JsonPrimitive(parameter_1_name));
		jsonParameter1.add("value", new JsonPrimitive(parameter_1_value));
		jsonParameter1.add("required", new JsonPrimitive(parameter_1_required));
		jsonParameter1.add("defaultValue", new JsonPrimitive(parameter_1_default_value));
		//parameters
		JsonArray jsonAction1Parameters = new JsonArray();
		jsonAction1Parameters.add(jsonParameter1);
		
		//Action 1
		JsonObject jsonAction1 = new JsonObject();
		jsonAction1.add("actionDisplayName", jsonAction1DisplayName);
		jsonAction1.add("description", jsonAction1Description);
		jsonAction1.add("parameters", jsonAction1Parameters);

		//actions
		JsonObject jsonActions = new JsonObject();
		jsonActions.add(action_id_1, jsonAction1);
		
		jsonRequest.add("actions", jsonActions);
		
		System.out.println("ADD " + jsonRequest.toString());
		
		//Add
		try {
			JsonObject jsonResponse = apiClient.addDeviceManagementExtension(jsonRequest);
			System.out.println("Response: " + jsonResponse.toString());
		} catch (IoTFCReSTException ex) {
			System.err.println(ex.getResponse());
			fail(ex.getMessage());
		} catch(Exception e) { 
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void test02GetDeviceManagementExtension() {
		System.out.println("GET " + bundleId);
		//Get
		try {
			JsonObject jsonResponse = apiClient.getDeviceManagementExtension(bundleId);
			System.out.println("Response: " + jsonResponse.toString());
		} catch (IoTFCReSTException ex) {
			System.err.println(ex.getResponse());
			fail(ex.getMessage());
		} catch(Exception e) { 
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void test03DeleteDeviceManagementExtension() {
		System.out.println("DELETE " + bundleId);
		//Delete
		try {
			apiClient.deleteDeviceManagementExtension(bundleId);
		} catch (IoTFCReSTException ex) {
			System.err.println(ex.getResponse());
			fail(ex.getMessage());
		} catch(Exception e) { 
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	
	
}