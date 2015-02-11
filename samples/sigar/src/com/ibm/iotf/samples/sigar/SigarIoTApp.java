package com.ibm.iotf.samples.sigar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.LogManager;

import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.ApplicationStatus;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.DeviceStatus;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.iotf.client.app.StatusCallback;


public class SigarIoTApp implements Runnable {
	
	private boolean quit = false;
	private Sigar sigar = null;
	private Properties options = new Properties();
	protected ApplicationClient client;
	
	public SigarIoTApp(String configFilePath) throws Exception {
		this.sigar = new Sigar();
		this.options = ApplicationClient.parsePropertiesFile(new File(configFilePath));
		this.client = new ApplicationClient(this.options);
	}

	public SigarIoTApp() throws Exception {
		this.sigar = new Sigar();
		this.options.put("org", "quickstart");
		this.options.put("type", "sigar");
		this.options.put("id", generateIdFromMacAddress() + "_app");
		this.client = new ApplicationClient(this.options);
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
			client.setEventCallback(new MyEventCallback());
			client.setStatusCallback(new MyStatusCallback());
			client.subscribeToDeviceStatus();
			client.subscribeToDeviceEvents("hursley-devices", "psutil");
			while (!quit) {
				Thread.sleep(1000);
			}
			
			// Once told to stop, cleanly disconnect from the service
			client.disconnect();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class MyEventCallback implements EventCallback {

		@Override
		public void processEvent(Event e) {
			//System.out.println(e.getTopic() + "--" + e.getPayload());
		}
		
		@Override
		public void processCommand(Command cmd) {
			
			
		}
		
	}
	

	private class MyStatusCallback implements StatusCallback {

		@Override
		public void processApplicationStatus(ApplicationStatus status) {
			//System.out.println(status.getPayload());
		}

		@Override
		public void processDeviceStatus(DeviceStatus status) {
			//System.out.println(status.getPayload());
		}
	}
	
	public static class LauncherOptions {
		@Option(name="-c", aliases={"--config"}, usage="The path to an application configuration file")
		public String configFilePath = null;
		
		public LauncherOptions() {} 
	}
	
	
	public static void main(String[] args) throws Exception {
		createShutDownHook();
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
		SigarIoTApp a;

		if (opts.configFilePath != null) {
			a = new SigarIoTApp(opts.configFilePath);
			Thread t1 = new Thread(a);
			t1.start();

			System.out.println("Connected successfully - Your device ID is " + a.client.getAppId());
			System.out.println(" * Organization: " + a.client.getOrgId() + " (" + a.client.getAuthToken() + ")");			
		} else {
			a = new SigarIoTApp();
			Thread t1 = new Thread(a);
			t1.start();
			
			System.out.println("Connected successfully - Your app ID is " + a.client.getAppId());
			System.out.println("");
			System.out.println("(Press <enter> to disconnect)");
		}
		// Wait for <enter>
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		sc.close();
		
		System.out.println("Closing connection to the IBM Internet of Things Foundation");
		// Let the device thread know it can terminate
		a.quit();
	}
	
	private static void createShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println();
				System.out.println("Thanks for using the application");
				System.out.println("Exiting...");
			}
		}));
	}}