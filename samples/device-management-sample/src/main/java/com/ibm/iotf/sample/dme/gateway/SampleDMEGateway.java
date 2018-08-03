package com.ibm.iotf.sample.dme.gateway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;
import com.ibm.iotf.sample.dme.SystemObject;

/**
 * This is a simple example to showcase the Device Management Extension(DME) for gateway.
 * 
 * The example publishes the event for Gateway and device for every one minute, and registers
 * a custom action handler that will listen for the custom action - the custom action changes
 * the event publish interval time.
 *
 */
public class SampleDMEGateway {
	
	private final static String PROPERTIES_FILE_NAME = "/DMGatewaySample.properties";
	private final static String ATTACHED_DEVICE_TYPE = "Attatched";
	private final static String ATTACHED_DEVICE_ID = "At01";
	private ManagedGateway dmGateway;
	private MyCustomActionHandler actionHandler;
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting sample DM agent...");
		
		SampleDMEGateway sample = new SampleDMEGateway();
		try {
			sample.createManagedClient(PROPERTIES_FILE_NAME);
			sample.connect();
			sample.addDMECallback();
			sample.startPublishDeviceEvents();
			sample.publishGWEvents();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.err.flush();
		} finally {
			sample.terminate();
		}
		
		System.out.println(" Exiting...");
	}
	
	private void startPublishDeviceEvents() {
		AttachedDevice at = new AttachedDevice(dmGateway, actionHandler);
		at.start();
	}

	/**
	 * Publish a sample gateway load event to Watson IoT Platform
	 */
	private void publishGWEvents() {
		SystemObject obj = new SystemObject();
		while(true) {
			try {
				//Generate a JSON object of the event to be published
				JsonObject event = new JsonObject();
				event.addProperty("name", SystemObject.getName());
				event.addProperty("cpu",  obj.getProcessCpuLoad());
				event.addProperty("mem",  obj.getMemoryUsed());
				
				this.dmGateway.publishGatewayEvent("load", event);
				System.out.println("publish GW event " + event + "  "+new Date());
				Thread.sleep(actionHandler.getPublishInterval(this.dmGateway.getGWDeviceType(), this.dmGateway.getGWDeviceId()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * A sample class that abstracts an Attached device
	 * 
	 * Publishes a sample load event every 1 second and changes the interval
	 * based on the custom action request from the server.
	 *
	 */
	private static class AttachedDevice extends Thread {
		
		private MyCustomActionHandler actionHandler;
		private ManagedGateway dmGateway;

		private AttachedDevice(ManagedGateway dmGateway, MyCustomActionHandler actionHandler) {
			this.dmGateway = dmGateway;
			this.actionHandler = actionHandler;
		}
		
		public void run() {
			SystemObject obj = new SystemObject();
			while(true) {
				//Generate a JSON object of the event to be published
				JsonObject event = new JsonObject();
				event.addProperty("name", SystemObject.getName());
				try {
					event.addProperty("cpu",  obj.getProcessCpuLoad());
				} catch (MalformedObjectNameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstanceNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReflectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				event.addProperty("mem",  obj.getMemoryUsed());
				
				this.dmGateway.publishDeviceEvent(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, "load", event);
				System.out.println("publish DE event " + event + "  "+new Date());
				try {
					Thread.sleep(actionHandler.getPublishInterval(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}
		
	}

	private void addDMECallback() throws Exception {
		this.actionHandler = new MyCustomActionHandler();
		this.dmGateway.addCustomActionHandler(actionHandler);
		Thread t = new Thread(actionHandler);
		t.start();
	}

	/** This method builds the device objects required to create the
	 * ManagedClient
	 * 
	 * @param propertiesFile
	 * @throws Exception
	 */
	private void createManagedClient(String propertiesFile) throws Exception {
		 /**
		  * Load device properties
		  */
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(SampleDMEGateway.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		

		DeviceData deviceData = getDeviceData(propertiesFile);
		
		dmGateway = new ManagedGateway(deviceProps, deviceData);		
	}
	
	private static DeviceData getDeviceData(String propertiesFile) {
		 /**
		  * Load device properties
		  */
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(SampleDMEGateway.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		

		/**
		 * Let us create the DeviceData object with the DeviceInfo object
		 */
		DeviceInfo deviceInfo = new DeviceInfo.Builder().
				serialNumber(trimedValue(deviceProps.getProperty("DeviceInfo.serialNumber"))).
				manufacturer(trimedValue(deviceProps.getProperty("DeviceInfo.manufacturer"))).
				model(trimedValue(deviceProps.getProperty("DeviceInfo.model"))).
				deviceClass(trimedValue(deviceProps.getProperty("DeviceInfo.deviceClass"))).
				description(trimedValue(deviceProps.getProperty("DeviceInfo.description"))).
				fwVersion(trimedValue(deviceProps.getProperty("DeviceInfo.fwVersion"))).
				hwVersion(trimedValue(deviceProps.getProperty("DeviceInfo.hwVersion"))).
				descriptiveLocation(trimedValue(deviceProps.getProperty("DeviceInfo.descriptiveLocation"))).
				build();
		
		DeviceFirmware firmware = new DeviceFirmware.Builder().version(deviceProps.getProperty("DeviceFirmware.version")).build();
		
		DeviceData deviceData = new DeviceData.Builder().
						 		  deviceInfo(deviceInfo).
						 		  deviceFirmware(firmware).
						 		  build();
		
		return deviceData;
	}
	
	/**
	 * This method connects the device to the Watson IoT Platform
	 */
	private void connect() throws Exception {		
		// Connect to Watson IoT Platform
		dmGateway.connect();
		List<String> bundleIds = new ArrayList<String>(); 
		bundleIds.add("example-dme-actions-v1");
		dmGateway.sendGatewayManageRequest(0, false, false, bundleIds);
		dmGateway.sendDeviceManageRequest(ATTACHED_DEVICE_TYPE, ATTACHED_DEVICE_ID, 0, false, false, bundleIds);
	}
	
	private void terminate() throws Exception {
		if(this.dmGateway != null) {
			dmGateway.disconnect();
			System.out.println("Bye !!");
			System.exit(-1);
		}
	}
		
	private static String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}
}

