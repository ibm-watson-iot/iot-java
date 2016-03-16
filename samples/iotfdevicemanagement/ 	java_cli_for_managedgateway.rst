======================================
Java Client Library - Managed Gateway (Update in progress)
======================================

Introduction
-------------

This client library describes how to use devices, gateways and applications with the Java WIoTP client library. For help with getting started with this module, see `Java Client Library - Introduction <https://github.com/ibm-messaging/iot-java/blob/master/README.md>`__. 

This section contains information on how gateways (and attached devices) can connect to the Internet of Things Platform Device Management service using Java and perform device management operations like firmware update, location update, and diagnostics update.

----

Device Management
-------------------------------------------------------------------------------
The `device management <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/index.html>`__ feature enhances the IBM Watson Internet of Things Platform service with new capabilities for managing devices. Device management makes a distinction between managed and unmanaged devices:

* **Managed Devices** are defined as devices which have a management agent installed. The management agent sends and receives device metadata and responds to device management commands from the IBM Watson Internet of Things Platform. 
* **Unmanaged Devices** are any devices which do not have a device management agent. All devices begin their lifecycle as unmanaged devices, and can transition to managed devices by sending a message from a device management agent to the IBM Watson Internet of Things Platform. 

----

Create DeviceData
------------------------------------------------------------------------
The `device model <https://docs.internetofthings.ibmcloud.com/reference/device_model.html>`__ describes the metadata and management characteristics of a device. The device database in the IBM Watson Internet of Things Platform is the master source of device information. Applications and managed devices are able to send updates to the database such as a location or the progress of a firmware update. Once these updates are received by the IBM Watson Internet of Things Platform, the device database is updated, making the information available to applications.

The device model in the WIoTP client library is represented as DeviceData and to create a DeviceData one needs to create the following objects,

* DeviceInfo (Optional)
* DeviceLocation (Optional, required only if the device wants to be notified about the location set by the application through Watson IoT Platform API)
* DeviceFirmware (Optional)
* DeviceMetadata (optional)

The following code snippet shows how to create the object DeviceInfo along with DeviceMetadata with sample data:

.. code:: java

     DeviceInfo deviceInfo = new DeviceInfo.Builder().
				serialNumber("10087").
				manufacturer("IBM").
				model("7865").
				deviceClass("A").
				description("My RasPi Device").
				fwVersion("1.0.0").
				hwVersion("1.0").
				descriptiveLocation("EGL C").
				build();
	
     /**
       * Create a DeviceMetadata object 
      **/
     JsonObject data = new JsonObject();
     data.addProperty("customField", "customValue");
     DeviceMetadata metadata = new DeviceMetadata(data);

The following code snippet shows how to create the DeviceData object with the above created DeviceInfo and DeviceMetadata objects:

.. code:: java

	DeviceData deviceData = new DeviceData.Builder().
				 deviceInfo(deviceInfo).
				 metadata(metadata).
				 build();
----

Construct ManagedGateway
-------------------------------------------------------------------------------
ManagedDevice - A device class that connects the device as managed device to IBM Watson Internet of Things Platform and enables the device to perform one or more Device Management operations. Also the ManagedDevice instance can be used to do normal device operations like publishing device events and listening for commands from application.

ManagedDevice exposes 2 different constructors to support different user patterns, 

**Constructor One**

Constructs a ManagedDevice instance by accepting the DeviceData and the following properties,

* Organization-ID - Your organization ID.
* Device-Type - The type of your device.
* Device-ID - The ID of your device.
* Authentication-Method - Method of authentication (The only value currently supported is "token"). 
* Authentication-Token - API key token

All these properties are required to interact with the IBM Watson Internet of Things Platform. 

The following code shows how to create a ManagedDevice instance:

