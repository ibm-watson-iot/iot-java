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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Scanner;

import com.ibm.iotf.devicemgmt.device.DeviceFirmware;
import com.ibm.iotf.devicemgmt.device.DeviceFirmwareHandler;
import com.ibm.iotf.devicemgmt.device.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.device.DeviceFirmware.FirmwareUpdateStatus;

/**
 * This sample Firmware handler demonstrates how one can download and 
 * apply a firmware image in simple steps.
 * 
 * 1. downloadFirmware method is invoked whenever there is a Firmware download
 *    request from the IoT Foundation server. In this example, we try to download
 *    a debian file using HTTP methods.
 *   
 * 2. updateFirmware method is invoked whenever there is a update firmware request
 *    from the IoT Foundation server. In this example, it tries to install the 
 *    debian package that is download.
 */
public class RasPiFirmwareHandlerSample extends DeviceFirmwareHandler {
	
	private static final String CLASS_NAME = RasPiFirmwareHandlerSample.class.getName();
	private static final String DEPENDENCY_ERROR_MSG = "dependency problems - leaving unconfigured";
	private static final String ERROR_MSG = "Errors were encountered while processing";
	private static final String INSTALL_LOG_FILE = "install.log";
	
	private enum InstalStatus {
		SUCCESS(0),
		DEPENDENCY_ERROR(2),
		ERROR(3);
		
		private int status;
		
		private InstalStatus(int status) {
			this.status = status;
		}
		
	}
	
	private String downloadedFirmwareName;
	
	public RasPiFirmwareHandlerSample() {
	}

	/**
	 * A sample method that downloads a firmware image (a debian file) from a HTTP server
	 * 
	 */
	@Override
	public void downloadFirmware(DeviceFirmware deviceFirmware) {
		
		System.out.println(CLASS_NAME + ": Firmware Download start...");
		boolean success = false;
		URL firmwareURL = null;
		URLConnection urlConnection = null;
		
		/**
		 * start downloading the firmware image
		 */
		try {
			System.out.println(CLASS_NAME + ": Downloading Firmware from URL " + deviceFirmware.getUrl());
			
			firmwareURL = new URL(deviceFirmware.getUrl());
			urlConnection = firmwareURL.openConnection();
			if(deviceFirmware.getName() != null &&
					!"".equals(deviceFirmware.getName())) {
				downloadedFirmwareName = deviceFirmware.getName();
			} else {
				// use the timestamp as the name
				downloadedFirmwareName = "firmware_" +new Date().getTime()+".deb";
			}
			
			File file = new File(downloadedFirmwareName);
			BufferedInputStream bis = new BufferedInputStream(urlConnection.getInputStream());
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file.getName()));
			
