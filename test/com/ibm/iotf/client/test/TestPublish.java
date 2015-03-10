package com.ibm.iotf.client.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.junit.Test;
import com.ibm.iotf.client.app.ApplicationClient;
import junit.framework.TestCase;

public class TestPublish extends TestCase{
	
	@Test
	public void testPublish(){
		try{
		Properties opt = new Properties();
		ApplicationClient client = null;
		
		
		
		File file = new File("test/test.properties");
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.load(fileInput);
		fileInput.close();


		
		opt.put("auth-key", properties.getProperty("authKey"));
		opt.put("auth-token", properties.getProperty("authToken"));
		opt.put("id", properties.getProperty("id"));
		opt.put("auth-method", properties.getProperty("auth-method"));
		
	
	
		client = new ApplicationClient(opt);
		client.connect();
		client.publishEvent(properties.getProperty("deviceType"), properties.getProperty("deviceId"), properties.getProperty("event"), 0);
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		}




}
