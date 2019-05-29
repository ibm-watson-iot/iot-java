package com.ibm.wiotp.sdk.device.config;

public class DeviceConfigOptionsMqtt {
	public int port = 8883;
	public String transport = "tcp";  // or "websockets"
	public String caFile = null;
	public boolean cleanStart = true;
	public int sessionExpiry = 60;
	public int keepAlive = 60;
	
	public DeviceConfigOptionsMqtt() {
	}
	
	public DeviceConfigOptionsMqtt(int port, String transport, String caFile, boolean cleanStart, int sessionExpiry, int keepAlive) {
		this.port = port;
		
		if (transport != null) this.transport = transport;
		if (caFile != null) this.caFile = caFile;
		
		this.cleanStart = cleanStart;
		this.sessionExpiry = sessionExpiry;
		this.keepAlive = keepAlive;
	}
	
	public static DeviceConfigOptionsMqtt generateFromEnv() {
		DeviceConfigOptionsMqtt mqtt = new DeviceConfigOptionsMqtt();
		
		final String portNumber = System.getenv("WIOTP_OPTIONS_MQTT_PORT");
		if (portNumber != null)
			mqtt.port = Integer.parseInt(portNumber);
		
		if (System.getenv("WIOTP_OPTIONS_MQTT_TRANSPORT") != null)
			mqtt.transport = System.getenv("WIOTP_OPTIONS_MQTT_TRANSPORT");

		if (System.getenv("WIOTP_OPTIONS_MQTT_CAFILE") != null)
			mqtt.caFile = System.getenv("WIOTP_OPTIONS_MQTT_CAFILE");
		
		if (System.getenv("WIOTP_OPTIONS_MQTT_CLEANSTART") != null)
			mqtt.cleanStart = Boolean.parseBoolean(System.getenv("WIOTP_OPTIONS_MQTT_CLEANSTART"));
		
		if (System.getenv("WIOTP_OPTIONS_MQTT_SESSIONEXPIRY") != null)
			mqtt.sessionExpiry = Integer.parseInt(System.getenv("WIOTP_OPTIONS_MQTT_SESSIONEXPIRY"));

		if (System.getenv("WIOTP_OPTIONS_MQTT_KEEPALIVE") != null)
			mqtt.keepAlive = Integer.parseInt(System.getenv("WIOTP_OPTIONS_MQTT_KEEPALIVE"));

		return mqtt;
	}
}