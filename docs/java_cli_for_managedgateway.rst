======================================
Java Client Library - Managed Gateway
======================================

Introduction
-------------

Gateway plays an important role in the device management of devices connected to it. Many of these devices will be too basic to be managed. For a managed device, the device management agent on the gateway acts as a proxy for the connected device. The protocol used by the gateway to manage the connected devices is arbitrary, and there need not be a device management agent on the connected devices. It is only necessary for the gateway to ensure that devices connected to it perform their responsibilities as managed devices, performing any translation and processing required to achieve this. The management agent in gateway will act as more than a transparent tunnel between the attached device and the Watson IoT Platform.

For example, It is unlikely that a device connected to a gateway will be able to download the firmware itself. In this case, the gateway’s device management agent will intercept the request to download the firmware and perform the download to its own storage. Then, when the device is instructed to perform the upgrade, the gateway’s device management agent will push the firmware to the device and update it.

This section contains information on how gateways (and attached devices) can connect to the Internet of Things Platform Device Management service using Java and perform device management operations like firmware update, location update, and diagnostics update.

----

Create DeviceData
------------------------------------------------------------------------
The `device model <https://docs.internetofthings.ibmcloud.com/reference/device_model.html>`__ describes the metadata and management characteristics of a device or a gateway. The device database in the IBM Watson Internet of Things Platform is the master source of device information. Applications and managed devices are able to send updates to the database such as a location or the progress of a firmware update. Once these updates are received by the IBM Watson Internet of Things Platform, the device database is updated, making the information available to applications.

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

Each gateway and attached devices must have its own DeviceData to represent itself in the Platform. In the case of gateway, the DeviceData will be passed to the library as part of constructing the ManagedGateway instance, and in the case of attached device, the DeviceData will be passed as part of the sendDeviceManageRequest(). 

----

Construct ManagedGateway
-------------------------------------------------------------------------------
ManagedGateway - A gateway class that connects the gateway as managed gateway to IBM Watson Internet of Things Platform and enables the gateway to perform one or more Device Management operations for itself and attached devices. Also the ManagedGateway instance can be used to do normal gateway operations like publishing gateway events, attached device events and listening for commands from application.

ManagedGateway exposes 2 different constructors to support different user patterns, 

**Constructor One**

Constructs a ManagedGateway instance by accepting the DeviceData and the following properties,

* Organization-ID - Your organization ID.
* Gateway-Type - The type of your gateway device.
* Gateway-ID - The ID of your gateway device.
* Authentication-Method - Method of authentication (The only value currently supported is "token"). 
* Authentication-Token - API key token
* clean-session - true or false (required only if you want to connect the Gateway in durable subscription. By default the clean-session is set to true).
* Port - Specify the port to connect to, supported ports are 8883 and 443. (default port is 8883). 
* WebSocket - true or false (default is false, required if you want to connect the device using websockets).
* MaxInflightMessages - Sets the maximum number of inflight messages for the connection (default value is 100).
* Automatic-Reconnect - true or false (default: false). When set, the library will automatically attempt to reconnect to the Watson IoT Platform while the client is in disconnected state.
* Disconnected-Buffer-Size - The maximum number of messages that will be stored in memory while the client is disconnected. Default: 5000.

**Note:** One must set clean-session to false to connect the Gateway in durable subscription. Refer to `Subscription Buffers and Clean Session <https://docs.internetofthings.ibmcloud.com/reference/mqtt/index.html#/subscription-buffers-and-clean-session#subscription-buffers-and-clean-session>`__ for more information about the clean session.


All these properties are required to interact with the IBM Watson Internet of Things Platform. 

The following code shows how to create a ManagedGateway instance:

.. code:: java

	Properties options = new Properties();
	options.setProperty("Organization-ID", "uguhsp");
	options.setProperty("Gateway-Type", "iotsample-arduino");
	options.setProperty("Gateway-ID", "00aabbccde03");
	options.setProperty("Authentication-Method", "token");
	options.setProperty("Authentication-Token", "AUTH TOKEN FOR DEVICE");
	
	ManagedGateway ManagedGateway = new ManagedGateway(options, deviceData);

**Constructor Two**

Construct a ManagedGateway instance by accepting the DeviceData and the MqttClient instance. This constructor requires the DeviceData to be created with additional device attributes like Device Type and Device Id as follows:

