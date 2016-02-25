package com.ibm.iotf.sample.devicemgmt.gateway.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;
import com.ibm.iotf.sample.client.SystemObject;
import com.ibm.iotf.sample.client.gateway.GatewayCommandCallback;
import com.ibm.iotf.sample.devicemgmt.gateway.GatewayActionHandlerSample;
import com.ibm.iotf.sample.devicemgmt.gateway.GatewayFirmwareHandlerSample;
import com.ibm.iotf.sample.devicemgmt.gateway.home.Device.DeviceType;
import com.ibm.iotf.sample.util.Utility;

/**
 * Gateways are a specialized class of devices in Watson IoT Platform which serve as access points 
 * to the Watson IoT Platform for other devices. Gateway devices have additional permission when 
 * compared to regular devices and can perform the following  functions:
 * 
 * 1. Register new devices to Watson IoT Platform
 * 2. Send and receive its own sensor data like a directly connected device,
 * 3. Send and receive data on behalf of the devices connected to it
 * 4. Run a device management agent, so that it can be managed, also manage the devices connected to it
 * 
 * In this sample we demonstrate a sample home gateway that manages few attached home devices like,
 * Lights, Switchs, Elevator, Oven and OutdoorTemperature. And the following configuration is assumed,
 *     
 * 1. Few devices are not manageable
 * 2. Few devices are manageable but accept only firmware
 * 3. Few devices are manageable but accept only Device actions
 * 4. Few devices are manageable and accept both firmware/device actions
 * 
 * All devices publish events and few devices accept commands. 
 * 
 *
 */
public class HomeGatewaySample {
	private final static String PROPERTIES_FILE_NAME = "DMGatewaySample.properties";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";
	
	// Threadpool used to read and send the attached device events to the Watson IoT Platform.
	// One can increase the number of threads when more & more devices are attached and need to
	// send in lesser frequent interval.
	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
	
	private ManagedGateway mgdGateway;
	private APIClient apiClient;
	private String registrationMode;
	private static List<Device> attachedDevices;
	
	/**
	 * This method creates a ManagedGateway instance by passing the required
	 * properties and connects the Gateway to the Watson IoT Platform by calling
	 * the connect function.
	 * 
	 * After the successful connection to the Watson IoT Platform, the Gateway
	 * can perform the following operations, 1. Publish events for itself and on
	 * behalf of devices connected behind the Gateway 2. Subscribe to commands
	 * for itself and on behalf of devices behind the Gateway 3. Send a manage
	 * request so that it can patricipate in the Device Management activities
	 */
	private void init(String propertiesFile) throws Exception {

		/**
		 * Load device properties
		 */
		Properties deviceProps = Utility.loadPropertiesFile(PROPERTIES_FILE_NAME, propertiesFile);

		/**
		 * Let us create the DeviceData object with the DeviceInfo object
		 */
		DeviceInfo deviceInfo = new DeviceInfo.Builder()
				.serialNumber(trimedValue(deviceProps.getProperty("DeviceInfo.serialNumber")))
				.manufacturer(trimedValue(deviceProps.getProperty("DeviceInfo.manufacturer")))
				.model(trimedValue(deviceProps.getProperty("DeviceInfo.model")))
				.deviceClass(trimedValue(deviceProps.getProperty("DeviceInfo.deviceClass")))
				.description(trimedValue(deviceProps.getProperty("DeviceInfo.description")))
				.fwVersion(trimedValue(deviceProps.getProperty("DeviceInfo.swVersion")))
				.hwVersion(trimedValue(deviceProps.getProperty("DeviceInfo.hwVersion")))
				.descriptiveLocation(trimedValue(deviceProps.getProperty("DeviceInfo.descriptiveLocation"))).build();

		DeviceData deviceData = new DeviceData.Builder().deviceInfo(deviceInfo).build();

		this.registrationMode = deviceProps.getProperty("Registration-Mode");
		if (!(this.registrationMode.equalsIgnoreCase(RegistrationMode.MANUAL.getRegistrationMode())
				|| this.registrationMode.equalsIgnoreCase(RegistrationMode.AUTOMATIC.toString()))) {
			throw new Exception("RegistrationMode not valid");
		}

		mgdGateway = new ManagedGateway(deviceProps, deviceData);

		// Connect to Watson IoT Platform
		mgdGateway.connect();

		// We need to create APIclint to register the device type of Arduino Uno
		// device, if its not registered already
		Properties options = new Properties();
		options.put("Organization-ID", deviceProps.getProperty("Organization-ID"));
		options.put("id", "app" + (Math.random() * 10000));
		options.put("Authentication-Method", "apikey");
		options.put("API-Key", deviceProps.getProperty("API-Key"));
		options.put("Authentication-Token", deviceProps.getProperty("API-Token"));

		this.apiClient = new APIClient(options);

		initDevicesBehindGateway();
	}

