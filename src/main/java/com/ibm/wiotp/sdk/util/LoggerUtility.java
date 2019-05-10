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
package com.ibm.wiotp.sdk.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class LoggerUtility {
	
	public static final String CLASS_NAME = LoggerUtility.class.getName();
	public static Logger LOGGER = null;
	
	static {
		InputStream stream = LoggerUtility.class.getClassLoader().getResourceAsStream("logging.properties");
	    try {
	    	LogManager.getLogManager().readConfiguration(stream);
	    	LOGGER = Logger.getLogger(CLASS_NAME);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	/* 
	 * These methods are all a bit pointless, but avoids a rewrite by keeping them here
	 * 
	 * Just use LoggerUtility.LOGGER directly (or even better use a per-class logger!)
	 */
	public static void log(Level level, String sourceClass, String sourceMethod, String message) {
		LOGGER.logp(level, sourceClass, sourceMethod, message);
	}
	
	public static void fine(String sourceClass, String sourceMethod, String message) {
		LOGGER.logp(Level.FINE, sourceClass, sourceMethod,  message);
	}
	
	public static void info(String sourceClass, String sourceMethod, String message) {
		LOGGER.logp(Level.INFO, sourceClass, sourceMethod, message);
	}
	
	public static void warn(String sourceClass, String sourceMethod, String message) {
		LOGGER.logp(Level.WARNING, sourceClass, sourceMethod, message);
	}
	
	public static void severe(String sourceClass, String sourceMethod, String message) {
		LOGGER.logp(Level.SEVERE, sourceClass, sourceMethod, message);
	}

	public static void log(Level level, String className, String method, String message, Throwable e) {
		LOGGER.logp(level, className, method, message, e);
	}

	public static boolean isLoggable(Level level) {
		return LOGGER.isLoggable(level);
	}


	


}
