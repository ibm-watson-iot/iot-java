/**
 *****************************************************************************
 Copyright (c) 2015-19 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 *
 */
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

