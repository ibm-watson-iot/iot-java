package com.ibm.wiotp.sdk.device.config;

public class DeviceConfigOptionsMqtt {
	public int port;
	public String transport; 
	public String caFile;
	public boolean cleanStart;
	public int sessionExpiry;
	public int keepAlive;
	
	public DeviceConfigOptionsMqtt() {
		port = 8883;
		transport = "tcp"; // or "websockets"
		caFile = null;
		cleanStart = true;
		sessionExpiry = 60;
		keepAlive = 60;
	}
	
	public DeviceConfigOptionsMqtt(int port, String transport, String caFile, boolean cleanStart, int sessionExpiry, int keepAlive) {
		this.port = port;
		this.transport = transport;
		this.caFile = caFile;
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