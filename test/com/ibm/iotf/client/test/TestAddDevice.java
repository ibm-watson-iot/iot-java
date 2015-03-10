package com.ibm.iotf.client.test;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Properties;

import org.junit.Test;

import com.ibm.iotf.client.api.Device;
import com.ibm.iotf.client.api.DeviceFactory;

import junit.framework.TestCase;

public class TestAddDevice extends TestCase {
	@Test
	public void testAddDevice(){
		try{
		Properties opt = new Properties();
		
		String deviceType = "";
		String deviceId = "";
		
		
		File file = new File("test/test.properties");
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.load(fileInput);
		fileInput.close();


		
		opt.put("authKey", properties.getProperty("authKey"));
		opt.put("authToken", properties.getProperty("authToken"));
		
	
		
		DeviceFactory factory = new DeviceFactory(opt);
		String metadata = "";
		
		Device device = factory.registerDevice(properties.getProperty("deviceType"), properties.getProperty("deviceId"), metadata);
		if(device != null)
			System.out.println("Device created and has " + device);
		else 
			System.out.println("Device not created....");
		
		assertEquals("Adddevices", device, factory.registerDevice(deviceType, deviceId, metadata));
		
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		}



}
