/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mike Tran - Initial Contribution
 *****************************************************************************
 */
package com.ibm.iotf.client.application.api;

import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.util.LoggerUtility;

import junit.framework.TestCase;
/**
 * This sample showcases various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/Gateway device(s)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceManagementExtensionsTest {
	
	private static final String CLASS_NAME = DeviceManagementExtensionsTest.class.getName();
	
	private static final String APP_ID = "DMExtApp1";
	private static final String DEVICE_TYPE = "DMExtType1";
	private static final String DEVICE_ID = "DMExtDev1";
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

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, DEVICE_ID);
		apiClient = new APIClient(appProps);

	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {
	}
	
	/**
	 * Expected JSON string when registering a new Device Management Extension:
	 * 
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
	
		final String METHOD = "test01AddDeviceManagementExtension";
		
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
		
		LoggerUtility.info(CLASS_NAME, METHOD,
				"Add " + jsonRequest.toString());
			
		//Add
		try {
			JsonObject jsonResponse = apiClient.addDeviceManagementExtension(jsonRequest);
			LoggerUtility.info(CLASS_NAME, METHOD, 
					"Response: " + jsonResponse.toString());
		} catch (IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		} catch(Exception e) { 
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void test02GetDeviceManagementExtension() {
		final String METHOD = "test02GetDeviceManagementExtension";
		LoggerUtility.info(CLASS_NAME, METHOD, "Get " + bundleId);
		//Get
		try {
			JsonObject jsonResponse = apiClient.getDeviceManagementExtension(bundleId);
			LoggerUtility.info(CLASS_NAME, METHOD, "Response: " + jsonResponse.toString());
		} catch (IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		} catch(Exception e) { 
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void test03DeleteDeviceManagementExtension() {
		final String METHOD = "test03DeleteDeviceManagementExtension";
		LoggerUtility.info(CLASS_NAME, METHOD, "Delete " + bundleId);
		//Delete
		try {
			apiClient.deleteDeviceManagementExtension(bundleId);
		} catch (IoTFCReSTException e) {
			String failMsg = "HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage();
			LoggerUtility.severe(CLASS_NAME, METHOD, failMsg);
			fail(failMsg);		
		} catch(Exception e) { 
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
}