			int data = bis.read();
			if(data != -1) {
				bos.write(data);
				byte[] block = new byte[1024];
				while (true) {
					int len = bis.read(block, 0, block.length);
					if(len != -1) {
						bos.write(block, 0, len);
					} else {
						break;
					}
				}
				bos.close();
				bis.close();
				
				success = true;
			} else {
				//There is no data to read, so throw an exception
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.INVALID_URI);
			}
			
			// Verify the firmware image if verifier is set
			if(deviceFirmware.getVerifier() != null && !deviceFirmware.getVerifier().equals("")) {
				success = verifyFirmware(file, deviceFirmware.getVerifier());
				
				/**
				 * As per the documentation, If a firmware verifier has been set, the device should 
				 * attempt to verify the firmware image. 
				 * 
				 * If the image verification fails, mgmt.firmware.state should be set to 0 (Idle) 
				 * and mgmt.firmware.updateStatus should be set to the error status value 4 (Verification Failed).
				 */
				if(success == false) {
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.VERIFICATION_FAILED);
					// the firmware state is updated to IDLE below
				}
			}
			
		} catch(MalformedURLException me) {
			// Invalid URL, so set the status to reflect the same,
			deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.INVALID_URI);
			me.printStackTrace();
		} catch (IOException e) {
			deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.CONNECTION_LOST);
			e.printStackTrace();
		} catch (OutOfMemoryError oom) {
			deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.OUT_OF_MEMORY);
		}
		
		/**
		 * Set the firmware download and possibly the firmware update status
		 * (will be sent later) accordingly
		 */
		if(success == true) {
			deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
			deviceFirmware.setState(FirmwareState.DOWNLOADED);
		} else {
			deviceFirmware.setState(FirmwareState.IDLE);
		}
		
		System.out.println(CLASS_NAME + ": Firmware Download END...("+success+ ")");
	}

	private boolean verifyFirmware(File file, String verifier) throws IOException {
		FileInputStream fis = null;
		String md5 = null;
		try {
			fis = new FileInputStream(file);
			md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			System.out.println("Downloaded Firmware MD5 sum:: "+ md5);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fis.close();
		}
		if(verifier.equals(md5)) {
			System.out.println("Firmware verification successful");
			return true;
		}
		System.out.println("Download firmware checksum verification failed.. "
				+ "Expected "+verifier + " found "+md5);
		return false;
	}

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
	@Override
	public void updateFirmware(DeviceFirmware deviceFirmware) {
		try {
			System.out.println(CLASS_NAME + ": Firmware update start...");
			
			ProcessBuilder pkgInstaller = null;
			ProcessBuilder dependencyInstaller = null;
			Process p = null;
			pkgInstaller = new ProcessBuilder("sudo", "dpkg", "-i", this.downloadedFirmwareName);
			pkgInstaller.redirectErrorStream(true);
			pkgInstaller.redirectOutput(new File(INSTALL_LOG_FILE));
			
			dependencyInstaller = new ProcessBuilder("sudo", "apt-get", "-fy", "install");
			dependencyInstaller.redirectErrorStream(true);
			dependencyInstaller.inheritIO();

			boolean success = false;
			try {
				p = pkgInstaller.start();
				boolean status = waitForCompletion(p, 5);
				if(status == false) {
					p.destroy();
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
					return;
				}
				// check for install error
				InstalStatus instalStatus = ParseInstallLog();
				if(instalStatus == InstalStatus.DEPENDENCY_ERROR) {
					System.err.println("Following dependency error occured while "
							+ "installing the image "+this.downloadedFirmwareName);
					System.err.println(getInstallLog());
					
					System.out.println("Trying to update the dependency with the following command...");
					System.out.println("sudo apt-get -fy install");
					p = dependencyInstaller.start();
					status = waitForCompletion(p, 5);
				} else if(instalStatus == InstalStatus.ERROR) {
					System.err.println("Following error occured while "
							+ "installing the image "+this.downloadedFirmwareName);
					System.err.println(getInstallLog());
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
					return;
				}
				System.out.println("Firmware Update command "+status);
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
				deviceFirmware.setState(FirmwareState.IDLE);
			} catch (IOException e) {
				e.printStackTrace();
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
			} catch (InterruptedException e) {
				e.printStackTrace();
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
			}
		} catch (OutOfMemoryError oom) {
			deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.OUT_OF_MEMORY);
		}
		
		/**
		 * Delete the temporary firmware file
		 */
		try {
			Path path = new File(downloadedFirmwareName).toPath();
			Files.deleteIfExists(path);
			path = new File(INSTALL_LOG_FILE).toPath();
			Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

		downloadedFirmwareName = null;
		System.out.println(CLASS_NAME + ": Firmware update End...");
	}
	
	/**
	 * Since JDK7 doesn't take any timeout parameter, we provide an workaround
	 * that wakes up every second and checks for the completion status of the process.
	 * @param process
	 * @param minutes
	 * @return
	 * @throws InterruptedException 
	 */
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

	
	private InstalStatus ParseInstallLog() throws FileNotFoundException {
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

}
