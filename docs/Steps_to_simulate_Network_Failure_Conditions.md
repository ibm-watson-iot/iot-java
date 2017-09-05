# Java Client Library - Application Test(s)
Tests to simulate _Network Failure_ conditions

## Introduction

Two test cases, _**testConnectionLossClientToServer()**_ and _**testConnectionLossServerToClient()**_ have been added to the Watson Iot Platform Java Client Library, that help simulate network outage issue. The test cases make use of the Proxy server that helps simulate the network outages between the Client and the Server communications.

The Network failures can be created, simulated in-house using various means, like:
1. Unplug the network cable
1. Disconnect the Wifi connection
1. Block the Network ports
1. Initiate connections to non-existent Host / IP Address / Port
and many other means

In the scope of the two test cases mentioned above, we are making use a Proxy Server [ConnectionManipulationProxyServer()](iot-java/src/test/java/org/eclipse/paho/client/mqttv3/test/utilities/ConnectionManipulationProxyServer.java), that help us simulate the following two scenarios:
1. Network disconnect between Client to Server, while Publishing Blink event
1. Network disconnect between Server to Client, while acknowledging Publish event

## Proxy Server - What's the need?

[ConnectionManipulationProxyServer()](iot-java/src/test/java/org/eclipse/paho/client/mqttv3/test/utilities/ConnectionManipulationProxyServer.java) is a 3rd party contribution, currently being made available under the Utilities of Paho MQTT Java library. This utility helps setup a Proxy Server and in the scope of the two tests added to Watson IoT Platform Java Client Library, the Proxy Server sits between the Client and the Server (In the scope of our tests) and controls the connection as well the communication between the Client and the Server.

The [ConnectionManipulationProxyServer()](iot-java/src/test/java/org/eclipse/paho/client/mqttv3/test/utilities/ConnectionManipulationProxyServer.java) method provides many options to simulate network outages, based on need and requirements. The Proxy server helps give a feel that the Client and Server are connected to each other, but in reality, they are connected to this Proxy Server. Thus, giving you the flexibility to induce network outages in communications between the Client and Server.

## Files Involved

Following are the set of the files that involve manipulations, to get the Proxy sever up & running against the Java Client Library:
* [iot-java/src/test/java/com/ibm/iotf/client/application/ApplicationEventSubscriptionTest.java](https://github.com/ibm-watson-iot/iot-java/blob/master/src/test/java/com/ibm/iotf/client/application/ApplicationEventSubscriptionTest.java)
* [iot-java/src/test/java/org/eclipse/paho/client/mqttv3/test/utilities/ConnectionManipulationProxyServer.java]()
* [iot-java/src/main/java/com/ibm/iotf/client/AbstractClient.java](https://github.com/ibm-watson-iot/iot-java/blob/master/src/main/java/com/ibm/iotf/client/AbstractClient.java)
* [iot-java/src/main/java/com/ibm/iotf/client/device/DeviceClient.java](https://github.com/ibm-watson-iot/iot-java/blob/master/src/main/java/com/ibm/iotf/client/device/DeviceClient.java)

## Simulate network failure - The flow

### Network disconnect between Server to Client, while acknowledging Publish event
Consider the scenario where the Client has successfully published an Event on the Server, through the Proxy server. Now, the Server sends the acknowledgement back to the Client, suggesting that the Publish event was successful, again, through the Proxy Server. Now, the Proxy Server manipulates the scenario such that, it simulates the network disconnection between the Server and Client, before the acknowledgement is received by the Client.

The following Image depicts the connection disruption while Server acknowledges to Client, through Proxy Server

![](https://github.com/amprasanna/images/blob/master/ServerToClient.PNG)

Following are the set of actions that occur when you execute testConnectionLossServerToClient():
1. The test case testConnectionLossServerToClient() involves a Client, Proxy Server and the WIoTP
1. The Proxy server sits between the Client and the Server. It accepts and processes HTTP requests.
1. On execution, the Client initiates a connection request to the Server. However, the Proxy Server that sits between the Client and the Server, takes in the connection request from the Client and passes it on to the Server
1. This action gives an impression to the Client that it has directly connected to the Server. However, in reality, it has connected to the Proxy Server and in turn, the Proxy Server has now connected to the Server
1. The Client triggers a Publish Event action and this is received by the Proxy Server. The Proxy server passes on the action to have the Blink event published on Server. 
1. The Server now acknowledges the completion of action to the Proxy Server (assuming it acknowledged the Client). The Proxy Server, now manipulates the Network Connection using the method addDelayInServerResponse(delayInMilliSeconds) that helps induce the delay taking the parameter value in Milliseconds. Thus holding back the publish event response from being sent to Client.
1. The parameter value passed on to the addDelayInServerResponse() method is compared against the time out value specified against the method waitForCompletion(), who has a value set to 60 Seconds (60 * 1000)
1. If the parameter value of addDelayInServerResponse() is higher than that of waitForCompletion(), then, we have successfully simulated the Network failure or disconnectivity in communication between the Server and the Client.
1. If the parameter value of addDelayInServerResponse() is less than that of waitForCompletion(), then, the acknowledgement response shall reach the Client, signalling that, the Network outage simulation didn't come into the picture.
1. The success of the test case depends on the outcome of the waitForCompletion() method. If it times out, then the test is successful, else, the test fails

### Network disconnect between Client to Server, while Publishing Blink event

Consider the scenario where the Client is about to publish a Blink event to the Server, through Proxy server. The Client triggers the Blink event to have it published on the Server. Now, the Proxy Server manipulates the scenario such that, it simulates the network disconnection between the Client and the Server, holding back the action, ensuring the Blink event does not get published on the Server.

The following Image depicts the connection disruption while Client works on Publishing a Blink event on to the Server, through Proxy Server

![](https://github.com/amprasanna/images/blob/master/ClientToServer.PNG)

Following are the set of actions that occur when you execute testConnectionLossClientToServer():
1. The test case testConnectionLossClientToServer() involves a Client, Proxy Server and the WIoTP
1. The Proxy server sits between the Client and the Server. It accepts and processes HTTP requests.
1. On execution, the Client initiates a connection request to the Server. However, the Proxy Server that sits between the Client and the Server, takes in the connection request from the Client and passes it on to the Server
1. This action gives an impression to the Client that it has directly connected to the Server. However, in reality, it has connected to the Proxy Server and in turn, the Proxy Server has now connected to the Server
1. The Client triggers a Publish Event action and this is received by the Proxy Server. The Proxy server doesn't pass on the action immediately, to have the Blink event published on Server. 
1. The Proxy Server, now manipulates the Network Connection using the method addDelayInClientPublish(delayInMilliSeconds) that helps induce the delay, taking the parameter value for delay in Milliseconds. Thus holding back the publish event from being sent to Server.
1. The parameter value passed on to the addDelayInClientPublish() method is compared against the time out value specified against the method waitForCompletion(), which has a value set to 60 Seconds (60 * 1000)
1. If the parameter value of addDelayInClientPublish() is higher than that of waitForCompletion(), then, we have successfully simulated the Network failure or disconnectivity in communication between the Client and the Server.
1. If the parameter value of addDelayInClientPublish() is less than that of waitForCompletion(), then, the Blink event gets published on the Server, signalling that, the Network outage simulation didn't come into the picture. 
1. The success of the test case depends on the outcome of the waitForCompletion() method. If it times out, then the test is successful, else, the test fails