.. code:: java

	Properties options = new Properties();
	options.setProperty("Organization-ID", "uguhsp");
	options.setProperty("Device-Type", "iotsample-arduino");
	options.setProperty("Device-ID", "00aabbccde03");
	options.setProperty("Authentication-Method", "token");
	options.setProperty("Authentication-Token", "AUTH TOKEN FOR DEVICE");
	
	ManagedDevice managedDevice = new ManagedDevice(options, deviceData);
 
The existing users of DeviceClient might observe that the names of these properties have changed slightly. These names have been changed to mirror the names in the IBM Watson Internet of Things Platform Dashboard, but the existing users who want to migrate from the DeviceClient to the ManagedDevice can still use the old format and construct the ManagedDevice instance as follows:

.. code:: java

	Properties options = new Properties();
	options.setProperty("org", "uguhsp");
	options.setProperty("type", "iotsample-arduino");
	options.setProperty("id", "00aabbccde03");
	options.setProperty("auth-method", "token");
	options.setProperty("auth-token", "AUTH TOKEN FOR DEVICE");
	ManagedDevice managedDevice = new ManagedDevice(options, deviceData);

**Constructor Two**

Construct a ManagedDevice instance by accepting the DeviceData and the MqttClient instance. This constructor requires the DeviceData to be created with additional device attributes like Device Type and Device Id as follows:

.. code:: java
	
	// Code that constructs the MqttClient (either Synchronous or Asynchronous MqttClient)
	.....
	
	// Code that constructs the DeviceData
	DeviceData deviceData = new DeviceData.Builder().
				 typeId("Device-Type").
				 deviceId("Device-ID").
				 deviceInfo(deviceInfo).
				 metadata(metadata).
				 build();
	
	....
	ManagedDevice managedDevice = new ManagedDevice(mqttClient, deviceData);
	
Note this constructor helps the custom device users to create a ManagedDevice instance with the already created and connected MqttClient instance to take advantage of device management operations. But we recommend the users to use the library for all the device functionalities.

----

Manage	
------------------------------------------------------------------
The device can invoke sendManageRequest() method to participate in device management activities. The manage request will initiate a connect request internally if the device is not connected to the IBM Watson Internet of Things Platform already:

.. code:: java

	managedDevice.manage(0, true, true);
	
As shown, this method accepts following 3 parameters,

* *lifetime* The length of time in seconds within which the device must send another **Manage device** request in order to avoid being reverted to an unmanaged device and marked as dormant. If set to 0, the managed device will not become dormant. When set, the minimum supported setting is 3600 (1 hour).
* *supportFirmwareActions* Tells whether the device supports firmware actions or not. The device must add a firmware handler to handle the firmware requests.
* *supportDeviceActions* Tells whether the device supports Device actions or not. The device must add a Device action handler to handle the reboot and factory reset requests.


Refer to the `documentation <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/index.html#/manage-device#manage-device>`__ for more information about the manage operation.

----

Unmanage
-----------------------------------------------------

A device can invoke sendUnmanageRequest() method when it no longer needs to be managed. The IBM Watson Internet of Things Platform will no longer send new device management requests to this device and all device management requests from this device will be rejected other than a **Manage device** request.

.. code:: java

	managedDevice.sendUnmanageRequest();

Refer to the `documentation <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/index.html#/unmanage-device#unmanage-device>`__ for more information about the Unmanage operation.

----

Location Update
-----------------------------------------------------

Devices that can determine their location can choose to notify the IBM Watson Internet of Things Platform about location changes. The Device can invoke one of the overloaded updateLocation() method to update the location of the device. 

.. code:: java

    // update the location with latitude, longitude and elevation
    int rc = managedDevice.updateLocation(30.28565, -97.73921, 10);
    if(rc == 200) {
        System.out.println("Location updated successfully !!");
    } else {
     	System.err.println("Failed to update the location !!");
    }

Refer to the `documentation <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/index.html#/update-location#update-location>`__ for more information about the Location update.

----

Append/Clear ErrorCodes
-----------------------------------------------

Devices can choose to notify the IBM Watson Internet of Things Platform about changes in their error status. The Device can invoke  addErrorCode() method to add the current errorcode to Watson IoT Platform.

