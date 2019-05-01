package com.ibm.wiotp.sdk.device.config;

public class DeviceConfigOptionsMqtt {
	public int port;
	public String transport = "tcp"; // or "websockets"
	public String caFile;
	public boolean cleanStart;
	public int sessionExpiry;
	public int keepAlive;
	
	public DeviceConfigOptionsMqtt() {}
	
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
		mqtt.port = Integer.parseInt(System.getenv("WIOTP_OPTIONS_MQTT_PORT"));
		mqtt.transport = System.getenv("WIOTP_OPTIONS_MQTT_TRANSPORT");
		mqtt.caFile = System.getenv("WIOTP_OPTIONS_MQTT_CAFILE");
		mqtt.cleanStart = Boolean.parseBoolean(System.getenv("WIOTP_OPTIONS_MQTT_CLEANSTART"));
		mqtt.sessionExpiry = Integer.parseInt(System.getenv("WIOTP_OPTIONS_MQTT_SESSIONEXPIRY"));
		mqtt.keepAlive = Integer.parseInt(System.getenv("WIOTP_OPTIONS_MQTT_KEEPALIVE"));
		
		return mqtt;
	}
}