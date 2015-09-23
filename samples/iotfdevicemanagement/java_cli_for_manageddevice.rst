===============================================================================
Java Client Library - Managed Device (Update In Progress)
===============================================================================

Introduction
-------------------------------------------------------------------------------

This client library describes how to use devices with the Java ibmiotf client library. For help with getting started with this module, see `Java Client Library - Introduction <https://docs.internetofthings.ibmcloud.com/java/javaintro.html/>`__. 

This client library is divided into four sections, all included within the library. This section contains information on how devices can participate in various device management activities like, firmware update, location update, diagnostics update, device actions and etc..

The Device section contains information on how devices can publish events and handle commands using the Java ibmiotf Client Library. 

The Applications section contains information on how applications can use the Java ibmiotf Client Library to interact with devices. 

The Historian section contains information on how applications can use the Java ibmiotf Client Library to retrieve the historical information.

Device Management
-------------------------------------------------------------------------------
The `device management<managedDevices://docs.internetofthings.ibmcloud.com/reference/device_mgmt.html>`__ feature enhances the Internet of Things Foundation service with new capabilities for managing devices. It creates a distinction between managed and unmanaged devices,

* **Managed Devices** are defined as devices which have a management agent installed. The management agent sends and receives device metadata and responds to device management commands from the Internet of Things Foundation. 
* **Unmanaged Devices** are any devices which do not have a device management agent. All devices begin their lifecycle as unmanaged devices, and can transition to managed devices by sending a message from a device management agent to the Internet of Things Foundation. 

Create DeviceData
------------------------------------------------------------------------
The `device model <https://docs.internetofthings.ibmcloud.com/reference/device_model.html>`__ describes the metadata and management characteristics of a device. The device database in the Internet of Things Foundation is the master source of device information. Applications and managed devices are able to send updates to the database such as a location or the progress of a firmware update. Once these updates are received by the Internet of Things Foundation, the device database is updated, making the information available to applications.

The device model on the Device is represented as DeviceData and to create a DeviceData one needs to create the following objects,

* DeviceInfo (mandatory)
* DeviceLocation (optional)
* DeviceDiagnostic (optional)
* DeviceFirmware (optional)

The following code snippet shows how to create the DeviceInfo Object with sample data,

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

The following code snippet shows how to create the DeviceData Object with the above created DeviceInfo object,

.. code:: java

	DeviceData deviceData = new DeviceData.Builder().
				 deviceInfo(deviceInfo).
				 metadata(new JsonObject()).
				 build();
Construct ManagedDevice
-------------------------------------------------------------------------------
ManagedDevice - A device class that connects the device as managed device to Internet of Things Foundation server and enables the device to perform one or more Device Management operations. Also the ManagedDevice instance can be used to do normal device operations like publishing device events and listening for commands from application.

ManagedDevice exposes 2 different constructors to support different user patterns, 

**Constructor#1**

Constructs a ManagedDevice instance by accepting the DeviceData and the following properties,

* Organization-ID - Your organization ID.
* Device-Type - The type of your device.
* Device-ID - The ID of your device.
* Authentication-Method - Method of authentication (The only value currently supported is "token"). 
* Authentication-Token - API key token

The Properties object creates definitions which are used to interact with the Internet of Things Foundation module. 

The following code shows how to create a ManagedDevice instance,

.. code:: java

	Properties options = new Properties();
	options.setProperty("Organization-ID", "organization");
	options.setProperty("Device-Type", "deviceType");
	options.setProperty("Device-ID", "deviceId");
	options.setProperty("Authentication-Method", "authMethod");
	options.setProperty("Authentication-Token", "authToken");
	
	ManagedDevice managedDevice = new ManagedDevice(options, deviceData);
 
The existing users of DeviceClient might observe that the names of these properties are slightly changed. These names are changed to mirror the names in the Internet of Things Foundation Dashboard, but the existing users who wants to migrate from the DeviceClient to ManagedDevice can still use the old format and construct the ManagedDevice instance as follows,

.. code:: java

	Properties options = new Properties();
	options.setProperty("org", "organization");
	options.setProperty("type", "deviceType");
	options.setProperty("id", "deviceId");
	options.setProperty("auth-method", "authMethod");
	options.setProperty("auth-token", "authToken");
	ManagedDevice managedDevice = new ManagedDevice(options, deviceData);

**Constructor#2**

Construct a ManagedDevice instance by accepting the DeviceData and the MqttClient instance. And this constructor requires the DeviceData to be created with additional device attributes like Device Type and Device Id as follows,

