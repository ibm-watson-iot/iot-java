package com.ibm.iotf.test.common.handlers;

import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmwareHandler;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.wiotp.sdk.devicemgmt.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.wiotp.sdk.devicemgmt.device.ManagedDevice;
import com.ibm.wiotp.sdk.util.LoggerUtility;

public class TestDeviceFirmwareHandler extends DeviceFirmwareHandler {
	private static final String CLASS_NAME = TestDeviceFirmwareHandler.class.getName();
	private volatile String name = null;
	private volatile String version = null;
	private volatile String url = null;
	private volatile String verifier = null;
	
	private volatile boolean firmwareUpdateCalled = false;
	private volatile boolean firmwaredownloaded = false;
	
	public String getDeviceFirmwareName() { return name; }
	public String getDeviceFirmwareVersion() { return version; }
	public String getDeviceFirmwareURL() { return url; }
	public String getDeviceFirmwareVerifier() { return verifier; }
	
	public boolean firmwareDownloaded() { return firmwaredownloaded; }
	public boolean firmwareUpdated() { return firmwareUpdateCalled; }
	
	private ManagedDevice dmClient = null;
	
	public TestDeviceFirmwareHandler(ManagedDevice dmClient) {
		this.dmClient = dmClient;
	}
	
	@Override
	public void downloadFirmware(final DeviceFirmware deviceFirmware) {
		final String METHOD = "downloadFirmware";
		LoggerUtility.info(CLASS_NAME, METHOD, "download firmware initiated for client ID " + dmClient.getConfig().getClientId());
		new Thread() {
			public void run() {
			
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
				deviceFirmware.setState(FirmwareState.DOWNLOADED);
				
				name = deviceFirmware.getName();
				version = deviceFirmware.getVersion();
				url = deviceFirmware.getUrl();
				verifier = deviceFirmware.getVerifier();
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Fake completion
				//deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
				deviceFirmware.setState(FirmwareState.DOWNLOADED);
				firmwaredownloaded = true;
			}
		}.start();
	}

	@Override
	public void updateFirmware(final DeviceFirmware deviceFirmware) {
		final String METHOD = "updateFirmware";
		LoggerUtility.info(CLASS_NAME, METHOD, "update firmware initiated for client ID " + dmClient.getConfig().getClientId());
		new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Pretend that the update is successful
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
				deviceFirmware.setState(FirmwareState.IDLE);
				firmwareUpdateCalled = true;
			}
		}.start();
	}

}
