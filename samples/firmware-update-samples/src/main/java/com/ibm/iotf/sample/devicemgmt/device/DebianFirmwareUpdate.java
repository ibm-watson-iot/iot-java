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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareUpdateStatus;

/**
 * A sample firmware update method that installs the debian package
 * with the following command
 * 
 * sudo dpkg -i /path/to/filename.deb
 * 
 * If this fails with a message about the package depending on something 
 * that isn't installed, then fix it by running the following command,
 * 
 * sudo apt-get -fy install
 * 
 * This will install the dependencies (assuming they're available in the 
 * repos your system knows about) AND the package that were originally 
 * requested to install ('f' is the 'fix' option and 'y' is the 'assume 
 * yes to prompts' or 'don't ask me if it's ok, just install it already' 
 * option -- very useful for scripted silent installs). 
 * 
 */
public class DebianFirmwareUpdate {
	
	private final static String CLASS_NAME =  DebianFirmwareUpdate.class.getName();
	
	private static final String DEPENDENCY_ERROR_MSG = "dependency problems - leaving unconfigured";
	private static final String ERROR_MSG = "Errors were encountered while processing";
	private static final String INSTALL_LOG_FILE = "install.log";

	private DeviceFirmware deviceFirmware;
	private boolean requirePlatformUpdate = false;

	public DebianFirmwareUpdate(boolean requirePlatformUpdate) {
		this.requirePlatformUpdate = requirePlatformUpdate;
	}
	
	public DebianFirmwareUpdate(DeviceFirmware deviceFirmware, boolean requirePlatformUpdate) {
		this.deviceFirmware = deviceFirmware;
		this.requirePlatformUpdate = requirePlatformUpdate;
	}

	private enum InstalStatus {
		SUCCESS(0),
		DEPENDENCY_ERROR(2),
		ERROR(3);
		
		private int status;
		
		private InstalStatus(int status) {
			this.status = status;
		}
		
	}	
	
	public boolean updateFirmware(String firmwareFile) {
		try {
			System.out.println(CLASS_NAME + ": Firmware update start...");
			
			ProcessBuilder pkgInstaller = null;
			ProcessBuilder dependencyInstaller = null;
			Process p = null;
			pkgInstaller = new ProcessBuilder("sudo", "dpkg", "-i", firmwareFile);
			pkgInstaller.redirectErrorStream(true);
			pkgInstaller.redirectOutput(new File(INSTALL_LOG_FILE));
			
			dependencyInstaller = new ProcessBuilder("sudo", "apt-get", "-fy", "install");
			dependencyInstaller.redirectErrorStream(true);
			dependencyInstaller.inheritIO();

			try {
				p = pkgInstaller.start();
				boolean status = waitForCompletion(p, 1);
				if(status == false) {
					p.destroy();
					
					// Update the status information to Watson IoT Platform only when the requested firmware update fails. 
					// i.e don't update when we fallback.
					if(requirePlatformUpdate) {
						deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
						deviceFirmware.setState(FirmwareState.IDLE);
					}
					return false;
				}
				// check for install error
				InstalStatus instalStatus = ParseInstallLog();
				if(instalStatus == InstalStatus.DEPENDENCY_ERROR) {
					System.err.println("Following dependency error occured while "
							+ "installing the image "+firmwareFile);
					System.err.println(getInstallLog());
					
					System.out.println("Trying to update the dependency with the following command...");
					System.out.println("sudo apt-get -fy install");
					p = dependencyInstaller.start();
					status = waitForCompletion(p, 1);
				} else if(instalStatus == InstalStatus.ERROR) {
					System.err.println("Following error occured while "
							+ "installing the image "+firmwareFile);
					System.err.println(getInstallLog());
					if(requirePlatformUpdate) {
						deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
						deviceFirmware.setState(FirmwareState.IDLE);
					}
					return false;
				}
				System.out.println("Firmware Update command "+status);
				if(requirePlatformUpdate) {
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
					deviceFirmware.setState(FirmwareState.IDLE);
				}
			} catch (IOException e) {
				e.printStackTrace();
				if(requirePlatformUpdate) {
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
					deviceFirmware.setState(FirmwareState.IDLE);
				}
				return false;
			} catch (InterruptedException e) {
				e.printStackTrace();
				if(requirePlatformUpdate) {
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
					deviceFirmware.setState(FirmwareState.IDLE);
				}
				return false;
			}
		} catch (OutOfMemoryError oom) {
			if(requirePlatformUpdate) {
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.OUT_OF_MEMORY);
				deviceFirmware.setState(FirmwareState.IDLE);
			}
			return false;
		}
		
		/**
		 * Delete the temporary firmware file
		 */
		try {
			Path path = new File(INSTALL_LOG_FILE).toPath();
			Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(CLASS_NAME + ": Firmware update End...");
		return true;
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
	
	
	private static InstalStatus ParseInstallLog() throws FileNotFoundException {
		try {
			File file = new File(INSTALL_LOG_FILE);
		    Scanner scanner = new Scanner(file);
	
		    while (scanner.hasNextLine()) {
		        String line = scanner.nextLine();
		        if(line.contains(DEPENDENCY_ERROR_MSG)) {
		        	scanner.close();
		            return InstalStatus.DEPENDENCY_ERROR;
		        } else if(line.contains(ERROR_MSG)) {
		        	scanner.close();
		        	return InstalStatus.ERROR;
		        }
		    }
		    scanner.close();
		} catch(FileNotFoundException e) { 
		    throw e;
		}
		return InstalStatus.SUCCESS;
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

	
	public void setDeviceFirmware(DeviceFirmware deviceFirmware) {
		this.deviceFirmware = deviceFirmware;
	}
	
}
