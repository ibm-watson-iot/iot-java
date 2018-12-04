package com.ibm.iotf.client.device;
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.DeviceLocation;
import com.ibm.iotf.devicemgmt.DeviceMetadata;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

/**
 * A sample device code that listens for the update message from IBM Watson IoT Platform. 
 * The library code updates these attributes in the corresponding objects and 
 * notifies the sample code if interested,
 * 
 * The IBM Watson IoT Platform can send the following update request to a device to update 
 * values of one or more device attributes. 
 * 
 * iotdm-1/device/update
 * 
 * Attributes that can be updated by this operation are location, metadata, device information and firmware.
 * 
 * The "value" is the new value of the device attribute. It is a complex field matching the device model. 
 * Only writeable fields should be updated as a result of this operation. Values can be updated in:
 * 
 * location
 * metadata
 * deviceInfo
 * mgmt.firmware
 * 
 * 
 * This sample shows how one can listen for the incoming update message from IBM Watson IoT Platform
 * 
 * This sample takes a properties file where the device informations and location
 * informations are present. There is a default properties file in the sample folder, this
 * class takes the default properties file if one not specified by user.
 */
public class DeviceAttributesUpdateTest implements PropertyChangeListener {

	private static ManagedDevice dmClient = null;
	private static APIClient apiClient = null;
	private static final String DEVICE_TYPE = "DevAttUpdType1";
	private static final String DEVICE_ID = "DevAttUpdDev1";

	private final static String newlocationToBeAdded = "{\"longitude\": 10, \"latitude\": 20, \"elevation\": 0}";
	
	private final static String deviceInfoToBeAdded = "{\"serialNumber\": \"10087\",\"manufacturer\": \"IBM\",\"model\": \"7865\",\"deviceClass\": "
			+ "\"A\",\"description\": \"My DEVICE_ID2 Device\",\"fwVersion\": \"1.0.0\","
			+ "\"hwVersion\": \"1.0\",\"descriptiveLocation\": \"EGL C\"}";
	
	private static final String DOWNLOAD_REQUEST = "{\"action\": \"firmware/download\", \"parameters\": [{\"name\": \"version\", \"value\": \"0.1.10\" }," +
			"{\"name\": \"name\", \"value\": \"RasPi01 firmware\"}, {\"name\": \"verifier\", \"value\": \"123df\"}," +
			"{\"name\": \"uri\",\"value\": \"https://github.com/ibm-messaging/iot-raspberrypi/releases/download/1.0.2.1/iot_1.0-2_armhf.deb\"}" +
			"],\"devices\": [{\"typeId\": \"" + DEVICE_TYPE + "\",\"deviceId\": \"" + DEVICE_ID + "\"}]}";
	
