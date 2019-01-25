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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.net.util.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
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
	
	private Properties properties = null;

	private String authKey = null;
	private String authToken = null;
	private SSLContext sslContext = null;
	private String orgId = null;
	private String mdeviceType = null;
	private String mdeviceId = null;

	private String domain;
	private boolean isQuickstart = false;
	
	// Enum for content-type header
		public enum ContentType {
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
	
	public enum SchemaOperation {

		VALIDATE("validate-configuration"), 
		ACTIVATE("activate-configuration"),
		DEACTIVATE("deactivate-configuration");
		
		SchemaOperation(String operation) {
			stateOperation = operation;
		}
		
		public String getOperation() {
			return stateOperation;
		}
		
		private String stateOperation;
	}
		
	private ContentType contentType = ContentType.json;
	private boolean isSecured = true;
	
	/**
	 * API client constructor
	 * 
	 * @param props Properties of this API client object
	 * @throws NoSuchAlgorithmException Thrown if failed to get an instance of SSL context
	 * @throws KeyManagementException Thrown if SSL context failed to initialzie
	 */
	public APIClient(Properties props) throws NoSuchAlgorithmException, KeyManagementException {
		boolean isGateway = false;
		String authKeyPassed = null;
		
		this.properties = new Properties(props);
		
		if("gateway".equalsIgnoreCase(APIClient.getAuthMethod(properties))) {
			isGateway = true;
		} else if("device".equalsIgnoreCase(APIClient.getAuthMethod(properties))) {
			authKey = "use-token-auth";
		} else {
			authKeyPassed = properties.getProperty("auth-key");
			if(authKeyPassed == null) {
				authKeyPassed = properties.getProperty("API-Key");
			}
		
			authKey = trimedValue(authKeyPassed);
		}
		String token = properties.getProperty("API-Token");
		if (token == null) {
			token = properties.getProperty("auth-token");
			if (token == null) {
				token = properties.getProperty("Authentication-Token");
			}
		}
		authToken = trimedValue(token);

		String org = null;
		org = properties.getProperty("org");
		
		if(org == null) {
			org = properties.getProperty("Organization-ID");
		}
		
		this.orgId = trimedValue(org);
		this.domain = getDomain(properties);
		
		if(this.orgId == null || this.orgId.equalsIgnoreCase("quickstart"))
			isQuickstart = true;
		this.mdeviceType = this.getDeviceType(properties);
		this.mdeviceId = this.getDeviceId(properties);
		this.isSecured = this.IsSecuredConnection(properties);
		if(isGateway) {
			authKey = "g/" + this.orgId + '/' + mdeviceType + '/' + mdeviceId;
		}
		
		TrustManager[] trustAllCerts = null;
		boolean trustAll = false;
		
		String value = properties.getProperty("Trust-All-Certificates");
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
	 * Return the properties of this API client object.
	 * 
	 * @return Properties
	 */
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * Get domain from properties
	 * 
	 * @param propertiesions List of properties 
	 * @return the domain e.g. internetofthings.ibmcloud.com
	 */
	protected String getDomain(Properties propertiesions) {
		String domain;
		domain = propertiesions.getProperty("domain");
		
		if(domain == null) {
			domain = propertiesions.getProperty("Domain");
		}
		domain = trimedValue(domain);
		
		if(domain != null && !("".equals(domain))) {
			return domain;
		} else {
			return AbstractClient.DEFAULT_DOMAIN;
		}
	}
	
	private static String getAuthMethod(Properties properties) {
		String method = properties.getProperty("auth-method");
		if(method == null) {
			method = properties.getProperty("Authentication-Method");
		}
		
		return trimedValue(method);
	}

	/**
	 * Get Device ID from properties
	 * 
	 * @param propertiesions Properties to check for device ID
	 * @return String Device ID
	 */
	protected String getDeviceId(Properties propertiesions) {
		String id;
		id = propertiesions.getProperty("Gateway-ID");
		if(id == null) {
			id = propertiesions.getProperty("Device-ID");
		}
		if(id == null) {
			id = propertiesions.getProperty("id");
		}
		return trimedValue(id);
	}
	
	/**
	 * Get Device Type from properties
	 * @param propertiesions Properties to check for device type
	 * @return String Device Type
	 */
	protected String getDeviceType(Properties propertiesions) {
		String type;
		type = propertiesions.getProperty("Gateway-Type");
		if(type == null) {
			type = propertiesions.getProperty("Device-Type");
		}
		if(type == null) {
			type = propertiesions.getProperty("type");
		}
		return trimedValue(type);
	}
	
	/**
	 * Check if the connection is secured.
	 * 
	 * @param propertiesions Properties to check for SSL connection
	 * @return true if connection is secured, false otherwise
	 */
	protected boolean IsSecuredConnection(Properties propertiesions) {
		boolean type = true;
		String id;
		id = propertiesions.getProperty("Secure");
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
			List<NameValuePair> queryParameters) throws URISyntaxException, IOException {
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
			case "patch":
				return casePatchFromConnect(queryParameters, url, METHOD,input, encodedString);

		}
		return null;
			
	}
	
	private HttpResponse casePostFromConnect(List<NameValuePair> queryParameters, String url, String method, StringEntity input, String encodedString) throws URISyntaxException, IOException {
		URIBuilder builder = new URIBuilder(url);
		if(queryParameters != null) {
			builder.setParameters(queryParameters);
		}

		//ContentType content = ContentType.valueOf(contentType);		
		HttpPost post = new HttpPost(builder.build());
		post.setEntity(input);
		post.addHeader("Content-Type", contentType.getType());
		post.addHeader("Accept", "application/json");
		if(isQuickstart == false)
		post.addHeader("Authorization", "Basic " + encodedString);
		try {
			HttpClient client = HttpClientBuilder.create().useSystemProperties().setSslcontext(sslContext).build();
			
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
			HttpClient client = HttpClientBuilder.create().useSystemProperties().setSslcontext(sslContext).build();
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
			HttpClient client = HttpClientBuilder.create().useSystemProperties().setSslcontext(sslContext).build();					
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
			HttpClient client = HttpClientBuilder.create().useSystemProperties().setSslcontext(sslContext).build();					
			return client.execute(delete);
		} catch (IOException e) {
			LoggerUtility.warn(CLASS_NAME, method, e.getMessage());
			throw e;
		} 

	}
	
	private HttpResponse casePatchFromConnect(List<NameValuePair> queryParameters, String url, String method, StringEntity input, String encodedString) throws URISyntaxException, IOException {
		URIBuilder putBuilder = new URIBuilder(url);
		if(queryParameters != null) {
			putBuilder.setParameters(queryParameters);
		}
		HttpPatch patch = new HttpPatch(putBuilder.build());
		patch.setEntity(input);
		patch.addHeader("Content-Type", "application/json");
		patch.addHeader("Accept", "application/json");
		patch.addHeader("Authorization", "Basic " + encodedString);
		try {
			HttpClient client = HttpClientBuilder.create().useSystemProperties().setSslcontext(sslContext).build();
			return client.execute(patch);
		} catch (IOException e) {
			LoggerUtility.warn(CLASS_NAME, method, e.getMessage());
			throw e;
		} 

	}	
	
	private String readContent(HttpResponse response, String method) 
			throws IllegalStateException, IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
		String line = null;
		StringBuffer responseLine = new StringBuffer();
		try {
			while((line = br.readLine()) != null) {
				responseLine.append(line);
			}
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
		return responseLine.toString();
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
		JsonElement jsonResponse = null;
		try {			
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the Device URL("
					+ sb.toString() + ") Response code (" + code + ") Exception: "+ e.getMessage());
			ex.initCause(e);
			throw ex;
		}

		if (code == 200) {
			return true;
		} else if (code == 404) {
			return false;
		} else if (code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the Device "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The device type or device does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieveing the Device Location "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 404) {
			throw new IoTFCReSTException(code, "Device location information not found", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200 || code == 409) {
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
			throw new IoTFCReSTException(code, "Device location information not found", jsonResponse);
		} else if(code == 409) {
			throw new IoTFCReSTException(code, "The update could not be completed due to a conflict", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * Gets Weather location information for a device.
	 * 
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/get_device_types_typeId_devices_deviceId_location">link</a>
	 * for more information about the response.</p>
	 * 
	 * @param deviceType String which contains device type
	 * @param deviceId String which contains device id
	 * 
	 * @return JsonObject containing the device location weather
	 * @throws IoTFCReSTException Failure in getting the device location weather
	 */
	public JsonObject getDeviceLocationWeather(String deviceType, String deviceId) throws IoTFCReSTException {
		final String METHOD = "getDeviceLocationWeather";
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
		   append("/exts/twc/ops/geocode");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieveing the Device Location Weather "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 400) {
			throw new IoTFCReSTException(code, "Bad request. Most likely caused by your device lacking location information.", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "Not authorized. Most likely caused by an invalid API key being provided.", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "Device location information Weather not found", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		} else if(code == 503) {
			throw new IoTFCReSTException(code, "Service Unavailable", jsonResponse);
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieveing the Device Management Information "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 404) {
			throw new IoTFCReSTException(code, "Device not found", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200 || code == 409) {
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
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The organization, device type or device does not exist", jsonResponse);
		} else if(code == 409) {
			throw new IoTFCReSTException(code, "The update could not be completed due to a conflict", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				// success
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the Organization detail, "
					+ ":: "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the api key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The organization does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, parameters);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				// success
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the Device details, "
					+ ":: "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The organization or device type does not exist");
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, parameters);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				// success
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the Device details, "
					+ ":: "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The device type does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		int code = 0;
		try {
			response = connect("get", sb.toString(), null, parameters);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				// success
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the DeviceType details, "
					+ ":: "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		try {			
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the Device Type URL("
					+ sb.toString() + ") Response code (" + code + ") Exception: "+ e.getMessage());
			ex.initCause(e);
			throw ex;
		}

		if (code == 200) {
			return true;
		} else if(code == 404) {
			return false;
		} else if (code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the Device Type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The device type does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200 || code == 409) {
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
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The device type does not exist", jsonResponse);
		} else if(code == 409) {
			throw new IoTFCReSTException(code, "The update could not be completed due to a conflict", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 201 || code == 400 || code == 409) {
				// success
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
			throw new IoTFCReSTException(401, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(403, "The authentication method is invalid or "
					+ "the API key used does not exist");
		} else if (code == 409) {
			throw new IoTFCReSTException(409, "The device type already exists", jsonResponse);  
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
	 * @return true for success, false otherwise
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
		JsonElement jsonResponse = null;
		try {
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();

			if(code == 204) {
				return true;
			}

			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Device Type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 201 || code == 400 || code == 409) {
				// Get the response
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
		
		if (device != null) {
			JsonObject deviceObj = device.getAsJsonObject();
			deviceObj.addProperty("gatewayId", gatewayId);
			deviceObj.addProperty("gatewayTypeId", gatewayTypeId);
		}
		
		JsonObject jsonDevice = registerDevice(typeId, device);
		
		if (jsonDevice != null) {
			String gwClientID =  "g:" + this.orgId + ":" + gatewayTypeId + ":" + gatewayId;
			JsonArray jarrayGroups = getResourceGroups(gwClientID);
			if (jarrayGroups != null && jarrayGroups.size() > 0) {
				
				for (int j=0; j<jarrayGroups.size(); j++) {
					String groupId = jarrayGroups.get(j).getAsString();
					// Assign devices to the resource group
					JsonArray jarrayDevices = new JsonArray();
					
					JsonObject aDevice = new JsonObject();
					aDevice.addProperty("typeId", typeId);
					aDevice.addProperty("deviceId", device.getAsJsonObject().get("deviceId").getAsString());
					jarrayDevices.add(aDevice);
					
					JsonObject gwDevice = new JsonObject();
					gwDevice.addProperty("typeId", gatewayTypeId);
					gwDevice.addProperty("deviceId", gatewayId);
					jarrayDevices.add(gwDevice);
					
					try {
						assignDevicesToResourceGroup(groupId, jarrayDevices);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return jsonDevice;
	}

	/**
	 * Get devices that are connected through the gateway
	 * 
	 * @param gatewayType Gateway device type
	 * @param gatewayId Gateway device ID
	 * @see https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/org-admin.html#!/Device_Configuration/get_device_types_typeId_devices_deviceId_devices
	 * @return JSON object describes devices connected through the gateway
	 * @throws IoTFCReSTException
	 */
	public JsonObject getDevicesConnectedThroughGateway(String gatewayType, String gatewayId) throws IoTFCReSTException {
		List<NameValuePair> queryParms = null;
		return getDevicesConnectedThroughGateway(gatewayType, gatewayId, queryParms);
	}
	
	/**
	 * Get devices that are connected through the gateway
	 * 
	 * @param gatewayType Gateway device type
	 * @param gatewayId Gateway device ID
	 * @param bookmark 
	 * @see https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/org-admin.html#!/Device_Configuration/get_device_types_typeId_devices_deviceId_devices
	 * @return JSON object describes devices connected through the gateway
	 * @throws IoTFCReSTException
	 */
	public JsonObject getDevicesConnectedThroughGateway(String gatewayType, String gatewayId, String bookmark) throws IoTFCReSTException {
		List<NameValuePair> queryParms = null;
		if (bookmark != null) {
			queryParms = new ArrayList<>();
			queryParms.add(new BasicNameValuePair("_bookmark", bookmark));
		}
		return getDevicesConnectedThroughGateway(gatewayType, gatewayId, queryParms);	
	}
	
	/**
	 * Get devices that are connected through the gateway
	 * 
	 * @param gatewayType  Gateway device type
	 * @param gatewayId Gateway device ID
	 * @param queryParameters Query parameters such as _bookmark
	 * @return JSON object describes devices connected through the gateway
	 * @throws IoTFCReSTException
	 */
	public JsonObject getDevicesConnectedThroughGateway(String gatewayType, String gatewayId, List<NameValuePair> queryParameters) throws IoTFCReSTException {
		final String METHOD = "getDevicesConnectedThroughGateway";
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/").
		   append(gatewayType).
		   append("/devices/").
		   append(gatewayId).
		   append("/devices");
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;

		try {
			response = connect("get", sb.toString(), null, queryParameters);
			code = response.getStatusLine().getStatusCode();
			if (response != null) {
				String result = this.readContent(response, METHOD);
				if (result != null) {
					jsonResponse = new JsonParser().parse(result);
				}
			}
			if (code == 200) {
				return jsonResponse.getAsJsonObject();
			}

		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving devices under gateway "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 403) {
			throw new IoTFCReSTException(code, "The request is only allowed if the classId of the device type is Gateway", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "Device type or device not found", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
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
		JsonElement jsonResponse = null;
		try {
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
/*			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
*/			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Device"
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		try {
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
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
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
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
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		try {
			response = connect("post", sb.toString(), log.toString(), null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
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
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		try {
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
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
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the Diagnostic Log "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 404) {
			throw new IoTFCReSTException(code, "Device not found", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		try {
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
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
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				return jsonResponse.getAsJsonArray();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the diagnostic Errorcodes "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 404) {
			throw new IoTFCReSTException(code, "Device not found", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		try {
			response = connect("post", sb.toString(), errorcode.toString(), null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
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
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				return jsonResponse.getAsJsonArray();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the connection logs "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The device type does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code != 500) {
				// success
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
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if (code == 201) {
				return jsonResponse.getAsJsonArray();
			} else if (code == 202) {
				// Loop through all devices to determine success or failure.
				JsonArray devices = jsonResponse.getAsJsonArray();
				boolean success = true;
				for (Iterator<JsonElement> iterator = devices.iterator(); iterator.hasNext(); ) {
					JsonElement deviceElement = iterator.next();
					JsonObject jsonDevice = deviceElement.getAsJsonObject();
					if (jsonDevice.get("success").getAsBoolean() == false) {
						success = false;
						break;
					}
				}
				// All devices were deleted ?
				if (success == true) {
					return devices;
				}
			}
		} catch (Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Devices, "
					+ ":: "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		IoTFCReSTException e = null;
		switch (code) {
		case 202:
			e = new IoTFCReSTException(code, "Some devices deleted successfully", jsonResponse);
			break;
		case 400:
			e = new IoTFCReSTException(code, "Invalid request (No body, invalid JSON, unexpected key, bad value)", jsonResponse);
			break;
		case 413:
			e = new IoTFCReSTException(code, "Request content exceeds 512Kb", jsonResponse);
			break;
		case 500:
			e = new IoTFCReSTException(code, "Unexpected error", jsonResponse);
			break;
		default:
			e = new IoTFCReSTException(code, "", jsonResponse);
			break;
		}
		
		throw e;
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in getting the Device management Requests "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if (code == 201 || code == 400 || code == 401 || code == 403 || code == 409 || code == 500) {
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
				throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
			}
		} catch (IoTFCReSTException e) {
			throw e;
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if (code == 204) {
				//Success
				return;
			}
			if (code == 400 || code == 401 || code == 403 || code == 500) {
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
				throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
	 * @param bundleId bundle id
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if (code == 200 || code == 400 || code == 401 || code == 403 || code == 404 || code == 500) {
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
				throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			switch (code) {
			case 202:
				break;
			case 400:
				ex = new IoTFCReSTException(method, sb.toString(), request.toString(), code, IoTFCReSTException.HTTP_INITIATE_DM_REQUEST_ERR_400, jsonResponse);
				break;
			case 401:
				ex = new IoTFCReSTException(method, sb.toString(), request.toString(), code, IoTFCReSTException.HTTP_INITIATE_DM_REQUEST_ERR_401, jsonResponse);
				break;
			case 403:
				ex = new IoTFCReSTException(method, sb.toString(), request.toString(), code, IoTFCReSTException.HTTP_INITIATE_DM_REQUEST_ERR_403, jsonResponse);
				break;
			case 404:
				ex = new IoTFCReSTException(method, sb.toString(), request.toString(), code, IoTFCReSTException.HTTP_INITIATE_DM_REQUEST_ERR_404, jsonResponse);
				break;
			case 500:
				ex = new IoTFCReSTException(method, sb.toString(), request.toString(), code, IoTFCReSTException.HTTP_INITIATE_DM_REQUEST_ERR_500, jsonResponse);
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
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 204) {
				return true;
			}

		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the DM Request for ID ("
					+ requestId + ")::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		IoTFCReSTException ex = null;
		String method = "get";
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			switch (code) {
			case 200:
				break;
			case 404:
				ex = new IoTFCReSTException(method, sb.toString(), null, code, IoTFCReSTException.HTTP_GET_DM_REQUEST_ERR_404, jsonResponse);
				break;
			case 500:
				ex = new IoTFCReSTException(method, sb.toString(), null, code, IoTFCReSTException.HTTP_GET_DM_REQUEST_ERR_500, jsonResponse);
				break;
			default:
				ex = new IoTFCReSTException(method, sb.toString(), null, code, IoTFCReSTException.HTTP_ERR_UNEXPECTED, null);
			}
		} catch(Exception e) {
			ex = new IoTFCReSTException("Failure in getting the DM Request for ID (" 
					+ requestId + ")::" + e.getMessage());
			ex.initCause(e);
			throw ex;
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
	 * @deprecated This method has been deprecated as /usage/active-devices is no longer supported
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
	@Deprecated
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
		} else if (code == 410) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throw new IoTFCReSTException(code, "", jsonResponse);
	}
	
	/**
	 * @deprecated: This functionality is no longer needed
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
	@Deprecated
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
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
			IoTFCReSTException ex = new IoTFCReSTException(code, "Failure in retrieving the data traffic "
					+ "::"+e.getMessage(), jsonResponse);
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
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
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
		JsonObject jsonDevice = new JsonObject();
		jsonDevice.addProperty("deviceId", deviceId);
		return this.registerDeviceUnderGateway(deviceType, gwDeviceId, gwTypeId, jsonDevice);
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
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);


			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
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
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);


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

	private boolean publishMessageOverHTTP(String eventId, Object payload,
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
				
		try {
			response = connect("post", sb.toString(), payload.toString(), null);			
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);			
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
					"The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(403,
					"The authentication method is invalid or "
							+ "the API key used does not exist", jsonResponse);
		} else if (code == 409) {
			throw new IoTFCReSTException(409, "The device type already exists",
					jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		} else if (ret == false) {
			throwException(response, METHOD);
		}

		return ret;
	}

	//This method has been kept only for backward compatibility and would soon be deprecated
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
				
		try {
			response = connect("post", sb.toString(), payload.toString(), null);			
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);			
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
					"The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(403,
					"The authentication method is invalid or "
							+ "the API key used does not exist");
		} else if (code == 409) {
			throw new IoTFCReSTException(409, "The device type already exists",
					jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		} else if (ret == false) {
			throwException(response, METHOD);
		}

		return ret;
	}
	
	/**
	 * Publishes events over HTTP for a device and application
	 * 
	 * @param eventId String representing the eventId to be added. 
	 * @param payload Object representing the payload to be added. Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_device_types_deviceType_devices_deviceId_events_eventName">link</a>
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_application_types_deviceType_devices_deviceId_events_eventName">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean indicates status of publishing event.
	 *  
	 * @throws IoTFCReSTException Failure publishing event.	 * 
	 */
 
	public boolean publishDeviceEventOverHTTP(String eventId, Object payload) throws Exception {
		boolean ret = false;		
		ret = publishMessageOverHTTP(eventId, payload, false, false);
		return ret;
	}
	
	//This method has been kept only for backward compatibility and would soon be deprecated
	/**
	 * Publishes events over HTTP for a device and application
	 * 
	 * @param eventId String representing the eventId to be added. 
	 * @param payload JsonObject representing the payload to be added. Refer to  
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
	 * @param payload Object representing the payload to be added. Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_device_types_deviceType_devices_deviceId_events_eventName">link</a>
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_application_types_deviceType_devices_deviceId_events_eventName">link</a> 
	 * for more information about the schema to be used
	 * @param contenttype Content type
	 * 
	 * @return boolean indicates status of publishing event.
	 *  
	 * @throws IoTFCReSTException Failure publishing event.	 * 
	 */
 
	public boolean publishDeviceEventOverHTTP(String eventId, Object payload, ContentType contenttype) throws Exception {
		boolean ret = false;
		contentType = contenttype;
		ret = publishDeviceEventOverHTTP(eventId, payload);
		return ret;
	}

	
	//This method has been kept only for backward compatibility and would soon be deprecated 
	/**
	 * Publishes events over HTTP for a device and application
	 * 
	 * @param eventId String representing the eventId to be added. 
	 * @param payload JsonObject representing the payload to be added. Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_device_types_deviceType_devices_deviceId_events_eventName">link</a>
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_application_types_deviceType_devices_deviceId_events_eventName">link</a> 
	 * for more information about the schema to be used
	 * @param contenttype Content type
	 * 
	 * @return boolean indicates status of publishing event.
	 *  
	 * @throws IoTFCReSTException Failure publishing event.	 * 
	 */

	public boolean publishDeviceEventOverHTTP(String eventId, JsonObject payload, ContentType contenttype) throws Exception {
		boolean ret = false;
		contentType = contenttype;
		ret = publishDeviceEventOverHTTP(eventId, payload);
		return ret;
	}

	/**
	 * Application Publishes events on behalf of device over HTTP 
	 * 
	 * @param deviceId Device ID
	 * @param deviceType Device type
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
	 * @param deviceId Device ID
	 * @param deviceType Device type
	 * @param eventId String representing the eventId to be added. 
	 * @param payload JSON object representing the payload to be added. Refer to  
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_device_types_deviceType_devices_deviceId_events_eventName">link</a>
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Connectivity/post_application_types_deviceType_devices_deviceId_events_eventName">link</a> 
	 * for more information about the schema to be used
	 * @param contenttype Content type
	 * 
	 * @return boolean indicates status of publishing event.
	 *  
	 * @throws IoTFCReSTException Failure publishing event.	 * 
	 */
	public boolean publishApplicationEventforDeviceOverHTTP(String deviceId, String deviceType, String eventId, JsonObject payload, ContentType contenttype) throws Exception {
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
	 * @param contenttype Content type
	 * 
	 * @return boolean indicates status of publishing event.
	 *  
	 * @throws IoTFCReSTException Failure publishing event.	 * 
	 */
	public boolean publishCommandOverHTTP(String eventId, JsonObject payload, ContentType contenttype) throws Exception {
		boolean ret = false;
		contentType = contenttype;
		ret = publishCommandOverHTTP(eventId, payload);
		return ret;
	}

	
	/**
	 * Create a draft logical interface.
	 * 
	 * @param draftLogicalInterface JSON string containing the draft logical interface.
	 * 
	 * @return If successful, JsonObject response from Watson IoT Platform.
	 * @throws IoTFCReSTException if failed.
	 * @see IoTFCReSTException
	 */
	public JsonObject addDraftLogicalInterface(String draftLogicalInterface) throws IoTFCReSTException {
		final String METHOD = "addDraftLogicalInterface";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		int code = 0;
		String method = "post";
		try {
			StringBuilder sb = new StringBuilder("https://");
			sb.append(orgId).
			   append('.').
			   append(this.domain).append(BASIC_API_V0002_URL).
			   append("/draft/logicalinterfaces");
			response = connect(method, sb.toString(), draftLogicalInterface, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if (code == 201 || code == 400 || code == 401 || code == 403 || code == 500) {
				if (code == 201) {
					//Success
					return jsonResponse.getAsJsonObject();
				} else {
					String reason = null;
					switch (code) {
					case 400:
						reason = IoTFCReSTException.HTTP_ADD_LOGICAL_INTERFACE_ERR_400;
						break;
					case 401:
						reason = IoTFCReSTException.HTTP_ADD_LOGICAL_INTERFACE_ERR_401;
						break;
					case 403:
						reason = IoTFCReSTException.HTTP_ADD_LOGICAL_INTERFACE_ERR_403;
						break;
					case 500:
						reason = IoTFCReSTException.HTTP_ADD_LOGICAL_INTERFACE_ERR_500;
						break;
					}
					throw new IoTFCReSTException(method, sb.toString(), draftLogicalInterface, code, reason, jsonResponse);
				}
			} else {
				throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
			}
		} catch (IoTFCReSTException e) {
			throw e;
		} catch (Exception e) {
			// This includes JsonSyntaxException
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding the Draft Logical Interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}
	
	
	/**
	 * Deletes a draft logical interface.
	 * 
	 * @param logicalInterfaceId String to be deleted from IBM Watson IoT Platform
	 *   
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Logical_Interface/delete_logical_interface_Id">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return JSON object containing the response of draft logical interface.
	 *  
	 * @throws IoTFCReSTException Failure in deleting the draft logical interface
	 */

	public boolean deleteDraftLogicalInterface(String logicalInterfaceId) throws IoTFCReSTException {
		final String METHOD = "deleteDraftLogicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/logicalinterfaces/").
		   append(logicalInterfaceId);
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		String method = "delete";
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Logical Interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A logical interface with the specified id does not exist", jsonResponse);
		} else if(code == 409) {
			throw new IoTFCReSTException(code, "The logical interface with the specified id is currently being referenced by another object", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return false;
	}
	
	/**
	 * Retrieve the draft logical interface
	 *
	 * @param logicalInterfaceId String to be retrieved from IBM Watson IoT Platform
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Logical_Interfaces/get_draft_logicalinterfaces_logicalInterfaceId">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSON response containing the draft logical interface
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the draft logical interface
	 */
	public JsonObject getDraftLogicalInterface(String logicalInterfaceId) throws IoTFCReSTException {
		final String METHOD = "getDraftLogicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/logicalinterfaces/").
		   append(logicalInterfaceId);
		
		int code = 0;
		String method = "get";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);

			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the draft logical interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if (code == 304) {
			throw new IoTFCReSTException(code, "The state of the logical interface has not been modified (response to a conditional GET)");
		} else if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if (code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A logical interface with the specified id does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throw new IoTFCReSTException(code, "", jsonResponse);
	}
	
	/**
	 * Retrieve the active logical interface
	 *
	 * @param logicalInterfaceId String to be retrieved from IBM Watson IoT Platform
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Logical_Interfaces/get_logicalinterfaces">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSON response containing the active logical interface
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the active logical interface
	 */
	public JsonObject getActiveLogicalInterface(String logicalInterfaceId) throws IoTFCReSTException {
		final String METHOD = "getActiveLogicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/logicalinterfaces/").
		   append(logicalInterfaceId);
		
		int code = 0;
		String method = "get";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the active logical interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if (code == 304) {
			throw new IoTFCReSTException(code, "The state of the logical interface has not been modified (response to a conditional GET)");
		} else if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if (code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A logical interface with the specified id does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throw new IoTFCReSTException(code, "", jsonResponse);
	}

	
	/**
	 * Updates the location information for a device. If no date is supplied, the entry is added with the current date and time.
	 *  
	 * @param logicalInterfaceId String which contains draft logical interface id to be retrieved
	 * 
	 * @param draftLogicalInterface String which contains the LogicalInterface in JSON format
     *
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/put_device_types_typeId_devices_deviceId_location">link</a>
	 * for more information about the JSON format</p>.
	 *   
	 * @return A JSON response containing the status of the update operation.
	 * 
	 * @throws IoTFCReSTException Failure in updting the device location
	 */
	public JsonObject updateDraftLogicalInterface(String logicalInterfaceId, String draftLogicalInterface) throws IoTFCReSTException {
		final String METHOD = "updateDraftLogicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/logicalinterfaces/").
		   append(logicalInterfaceId);
		
		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		String method = "put";
		try {
			response = connect(method, sb.toString(), draftLogicalInterface, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200 || code == 409) {
				if(code == 200) {
					return jsonResponse.getAsJsonObject();
				}
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in updating the draft logical interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (Invalid resource id specified in the path, no body, invalid JSON, unexpected key, bad value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "A logical interface with the specified id does not exist", jsonResponse);
		} else if(code == 412) {
			throw new IoTFCReSTException(code, "The state of the logical interface has been modified since the "
					+ "client retrieved its representation (response to a conditional PUT)", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	
	/**
	 * Perform operation against Draft Logical Interface
	 *  
	 * @param logicalInterfaceId String which contains the logical interface id
	 * @param operation String contains the operation details in JSON format
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Logical_Interfaces/patch_logicalinterfaces_logicalInterfaceId">link</a>
	 * for more information about the JSON format</p>.
	 *   
	 * @return A JSON response containing the status of the patch operation.
	 * 
	 * @throws IoTFCReSTException Failure in updating the device location
	 */
	public JsonObject performOperationAgainstDraftLogicalInterface(String logicalInterfaceId, String operation) throws IoTFCReSTException {
		final String METHOD = "performOperationAgainstDraftLogicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/logicalinterfaces/").
		   append(logicalInterfaceId);
		
		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		String method = "patch";
		try {
			System.out.println("URL = " + sb.toString() + " operation = " + operation);
			response = connect(method, sb.toString(), operation, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200 || code == 202) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in performing an operation against a draft logical interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (Invalid resource id specified in the path, no body, invalid JSON, unexpected key, bad value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "A logical interface does not exist", jsonResponse);
		} else if(code == 409) {
			throw new IoTFCReSTException(code, "The activate-configuration operation failed because the Information Management metadata associated with the logical interface is invalid", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		} else {
			System.out.println("code = " + code);
			throwException(response, METHOD);
		}
		return null;
	}
	
	
	/**
	 * Perform operation against Logical Interface
	 *  
	 * @param logicalInterfaceId String which contains the logical interface id
	 * 
	 * @param operation String contains the operation in JSON format
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Logical_Interfaces/patch_logicalinterfaces_logicalInterfaceId">link</a>
	 * for more information about the JSON format</p>.
	 *   
	 * @return A JSON response containing the status of the patch operation.
	 * 
	 * @throws IoTFCReSTException Failure in updating the device location
	 */
	public JsonObject performOperationAgainstLogicalInterface(String logicalInterfaceId, String operation) throws IoTFCReSTException {
		final String METHOD = "performOperationAgainstLogicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/logicalinterfaces/").
		   append(logicalInterfaceId);
		
		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		String method = "patch";
		try {
			response = connect(method, sb.toString(), operation, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200 || code == 202) {
				
				return new JsonParser().parse(result).getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in performing an operation against a logical interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (Invalid resource id specified in the path, no body, invalid JSON, unexpected key, bad value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "A logical interface does not exist", jsonResponse);
		} else if(code == 409) {
			throw new IoTFCReSTException(code, "The deactivate operation failed because there is no active configuration associated with the logical interface", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	

	/**
	 * Get list of all draft logical interfaces
	 * 
	 * @param parameters list of query parameters that controls the output.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Logical_Interfaces/get_draft_logicalinterfaces">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSON response containing list of draft logical interfaces
	 *  
	 * @throws IoTFCReSTException Failure in retrieving draft logical interface request status
	 */
	public JsonObject getAllDraftLogicalInterfaces(List<NameValuePair> parameters) throws IoTFCReSTException {
		
		final String METHOD = "getAllDraftLogicalInterfaces";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/logicalinterfaces/");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving list of draft logical interfaces "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	
	/**
	 * Get list of all active logical interfaces
	 * 
	 * @param parameters list of query parameters that controls the output.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/get_device_types_typeId_logicalinterfaces">link</a>
	 * for more information about the JSON format</p>. 
	 * 
	 * @return JSON response containing list of active logical interfaces
	 *  
	 * @throws IoTFCReSTException Failure in retrieving active logical interface request status
	 */
	public JsonObject getActiveLogicalInterfaces(List<NameValuePair> parameters) throws IoTFCReSTException {
		
		final String METHOD = "getActiveLogicalInterfaces";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/logicalinterfaces/");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving list of active logical interfaces "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	/**
	 * Get list of all draft physical interfaces
	 * 
	 * @param parameters list of query parameters that controls the output.
	 * 
	 * @return JSON response containing list of draft physical interfaces
	 *  
	 * @throws IoTFCReSTException Failure in retrieving draft physical interface request status
	 */
	public JsonObject getDraftPhysicalInterfaces(List<NameValuePair> parameters) throws IoTFCReSTException {
		
		final String METHOD = "getDraftPhysicalInterfaces";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/physicalinterfaces/");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving list of draft physical interfaces "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	
	/**
	 * Create a draft physical interface.
	 * 
	 * @param draftPhysicalInterface JSON string containing the draft logical interface.
	 * 
	 * @return If successful, JsonObject response from Watson IoT Platform.
	 * @throws IoTFCReSTException if failed.
	 * @see IoTFCReSTException
	 */
	public JsonObject addDraftPhysicalInterface(String draftPhysicalInterface) throws IoTFCReSTException {
		final String METHOD = "addDraftPhysicalInterface";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		int code = 0;
		String method = "post";
		try {
			StringBuilder sb = new StringBuilder("https://");
			sb.append(orgId).
			   append('.').
			   append(this.domain).append(BASIC_API_V0002_URL).
			   append("/draft/physicalinterfaces");
			response = connect(method, sb.toString(), draftPhysicalInterface, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if (code == 201 || code == 400 || code == 401 || code == 403 || code == 500) {
				if (code == 201) {
					//Success
					return jsonResponse.getAsJsonObject();
				} else {
					String reason = null;
					switch (code) {
					case 400:
						reason = IoTFCReSTException.HTTP_ADD_PHYSICAL_INTERFACE_ERR_400;
						break;
					case 401:
						reason = IoTFCReSTException.HTTP_ADD_PHYSICAL_INTERFACE_ERR_401;
						break;
					case 403:
						reason = IoTFCReSTException.HTTP_ADD_PHYSICAL_INTERFACE_ERR_403;
						break;
					case 500:
						reason = IoTFCReSTException.HTTP_ADD_PHYSICAL_INTERFACE_ERR_500;
						break;
					}
					throw new IoTFCReSTException(method, sb.toString(), draftPhysicalInterface, code, reason, jsonResponse);
				}
			} else {
				System.out.println("Code = " + code);
				throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
			}
		} catch (IoTFCReSTException e) {
			throw e;
		} catch (Exception e) {
			// This includes JsonSyntaxException
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding the Draft Physical Interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}
	
	
	/**
	 * Deletes a draft physical interface.
	 * 
	 * @param physicalInterfaceId String to be deleted from IBM Watson IoT Platform
	 *   
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Physical_Interface/delete_physical_interface_Id">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean object containing the response of deletion operation.
	 *  
	 * @throws IoTFCReSTException Failure in deleting the draft physical interface
	 */

	public boolean deleteDraftPhysicalInterface(String physicalInterfaceId) throws IoTFCReSTException {
		final String METHOD = "deleteDraftPhysicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/physicalinterfaces/").
		   append(physicalInterfaceId);
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		String method = "delete";
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting the Physical Interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A physical interface with the specified id does not exist", jsonResponse);
		} else if(code == 409) {
			throw new IoTFCReSTException(code, "The physical interface with the specified id is currently being referenced by another object", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return false;
	}
	
	
	/**
	 * Retrieve the draft physical interface
	 *
	 * @param physicalInterfaceId String to be retrieved from IBM Watson IoT Platform
	 * 
	 * @return JSON response containing the draft physical interface
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the draft physical interface
	 */
	public JsonObject getDraftPhysicalInterface(String physicalInterfaceId) throws IoTFCReSTException {
		final String METHOD = "getDraftPhysicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/physicalinterfaces/").
		   append(physicalInterfaceId);
		
		int code = 0;
		String method = "get";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);

			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the draft physical interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if (code == 304) {
			throw new IoTFCReSTException(code, "The state of the physical interface has not been modified (response to a conditional GET)");
		} else if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if (code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A physical interface with the specified id does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throw new IoTFCReSTException(code, "", jsonResponse);
	}
	
	/**
	 * Updates the physical interface	 *  
	 * @param physicalInterfaceId String which contains draft logical interface id to be retrieved
	 * 
	 * @param draftPhysicalInterface String which contains the LogicalInterface in JSON format
     *
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/put_device_types_typeId_devices_deviceId_location">link</a>
	 * for more information about the JSON format</p>.
	 *   
	 * @return A JSON response containing the status of the update operation.
	 * 
	 * @throws IoTFCReSTException Failure in updting the device location
	 */
	public JsonObject updateDraftPhysicalInterface(String physicalInterfaceId, String draftPhysicalInterface) throws IoTFCReSTException {
		final String METHOD = "updateDraftPhysicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/physicalinterfaces/").
		   append(physicalInterfaceId);
		
		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		String method = "put";
		try {
			response = connect(method, sb.toString(), draftPhysicalInterface, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200) {
				if(code == 200) {
					return jsonResponse.getAsJsonObject();
				}
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in updating the draft physical interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (Invalid resource id specified in the path, no body, invalid JSON, unexpected key, bad value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "A physical interface with the specified id does not exist", jsonResponse);
		} else if(code == 412) {
			throw new IoTFCReSTException(code, "The state of the physical interface has been modified since the "
					+ "client retrieved its representation (response to a conditional PUT)", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	
	/**
	 * Adds event to physical interface.
	 * 
	 * @param draftPhysicalInterfaceId String containing the draft logical interface.
	 * 
	 * @param event String in the form of JSON containing the event.
	 * 
	 * @return If successful, JsonObject response from Watson IoT Platform.
	 * 
	 * @throws IoTFCReSTException if failed.
	 * 
	 * @see IoTFCReSTException
	 */
	public JsonObject addEventToPhysicalInterface(String draftPhysicalInterfaceId, String event) throws IoTFCReSTException {
		final String METHOD = "addDraftPhysicalInterface";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		int code = 0;
		String method = "post";
		try {
			StringBuilder sb = new StringBuilder("https://");
			sb.append(orgId).
			   append('.').
			   append(this.domain).append(BASIC_API_V0002_URL).
			   append("/draft/physicalinterfaces/").
			   append(draftPhysicalInterfaceId).append("/events/");
			response = connect(method, sb.toString(), event, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if (code == 201 || code == 400 || code == 401 || code == 403 || code == 500) {
				if (code == 201) {
					//Success
					return jsonResponse.getAsJsonObject();
				} else {
					String reason = null;
					switch (code) {
					case 400:
						reason = IoTFCReSTException.HTTP_ADD_PHYSICAL_INTERFACE_ERR_400;
						break;
					case 401:
						reason = IoTFCReSTException.HTTP_ADD_PHYSICAL_INTERFACE_ERR_401;
						break;
					case 403:
						reason = IoTFCReSTException.HTTP_ADD_PHYSICAL_INTERFACE_ERR_403;
						break;
					case 500:
						reason = IoTFCReSTException.HTTP_ADD_PHYSICAL_INTERFACE_ERR_500;
						break;
					}
					throw new IoTFCReSTException(method, sb.toString(), draftPhysicalInterfaceId, code, reason, jsonResponse);
				}
			} else {
				throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
			}
		} catch (IoTFCReSTException e) {
			throw e;
		} catch (Exception e) {
			// This includes JsonSyntaxException
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding event to the Draft Physical Interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}


	/**
	 * Get list of all events mapped to draft physical interface
	 * 
	 * @param draftPhysicalInterfaceId String containing the Draft Physical Interface Id.
	 * 
	 * @return JSON response containing list of events
	 *  
	 * @throws IoTFCReSTException Failure in retrieving events mapped to a Draft Physical Interface
	 */
	public JsonArray getEventsMappedToDraftPhysicalInterface(String draftPhysicalInterfaceId) throws IoTFCReSTException {
		
		final String METHOD = "getEventsMappedToDraftPhysicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/physicalinterfaces/").
		   append(draftPhysicalInterfaceId).
		   append("/events/");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);

			if(code == 200) {
				return jsonResponse.getAsJsonArray();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving list of events associated with draft physical interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A physical interface with the specified id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	
	/**
	 * Removes an event mapped to draft physical interface.
	 * 
	 * @param draftPhysicalInterfaceId String Draft Physical Interface from where an event needs to be deleted
	 * 
	 * @param eventId String to be deleted from IBM Watson IoT Platform
	 *   
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Physical_Interface/delete_physical_interface_Id">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean object containing the response of the deletion operation.
	 *  
	 * @throws IoTFCReSTException Failure in deleting the draft physical interface
	 */

	public boolean deleteEventMappingFromPhysicalInterface(String draftPhysicalInterfaceId, String eventId) throws IoTFCReSTException {
		final String METHOD = "deleteDraftPhysicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/physicalinterfaces/").
		   append(draftPhysicalInterfaceId).
		   append("/events/").
		   append(eventId);
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		String method = "delete";
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in removing the event from the draft Physical Interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A physical interface with the specified id does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return false;
	}
	
	
	/**
	 * Get list of all active physical interfaces
	 * 
	 * @param parameters list of query parameters that controls the output.
	 * 
	 * @return JSON response containing list of active physical interfaces
	 *  
	 * @throws IoTFCReSTException Failure in retrieving active physical interface request status
	 */
	public JsonObject getActivePhysicalInterfaces(List<NameValuePair> parameters) throws IoTFCReSTException {
		
		final String METHOD = "getActivePhysicalInterfaces";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/physicalinterfaces/");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving list of active physical interfaces "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Retrieve the active physical interface
	 *
	 * @param physicalInterfaceId String to be retrieved from IBM Watson IoT Platform
	 * 
	 * @return JSON response containing the active physical interface
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the active physical interface
	 */
	public JsonObject getActivePhysicalInterface(String physicalInterfaceId) throws IoTFCReSTException {
		final String METHOD = "getActivePhysicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/physicalinterfaces/").
		   append(physicalInterfaceId);
		
		int code = 0;
		String method = "get";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);

			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the active logical interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if (code == 304) {
			throw new IoTFCReSTException(code, "The state of the physical interface has not been modified (response to a conditional GET)");
		} else if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if (code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A physical interface with the specified id does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throw new IoTFCReSTException(code, "", jsonResponse);
	}


	/**
	 * Get list of all events mapped to active physical interface
	 * 
	 * @param physicalInterfaceId String containing the Active Physical Interface Id.
	 * 
	 * @return JSON response containing list of events
	 *  
	 * @throws IoTFCReSTException Failure in retrieving events mapped to an Active Physical Interface
	 */
	public JsonObject getEventsMappedToPhysicalInterface(String physicalInterfaceId) throws IoTFCReSTException {
		
		final String METHOD = "getEventsMappedToPhysicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/physicalinterfaces/").
		   append(physicalInterfaceId).
		   append("/events/");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving list of events associated with active physical interface "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A physical interface with the specified id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	

	/**
	 * Get the state for the device with the specified id
	 * 
	 * @param typeId String containing the Device Type Id.
	 * 
	 * @param deviceId String containing the Device Id.
	 * 
	 * @param logicalInterfaceId String containing the Logical Interface Id.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Devices/get_device_types_typeId_devices_deviceId_state_logicalInterfaceId">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSONObject response containing current state of the device with the specified id
	 *  
	 * @throws IoTFCReSTException Failure in retrieving current state of the device with the specified id
	 */
	public JsonObject getDeviceState(String typeId, String deviceId, String logicalInterfaceId) throws IoTFCReSTException {
		
		final String METHOD = "getDeviceState";
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
		   append("/devices/").
		   append(deviceId).
		   append("/state/").
		   append(logicalInterfaceId);
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving current state of the device with the specified id "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "An active device type, device or logical interface with the specified ids do not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Get list of all draft event types
	 * 
	 * @param parameters list of query parameters that controls the output.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Event_Types/get_draft_event_types">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSONObject response containing list of all draft event types
	 *  
	 * @throws IoTFCReSTException Failure in retrieving draft event types
	 */
	public JsonObject getDraftEventTypes(List<NameValuePair> parameters) throws IoTFCReSTException {
		
		final String METHOD = "getDraftEventTypes";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/event/types");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving list of draft event types "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Adds draft event type for an organization in Watson IoT Platform.
	 * 
	 * @param draftEventType String in the form of JSON containing the event.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Event_Types/post_draft_event_types">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSONObject If successful JsonObject response from Watson IoT Platform.
	 * 
	 * @throws IoTFCReSTException if failed.
	 * 
	 * @see IoTFCReSTException
	 */
	public JsonObject addDraftEventType(String draftEventType) throws IoTFCReSTException {
		final String METHOD = "addDraftEventType";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		int code = 0;
		String method = "post";
		try {
			StringBuilder sb = new StringBuilder("https://");
			sb.append(orgId).
			   append('.').
			   append(this.domain).append(BASIC_API_V0002_URL).
			   append("/draft/event/types");
			response = connect(method, sb.toString(), draftEventType, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if (code == 201 || code == 400 || code == 401 || code == 403 || code == 500) {
				if (code == 201) {
					//Success
					return jsonResponse.getAsJsonObject();
				} else {
					String reason = null;
					switch (code) {
					case 400:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_EVENT_TYPE_ERR_400;
						break;
					case 401:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_EVENT_TYPE_ERR_401;
						break;
					case 403:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_EVENT_TYPE_ERR_403;
						break;
					case 500:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_EVENT_TYPE_ERR_500;
						break;
					}
					throw new IoTFCReSTException(method, sb.toString(), draftEventType, code, reason, jsonResponse);
				}
			} else {
				throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
			}
		} catch (IoTFCReSTException e) {
			throw e;
		} catch (Exception e) {
			// This includes JsonSyntaxException
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding draft event type to an organization in Watson IoT Platform "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}
	
	
	/**
	 * Removes a draft event type created in an organization in Watson IoT Platform.
	 * 
	 * @param eventTypeId String to be deleted from IBM Watson IoT Platform
	 *   
	 * <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Event_Types/delete_draft_event_types_eventTypeId">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean object containing the response of the deletion operation.
	 *  
	 * @throws IoTFCReSTException Failure in deleting the draft event type
	 */

	public boolean deleteDraftEventType(String eventTypeId) throws IoTFCReSTException {
		final String METHOD = "deleteDraftEventType";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/event/types/").
		   append(eventTypeId);
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		String method = "delete";
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in removing the event from an organization in atson IoT Platform "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A physical interface with the specified id does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return false;
	}
	
	
	/**
	 * Get draft event Type
	 * 
	 * @param eventTypeId String containing the Event Type Id.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Event_Types/get_draft_event_types_eventTypeId">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSON response containing the draft event type with the specified id
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the draft event type with the specified id
	 */
	public JsonObject getDraftEventType(String eventTypeId) throws IoTFCReSTException {
		
		final String METHOD = "getDraftEventType";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/event/types/").
		   append(eventTypeId);
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the draft event type with the specified id "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 304) {
			throw new IoTFCReSTException(code, "The state of the event type has not been modified (response to a conditional GET)");
		} if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "An event type with the specified id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Updates draft event type	 
	 *   
	 * @param eventTypeId String which contains draft event type id to be retrieved
	 * 
	 * @param draftEventType String which contains the Event Type in JSON format
     *
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Event_Types/put_draft_event_types_eventTypeId">link</a>
	 * for more information about the JSON format</p>.
	 *   
	 * @return A JSON response containing the status of the update operation.
	 * 
	 * @throws IoTFCReSTException Failure in updating the device location
	 */
	public JsonObject updateDraftEventType(String eventTypeId, String draftEventType) throws IoTFCReSTException {
		final String METHOD = "updateDraftEventType";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/event/types/").
		   append(eventTypeId);
		
		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		String method = "put";
		try {
			response = connect(method, sb.toString(), draftEventType, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200 || code == 409) {
				if(code == 200) {
					return jsonResponse.getAsJsonObject();
				}
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in updating the draft event type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (Invalid resource id specified in the path, no body, invalid JSON, unexpected key, bad value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "An Event Type with the specified id does not exist", jsonResponse);
		} else if(code == 412) {
			throw new IoTFCReSTException(code, "The state of the event type has been modified since the client retrieved "
					+ "its representation (response to a conditional PUT)", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
		
	/**
	 * Get list of all active event types
	 * 
	 * @param parameters list of query parameters that controls the output.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/get_device_types_typeId_physicalinterface">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSONObject response containing list of all active event types
	 *  
	 * @throws IoTFCReSTException Failure in retrieving active event types
	 */
	public JsonObject getAllActiveEventTypes(List<NameValuePair> parameters) throws IoTFCReSTException {
		
		final String METHOD = "getAllActiveEventTypes";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/event/types/");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving list of active event types "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}


	/**
	 * Get active event Type
	 * 
	 * @param eventTypeId String containing the active Event Type Id.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Event_Types/get_event_types_eventTypeId">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSON response containing the active event type with the specified id
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the active event type with the specified id
	 */
	public JsonObject getActiveEventType(String eventTypeId) throws IoTFCReSTException {
		
		final String METHOD = "getActiveEventType";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/event/types/").
		   append(eventTypeId);
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the active event type with the specified id "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 304) {
			throw new IoTFCReSTException(code, "The state of the event type has not been modified (response to a conditional GET)");
		} if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "An event type with the specified id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
		
	/**
	 * Get list of all device types
	 * 
	 * @param parameters list of query parameters that controls the output.
	 * 
	 * @return JSON response containing list of all device types
	 *  
	 * @throws IoTFCReSTException Failure in retrieving list of all device types
	 */
	public JsonObject getDeviceTypes(List<NameValuePair> parameters) throws IoTFCReSTException {
		
		final String METHOD = "getDeviceTypes";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/device/types/");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving list of all device types "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Perform operation against Device Type
	 *  
	 * @param typeId String which contains the device type id
	 * 
	 * @param deviceTypeOperation String contains the operation in JSON format
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/patch_device_types_typeId">link</a>
	 * for more information about the JSON format</p>.
	 *   
	 * @return A JSON response containing the status of the patch operation.
	 * 
	 * @throws IoTFCReSTException Failure in updating the device type operation
	 */
	public JsonObject performOperationAgainstDeviceType(String typeId, String deviceTypeOperation) throws IoTFCReSTException {
		final String METHOD = "performOperationAgainstDeviceType";
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
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		String method = "patch";
		try {
			response = connect(method, sb.toString(), deviceTypeOperation, null);
			System.out.println("method = " + method + " URL = " + sb.toString() + " payload = " + deviceTypeOperation);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200 || code == 202) {
				if(code == 200 || code == 202) {
					return jsonResponse.getAsJsonObject();
				}
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in updating the device type operation "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (Invalid resource id specified in the path, no body, invalid JSON, unexpected key, bad value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The device type does not exist", jsonResponse);
		} else if(code == 409) {
			throw new IoTFCReSTException(code, "The deactivate operation failed because there is no active configuration associated with the device type", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	

	/**
	 * Get active logical interfaces associated with a device type
	 * 
	 * @param typeId String containing the device Type Id.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/get_device_types_typeId_logicalinterfaces">link</a>
	 * for more information about the JSON format</p>.
	 *   
	 * @return JSON response containing the list of active logical interfaces with the specified device type id
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the list of active logical interfaces with the specified device type id
	 */
	public JsonArray getActiveLogicalInterfacesForDeviceType(String typeId) throws IoTFCReSTException {
		
		final String METHOD = "getActiveLogicalInterfacesForDeviceType";
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
		   append("/logicalinterfaces");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);

			if(code == 200) {
				return jsonResponse.getAsJsonArray();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the list of active logical interfaces with the specified device type id "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A device type with the specified id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	

	/**
	 * Get the list of active property mappings for a device type
	 * 
	 * @param typeId String containing the device Type Id.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/get_device_types_typeId_mappings">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSONArray response containing the list of active property mappings for a device type
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the list of active property mappings for a device type
	 */
	public JsonArray getActivePropertyMappingsForDeviceType(String typeId) throws IoTFCReSTException {
		
		final String METHOD = "getActivePropertyMappingsForDeviceType";
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
		   append("/mappings");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);

			if(code == 200) {
				return jsonResponse.getAsJsonArray();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the list of active property mappings for a device type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A device type with the specified id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}


	/**
	 * Get the list of active property mappings for a logical interface of a given device type
	 * 
	 * @param typeId String containing the device Type Id.
	 * 
	 * @param logicalInterfaceId String containing the logical interface Id
	 *  
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/get_device_types_typeId_mappings_logicalInterfaceId">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSONObject response containing the list of active property mappings for a logical interface of a given device type
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the list of active property mappings for a logical interface of a given device type
	 */
	public JsonObject getActivePropertyMappingsForLogicalInterfaceOfDeviceType(String typeId, String logicalInterfaceId) throws IoTFCReSTException {
		
		final String METHOD = "getActivePropertyMappingsForLogicalInterfaceOfDeviceType";
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
		   append("/mappings/").
		   append(logicalInterfaceId);
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the list of active property mappings for a logical interface of a given device type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A device type with the specified id does not exist or a property mapping for the specified logical interface id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Get active physical interface for a given device type
	 * 
	 * @param typeId String containing the device Type Id.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/get_device_types_typeId_physicalinterface">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSONObject response containing active physical interface for a given device type
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the active physical interface for a given device type
	 */
	public JsonObject getActivePhysicalInterfaceForDeviceType(String typeId) throws IoTFCReSTException {
		
		final String METHOD = "getActivePhysicalInterfaceForDeviceType";
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
		   append("/physicalinterface");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the active physical interface for a given device type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A device type with the specified id does not exist or has no active physical interface associated with it", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Get list of all device types associated with a logical of physical interface
	 * 
	 * @param parameters list of query parameters that controls the output.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/get_draft_device_types">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSONObject response containing list of all device types
	 *  
	 * @throws IoTFCReSTException Failure in retrieving list of all device types
	 */
	public JsonObject getDeviceTypesAssociatedWithLogicalOrPhysicalInterface(List<NameValuePair> parameters) throws IoTFCReSTException {
		
		final String METHOD = "getDeviceTypesAssociatedWithLogicalOrPhysicalInterface";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/device/types");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving list of all device types "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Perform operation against Draft Device Type
	 *  
	 * @param typeId String which contains the draft device type id
	 * 
	 * @param deviceTypeOperation String contains the operation in JSON format
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/patch_draft_device_types_typeId">link</a>
	 * for more information about the JSON format</p>.
	 *   
	 * @return A JSONObject response containing the status of the patch operation.
	 * 
	 * @throws IoTFCReSTException Failure in updating the draft device type operation
	 */
	public JsonObject performOperationAgainstDraftDeviceType(String typeId, String deviceTypeOperation) throws IoTFCReSTException {
		final String METHOD = "performOperationAgainstDraftDeviceType";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/device/types/").
		   append(typeId);
		
		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		String method = "patch";
		try {
			response = connect(method, sb.toString(), deviceTypeOperation, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200 || code == 202) {
				if(code == 200 || code == 202) {
					return jsonResponse.getAsJsonObject();
				}
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in updating the draft device type operation "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (Invalid resource id specified in the path, no body, invalid JSON, unexpected key, bad value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "The device type does not exist", jsonResponse);
		} else if(code == 409) {
			throw new IoTFCReSTException(code, "The activate-configuration operation failed because the Information Management metadata associated with the device type is invalid ");
		} else if (code == 500) {		
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}


	/**
	 * Get draft logical interfaces associated with a device type
	 * 
	 * @param typeId String containing the device Type Id.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/get_draft_device_types_typeId_logicalinterfaces">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JsonArray response containing the list of draft logical interfaces associated with the specified device type id
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the list of draft logical interfaces associated with the specified device type id
	 */
	public JsonArray getDraftLogicalInterfacesAssociatedWithDeviceType(String typeId) throws IoTFCReSTException {
		
		final String METHOD = "getDraftLogicalInterfacesAssociatedWithDeviceType";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/device/types/").
		   append(typeId).
		   append("/logicalinterfaces");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);

			if(code == 200) {
				return jsonResponse.getAsJsonArray();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the list of draft logical interfaces associated with the specified device type id "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A device type with the specified id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	
	/**
	 * Adds draft logical interface to a device type
	 * 
	 * @param typeId String containing the device type Id
	 * 
	 * @param draftLogicalInterface String in the form of JSON containing the event.
     *
     * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/post_draft_device_types_typeId_logicalinterfaces">link</a>
     * for more information about the JSON format</p>.
     *  
	 * @return JsonObject response from Watson IoT Platform.
	 * 
	 * @throws IoTFCReSTException if failed.
	 * 
	 * @see IoTFCReSTException
	 */
	public JsonObject associateDraftLogicalInterfaceToDeviceType(String typeId, String draftLogicalInterface) throws IoTFCReSTException {
		final String METHOD = "associateDraftLogicalInterfaceToDeviceType";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		int code = 0;
		String method = "post";
		try {
			StringBuilder sb = new StringBuilder("https://");
			sb.append(orgId).
			   append('.').
			   append(this.domain).append(BASIC_API_V0002_URL).
			   append("/draft/device/types/").
			   append(typeId).
			   append("/logicalinterfaces");
			response = connect(method, sb.toString(), draftLogicalInterface, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if (code == 201 || code == 400 || code == 401 || code == 403 || code == 404 || code == 500) {
				if (code == 201) {
					//Success
					return jsonResponse.getAsJsonObject();
				} else {
					String reason = null;
					switch (code) {
					case 400:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_LOGICAL_INTERFACE_ERR_400;
						break;
					case 401:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_LOGICAL_INTERFACE_ERR_401;
						break;
					case 403:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_LOGICAL_INTERFACE_ERR_403;
						break;
					case 404:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_LOGICAL_INTERFACE_ERR_404;
						break;
					case 500:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_LOGICAL_INTERFACE_ERR_500;
						break;
					}
					throw new IoTFCReSTException(method, sb.toString(), draftLogicalInterface, code, reason, jsonResponse);
				}
			} else {
				throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
			}
		} catch (IoTFCReSTException e) {
			throw e;
		} catch (Exception e) {
			// This includes JsonSyntaxException
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding draft logical interface to a device type in Watson IoT Platform "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}
	
	
	/**
	 * Removes a draft logical interface from a device type in Watson IoT Platform.
	 * 
	 * @param typeId String device type from which draft logical interface needs to be deleted from
	 * 
	 * @param draftLogicalInterfaceId String draft logical interface
	 *   
	 * <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/delete_draft_device_types_typeId_logicalinterfaces_logicalInterfaceId">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean object containing the response of the deletion operation.
	 *  
	 * @throws IoTFCReSTException Failure in deleting the draft logical interface from device type
	 */

	public boolean dissociateDraftLogicalInterfaceFromDeviceType(String typeId, String draftLogicalInterfaceId) throws IoTFCReSTException {
		final String METHOD = "dissociateDraftLogicalInterfaceFromDeviceType";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/device/types/").
		   append(typeId).
		   append("/logicalinterfaces/").
		   append(draftLogicalInterfaceId);
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		String method = "delete";
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in removing draft logical interface from a device type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A device type with the specified id does not exist or the draft logical interface with specified id is not associated with the device type", jsonResponse);
		} else if (code == 409) {
			throw new IoTFCReSTException(code, "The draft logical interface with the specified id is currently being referenced by the property mappings on the device type", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return false;
	}
	

	/**
	 * Get list of draft property mappings for a device type
	 * 
	 * @param typeId String containing the device Type Id.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/get_draft_device_types_typeId_mappings_logicalInterfaceId">link</a>
	 * for more information about the JSON format</p>.
	 *  
	 * @return JSONArray response containing the list of draft property mappings associated with the specified device type id
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the list of draft property mappings associated with the specified device type id
	 */
	public JsonArray getDraftPropertyMappingsForDeviceType(String typeId) throws IoTFCReSTException {
		
		final String METHOD = "getDraftPropertyMappingsForDeviceType";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/device/types/").
		   append(typeId).
		   append("/mappings");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);

			if(code == 200) {
				System.out.println("Content = " + jsonResponse);
				return jsonResponse.getAsJsonArray();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the list of draft property mappings associated with the specified device type id "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A device type with the specified id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	
	/**
	 * Adds draft property mappings to a device type
	 * 
	 * @param typeId String containing the device type Id
	 * 
	 * @param draftDeviceTypePropertyMappings String in the form of JSON containing the event.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/post_draft_device_types_typeId_mappings">link</a>
	 * for more information about the JSON format</p>.
	 *  
	 * @return If successful, JsonObject response from Watson IoT Platform.
	 * 
	 * @throws IoTFCReSTException if failed.
	 * 
	 * @see IoTFCReSTException
	 */
	public JsonObject addDraftPropertyMappingsToDeviceType(String typeId, String draftDeviceTypePropertyMappings) throws IoTFCReSTException {
		final String METHOD = "addDraftPropertyMappingsToDeviceType";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		int code = 0;
		String method = "post";
		try {
			StringBuilder sb = new StringBuilder("https://");
			sb.append(orgId).
			   append('.').
			   append(this.domain).append(BASIC_API_V0002_URL).
			   append("/draft/device/types/").
			   append(typeId).
			   append("/mappings");
			response = connect(method, sb.toString(), draftDeviceTypePropertyMappings, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if (code == 201 || code == 400 || code == 401 || code == 403 || code == 404 || code == 500) {
				if (code == 201) {
					//Success
					return jsonResponse.getAsJsonObject();
				} else {
					String reason = null;
					switch (code) {
					case 400:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_PROPERTY_MAPPINGS_ERR_400;
						break;
					case 401:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_PROPERTY_MAPPINGS_ERR_401;
						break;
					case 403:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_PROPERTY_MAPPINGS_ERR_403;
						break;
					case 404:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_PROPERTY_MAPPINGS_ERR_404;
						break;
					case 500:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_PROPERTY_MAPPINGS_ERR_500;
						break;
					}
					throw new IoTFCReSTException(method, sb.toString(), draftDeviceTypePropertyMappings, code, reason, jsonResponse);
				}
			} else {
				throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
			}
		} catch (IoTFCReSTException e) {
			throw e;
		} catch (Exception e) {
			// This includes JsonSyntaxException
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding draft property mappings to a device type in Watson IoT Platform "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}
	
	
	/**
	 * Removes a draft property mappings for specific logical interface for a device type in Watson IoT Platform.
	 * 
	 * @param typeId String device type from which draft property mappings needs to be deleted from
	 * 
	 * @param draftLogicalInterfaceId String draft logical interface
	 *   
	 * <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/delete_draft_device_types_typeId_mappings_logicalInterfaceId">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean object containing the response of the deletion operation.
	 *  
	 * @throws IoTFCReSTException Failure in deleting the draft property mappings for a specific logical interface for a device type
	 */

	public boolean deleteDraftPropertyMappings(String typeId, String draftLogicalInterfaceId) throws IoTFCReSTException {
		final String METHOD = "deleteDraftPropertyMappings";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/device/types/").
		   append(typeId).
		   append("/mappings/").
		   append(draftLogicalInterfaceId);
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		String method = "delete";
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure deleting the draft property mappings for a specific logical interface for a device type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A device type with the specified id does not exist or property the draft logical interface with specified id is not associated with the device type", jsonResponse);
		} else if (code == 409) {
			throw new IoTFCReSTException(code, "The draft logical interface with the specified id is currently being referenced by the property mappings for the specified logical interface does not exist", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return false;
	}

	
	/**
	 * Get list of draft property mappings for a specific logical interface of a device type
	 * 
	 * @param typeId String containing the device Type Id.
	 * 
	 * @param logicalInterfaceId String containing the Logical Interface Id
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/get_draft_device_types_typeId_mappings_logicalInterfaceId">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSONObject response containing the list of draft property mappings for a specific logical interface id for a device type
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the list of draft property mappings for a specific logical interface id for a device type
	 */
	public JsonObject getDraftPropertyMappingsForSpecificLogicalInterfaceDeviceType(String typeId, String logicalInterfaceId) throws IoTFCReSTException {
		
		final String METHOD = "getDraftPropertyMappingsForSpecificLogicalInterfaceDeviceType";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/device/types/").
		   append(typeId).
		   append("/mappings/").
		   append(logicalInterfaceId);
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the list of draft property mappings for a specific logical interface id for a device type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A device type with the specified id does not exist or a property mapping for the specified logical interface id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Update the draft property mappings for a specific logical interface for the device type	 
	 *   
	 * @param typeId String which contains device type id to be retrieved
	 * 
	 * @param logicalInterfaceId String which contains the Logical Interface Id
	 * 
	 * @param deviceTypePropertyMappings String which contains the device type property mappings in JSON format
     *
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/put_draft_device_types_typeId_mappings_logicalInterfaceId">link</a>
	 * for more information about the JSON format</p>.
	 *   
	 * @return A JSON response containing the status of the update operation.
	 * 
	 * @throws IoTFCReSTException Failure in updating the draft property mappings for a specific logical interface for the device type
	 */
	public JsonObject updateDraftPropertyMappingsForSpecificLogicalInterfaceOfDeviceType(String typeId, String logicalInterfaceId, 
			String deviceTypePropertyMappings) throws IoTFCReSTException {
		final String METHOD = "updateDraftPropertyMappingsForSpecificLogicalInterfaceOfDeviceType";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/device/types/").
		   append(typeId).
		   append("/mappings/").
		   append(logicalInterfaceId);
		
		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		String method = "put";
		try {
			response = connect(method, sb.toString(), deviceTypePropertyMappings, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200 || code == 409) {
				if(code == 200) {
					return jsonResponse.getAsJsonObject();
				}
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in updating the draft property mappings for a specific logical interface for the device type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (Invalid resource id specified in the path, no body, invalid JSON, unexpected key, bad value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "A device type with the specified id does not exist or a property mapping for the specified logical interface id does not exist", jsonResponse);
		} else if (code == 500) {		
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
		
	
	/**
	 * Associates a draft physical interface with the specified device type
	 * 
	 * @param typeId String containing the device type Id
	 * 
	 * @param draftPhysicalInterface String containing the draft physical interface in JSON format
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/post_draft_device_types_typeId_physicalinterface">link</a>
	 * 
	 * @return If successful, JsonObject response from Watson IoT Platform.
	 * 
	 * @throws IoTFCReSTException Failure in associating a draft physical interface with the specified device type
	 * 
	 * @see IoTFCReSTException
	 */
	public JsonObject associateDraftPhysicalInterfaceWithDeviceType(String typeId, String draftPhysicalInterface) throws IoTFCReSTException {
		final String METHOD = "associateDraftPhysicalInterfaceWithDeviceType";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		int code = 0;
		String method = "post";
		try {
			StringBuilder sb = new StringBuilder("https://");
			sb.append(orgId).
			   append('.').
			   append(this.domain).append(BASIC_API_V0002_URL).
			   append("/draft/device/types/").
			   append(typeId).
			   append("/physicalinterface");
			response = connect(method, sb.toString(), draftPhysicalInterface, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if (code == 201 || code == 400 || code == 401 || code == 403 || code == 404 || code == 500) {
				if (code == 201) {
					//Success
					return jsonResponse.getAsJsonObject();
				} else {
					String reason = null;
					switch (code) {
					case 400:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_PHYSICAL_INTERFACE_ERR_400;
						break;
					case 401:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_PHYSICAL_INTERFACE_ERR_401;
						break;
					case 403:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_PHYSICAL_INTERFACE_ERR_403;
						break;
					case 404:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_PHYSICAL_INTERFACE_ERR_404;
						break;
					case 500:
						reason = IoTFCReSTException.HTTP_ADD_DRAFT_PHYSICAL_INTERFACE_ERR_500;
						break;
					}
					throw new IoTFCReSTException(method, sb.toString(), draftPhysicalInterface, code, reason, jsonResponse);
				}
			} else {
				throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
			}
		} catch (IoTFCReSTException e) {
			throw e;
		} catch (Exception e) {
			// This includes JsonSyntaxException
			IoTFCReSTException ex = new IoTFCReSTException("Failure in adding draft property mappings to a device type in Watson IoT Platform "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}
	
	
	/**
	 * Dissociates a draft physical interface from device type.
	 * 
	 * @param typeId String device type from which draft physical interface needs to be dissociated from
	 * 
	 * <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/delete_draft_device_types_typeId_physicalinterface">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean object containing the response of the deletion operation.
	 *  
	 * @throws IoTFCReSTException Failure in dissociating a draft physical interface from a device type
	 */

	public boolean dissociateDraftPhysicalInterfaceFromDeviceType(String typeId) throws IoTFCReSTException {
		final String METHOD = "dissociateDraftPhysicalInterfaceFromDeviceType";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/device/types/").
		   append(typeId).
		   append("/physicalinterface");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		String method = "delete";
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in dissociating a draft physical interface from a device type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A device type with the specified id does not exist or has no draft physical interface associated with it", jsonResponse);
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return false;
	}

	
	/**
	 * Get draft physical interface associated with device type
	 * 
	 * @param typeId String containing the device Type Id.
	 * 
	 * <p> Refer to the <a href="https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/state-mgmt.html#!/Device_Types/get_draft_device_types_typeId_physicalinterface">link</a>
	 * for more information about the JSON format</p>.
	 * 
	 * @return JSON response containing draft physical interface associated with device type
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the draft physical interface associated with device type
	 */
	public JsonObject getDraftPhysicalInterfaceAssociatedWithDeviceType(String typeId) throws IoTFCReSTException {
		
		final String METHOD = "getDraftPhysicalInterfaceAssociatedWithDeviceType";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/device/types/").
		   append(typeId).
		   append("/physicalinterface");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the draft physical interface associated with device type "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A device type with the specified id does not exist or has no physical interface associated with it", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Query draft schema definitions
	 * 
	 * @param parameters list of query parameters that controls the output.
	 * 
	 * @return JSON response containing list of all draft schemas
	 *  
	 * @throws IoTFCReSTException Failure in retrieving list of all draft schemas
	 */
	public JsonObject getDraftSchemas(List<NameValuePair> parameters) throws IoTFCReSTException {
		
		final String METHOD = "getDraftSchemas";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/schemas/");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving list of all draft schemas "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	
	/**
	 * Create a draft schema definition
	 * 
	 * @param schemaFile File containing the schema file
	 * 	  
	 * @param schemaName String containing the schema name
	 * 	  
	 * @param schemaDescription String containing the description of schema
	 * 
	 * @param schemaType String containing the schema type (like for e.g. json-schema)
	 * 
	 * @return If successful, JsonObject response from Watson IoT Platform.
	 * 
	 * @throws IoTFCReSTException Failure in creating a draft schema definition
	 */
	public JsonObject addDraftSchemaDefinition(File schemaFile, String schemaName, String schemaDescription, String schemaType) throws IoTFCReSTException {
		final String METHOD = "addDraftSchemaDefinition";
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		int code = 0;
		String method = "post";
		try {
			StringBuilder sb = new StringBuilder("https://");
			sb.append(orgId).
			   append('.').
			   append(this.domain).append(BASIC_API_V0002_URL).
			   append("/draft/schemas/");
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			
			builder.addBinaryBody(
				"schemaFile",
			    new FileInputStream(schemaFile),
			    org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM,
			    schemaFile.getName()
			);
			
			builder.addTextBody("description", schemaDescription);
			builder.addTextBody("name", schemaName);

			URIBuilder uri = new URIBuilder(sb.toString());
			
			HttpPost post = new HttpPost(uri.build());

			byte[] encoding = Base64.encodeBase64(new String(authKey + ":" + authToken).getBytes());
			String encodedString = new String(encoding);
			
			post.setHeader("Authorization", "Basic " + encodedString);
			
			HttpClient client = HttpClientBuilder.create().useSystemProperties().setSslcontext(sslContext).build();
			HttpEntity multipart = builder.build();
			post.setEntity(multipart);
			
			response = client.execute(post);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if (code == 201 || code == 400 || code == 401 || code == 403 || code == 404 || code == 500) {
				if (code == 201) {
					//Success
					return jsonResponse.getAsJsonObject();
				} else {
					String reason = null;
					switch (code) {
					case 400:
						reason = IoTFCReSTException.HTTP_ADD_SCHEMA_DEFINITION_ERR_400;
						break;
					case 401:
						reason = IoTFCReSTException.HTTP_ADD_SCHEMA_DEFINITION_ERR_401;
						break;
					case 403:
						reason = IoTFCReSTException.HTTP_ADD_SCHEMA_DEFINITION_ERR_403;
						break;
					case 500:
						reason = IoTFCReSTException.HTTP_ADD_SCHEMA_DEFINITION_ERR_500;
						break;
					}
					throw new IoTFCReSTException(method, sb.toString(), null, code, reason, jsonResponse);
				}
			} else {
				throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
			}
		} catch (IoTFCReSTException e) {
			throw e;
		} catch (Exception e) {
			// This includes JsonSyntaxException
			IoTFCReSTException ex = new IoTFCReSTException("Failure in creating a draft schema definition "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	
	/**
	 * Deletes a draft schema definition
	 * 
	 * @param draftSchemaId String schema Id which needs to be deleted
	 * 
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Physical_Interface/delete_physical_interface_Id">link</a> 
	 * for more information about the schema to be used
	 * 
	 * @return boolean object containing the response of the deletion operation.
	 *  
	 * @throws IoTFCReSTException Failure in deleting a draft schema definition
	 */

	public boolean deleteDraftSchemaDefinition(String draftSchemaId) throws IoTFCReSTException {
		final String METHOD = "deleteDraftSchemaDefinition";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/schemas/").
		   append(draftSchemaId);
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		String method = "delete";
		try {
			response = connect(method, sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 204) {
				return true;
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting a draft schema definition "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A schema definition with the specified id does not exist", jsonResponse);
		} else if (code == 409) {
			throw new IoTFCReSTException(code, "The schema definition with the specified id is currently being referenced by another object");
		} else if (code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return false;
	}


	
	/**
	 * Get draft schema definition metadata
	 * 
	 * @param draftSchemaId String containing the device Type Id.
	 * 
	 * @return JSON response containing draft schema Id
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the draft schema definition metadata
	 */
	public JsonObject getDraftSchemaDefinitionMetadata(String draftSchemaId) throws IoTFCReSTException {
		
		final String METHOD = "getDraftSchemaDefinitionMetadata";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/schemas/").
		   append(draftSchemaId);
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the draft schema definition metadata "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 304) {
			throw new IoTFCReSTException(code, "The state of the schema definition has not been modified (response to a conditional GET).", jsonResponse);
		} else if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A schema definition with the specified id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Update draft schema definition metadata	 
	 *   
	 * @param schemaId String which contains draft schema definition metadata which needs to be modified
	 * 
	 * @param schemaDefinition String which contains the draft schema definition metadata to be added
     *
	 * <p> Refer to the
	 * <a href="https://docs.internetofthings.ibmcloud.com/swagger/v0002.html#!/Devices/put_device_types_typeId_devices_deviceId_location">link</a>
	 * for more information about the JSON format</p>.
	 *   
	 * @return A JSON response containing the status of the update operation.
	 * 
	 * @throws IoTFCReSTException Failure in updating the draft schema definition metadata
	 */
	public JsonObject updateDraftSchemaDefinitionMetadata(String schemaId, String schemaDefinition) throws IoTFCReSTException {
		final String METHOD = "updateDraftSchemaDefinitionMetadata";
		/**
		 * Form the url based on this swagger documentation
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/schemas/").
		   append(schemaId);
		
		int code = 0;
		JsonElement jsonResponse = null;
		HttpResponse response = null;
		String method = "put";
		try {
			response = connect(method, sb.toString(), schemaDefinition, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			if(code == 200 || code == 409) {
				if(code == 200) {
					return jsonResponse.getAsJsonObject();
				}
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in updating the draft schema definition metadata "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		
		if(code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (Invalid resource id specified in the path, no body, invalid JSON, unexpected key, bad value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if(code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 404) {
			throw new IoTFCReSTException(code, "A schema definition with the specified id does not exist", jsonResponse);
		} else if(code == 412) {
			throw new IoTFCReSTException(code, "The state of the schema definition has been modified since the client retrieved its representation (response to a conditional PUT)");
		} else if (code == 500) {		
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Get the contents of the draft schema definition file
	 * 
	 * @param draftSchemaId String containing the device Type Id.
	 * 
	 * @return JSON response containing contents of schema definition file
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the contents of the draft schema definition file
	 */
	public JsonObject getDraftSchemaDefinitionContents(String draftSchemaId) throws IoTFCReSTException {
		
		final String METHOD = "getDraftSchemaDefinitionContents";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/draft/schemas/").
		   append(draftSchemaId).
		   append("/content/");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			String result = this.readContent(response, METHOD);
			jsonResponse = new JsonParser().parse(result);
			System.out.println("Parsing successful.....");
			if(code == 200) {
				return jsonResponse.getAsJsonObject();
			}
		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the contents of the draft schema definition file "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A schema definition with the specified id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Query active schema definitions
	 * 
	 * @param parameters list of query parameters that controls the output.
	 * 
	 * @return JSON response containing list of all active schemas
	 *  
	 * @throws IoTFCReSTException Failure in retrieving list of all active schemas
	 */
	public JsonObject getActiveSchemas(List<NameValuePair> parameters) throws IoTFCReSTException {
		
		final String METHOD = "getActiveSchemas";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/schemas/");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving list of all active schemas "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid query parameter, invalid query parameter value)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Get active schema definition metadata
	 * 
	 * @param activeSchemaId String containing the device Type Id.
	 * 
	 * @return JSON response containing active schema Id
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the active schema definition metadata
	 */
	public JsonObject getActiveSchemaDefinitionMetadata(String activeSchemaId) throws IoTFCReSTException {
		
		final String METHOD = "getActiveSchemaDefinitionMetadata";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/schemas/").
		   append(activeSchemaId);
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the active schema definition metadata "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if(code == 304) {
			throw new IoTFCReSTException(code, "The state of the schema definition has not been modified (response to a conditional GET).", jsonResponse);
		} else if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A schema definition with the specified id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}

	
	/**
	 * Get the contents of the active schema definition file
	 * 
	 * @param activeSchemaId String containing the device Type Id.
	 * 
	 * @return JSON response containing contents of schema definition file
	 *  
	 * @throws IoTFCReSTException Failure in retrieving the contents of the active schema definition file
	 */
	public JsonObject getActiveSchemaDefinitionContents(String activeSchemaId) throws IoTFCReSTException {
		
		final String METHOD = "getActiveSchemaDefinitionContents";
		/**
		 * Form the url based on this swagger documentation
		 * 
		 */
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/schemas/").
		   append(activeSchemaId).
		   append("/content/");
		
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
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the contents of the active schema definition file "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "A schema definition with the specified id does not exist", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	

	/**
	 * Get access control properties of a given device
	 * 
	 * See https://docs.internetofthings.ibmcloud.com/apis/swagger/v0002/security.html#!/Authorization_-_Device_Management/get_authorization_devices_deviceId
	 * @param deviceId Device ID e.g. d:orgid:deviceType:deviceID
	 * @param parameters to control the output such as _bookmark, can be null
	 * @return JsonObject JSON object describes access control properties
	 * @throws IoTFCReSTException Thrown if a HTTP error occurs
	 * @throws UnsupportedEncodingException Thrown if error occurs when parsing the deviceId
	 */
	public JsonObject getAccessControlProperties(String deviceId, List<NameValuePair> parameters) throws IoTFCReSTException, UnsupportedEncodingException {
		final String METHOD = "getAccessControlProperties";
		String sDeviceId = URLEncoder.encode(deviceId, "UTF-8");
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/authorization/devices/").
		   append(sDeviceId);
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;

		try {
			response = connect("get", sb.toString(), null, parameters);
			code = response.getStatusLine().getStatusCode();
			if (response != null) {
				String result = this.readContent(response, METHOD);
				if (result != null) {
					jsonResponse = new JsonParser().parse(result);
				}
			}
			if (code == 200) {
				return jsonResponse.getAsJsonObject();
			}

		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the access control properties for device ID (" + deviceId + ") Exception: "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "Invalid request", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
		
	}
	
	/**
	 * Get resource groups for a client ID
	 * 
	 * @param clientID e.g. g:abcdef:gwType1:gwDev1
	 * @return JsonArray e.g. ["gw_def_res_grp:abcdef:gwType1:gwDev1"],
	 */
	public JsonArray getResourceGroups(String clientID) {
		final String METHOD = "getResourceGroups";
		JsonArray groups = null;
		try {
			boolean done = false;
			String bookmark = null;
			while (!done) {
				List <NameValuePair> parameters = null;
				if (bookmark != null) {
					parameters = new ArrayList<NameValuePair>();
					NameValuePair nvpBookmark = new BasicNameValuePair("_bookmark", bookmark);
					parameters.add(nvpBookmark);
				}
				JsonObject jsonResult = getAccessControlProperties(clientID, parameters);
				if (jsonResult != null) {
					if (jsonResult.has("results")) {
						JsonArray devicesArray = jsonResult.get("results").getAsJsonArray();
						for (Iterator<JsonElement> iterator = devicesArray.iterator(); iterator.hasNext(); ) {
							JsonElement deviceElement = iterator.next();
							JsonObject jsonDevice = deviceElement.getAsJsonObject();
							if (jsonDevice.has("resourceGroups")) {
								JsonArray innerGroups = jsonDevice.get("resourceGroups").getAsJsonArray();
								if (groups == null) {
									groups = innerGroups;
								} else {
									groups.addAll(innerGroups);
								}
							}
						}
						
						if (jsonResult.has("bookmark")) {
							bookmark = jsonResult.get("bookmark").getAsString();
						}
					}
					
					if (bookmark == null) {
						done = true;
					}					
				} else {
					done = true;
				}
			}
		} catch (UnsupportedEncodingException e) {
			LoggerUtility.warn(CLASS_NAME, METHOD, e.getMessage());
			e.printStackTrace();
			return null;
		} catch (IoTFCReSTException e) {
			LoggerUtility.warn(CLASS_NAME, METHOD, "HTTP Status Code (" + e.getHttpCode() + ") " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		return groups;
		
	}
	
	/**
	 * Assign devices to a resource group 
	 * @param groupId Unique identifier (e.g. acc8b2a1-b979-4323-9d8f-7e2c4752d39a).
	 * @param devices Json Array of devices [{ "typeId": "string", "deviceId" : "string"}]
	 * @throws UnsupportedEncodingException Thrown if an error occurs when parsing groupId
	 * @throws IoTFCReSTException Thrown if an HTTP error occurs
	 */
	public void assignDevicesToResourceGroup(String groupId, JsonArray devices) throws UnsupportedEncodingException, IoTFCReSTException {
		
		final String METHOD = "assignDevicesToResourceGroup";
		String sGroupId = URLEncoder.encode(groupId, "UTF-8");
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/bulk/devices/").
		   append(sGroupId).
		   append("/add");
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;

		try {
			response = connect("put", sb.toString(), devices.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if (response != null) {
				String result = this.readContent(response, METHOD);
				if (result != null) {
					jsonResponse = new JsonParser().parse(result);
				}
			}
			if (code == 200) {
				return;
			}

		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving devices in resource group "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "Invalid request", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);	
	}
	
	/**
	 * Get devices in resource group
	 * 
	 * @param groupId Resource group ID
	 * @param bookmark Bookmark for next page
	 * @return JsonObject JSON object describes devices in the resource group
	 * @throws IoTFCReSTException Thrown if an HTTP error occurs
	 * @throws UnsupportedEncodingException Thrown if an error occurs when parsing group ID
	 */
	public JsonObject getDevicesInResourceGroup(String groupId, String bookmark) throws IoTFCReSTException, UnsupportedEncodingException {
		List<NameValuePair> queryParms = null;
		if (bookmark != null) {
			queryParms = new ArrayList<>();
			queryParms.add(new BasicNameValuePair("_bookmark", bookmark));
		}
		return getDevicesInResourceGroup(groupId, queryParms);
	}

	/**
	 * Get devices in resource group
	 * 
	 * @param groupId Resource group ID
	 * @param queryParameters Additional query parameter such as _bookmark
	 * @return JsonObject JSON object describes devices in the resource group
	 * @throws IoTFCReSTException Thrown if an HTTP error occurs
	 * @throws UnsupportedEncodingException Thrown if an error occurs when parsing group ID
	 */
	public JsonObject getDevicesInResourceGroup(String groupId, List<NameValuePair> queryParameters) throws IoTFCReSTException, UnsupportedEncodingException {
		final String METHOD = "getActiveSchemaDefinitionContents";
		String sGroupId = URLEncoder.encode(groupId, "UTF-8");
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/bulk/devices/").
		   append(sGroupId);
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;

		try {
			response = connect("get", sb.toString(), null, queryParameters);
			code = response.getStatusLine().getStatusCode();
			if (response != null) {
				String result = this.readContent(response, METHOD);
				if (result != null) {
					jsonResponse = new JsonParser().parse(result);
				}
			}
			if (code == 200) {
				return jsonResponse.getAsJsonObject();
			}

		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving devices in resource group "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "Invalid request", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
		
	}
	
	public JsonArray getAllAPIKeys() throws IoTFCReSTException {
		final String METHOD = "deleteAPIKey";
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/authorization/apikeys/");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;

		try {
			response = connect("get", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if (response != null) {
				String result = this.readContent(response, METHOD);
				if (result != null) {
					jsonResponse = new JsonParser().parse(result);
				}
			}
			if (code == 200) {
				return jsonResponse.getAsJsonArray();
			}

		} catch (Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in creating API Key roles "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "Invalid request", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
	}
	
	/**
	 * Delete API Key
	 * 
	 * @param apiKey API key to delete
	 * @return true for success, false for failure
	 * @throws IoTFCReSTException Thrown if an HTTP error occurs
	 * @throws UnsupportedEncodingException  Thrown if an error occurs when parsing apiKey
	 */
	public boolean deleteAPIKey(String apiKey) throws IoTFCReSTException, UnsupportedEncodingException {
		final String METHOD = "deleteAPIKey";
		String sAPIKey = URLEncoder.encode(apiKey, "UTF-8");
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/authorization/apikeys/").
		   append(sAPIKey);
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;

		try {
			response = connect("delete", sb.toString(), null, null);
			code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				return true;
			}

		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in creating API Key roles "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "Invalid request", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return false;

	}

	/**
	 * Create API Key
	 * 
	 * @param apiDetails JSON object describes API details
	 * @return JsonObject JSON Object which contains the API key and token
	 * @throws IoTFCReSTException Thrown if an HTTP error occurs
	 */
	public JsonObject createAPIKey(JsonObject apiDetails) throws IoTFCReSTException {
		final String METHOD = "createAPIKey";
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/authorization/apikeys");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;

		try {
			response = connect("post", sb.toString(), apiDetails.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if (response != null) {
				String result = this.readContent(response, METHOD);
				if (result != null) {
					jsonResponse = new JsonParser().parse(result);
				}
			}
			if (code == 200) {
				return jsonResponse.getAsJsonObject();
			}

		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in deleting API Key roles "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "Invalid request", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;

	}

	/**
	 * Get roles of the API key
	 * 
	 * @param apiKey API Key
	 * @param bookmark Issue the first request without specifying a bookmark,
	 *                 then take the bookmark returned in the response and provide it 
	 *                 on the request for the next page.
	 * @return JsonObject JSON Object describes roles of the API Key
	 * @throws IoTFCReSTException Thrown if an HTTP error occurs
	 * @throws UnsupportedEncodingException Thrown if an error occurs when parsing apiKey
	 */
	public JsonObject getGetAPIKeyRoles(String apiKey, String bookmark) throws IoTFCReSTException, UnsupportedEncodingException {
		List<NameValuePair> queryParms = null;
		if (bookmark != null) {
			queryParms = new ArrayList<>();
			queryParms.add(new BasicNameValuePair("_bookmark", bookmark));
		}
		return getGetAPIKeyRoles(apiKey, queryParms);
	}
	
	/**
	 * Get roles of this API Client's API Key
	 * 
	 * @param bookmark Issue the first request without specifying a bookmark,
	 *                 then take the bookmark returned in the response and provide it 
	 *                 on the request for the next page.
	 * @return JsonObject JSON Object describes roles of the API Key
	 * @throws IoTFCReSTException Thrown if an HTTP error occurs
	 * @throws UnsupportedEncodingException Thrown if an error occurs when parsing apiKey
	 */
	public JsonObject getGetAPIKeyRoles(String bookmark) throws IoTFCReSTException, UnsupportedEncodingException {
		List<NameValuePair> queryParms = null;
		if (bookmark != null) {
			queryParms = new ArrayList<>();
			queryParms.add(new BasicNameValuePair("_bookmark", bookmark));
		}
		return getGetAPIKeyRoles(this.authKey, queryParms);
	}
	
	/**
	 * Get API Key roles
	 * 
	 * @param apiKey API Key
	 * @param queryParameters such as a bookmark. 
	 *        Issue the first request without specifying a bookmark, 
	 *        then take the bookmark returned in the response and provide it 
	 *        on the request for the next page. 
	 * @return JsonObject JSON Object describes roles of the API Key
	 * @throws IoTFCReSTException Thrown if an HTTP error occurs
	 * @throws UnsupportedEncodingException Thrown if an error occurs when parsing apiKey
	 */
	public JsonObject getGetAPIKeyRoles(String apiKey, List<NameValuePair> queryParameters) throws IoTFCReSTException, UnsupportedEncodingException {
		final String METHOD = "getGetAPIKeyRoles";
		String sAPIKey = URLEncoder.encode(apiKey, "UTF-8");
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/authorization/apikeys/").
		   append(sAPIKey).
		   append("/roles");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		
		try {
			response = connect("get", sb.toString(), null, queryParameters);
			code = response.getStatusLine().getStatusCode();
			if (response != null) {
				String result = this.readContent(response, METHOD);
				if (result != null) {
					jsonResponse = new JsonParser().parse(result);
				}
			}
			if (code == 200) {
				return jsonResponse.getAsJsonObject();
			}

		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving API Key roles "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "Invalid request", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
		
	}

	/**
	 * Update roles of this API client's API key
	 * 
	 * @param listOfRoles List of roles to update
	 * @return JsonObject JSON object with updated roles
	 * @throws UnsupportedEncodingException Thrown if an error occurs when parsing API key
	 * @throws IoTFCReSTException Thrown if an HTTP error occurs
	 */
	public JsonObject updateAPIKeyRoles(JsonObject listOfRoles) throws UnsupportedEncodingException, IoTFCReSTException {
		return updateAPIKeyRoles(this.authKey, listOfRoles);
	}
	
	/**
	 * Update roles of the API key
	 * 
	 * @param apiKey API Key 
	 * @param listOfRoles List of roles to update
	 * @return JsonObject JSON object with updated roles
	 * @throws UnsupportedEncodingException Thrown if an error occurs when parsing API key
	 * @throws IoTFCReSTException Thrown if an HTTP error occurs
	 */
	public JsonObject updateAPIKeyRoles(String apiKey, JsonObject listOfRoles) throws UnsupportedEncodingException, IoTFCReSTException {
		final String METHOD = "updateAPIKeyRoles";
		String sAPIKey = URLEncoder.encode(apiKey, "UTF-8");
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/authorization/apikeys/").
		   append(sAPIKey).
		   append("/roles");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;
		
		try {
			response = connect("put", sb.toString(), listOfRoles.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if (response != null) {
				String result = this.readContent(response, METHOD);
				if (result != null) {
					jsonResponse = new JsonParser().parse(result);
				}
			}
			if (code == 200) {
				return jsonResponse.getAsJsonObject();
			}

		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving API Key roles "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "Invalid request", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
		
	}
	
	/**
	 * Assign or update a role or list of roles to the given device or gateway
	 * 
	 * @param deviceId Unique device identifier e.g. g:orgid:gwType:gwID
	 * @param jsonRoles Roles to be updated
	 * @return JSON object describes roles updated
	 * @throws IoTFCReSTException
	 * @throws UnsupportedEncodingException
	 */
	public JsonObject updateDeviceRoles(String deviceId, JsonObject jsonRoles) throws IoTFCReSTException, UnsupportedEncodingException {
		final String METHOD = "getAccessControlProperties";
		String sDeviceId = URLEncoder.encode(deviceId, "UTF-8");
		StringBuilder sb = new StringBuilder("https://");
		sb.append(orgId).
		   append('.').
		   append(this.domain).append(BASIC_API_V0002_URL).
		   append("/authorization/devices/").
		   append(sDeviceId).
		   append("/roles");
		
		int code = 0;
		HttpResponse response = null;
		JsonElement jsonResponse = null;

		try {
			response = connect("put", sb.toString(), jsonRoles.toString(), null);
			code = response.getStatusLine().getStatusCode();
			if (response != null) {
				String result = this.readContent(response, METHOD);
				if (result != null) {
					jsonResponse = new JsonParser().parse(result);
				}
			}
			if (code == 200) {
				return jsonResponse.getAsJsonObject();
			}

		} catch(Exception e) {
			IoTFCReSTException ex = new IoTFCReSTException("Failure in retrieving the access control properties for device ID (" + deviceId + ") Exception: "
					+ "::"+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		if (code == 400) {
			throw new IoTFCReSTException(code, "Invalid request (invalid resource id specified in the path)", jsonResponse);
		} else if(code == 401) {
			throw new IoTFCReSTException(code, "The authentication token is empty or invalid", jsonResponse);
		} else if (code == 403) {
			throw new IoTFCReSTException(code, "The authentication method is invalid or the API key used does not exist", jsonResponse);
		} else if (code == 404) {
			throw new IoTFCReSTException(code, "Invalid request", jsonResponse);
		} else if(code == 500) {
			throw new IoTFCReSTException(code, "Unexpected error", jsonResponse);
		}
		throwException(response, METHOD);
		return null;
				
	}

}
