# IBM Watson IoT Platform OSHI Device Client

## Overview
Device code for sending system utilization data to IBM Watson IoT Platform, powered by [OSHI](https://github.com/oshi/oshi)

> OSHI is a free JNA-based (native) Operating System and Hardware Information library for Java. It does not require the installation of any additional native libraries and aims to provide a cross-platform implementation to retrieve system information, such as OS version, processes, memory & CPU usage, disks & partitions, devices, sensors, etc.

The following data points are collected:
 * CPU utilization (%)
 * Memory utilization (%)


## Event Format

- `cpu` obtained from `oshi.hardware.CentralProcessor`
- `memory` obtained from `oshi.hardware.GlobalMemory`


## Before you Begin

Register a device with IBM Watson IoT Platform.  

For information on how to register devices, see the [Connecting Devices](https://www.ibm.com/support/knowledgecenter/SSQP8H/iot/platform/iotplatform_task.html) topic in the IBM Watson IoT Platform documentation.  

At the end of the registration process, make a note of the following parameters: 
   - Organization ID
   - Type ID
   - Device ID
   - Authentication Token  

## Docker

The easiest way to test out the sample is via the [wiotp/oshi](https://cloud.docker.com/u/wiotp/repository/docker/wiotp/oshi) Docker image provided and the `--quickstart` command line option.

The resource requirements for this container are tiny, if you use the accompanying helm chart it is by default confiugured with a request of 2m CPU + 18Mi memory, and  limits set to 4m cpu + 24Mi memory.

```
$ docker run -d --name psutil wiotp/oshi --quickstart
oshi
$ docker logs -tf oshi
2019-05-30 10:52:36 INFO  OshiDevice           IBM Watson IoT Platform OSHI Device Client
2019-05-30 10:52:36 INFO  OshiDevice           https://github.com/ibm-watson-iot/iot-java/tree/master/samples/oshi
2019-05-30 10:52:36 INFO  OshiDevice
2019-05-30 10:52:36 INFO  OshiDevice           Welcome to IBM Watson IoT Platform Quickstart, view a vizualization of live data from this device at the URL below:
2019-05-30 10:52:36 INFO  OshiDevice           https://quickstart.internetofthings.ibmcloud.com/#/device/3202968a743d/sensor/
2019-05-30 10:52:36 INFO  OshiDevice
2019-05-30 10:52:36 INFO  OshiDevice           (Press <Ctrl+C> to quit)
```

To connect as a registered device in your organization you must set the following environment variables in the container's environment. These variables correspond to the device parameters for your registered device: 
- `WIOTP_IDENTITY_ORGID`
- `WIOTP_IDENTITY_TYPEID`
- `WIOTP_IDENTITY_DEVICEID`
- `WIOTP_AUTH_TOKEN`.

The following example shows how to set the environment variables:

```
$ export WIOTP_IDENTITY_ORGID=myorgid
$ export WIOTP_IDENTITY_TYPEID=mytypeid
$ export WIOTP_IDENTITY_DEVICEID=mydeviceid
$ export WIOTP_AUTH_TOKEN=myauthtoken
$ docker run -d -e WIOTP_IDENTITY_ORGID -e WIOTP_IDENTITY_TYPEID -e WIOTP_IDENTITY_DEVICEID -e WIOTP_AUTH_TOKEN --name oshi wiotp/oshi
oshi
$ docker logs -tf oshi
2019-05-30 10:52:36 INFO  OshiDevice           IBM Watson IoT Platform OSHI Device Client
2019-05-30 10:52:36 INFO  OshiDevice           https://github.com/ibm-watson-iot/iot-java/tree/master/samples/oshi
2019-05-30 10:52:36 INFO  OshiDevice
2019-05-30 10:52:36 INFO  OshiDevice           (Press <Ctrl+C> to quit)
```

## Kubernetes & Helm

A [helm chart](https://github.com/ibm-watson-iot/iot-python/tree/master/samples/psutil/helm/psutil) is available if that is your preferred way to Docker.  The chart accepts the standard format device configuration file as a Helm values file:

```
$ helm repo add wiotp https://ibm-watson-iot.github.io/helm/charts/
$ helm install oshi-mydevice wiotp/oshi --values path/to/mydevice.yaml
```

If you provide no additional values the chart will deploy in a configuration supporting Quickstart by default:

```
$ helm repo add wiotp https://ibm-watson-iot.github.io/helm/charts/
$ helm install oshi-quickstart wiotp/oshi
```

The pod consumes very little resource during operation, you can easily max out the default 110 pod/node limit with a cheap 2cpu/4gb worker if you are looking to deploy this chart at scale.


## Local Installation
Installation across all OS's is pretty much the same:

- Ensure you already have java installed
- Download the `jar-with-dependencies` artifact from Maven Central
- Run the sample

```
$ wget https://repo1.maven.org/maven2/com/ibm/wiotp/com.ibm.wiotp.samples.oshi/1.0.0/com.ibm.wiotp.samples.oshi-1.0.0-jar-with-dependencies.jar
$ java -jar com.ibm.wiotp.samples.oshi-1.0.0-jar-with-dependencies.jar --quickstart
```

Note: Set the same environment variables detailed in the Docker section of this README (above) and ommit the `--quickstart` argument to connect to IBM Watson IoT Platform as a registered device.

