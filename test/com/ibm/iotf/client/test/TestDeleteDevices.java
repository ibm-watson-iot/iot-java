package com.ibm.iotf.client.test;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Properties;

import org.junit.Test;


import com.ibm.iotf.client.api.DeviceFactory;

import junit.framework.TestCase;

public class TestDeleteDevices extends TestCase {
	@Test
	public void testDeleteDevice(){
		
		try{
			Properties opt = new Properties();
		
			
			File file = new File("src/com/ibm/iotf/client/device/test/test.properties");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();

				
			
			
			opt.put("authKey", properties.getProperty("authKey"));
			opt.put("authToken", properties.getProperty("authToken"));

		DeviceFactory factory = new DeviceFactory(opt);
		
		
			assertEquals("Device didn't exist...", false, factory.deleteDevice(properties.getProperty("deviceType"), properties.getProperty("deviceId")));
			assertEquals("Device deregistered....", true, factory.deleteDevice(properties.getProperty("deviceType"), properties.getProperty("deviceId")));
			
		
		
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		}



}
