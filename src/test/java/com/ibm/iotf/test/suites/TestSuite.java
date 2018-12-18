package com.ibm.iotf.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.iotf.client.gateway.GatewayManagementTest;
import com.ibm.iotf.client.application.ApplicationEventSubscriptionTest;
import com.ibm.iotf.client.application.api.BulkAPIOperationsTest;
import com.ibm.iotf.client.device.DeviceCommandSubscriptionTest;
import com.ibm.iotf.client.device.DeviceEventPublishTest;
import com.ibm.iotf.client.device.DeviceManagementTest1;
import com.ibm.iotf.client.device.DeviceManagementTest2;
import com.ibm.iotf.client.device.DeviceManagementTest3;
import com.ibm.iotf.client.device.DeviceManagementTest4;
import com.ibm.iotf.client.device.DeviceManagementTest5;
import com.ibm.iotf.client.device.DeviceManagementTest6;
import com.ibm.iotf.client.device.DeviceManagementTest7;
import com.ibm.iotf.client.gateway.GatewayCommandSubscriptionTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	DeviceManagementTest1.class,
	DeviceManagementTest2.class,
	DeviceManagementTest3.class,
	DeviceManagementTest4.class,
	DeviceManagementTest5.class,
	DeviceManagementTest6.class,
	DeviceManagementTest7.class,
	DeviceEventPublishTest.class,
	DeviceCommandSubscriptionTest.class,
	GatewayCommandSubscriptionTest.class
	//BulkAPIOperationsTest.class
	//GatewayManagementTest.class,
	//ApplicationEventSubscriptionTest.class
})
public class TestSuite {

}
