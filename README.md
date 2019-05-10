# IBM Watson IoT Platform Java SDK

[![Build Status](https://travis-ci.org/ibm-watson-iot/iot-java.svg?branch=master)](https://travis-ci.org/ibm-watson-iot/iot-java)
[![GitHub issues](https://img.shields.io/github/issues/ibm-watson-iot/iot-java.svg)](https://github.com/ibm-watson-iot/iot-java/issues)
[![GitHub](https://img.shields.io/github/license/ibm-watson-iot/iot-java.svg)](https://github.com/ibm-watson-iot/iot-java/blob/master/LICENSE)
[![Coverage Status](https://coveralls.io/repos/github/ibm-watson-iot/iot-java/badge.svg?branch=master)](https://coveralls.io/github/ibm-watson-iot/iot-java?branch=master)


## Overview

- The clients only support handling of JSON formatted messages currently.
- HTTP API support is provided by [swagger-codegen](https://github.com/swagger-api/swagger-codegen): 


## Documentation

https://ibm-watson-iot.github.io/iot-java/


## Basic Usage

### Applications

Simple example that initialises an application client from environment variables, connects, sends 1 command to a device, and then disconnects.

```java
import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.app.ApplicationClient;

class simpleTest {
	public static void main(String[] args) {
		ApplicationClient appClient = new ApplicationClient();
		appClient.connect();
		JsonObject data = new JsonObject();
		data.addProperty("distance", 10);
		appClient.publishCommand("myDeviceType", "myDeviceId", "mycommand", data);
		appClient.disconnect();
	}
}
```


### Devices

Simple example that initialises a device client from environment variables, connects, sends 1 event, and then disconnects.

```java
import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.app.DeviceClient;

class simpleTest {
	public static void main(String[] args) {
		DeviceClient deviceClient = new DeviceClient();
		deviceClient.connect();
		JsonObject data = new JsonObject();
		data.addProperty("distance", 10);
		deviceClient.publishEvent("myevent", data);
		deviceClient.disconnect();
	}
}
```