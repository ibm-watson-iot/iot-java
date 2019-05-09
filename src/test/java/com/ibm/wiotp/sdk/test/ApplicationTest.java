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
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.app.ApplicationClient;
import com.ibm.wiotp.sdk.app.messages.Command;
import com.ibm.wiotp.sdk.app.messages.Event;
import com.ibm.wiotp.sdk.codecs.JsonCodec;
import com.ibm.wiotp.sdk.codecs.Utf8Codec;
import com.ibm.wiotp.sdk.test.util.AbstractTest;
import com.ibm.wiotp.sdk.test.util.callbacks.AppCommandCallbackJson;
import com.ibm.wiotp.sdk.test.util.callbacks.AppEventCallbackJson;
import com.ibm.wiotp.sdk.test.util.callbacks.AppEventCallbackUtf8;

public class ApplicationTest extends AbstractTest {
	
	private final static String DEVICE_TYPE = "AppCmdSubTestType1";
	private final static String DEVICE_ID = "AppCmdSubTestDev1";
	
	private ApplicationClient app1Client;
	
	@After
	public void cleanupClient() {
		if (app1Client != null && app1Client.isConnected()) {
			app1Client.disconnect();
			assertTrue("Client is not connected", !app1Client.isConnected());
		}
	}
	
	@Test
	public void testConnect() throws Exception {
		logTestStart("testConnect");
		app1Client = new ApplicationClient();
		app1Client.connect();
		assertTrue("Client is connected", app1Client.isConnected());
	}
	
	@Test
	public void testSendAndSubscribeToCommand() throws Exception {
		logTestStart("testSendAndSubscribeToCommand");
		app1Client = new ApplicationClient();
		app1Client.connect();
		assertTrue("Client is connected", app1Client.isConnected());

		// Create the subscription
		AppCommandCallbackJson cmdCallback = new AppCommandCallbackJson();
		app1Client.registerCodec(new JsonCodec());
		app1Client.registerCommandCallback(cmdCallback);
		
		app1Client.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID);
		
		// Send a command
		JsonObject data = new JsonObject();
		data.addProperty("distance", 10);
		boolean success = app1Client.publishCommand(DEVICE_TYPE, DEVICE_ID, "run", data);
		assertTrue("Publish was a success", success);

