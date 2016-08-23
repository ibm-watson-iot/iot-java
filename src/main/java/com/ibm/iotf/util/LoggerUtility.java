/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.util;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtility {
	private static final String CLASS_NAME = LoggerUtility.class.getName();
	private static Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	public static void log(Level level, String sourceClass, 
			String sourceMethod, String message) {
		
		String threadName = Thread.currentThread().getName();
		LOGGER.logp(level, sourceClass, sourceMethod, threadName + ": "+ message);
	}
	
	public static void fine(String sourceClass, 
			String sourceMethod, String message) {
		
		String threadName = Thread.currentThread().getName();
		LOGGER.logp(Level.FINE, sourceClass, sourceMethod, threadName + ": "+ message);
	}
	
	public static void info(String sourceClass, 
			String sourceMethod, String message) {
		
		String threadName = Thread.currentThread().getName();
		LOGGER.logp(Level.INFO, sourceClass, sourceMethod, threadName + ": "+ message);
	}
	
	public static void warn(String sourceClass, 
			String sourceMethod, String message) {
		
		String threadName = Thread.currentThread().getName();
		LOGGER.logp(Level.WARNING, sourceClass, sourceMethod, threadName + ": "+ message);
	}
	
	public static void severe(String sourceClass, 
			String sourceMethod, String message) {
		
		String threadName = Thread.currentThread().getName();
		LOGGER.logp(Level.SEVERE, sourceClass, sourceMethod, threadName + ": "+ message);
	}

	public static void log(Level level, String className, String method,
			String message, Throwable e) {
		String threadName = Thread.currentThread().getName();
		LOGGER.logp(level, className, method, threadName + ": "+ message, e);
	}

	public static boolean isLoggable(Level level) {
		return LOGGER.isLoggable(level);
	}


}
