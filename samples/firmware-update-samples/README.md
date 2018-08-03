# Firmware Update Samples

These set of samples are intended to demonstrate ‘Firmware Upgrade’, one of the device management capabilities, as initiated by:

1. **Device**

1. **Platform**

1. **Platform with Background Download & Update**

Following stand-alone samples (present in this project) demonstrate Device Management samples through IBM Watson IoT Platform.

**[DeviceInitiatedHandlerSample](https://github.com/amprasanna/iot-device-samples/blob/master/java/firmware-update-samples/src/main/java/com/ibm/iotf/sample/devicemgmt/device/DeviceInitiatedHandlerSample.java)**                        : Sample that performs Device Initiated Firmware Upgrade

**[PlatformInitiatedHandlerSample](https://github.com/amprasanna/iot-device-samples/blob/master/java/firmware-update-samples/src/main/java/com/ibm/iotf/sample/devicemgmt/device/PlatformInitiatedHandlerSample.java)**                      : Sample that performs Platform Initiated Firmware Upgrade

**[PlatformInitiatedWithBkgrndDwnldHandlerSample](https://github.com/amprasanna/iot-device-samples/blob/master/java/firmware-update-samples/src/main/java/com/ibm/iotf/sample/devicemgmt/device/PlatformInitiatedWithBkgrndDwnldHandlerSample.java)**       : Sample that performs Platform Initiated Firmware Upgrade, but in the background, without affecting the operations running in the foreground

**[DeviceFirmwareSample](https://github.com/amprasanna/iot-device-samples/blob/master/java/firmware-update-samples/src/main/java/com/ibm/iotf/sample/devicemgmt/device/DeviceFirmwareSample.java)**                                : Sample that publishes Device Information and Firmware details to Watson IoT Platform Dashboard. It updates the Device location and publishes Device event at every 5 second interval.

**[ApplicationFirmwareRequestSample](https://github.com/amprasanna/iot-device-samples/blob/master/java/firmware-update-samples/src/main/java/com/ibm/iotf/sample/client/application/ApplicationFirmwareRequestSample.java)**                                : Application Sample that helps initiate connection to Watson IoT Platform from the Client Side and helps trigger Firmware Update operation on the Device, through the WIoT Platform.

**[Handler](https://github.com/amprasanna/iot-device-samples/blob/master/java/firmware-update-samples/src/main/java/com/ibm/iotf/sample/devicemgmt/device/Handler.java)**                                             : Sample that obtains User input, defined against source of Firmware Upgrade

**[HTTPFirmwareDownload](https://github.com/amprasanna/iot-device-samples/blob/master/java/firmware-update-samples/src/main/java/com/ibm/iotf/sample/devicemgmt/device/HTTPFirmwareDownload.java)**                                : Sample that downloads Firmware (Debian package) from Cloudant NoSQL Database using the Document ID

**[DebianFirmwareUpdate](https://github.com/amprasanna/iot-device-samples/blob/master/java/firmware-update-samples/src/main/java/com/ibm/iotf/sample/devicemgmt/device/DebianFirmwareUpdate.java)**                                : Sample that Updates the Firmware (Debian package), as downloaded from the Cloudant NoSQL Database



The samples are written using the [Java Client Library](https://github.com/ibm-watson-iot/iot-java) for IBM Watson IoT Platform that simplifies the interactions with the IBM Watson IoT Platform.

***

### Pre-Requisites
To build and run the sample, you must have the following set of Hardware and Software:

**Software Requirements**

* Maven

* Git

* IBM Bluemix Account

**Hardware Requirements**

* Raspberry Pi with at least 8 GB SD Card

***

### Register Device in IBM Watson IoT Platform

Follow the steps [in this recipe](https://developer.ibm.com/recipes/tutorials/how-to-register-devices-in-ibm-iot-foundation/) to register your device in Watson IoT Platform if not registered already. And copy the registration details, like the following,

**Device Credentials**


     * Organization-ID = [Your Organization ID]

     * Device-Type = [Your Device Type]

     * Device-ID = [Your Device ID]

     * Authentication-Method = token

     * Authentication-Token = [Your Device Token]

**Application API-Key**

     * id = [Unique Application ID]

     * Organization-ID = [Your Organization ID]

     * Authentication-Method = [apikey]

     * API-Key = [API-Key]

     * Authentication-Token = [Authentication-Token]

We would need these details to connect the device to IBM Watson IoT Platform.

***

### Create a Cloudant NoSQL Database Service

The Cloudant NoSQL Database Service shall be used to host the Firmware Repository, where you shall be uploading the Debain packages as attachments. The Device shall check this firmware repository at defined time intervals, to see, if a newer version of firmware is available for download.

Step 1: Go to [Bluemix](https://console.ng.bluemix.net/?cm_mmc=developerWorks-_-dWdevcenter-_-recipes-_-lp). If you are an existing Bluemix user, log in as usual. If you are new to Bluemix you can sign up for a free 30 day trial.
Step 2: Once you signed up to Bluemix, [click this link](https://console.ng.bluemix.net/catalog/services/cloudant-nosql-db/) to create the Cloudant NoSQL DB service in Bluemix.
Step 3: Type a name for your service and click Create button.
Step 4: Navigate to Service Credentials tab as shown below and note down the username & password that will be required later to connect to the Cloudant DB service

***

### Build & Run the Device Initiated Firmware Update sample using Eclipse

You must have installed the [Eclipse Maven Plugin](http://www.eclipse.org/m2e/), to import & run the samples in eclipse. Go to the next step, if you want to run manually.

Step 1: Clone the device-samples project using git clone as follows:

` git clone https://github.com/ibm-messaging/iot-device-samples.git`


Step 2: Import the iot-device-samples project into eclipse using the File->Import option in eclipse.

Step 3: We are demonstrating the Device Initiated Firmware Update Sample in this recipe. Hence, ensure, you are working against the project 'firmware-update-samples' within iot-device-samples

Step 4: Modify the DMDeviceSample.properties file with the device registration details and Cloudant NoSQL DB Authentication details, that you noted in the earlier steps.

Step 5: Build the device-samples, by right clicking on the project and then choosing Run As and selecting Maven build:

`device-samples -> Run As -> Maven build`

Step 6: When prompted with ‘Edit Configuration’ pop-up window, where in, you are required to specify the Goal(s) of the build process. Specify the following against Goals:

`clean package`

Step 7: Click on Run to initiate the build process. Observe the build process in the Console tab and ensure it is concluded successfully.

Step 8: Once the build process is concluded successfully, Right-click on **DeviceFirmwareSample.java**, choose **Run-As** and then select **Java Application**

`DeviceFirmwareSample.java -> Right Click -> Run-As -> Java Appliation`

Monitor the execution, as the Firmware Update Sample showcases the following in the logs:

1. Successful connection to Watson IoT Platform

2. Successful connection to Cloudant NoSQL Database service

3. Device Information and Events updated to Watson IoT Platform Dashboard

4. Device Events published to Watson IoT Platform Dashboard at every 5 Second Interval

***

### Building the sample - Required if you want to run the samples outside of Eclipse

Clone the iot-device-samples project using git clone as follows,

` git clone https://github.com/ibm-messaging/iot-device-samples.git`

We are demonstrating the Device Initiated Firmware Update Sample in this recipe. Hence, navigate to the source directory structure of firmware update samples, within device initiated, as shown below:

`cd iot-device-samples/java/firmware-update-samples/`

Run the maven build as follows,

`mvn clean package`

This will download the Java Client library for Watson IoT Platform, other required dependencies and starts the building process. 

***

### Executing the Device Initiated Firmware Update sample outside Eclipse

Step 1: Navigate to target/classes/Resources directory and modify DMDeviceSample.properties file & application.properties file with the Device registration details and Cloudant NoSQL DB Authentication details, that you noted in the earlier steps.

Step 2: Execute the **DeviceFirmwareSample** using the following command

`mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.devicemgmt.device.DeviceFirmwareSample"`

Monitor the execution, as the Firmware Update Sample showcases the following in the logs:

1. Successful connection to Watson IoT Platform

2. Successful connection to Cloudant NoSQL Database service

3. Device Information and Events updated to Watson IoT Platform Dashboard

4. Device Events published to Watson IoT Platform Dashboard at every 5 Second Interval



***

### Upload Latest Firmware package to Cloudant NoSQL DB, on the Run

With the **DeviceFirmwareSample** continuing it's run, Upload a **New Firmware** to the Cloudant NoSQL Database Service. Do the following steps to upload the latest firmware on to the repository:

Step 1: Clicking on the 'Launch', shall land you on to the Cloudant NoSQL Dashboard, providing default view of _Databases_. Click on the **Create Database** option, provide a **custom name** of choice and click on **Create** to create the database. For the scope of this recipe, let us consider the database name to be **firmware_repository** 

Step 2: The previous step shall help open up _All Documents_ view, as the default view, within the database ‘firmware_repository’. 

Click on the **+** symbol, provided against ‘All Documents’ entry, to pull down a drop down menu, that shall provide you the options that can be performed with respect to Cloudant documents. In the drop down menu, choose the option **New Doc**, to create a new Cloudant NoSQL Document

Step 3: Clicking on the ‘New Doc’, prompts a pop up window of new document, pre-loaded with a random Cloudant Document ID **_id**, in the JSON structure. Any subsequent entries to the document should conform to JSON structure only. Click on the **Create Document** to complete the action.

Step 4: The new Cloudant Document that was successfully created in the previous step, shall now appear in the ‘All Documents’ option of the database ‘firmware_repository’, under the Databases section. To _Edit the document_, you can make use of the **Edit** action button, provided on the right-top corner of the document entry

Step 5: Here, in this recipe, considering the fact that, the platform is currently loaded with the package ‘iot_1.0-2_armhf.deb’, let us upload the next version of the Debian package ‘iot_1.0-3_armhf.deb’ and have it deployed on the platform.

To upload the Debian package, click on the **Upload Attachment** option. A pop up window shall appear, with the options **Choose file** and **Upload Attachment**. Click on the _Choose file_ to browse through the file system, navigate to the directory where you have the Debian package(s) and choose the Debian package of choice. You should be able to find the set of Debian packages under **target/classes/Debian_Packages/**. Click on _Upload Attachment_ option to complete the attachment upload operation

Step 6: The device sample is designed to compare the Debian package currently deployed on the device against the one that is available in the firmware repository on Cloudant NoSQL Database, using the **Firmware Version**.

Since, the Debian package was uploaded as an attachment, we need to manually enter the Firmware Version into the Cloudant Document. Here, in this recipe, you will have to enter the following line into the Cloudant Document:

`"version": "1.0.3"`

Details of the Debian packaged, successfully uploaded as an attachment in the previous step, along with the version value, entered above, shall be displayed as shown:

Click on ‘Save Changes’ to save the contents and complete the operation of editing the Cloudant Document.


***

### Monitor, how Device Initiated Firmware Update execution unfolds

The Cloudant NoSQL Database has now been successfully uploaded with the latest version of the firmware. 

Continue to monitor the messages on the screen, where you had executed the Device Firmware Sample. You should now be able to see that, the firmware update sample has identified that a new Cloudant Document is available. Picks up the debian package **iot_1.0-3_armhf.deb** and it's firmware version **1.0.3**, as available against the Document ID in the Cloudant NoSQL Database, and compares the version with the firmware version, curently active on the device. If the version of the firmware on the device is older than the version of the firmware on Cloudant NoSQL DB 'firmware_repository', then Firmware Update operation is kick started.

It initiates the Firmware Download action to have the firmware downloaded on to the local file system and on completion, triggers Firmware Update process. Finally, when the firmware upgrade is applied successfully on the Device, you should see the completion message.


***

In this section, the Firmware Upgrade operation, as initiated by the Device was demonstrated to the completion, thus successfully concluding the objective set

***

### Build & Run the Platform Initiated Firmware Update sample using Eclipse

Let us continue to use the project 'firmware-update-samples' within iot-device-samples, that was compiled & built earlier in the section **Build & Run the Device Initiated Firmware Update sample using Eclipse**, from Step 1 through to Step 7. Here, in this section, perform Platform Initiated Firmware Upgrade as follows:

**Connect Device to the Watson IoT Platform and listen for Application**

Once the build process is concluded successfully, Right-click on **DeviceFirmwareSample.java**, choose **Run-As** and then select **Java Application**

`DeviceFirmwareSample.java -> Right Click -> Run-As -> Java Appliation`

Monitor the execution, as the Firmware Update Sample showcases the following in the logs:

1. Successful connection to Watson IoT Platform

2. Successful connection to Cloudant NoSQL Database service

3. Device Information (including Device Firmware Name and Firmware Version) is published to Watson IoT Platform Dashboard.

4. Device Events updated to Watson IoT Platform Dashboard at every 5 Second interval

5. Device waits to listen from the Watson IoT Platform for any commands being triggered by Application

**Application Initiates connection to Watson IoT Platform and sends commands to Device**

Right-click on **ApplicationFirmwareRequestSample.java**, choose **Run-As** and then select **Java Application**

`ApplicationFirmwareRequestSample.java -> Right Click -> Run-As -> Java Appliation`

Monitor the execution, as the Firmware Update Sample showcases the following in the logs:

1. Successful connection to Watson IoT Platform. Fetches the Device Information, primarily Device Firmware Name and Firmware Version

2. Successful connection to Cloudant NoSQL Database service. 

3. Compares the Device Firmware Version with the Firmware available on Cloudant NoSQL Database **firmware_repository**

4. If the 'firmware_repository' has the latest version, then, it triggers Firmware Download

5. Successful completion of Firmware Download, immediately triggers Firmware Update action

6. With the Firmware Update process concluding smoothly, the Application waits till the time for next invocation, elapses



***

### Building the Platform Initiated Firmware sample - Required if you want to run the samples outside of Eclipse

Clone the iot-device-samples project using git clone as follows,

` git clone https://github.com/ibm-messaging/iot-device-samples.git`

We are demonstrating the Platform Initiated Firmware Update Sample in this recipe. Hence, navigate to the source directory structure of firmware update samples, as shown below:

`cd iot-device-samples/java/firmware-update-samples/`

Run the maven build as follows,

`mvn clean package`

This will download the Java Client library for Watson IoT Platform, other required dependencies and starts the building process. 

***

### Executing the Platform Initiated Firmware Update sample outside Eclipse

Step 1: Navigate to target/classes/Resources directory and modify DMDeviceSample.properties file & application.properties file with the Device registration details and Cloudant NoSQL DB Authentication details, that you noted in the earlier steps.

Step 2: Execute the **DeviceFirmwareSample** using the following command

`mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.application.ApplicationFirmwareRequestSample"`

Monitor the execution, as the Firmware Update Sample showcases the following in the logs:

1. Successful connection to Watson IoT Platform. Fetches the Device Information, primarily Device Firmware Name and Firmware Version

2. Successful connection to Cloudant NoSQL Database service. 

3. Compares the Device Firmware Version with the Firmware available on Cloudant NoSQL Database **firmware_repository**

4. If the 'firmware_repository' has the latest version, then, it triggers Firmware Download

5. Successful completion of Firmware Download, immediately triggers Firmware Update action

6. With the Firmware Update process concluding smoothly, the Application waits till the time for next invocation, elapses



***


### 

In this section, the Firmware Upgrade operation, as initiated by the Platform was demonstrated to the completion, thus successfully concluding the objective set


***


### Build & Run the Platform Initiated Firmware Update sample with background execution


Consider a scenario, where your device is currently capturing data from various sensors and is publishing the events to Watson IoT Platform, parallelly, it is also handling data cleansing operations, sorting operations. The OS has it's own set of processess running, each of them taking their own share of processing capacity. In such situations, you would always wish to push few operations that could be run in the background, by assigning them to daemon processes and have them executed behind the scenes, whenever, there is less burden on the processor.

To help assist in such scenarios, you can make use of the Sample **PlatformInitiatedWithBkgrndDwnldHandlerSample**, that is specifically designed, to be implemented in tight situations, where certain jobs are of high priority and are critical to the application. Firmware Download and Firmware Update operations are assigned to daemon threads and are executed in the background, without hampering the performance of the processes running in the foreground.

You can make use of the execution code and steps mentioned in the earlier sections, to experience the Platform Initiated Firmware Update with Background Execution Sample, by setting the value of the parameter **option** to **PlatformBackground** in the DMDeviceSample.properties file, as located under target/classes.


***


### 

In this section, the Firmware Upgrade operation, as initiated by the Platform, but with the execution happening in the background, was demonstrated to the completion, thus successfully concluding the objective set


***


### Experience Firmware Roll Back and Firmware Factory Reset


The Firmware Update Samples discussed here, are also equipped to handle to extended Firmware Management capabilities:

* Firmware Roll Back
* Firmware Factory Reset 

In scenarios, where the uploaded Firmware is corrupt or damaged or in scenarios where the downloaded Firmware is incomplete, say missing checksum, etc, the Firmware Update operation is bound to fail. In such scenarios, based on the situation, the code handles the Firmware Roll Back and Factory Reset.

**_Firmware Roll Back_** is the first preferred fall back mechanism, where in, if the Firmware Update operation fails to successfully apply the latest firmware, then, the priority is to first retain the existing setup. The Roll Back operation does the same, i.e it applies the existing the version of the Firmware on to the Device and updates the WIoTP Dashboard about the status message.

The success of the Firmware Roll Back operation depends on the availability of the Firmware build package, being made available at a given location for usage. If the build package is not available, then the next preferred fall back mechanism is **_Firmware Factory Reset_**. In this scenario, the Firmware Update operation directly applies the Factory Version of the Firmware, i.e v1.0.1 on to the Device.


***
