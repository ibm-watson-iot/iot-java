/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
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
package com.ibm.iotf.sample.devicemgmt.device;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ibm.iotf.devicemgmt.device.DeviceAction;
import com.ibm.iotf.devicemgmt.device.DeviceActionHandler;

/**
 * This sample Device action handler demonstrates how one can reboot the device 
 * 
 */
public class DeviceActionHandlerSample extends DeviceActionHandler {
	
	public DeviceActionHandlerSample() {
	}

	/**
	 * If reboot attempt fails, set status to FAILED and the "message" 
	 * field should be set accordingly, if the reboot is not supported, 
	 * set status to NOTSUPPORTED and optionally set "message" accordingly
	 */
	@Override
	public void handleReboot(DeviceAction action) {
		
		ProcessBuilder processBuilder = null;
		Process p = null;
		
		String osname = System.getProperty("os.name");
		
		if(osname.startsWith("Windows")) {
			processBuilder = new ProcessBuilder("shutdown", "-r");
		} else {
			processBuilder = new ProcessBuilder("sudo", "shutdown", "-r", "now");
		}

		processBuilder.redirectErrorStream(true);
		processBuilder.inheritIO();
		
		
		boolean status = false;
		try {
			p = processBuilder.start();
			// wait for say 2 minutes before giving it up
			status = waitForCompletion(p, 2);
			System.out.println("Executed restart command "+status);
		} catch (IOException e) {
			action.setMessage(e.getMessage());
		} catch (InterruptedException e) {
			action.setMessage(e.getMessage());
		}
		
		System.out.println("Executed restart command status ("+status+")");
		if(status == false) {
			action.setStatus(DeviceAction.Status.FAILED);
		}
		
	}

	/**
	 * If factory reset attempt fails, set status to FAILED and the "message" 
	 * field should be set accordingly, if the factory reset is not supported, 
	 * set status to NOTSUPPORTED and optionally set "message" accordingly
	 */
	@Override
	public void handleFactoryReset(DeviceAction action) {
		
		/**
		 * This sample doesn't support factory reset, so respond accordingly
		 */
		action.setStatus(DeviceAction.Status.UNSUPPORTED);
	}
	
	/**
	 * Since JDK7 doesn't take any timeout parameter, we provide an workaround
	 * that wakes up every second and checks for the completion status of the process.
	 * @param process
	 * @param minutes
	 * @return
	 * @throws InterruptedException 
	 */
	private static boolean waitForCompletion(Process process, int minutes) throws InterruptedException {
		long timeToWait = (60 * minutes);
		
		int exitValue = -1;
		for(int i = 0; i < timeToWait; i++) {
			try {
				exitValue = process.exitValue();
			} catch(IllegalThreadStateException  e) {
				// Process is still running
			}
			if(exitValue == 0) {
				return true;
			}
			Thread.sleep(1000);
		}
		// Destroy the process forcibly
		try {
			process.destroy();
		} catch(Exception e) {}
	
		return false;
	}

}