.. code:: java

	int rc = managedDevice.addErrorCode(300);

Also, the ErrorCodes can be cleared from IBM Watson Internet of Things Platform by calling the clearErrorCodes() method as follows:

.. code:: java

	int rc = managedDevice.clearErrorCodes();

----

Append/Clear Log messages
-----------------------------
Devices can choose to notify the IBM Watson Internet of Things Platform about changes by adding a new log entry. Log entry includes a log messages, its timestamp and severity, as well as an optional base64-encoded binary diagnostic data. The Devices can invoke addLog() method to send log messages,

.. code:: java
	// An example Log event
	String message = "Firmware Download Progress (%): " + 50;
	Date timestamp = new Date();
	LogSeverity severity = LogSeverity.informational;
	int rc = managedDevice.addLog(message, timestamp, severity);
	
Also, the log messages can be cleared from IBM Watson Internet of Things Platform by calling the clearLogs() method as follows:

.. code:: java

	rc = managedDevice.clearLogs();

The device diagnostics operations are intended to provide information on device errors, and does not provide diagnostic information relating to the devices connection to the IBM Watson Internet of Things Platform.

Refer to the `documentation <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/index.html#/update-location#update-location>`__ for more information about the Diagnostics operation.

----

Firmware Actions
-------------------------------------------------------------
The firmware update process is separated into two distinct actions:

* Downloading Firmware 
* Updating Firmware. 

The device needs to do the following activities to support Firmware Actions:

**1. Construct DeviceFirmware Object (Optional)**

In order to perform Firmware actions the device can optionally construct the DeviceFirmware object and add it to DeviceData as follows:

.. code:: java

	DeviceFirmware firmware = new DeviceFirmware.Builder().
				version("Firmware.version").
				name("Firmware.name").
				url("Firmware.url").
				verifier("Firmware.verifier").
				state(FirmwareState.IDLE).				
				build();
				
	DeviceData deviceData = new DeviceData.Builder().
				deviceInfo(deviceInfo).
				deviceFirmware(firmware).
				metadata(metadata).
				build();
	
	ManagedDevice managedDevice = new ManagedDevice(options, deviceData);
	managedDevice.connect();
		

The DeviceFirmware object represents the current firmware of the device and will be used to report the status of the Firmware Download and Firmware Update actions to IBM Watson Internet of Things Platform. In case this DeviceFirmware object is not constructed by the device, then the library creates an empty object and reports the status to Watson IoT Platform.

**2. Inform the server about the Firmware action support**

The device needs to set the firmware action flag to true in order for the server to initiate the firmware request. This can be achieved by invoking the sendManageRequest() method with a true value for supportFirmwareActions parameter,

.. code:: java

    	managedDevice.sendManageRequest(3600, true, false);

Once the support is informed to the DM server, the server then forwards the firmware actions to the device.

**3. Create the Firmware Action Handler**

In order to support the Firmware action, the device needs to create a handler and add it to ManagedDevice. The handler must extend a DeviceFirmwareHandler class and implement the following methods:

.. code:: java

	public abstract void downloadFirmware(DeviceFirmware deviceFirmware);
	public abstract void updateFirmware(DeviceFirmware deviceFirmware);

**3.1 Sample implementation of downloadFirmware**

The implementation must create a separate thread and add a logic to download the firmware and report the status of the download via DeviceFirmware object. If the Firmware Download operation is successful, then the state of the firmware to be set to DOWNLOADED and UpdateStatus should be set to SUCCESS.

If an error occurs during Firmware Download the state should be set to IDLE and updateStatus should be set to one of the error status values:

* OUT_OF_MEMORY
* CONNECTION_LOST
* INVALID_URI

A sample Firmware Download implementation for a Raspberry Pi device is shown below:

