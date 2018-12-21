package com.ibm.iotf.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.iotf.client.application.ApplicationEventSubscriptionTest;
import com.ibm.iotf.client.application.ApplicationCommandStatusSubscriptionTest1;
import com.ibm.iotf.client.application.ApplicationEventStatusSubscriptionTest1;
import com.ibm.iotf.client.application.ApplicationEventStatusSubscriptionTest2;
import com.ibm.iotf.client.application.ApplicationEventStatusSubscriptionTest3;
import com.ibm.iotf.client.application.ApplicationEventStatusSubscriptionTest4;


@RunWith(Suite.class)
@SuiteClasses({ 
	ApplicationEventSubscriptionTest.class,
	ApplicationCommandStatusSubscriptionTest1.class,
	ApplicationEventStatusSubscriptionTest1.class,
	ApplicationEventStatusSubscriptionTest2.class,
	ApplicationEventStatusSubscriptionTest3.class,
	ApplicationEventStatusSubscriptionTest4.class,
})
public class MessagingApplicationTestSuite {

}