.. code:: java
	
	// Code that constructs the MqttClient (either Synchronous or Asynchronous MqttClient)
	.....
	
	// Code that constructs the DeviceData
	DeviceData deviceData = new DeviceData.Builder().
				 typeId("Gateway-Type").
				 deviceId("Gateway-ID").
				 deviceInfo(deviceInfo).
				 metadata(metadata).
				 build();
	
	....
	ManagedGateway ManagedGateway = new ManagedGateway(mqttClient, deviceData);
	
Note this constructor helps the custom device users to create a ManagedGateway instance with the already created and connected MqttClient instance to take advantage of device management operations. But we recommend the users to use the library for all the device functionalities.

----

Manage request - gateway
-------------------------------------------------------

The gateway can invoke sendGatewayManageRequest() method to participate in device management activities. The manage request will initiate a connect request internally if the device is not connected to the IBM Watson Internet of Things Platform already:

.. code:: java

	managedGateway.sendGatewayManageRequest(0, true, true);
	
As shown, this method accepts following 3 parameters,

* *lifetime* The length of time in seconds within which the gateway must send another **Manage** request in order to avoid being reverted to an unmanaged device and marked as dormant. If set to 0, the managed gateway will not become dormant. When set, the minimum supported setting is 3600 (1 hour).
* *supportFirmwareActions* Tells whether the gateway supports firmware actions or not. The gateway must add a firmware handler to handle the firmware requests.
* *supportDeviceActions* Tells whether the gateway supports Device actions or not. The gateway must add a Device action handler to handle the reboot and factory reset requests.

Manage request - attached devices
--------------------------------------

The gateway can invoke sendDeviceManageRequest() method to make the attached devices participate in the device management activities. 

.. code:: java

	managedGateway.sendDeviceManageRequest(typeId, deviceId, lifetime, true, true);

As shown, this method accepts the details of the attached device apart from the lifetime and device/firmware support parameters. The gateway can also use the overloaded sendDeviceManageRequest() method to specify the DeviceData for the attached device.

Refer to the `documentation <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/index.html#/manage-device#manage-device>`__ for more information about the manage operation.

----

Unmanage request - gateway
-----------------------------------------------------

A gateway can invoke sendGatewayUnmanageRequest() method when it no longer needs to be managed. The IBM Watson Internet of Things Platform will no longer send new device management requests for this gateway and all device management requests from the gateway (only for the gateway and not for the attached devices) will be rejected other than a **Manage** request.

.. code:: java

	managedGateway.sendGatewayUnmanageRequest();

Unmanage request - attached devices
-----------------------------------------------------

The gateway can invoke sendDeviceUnmanageRequest() method to move the attached device from managed state to unmanaged state. The IBM Watson Internet of Things Platform will no longer send new device management requests for this device and all device management requests from the gateway for this attached device will be rejected other than a **Manage** request.

.. code:: java

	managedGateway.sendDeviceUnmanageRequest();

Refer to the `documentation <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/index.html#/unmanage-device#unmanage-device>`__ for more information about the Unmanage operation.

----

Location update - gateway
-----------------------------------------------------

Gateways that can determine their location can choose to notify the IBM Watson Internet of Things Platform about location changes. The gateway can invoke one of the overloaded updateLocation() method to update the location of the device. 

.. code:: java

    // update the location with latitude, longitude and elevation
    int rc = managedGateway.updateGatewayLocation(30.28565, -97.73921, 10);
    if(rc == 200) {
        System.out.println("Location updated successfully !!");
    } else {
     	System.err.println("Failed to update the location !!");
    }

Location update - attached devices
---------------------------------------

The gateway can invoke corresponding device method updateDeviceLocation() to update the location of the attached devices. The overloaded method can be used to specify the measuredDateTime and etc..

.. code:: java

    // update the location of the attached device with latitude, longitude and elevation
    int rc = managedGateway.updateDeviceLocation(typeId, deviceId, 30.28565, -97.73921, 10);


Refer to the `documentation <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/index.html#/update-location#update-location>`__ for more information about the Location update.

----

Append/Clear ErrorCodes - gateway
-----------------------------------------------

Gateways can choose to notify the IBM Watson Internet of Things Platform about changes in their error status. The gateway can invoke  addErrorCode() method to add the current errorcode to Watson IoT Platform.

.. code:: java

	int rc = managedGateway.addGatewayErrorCode(300);

Also, the ErrorCodes of gateway can be cleared from IBM Watson Internet of Things Platform by calling the clearErrorCodes() method as follows:

.. code:: java

	int rc = managedGateway.clearGatewayErrorCodes();

Append/Clear ErrorCodes - attached devices
-----------------------------------------------