	private String trimedValue(String value) {
		if (value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}

	/**
	 * Init the attached devices, not all the devices are manageable, So the gateway will have the following
	 * configuration.
	 * 
	 * 1. Few devices are not manageable
	 * 2. Few devices are manageable but accept only firmware
	 * 3. Few devices are manageable but accept only Device actions
	 * 4. Few devices are manageable and accept both firmware/device actions
	 * 
	 * All devices publish events and few devices accept commands. 
	 */
	private void initDevicesBehindGateway() {
		Device oven = new Oven("MyOven", this.mgdGateway, 3);
		oven.setManagable(true, true);
		oven.sendLog(LogSeverity.informational, "Oven connected to Watson IoT", null, new Date());
		oven.sendLog(LogSeverity.warning, "Warning 14625: inconsistent state", null, new Date());

		Device light1 = new Light("LightDeviceActionManagable", this.mgdGateway, 8);
		light1.setManagable(false, true);

		Device light2 = new Light("LightNotManageable", this.mgdGateway, 12);
		light2.setManagable(false, false);

		Device elevator = new Elevator("MainElevator", this.mgdGateway, 5);
		elevator.setManagable(true, true);
		elevator.sendErrorCode(0);

		Device switch1 = new Switch("Hall-Swicth", this.mgdGateway, 12);
		Device switch2 = new Switch("Kitchen-Swicth", this.mgdGateway, 17);
		Device switch3 = new Switch("MasterRoom-Swicth", this.mgdGateway, 30);

		Device temperatureSensor = new Temperature("OutdoorTemperature", this.mgdGateway, 2);

		attachedDevices = new ArrayList<Device>();
		attachedDevices.add(oven);
		attachedDevices.add(light1);
		attachedDevices.add(light2);
		attachedDevices.add(elevator);
		attachedDevices.add(switch1);
		attachedDevices.add(switch2);
		attachedDevices.add(switch3);
		attachedDevices.add(temperatureSensor);
	}

	private void disconnect() {
		// Disconnect cleanly
		mgdGateway.disconnect();
	}

	public static void main(String[] args) throws Exception {

		System.out.println("Starting the Managed Gateway...");
		String fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);

		HomeGatewaySample sample = new HomeGatewaySample();
		try {
			sample.init(fileName);

			// for manual registration mode only
			if (sample.getRegistrationMode().equalsIgnoreCase(RegistrationMode.MANUAL.getRegistrationMode())) {
				System.out.println("<-- Registring the devices manually if not added already");
				// manually register all devicetypes that are needed (if they do
				// not exist yet) with api key
				for (DeviceType deviceType : DeviceType.values()) {
					System.out.println("<-- Adding deviceType " + deviceType.getDeviceType());
					String dt = deviceType.getDeviceType();
					try {
						JsonObject joDt = sample.apiClient.getDeviceType(dt);
						if (!joDt.isJsonNull()) {
							// device type already exist in iotf
							continue;
						}
						// device type has to be created in iotf

					} catch (IoTFCReSTException e) {
						if (e.getHttpCode() == 404) {
							sample.apiClient.addDeviceType(dt, dt, null, null);
						} else {
							System.out.println("ERROR: unable to add manually device type " + deviceType.toString());
						}
					}
				}

				// manually register all devices that are needed (if they do not
				// exist yet) with gateway credentials
				for (Device device : attachedDevices) {
					try {
						System.out.println("<-- Adding device "+device.getDeviceId());
						sample.mgdGateway.api().getDevice(device.getDeviceType(), device.getDeviceId());
					} catch (IoTFCReSTException ex) {
						if (ex.getHttpCode() == 404) {
							sample.mgdGateway.api().registerDeviceUnderGateway(device.getDeviceType(), device.getDeviceId(),
									sample.mgdGateway.getGWTypeId(), sample.mgdGateway.getGWDeviceId());

						}
					}
				}
			}

			// add command callback for these devices or gateway
			sample.addCommandCallback();
			
			// can also not be managed
			sample.mgdGateway.sendGatewayManageRequest(0, true, true);
			
			/**
			 * Update a random Gateway Location. In reality, you may get the location of the Gateway/attached 
			 * devices using the GPS and update it to Watson IoT Platform.
			 */
			sample.updateGatewayLocation();
			
			
			// Initialize a firmware handler that handles the firmware update for the Gateway and 
			// attached devices that supports firmware actions
			GatewayFirmwareHandlerSample fwHandler = new GatewayFirmwareHandlerSample();
			fwHandler.setGateway(sample.mgdGateway);
			
			// Initialize a device action handler that handles the reboot or reset request for the Gateway and 
			// attached devices
			GatewayActionHandlerSample actionHandler = new GatewayActionHandlerSample();
			actionHandler.setGatewayDeviceId(sample.mgdGateway.getGWDeviceId());
			
			// schedule a thread to send these attached device events at different interval
			sample.scheduleDeviceEvents();
			
			// set devices as managable where needed
			for (Device device : attachedDevices) {
				if (device.isManagable() && !device.isManaged()) {
					System.out.println("<-- Sending manage request for device .." + device.getDeviceId());
					sample.mgdGateway.sendDeviceManageRequest(device.getDeviceType(), device.getDeviceId(), 0,
							device.isFirmwareAction(), device.isDeviceAction());
					device.setManaged(true);
					// add the entry into the firmwarehandler if the device supports Firmware update
					if(device.isFirmwareAction()) {
						fwHandler.addDeviceInterface(device.getDeviceId(), device);
					}
					if(device.isDeviceAction()) {
						actionHandler.addDeviceInterface(device.getDeviceId(), device);
					}
				}
			}

			// Add the firmware and device action handler to Gateway
			sample.mgdGateway.addFirmwareHandler(fwHandler);
			sample.mgdGateway.addDeviceActionHandler(actionHandler);
			
			/**
			 * While the gateway publishes events on behalf of the attached devices, the gateway 
			 * can publish its own events as well. 
			 * 
			 * The sample publishes a blink event every 5 seconds, that has the CPU and memory utilization of 
			 * this sample Gateway process.
			 */
			SystemObject obj = new SystemObject();
			while (true) {
				//Generate a JSON object of the event to be published
				JsonObject event = new JsonObject();
				event.addProperty("name", SystemObject.getName());
				try {
					event.addProperty("cpu",  obj.getProcessCpuLoad());
				} catch (MalformedObjectNameException e) {
					e.printStackTrace();
				} catch (InstanceNotFoundException e) {
					e.printStackTrace();
				} catch (ReflectionException e) {
					e.printStackTrace();
				}
				event.addProperty("mem",  obj.getMemoryUsed());
					
				sample.mgdGateway.publishGatewayEvent("blink", event);
				System.out.println("<--(GW) Gateway event :: "+event);
				Thread.sleep(5000);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.err.flush();
		} finally {
			sample.disconnect();
		}

		System.out.println(" Exiting...");
	}
	
	/**
	 * A method that updates a random location for the Gateway
	 */
	private void updateGatewayLocation() {
		Random random = new Random();
		// ...update location
		double latitude = random.nextDouble() + 30;
		double longitude = random.nextDouble() - 98;
		double elevation = (double)random.nextInt(100);
		int rc = mgdGateway.updateGatewayLocation(latitude, longitude, elevation);
		if(rc == 200) {
			System.out.println("Updated random location (" + latitude + " " + longitude +" " + elevation + ") for Gateway");
		} else {
			System.err.println("Failed to update the location (" + 
					latitude + " " + longitude +" " + elevation + "), rc ="+rc);
		}
	}
	
	private void scheduleDeviceEvents() {
		for (Device device : attachedDevices) {
			scheduledThreadPool.scheduleAtFixedRate(device, 0, 
					device.getEventUpdateInterval(), TimeUnit.SECONDS);
		}
		
	}
	

	private String getRegistrationMode() {
		return this.registrationMode;
	}
	
	/**
	 * When the GatewayClient connects, it automatically subscribes to any commands for this Gateway. 
	 * But to subscribe to commands for the devices connected to the Gateway, one needs to use the 
	 * subscribeToDeviceCommands() method from the GatewayClient class. 
	 * 
	 * To receive and process the commands for the attached devices, the Gateway sample does the following,
	 *   1. Adds a command callback method
	 *   2. Subscribes to commands for the attached device
	 *   
	 * The callback method processCommand() is invoked by the GatewayClient when it receives any command 
	 * for the attached devices from Watson IoT Platform. The Gateway CommandCallback defines a BlockingQueue 
	 * to store and process the commands (in separate thread) for smooth handling of MQTT publish message.
	 */
	private void addCommandCallback() {
		System.out.println("<-- Subscribing to commands for all the devices..");
		GatewayCommandCallback callback = new GatewayCommandCallback();
		mgdGateway.setGatewayCallback(callback);
		for (Device device : attachedDevices) {
			mgdGateway.subscribeToDeviceCommands(device.getDeviceType(), device.getDeviceId());
			callback.addDeviceInterface(device.getDeviceId(), device);
		}
		
		try {
			Thread t = new Thread(callback);
			t.start();
		} catch(Exception | Error e) {
			e.printStackTrace();
		}
		
	}
	
	private enum RegistrationMode {
		MANUAL("manual"), AUTOMATIC("automatic");

		private String registrationMode;

		RegistrationMode(String registrationMode) {
			this.registrationMode = registrationMode;
		}

		public String getRegistrationMode() {
			return this.registrationMode;
		}
	}
}
