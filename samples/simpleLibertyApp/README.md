# Liberty Profile Web application
### Introduction

This web application runs on liberty profile deployed on the IBM Bluemix environment. It provides frames UI for performing various activities such as subscribing to device events, changing the subscription. It uses the VCAP credentials from the Bluemix environment itself and so needs the IoT service to be bound to the liberty application.

In this project we are using the following libraries
  - IoTF java client
  - MQTT Paho client
  - Commons
  - HTTP Client
  

---
### Version
0.0.2

---
### Technology

This project makes use of plain JSPs, servlets and IoTF Java client library and Paho client library


---
### Installation
1. Create a dynamic web project in Eclipse (Luna and above).
2. Copy the contents of the java source file inside src. 
3. Add the jsp/html files, from the Webcontent folder, as well as, the libraries inside the WEB-INF/lib folder, and the web.xml to the WebContent folder.
4. Build this web project.
5. Deploy the code to the Bluemix Liberty profile


---

###License
 - Apache License

