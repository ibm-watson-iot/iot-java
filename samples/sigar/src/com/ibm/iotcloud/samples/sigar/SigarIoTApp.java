package com.ibm.iotcloud.samples.sigar;

import java.io.File;
import java.util.Properties;

import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.ibm.iotcloud.client.Application;
import com.ibm.iotcloud.client.Device;
import com.ibm.iotcloud.client.DeviceEventCallback;
import com.ibm.iotcloud.client.Event;

public class SigarIoTApp implements Runnable {
	
	private boolean quit = false;
	private Sigar sigar = null;
	private Properties options = new Properties();
	protected Application client;
	
	public SigarIoTApp(String configFilePath) throws Exception {
		this.sigar = new Sigar();
		this.options = Application.parsePropertiesFile(new File(configFilePath));
		this.client = new Application(this.options);
	}

	public SigarIoTApp() throws Exception {
		this.sigar = new Sigar();
		this.options.put("org", "quickstart");
		this.options.put("type", "sigar");
		this.options.put("id", generateIdFromMacAddress() + "_app");
		this.client = new Application(this.options);
	}
	
	private String generateIdFromMacAddress() {
		// Try to obtain a(ny) MAC Address on this system
		NetInterfaceConfig config = null;
		String macAddress = null;
		String deviceId;
		try {
			config = sigar.getNetInterfaceConfig(null);
			macAddress = config.getHwaddr();
		} catch (SigarException e) {
			System.out.println("Problem getting MAC address: " + e.toString());
		}
		
		// If we couldn't find a MAC address then we can't assign a Device ID
		if (macAddress == null) {
			deviceId = null;
		} else {
			deviceId = macAddress.toLowerCase().replaceAll(":", "");
		}
		
		return deviceId;
	}

	public void quit() {
		this.quit = true;
	}
	
	public void run() {
		try {
			client.connect();
			client.setDeviceEventCallback(new MyEventCallback());
			client.subscribeToDeviceEvents();
			// Send a dataset every 1 second, until we are told to quit
			while (!quit) {
				Thread.sleep(1000);
			}
			
			// Once told to stop, cleanly disconnect from the service
			client.disconnect();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class MyEventCallback implements DeviceEventCallback {

		@Override
		public void processEvent(Event e) {
			System.out.println(e.getTopic() + "--" + e.getPayload());
		}
		
	}
}