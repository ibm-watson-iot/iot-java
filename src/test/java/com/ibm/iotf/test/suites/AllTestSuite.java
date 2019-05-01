package com.ibm.iotf.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.iotf.client.application.ApplicationTest;
import com.ibm.iotf.client.device.DeviceCommandSubscriptionTest;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest1;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest2;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest3;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest4;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest5;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest6;
import com.ibm.iotf.client.device.devicemanagement.DeviceManagementTest7;
import com.ibm.iotf.client.gateway.GatewayCommandSubscriptionTest;
//import com.ibm.iotf.client.gateway.GatewayManagementTest;
import com.ibm.iotf.client.gateway.GatewayManagementTest2;
import com.ibm.iotf.client.gateway.GatewayRegisterDeviceTest1;
import com.ibm.iotf.client.gateway.GatewayRegisterDeviceTest2;

@RunWith(Suite.class)
@SuiteClasses({ 
	ApplicationTest.class,
	DeviceManagementTest1.class,
	DeviceManagementTest2.class,
	DeviceManagementTest3.class,
	DeviceManagementTest4.class,
	DeviceManagementTest5.class,
	DeviceManagementTest6.class,
	DeviceManagementTest7.class,
	DeviceCommandSubscriptionTest.class,
	GatewayCommandSubscriptionTest.class,
	GatewayRegisterDeviceTest1.class,
	GatewayRegisterDeviceTest2.class,
	//GatewayManagementTest.class,
	GatewayManagementTest2.class,
})
public class AllTestSuite {

}
