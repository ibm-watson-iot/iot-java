package com.ibm.iotf.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.iotf.client.gateway.GatewayManagementTest;
import com.ibm.iotf.client.application.ApplicationEventSubscriptionTest;
import com.ibm.iotf.client.application.api.BulkAPIOperationsTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	BulkAPIOperationsTest.class
	//GatewayManagementTest.class,
	//ApplicationEventSubscriptionTest.class
})
public class TestSuite {

}
