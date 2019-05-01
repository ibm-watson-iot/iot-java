/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 Prasanna A Mathada - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.application;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.iotf.test.common.AbstractTest;
import com.ibm.iotf.test.common.callbacks.TestAppCommandCallback;
import com.ibm.iotf.test.common.callbacks.TestAppEventCallback;
import com.ibm.wiotp.sdk.app.ApplicationClient;
import com.ibm.wiotp.sdk.app.messages.Command;
import com.ibm.wiotp.sdk.app.messages.Event;

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
		TestAppCommandCallback cmdCallback = new TestAppCommandCallback();
		app1Client.setCommandCallback(cmdCallback);
		
		app1Client.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID);
		
		// Send a command
		JsonObject data = new JsonObject();
		data.addProperty("distance", 10);
		boolean success = app1Client.publishCommand(DEVICE_TYPE, DEVICE_ID, "run", data);
		assertTrue("Publish was a success", success);

		int count = 0;
		Command cmd = cmdCallback.getCommand();
		while( cmd == null && count++ <= 10) {
			try {
				cmd = cmdCallback.getCommand();
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("Command is received by application", (cmd != null));
	}	

	@Test
	public void testSendAndSubscribeToEvent() throws Exception {
		logTestStart("testSendAndSubscribeToEvent");
		app1Client = new ApplicationClient();
		app1Client.connect();
		assertTrue("Client is connected", app1Client.isConnected());

		// Create the subscription
		TestAppEventCallback evtCallback = new TestAppEventCallback();
		app1Client.setEventCallback(evtCallback);
		app1Client.subscribeToDeviceEvents(DEVICE_TYPE, DEVICE_ID);
		
		// Send an event
		JsonObject data = new JsonObject();
		data.addProperty("distance", 10);
		boolean success = app1Client.publishEvent(DEVICE_TYPE, DEVICE_ID, "run", data);
		assertTrue("Publish was a success", success);
		
		int count = 0;
		Event evt = evtCallback.getEvent();
		while( evt == null && count++ <= 10) {
			try {
				evt = evtCallback.getEvent();
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		assertTrue("Event is received by application", (evt != null));
	}	

}