.. code:: java
	
	// Code that constructs the MqttClient (either Synchronous or Asynchronous MqttClient)
	.....
	
	// Code that constructs the DeviceData
	DeviceData deviceData = new DeviceData.Builder().
				 typeId("Device-Type").
				 deviceId("Device-ID").
				 deviceInfo(deviceInfo).
				 metadata(new JsonObject()).
				 build();
	
	....
	ManagedDevice managedDevice = new ManagedDevice(mqttClient, deviceData);
	
Note this constructor helps the custom device users to create ManagedDevice instance with the already created and connected MqttClient instance to take advantage of device management operations. But we recommend the users to use the library for all the device functionalities.

Manage	
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
In order to participate in device management activities, the device needs to send a manage request to Internet of Things Foundation. The connect() method on ManagedDevice implicitly sends a manage request to connect the device as Managed device.

.. code:: java

	managedDevice.connect(3600);
	
Actually the connect() method does the following,

* Connects the device to Internet of Things Foundation and
* Sends the manage request such that the device becomes an Managed Device.

The overloaded connect(long) method can be used to register the device with lifetime. The lifetime specifies the length of time within which the device must send another **Manage device** request in order to avoid being reverted to an unmanaged device and marked as dormant.

.. code:: java

	managedDevice.connect(3600);

Also, the manage(long) method can be used to send the manage request to Internet of Things Foundation at any point - The custom device clients that pass the MqttClient instance, must explicitly invoke the manage() method to participate in device management activities.

.. code:: java

	managedDevice.manage(4800);
	
Unmanage
-----------------------------------------------------

A device can invoke unmanage() method when it no longer needs to be managed. The Internet of Things Foundation will no longer send new device management requests to this device and all device management requests from this device will be rejected other than a **Manage device** request.

.. code:: java

	managedDevice.unmanage();

Refer to the `documenation <https://docs.internetofthings.ibmcloud.com/device_mgmt/operations/manage.html>`__ for more information about the manage operation.

Location Update
-----------------------------------------------------

Devices that can determine their location can choose to notify the Internet of Things Foundation device management server about location changes. In order to update the location, the device needs to create DeviceData instance with the DeviceLocation object.

.. code:: java
    // Construct the location object with latitude, longitude and elevation
    DeviceLocation deviceLocation = new DeviceLocation.Builder(30.28565, -97.73921).
								elevation(10).
								build();
    DeviceData deviceData = new DeviceData.Builder().
				 deviceInfo(deviceInfo).
				 deviceLocation(deviceLocation).
				 metadata(new JsonObject()).
				 build();
	
    
Once the device is connected to Internet of Things Foundation, the location can be updated by invoking the following method,

.. code:: java

	deviceLocation.sendLocation();

Later, any new location can be easily updated by changing the properties of the DeviceLocation object,

.. code:: java

	deviceLocation.update(40.28, -98.33, 11);
	if(rc == 200) {
		System.out.println("Current location (" + deviceLocation.toString() + ")");
	} else {
		System.err.println("Failed to update the location");
	}

Listening for Location change
-----------------------------

As the location of the device can be updated using the the Internet of Things Foundation REST API, the library code updates the DeviceLocation object whenever it receives the update from the Internet of Things Foundation. The device can listen for such a location change by adding itself as a property change listener in DeviceLocation object and query the properties whenever the location is changed.

.. code:: java

	// Add a listener for location change
	location.addPropertyChangeListener(this);
	
	...
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println("Received a new location - "+evt.getNewValue());
	}

Refer to the `documenation <https://docs.internetofthings.ibmcloud.com/device_mgmt/operations/update.html>`__ for more information about the Location update.

Append/Clear ErrorCodes
-----------------------------------------------

Devices can choose to notify the Internet of Things Foundation device management server about changes in their error status. In order to send the ErrorCodes the device needs to construct a DeviceDiagnostic object as follows,

.. code:: java

	DiagnosticErrorCode errorCode = new DiagnosticErrorCode(0);
	DeviceDiagnostic diag = new DeviceDiagnostic(errorCode);
	this.deviceData = new DeviceData.Builder().
				 deviceInfo(deviceInfo).
				 deviceDiag(diag).
				 metadata(new JsonObject()).
				 build();

Once the device is connected to Internet of Things Foundation, the ErrorCode can be sent by calling the sendErrorCode() method as follows,

.. code:: java

	diag.sendErrorCode();

Later, any new ErrorCodes can be easily added to the Internet of Things Foundation server by calling the append method as follows,

.. code:: java

	int rc = diag.append(random.nextInt(500));
	if(rc == 200) {
		System.out.println("Current Errorcode (" + diag.getErrorCode() + ")");
	} else {
		System.out.println("Errorcode addition failed!");
	}

