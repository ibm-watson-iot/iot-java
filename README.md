# Java for IBM Watson IoT Platform

[![Build Status](https://travis-ci.org/ibm-watson-iot/iot-java.svg?branch=master)](https://travis-ci.org/ibm-watson-iot/iot-java)
[![Coverage Status](https://coveralls.io/repos/github/ibm-watson-iot/iot-java/badge.svg?branch=master)](https://coveralls.io/github/ibm-watson-iot/iot-java?branch=master)
[![GitHub issues](https://img.shields.io/github/issues/ibm-watson-iot/iot-java.svg)](https://github.com/ibm-watson-iot/iot-java/issues)
[![GitHub](https://img.shields.io/github/license/ibm-watson-iot/iot-java.svg)](https://github.com/ibm-watson-iot/iot-java/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.ibm.wiotp/com.ibm.wiotp.sdk.svg)](https://search.maven.org/search?q=g:com.ibm.wiotp)


## Overview

- Logging powered by [SLF4J](https://www.slf4j.org/manual.html)
- HTTP API support is provided by [swagger-codegen](https://github.com/swagger-api/swagger-codegen). See [com.ibm.wiotp.sdk.swagger](https://github.ibm.com/ibm-watson-iot/swagger-java)


## Documentation

[https://ibm-watson-iot.github.io/iot-java/](https://ibm-watson-iot.github.io/iot-java/)


## Usage

Add a dependency on `com.ibm.wiotp.sdk` to your project's `pom.xml`:

```xml
<dependency>
  <groupId>com.ibm.wiotp</groupId>
  <artifactId>com.ibm.wiotp.sdk</artifactId>
  <version>0.2.3</version>
</dependency>
```

### Applications

Simple example that initialises an application client from environment variables, connects, sends 1 command to a device, and then disconnects.

```java
import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.app.ApplicationClient;
import com.ibm.wiotp.sdk.codecs.JsonCodec;

class simpleTest {
	public static void main(String[] args) {
		ApplicationClient appClient = new ApplicationClient();
		appClient.registerCodec(new JsonCodec());
		appClient.connect();
		JsonObject data = new JsonObject();
		data.addProperty("distance", 10);
		appClient.publishCommand("myDeviceType", "myDeviceId", "myCommand", data);
		appClient.disconnect();
	}
}
```


### Devices

Simple example that initialises a device client from environment variables, connects, sends 1 event, and then disconnects.

```java
import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.device.DeviceClient;
import com.ibm.wiotp.sdk.codecs.JsonCodec;

class simpleTest {
	public static void main(String[] args) {
		DeviceClient deviceClient = new DeviceClient();
		deviceClient.registerCodec(new JsonCodec());
		deviceClient.connect();
		JsonObject data = new JsonObject();
		data.addProperty("distance", 10);
		deviceClient.publishEvent("myEvent", data);
		deviceClient.disconnect();
	}
}
```