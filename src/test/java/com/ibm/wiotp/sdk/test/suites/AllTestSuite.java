package com.ibm.wiotp.sdk.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.wiotp.sdk.test.ApplicationTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	ApplicationTest.class
})
public class AllTestSuite {

}
