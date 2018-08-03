/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 Prasanna Alur Mathada - Initial Contribution
 *****************************************************************************
 *
 */

package com.ibm.iotf.sample.devicemgmt.device;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ibm.iotf.devicemgmt.DeviceAction;
import com.ibm.iotf.devicemgmt.DeviceActionHandler;
import com.ibm.iotf.devicemgmt.DeviceAction.Status;


/**
 * This sample Device action handler demonstrates how one can reboot the device 
 * 
 */
public class DeviceActionHandlerSample extends DeviceActionHandler {
	private static final String CLASS_NAME = DeviceActionHandlerSample.class.getName();
	private static String FACTORY_FIRMWARE_NAME = "iot_1.0-1_armhf.deb";
	private static final String INSTALL_LOG_FILE = "install.log";
	
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public DeviceActionHandlerSample() {
	}

	/**
	 * If reboot attempt fails, set status to FAILED and the "message" 
	 * field should be set accordingly, if the reboot is not supported, 
	 * set status to NOTSUPPORTED and optionally set "message" accordingly
	 */
	private static class RebootTask implements Runnable {
		
		private DeviceAction action;
		private DeviceActionHandlerSample handler;

		public RebootTask(DeviceAction deviceAction, DeviceActionHandlerSample handler) {
			this.action = deviceAction;
			this.handler = handler;
		}

		@Override
		public void run() {

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
	}
	
	@Override
	public void handleReboot(DeviceAction action) {
		// set the support before handing over to the pool
		action.setStatus(Status.ACCEPTED);
		RebootTask task = new RebootTask(action, this);
		executor.execute(task);
	}

	/**
	 * If factory reset attempt fails, set status to FAILED and the "message" 
	 * field should be set accordingly, if the factory reset is not supported, 
	 * set status to NOTSUPPORTED and optionally set "message" accordingly
	 */
	private static class FactoryResetTask implements Runnable {
		
		private DeviceAction action;
		private DeviceActionHandlerSample handler;

		public FactoryResetTask(DeviceAction deviceAction, DeviceActionHandlerSample handler) {
			this.action = deviceAction;
			this.handler = handler;
		}

		@Override
		public void run() {
			try {
				System.out.println(CLASS_NAME + ": Factory reset start...");
				
				ProcessBuilder pkgInstaller = null;
				Process p = null;
				pkgInstaller = new ProcessBuilder("sudo", "dpkg", "-i", FACTORY_FIRMWARE_NAME);
				pkgInstaller.redirectErrorStream(true);
				pkgInstaller.redirectOutput(new File(INSTALL_LOG_FILE));
				
				try {
					p = pkgInstaller.start();
					boolean status = waitForCompletion(p, 5);
					if(status == false) {
						p.destroy();
						// set failed status
						action.setStatus(Status.FAILED);
						// Optionally set a message
						action.setMessage("Not completed in time .. " + getInstallLog());
					}
					System.out.println("Install log -- "+getInstallLog());
					System.out.println("Factory reset status: "+status);
				} catch (IOException e) {
					e.printStackTrace();
					// set failed status
					action.setStatus(Status.FAILED);
					// Optionally set a message
					action.setMessage("Exception occured .. " + e.getMessage());

				} catch (InterruptedException e) {
					e.printStackTrace();
					// set failed status
					action.setStatus(Status.FAILED);
					// Optionally set a message
					action.setMessage("Exception occured .. " + e.getMessage());
				}
			} catch (OutOfMemoryError oom) {
				// set failed status
				action.setStatus(Status.FAILED);
				// Optionally set a message
				action.setMessage("Exception occured .. " + oom.getMessage());
			}
		}

	}
	
	@Override
	public void handleFactoryReset(DeviceAction action) {
		action.setStatus(Status.ACCEPTED);
		FactoryResetTask task = new FactoryResetTask(action, this);
		executor.execute(task);		
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

	private static String getInstallLog() throws FileNotFoundException {
		File file = new File(INSTALL_LOG_FILE);
	    Scanner scanner = new Scanner(file);
	    StringBuilder sb = new StringBuilder();
	    while (scanner.hasNextLine()) {
	        String line = scanner.nextLine();
	        sb.append(line);
	        sb.append('\n');
	    }
	    scanner.close();
	    return sb.toString();
	}
}