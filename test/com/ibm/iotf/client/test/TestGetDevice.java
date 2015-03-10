package com.ibm.iotf.client.device.test;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Properties;

import com.ibm.iotf.client.api.Device;
import com.ibm.iotf.client.api.DeviceFactory;

import junit.framework.TestCase;

public class TestGetDevice extends TestCase {
	
	public void testGetDevice(){
		
		
		try{
		Properties opt = new Properties();
	
		
		File file = new File("test/test.properties");
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.load(fileInput);
		fileInput.close();

		
		
		
		
		opt.put("authKey",  properties.getProperty("authKey"));
		opt.put("authToken", properties.getProperty("authToken"));
		
		DeviceFactory factory = new DeviceFactory(opt);
		
		Device device = factory.getDevice(properties.getProperty("deviceType"), properties.getProperty("deviceId"));
		if(device != null)
			System.out.println("Get Device...  " + device);
		else 
			System.out.println("Not able to get Device... ");
		
		assertEquals("Getdevices", 0, factory.getDevices());
		
		
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		}



}
