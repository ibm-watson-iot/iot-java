IBM IOT Cloud SIGAR Adapter
===========================

This sample utilizes Hyperic's System Information Gatherer (SIGAR) library, a cross-platform API for collecting 
software inventory data.  The sample supports [IBM Internet of Things QuickStart](http://quickstart.internetofthings.ibmcloud.com) as well as 
allowing Managed Beta participants to connect this sample to their private account.

SIGAR includes support for Linux, FreeBSD, Windows, Solaris, AIX, HP-UX and Mac OSX across a variety of 
versions and architectures. 

Hyperic SIGAR is licensed under the terms of the Apache 2.0 license.


Dependencies
------------
* [Sigar](http://www.hyperic.com/products/sigar)


Usage - Linux
-------------

###Clone and build the project
```
git clone https://github.com/durera/iot-java
cd iot-java
ant build
```

###Connect to QuickStart
```
samples/sigar/launch.sh
Connected successfully - Your device ID is 060058000321
 * http://quickstart.internetofthings.ibmcloud.com/?deviceId=060058000321
Visit the QuickStart portal to see this device's data visualized in real time and learn more about the IBM Internet of Things Cloud

(Press <enter> to disconnect)
```

###Connect to the Closed Beta
```
samples/sigar/launch --account <account> --username <username> --password <password>
Connected successfully - Your device ID is 060058000321
 * Account: <account> (<username>/<password>)

(Press <enter> to disconnect)
```
