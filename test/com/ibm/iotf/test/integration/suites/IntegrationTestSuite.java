package com.ibm.iotf.test.integration.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.iotf.client.test.TestAddDevice;
import com.ibm.iotf.client.test.TestGetDevice;
import com.ibm.iotf.client.test.TestGetDevices;
import com.ibm.iotf.client.test.TestHistoricalEvents;
import com.ibm.iotf.client.test.TestDeleteDevices;
import com.ibm.iotf.client.test.TestPublish;


@RunWith(Suite.class)
@SuiteClasses({TestAddDevice.class, TestGetDevice.class,TestGetDevices.class,TestPublish.class,TestHistoricalEvents.class,TestDeleteDevices.class})
public class IntegrationTestSuite {

}