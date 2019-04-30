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
package com.ibm.iotf.client.app;

/**
 * This is an interface to capture callbacks (either events or commands) 
 *
 */
public interface EventCallback {

	/**
	 * This method processes Event passed
	 * @param evt
	 * 			an object of Event which represents the Event passed
	 */
	public void processEvent(Event evt);
	
	/**
	 * This method processes the command passed
	 * @param cmd
	 * 			an object of Command which represents the Command passed
	 */
	public void processCommand(Command cmd);
}
