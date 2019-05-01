package com.ibm.wiotp.sdk.test.util;

import com.ibm.wiotp.sdk.util.LoggerUtility;

public class AbstractTest {

	public static void logTestStart(String testName) {
		LoggerUtility.info("", "", "=========================================");
		LoggerUtility.info("", "", testName);
		LoggerUtility.info("", "", "=========================================");
	}
}
