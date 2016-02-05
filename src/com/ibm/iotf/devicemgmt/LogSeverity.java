package com.ibm.iotf.devicemgmt;

/**
 * 
 * <p> Provides the possible Log severities </p>
 */
public enum LogSeverity {
	informational(0), warning(1), error(2);
	
	private final int severity;
	
	private LogSeverity(int severity) {
		this.severity = severity;
	}
	
	public int getSeverity() {
		return severity;
	}
}

