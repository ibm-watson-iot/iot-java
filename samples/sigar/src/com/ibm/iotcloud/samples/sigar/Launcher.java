package com.ibm.iotcloud.samples.sigar;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.LogManager;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


public class Launcher {

	private String deviceId = null;
	
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
			System.out.println(" * Account: " + d.client.getOrgId() + " (" + d.client.getAuthToken() + ")");			
		} else {
			d = new SigarIoTDevice();
			Thread t1 = new Thread(d);
			t1.start();
			
			System.out.println("Connected successfully - Your device ID is " + d.client.getDeviceId());
			System.out.println(" * http://quickstart.internetofthings.ibmcloud.com/?deviceId=" + d.client.getDeviceId());
			System.out.println("Visit the QuickStart portal to see this device's data visualized in real time and learn more about the IBM Internet of Things Cloud");
			System.out.println("");
			System.out.println("(Press <enter> to disconnect)");
		}
		// Wait for <enter>
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		sc.close();
		
		System.out.println("Closing connection to the IBM Internet of Things Cloud service");
		// Let the device thread know it can terminate
		d.quit();
	}
}