Also, the ErrorCodes can be cleared from Internet of Things Foundation server by calling the clear method as follows,

.. code:: java

	int rc = diag.clearErrorCode();
	if(rc == 200) {
		System.out.println("ErrorCodes are cleared successfully!");
	} else {
		System.out.println("Failed to clear the ErrorCodes!");
	}

Append/Clear Log messages
-----------------------------
Devices can choose to notify the Internet of Things Foundation server about changes by adding a new log entry. Log entry includes a log messages, its timestamp and severity, as well as an optional base64-encoded binary diagnostic data. In order to send log messages, the device needs to construct a DeviceDiagnostic object as follows,

.. code:: java

	DiagnosticLog log = new DiagnosticLog(
				"Simple Log Message", 
				new Date(),
				DiagnosticLog.LogSeverity.informational);
		
	DeviceDiagnostic diag = new DeviceDiagnostic(log);
	
	this.deviceData = new DeviceData.Builder().
				 deviceInfo(deviceInfo).
				 deviceDiag(diag).
				 metadata(new JsonObject()).
				 build();

Once the device is connected to Internet of Things Foundation, the log message can be sent by calling the sendLog() method as follows,

.. code:: java

	diag.sendLog();

Later, any new log messages can be easily added to the Internet of Things Foundation server by calling the append method as follows,

.. code:: java

	int rc = diag.append("Log event " + count++, new Date(), 
				DiagnosticLog.LogSeverity.informational);
			
	if(rc == 200) {
		System.out.println("Current Log (" + diag.getLog() + ")");
	} else {
		System.out.println("Log Addition failed");
	}

Also, the ErrorCodes can be cleared from Internet of Things Foundation server by calling the clear method as follows,

.. code:: java

	rc = diag.clearLog();
	if(rc == 200) {
		System.out.println("Logs are cleared successfully");
	} else {
		System.out.println("Failed to clear the Logs")
	}	

The device diagnostics operations are intended to provide information on device errors, and does not provide diagnostic information relating to the devices connection to the Internet of Things Foundation.

Refer to the `documentation <https://docs.internetofthings.ibmcloud.com/device_mgmt/operations/diagnostics.html>`__ for more information about the Diagnostics operation.

Firmware Actions
-------------------------------------------------------------
The firmware update process is separated into two distinct actions, Downloading Firmware, and Updating Firmware.

**Construct DeviceFirmware Object**

In order to perform Firmware actions the device needs to construct the DeviceFirmware object and add it to DeviceData as follows,

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
				metadata(new JsonObject()).
				build();
	
	ManagedDevice managedDevice = new ManagedDevice(options, deviceData);
	managedDevice.connect();
		

The DeviceFirmware object represents the current firmware of the device and will be used to report the status of the Firmware Download and Firmware Update actions to Internet of Things Foundation server.

**Inform the Firmware action support**

The device needs to set the firmware action flag to true in order for the server to initiate the firmware request. This can be achieved by invoking a following method with a boolean value,

.. code:: java
	
	ManagedDevice managedDevice = new ManagedDevice(options, deviceData);
	managedDevice.supportsFirmwareActions(true);
	managedDevice.connect();
	
Note that the supportsFirmwareActions() method to be called before the connect() method as the ManagedDevice sends a manage request as part of the connect() method. As part of manage request the ibmiotf client library informs the Internet Of Things Server about the firmware action support and hence it needs to be added prior to calling connect() method.

Alternatively, the support can be added later as well followed by the manage request as follows,

.. code:: java

	ManagedDevice managedDevice = new ManagedDevice(options, deviceData);
    	managedDevice.connect();
    	...
    	managedDevice.supportsFirmwareActions(true);
    	managedDevice.manage(3600);
	
**Defining the Firmware Action Handler**

In order to support the Firmware action, the device needs to create a handler and add it to ManagedDevice. The handler must extend a DeviceFirmwareHandler class and implement the following methods,

.. code:: java

	public abstract void downloadFirmware(DeviceFirmware deviceFirmware);
	public abstract void updateFirmware(DeviceFirmware deviceFirmware);

**Sample implementation of downloadFirmware**

The implementation must report the status of the Firmware Download via DeviceFirmware object. If the Firmware Download operation is successful, then the state of the firmware to be set to DOWNLOADED and UpdateStatus should be set to SUCCESS. If an error occurs during Firmware Download the state should be set to IDLE and updateStatus should be set to one of the error status values,
    * OUT_OF_MEMORY
    * CONNECTION_LOST
    * INVALID_URI
			
A sample Firmware Download implementation for a Raspberry Pi device is shown below,

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

**Sample implementation of updateFirmware**

