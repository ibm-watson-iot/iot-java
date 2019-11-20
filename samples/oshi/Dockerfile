FROM openjdk:8u212-jdk-alpine3.9

ADD target/com.ibm.wiotp.samples.oshi-1.0.2-jar-with-dependencies.jar /opt/ibm/iotoshi/

ENTRYPOINT ["java", "-jar", "/opt/ibm/iotoshi/com.ibm.wiotp.samples.oshi-1.0.2-jar-with-dependencies.jar"]