Similarly, the gateway can invoke the corresponding device method to add/clear the errorcodes of the attached devices,

.. code:: java

	int rc = managedGateway.addDeviceErrorCode(typeId, deviceId, 300);
	rc = managedGateway.clearDeviceErrorCodes(typeId, deviceId);

----

Append/Clear Log messages - gateway
--------------------------------------
Gateways can choose to notify the IBM Watson Internet of Things Platform about changes by adding a new log entry. Log entry includes a log messages, its timestamp and severity, as well as an optional base64-encoded binary diagnostic data. The gateways can invoke addGatewayLog() method to send log messages,

.. code:: java
	// An example Log event
	String message = "Firmware Download Progress (%): " + 50;
	Date timestamp = new Date();
	LogSeverity severity = LogSeverity.informational;
	int rc = managedGateway.addGatewayLog(message, timestamp, severity);
	
Also, the log messages can be cleared from IBM Watson Internet of Things Platform by calling the clearLogs() method as follows:

.. code:: java

	rc = managedGateway.clearGatewayLogs();
	
Append/Clear Logs - attached devices
-----------------------------------------------

Similarly, the gateway can invoke the corresponding device method to add/clear the Logs of the attached devices,

.. code:: java

	// An example Log event
	String message = "Firmware Download Progress (%): " + 50;
	Date timestamp = new Date();
	LogSeverity severity = LogSeverity.informational;
	int rc = managedGateway.addDeviceLog(typeId, deviceId, message, timestamp, severity);
	
and to clear the Logs of attached devices, invoke the clearDeviceLogs() method with the details of the attached device,

..code:: java

     int rc = managedGateway.clearDeviceLogs(typeId, deviceId);

The device diagnostics operations are intended to provide information on gateway/device errors, and does not provide diagnostic information relating to the devices connection to the IBM Watson Internet of Things Platform.

Refer to the `documentation <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/index.html#/update-location#update-location>`__ for more information about the Diagnostics operation.

----

Firmware Action
-------------------------------------------------------------
The firmware update process is separated into two distinct actions:

* Downloading Firmware 
* Updating Firmware. 

The gateway needs to do the following activities to support Firmware Action for itself and for the attached devices:

**1. Construct DeviceFirmware Object (Optional)**

In order to perform Firmware action, the gateway can optionally construct a DeviceFirmware object for itself and for attached devices and add it to DeviceData as follows:

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
	
	ManagedGateway ManagedGateway = new ManagedGateway(options, deviceData);
	managedGateway.connect();

And in the case of attached devices, the constructed DeviceData can be passed to the library while sending the manage request. i.e

.. code:: java

    managedGateway.sendDeviceManageRequest(typeId, deviceId, deviceData, lifetime, supportFirmwareActions, supportDeviceActions);

The DeviceFirmware object represents the current firmware of the gateway or attached device and will be used to report the status of the Firmware Download and Firmware Update actions to IBM Watson Internet of Things Platform. In case this DeviceFirmware object is not constructed by the gateway, the library creates an empty object and reports the status to Watson IoT Platform.

**2. Inform the server about the Firmware action support**

The gateway/attached devices needs to set the firmware action flag to true in order for the server to initiate the firmware request. This can be achieved by passing true value for supportFirmwareActions parameter while sending the manage request.

The gateway can invoke the following method to inform the server about its firmware support,

.. code:: java

    	managedGateway.sendGatewayManageRequest(3600, true, false);

Similarly, the gateway can invoke the corresponding device method to inform the firmware support of attached devices,

.. code:: java

    	managedGateway.sendDeviceManageRequest(typeId, deviceId, deviceData, 3600, true, false);

Once the support is informed to the DM server, the server then forwards the firmware actions to the gateway for the gateway itself/attached devices.

**3. Create the Firmware Action Handler**

In order to support the Firmware action, the gateway needs to create a handler and add it to managedGateway. The handler must extend a DeviceFirmwareHandler class and implement the following methods:

.. code:: java

	public abstract void downloadFirmware(DeviceFirmware deviceFirmware);
	public abstract void updateFirmware(DeviceFirmware deviceFirmware);

**Note**: There must be only one handler added to the library for both the gateway and attached devices where the firmware download/update requests will be redirected. The implementation must create a thread (possibly a pool of threads) to handle multiple firmware requests at the same time. A sample handler implementation with a threadpool is `demonstrated here <https://github.com/ibm-messaging/iot-gateway-samples/blob/master/java/advanced-gateway-sample/src/main/java/com/ibm/iotf/sample/gateway/GatewayFirmwareHandlerSample.java>`__ 