.. code:: java

	public void downloadFirmware(DeviceFirmware deviceFirmware) {
		boolean success = false;
		URL firmwareURL = null;
		URLConnection urlConnection = null;
		
		try {
			firmwareURL = new URL(deviceFirmware.getUrl());
			urlConnection = firmwareURL.openConnection();
			if(deviceFirmware.getName() != null) {
				downloadedFirmwareName = deviceFirmware.getName();
			} else {
				// use the timestamp as the name
				downloadedFirmwareName = "firmware_" +new Date().getTime()+".deb";
			}
			
			File file = new File(downloadedFirmwareName);
			BufferedInputStream bis = new BufferedInputStream(urlConnection.getInputStream());
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file.getName()));
			
			int data = bis.read();
			if(data != -1) {
				bos.write(data);
				byte[] block = new byte[1024];
				while (true) {
					int len = bis.read(block, 0, block.length);
					if(len != -1) {
						bos.write(block, 0, len);
					} else {
						break;
					}
				}
				bos.close();
				bis.close();
				success = true;
			} else {
				//There is no data to read, so set an error
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.INVALID_URI);
			}
		} catch(MalformedURLException me) {
			// Invalid URL, so set the status to reflect the same,
			deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.INVALID_URI);
		} catch (IOException e) {
			deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.CONNECTION_LOST);
		} catch (OutOfMemoryError oom) {
			deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.OUT_OF_MEMORY);
		}
		
		if(success == true) {
			deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
			deviceFirmware.setState(FirmwareState.DOWNLOADED);
		} else {
			deviceFirmware.setState(FirmwareState.IDLE);
		}
	}

Device can check the integrity of the downloaded firmware image using the verifier and report the status back to IBM Watson Internet of Things Platform. The verifier can be set by the device during the startup (while creating the DeviceFirmware Object) or as part of the Download Firmware request by the application. A sample code to verify the same is below:

.. code:: java

	private boolean verifyFirmware(File file, String verifier) throws IOException {
		FileInputStream fis = null;
		String md5 = null;
		try {
			fis = new FileInputStream(file);
			md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			System.out.println("Downloaded Firmware MD5 sum:: "+ md5);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fis.close();
		}
		if(verifier.equals(md5)) {
			System.out.println("Firmware verification successful");
			return true;
		}
		System.out.println("Download firmware checksum verification failed.. "
				+ "Expected "+verifier + " found "+md5);
		return false;
	}

The complete code can be found in the device management sample `RasPiFirmwareHandlerSample <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/src/com/ibm/iotf/sample/devicemgmt/device/RasPiFirmwareHandlerSample.java>`__.

**3.2 Sample implementation of updateFirmware**

The implementation must create a separate thread and add a logic to install the downloaded firmware and report the status of the update via DeviceFirmware object. If the Firmware Update operation is successful, then the state of the firmware should to be set to IDLE and UpdateStatus should be set to SUCCESS. 

If an error occurs during Firmware Update, updateStatus should be set to one of the error status values:

* OUT_OF_MEMORY
* UNSUPPORTED_IMAGE
			
A sample Firmware Update implementation for a Raspberry Pi device is shown below:

.. code:: java
	
	public void updateFirmware(DeviceFirmware deviceFirmware) {
		try {
			ProcessBuilder pkgInstaller = null;
			Process p = null;
			pkgInstaller = new ProcessBuilder("sudo", "dpkg", "-i", downloadedFirmwareName);
			boolean success = false;
			try {
				p = pkgInstaller.start();
				boolean status = waitForCompletion(p, 5);
				if(status == false) {
					p.destroy();
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
					return;
				}
				System.out.println("Firmware Update command "+status);
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
				deviceFirmware.setState(FirmwareState.IDLE);
			} catch (IOException e) {
				e.printStackTrace();
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
			} catch (InterruptedException e) {
				e.printStackTrace();
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
			}
		} catch (OutOfMemoryError oom) {
			deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.OUT_OF_MEMORY);
		}
	}

