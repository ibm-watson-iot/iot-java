package com.ibm.iotf.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.iotf.client.gateway.GatewayManagementTest;
import com.ibm.iotf.client.gateway.GatewayCommandSubscriptionTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	GatewayManagementTest.class,
	GatewayCommandSubscriptionTest.class
})
public class MessagingGatewayTestSuite {

}
