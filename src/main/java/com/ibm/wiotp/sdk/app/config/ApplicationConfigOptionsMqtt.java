package com.ibm.wiotp.sdk.app.config;

public class ApplicationConfigOptionsMqtt {
	public int port;
	public String transport = "tcp"; // or "websockets"
	public String caFile; // Not sure we use this atm
	public boolean cleanStart;
	public int sessionExpiry;
	public int keepAlive;
	public boolean sharedSubscription;
	
	public ApplicationConfigOptionsMqtt() {
		port = 8883;
		transport = "tcp";
		caFile = null;
		cleanStart = true;
		sessionExpiry = 60;
		keepAlive = 60;
		sharedSubscription = false;
	}
	
	public ApplicationConfigOptionsMqtt(int port, String transport, String caFile, boolean cleanStart, int sessionExpiry, int keepAlive, boolean sharedSubscription) {
		this.port = port;
		this.transport = transport;
		this.caFile = caFile;
		this.cleanStart = cleanStart;
		this.sessionExpiry = sessionExpiry;
		this.keepAlive = keepAlive;
		this.sharedSubscription = sharedSubscription;
	}
	
	public static ApplicationConfigOptionsMqtt generateFromEnv() {
		ApplicationConfigOptionsMqtt mqtt = new ApplicationConfigOptionsMqtt();
		
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

		if (System.getenv("WIOTP_OPTIONS_MQTT_SHAREDSUBSCRIPTION") != null)
			mqtt.sharedSubscription = Boolean.parseBoolean(System.getenv("WIOTP_OPTIONS_MQTT_SHAREDSUBSCRIPTION"));
		
		
		return mqtt;
	}
}