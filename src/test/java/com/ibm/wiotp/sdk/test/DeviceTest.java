/**
 *****************************************************************************
 Copyright (c) 2019 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 *
 */
package com.ibm.wiotp.sdk.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.app.ApplicationClient;
import com.ibm.wiotp.sdk.app.messages.Event;
import com.ibm.wiotp.sdk.codecs.JsonCodec;
import com.ibm.wiotp.sdk.device.DeviceClient;
import com.ibm.wiotp.sdk.device.config.DeviceConfig;
import com.ibm.wiotp.sdk.device.config.DeviceConfigAuth;
import com.ibm.wiotp.sdk.device.config.DeviceConfigIdentity;
import com.ibm.wiotp.sdk.device.config.DeviceConfigOptions;
import com.ibm.wiotp.sdk.swagger.ApiClient;
import com.ibm.wiotp.sdk.swagger.ApiException;
import com.ibm.wiotp.sdk.swagger.Configuration;
import com.ibm.wiotp.sdk.swagger.api.DeviceConfigurationApi;
import com.ibm.wiotp.sdk.swagger.auth.HttpBasicAuth;
import com.ibm.wiotp.sdk.swagger.model.DeviceAdditionRequest;
import com.ibm.wiotp.sdk.swagger.model.DeviceAdditionResponse;
import com.ibm.wiotp.sdk.test.util.AbstractTest;
import com.ibm.wiotp.sdk.test.util.callbacks.AppEventCallbackJson;

public class DeviceTest extends AbstractTest {
	
	private final static String TYPE_ID = "AppCmdSubTestType1";
	private final String DEVICE_ID = UUID.randomUUID().toString();
	
	private ApplicationClient app1Client;
	private DeviceClient device1Client;
	
	@Before
	public void setupClient() throws Exception {
		app1Client = new ApplicationClient();
	}
	
	@After
	public void cleanupClient() {
		if (app1Client != null && app1Client.isConnected()) {
			app1Client.disconnect();
			assertTrue("Client is not connected", !app1Client.isConnected());
		}
		if (device1Client != null && device1Client.isConnected()) {
			device1Client.disconnect();
			assertTrue("Client is not connected", !device1Client.isConnected());
		}
	}
	
	private DeviceConfig registerDeviceAndSaveCfg() throws ApiException {
		// This is temporary until fully integrate swaggergen client into the library
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		defaultClient.setBasePath(app1Client.getConfig().getHttpApiBasePath());
		
        HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
        ApiKey.setUsername(System.getenv("WIOTP_API_KEY"));
        ApiKey.setPassword(System.getenv("WIOTP_API_TOKEN"));

        DeviceConfigurationApi deviceApi = new DeviceConfigurationApi();

        // Device device = deviceApi.deviceTypesTypeIdDevicesDeviceIdGet(TYPE_ID, DEVICE_ID, null);
        
        DeviceAdditionRequest daRequest = new DeviceAdditionRequest();
        daRequest.setDeviceId(DEVICE_ID);
        DeviceAdditionResponse daResponse = deviceApi.deviceTypesTypeIdDevicesPost(TYPE_ID, daRequest);
        
        String authToken = daResponse.getAuthToken();
        
        DeviceConfigIdentity cfgIdentity = new DeviceConfigIdentity(app1Client.getConfig().getOrgId(), TYPE_ID, DEVICE_ID);
        DeviceConfigAuth cfgAuth = new DeviceConfigAuth(authToken);
        DeviceConfigOptions cfgOptions= new DeviceConfigOptions();
        DeviceConfig cfg = new DeviceConfig(cfgIdentity, cfgAuth, cfgOptions);
        
        return cfg;
	}
	
	
	@Test
	public void testPublishEvent() throws Exception {
		logTestStart("testConnect");
		app1Client.connect();
		assertTrue("Client is connected", app1Client.isConnected());
		
        DeviceConfig cfg = registerDeviceAndSaveCfg();
        
        device1Client = new DeviceClient(cfg);
        device1Client.registerCodec(new JsonCodec());
        device1Client.connect();
		assertTrue("Client is connected", device1Client.isConnected());
		
		// Create the subscription
		AppEventCallbackJson evtCallback = new AppEventCallbackJson();
		app1Client.registerCodec(new JsonCodec());
		app1Client.registerEventCallback(evtCallback);
		app1Client.subscribeToDeviceEvents(TYPE_ID, DEVICE_ID);
		
		// Send an event
		JsonObject data = new JsonObject();
		data.addProperty("distance", 10);
		boolean success = device1Client.publishEvent("run", data, 1);
		assertTrue("Publish was a success", success);
		
		int count = 0;
		Event<JsonObject> evt = evtCallback.getEvent();
		while( evt == null && count++ <= 10) {
			try {
				evt = evtCallback.getEvent();
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("Event is received by application", (evt != null));
		assertEquals(10, evt.getData().get("distance").getAsInt());
		assertNull(evt.getTimestamp());
		
	}
	
	@Test
	public void testMissingEncoder() throws Exception {
		logTestStart("testMissingEncoder");
        DeviceConfig cfg = registerDeviceAndSaveCfg();

        device1Client = new DeviceClient(cfg);
        device1Client.connect();
		assertTrue("Client is connected", device1Client.isConnected());

		// Send an event (without registering a codec)
		boolean success = device1Client.publishEvent("run", new JsonObject());
		assertFalse("Publish without a known codec failed", success);
	}
	
	@Test
	public void testMissingDecoderInApp() throws Exception {
		logTestStart("testMissingDecoderInApp");
		app1Client.connect();
		assertTrue("Client is connected", app1Client.isConnected());
		
        DeviceConfig cfg = registerDeviceAndSaveCfg();
        
        device1Client = new DeviceClient(cfg);
        device1Client.registerCodec(new JsonCodec());
        device1Client.connect();
		assertTrue("Client is connected", device1Client.isConnected());
		
		// Create the subscription
		AppEventCallbackJson evtCallback = new AppEventCallbackJson();
		app1Client.registerEventCallback(evtCallback);
		app1Client.subscribeToDeviceEvents(TYPE_ID, DEVICE_ID);
		
		// Send an event
		JsonObject data = new JsonObject();
		data.addProperty("distance", 10);
		boolean success = device1Client.publishEvent("run", data, 1);
		assertTrue("Publish was a success", success);
		
		int count = 0;
		Event<JsonObject> evt = evtCallback.getEvent();
		while( evt == null && count++ <= 10) {
			try {
				evt = evtCallback.getEvent();
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		assertEquals(null, evt);
	}
	
}
