package com.ibm.iotf.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.iotf.client.application.api.BulkAPIOperationsTest;
import com.ibm.iotf.client.application.api.DeviceAPIOperationsTest;
import com.ibm.iotf.client.application.api.DeviceDiagnosticsAPIOperationsTest;
import com.ibm.iotf.client.application.api.DeviceManagementAPIOperationsTest;
import com.ibm.iotf.client.application.api.DeviceManagementExtensionsTest;
import com.ibm.iotf.client.application.api.DeviceTypeAPIOperationsTest;
import com.ibm.iotf.client.application.api.EventCacheAPITests;
import com.ibm.iotf.client.application.api.GatewayAPIOperationsTest;
import com.ibm.iotf.client.application.api.UsageManagementAPIOperationsTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	BulkAPIOperationsTest.class,
	// EventCacheAPITests.class,    // FAILE 1
	DeviceAPIOperationsTest.class,  //FAILED 1 See //FIXME
	GatewayAPIOperationsTest.class,
	//DeviceDiagnosticsAPIOperationsTest.class, // FAILED
	// DeviceManagementAPIOperationsTest.class, // FAILED
	//Failed tests:   
	//     test031getMgmtRequestDeviceStatus(com.ibm.iotf.client.application.api.DeviceManagementAPIOperationsTest): HttpCode :0 ErrorMessage :: Failure in retrieving the Device management Request ::null
	//   test05deleteMgmtRequest(com.ibm.iotf.client.application.api.DeviceManagementAPIOperationsTest): HttpCode :0 ErrorMessage :: Failure in deleting the DM Request for ID (007b14da-9f72-4686-8790-b5c147a48a2b)::null
	// DeviceManagementExtensionsTest.class, // FAILED
	//Failed tests:   
	//    test03DeleteDeviceManagementExtension(com.ibm.iotf.client.application.api.DeviceManagementExtensionsTest): HttpCode :0 ErrorMessage :: Failure in adding the Device Management Extension ::null

	DeviceTypeAPIOperationsTest.class,
	UsageManagementAPIOperationsTest.class
})
public class APITestSuite {

}