**3.1 Sample implementation of downloadFirmware**

The implementation must add a logic to download the firmware and report the status of the download via DeviceFirmware object. If the Firmware Download operation is successful, then the state of the firmware to be set to DOWNLOADED and UpdateStatus should be set to SUCCESS.

If an error occurs during Firmware Download the state should be set to IDLE and updateStatus should be set to one of the error status values:

* OUT_OF_MEMORY
* CONNECTION_LOST
* INVALID_URI

A sample Firmware Download implementation is shown below, (The below code doesn't include the threadpool part, refer to the `github sample <https://github.com/ibm-messaging/iot-gateway-samples/blob/master/java/advanced-gateway-sample/src/main/java/com/ibm/iotf/sample/gateway/GatewayFirmwareHandlerSample.java>`__  for the complete implementation of the FirmwareHandler).

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

Gateway can check the integrity of the downloaded firmware image using the verifier and report the status back to IBM Watson Internet of Things Platform. The verifier can be set by the gateway during the startup (while creating the DeviceFirmware Object) or as part of the Download Firmware request by the application. A sample code to verify the same is below:

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

The complete code can be found in the gateway management sample `GatewayFirmwareHandlerSample <https://github.com/ibm-messaging/iot-gateway-samples/blob/master/java/advanced-gateway-sample/src/main/java/com/ibm/iotf/sample/gateway/GatewayFirmwareHandlerSample.java>`__.

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

The complete code can be found in the gateway management sample `GatewayFirmwareHandlerSample <https://github.com/ibm-messaging/iot-gateway-samples/blob/master/java/advanced-gateway-sample/src/main/java/com/ibm/iotf/sample/gateway/GatewayFirmwareHandlerSample.java>`__.

**4. Add the handler to ManagedGateway**

The created handler needs to be added to the ManagedGateway instance so that the WIoTP client library invokes the corresponding method when there is a Firmware action request from IBM Watson Internet of Things Platform.

.. code:: java

	GatewayFirmwareHandlerSample fwHandler = new GatewayFirmwareHandlerSample();
	mgdGateway.addFirmwareHandler(fwHandler);
	
Refer to `this page <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/requests.html#/firmware-actions#firmware-actions>`__ for more information about the Firmware action.

----

Device Actions
------------------------------------
The IBM Watson Internet of Things Platform supports the following device actions:

* Reboot
* Factory Reset

The gateway needs to do the following activities to support Device Actions for itself and for the attached devices:

**1. Inform server about the Device Actions support**

In order to perform Reboot or Factory Reset action for itself and attached devices, the gateway needs to inform the IBM Watson Internet of Things Platform about the support first. This can be achieved by passing true value for supportDeviceActions parameter while sending the manage request.

The gateway can invoke the following method to inform the server about its device action support,

.. code:: java
	// Last parameter represents the device action support
    	managedGateway.sendGatewayManageRequest(3600, true, true);

Similarly, the gateway can invoke the corresponding device method to inform the device action support of attached devices,

.. code:: java
	// Last parameter represents the device action support
    	managedGateway.sendDeviceManageRequest(typeId, deviceId, 0, true, true);
    	
Once the support is informed to the DM server, the server then forwards the device action requests to the device.
	
**2. Create the Device Action Handler**

In order to support the device action, the gateway needs to create a handler and add it to managedGateway instance. The handler must extend a DeviceActionHandler class and provide implementation for the following methods:

.. code:: java

	public abstract void handleReboot(DeviceAction action);
	public abstract void handleFactoryReset(DeviceAction action);

**Note:** There must be only one handler added to the library for both the gateway and attached devices where the device action requests will be redirected. The implementation must create a thread (possibly a pool of threads) to handle multiple device action requests at the same time. A sample handler implementation with a threadpool is `demonstrated here <https://github.com/ibm-messaging/iot-gateway-samples/blob/master/java/advanced-gateway-sample/src/main/java/com/ibm/iotf/sample/gateway/GatewayActionHandlerSample.java>`__.

**2.1 Sample implementation of handleReboot**

The implementation must create a separate thread and add a logic to reboot the gateway/attached device and report the status of the reboot via DeviceAction object. Upon receiving the request, the gateway first needs to inform the server about the support(or failure) before proceeding with the actual reboot. And if the sample can not reboot the device or any other error during the reboot, the gateway can update the status along with an optional message. A sample reboot implementation for a Raspberry Pi device is shown below (The below code doesn't include the threadpool part, refer to the `github location <https://github.com/ibm-messaging/iot-gateway-samples/blob/master/java/advanced-gateway-sample/src/main/java/com/ibm/iotf/sample/gateway/GatewayActionHandlerSample.java>`__ for the complete implementation of a sample device action handler).

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

The complete code can be found in the device management sample `GatewayActionHandlerSample <https://github.com/ibm-messaging/iot-gateway-samples/blob/master/java/advanced-gateway-sample/src/main/java/com/ibm/iotf/sample/gateway/GatewayActionHandlerSample.java>`__.

**2.2 Sample implementation of handleFactoryReset**

The implementation must create a separate thread and add a logic to reset the gateway/attached device to factory settings and report the status of the reset via DeviceAction object. Upon receiving the request, the gateway first needs to inform the server about the support(or failure) before proceeding with the actual reset. The skeleton of the Factory Reset implementation is shown below:

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

**3. Add the handler to ManagedGateway**

The created handler needs to be added to the ManagedGateway instance so that the WIoTP client library invokes the corresponding method when there is a device action request for this gateway or attached devices, from IBM Watson Internet of Things Platform.

.. code:: java

	GatewayActionHandlerSample actionHandler = new GatewayActionHandlerSample();
	mgdGateway.addDeviceActionHandler(actionHandler);

Refer to `this page <https://docs.internetofthings.ibmcloud.com/devices/device_mgmt/requests.html#/device-actions-reboot#device-actions-reboot>`__ for more information about the Device Action.

----

Listen for Device attribute changes
-----------------------------------------------------------------

This WIoTP client library updates the corresponding objects whenever there is an update request from the IBM Watson Internet of Things Platform, these update requests are initiated by the application either directly or indirectly (Firmware Update) via the IBM Watson Internet of Things Platform ReST API. Apart from updating these attributes, the library provides a mechanism where the gateway can be notified whenever a device attribute is updated.

Attributes that can be updated by this operation are location, metadata, device information and firmware of the gateway/attached devices.

In order to get notified, the gateway needs to add a property change listener on those objects that it is interested.

.. code:: java

	deviceLocation.addPropertyChangeListener(listener);
	firmware.addPropertyChangeListener(listener);
	deviceInfo.addPropertyChangeListener(listener);
	metadata.addPropertyChangeListener(listener);
	
Also, the gateway needs to implement the propertyChange() method where it receives the notification. A sample implementation is as follows:

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
* `ManagedRasPiGateway <https://github.com/ibm-messaging/iot-gateway-samples/blob/master/java/gateway-samples/src/main/java/com/ibm/iotf/sample/client/gateway/devicemgmt/ManagedRasPiGateway.java>`__ - Gateway Device Management(DM) capabilities are demonstrated in this sample by managing the Arduino Uno device through the Raspberry Pi Gateway. If you do not have Raspberry Pi and Arduino UNO, don’t worry, you can still follow the sample to connect your device as a gateway and manage one or more attached devices. 
* `HomeGatewaySample <https://github.com/ibm-messaging/iot-gateway-samples/blob/master/java/advanced-gateway-sample/src/main/java/com/ibm/iotf/sample/gateway/HomeGatewaySample.java>`__ - A home gateway sample that manages few attached home devices like, Lights, Switches, Elevator, Oven and OutdoorTemperature.
* `GatewayFirmwareHandlerSample <https://github.com/ibm-messaging/iot-gateway-samples/blob/master/java/gateway-samples/src/main/java/com/ibm/iotf/sample/client/gateway/devicemgmt/GatewayFirmwareHandlerSample.java>`__ - A sample implementation of FirmwareHandler.
* `GatewayActionHandlerSample <https://github.com/ibm-messaging/iot-gateway-samples/blob/master/java/gateway-samples/src/main/java/com/ibm/iotf/sample/client/gateway/devicemgmt/GatewayActionHandlerSample.java>`__ - A sample implementation of DeviceActionHandler.

----

Recipe
----------

Refer to `the recipe <https://developer.ibm.com/recipes/tutorials/raspberry-pi-as-managed-gateway-in-watson-iot-platform-part-1/>`__ that shows how to connect Raspberry Pi as Managed Gateway to IBM Watson IoT Platform and manage the attached devices. For example, update Arduino Uno device with a new sketch program, reboot Arduino Uno using the Watson IoT Platform device management protocol and etc..