The complete code can be found in the device management sample `RasPiFirmwareHandlerSample <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/src/com/ibm/iotf/sample/devicemgmt/device/RasPiFirmwareHandlerSample.java>`__.

**4. Add the handler to ManagedDevice**

The created handler needs to be added to the ManagedDevice instance so that the WIoTP client library invokes the corresponding method when there is a Firmware action request from IBM Watson Internet of Things Platform.

.. code:: java

	DeviceFirmwareHandlerSample fwHandler = new DeviceFirmwareHandlerSample();
	deviceData.addFirmwareHandler(fwHandler);

Refer to `this page <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/requests.html#/firmware-actions#firmware-actions>`__ for more information about the Firmware action.

----

Device Actions
------------------------------------
The IBM Watson Internet of Things Platform supports the following device actions:

* Reboot
* Factory Reset

The device needs to do the following activities to support Device Actions:

**1. Inform server about the Device Actions support**

In order to perform Reboot and Factory Reset, the device needs to inform the IBM Watson Internet of Things Platform about its support first. This can be achieved by invoking the sendManageRequest() method with a true value for supportDeviceActions parameter,

.. code:: java
	// Last parameter represents the device action support
    	managedDevice.sendManageRequest(3600, true, true);

Once the support is informed to the DM server, the server then forwards the device action requests to the device.
	
**2. Create the Device Action Handler**

In order to support the device action, the device needs to create a handler and add it to ManagedDevice. The handler must extend a DeviceActionHandler class and provide implementation for the following methods:

.. code:: java

	public abstract void handleReboot(DeviceAction action);
	public abstract void handleFactoryReset(DeviceAction action);

**2.1 Sample implementation of handleReboot**

The implementation must create a separate thread and add a logic to reboot the device and report the status of the reboot via DeviceAction object. The device needs to update the status along with a optional message only when there is a failure (because the successful operation reboots the device and the device code will not have a control to update the IBM Watson Internet of Things Platform). A sample reboot implementation for a Raspberry Pi device is shown below:

.. code:: java

	public void handleReboot(DeviceAction action) {
		ProcessBuilder processBuilder = null;
		Process p = null;
		processBuilder = new ProcessBuilder("sudo", "shutdown", "-r", "now");
		boolean status = false;
		try {
			p = processBuilder.start();
			// wait for say 2 minutes before giving it up
			status = waitForCompletion(p, 2);
		} catch (IOException e) {
			action.setMessage(e.getMessage());
		} catch (InterruptedException e) {
			action.setMessage(e.getMessage());
		}
		if(status == false) {
			action.setStatus(DeviceAction.Status.FAILED);
		}
	}

The complete code can be found in the device management sample `DeviceActionHandlerSample <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/src/com/ibm/iotf/sample/devicemgmt/device/DeviceActionHandlerSample.java>`__.

**2.2 Sample implementation of handleFactoryReset**

The implementation must create a separate thread and add a logic to reset the device to factory settings and report the status via DeviceAction object. The device needs to update the status along with a optional message only when there is a failure (because as part of this process, the device reboots and the device will not have a control to update status to IBM Watson Internet of Things Platform). The skeleton of the Factory Reset implementation is shown below:

.. code:: java
	
	public void handleFactoryReset(DeviceAction action) {
		try {
			// code to perform Factory reset
		} catch (IOException e) {
			action.setMessage(e.getMessage());
		}
		if(status == false) {
			action.setStatus(DeviceAction.Status.FAILED);
		}
	}

**3. Add the handler to ManagedDevice**

The created handler needs to be added to the ManagedDevice instance so that the WIoTP client library invokes the corresponding method when there is a device action request from IBM Watson Internet of Things Platform.

.. code:: java

	DeviceActionHandlerSample actionHandler = new DeviceActionHandlerSample();
	deviceData.addDeviceActionHandler(actionHandler);

Refer to `this page <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/requests.html#/device-actions-reboot#device-actions-reboot>`__ for more information about the Device Action.

----

Listen for Device attribute changes
-----------------------------------------------------------------