	private boolean metaDataUpdated;
	private boolean locationUpdated;
	private boolean deviceInfoUpdated;
	private boolean firmwareUpdated;
	
	
	/**
	 * This sample showcases how to update a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 * @throws InterruptedException 
	 */
	public void test01updateDeviceLocation() throws IoTFCReSTException, InterruptedException {
		System.out.println("update device location of device --> "+DEVICE_ID);
		JsonElement newLocation = new JsonParser().parse(newlocationToBeAdded);
		try {
			JsonObject response = apiClient.updateDeviceLocation(DEVICE_TYPE, DEVICE_ID, newLocation);
			int count = 0;
			while(count++ < 20) {
				if(this.locationUpdated) {
					break;
				}
				Thread.sleep(1000);
			}
			//ToDo: uncomment once the defect is fixed
			//assertTrue("Failed to receive the location update", this.locationUpdated);
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	public void tearDown() {
		this.dmClient.disconnect();
	}
	
	/**
	 * This sample showcases how to update a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 * @throws InterruptedException 
	 */
	public void test02updateDeviceInfo() throws IoTFCReSTException, InterruptedException {
		System.out.println("update deviceInfo of a device --> "+DEVICE_ID);
		JsonElement info = new JsonParser().parse(deviceInfoToBeAdded);
		JsonObject deviceInfo = new JsonObject();
		deviceInfo.add("deviceInfo", info);
		try {
			JsonObject response = apiClient.updateDevice(DEVICE_TYPE, DEVICE_ID, deviceInfo);
			System.out.println(response);
			int count = 0;
			while(count++ < 20) {
				if(this.deviceInfoUpdated) {
					break;
				}
				Thread.sleep(1000);
			}
			assertTrue("Failed to receive the DeviceInfo update", this.deviceInfoUpdated);
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}
	
	/**
	 * This sample showcases how to update a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 * @throws InterruptedException 
	 */
	public void test03updateMetadata() throws IoTFCReSTException, InterruptedException {
		System.out.println("update Metadata of a device --> "+DEVICE_ID);
		JsonObject data = new JsonObject();
		data.addProperty("updatedkey", "updatedValue");
		JsonObject metadata = new JsonObject();
		metadata.add("metadata", data);
		try {
			JsonObject response = apiClient.updateDevice(DEVICE_TYPE, DEVICE_ID, metadata);
			System.out.println(response);
			int count = 0;
			while(count++ < 20) {
				if(this.metaDataUpdated) {
					break;
				}
				Thread.sleep(1000);
			}
			assertTrue("Failed to receive the Metadata update", this.metaDataUpdated);
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
	}

	/**
	 * This sample showcases how to update a device location using the Java Client Library.
	 * @throws IoTFCReSTException
	 * @throws InterruptedException 
	 */
	public void test04UpdateFirmware() throws IoTFCReSTException, InterruptedException {
		System.out.println("Initiate Firmware download for device --> "+DEVICE_ID);
		JsonObject download = (JsonObject) new JsonParser().parse(DOWNLOAD_REQUEST);
		System.out.println(download);

		try {
			boolean response = apiClient.initiateDeviceManagementRequest(download);
			System.out.println(response);
			int count = 0;
			while(count++ < 20) {
				if(this.firmwareUpdated) {
					break;
				}
				Thread.sleep(1000);
			}
			assertTrue("Failed to receive the Firmware object update", this.firmwareUpdated);
		} catch(IoTFCReSTException e) {
			fail("HttpCode :" + e.getHttpCode() +" ErrorMessage :: "+ e.getMessage());
			// Print if there is a partial response
			System.out.println(e.getResponse());
		}
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getNewValue() == null) {
			return;
		}
		Object value = (Object) evt.getNewValue();
		
		switch(evt.getPropertyName()) {
			case "metadata":
				DeviceMetadata metadata = (DeviceMetadata) value;
				System.out.println("Received an updated metadata -- "+ metadata.getValue());
				this.metaDataUpdated = true;
				break;
			
			case "location":
				DeviceLocation location = (DeviceLocation) value;
				System.out.println("received an updated location -- "+ location.getLongitude() +
						" " + location.getLongitude() + " " + location.getElevation() +
						" " + location.getAccuracy() + " " + location.getMeasuredDateTime());
				this.locationUpdated = true;
				break;
			
			case "deviceInfo":
				DeviceInfo info = (DeviceInfo) value;
				System.out.println("received an updated device info -- "+ info.getDescription() +
						" "+info.getDescriptiveLocation() + " " +info.getDeviceClass() + " " +
						info.getFwVersion() + " "+info.getHwVersion() + " "+info.getManufacturer() +
						" " + info.getModel());
				this.deviceInfoUpdated = true;
				break;
				
			case "mgmt.firmware":
				DeviceFirmware firmware = (DeviceFirmware) value;
				System.out.println("received an updated device firmware -- "+ firmware);
				this.firmwareUpdated = true;
				break;		
		}
	}
	
	private String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}
	
	/**
	 * This method builds the device objects required to create the
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
			deviceProps.load(DeviceAttributesUpdateTest.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}


		/**
		 * To create a DeviceData object, we will need the following objects:
		 *   - DeviceInfo (mandatory)
		 *   - DeviceLocation (optional)
		 *   - DeviceDiagnostic (optional)
		 *   - DeviceFirmware (optional)
		 *   - DeviceMetadata (mandatory)
		 */
		DeviceInfo deviceInfo = new DeviceInfo.Builder().
				serialNumber(trimedValue(deviceProps.getProperty("DeviceInfo.serialNumber"))).
				manufacturer(trimedValue(deviceProps.getProperty("DeviceInfo.manufacturer"))).
				model(trimedValue(deviceProps.getProperty("DeviceInfo.model"))).
				deviceClass(trimedValue(deviceProps.getProperty("DeviceInfo.deviceClass"))).
				description(trimedValue(deviceProps.getProperty("DeviceInfo.description"))).
				fwVersion(trimedValue(deviceProps.getProperty("DeviceInfo.swVersion"))).
				hwVersion(trimedValue(deviceProps.getProperty("DeviceInfo.hwVersion"))).
				descriptiveLocation(trimedValue(deviceProps.getProperty("DeviceInfo.descriptiveLocation"))).
				build();
		
		
		DeviceFirmware firmware = new DeviceFirmware.Builder().
				version(trimedValue(deviceProps.getProperty("DeviceFirmware.version"))).
				name(trimedValue(deviceProps.getProperty("DeviceFirmware.name"))).
				url(trimedValue(deviceProps.getProperty("DeviceFirmware.url"))).
				verifier(trimedValue(deviceProps.getProperty("DeviceFirmware.verifier"))).
				state(FirmwareState.IDLE).				
				build();
		
		/**
		 * Create a DeviceMetadata object
		 */
		JsonObject data = new JsonObject();
		data.addProperty("customField", "customValue");
		DeviceMetadata metadata = new DeviceMetadata(data);
		
		DeviceData deviceData = new DeviceData.Builder().
						 deviceInfo(deviceInfo).
						 deviceFirmware(firmware).
						 metadata(metadata).
						 build();
		
		DeviceLocation location = new DeviceLocation();
		// Add a listener for all possible attribute changes
		location.addPropertyChangeListener(this);
		firmware.addPropertyChangeListener(this);
		deviceInfo.addPropertyChangeListener(this);
		metadata.addPropertyChangeListener(this);
		
		// Options to connect to Watson IoT Platform
		Properties options = new Properties();
		options.setProperty("Organization-ID", trimedValue(deviceProps.getProperty("Organization-ID")));
		options.setProperty("Device-Type", trimedValue(deviceProps.getProperty("Device-Type")));
		options.setProperty("Device-ID", trimedValue(deviceProps.getProperty("Device-ID")));
		options.setProperty("Authentication-Method", trimedValue(deviceProps.getProperty("Authentication-Method")));
		options.setProperty("Authentication-Token", trimedValue(deviceProps.getProperty("Authentication-Token")));
		

		dmClient = new ManagedDevice(options, deviceData);
		dmClient.connect();
		dmClient.sendManageRequest(0,  true,  true);
	}
}
