package com.ibm.iotf.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.iotf.client.gateway.GatewayManagementTest;
import com.ibm.iotf.client.application.ApplicationEventSubscriptionTest;
import com.ibm.iotf.client.application.api.BulkAPIOperationsTest;
import com.ibm.iotf.client.device.DeviceCommandSubscriptionTest;
import com.ibm.iotf.client.device.DeviceEventPublishTest;
import com.ibm.iotf.client.device.DeviceManagementTest;
import com.ibm.iotf.client.gateway.GatewayCommandSubscriptionTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	DeviceManagementTest.class,
	DeviceEventPublishTest.class,
	DeviceCommandSubscriptionTest.class,
	GatewayCommandSubscriptionTest.class
	//BulkAPIOperationsTest.class
	//GatewayManagementTest.class,
	//ApplicationEventSubscriptionTest.class
})
public class TestSuite {

}