		int count = 0;
		Command<JsonObject> cmd = cmdCallback.getCommand();
		while( cmd == null && count++ <= 10) {
			try {
				cmd = cmdCallback.getCommand();
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		assertTrue("Command is received by application", (cmd != null));
		assertEquals(DEVICE_TYPE, cmd.getTypeId());
		assertEquals(DEVICE_ID, cmd.getDeviceId());
		assertEquals(10, cmd.getData().get("distance").getAsInt());

		app1Client.unsubscribeFromDeviceCommands(DEVICE_TYPE, DEVICE_ID);
	}	

	@Test
	public void testSendAndSubscribeToNullCommand() throws Exception {
		logTestStart("testSendAndSubscribeToNullCommand");
		app1Client = new ApplicationClient();
		app1Client.connect();
		assertTrue("Client is connected", app1Client.isConnected());

		// Create the subscription
		AppCommandCallbackJson cmdCallback = new AppCommandCallbackJson();
		app1Client.registerCodec(new JsonCodec());
		app1Client.registerCommandCallback(cmdCallback);
		
		app1Client.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID);
		
		// Send a command
		boolean exceptionCaught = false;
		try {
			app1Client.publishCommand(DEVICE_TYPE, DEVICE_ID, "run", null);
			assertTrue("Publish null object failed", false);
		} catch (NullPointerException e) {
			exceptionCaught = true;
		}
		assertTrue("Publish null commandwas a failure", exceptionCaught);
		
		int count = 0;
		Command<JsonObject> evt = cmdCallback.getCommand();
		while( evt == null && count++ <= 5) {
			try {
				evt = cmdCallback.getCommand();
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("Null command is not received by application", (evt == null));
		app1Client.unsubscribeFromDeviceEvents(DEVICE_TYPE, DEVICE_ID);
	}

	@Test
	public void testSendAndSubscribeToJSONEvent() throws Exception {
		logTestStart("testSendAndSubscribeToJSONEvent");
		app1Client = new ApplicationClient();
		app1Client.connect();
		assertTrue("Client is connected", app1Client.isConnected());

		// Create the subscription
		AppEventCallbackJson evtCallback = new AppEventCallbackJson();
		app1Client.registerCodec(new JsonCodec());
		app1Client.registerEventCallback(evtCallback);
		app1Client.subscribeToDeviceEvents(DEVICE_TYPE, DEVICE_ID);
		
		// Send an event
		JsonObject data = new JsonObject();
		data.addProperty("distance", 10);
		boolean success = app1Client.publishEvent(DEVICE_TYPE, DEVICE_ID, "run", data);
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
		assertEquals(DEVICE_TYPE, evt.getTypeId());
		assertEquals(DEVICE_ID, evt.getDeviceId());
		assertEquals(10, evt.getData().get("distance").getAsInt());
		
		app1Client.unsubscribeFromDeviceEvents(DEVICE_TYPE, DEVICE_ID);
	}	

	@Test
	public void testSendAndSubscribeToUTF8Event() throws Exception {
		logTestStart("testSendAndSubscribeToUTF8Event");
		app1Client = new ApplicationClient();
		app1Client.connect();
		assertTrue("Client is connected", app1Client.isConnected());

		// Create the subscription
		AppEventCallbackUtf8 evtCallback = new AppEventCallbackUtf8();
		app1Client.registerCodec(new Utf8Codec());
		app1Client.registerEventCallback(evtCallback);
		app1Client.subscribeToDeviceEvents(DEVICE_TYPE, DEVICE_ID);
		
		// Send an event
		String data = "Hi Dave, this is fun, isn't it?";
		boolean success = app1Client.publishEvent(DEVICE_TYPE, DEVICE_ID, "run", data);
		assertTrue("Publish was a success", success);
		
		int count = 0;
		Event<String> evt = evtCallback.getEvent();
		while( evt == null && count++ <= 10) {
			try {
				evt = evtCallback.getEvent();
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("Event is received by application", (evt != null));
		assertEquals(DEVICE_TYPE, evt.getTypeId());
		assertEquals(DEVICE_ID, evt.getDeviceId());
		assertEquals(data, evt.getData());
		
		app1Client.unsubscribeFromDeviceEvents(DEVICE_TYPE, DEVICE_ID);
	}	

	@Test
	public void testSendAndSubscribeToNullEvent() throws Exception {
		logTestStart("testSendAndSubscribeToNullEvent");
		app1Client = new ApplicationClient();
		app1Client.connect();
		assertTrue("Client is connected", app1Client.isConnected());

		// Create the subscription
		AppEventCallbackJson evtCallback = new AppEventCallbackJson();
		app1Client.registerCodec(new JsonCodec());
		app1Client.registerEventCallback(evtCallback);
		app1Client.subscribeToDeviceEvents(DEVICE_TYPE, DEVICE_ID);
		
		// Send an event
		boolean exceptionCaught = false;
		try {
			app1Client.publishEvent(DEVICE_TYPE, DEVICE_ID, "run", null);
			assertTrue("Publish null object failed", false);
		} catch (NullPointerException e) {
			exceptionCaught = true;
		}
		assertTrue("Publish null event was a failure", exceptionCaught);
		
		int count = 0;
		Event<JsonObject> evt = evtCallback.getEvent();
		while( evt == null && count++ <= 5) {
			try {
				evt = evtCallback.getEvent();
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("Null Event is not received by application", (evt == null));
		app1Client.unsubscribeFromDeviceEvents(DEVICE_TYPE, DEVICE_ID);
	}
}
