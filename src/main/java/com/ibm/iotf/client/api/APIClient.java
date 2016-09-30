/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.cert.X509Certificate;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.net.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.AbstractClient;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.util.LoggerUtility;

/**
 * Class to register, delete and retrieve information about devices <br>
 * This class can also be used to retrieve historian information
 */

public class APIClient {

	private static final String CLASS_NAME = APIClient.class.getName();
	
	private static final String BASIC_API_V0002_URL = "/api/v0002";

	private String authKey = null;
	private String authToken = null;
	private SSLContext sslContext = null;
	private String orgId = null;
	private String mdeviceType = null;
	private String mdeviceId = null;

	private String domain;	
	private boolean isQuickstart = false;
	
	// Enum for content-type header
	enum ContentType {
		text("text/plain"), json("application/json"), xml("application/xml"), bin(
				"application/octet-stream");

		ContentType(String type) {
			mType = type;
		}

		public String getType() {
			return mType;
		}

		private String mType;

	}//ending enum
	
	private String contentType = "json";
	private boolean isSecured = true;
	
	public APIClient(Properties opt) throws NoSuchAlgorithmException, KeyManagementException {
		boolean isGateway = false;
		String authKeyPassed = null;
		
		if("gateway".equalsIgnoreCase(APIClient.getAuthMethod(opt))) {
			isGateway = true;
		} else if("device".equalsIgnoreCase(APIClient.getAuthMethod(opt))) {
			authKey = "use-token-auth";
		} else {
			authKeyPassed = opt.getProperty("auth-key");
			if(authKeyPassed == null) {
				authKeyPassed = opt.getProperty("API-Key");
			}

			authKey = trimedValue(authKeyPassed);
		}
		
		String token = opt.getProperty("auth-token");
		if(token == null) {
			token = opt.getProperty("Authentication-Token");
		}
		authToken = trimedValue(token);
		
		String org = opt.getProperty("org");
		if(org == null) {
			org = opt.getProperty("Organization-ID");
		}
		
		this.orgId = trimedValue(org);
		this.domain = getDomain(opt);
		
		if(this.orgId == null || this.orgId.equalsIgnoreCase("quickstart"))
			isQuickstart = true;
		
		this.mdeviceType = this.getDeviceType(opt);
		this.mdeviceId = this.getDeviceId(opt);
		this.isSecured = this.IsSecuredConnection(opt);
		
		if(isGateway) {
			authKey = "g/" + this.orgId + '/' + mdeviceType + '/' + mdeviceId;
		}
		
		TrustManager[] trustAllCerts = null;
		boolean trustAll = false;
		
		String value = opt.getProperty("Trust-All-Certificates");
		if (value != null) {
			trustAll = Boolean.parseBoolean(trimedValue(value));
		}
		
		if (trustAll) {
			trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
	
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
	
				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };
		}

		sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(null, trustAllCerts, null);
	}
	
	/**
	 * 
	 * @return the domain
	 */
	protected String getDomain(Properties options) {
		String domain;
		domain = options.getProperty("domain");
		
		if(domain == null) {
			domain = options.getProperty("Domain");
		}
		domain = trimedValue(domain);
		
		if(domain != null && !("".equals(domain))) {
			return domain;
		} else {
			return AbstractClient.DEFAULT_DOMAIN;
		}
	}
	
	private static String getAuthMethod(Properties opt) {
		String method = opt.getProperty("auth-method");
		if(method == null) {
			method = opt.getProperty("Authentication-Method");
		}
		
		return trimedValue(method);
	}
	
	private static String getApiKey(Properties opt) {
		String method = opt.getProperty("auth-key");
		if (method == null) {
			method = opt.getProperty("API-Key");
		}
		return trimedValue(method);
	}
	
	/*
	 * old style - id
	 * new style - Device-ID
	 */
	protected String getDeviceId(Properties options) {
		String id;
		id = options.getProperty("Gateway-ID");
		if(id == null) {
			id = options.getProperty("Device-ID");
		}
		if(id == null) {
			id = options.getProperty("id");
		}
		return trimedValue(id);
	}
	
	protected String getDeviceType(Properties options) {
		String type;
		type = options.getProperty("Gateway-Type");
		if(type == null) {
			type = options.getProperty("Device-Type");
		}
		if(type == null) {
			type = options.getProperty("type");
		}
		return trimedValue(type);
	}
	
