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
import java.util.Date;
import java.util.Properties;
import org.apache.commons.net.util.Base64;  


import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

/** 
 * A sample method that downloads a firmware image (a debian file) from a HTTP server
 */
public class HTTPFirmwareDownload {
	
	private final static String PROPERTIES_FILE_NAME = "/DMDeviceSample.properties";
	private final static String CLASS_NAME =  HTTPFirmwareDownload.class.getName();
	
	private DeviceFirmware deviceFirmware;
	private boolean requirePlatformUpdate = false;
	ManagedDevice dmClient;

	
	public HTTPFirmwareDownload(boolean requirePlatformUpdate, ManagedDevice dmClient) {
		this.requirePlatformUpdate = requirePlatformUpdate;
		this.dmClient = dmClient;
	}

	public String downloadFirmware() {
		System.out.println(CLASS_NAME + ": Firmware Download start...");
		boolean success = false;
		URL firmwareURL = null;
		URLConnection urlConnection = null;
		String downloadedFirmwareName = null;
		
		Properties props = new Properties();
		try {
			props.load(HTTPFirmwareDownload.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
					
		String username = trimedValue(props.getProperty("User-Name"));
		String password = trimedValue(props.getProperty("Password"));
		String dbName = trimedValue(props.getProperty("Repository-DB"));
		
		/**
		 * start downloading the firmware image
		 */
		try {
			System.out.println(CLASS_NAME + ": Downloading Firmware from URL " + deviceFirmware.getUrl());
			
			firmwareURL = new URL(deviceFirmware.getUrl());
			urlConnection = firmwareURL.openConnection();

			byte[] encoding = Base64.encodeBase64(new String(username + ":" + password).getBytes() );  
			String encodedString = "Basic " + new String(encoding);  

			urlConnection.setRequestProperty ("Authorization", encodedString);
			
			int fileSize = urlConnection.getContentLength();
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
			
			// count the download size to send the progress report as DiagLog to Watson IoT Platform
			int downloadedSize = 0;
			
			int data = bis.read();
			downloadedSize += 1;

			if(data != -1) {
				bos.write(data);
				byte[] block = new byte[1024 * 10];
				int previousProgress = 0;
				while (true) {
					int len = bis.read(block, 0, block.length);
					downloadedSize = downloadedSize + len;
					if(len != -1) {
						// Send the progress update
						if(fileSize > 0) {
							int progress = (int) (((float)downloadedSize / fileSize) * 100);
							if(progress > previousProgress) {
								String message = "Firmware Download progress: "+progress + "%";
								addDiagLog(message, new Date(), LogSeverity.informational);
								System.out.println(message);
							}
						} else {
							// If we can't retrieve the filesize, let us update how much we have download so far
							String message = "Downloaded : "+ downloadedSize + " bytes so far";
							addDiagLog(message, new Date(), LogSeverity.informational);
							System.out.println(message);
						}
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
				if(requirePlatformUpdate) {
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.INVALID_URI);
				}
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
					if(requirePlatformUpdate) {
						deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.VERIFICATION_FAILED);
					}
					// the firmware state is updated to IDLE below
				}
			}
			
		} catch(MalformedURLException me) {
			// Invalid URL, so set the status to reflect the same,
			if(requirePlatformUpdate) {
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.INVALID_URI);
			}
			me.printStackTrace();
		} catch (IOException e) {
			if(requirePlatformUpdate) {
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.CONNECTION_LOST);
			}
			e.printStackTrace();
		} catch (OutOfMemoryError oom) {
			if(requirePlatformUpdate) {
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.OUT_OF_MEMORY);
			}
		}
		
		/**
		 * Set the firmware download and possibly the firmware update status
		 * (will be sent later) accordingly
		 */
		if(success == true) {
			if(requirePlatformUpdate) {
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
				deviceFirmware.setState(FirmwareState.DOWNLOADED);
			}
		} else {
			if(requirePlatformUpdate) {
				deviceFirmware.setState(FirmwareState.IDLE);
			}
			return null;
		}
		
		System.out.println(CLASS_NAME + ": Firmware Download END...("+success+ ")");
		return downloadedFirmwareName;
	}

	private void addDiagLog(String message, Date date, LogSeverity severity) {
		this.dmClient.addLog(message, date, severity);
	}
	
	/**
	 * The verifyFirmware() method verifies if the downloaded firmware is complete and intact.
	 * It requires the checksum to be passed, which shall be used to compare with the checksum
	 * of the downloaded firmware.
	 * @param file
	 * @param verifier
	 * @return
	 * @throws IOException
	 */
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

	public void setDeviceFirmware(DeviceFirmware deviceFirmware) {
		this.deviceFirmware = deviceFirmware;
	}
	
	private String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}

}
	

