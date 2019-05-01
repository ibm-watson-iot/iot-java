package com.ibm.iotf.test.common;

import com.ibm.wiotp.sdk.util.LoggerUtility;

public class AbstractTest {

	public static void logTestStart(String testName) {
		LoggerUtility.info("", "", "=========================================");
		LoggerUtility.info("", "", testName);
		LoggerUtility.info("", "", "=========================================");
	}
}
