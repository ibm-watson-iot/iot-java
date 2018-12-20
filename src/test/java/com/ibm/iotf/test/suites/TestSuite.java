package com.ibm.iotf.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.iotf.client.gateway.GatewayManagementTest;
import com.ibm.iotf.client.application.ApplicationEventSubscriptionTest;
import com.ibm.iotf.client.application.ApplicationCommandStatusSubscriptionTest1;
import com.ibm.iotf.client.application.ApplicationEventStatusSubscriptionTest1;
import com.ibm.iotf.client.application.ApplicationEventStatusSubscriptionTest2;
import com.ibm.iotf.client.application.ApplicationEventStatusSubscriptionTest3;
import com.ibm.iotf.client.application.ApplicationEventStatusSubscriptionTest4;
import com.ibm.iotf.client.application.api.BulkAPIOperationsTest;
import com.ibm.iotf.client.device.DeviceCommandSubscriptionTest;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest1;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest2;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest3;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest4;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest5;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest6;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest7;
import com.ibm.iotf.client.gateway.GatewayCommandSubscriptionTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	ApplicationCommandStatusSubscriptionTest1.class,
	ApplicationEventStatusSubscriptionTest1.class,
	ApplicationEventStatusSubscriptionTest2.class,
	ApplicationEventStatusSubscriptionTest3.class,
	ApplicationEventStatusSubscriptionTest4.class,
	ApplicationEventSubscriptionTest.class,
	DeviceManagementTest1.class,
	DeviceManagementTest2.class,
	DeviceManagementTest3.class,
	DeviceManagementTest4.class,
	DeviceManagementTest5.class,
	DeviceManagementTest6.class,
	DeviceManagementTest7.class,
	DeviceCommandSubscriptionTest.class,
	GatewayCommandSubscriptionTest.class,
	GatewayManagementTest.class,
	BulkAPIOperationsTest.class
})
public class TestSuite {

}
