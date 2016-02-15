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
package com.ibm.iotf.devicemgmt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import com.ibm.iotf.util.LoggerUtility;

/**
 * <p>If a Gateway or Device supports device actions like reboot and factory reset, 
 * this abstract class <code>DeviceActionHandler</code>
 * should be extended by the Gateway or Device code.</p>  
 * 
 * <p>The {@link com.ibm.iotf.devicemgmt.DeviceActionHandler#handleReboot} and 
 * {@link com.ibm.iotf.devicemgmt.DeviceActionHandler#handleFactoryReset}
 *  must be implemented by the subclass to handle the actions sent by the IBM Watson IoT Platform.</p>
 *
 */
public abstract class DeviceActionHandler extends Thread implements PropertyChangeListener {
	
	private static final String CLASS_NAME = DeviceActionHandler.class.getName();
	
	private static final int REBOOT_ACTION = 0;
	private static final int FACTORY_RESET_ACTION = 1;
	
	private BlockingQueue<Event> queue;
	private Event dummy = new Event();
	private boolean running = false;
	
	private class Event {
		private int action;
		private DeviceAction deviceAction;
	}
	
	public DeviceActionHandler() {
		queue = new LinkedBlockingQueue<Event>();
		this.setName("ActionHandler-Thread");
	}
	
	@Override
	public void run() {
		
		final String METHOD = "run";
		running = true;
		while (running) {
			try {
				Event evt = queue.take();
				if (evt == dummy) {
					LoggerUtility.info(CLASS_NAME, METHOD,  "Is it time to quit?");
				} else {
					if (evt.action == REBOOT_ACTION) {
						LoggerUtility.info(CLASS_NAME, METHOD,  "Invoking reboot handler");
						handleReboot(evt.deviceAction);
						evt.deviceAction.fireEvent(DeviceAction.DEVICE_REBOOT_STOP);
					} else if (evt.action == FACTORY_RESET_ACTION) {
						LoggerUtility.info(CLASS_NAME, METHOD,  "Invoking factory reset handler");
						handleFactoryReset(evt.deviceAction);
						evt.deviceAction.fireEvent(DeviceAction.DEVICE_FACTORY_RESET_STOP);
					}
				}
			} catch (InterruptedException e) {
				LoggerUtility.severe(CLASS_NAME, METHOD, "Unexpected exception " + e.getMessage());
			}
		}
		LoggerUtility.info(CLASS_NAME, METHOD,  "Exiting...");
	}

	public void terminate() {
		running = false;
		try {
			queue.put(dummy);
		} catch (InterruptedException e) {
		}
	}

	@Override
	/**
	 * This method listens for the device action events and calls the
	 * appropriate methods to complete the action. 
	 */
	public void propertyChange(PropertyChangeEvent pce) {
		final String METHOD = "propertyChange";
		try {
			String propName = pce.getPropertyName();
			if (propName.equals(DeviceAction.DEVICE_REBOOT_START)) {
				Event evnt = new Event();
				evnt.action = REBOOT_ACTION;
				evnt.deviceAction = (DeviceAction) pce.getNewValue();
				queue.put(evnt);
			} else if (propName.equals(DeviceAction.DEVICE_FACTORY_RESET_START)) {
				Event evnt = new Event();
				evnt.action = FACTORY_RESET_ACTION;
				evnt.deviceAction = (DeviceAction) pce.getNewValue();
				queue.put(evnt);
			}
		} catch (Exception e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Unexpected exception " + e.getMessage());
		}
	}
	
	/**
	 * Subclass must implement this method.  
	 * <p>If the device supports reboot, subclass must add logic to reboot the 
	 * device.  
	 *<br>
	 *<br>
	 * If reboot attempt fails, the "rc" is set to 500 and the "message" 
	 * field should be set accordingly, if the reboot is not supported, 
	 * set "rc" to 501 and optionally set "message" accordingly</p>
	 * 
	 * <p>Gateway must use the class {@link com.ibm.iotf.devicemgmt.DeviceAction} to retrieve the 
	 *  DeviceType and DeviceId for which the reboot request is received and act accordingly.</p>
	 * 
	 * @param action DeviceAction where the device code can set the failure status and message
	 * @see DeviceAction
	 */
	public abstract void handleReboot(DeviceAction action);
	
	/**
	 * Subclass must implement this method.  
	 * <p>If the device supports factory reset, subclass must add logic to reset the 
	 * device to factory settings
	 *<br>
	 *<br>
	 * If the factory reset attempt fails, the "rc" should be 500 and the "message" 
	 * field should be set accordingly, if the factory reset action is not supported, 
	 * set "rc" to 501 and optionally set "message" accordingly.</p>
	 * 
	 * <p>Gateway must use the class {@link com.ibm.iotf.devicemgmt.DeviceAction} to retrieve the 
	 *  DeviceType and DeviceId for which the Factory reset request is received and act accordingly.</p>
	 *  
 	 * @param action DeviceAction where the device code can set the failure status and message
	 * @see DeviceAction
	 */
	public abstract void handleFactoryReset(DeviceAction action);

}
