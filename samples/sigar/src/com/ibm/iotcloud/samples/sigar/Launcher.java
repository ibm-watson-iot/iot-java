package com.ibm.iotcloud.samples.sigar;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.LogManager;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.ibm.iotcloud.client.Device;

public class Launcher {

	private String deviceId = null;
	
	public static class LauncherOptions {
		@Option(name="-a", aliases={"--account"}, usage="Connect to an account in the IBM Internet of Things Cloud")
		public String account = null;
	
		@Option(name="-u", aliases={"--username"}, usage="Username used to authenticate this device to the IBM Internet of Things Cloud")
		public String username = null;
	
		@Option(name="-p", aliases={"--password"}, usage="Password used to authenticate this device to the IBM Internet of Things Cloud")
		public String password = null;
		
		public LauncherOptions() {} 
	}
	
	
	public static void main(String[] args) throws InterruptedException {
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
		SigarIoTDevice d = new SigarIoTDevice(opts.account, opts.username, opts.password);
		Thread t1 = new Thread(d);
		t1.start();

		if (opts.account != null) {
			System.out.println("Connected successfully - Your device ID is " + d.getDeviceId());
			System.out.println(" * Account: " + opts.account + " (" + opts.username + "/" + opts.password + ")");			
		} else {
			System.out.println("Connected successfully - Your device ID is " + d.getDeviceId());
			System.out.println(" * http://quickstart.internetofthings.ibmcloud.com/?deviceId=" + d.getDeviceId());
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