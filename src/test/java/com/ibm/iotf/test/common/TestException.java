package com.ibm.iotf.test.common;

public class TestException extends Exception {

	private static final long serialVersionUID = 1111111L;
	
	public static final String MQTT_APP_CLIENT_NOT_INITIALIZED = "MQTT application client not initialized";
	
	private String message = null;
	
	public TestException(String message) {
		super();
		this.message = message;
	}
	
	public String getMessage() { return this.message; }
	
	public String getLocalizedMessage() { return getMessage(); }
	
	public String toString() { return TestException.class.getName() + ": " + this.message; }

}