This WIoTP client library updates the corresponding objects whenever there is an update request from the IBM Watson Internet of Things Platform, these update requests are initiated by the application either directly or indirectly (Firmware Update) via the IBM Watson Internet of Things Platform ReST API. Apart from updating these attributes, the library provides a mechanism where the device can be notified whenever a device attribute is updated.

Attributes that can be updated by this operation are location, metadata, device information and firmware.

In order to get notified, the device needs to add a property change listener on those objects that it is interested.

.. code:: java

	deviceLocation.addPropertyChangeListener(listener);
	firmware.addPropertyChangeListener(listener);
	deviceInfo.addPropertyChangeListener(listener);
	metadata.addPropertyChangeListener(listener);
	
Also, the device needs to implement the propertyChange() method where it receives the notification. A sample implementation is as follows:

.. code:: java

	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getNewValue() == null) {
			return;
		}
		Object value = (Object) evt.getNewValue();
		
		switch(evt.getPropertyName()) {
			case "metadata":
				DeviceMetadata metadata = (DeviceMetadata) value;
				System.out.println("Received an updated metadata -- "+ metadata);
				break;
			
			case "location":
				DeviceLocation location = (DeviceLocation) value;
				System.out.println("Received an updated location -- "+ location);
				break;
			
			case "deviceInfo":
				DeviceInfo info = (DeviceInfo) value;
				System.out.println("Received an updated device info -- "+ info);
				break;
				
			case "mgmt.firmware":
				DeviceFirmware firmware = (DeviceFirmware) value;
				System.out.println("Received an updated device firmware -- "+ firmware);
				break;		
		}
	}

Refer to `this page <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/index.html#/update-device-attributes#update-device-attributes>`__ for more information about updating the device attributes.

----

Examples
-------------
* `SampleRasPiDMAgent <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/src/com/ibm/iotf/sample/devicemgmt/device/SampleRasPiDMAgent.java>`__ - A sample agent code that shows how to perform various device management operations on Raspberry Pi.
* `SampleRasPiManagedDevice <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/src/com/ibm/iotf/sample/devicemgmt/device/SampleRasPiManagedDevice.java>`__ - A sample code that shows how one can perform both device operations and management operations.
* `SampleRasPiDMAgentWithCustomMqttAsyncClient <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/src/com/ibm/iotf/sample/devicemgmt/device/SampleRasPiDMAgentWithCustomMqttAsyncClient.java>`__ - A sample agent code with custom MqttAsyncClient.
* `SampleRasPiDMAgentWithCustomMqttClient <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/src/com/ibm/iotf/sample/devicemgmt/device/SampleRasPiDMAgentWithCustomMqttClient.java>`__ - A sample agent code with custom MqttClient.
* `RasPiFirmwareHandlerSample <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/src/com/ibm/iotf/sample/devicemgmt/device/RasPiFirmwareHandlerSample.java>`__ - A sample implementation of FirmwareHandler for Raspberry Pi.
* `DeviceActionHandlerSample <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/src/com/ibm/iotf/sample/devicemgmt/device/DeviceActionHandlerSample.java>`__ - A sample implementation of DeviceActionHandler
* `ManagedDeviceWithLifetimeSample <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/src/com/ibm/iotf/sample/devicemgmt/device/ManagedDeviceWithLifetimeSample.java>`__ - A sample that shows how to send regular manage request with lifetime specified.
* `DeviceAttributesUpdateListenerSample <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/src/com/ibm/iotf/sample/devicemgmt/device/DeviceAttributesUpdateListenerSample.java>`__ - A sample listener code that shows how to listen for a various device attribute changes.

----

Recipe
----------

Refer to `the recipe <https://developer.ibm.com/recipes/tutorials/connect-raspberry-pi-as-managed-device-to-ibm-iot-foundation/>`__ that shows how to connect the Raspberry Pi device as managed device to IBM Watson Internet of Things Platform to perform various device management operations in step by step using this client library.