//	protected String getContentType(Properties options) {
//		String id;
//		id = options.getProperty("Content-Type");		
//		return trimedValue(id);
//	}
	
	protected boolean IsSecuredConnection(Properties options) {
		boolean type = true;
		String id;
		id = options.getProperty("Secure");
		if(id != null)
			type = trimedValue(id).equalsIgnoreCase("true");
		
		return type;
	}
	
	private static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}
	
	private HttpResponse connect(String httpOperation, String url, String jsonPacket, 
			List<NameValuePair> queryParameters) throws IoTFCReSTException, URISyntaxException, IOException {
		final String METHOD = "connect";
		
		StringEntity input = null;
		if(jsonPacket != null) {
			input = new StringEntity(jsonPacket, StandardCharsets.UTF_8);
		}
		
		
		String encodedString = null;
		if (!isQuickstart) {
			byte[] encoding = Base64.encodeBase64(new String(authKey + ":"
					+ authToken).getBytes());
			encodedString = new String(encoding);
		}
		
		switch(httpOperation) {
			case "post":
				return casePostFromConnect(queryParameters, url, METHOD,input, encodedString);
			case "put":
				return casePutFromConnect(queryParameters, url, METHOD,input, encodedString);
			case "get":
				return caseGetFromConnect(queryParameters, url, METHOD,input, encodedString);
			case "delete":
				return caseDeleteFromConnect(queryParameters, url, METHOD,input, encodedString);
		}
		return null;
			
	}
	
	private HttpResponse casePostFromConnect(List<NameValuePair> queryParameters, String url, String method, StringEntity input, String encodedString) throws URISyntaxException, IOException {
		URIBuilder builder = new URIBuilder(url);
		if(queryParameters != null) {
			builder.setParameters(queryParameters);
		}

		ContentType content = ContentType.valueOf(contentType);		
		
		HttpPost post = new HttpPost(builder.build());
		post.setEntity(input);
		post.addHeader("Content-Type", content.getType());
		post.addHeader("Accept", "application/json");
		
		if(isQuickstart == false)
			post.addHeader("Authorization", "Basic " + encodedString);
		try {
			HttpClient client = HttpClientBuilder.create().setSslcontext(sslContext).build();
			return client.execute(post);
		} catch (IOException e) {
			LoggerUtility.warn(CLASS_NAME, method, e.getMessage());
			throw e;
		}  
		
	}
	
	private HttpResponse casePutFromConnect(List<NameValuePair> queryParameters, String url, String method, StringEntity input, String encodedString) throws URISyntaxException, IOException {
		URIBuilder putBuilder = new URIBuilder(url);
		if(queryParameters != null) {
			putBuilder.setParameters(queryParameters);
		}
		HttpPut put = new HttpPut(putBuilder.build());
		put.setEntity(input);
		put.addHeader("Content-Type", "application/json");
		put.addHeader("Accept", "application/json");
		put.addHeader("Authorization", "Basic " + encodedString);
		try {
			HttpClient client = HttpClientBuilder.create().setSslcontext(sslContext).build();
			return client.execute(put);
		} catch (IOException e) {
			LoggerUtility.warn(CLASS_NAME, method, e.getMessage());
			throw e;
		} 

	}
	
	private HttpResponse caseGetFromConnect(List<NameValuePair> queryParameters, String url, String method, StringEntity input, String encodedString) throws URISyntaxException, IOException {

		URIBuilder getBuilder = new URIBuilder(url);
		if(queryParameters != null) {
			getBuilder.setParameters(queryParameters);
		}
		HttpGet get = new HttpGet(getBuilder.build());
		get.addHeader("Content-Type", "application/json");
		get.addHeader("Accept", "application/json");
		get.addHeader("Authorization", "Basic " + encodedString);
		try {
			HttpClient client = HttpClientBuilder.create().setSslcontext(sslContext).build();					
			return client.execute(get);
		} catch (IOException e) {
			LoggerUtility.warn(CLASS_NAME, method, e.getMessage());
			throw e;
		}			

	}
	
	private HttpResponse caseDeleteFromConnect(List<NameValuePair> queryParameters, String url, String method, StringEntity input, String encodedString) throws URISyntaxException, IOException {

		URIBuilder deleteBuilder = new URIBuilder(url);
		if(queryParameters != null) {
			deleteBuilder.setParameters(queryParameters);
		}

		HttpDelete delete = new HttpDelete(deleteBuilder.build());
		delete.addHeader("Content-Type", "application/json");
		delete.addHeader("Accept", "application/json");
		delete.addHeader("Authorization", "Basic " + encodedString);
		try {
			HttpClient client = HttpClientBuilder.create().setSslcontext(sslContext).build();					
			return client.execute(delete);
		} catch (IOException e) {
			LoggerUtility.warn(CLASS_NAME, method, e.getMessage());
			throw e;
		} 

	}
	
	private String readContent(HttpResponse response, String method) 
			throws IllegalStateException, IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
		String line = null;
		try {
			line = br.readLine();
		} catch (IOException e) {
			LoggerUtility.warn(CLASS_NAME, method, e.getMessage());
			throw e;
		}
		LoggerUtility.fine(CLASS_NAME, method, line);
		try {
			if(br != null)
				br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return line;
	}
	
	/**
	 * Checks whether the given device exists in the Watson IoT Platform
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Types/get_device_types_typeId">link</a>
	 * for more information about the response</p>.
	 * 
	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * 
	 * @return A boolean response containing the status
	 * @throws IoTFCReSTException When there is a failure in device information
	 */
	public boolean isDeviceExist(String deviceType, String deviceId) throws IoTFCReSTException {
		final String METHOD = "isDeviceExist";
		/**
		 * Form the url based on this swagger documentation
		 */
		System.out.println("OrgId:" + orgId+"**DeviceType: "+deviceType+ "**DeviceId: "+deviceId);
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId);
		
		int code = 0;
		HttpResponse response = null;
		try {
			//System.out.println("bfr connect: "+sb.toString());
			response = connect("get", sb.toString(), null, null);
			//System.out.println("bfr resp");
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the Device "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist");
		} else if(code == 404) {
			return false;
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return false;
	}

	/**
	 * This method retrieves a device based on the deviceType and DeviceID of the organization passed.
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/get_device_types_typeId_devices_deviceId">link</a>
	 * for more information about the response.</p>
	 * 
	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * 
	 * @return JsonObject containing the device details
	 * @throws IoTFCReSTException When there is a failure in device information
	 */
	public JsonObject getDevice(String deviceType, String deviceId) throws IoTFCReSTException {
		final String METHOD = "getDevice";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId);
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the Device "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist");
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The device type does not exist");
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * Gets location information for a device.
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/get_device_types_typeId_devices_deviceId_location">link</a>
	 * for more information about the response.</p>
	 * 
	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * 
	 * @return JsonObject containing the device location
	 * @throws IoTFCReSTException Failure in getting the device location 
	 */
	public JsonObject getDeviceLocation(String deviceType, String deviceId) throws IoTFCReSTException {
		final String METHOD = "getDeviceLocation";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId).
		   append("/location");
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieveing the Device Location "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 404) {
			throw new IoTFCReSTException(code, "Device location information not found");
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * Updates the location information for a device. If no date is supplied, the entry is added with the current date and time.
	 *  
	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * @param location contains the new location
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/put_device_types_typeId_devices_deviceId_location">link</a>
	 * for more information about the JSON format</p>.
	 *   
	 * @return A JSON response containing the status of the update operation.
	 * 
	 * @throws IoTFCReSTException Failure in updting the device location
	 */
	public JsonObject updateDeviceLocation(String deviceType, String deviceId, 
			JsonElement location) throws IoTFCReSTException {
		final String METHOD = "updateDeviceLocation";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId).
		   append("/location");
		
		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		try {
			response = connect("put", sb.toString(), location.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200 || code == 409) {
				String result = this.readContent(response, METHOD);
				jsonResponse = new JsonParser().parse(result);
				if(code == 200) {
					return jsonResponse.getAsJsonObject();
				}
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in updating the Device Location "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 404) {
			throw new IoTFCReSTException(code, "Device location information not found");
		} else if(code == 409) {
			throw new IoTFCReSTException(code, "The update could not be completed due to a conflict", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * Gets device management information for a device.
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/get_device_types_typeId_devices_deviceId_mgmt">link</a>
	 * for more information about the JSON Response.</p>
	 * 
	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * 
	 * @return JsonObject containing the device management information
	 * @throws IoTFCReSTException Failure in getting the device management information 
	 */
	public JsonObject getDeviceManagementInformation(String deviceType, String deviceId) throws IoTFCReSTException {
		final String METHOD = "getDeviceManagementInformation";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId).
		   append("/mgmt");
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieveing the Device Management Information "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 404) {
			throw new IoTFCReSTException(code, "Device not found");
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * Gets device type details.
	 *  
	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * @param propertiesToBeModified contains the parameters to be updated
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/put_device_types_typeId_devices_deviceId">link</a>
	 * for more information about the response</p>.
	 *   
	 * @return A JSON response containing the status of the update operation.
	 * 
	 * @throws IoTFCReSTException Failure in updating the device
	 */
	public JsonObject updateDevice(String deviceType, String deviceId, 
			JsonElement propertiesToBeModified) throws IoTFCReSTException {
		
		final String METHOD = "updateDevice";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId);
		
		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		try {
			response = connect("put", sb.toString(), propertiesToBeModified.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200 || code == 409) {
				String result = this.readContent(response, METHOD);
				jsonResponse = new JsonParser().parse(result);
				if(code == 200) {
					return jsonResponse.getAsJsonObject();
				}
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in updating the Device "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist");
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The organization, device type or device does not exist");
		} else if(code == 409) {
			throw new IoTFCReSTException(code, "The update could not be completed due to a conflict", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}


	/**
	 * Get details about an organization. 
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Organization_Configuration/get">link</a>
	 * for more information about the response.</p>
	 *   
	 * @return details about an organization.
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the organization details 
	 */
	public JsonObject getOrganizationDetails() throws IoTFCReSTException {
		final String METHOD = "getOrganizationDetails";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 * http://iot-test-01.hursley.ibm.com/docs/api/v0002.html#!/Organization_Configuration/get
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/");
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				// success
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the Organization detail, "
					+ ":: "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the api key used does not exist");
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The organization does not exist");
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}

	/**
	 * This method returns all the devices belonging to the organization, This method
	 * provides more control in returning the response over the no argument method.
	 * 
	 * <p>For example, Sorting can be performed on any of the properties.</p>
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Bulk_Operations/get_bulk_devices">link</a>
	 * for more information about how to control the response.</p>
	 * 
	 * @param parameters list of query parameters that controls the output. For more information about the
	 * list of possible query parameters, refer to this 
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Bulk_Operations/get_bulk_devices">link</a>. 
	 *   
	 * @return JSON response containing the list of devices.
	 * <p> The response will contain more parameters that can be used to issue the next request. 
	 * The result element will contain the current list of devices.</p>
	 *  
	 * @throws IoTFCReSTException Failure in retrieving all the devices 
	 */
	public JsonObject getAllDevices(List<NameValuePair> parameters) throws IoTFCReSTException {
		final String METHOD = "getDevices(1)";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/bulk/devices");
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, parameters);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				// success
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the Device details, "
					+ ":: "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the api key used does not exist");
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The organization does not exist");
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * This method returns all the devices belonging to the organization
	 * 
	 * <p> Invoke the overloaded method, if you want to have control over the response, for example sorting.</p>
	 * 
	 * @return Jsonresponse containing the list of devices. Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Bulk_Operations/get_bulk_devices">link</a>
	 * for more information about the response.
	 * <p> The response will contain more parameters that can be used to issue the next request. 
	 * The result element will contain the current list of devices.</p>
	 *  
	 * @throws IoTFCReSTException Failure in retrieving all the devices 
	 */
	public JsonObject getAllDevices() throws IoTFCReSTException {
		return getAllDevices((ArrayList<NameValuePair>)null);
	}
	
	/**
	 * This method returns all the devices belonging to a particular device type, This method
	 * provides more control in returning the response over the no argument method.
	 * 
	 * <p>For example, Sorting can be performed on any of the properties.</p>
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/get_device_types_typeId_devices">link</a>
	 * for more information about how to control the response.</p>
	 * 
	 * @param deviceType Device type ID
	 * @param parameters list of query parameters that controls the output. For more information about the
	 * list of possible query parameters, refer to this 
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/get_device_types_typeId_devices">link</a>.
	 * 
	 * @return JSON response containing the list of devices.
	 * <p> The response will contain more parameters that can be used to issue the next request. 
	 * The result element will contain the current list of devices.</p>
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the devices 
	 */
	public JsonObject retrieveDevices(String deviceType, List<NameValuePair> parameters) throws IoTFCReSTException {
		
		final String METHOD = "getDevices(typeID)";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).append("/devices");
				   
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, parameters);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				// success
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the Device details, "
					+ ":: "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the api key used does not exist");
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The organization does not exist");
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * This method returns all the devices belonging to a particular device type in an organization.
	 * 
	 * <p> Invoke the overloaded method, if you want to have control over the response, for example sorting.</p>
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/get_device_types_typeId_devices">link</a>
	 * for more information about how to control the response.</p>
	 * 
	 * @param deviceType Device type ID 
	 * 
	 * @return JSON response containing the list of devices.
	 * <p> The response will contain more parameters that can be used to issue the next request. 
	 * The result element will contain the current list of devices.</p>
	 * 	 *  
	 * @throws IoTFCReSTException Failure in retrieving the devices 
	 */
	public JsonObject retrieveDevices(String deviceType) throws IoTFCReSTException {
		return retrieveDevices(deviceType, (ArrayList)null);
	}
	
	/**
	 * This method returns all devices that are connected through the specified gateway(typeId, deviceId) to Watson IoT Platform.
	 * 
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/get_device_types_typeId_devices_deviceId_devices">link</a>
	 * for more information about how to control the response.</p>
	 * 
	 * @param gatewayType Gateway Device type ID 
	 * @param gatewayId Gateway Device ID
	 * 
	 * @return JSON response containing the list of devices.
	 * <p> The response will contain more parameters that can be used to issue the next request. 
	 * The result element will contain the current list of devices.</p>
	 * 	 *  
	 * @throws IoTFCReSTException failure in getting the devices 
	 */
	public JsonObject getDevicesConnectedThroughGateway(String gatewayType, String gatewayId) throws IoTFCReSTException {
		final String METHOD = "getDevicesConnectedThroughGateway(typeID, deviceId)";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(gatewayType).
		   append("/devices/").
		   append(gatewayId).append("/devices");
				   
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				// success
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the device information "
					+ "that are connected through the specified gateway, "
					+ ":: "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 403) {
			throw new IoTFCReSTException(code, "Request is only allowed if the classId of the device type is 'Gateway'");
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "Device type or device not found");
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}



	/**
	 * This method returns all the device types belonging to the organization, This method
	 * provides more control in returning the response over the no argument method.
	 * 
	 * <p>For example, Sorting can be performed on any of the properties.</p>
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Types/get_device_types">link</a>
	 * for more information about how to control the response.</p>
	 * 
	 * @param parameters list of query parameters that controls the output. For more information about the
	 * list of possible query parameters, refer to this 
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Types/get_device_types">link</a>.
	 * 
	 * @return A JSON response containing the list of device types.
	 * 	 * <p> The response will contain more parameters that can be used to issue the next request. 
	 * The result element will contain the current list of device types.</p>
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the device types 
	 */
	public JsonObject getAllDeviceTypes(List<NameValuePair> parameters) throws IoTFCReSTException {
		final String METHOD = "getDeviceTypes";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 * http://iot-test-01.hursley.ibm.com/docs/api/v0002.html#!/Bulk_Operations/get_bulk_devices
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types");
		
		HttpResponse response = null;
		int code = 0;
		try {
			response = connect("get", sb.toString(), null, parameters);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				// success
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the DeviceType details, "
					+ ":: "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the api key used does not exist");
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * This method returns all the device types belonging to the organization. 
	 * <p> Invoke the overloaded method, if you want to have control over the response, for example sorting.</p>
	 * 
	 * @return A JSON response containing the list of device types. Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Types/get_device_types">link</a>
	 * for more information about the response.
	 * <p> The response will contain more parameters that can be used to issue the next request. 
	 * The result element will contain the current list of device types.</p>
	 *  
	 * @throws IoTFCReSTException Failure in retrieving all the device types 
	 */
	public JsonObject getDeviceTypes() throws IoTFCReSTException {
		return getAllDeviceTypes(null);
	}
	
	
	/**
	 * Check whether the given device type exists in the Watson IoT Platform
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Types/get_device_types_typeId">link</a>
	 * for more information about the response</p>.
	 * @param deviceType The device type to be checked in Watson IoT Platform
	 * @return A boolean response containing the status
	 * @throws IoTFCReSTException Failure in checking if device type exists
	 */
	public boolean isDeviceTypeExist(String deviceType) throws IoTFCReSTException {
		final String METHOD = "isDeviceTypeExist";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType);
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the Device Type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist");
		} else if(code == 404) {
			return false;
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return false;
	}
	
	/**
	 * Gets device type details.
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Types/get_device_types_typeId">link</a>
	 * for more information about the response</p>.
	 *   
	 * @param  deviceType the type of the device in String
	 *  
	 * @return A JSON response containing the device type.
	 * 
	 * @throws IoTFCReSTException Failure in retrieving the device type 
	 */
	public JsonObject getDeviceType(String deviceType) throws IoTFCReSTException {
		final String METHOD = "getDeviceType";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType);
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the Device Type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist");
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The device type does not exist");
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * Updates device type details.
	 * 
	 * @param updatedValues contains the parameters to be updated
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Types/put_device_types_typeId">link</a>
	 * for more information about the response</p>.
	 * 
	 * @param deviceType The type of device in String  
	 * @return A JSON response containing the status of the update operation.
	 * @throws IoTFCReSTException Failure in updating the device type
	 */
	public JsonObject updateDeviceType(String deviceType, JsonElement updatedValues) throws IoTFCReSTException {
		final String METHOD = "updateDeviceType";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType);
		
		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		try {
			response = connect("put", sb.toString(), updatedValues.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200 || code == 409) {
				String result = this.readContent(response, METHOD);
				jsonResponse = new JsonParser().parse(result);
				if(code == 200) {
					return jsonResponse.getAsJsonObject();
				}
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in updating the Device Type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist");
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The device type does not exist");
		} else if(code == 409) {
			throw new IoTFCReSTException(code, "The update could not be completed due to a conflict", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}
	
	
	/**
	 * Creates a device type.
	 * 
	 * @param deviceType JSON object representing the device type to be added. Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Types/post_device_types">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return JSON object containing the response of device type.
	 *  
	 * @throws IoTFCReSTException Failure in adding the device type
	 */

	public JsonObject addDeviceType(JsonElement deviceType) throws IoTFCReSTException {
		
		final String METHOD = "addDeviceType";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("post", sb.toString(), deviceType.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if(code == 201 || code == 400 || code == 409) {
				// success
				String result = this.readContent(response, METHOD);
				jsonResponse = new JsonParser().parse(result);
			}
			if(code == 201) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding the device Type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(400, "Invalid request (No body, invalid JSON, "
					+ "unexpected key, bad value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(401, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(403, "The authentication method is invalid or "
					+ "the API key used does not exist");
		} else if (code == 409) {
			throw new IoTFCReSTException(409, "The device type already exists", jsonResponse);  
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * Creates a gateway device type.
	 * 
	 * @param deviceType JSON object representing the gateway device type to be added. Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Types/post_device_types">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return JSON object containing the response of device type.
	 *  
	 * @throws IoTFCReSTException Failure in adding the gateway device type
	 */

	public JsonObject addGatewayDeviceType(JsonElement deviceType) throws IoTFCReSTException {
		
		if(deviceType != null && !deviceType.getAsJsonObject().has("classId")) {
			deviceType.getAsJsonObject().addProperty("classId", "Gateway");
		}
		return this.addDeviceType(deviceType);
	}


	/**
	 * 
	 * Creates a device type.Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Types/post_device_types">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @param id ID of the Device Type to be added
	 * @param description Description of the device Type to be added
	 * @param deviceInfo DeviceInfo to be added. Must be specified in JSON format
	 * @param metadata Metadata to be added
	 * 
	 * @return JSON object containing the response of device type.
	 * 
	 * @throws IoTFCReSTException Failure in adding the device type
	 */

	public JsonObject addDeviceType(String id, String description, 
			JsonElement deviceInfo, JsonElement metadata) throws IoTFCReSTException {
		
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types");

		JsonObject input = new JsonObject();
		if(id != null) {
			input.addProperty("id", id);
		}
		if(description != null) {
			input.addProperty("description", description);
		}
		if(deviceInfo != null) {
			input.add("deviceInfo", deviceInfo);
		}
		if(metadata != null) {
			input.add("metadata", metadata);
		}
		
		return this.addDeviceType(input);
	}
	
	/**
	 * Deletes a device type.
	 * 
	 * @param typeId DeviceType to be deleted from IBM Watson IoT Platform
	 *   
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Types/delete_device_types_typeId">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return JSON object containing the response of device type.
	 *  
	 * @throws IoTFCReSTException Failure in deleting the device type
	 */

	public boolean deleteDeviceType(String typeId) throws IoTFCReSTException {
		final String METHOD = "deleteDeviceType";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(typeId);
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Device Type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist");
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return false;
	}
	
	/**
	 * This method registers a device, by accepting more parameters. Refer to 
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/post_device_types_typeId_devices">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @param deviceType String representing device type.
	 * @param deviceId String representing device id.
	 * @param authToken String representing the authentication token of the device (can be null). If its null
	 * the IBM Watson IoT Platform will generate a token.
	 * @param deviceInfo JsonObject representing the device Info (can be null).
	 * @param location JsonObject representing the location of the device (can be null).
	 * @param metadata JsonObject representing the device metadata (can be null).
	 * 
	 * @return JsonObject containing the registered device details
	 * @throws IoTFCReSTException Failure in registering the device
	 */
	public JsonObject registerDevice(String deviceType, String deviceId, 
			String authToken, JsonElement deviceInfo, JsonElement location, 
			JsonElement metadata) throws IoTFCReSTException {
		
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices");
		
		JsonObject input = new JsonObject();
		if(deviceId != null) {
			input.addProperty("deviceId", deviceId);
		}
		if(authToken != null) {
			input.addProperty("authToken", authToken);
		}
		if(deviceInfo != null) {
			input.add("deviceInfo", deviceInfo);
		}
		if(location != null) {
			input.add("location", location);
		}
		if(metadata != null) {
			input.add("metadata", metadata);
		}

		return this.registerDevice(deviceType, input);
	}
	
	/**
	 * Register a new device.
	 *  
	 * The response body will contain the generated authentication token for the device. 
	 * The caller of the method must make sure to record the token when processing 
	 * the response. The IBM Watson IoT Platform will not be able to retrieve lost authentication tokens.
	 * 
	 * @param typeId DeviceType ID 
	 * 
	 * @param device JSON representation of the device to be added. Refer to 
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/post_device_types_typeId_devices">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return JsonObject containing the generated authentication token for the device. 
	 *  
	 * @throws IoTFCReSTException Failure in registering the device
	 */

	public JsonObject registerDevice(String typeId, JsonElement device) throws IoTFCReSTException {
		final String METHOD = "registerDevice";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(typeId).
		   append("/devices");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		String method = "post";
		try {
			response = connect(method, sb.toString(), device.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if(code == 201 || code == 400 || code == 409) {
				// Get the response
				String result = this.readContent(response, METHOD);
				jsonResponse = new JsonParser().parse(result);
			}
			if(code == 201) {
				// Success
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in registering the device "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		String reason = null;
		switch (code) {
		case 400:
			reason = IoTFCReSTException.HTTP_ADD_DEVICE_ERR_400;
			break;
		case 401:
			reason = IoTFCReSTException.HTTP_ADD_DEVICE_ERR_401;
			break;
		case 403:
			reason = IoTFCReSTException.HTTP_ADD_DEVICE_ERR_403;
			break;
		case 409:
			reason = IoTFCReSTException.HTTP_ADD_DEVICE_ERR_409;
			break;
		case 500:
			reason = IoTFCReSTException.HTTP_ADD_DEVICE_ERR_500;
			break;
		default:
			reason = IoTFCReSTException.HTTP_ERR_UNEXPECTED;
		}
		throw new IoTFCReSTException(method, sb.toString(), device.toString(), code, reason, jsonResponse);
	}
	
	/**
	 * Register a new device under the given gateway.
	 *  
	 * The response body will contain the generated authentication token for the device. 
	 * The caller of the method must make sure to record the token when processing 
	 * the response. The IBM Watson IoT Platform will not be able to retrieve lost authentication tokens.
	 * 
	 * @param typeId DeviceType ID
	 * @param gatewayId The deviceId of the gateway 
	 * @param gatewayTypeId The device type of the gateway  
	 * 
	 * @param device JSON representation of the device to be added. Refer to 
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/post_device_types_typeId_devices">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return JsonObject containing the generated authentication token for the device. 
	 *  
	 * @throws IoTFCReSTException Failure in registering the device under the given Gateway
	 */

	public JsonObject registerDeviceUnderGateway(String typeId, String gatewayId, 
			String gatewayTypeId, JsonElement device) throws IoTFCReSTException {
		
		if(device != null) {
			JsonObject deviceObj = device.getAsJsonObject();
			deviceObj.addProperty("gatewayId", gatewayId);
			deviceObj.addProperty("gatewayTypeId", gatewayTypeId);
		}
		return this.registerDevice(typeId, device);
	}
	
	/**
	 * This method deletes the device which matches the device id and type of the organization.	 
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/delete_device_types_typeId_devices_deviceId">link</a>
	 * for more information about the response</p>.

	 * @param deviceType
	 * 				object of String which represents device Type
	 * @param deviceId
	 * 				object of String which represents device id
	 * @return boolean to denote success or failure of operation
	 * @throws IoTFCReSTException Failure in deleting the device
	 */
	public boolean deleteDevice(String deviceType, String deviceId) throws IoTFCReSTException {
		final String METHOD = "deleteDevice";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId);
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Device"
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist");
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		
		throwException(response, METHOD);
		return false;
	}
	
	/**
	 * This method Clears the diagnostic log for the device.	 
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Diagnostics/delete_device_types_typeId_devices_deviceId_diag_logs">link</a>
	 * for more information about the JSON message format</p>.

	 * @param deviceType
	 * 				object of String which represents device Type
	 * @param deviceId
	 * 				object of String which represents device id
	 * @return boolean to denote success or failure of operation
	 * @throws IoTFCReSTException Failure in clearing the diagnostic logs 
	 */
	public boolean clearAllDiagnosticLogs(String deviceType, String deviceId) throws IoTFCReSTException {
		final String METHOD = "clearDiagnosticLogs";
		
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId).
		   append("/diag/logs");
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Diagnostic Logs "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		
		throwException(response, METHOD);
		return false;
	}
	
	/**
	 * This method retrieves all the diagnostic logs for a device.
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Diagnostics/get_device_types_typeId_devices_deviceId_diag_logs">link</a>
	 * for more information about the response in JSON format.</p>
	 * 
	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * 
	 * @return JsonArray Containing all the diagnostic logs
	 * @throws IoTFCReSTException Failure in retrieving all the diagnostic logs
	 */
	public JsonArray getAllDiagnosticLogs(String deviceType, String deviceId) throws IoTFCReSTException {
		final String METHOD = "getAllDiagnosticLogs";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId).
		   append("/diag/logs");
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonArray();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the diagnostic logs "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}		
		if(code == 404) {
			throw new IoTFCReSTException(code, "Device log not found");
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Adds an entry in the log of diagnostic information for the device. 
	 * The log may be pruned as the new entry is added. If no date is supplied, 
	 * the entry is added with the current date and time.
	 * 
	 * <p> Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Diagnostics/post_device_types_typeId_devices_deviceId_diag_logs">link</a> 
	 * for more information about the schema to be used </p>
	 * 
 	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * @param log the Log message to be added

	 * @return boolean containing the status of the load addition.
	 *  
	 * @throws IoTFCReSTException Failure in adding the diagnostic logs
	 */

	public boolean addDiagnosticLog(String deviceType, String deviceId, JsonElement log) throws IoTFCReSTException {
		final String METHOD = "addDiagnosticLog";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId).
		   append("/diag/logs");
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("post", sb.toString(), log.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if(code == 201 ) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding the diagnostic Log "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return false;
	}
	
	/**
	 * Delete this diagnostic log for the device.	 
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Diagnostics/delete_device_types_typeId_devices_deviceId_diag_logs_logId">link</a>
	 * for more information about the JSON Format</p>.

	 * @param deviceType
	 * 				object of String which represents device Type
	 * @param deviceId
	 * 				object of String which represents device id
	 * 
	 * @param logId object of String which represents log id
	 *  
	 * @return boolean to denote success or failure of operation
	 * @throws IoTFCReSTException Failure in deleting the diagnostic log
	 */
	public boolean deleteDiagnosticLog(String deviceType, String deviceId, String logId) throws IoTFCReSTException {
		final String METHOD = "deleteDiagnosticLog";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId).
		   append("/diag/logs/").
		   append(logId);
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Diagnostic Log "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return false;
	}
	
	private void throwException(HttpResponse response, String method) throws IoTFCReSTException {
		int code = 0;
		JsonElement jsonResponse = null;
	
		if(response != null) {
			code = response.getStatusLine().getStatusCode();
		
			try {
				String result = this.readContent(response, method);
				jsonResponse = new JsonParser().parse(result);
			} catch(Exception e) {}
		}
		
		throw new IoTFCReSTException(code, "", jsonResponse);
	}
	
	/**
	 * Gets diagnostic log for a device.	 
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Diagnostics/delete_device_types_typeId_devices_deviceId_diag_logs_logId">link</a>
	 * for more information about the JSON Format</p>.

	 * @param deviceType
	 * 				object of String which represents device Type
	 * @param deviceId
	 * 				object of String which represents device id
	 * 
	 * @param logId object of String which represents log id
	 *  
	 * @return JsonObject the DiagnosticLog in JSON Format
	 * 
	 * @throws IoTFCReSTException Failure in retrieving the diagnostic log
	 */
	public JsonObject getDiagnosticLog(String deviceType, String deviceId, String logId) throws IoTFCReSTException {
		final String METHOD = "getDiagnosticLog";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId).
		   append("/diag/logs/").
		   append(logId);
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the Diagnostic Log "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 404) {
			throw new IoTFCReSTException(code, "Device not found");
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}


	/**
	 * Clears the list of error codes for the device. The list is replaced with a single error code of zero.	 
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Diagnostics/delete_device_types_typeId_devices_deviceId_diag_errorCodes">link</a>
	 * for more information about the JSON message format</p>.

	 * @param deviceType
	 * 				object of String which represents device Type
	 * @param deviceId
	 * 				object of String which represents device id
	 * @return boolean to denote success or failure of operation
	 * @throws IoTFCReSTException Failure in clearing all the diagnostic error codes 
	 */
	public boolean clearAllDiagnosticErrorCodes(String deviceType, String deviceId) throws IoTFCReSTException {
		String METHOD = "clearAllDiagnosticErrorCodes";
		
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId).
		   append("/diag/errorCodes");
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Diagnostic Errorcodes "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return false;
	}
	
	/**
	 * This method retrieves all the diagnostic error codes for a device.
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Diagnostics/get_device_types_typeId_devices_deviceId_diag_errorCodes">link</a>
	 * for more information about the response in JSON format.</p>
	 * 
	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * 
	 * @return JsonArray Containing all the diagnostic error codes
	 * @throws IoTFCReSTException Failure in retrieving the error codes
	 */
	public JsonArray getAllDiagnosticErrorCodes(String deviceType, String deviceId) throws IoTFCReSTException {
		final String METHOD = "getAllDiagnosticErrorCodes";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId).
		   append("/diag/errorCodes");
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonArray();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the diagnostic Errorcodes "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 404) {
			throw new IoTFCReSTException(code, "Device not found");
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}

	/**
	 * Adds an error code to the list of error codes for the device. 
	 * The list may be pruned as the new entry is added.
	 * 
	 * <p> Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Diagnostics/post_device_types_typeId_devices_deviceId_diag_errorCodes">link</a> 
	 * for more information about the schema to be used </p>
	 * 
 	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * @param errorcode ErrorCode to be added in Json Format
	 * 
	 * @return boolean containing the status of the add operation.
	 *  
	 * @throws IoTFCReSTException Failure in adding the error codes
	 */

	public boolean addDiagnosticErrorCode(String deviceType, String deviceId, JsonElement errorcode) throws IoTFCReSTException {
		final String METHOD = "addDiagnosticErrorCode";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(deviceType).
		   append("/devices/").
		   append(deviceId).
		   append("/diag/errorCodes");
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("post", sb.toString(), errorcode.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if(code == 201) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding the Errorcode "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return false;
	}

	/**
	 * Adds an error code to the list of error codes for the device. 
	 * The list may be pruned as the new entry is added.
	 * 
	 * <p> Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Device_Diagnostics/post_device_types_typeId_devices_deviceId_diag_errorCodes">link</a> 
	 * for more information about the schema to be used </p>
	 * 
 	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * @param errorcode ErrorCode to be added in integer format
	 * @param date current date (can be null)
	 * 
	 * @return boolean containing the status of the add operation.
	 *  
	 * @throws IoTFCReSTException Failure in adding the error codes
	 */

	public boolean addDiagnosticErrorCode(String deviceType, String deviceId, 
			int errorcode, Date date) throws IoTFCReSTException {
		
		JsonObject ec = new JsonObject();
		ec.addProperty("errorCode", errorcode);
		if(date == null) {
			date = new Date();
		}
		String utcTime = DateFormatUtils.formatUTC(date, 
				DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
		
		ec.addProperty("timestamp", utcTime);
		return addDiagnosticErrorCode(deviceType, deviceId, ec);
	}
	
	/**
	 * List connection log events for a device to aid in diagnosing connectivity problems. 
	 * The entries record successful connection, unsuccessful connection attempts, 
	 * intentional disconnection and server-initiated disconnection.
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Problem_Determination/get_logs_connection">link</a>
	 * for more information about the JSON response.</p>
	 * 
	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * 
	 * @return JsonArray Containing the device connection logs
	 * @throws IoTFCReSTException Failure in retrieving the device connection logs 
	 */
	public JsonArray getDeviceConnectionLogs(String deviceType, String deviceId) throws IoTFCReSTException {
		final String METHOD = "getDeviceConnectionLogs";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/logs/connection");
		
		// add the query parameters
		
		sb.append("?typeId=").
		   append(deviceType).
		   append("&deviceId=").
		   append(deviceId);
		
		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				String result = this.readContent(response, METHOD);
				JsonElement jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonArray();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the connection logs "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid");
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist");
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The device type does not exist");
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * Register multiple new devices, each request can contain a maximum of 512KB. 
	 * The response body will contain the generated authentication tokens for all devices. 
	 * The caller of the method must make sure to record these tokens when processing 
	 * the response. The IBM Watson IoT Platform will not be able to retrieve lost authentication tokens
	 * 
	 * @param arryOfDevicesToBeAdded Array of JSON devices to be added. Refer to 
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Bulk_Operations/post_bulk_devices_add">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return JsonArray containing the generated authentication tokens for all the devices 
	 * for all devices. 
	 *  
	 * @throws IoTFCReSTException Failure in adding devices
	 */

	public JsonArray addMultipleDevices(JsonArray arryOfDevicesToBeAdded) throws IoTFCReSTException {
		final String METHOD = "bulkDevicesAdd";		
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/bulk/devices/add");

		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		try {
			response = connect("post", sb.toString(), arryOfDevicesToBeAdded.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if(code != 500) {
				// success
				String result = this.readContent(response, METHOD);
				jsonResponse = new JsonParser().parse(result);
			}
			if(code == 201) {
				return jsonResponse.getAsJsonArray();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding the Devices, "
					+ ":: "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 202) {
			throw new IoTFCReSTException(202, "Some devices registered successfully", jsonResponse);
		} else if(code == 400) {
			throw new IoTFCReSTException(400, "Invalid request (No body, invalid JSON, unexpected key, bad value)", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(403, "Maximum number of devices exceeded", jsonResponse);
		} else if(code == 413) {
			throw new IoTFCReSTException(413, "Request content exceeds 512Kb", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		
		throw new IoTFCReSTException(code, "",jsonResponse);
	}
	
	/**
	 * Delete multiple devices, each request can contain a maximum of 512Kb
	 * 
	 * @param arryOfDevicesToBeDeleted Array of JSON devices to be deleted. Refer to 
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Bulk_Operations/post_bulk_devices_remove">link</a> 
	 * for more information about the schema to be used.
	 * 
	 * @return JsonArray containing the status of the operations for all the devices
	 *  
	 * @throws IoTFCReSTException Failure in deleting devices
	 */
	public JsonArray deleteMultipleDevices(JsonArray arryOfDevicesToBeDeleted) throws IoTFCReSTException {
		final String METHOD = "bulkDevicesRemove";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/bulk/devices/remove");

		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("post", sb.toString(), arryOfDevicesToBeDeleted.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if(code != 500) {
				// success
				String result = this.readContent(response, METHOD);
				jsonResponse = new JsonParser().parse(result);
			}
			if(code == 201) {
				return jsonResponse.getAsJsonArray();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Devices, "
					+ ":: "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 202) {
			throw new IoTFCReSTException(202, "Some devices deleted successfully", jsonResponse);
		} else if(code == 400) {
			throw new IoTFCReSTException(400, "Invalid request (No body, invalid JSON, unexpected key, bad value)", jsonResponse);
		} else if(code == 413) {
			throw new IoTFCReSTException(413, "Request content exceeds 512Kb", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		}
		throw new IoTFCReSTException(code, "", jsonResponse);
	}
	
	/**
	 * Gets a list of device management requests, which can be in progress or recently completed.
	 * 
	 * @return JSON response containing the list of device management requests.
	 *  
	 * @throws IoTFCReSTException Failure in retrieving all DM requests
	 */
	
	public JsonObject getAllDeviceManagementRequests() throws IoTFCReSTException {
		return getAllDeviceManagementRequests((ArrayList<NameValuePair>)null);
	}
	
	
	/**
	 * Gets a list of device management requests, which can be in progress or recently completed.
	 * 
	 * @param parameters list of query parameters that controls the output.
	 * 
	 * @return JSON response containing the list of device management requests.
	 * @throws IoTFCReSTException Failure in retrieving all DM requests
	 */
	public JsonObject getAllDeviceManagementRequests(List<NameValuePair> parameters) throws IoTFCReSTException {
		final String METHOD = "getAllDeviceManagementRequests";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/mgmt/requests");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, parameters);
			code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				String result = this.readContent(response, METHOD);
				jsonResponse = new JsonParser().parse(result);
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the Device management Requests "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * Add a Device Management Extension.
	 * 
	 * @param request JSON object containing the the DM Extension request.
	 * @return If successful, JsonObject response from Watson IoT Platform.
	 * @throws IoTFCReSTException if failed.
	 * @see IoTFCReSTException
	 */
	public JsonObject addDeviceManagementExtension(JsonObject request) throws IoTFCReSTException {
		return addDeviceManagementExtension(request.toString());
	}
	
	/**
	 * Add a Device Management Extension.
	 * 
	 * @param request JSON string containing the the DM Extension request.
	 * @return If successful, JsonObject response from Watson IoT Platform.
	 * @throws IoTFCReSTException if failed.
	 * @see IoTFCReSTException
	 */
	public JsonObject addDeviceManagementExtension(String request) throws IoTFCReSTException {
		final String METHOD = "addDeviceManagementExtension";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		int code = 0;
		String method = "post";
		try {
			StringBuilder sb = new StringBuilder("https://");
			sb.append(orgId).
			   append('.').
			   append(this.domain).append(BASIC_API_V0002_URL).
			   append("/mgmt/custom/bundle");
			response = connect(method, sb.toString(), request, null);
			code = response.getStatusLine().getStatusCode();
			if (code == 201 || code == 400 || code == 401 || code == 403 || code == 409 || code == 500) {
				String result = this.readContent(response, METHOD);
				jsonResponse = new JsonParser().parse(result);
				if (code == 201) {
					//Success
					return jsonResponse.getAsJsonObject();
				} else {
					String reason = null;
					switch (code) {
					case 400:
						reason = IoTFCReSTException.HTTP_ADD_DM_EXTENSION_ERR_400;
						break;
					case 401:
						reason = IoTFCReSTException.HTTP_ADD_DM_EXTENSION_ERR_401;
						break;
					case 403:
						reason = IoTFCReSTException.HTTP_ADD_DM_EXTENSION_ERR_403;
						break;
					case 409:
						reason = IoTFCReSTException.HTTP_ADD_DM_EXTENSION_ERR_409;
						break;
					case 500:
						reason = IoTFCReSTException.HTTP_ADD_DM_EXTENSION_ERR_500;
						break;
					}
					throw new IoTFCReSTException(method, sb.toString(), request, code, reason, jsonResponse);
				}
			} else {
				throw new IoTFCReSTException(code, "Unexpected error");
			}
		} catch (Exception e) {
			// This includes JsonSyntaxException
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding the Device Management Extension "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}
	
	/**
	 * Delete a registered Device Management Extension.
	 * 
	 * @param bundleId The bundle ID of the registered Device Management Extension.
	 * @throws IoTFCReSTException if failed.
	 * @see IoTFCReSTException
	 */
	public void deleteDeviceManagementExtension(String bundleId) throws IoTFCReSTException {
		final String METHOD = "deleteDeviceManagementExtension";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		int code = 0;
		try {
			StringBuilder sb = new StringBuilder("https://");
			sb.append(orgId).
			   append('.').
			   append(this.domain).append(BASIC_API_V0002_URL).
			   append("/mgmt/custom/bundle/" + bundleId);
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if (code == 204) {
				//Success
				return;
			}
			if (code == 400 || code == 401 || code == 403 || code == 500) {
				String result = this.readContent(response, METHOD);
				jsonResponse = new JsonParser().parse(result);
					String reason = null;
					switch (code) {
					case 400:
						reason = new String("Invalid request");
						break;
					case 401:
						reason = new String("Unauthorized");
						break;
					case 403:
						reason = new String("Forbidden");
						break;
					case 500:
						reason = new String("Internal server error");
						break;
					}
					throw new IoTFCReSTException(code, reason, jsonResponse);
			} else {
				throw new IoTFCReSTException(code, "Unexpected error");
			}
		} catch (Exception e) {
			// This includes JsonSyntaxException
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding the Device Management Extension "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}		
	}

	/**
	 * Get a specific registered device management extension.
	 * 
	 * @param bundleId
	 * @return If successful, JsonObject response from Watson IoT Platform.
	 * @throws IoTFCReSTException if failed.
	 * @see IoTFCReSTException
	 */
	public JsonObject getDeviceManagementExtension(String bundleId) throws IoTFCReSTException {
		final String METHOD = "addDeviceManagementExtension";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		int code = 0;
		try {
			StringBuilder sb = new StringBuilder("https://");
			sb.append(orgId).
			   append('.').
			   append(this.domain).append(BASIC_API_V0002_URL).
			   append("/mgmt/custom/bundle/" + bundleId);
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if (code == 200 || code == 400 || code == 401 || code == 403 || code == 404 || code == 500) {
				String result = this.readContent(response, METHOD);
				jsonResponse = new JsonParser().parse(result);
				if (code == 200) {
					return jsonResponse.getAsJsonObject();
				} else {
					String reason = null;
					switch (code) {
					case 400:
						reason = new String("Invalid request");
						break;
					case 401:
						reason = new String("Unauthorized");
						break;
					case 403:
						reason = new String("Forbidden");
						break;
					case 404:
						reason = new String("Not Found");
						break;
					case 500:
						reason = new String("Internal server error");
						break;
					}
					throw new IoTFCReSTException(code, reason, jsonResponse);
				}
			} else {
				throw new IoTFCReSTException(code, "Unexpected error");
			}
		} catch (Exception e) {
			// This includes JsonSyntaxException
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding the Device Management Extension "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	/**
	 * Initiates a device management request, such as reboot.
	 * 
	 * @param request JSON object containing the management request
	 * 
	 * @return boolean response containing the status of the initiate DM request
	 *  
	 * @throws IoTFCReSTException Failure in initiating a DM request
	 */
	public boolean initiateDeviceManagementRequest(JsonObject request) throws IoTFCReSTException {
		try {
			initiateDMRequest(request);
			return true;
		} catch (IoTFCReSTException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
		// Unreachable code
		//return false;
	}
	
	/**
	 * Initiates a device management request, such as reboot.
	 * 
	 * @param request JSON object containing the management request
	 * @return JSON object containing the response from Watson IoT Platform
	 * @throws IoTFCReSTException Failure in initiating a DM request
	 */
	public JsonObject initiateDMRequest(JsonObject request) throws IoTFCReSTException {
		final String METHOD = "initiateDMRequest";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).append('.').append(this.domain).append(BASIC_API_V0002_URL).
		   append("/mgmt/requests");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		String method = "post";
		IoTFCReSTException ex = null;
		try {
			response = connect(method, sb.toString(), request.toString(), null);
			code = response.getStatusLine().getStatusCode();
			switch (code) {
			case 202:
				String result = this.readContent(response, METHOD);
				jsonResponse = new JsonParser().parse(result);
				break;
			case 500:
				ex = new IoTFCReSTException(method, sb.toString(), request.toString(), code, IoTFCReSTException.HTTP_INITIATE_DM_REQUEST_ERR_500, null);
				break;
			default:
				ex = new IoTFCReSTException(method, sb.toString(), request.toString(), code, IoTFCReSTException.HTTP_ERR_UNEXPECTED, null);
			}
		} catch(Exception e) {
			ex = new IoTFCReSTException("Failure in initiating the Device management Request "
					+ "::"+e.getMessage());
			ex.initCause(e);
		}
		if (jsonResponse != null) {
			return jsonResponse.getAsJsonObject();
		} else {
			if (ex != null) {
				throw ex;
			}
			return null;
		}
	}
	

	/**
	 * Clears the status of a device management request. The status for a 
	 * request that has been completed is automatically cleared soon after 
	 * the request completes. You can use this operation to clear the status 
	 * for a completed request, or for an in-progress request which may never 
	 * complete due to a problem.
	 * 
	 * @param requestId String ID representing the management request
	 * @return JSON response containing the newly initiated request.
	 *  
	 * @throws IoTFCReSTException Failure in deleting a DM request
	 */
	public boolean deleteDeviceManagementRequest(String requestId) throws IoTFCReSTException {
		String METHOD = "deleteDeviceManagementRequest";
		/**
		 * Form the url based on the swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/mgmt/requests/").append(requestId);

		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		try {
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if(code == 204) {
				return true;
			}
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Device management Request "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error", jsonResponse);
		}
		throw new IoTFCReSTException(code, "", jsonResponse);
	}
	
	/**
	 * Gets details of a device management request.
	 * 
	 * @param requestId String ID representing the management request
	 * @return JSON response containing the device management request
	 *  
	 * @throws IoTFCReSTException Failure in retrieving a DM request
	 */
	public JsonObject getDeviceManagementRequest(String requestId) throws IoTFCReSTException {
		final String METHOD = "getDeviceManagementRequest";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/mgmt/requests/").append(requestId);
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Device management Request "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "Request status not found");
		}
		throwException(response, METHOD);
		return null;
	}

	/**
	 * Get a list of device management request device statuses
	 * 
	 * @param requestId String ID representing the management request
	 * @param parameters list of query parameters that controls the output.
	 * 
	 * @return JSON response containing the device management request
	 *  
	 * @throws IoTFCReSTException Failure in retrieving a DM request status
	 */
	public JsonObject getDeviceManagementRequestStatus(String requestId, 
			List<NameValuePair> parameters) throws IoTFCReSTException {
		
		final String METHOD = "getDeviceManagementRequestStatus";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/mgmt/requests/").
		   append(requestId).
		   append("/deviceStatus");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, parameters);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the Device management Request "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "Request status not found", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * Get a list of device management request device statuses
	 * 
	 * @param requestId String ID representing the management request
	 * @return JSON response containing the device management request
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the DM request status
	 */
	public JsonObject getDeviceManagementRequestStatus(String requestId) throws IoTFCReSTException {
		return getDeviceManagementRequestStatus(requestId, null);
	}


	/**
	 * Get an individual device mangaement request device status
	 * 
	 * @param requestId String ID representing the management request
	 * @param deviceType Device Type of the device
	 * @param deviceId Device Id of the device
	 * 
	 * @return JSON response containing the device management request
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the DM request device status
	 */
	public JsonObject getDeviceManagementRequestStatusByDevice(String requestId, 
			String deviceType, String deviceId) throws IoTFCReSTException {
		
		final String METHOD = "getDeviceManagementRequestStatusByDevice";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/mgmt/requests/").
		   append(requestId).
		   append("/deviceStatus/").
		   append(deviceType).
		   append('/').
		   append(deviceId);
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the Device management Request "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "Request status not found", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * Retrieve the number of active devices over a period of time
	 * 
	 * @param startDate Start date in one of the following formats: YYYY (last day of the year), 
	 * YYYY-MM (last day of the month), YYYY-MM-DD (specific day)
	 * 
	 * @param endDate End date in one of the following formats: YYYY (last day of the year), 
	 * YYYY-MM (last day of the month), YYYY-MM-DD (specific day)
	 * 
	 * @param detail Indicates whether a daily breakdown will be included in the resultset
	 * 
	 * @return JSON response containing the active devices over a period of time
	 *  
	 * @throws IoTFCReSTException Failure in retrieving all active device details
	 */
	public JsonObject getActiveDevices(String startDate, String endDate, boolean detail) throws IoTFCReSTException {
		final String METHOD = "getActiveDevices";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/usage/active-devices");
		
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		if(startDate != null) {
			parameters.add(new BasicNameValuePair("start", startDate));
		}
		if(endDate != null) {
			parameters.add(new BasicNameValuePair("end", endDate));
		}
		parameters.add(new BasicNameValuePair("detail", Boolean.toString(detail)));
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, parameters);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the Active Devices "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Bad Request", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throw new IoTFCReSTException(code, "", jsonResponse);
	}
	
	/**
	 * Retrieve the amount of storage being used by historical event data
	 * 
	 * @param startDate Start date in one of the following formats: YYYY (last day of the year), 
	 * YYYY-MM (last day of the month), YYYY-MM-DD (specific day)
	 * 
	 * @param endDate End date in one of the following formats: YYYY (last day of the year), 
	 * YYYY-MM (last day of the month), YYYY-MM-DD (specific day)
	 * 
	 * @param detail Indicates whether a daily breakdown will be included in the resultset
	 * 
	 * @return JSON response containing the active devices over a period of time
	 *  
	 * @throws IoTFCReSTException Failure in retrieving historical data usage
	 */
	public JsonObject getHistoricalDataUsage(String startDate, String endDate, boolean detail) throws IoTFCReSTException {
		final String METHOD = "getHistoricalDataUsage";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/usage/historical-data");
		
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		if(startDate != null) {
			parameters.add(new BasicNameValuePair("start", startDate));
		}
		if(endDate != null) {
			parameters.add(new BasicNameValuePair("end", endDate));
		}
		parameters.add(new BasicNameValuePair("detail", Boolean.toString(detail)));
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, parameters);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the historical data storage "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Bad Request", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error", jsonResponse);
		}
		throw new IoTFCReSTException(code, "", jsonResponse);
	}
	
	/**
	 * Retrieve the amount of data used
	 * 
	 * @param startDate Start date in one of the following formats: YYYY (last day of the year), 
	 * YYYY-MM (last day of the month), YYYY-MM-DD (specific day)
	 * 
	 * @param endDate End date in one of the following formats: YYYY (last day of the year), 
	 * YYYY-MM (last day of the month), YYYY-MM-DD (specific day)
	 * 
	 * @param detail Indicates whether a daily breakdown will be included in the resultset
	 * 
	 * @return JSON response containing the active devices over a period of time
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the data traffic
	 */
	public JsonObject getDataTraffic(String startDate, String endDate, boolean detail) throws IoTFCReSTException {
		final String METHOD = "getDataTraffic";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/usage/data-traffic");
		
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		if(startDate != null) {
			parameters.add(new BasicNameValuePair("start", startDate));
		}
		if(endDate != null) {
			parameters.add(new BasicNameValuePair("end", endDate));
		}
		parameters.add(new BasicNameValuePair("detail", Boolean.toString(detail)));
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, parameters);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the data traffic "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Bad Request", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error", jsonResponse);
		}
		throw new IoTFCReSTException(code, "", jsonResponse);
	}
	
	/**
	 * Retrieve the status of services for an organization
	 * 
	 * @return JSON response containing the status of services for an organization
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the service status
	 */
	public JsonObject getServiceStatus() throws IoTFCReSTException {
		final String METHOD = "getServiceStatus";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/service-status");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the service status "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error", jsonResponse);
		}
		throw new IoTFCReSTException(code, "", jsonResponse);
	}

	/**
	 * Register a new device under the given gateway.
	 *  
	 * The response body will contain the generated authentication token for the device. 
	 * The caller of the method must make sure to record the token when processing 
	 * the response. The IBM Watson IoT Platform will not be able to retrieve lost authentication tokens.
	 * 
	 * @param deviceType DeviceType ID
	 * @param deviceId device to be added.
	 * @param gwTypeId The device type of the gateway
	 * @param gwDeviceId The deviceId of the gateway
	 * 
	 * @return JsonObject containing the generated authentication token for the device. 
	 *  
	 * @throws IoTFCReSTException Failure in registering a device under the gateway
	 */

	public JsonObject registerDeviceUnderGateway(String deviceType, String deviceId,
			String gwTypeId, String gwDeviceId) throws IoTFCReSTException {
		
		JsonObject deviceObj = new JsonObject();
		deviceObj.addProperty("deviceId", deviceId);
		deviceObj.addProperty("gatewayId", gwDeviceId);
		deviceObj.addProperty("gatewayTypeId", gwTypeId);
		
		return this.registerDevice(deviceType, deviceObj);
	}
	
	        /**
	 * This method retrieves all last events for a specific device
	 * 
	 * <p>
	 * Refer to the <a href=
	 * "https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Event_Cache/get_device_types_deviceType_devices_deviceId_events"
	 * >link</a> for more information about the query parameters and response in
	 * JSON format.
	 * </p>
	 * 
	 * @param deviceType
	 *            String which contains device type
	 * @param deviceId
	 *            String which contains device id
	 * 
	 * @return JsonElement containing the last event
	 * @throws IoTFCReSTException Failure in retrieving the last event
	 */
	public JsonElement getLastEvents(String deviceType, String deviceId)
			throws IoTFCReSTException {

		String METHOD = "getLastEvents(2)";
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).append('.').append(this.domain).append(BASIC_API_V0002_URL)
				.append("/device");

		if (deviceType != null) {
			sb.append("/types/").append(deviceType);
		}

		if (deviceId != null) {
			sb.append("/devices/").append(deviceId);
		}
		sb.append("/events");

		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			String result = this.readContent(response, METHOD);
			JsonElement jsonResponse = new JsonParser().parse(result);

			code = response.getStatusLine().getStatusCode();
			if (code == 400) {
				throw new IoTFCReSTException(400, "Invalid request",
						jsonResponse);
			} else if (code == 403) {
				throw new IoTFCReSTException(403, "Forbidden", jsonResponse);
			} else if (code == 500) {
				throw new IoTFCReSTException(500, "Internal server error",
						jsonResponse);
			}

			return jsonResponse;
		} catch (Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException(code,
					"Failure in retrieving " + "the last events. :: "
							+ e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	/**
	 * This method returns last event for a specific event id for a specific
	 * device
	 * 
	 * <p>
	 * Refer to the <a href=
	 * "https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Event_Cache/get_device_types_deviceType_devices_deviceId_events_eventName"
	 * >link</a> for more information about the query parameters and response in
	 * JSON format.
	 * </p>
	 * 
	 * @param deviceType
	 *            String which contains device type
	 * @param deviceId
	 *            String which contains device id
	 * @param eventId
	 *            String which contains event id
	 * 
	 * @return JsonElement Containing the last event
	 * @throws IoTFCReSTException Failure in retrieving the last event
	 */
	public JsonElement getLastEvent(String deviceType, String deviceId,
			String eventId) throws IoTFCReSTException {

		String METHOD = "getLastEvent(3)";
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).append('.').append(this.domain).append(BASIC_API_V0002_URL)
				.append("/device");

		if (deviceType != null) {
			sb.append("/types/").append(deviceType);
		}

		if (deviceId != null) {
			sb.append("/devices/").append(deviceId);
		}

		if (eventId != null) {
			sb.append("/events/").append(eventId);
		}

		int code = 0;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			JsonElement jsonResponse = new JsonParser().parse(result);

			if (code == 400) {
				throw new IoTFCReSTException(400, "Invalid request",
						jsonResponse);
			} else if (code == 403) {
				throw new IoTFCReSTException(403, "Forbidden", jsonResponse);
			} else if (code == 500) {
				throw new IoTFCReSTException(500, "Internal server error",
						jsonResponse);
			}

			return jsonResponse;
		} catch (Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException(code,
					"Failure in retrieving " + "the last event. :: "
							+ e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}
	
	private void validateNull (String property, String value) throws Exception {
		if(value == null || value == "")
			throw new Exception(property + "cannot be NULL or Empty!");
	}

	private boolean publishMessageOverHTTP(String eventId, JsonObject payload,
			boolean isApplication, boolean isCommand) throws Exception {

		final String METHOD = "publishMessageOverHTTP";
		StringBuilder sb = new StringBuilder();
		String port;
		validateNull("Organization ID", orgId);
		validateNull("Device Type", mdeviceType);
		validateNull("Device ID", mdeviceId);
		validateNull("Event Name", eventId);
				
		/**
		 * Form the url based on this swagger documentation
		 */

		if (isSecured) {
			sb.append("https://");
			port = "8883";
		} else {
			sb.append("http://");
			port = "1883";
		}

		String TYPE = "/device";
		if (isApplication)
			TYPE = "/application";

		String MESSAGE = "/events/";
		if(isCommand)
			MESSAGE = "/commands/";
			
		sb.append(orgId).append(".messaging.").append(domain).append(":")
				.append(port).append(BASIC_API_V0002_URL).append(TYPE)
				.append("/types/").append(mdeviceType).append("/devices/")
				.append(mdeviceId).append(MESSAGE).append(eventId);

		
		int code = 0;
		boolean ret = false;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		
		System.out.println("authKey: "+ authKey+ " authToken: "+ this.authToken);
		try {
			response = connect("post", sb.toString(), payload.toString(), null);			
			code = response.getStatusLine().getStatusCode();			
			if (code == 200) {
				// success
				ret = true;
			}

		} catch (Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException(
					"Failure in adding the device Type " + "::"
							+ e.getMessage());
			ex.initCause(e);
			throw ex;
		}

		if (code == 400) {
			throw new IoTFCReSTException(400,
					"Invalid request (No body, invalid JSON, "
							+ "unexpected key, bad value)", jsonResponse);
		} else if (code == 401) {
			throw new IoTFCReSTException(401,
					"The authentication token is empty or invalid");
		} else if (code == 403) {
			throw new IoTFCReSTException(403,
					"The authentication method is invalid or "
							+ "the API key used does not exist");
		} else if (code == 409) {
			throw new IoTFCReSTException(409, "The device type already exists",
					jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(500, "Unexpected error");
		} else if (ret == false) {
			throwException(response, METHOD);
		}

		return ret;
	}
	
	/**
	 * Publishes events over HTTP for a device and application
	 * 
	 * @param eventId String representing the eventId to be added. 
	 * @param payload JSON object representing the payload to be added. Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_device_types_deviceType_devices_deviceId_events_eventName">link</a>
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_application_types_deviceType_devices_deviceId_events_eventName">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean indicates status of publishing event.
	 *  
	 * @throws IoTFCReSTException Failure publishing event.	 * 
	 */
 
	public boolean publishDeviceEventOverHTTP(String eventId, JsonObject payload) throws Exception {
		boolean ret = false;		
		ret = publishMessageOverHTTP(eventId, payload, false, false);
		return ret;
	}
	
	/**
	 * Publishes events over HTTP for a device and application
	 * 
	 * @param eventId String representing the eventId to be added. 
	 * @param payload JSON object representing the payload to be added. Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_device_types_deviceType_devices_deviceId_events_eventName">link</a>
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_application_types_deviceType_devices_deviceId_events_eventName">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean indicates status of publishing event.
	 *  
	 * @throws IoTFCReSTException Failure publishing event.	 * 
	 */
 
	public boolean publishDeviceEventOverHTTP(String eventId, JsonObject payload, String contenttype) throws Exception {
		boolean ret = false;
		contentType = contenttype;
		ret = publishDeviceEventOverHTTP(eventId, payload);
		return ret;
	}

	/**
	 * Application Publishes events on behalf of device over HTTP 
	 * 
	 * @param eventId String representing the eventId to be added. 
	 * @param payload JSON object representing the payload to be added. Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_device_types_deviceType_devices_deviceId_events_eventName">link</a>
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_application_types_deviceType_devices_deviceId_events_eventName">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean indicates status of publishing event.
	 *  
	 * @throws IoTFCReSTException Failure publishing event.	 * 
	 */
 
	public boolean publishApplicationEventforDeviceOverHTTP(String deviceId, String deviceType, String eventId, JsonObject payload) throws Exception {
		boolean ret = false;		
		this.mdeviceId = deviceId;
		this.mdeviceType = deviceType;
		ret = publishMessageOverHTTP(eventId, payload, true, false);
		return ret;
	}
	
	/**
	 * Application Publishes events on behalf of device over HTTP 
	 * 
	 * @param eventId String representing the eventId to be added. 
	 * @param payload JSON object representing the payload to be added. Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_device_types_deviceType_devices_deviceId_events_eventName">link</a>
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_application_types_deviceType_devices_deviceId_events_eventName">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean indicates status of publishing event.
	 *  
	 * @throws IoTFCReSTException Failure publishing event.	 * 
	 */
 
	public boolean publishApplicationEventforDeviceOverHTTP(String deviceId, String deviceType, String eventId, JsonObject payload, String contenttype) throws Exception {
		boolean ret = false;		
		contentType = contenttype;
		ret = publishApplicationEventforDeviceOverHTTP(deviceId, deviceType, eventId, payload);
		return ret;
	}

	/**
	 * Publishes commands over HTTP for an application
	 * 
	 * @param eventId String representing the eventId to be added. 
	 * @param payload JSON object representing the payload to be added. Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_device_types_deviceType_devices_deviceId_events_eventName">link</a>
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_application_types_deviceType_devices_deviceId_events_eventName">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean indicates status of publishing event.
	 *  
	 * @throws IoTFCReSTException Failure publishing event.	 * 
	 */
	public boolean publishCommandOverHTTP(String eventId, JsonObject payload) throws Exception {
		boolean ret = false;
		ret = publishMessageOverHTTP(eventId, payload, true, true);
		return ret;
	}
	
	/**
	 * Publishes commands over HTTP for an application
	 * 
	 * @param eventId String representing the eventId to be added. 
	 * @param payload JSON object representing the payload to be added. Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_device_types_deviceType_devices_deviceId_events_eventName">link</a>
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_application_types_deviceType_devices_deviceId_events_eventName">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean indicates status of publishing event.
	 *  
	 * @throws IoTFCReSTException Failure publishing event.	 * 
	 */
	public boolean publishCommandOverHTTP(String eventId, JsonObject payload, String contenttype) throws Exception {
		boolean ret = false;
		contentType = contenttype;
		ret = publishCommandOverHTTP(eventId, payload);
		return ret;
	}

}
