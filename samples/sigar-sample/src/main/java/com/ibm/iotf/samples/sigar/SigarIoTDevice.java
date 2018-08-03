package com.ibm.iotf.samples.sigar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.LogManager;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.ibm.iotf.client.device.DeviceClient;

public class SigarIoTDevice implements Runnable {
	
	private boolean quit = false;
	private Sigar sigar = null;
	private Properties options = new Properties();
	protected DeviceClient client;
	
	public SigarIoTDevice(String configFilePath) throws Exception {
		this.sigar = new Sigar();
		this.options = DeviceClient.parsePropertiesFile(new File(configFilePath));
		this.client = new DeviceClient(this.options);
	}

	public SigarIoTDevice() throws Exception {
		this.sigar = new Sigar();
		this.options.put("org", "quickstart");
		this.options.put("type", "sigar");
		this.options.put("id", generateIdFromMacAddress());
		this.client = new DeviceClient(this.options);
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
			try {
				client.connect();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			System.out.println("Start publishing event every second...");
			// Send a dataset every 1 second, until we are told to quit
			while (!quit) {
				SigarData data = SigarData.create(sigar);
				System.out.println(data);
				client.publishEvent("sigar", data);
				Thread.sleep(1000);
			}
			
			// Once told to stop, cleanly disconnect from the service
			client.disconnect();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static class LauncherOptions {
		@Option(name="-c", aliases={"--config"}, usage="The path to a device configuration file")
		public String configFilePath = null;
		
		public LauncherOptions() {} 
	}
	
	public static void main(String[] args) throws Exception {
		// Load custom logging properties file
	    try {
			FileInputStream fis =  new FileInputStream("logging.properties");
			LogManager.getLogManager().readConfiguration(fis);
		} catch (SecurityException e) {
		} catch (IOException e) {
		}
	    
	    LauncherOptions opts = new LauncherOptions();
        CmdLineParser parser = new CmdLineParser(opts);
        try {
        	parser.parseArgument(args);
        } catch (CmdLineException e) {
            // Handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            System.exit(1);
        }
		
	    // Start the device thread
		SigarIoTDevice d;

		if (opts.configFilePath != null) {
			d = new SigarIoTDevice(opts.configFilePath);
			Thread t1 = new Thread(d);
			t1.start();

			System.out.println("Connected successfully - Your device ID is " + d.client.getDeviceId());
			System.out.println(" * Organization: " + d.client.getOrgId());			
		} else {
			d = new SigarIoTDevice();
			Thread t1 = new Thread(d);
			t1.start();
			
			System.out.println("Connected successfully - Your device ID is " + d.client.getDeviceId());
			System.out.println(" * http://quickstart.internetofthings.ibmcloud.com/?deviceId=" + d.client.getDeviceId());
			System.out.println("Visit the QuickStart portal to see this device's data visualized in real time and learn more about the IBM Internet of Things Cloud");
		}
		
		System.out.println("");
		System.out.println("(Press <enter> to disconnect)");

		// Wait for <enter>
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		sc.close();
		
		System.out.println("Closing connection to the IBM Internet of Things Cloud service");
		// Let the device thread know it can terminate
		d.quit();
	}
	
}