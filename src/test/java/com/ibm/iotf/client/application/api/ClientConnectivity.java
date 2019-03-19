package com.ibm.iotf.client.application.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.test.common.TestEnv;

/**
 * This test-case tests various ReST operations that can be performed on Watson IoT Platform to
 * add/update/get/delete device(s)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientConnectivity {
	
	private static final String CLASS_NAME = ClientConnectivity.class.getName();
	
	private static final String APP_ID = "DevApiOpApp1";
	
	//private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private static final String DEVICE_TYPE = "DevApiOpType";
	private static final String CLIENT_ID = "DevApiOpId1";

	private static APIClient apiClient = null;
	
	
	//TODO set up example device(s) then get device id for use with single query
	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, null);
		apiClient = new APIClient(appProps);
	}
	
	/**
	 * This test-case tests how to get all connectivity states
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void test01getAllConnectivityStates() throws IoTFCReSTException, UnsupportedEncodingException {
		try {
			JsonObject response = apiClient.getConnectionStates();
			assertFalse("Response must not be null", response.isJsonNull());			
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		} catch(UnsupportedEncodingException e) {
			fail("ErrorMessage :: "+e.getMessage());
		}
		
	}
	
	/**
	 * This test-case tests how to get connectivity states of all connected devices
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void test02getAllConnectedConnectivityStates() throws IoTFCReSTException, UnsupportedEncodingException {
		try {
			JsonObject response = apiClient.getConnectedConnectionStates();
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		} catch(UnsupportedEncodingException e) {
			fail("ErrorMessage :: "+e.getMessage());
		}
		
	}
	
	/**
	 * This test-case tests how to get connectivity state of a single device
	 * 
	 * if the CLIENT_ID doesn't exist returns 404 with obj
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void test03getSingleConnectivityState() throws IoTFCReSTException, UnsupportedEncodingException {
		try {
			JsonObject response = apiClient.getConnectionState(CLIENT_ID);
			assertFalse("Response must not be null", response.isJsonNull());		
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		} catch(UnsupportedEncodingException e) {
			fail("ErrorMessage :: "+e.getMessage());
		}
		
	}
	
	/**
	 * This test-case tests how to get device details using the Java Client Library.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void test04getRecentDasyActivityStates() throws IoTFCReSTException, UnsupportedEncodingException {
		try {
			Date date = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, -2);
			Date dateTwoDaysAgo = cal.getTime();
			
			String utcTime = DateFormatUtils.formatUTC(dateTwoDaysAgo, 
					DateFormatUtils.ISO_DATETIME_FORMAT.getPattern());

			JsonObject response = apiClient.getActiveInRecentDaysConnectionStates(utcTime);
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		} catch(UnsupportedEncodingException e) {
			fail("ErrorMessage :: "+e.getMessage());
		}
		
	}
	
	/**
	 * This test-case tests how to get a custom query using the Java Client Library.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void test05getCustomConnectionState() throws IoTFCReSTException, UnsupportedEncodingException {
		try {
	     	JsonObject response = apiClient.getCustomConnectionState("?connectionStatus=disconnected");
			assertFalse("Response must not be null", response.isJsonNull());
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
		} catch(UnsupportedEncodingException e) {
			fail("ErrorMessage :: "+e.getMessage());
		}
		
	}
	
	
}