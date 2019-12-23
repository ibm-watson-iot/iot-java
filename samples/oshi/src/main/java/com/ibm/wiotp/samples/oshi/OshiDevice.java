package com.ibm.wiotp.samples.oshi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.codecs.JsonCodec;
import com.ibm.wiotp.sdk.device.DeviceClient;
import com.ibm.wiotp.sdk.device.config.DeviceConfig;
import com.ibm.wiotp.sdk.device.config.DeviceConfigIdentity;
import com.ibm.wiotp.sdk.device.config.DeviceConfigOptions;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

public class OshiDevice {

	private static final Logger LOG = LoggerFactory.getLogger(OshiDevice.class);

	private static DeviceClient client;
	private static SystemInfo si = new SystemInfo();

	private static JsonObject createOshiData() {
		HardwareAbstractionLayer hal = si.getHardware();
		CentralProcessor processor = hal.getProcessor();
		double loadAverage = processor.getSystemCpuLoad() * 100;

		GlobalMemory memory = hal.getMemory();
		long availableMemory = memory.getAvailable();
		long totalMemory = memory.getTotal();
		long usedMemory = totalMemory - availableMemory;
		double memoryUtilization = (usedMemory / (double) totalMemory) * 100;

		JsonObject json = new JsonObject();
		json.addProperty("memory", memoryUtilization);
		json.addProperty("cpu", loadAverage);
		return json;
	}

	public static void main(String[] args) throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOG.info("Closing connection to IBM Watson IoT Platform ...");
				// Once told to stop, cleanly disconnect from the service
				client.disconnect();
				// Allow 3 seconds for disconnect to complete
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
				}
			}
		});

		DeviceConfig config = null;
		if (args.length > 0 && args[0].equals("--quickstart")) {
			SystemInfo si = new SystemInfo();
			String macAddress = si.getHardware().getNetworkIFs()[0].getMacaddr().replace(":", "");
			DeviceConfigIdentity identity = new DeviceConfigIdentity("quickstart", "iotsigar", macAddress);
			DeviceConfigOptions options = new DeviceConfigOptions();
			options.mqtt.port = 1883;

			config = new DeviceConfig(identity, null, options);
		} else {
			config = DeviceConfig.generateFromEnv();
		}

		client = new DeviceClient(config);
		client.registerCodec(new JsonCodec());

		LOG.info("IBM Watson IoT Platform OSHI Device Client");
		LOG.info("https://github.com/ibm-watson-iot/iot-java/tree/master/samples/oshi");
		LOG.info("");
		if (args.length > 0 && args[0].equals("--quickstart")) {
			LOG.info(
					"Welcome to IBM Watson IoT Platform Quickstart, view a vizualization of live data from this device at the URL below:");
			LOG.info("https://quickstart.internetofthings.ibmcloud.com/#/device/" + config.identity.deviceId
					+ "/sensor/");
			LOG.info("");
		}
		LOG.info("(Press <Ctrl+C> to quit)");

		client.connect();
		// Send a dataset every 1 second, until we are told to quit
		while (true) {
			JsonObject data = createOshiData();
			client.publishEvent("oshi", data, 0);
			Thread.sleep(5000);
		}

	}

}