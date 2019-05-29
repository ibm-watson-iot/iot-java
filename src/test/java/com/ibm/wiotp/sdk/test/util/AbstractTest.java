package com.ibm.wiotp.sdk.test.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class);
	
	public static void logTestStart(String testName) {
		LOG.info("", "", "=========================================");
		LOG.info("", "", testName);
		LOG.info("", "", "=========================================");
	}
}