The implementation must report the status of the Firmware Update via DeviceFirmware object. If the Firmware Update operation is successful, then the state of the firmware should to be set to IDLE and UpdateStatus should be set to SUCCESS. 

If an error occurs during Firmware Update, updateStatus should be set to one of the error status values,
    * OUT_OF_MEMORY
    * UNSUPPORTED_IMAGE
			
A sample Firmware Update implementation for a Raspberry Pi device is shown below,

.. code:: java
	
	public void updateFirmware(DeviceFirmware deviceFirmware) {
		try {
			ProcessBuilder pkgInstaller = null;
			Process p = null;
			pkgInstaller = new ProcessBuilder("sudo", "dpkg", "-i", this.downloadedFirmwareName);
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


**Adding the handler to ManagedDevice**

The created handler needs to be added to the ManagedDevice instance so that the ibmiotf client library invokes the corresponding method when there is a Firmware action request from Internet of Things Foundation server.

.. code:: java

	DeviceFirmwareHandlerSample fwHandler = new DeviceFirmwareHandlerSample();
	deviceData.addFirmwareHandler(fwHandler);

Refer to `this page <https://docs.internetofthings.ibmcloud.com/device_mgmt/operations/firmware_actions.html>`__ for more information about the Firmware action.

Device Actions
------------------------------------
The Internet of Things Foundation supports the following device actions,

* Reboot
* Factory Reset

In order to perform Reboot and Factory Reset the device needs to inform the Internet of Things server about its support first. This can achieved by invoking a following method with a boolean value,

.. code:: java
	
	ManagedDevice managedDevice = new ManagedDevice(options, deviceData);
	managedDevice.supportsDeviceActions(true);
	managedDevice.connect();
	
Note that the supportsDeviceActions() method to be called before the connect() method as the ManagedDevice sends a manage request as part of the connect() method. As part of manage request the ibmiotf client library informs the Internet Of Things Server about the device action support and hence it needs to be added prior to calling connect() method.

Alternatively, the support can be added later as well followed by the manage request as follows,

.. code:: java

	ManagedDevice managedDevice = new ManagedDevice(options, deviceData);
    	managedDevice.connect();
    	...
    	managedDevice.supportsDeviceActions(true);
    	managedDevice.manage(3600);
	
**Creating the Device Action Handler**

In order to support the device action, the device needs to create a handler and add it to ManagedDevice. The handler must extend a DeviceActionHandler class and provide implementation for the following methods,

.. code:: java

	public abstract void handleReboot(DeviceAction action);
	public abstract void handleFactoryReset(DeviceAction action);

**Sample implementation of handleReboot**

The implementation must set the status of the reboot operation along with a optional message when there is a failure. The DeviceAction object to be used to update the status of the reboot operation. A sample reboot implementation for a Raspberry Pi device is shown below,

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


**Sample implementation of handleFactoryReset**

Similar to handleReboot() method, the implementation must set the status of the factory reset operation along with a optional message when there is a failure. The skeleton of the Factory Reset implementation is shown below,

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

**Adding the handler to ManagedDevice**

The created handler needs to be added to the ManagedDevice instance so that the ibmiotf client library invokes the corresponding method when there is a device action request from Internet of Things Foundation server.

.. code:: java

	DeviceActionHandlerSample actionHandler = new DeviceActionHandlerSample();
	deviceData.addDeviceActionHandler(actionHandler);

Refer to `this page <https://docs.internetofthings.ibmcloud.com/device_mgmt/operations/device_actions.html>`__ for more information about the Device Action.

Examples
-------------
* SampleRasPiDMAgent - A sample agent code that shows how to perform various device management operations on Raspberry Pi
* SampleRasPiManagedDevice - A sample code that shows how one can perform both device operations and management operations
* SampleRasPiDMAgentWithCustomMqttAsyncClient - A sample agent code with custom MqttAsyncClient
* SampleRasPiDMAgentWithCustomMqttClient - A sample agent code with custom MqttClient
* RasPiFirmwareHandlerSample - A sample implementation of FirmwareHandler for Raspberry Pi
* DeviceActionHandlerSample - A sample implementation of DeviceActionHandler
* ManagedDeviceWithLifetimeSample - A sample that shows how to send regular manage request with lifetime specified
* LocationUpdateListenerSample - A sample that shows how to listen for a location update message from the IoT Foundation server 
* NonBlockingDiagnosticsErrorCodeUpdateSample - A sample that shows how to add ErrorCode without waiting for response from the server

Recipe
----------

A recipe that shows how to connect the Raspberry Pi device as managed device to Internet Of Things Foundation to perform various device management operations in step by step using this client library.
