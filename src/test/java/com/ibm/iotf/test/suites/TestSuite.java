package com.ibm.iotf.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.iotf.client.gateway.GatewayManagementTest;
import com.ibm.iotf.client.application.ApplicationEventSubscriptionTest;
import com.ibm.iotf.client.application.api.BulkAPIOperationsTest;
import com.ibm.iotf.client.device.DeviceCommandSubscriptionTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	DeviceCommandSubscriptionTest.class,
	//BulkAPIOperationsTest.class
	//GatewayManagementTest.class,
	//ApplicationEventSubscriptionTest.class
})
public class TestSuite {

}
