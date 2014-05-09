#!/bin/bash

SAMPLE_HOME=`dirname $0`
LIB_DIR=${SAMPLE_HOME}/../../lib

LOG_PROPS=../common/logging.properties

CLASSPATH=${LIB_DIR}/org.eclipse.paho.client.mqttv3.jar:${LIB_DIR}/gson-2.2.4.jar
CLASSPATH=${CLASSPATH}:${LIB_DIR}/com.ibm.iotcloud.client-1.0.0.jar:${LIB_DIR}/com.ibm.iotcloud.samples-1.0.0.jar
CLASSPATH=${CLASSPATH}:${SAMPLE_HOME}/../common/lib/args4j-2.0.21.jar:${SAMPLE_HOME}/lib/sigar.jar

java -Djava.util.logging.config.file=${LOG_PROPS} -cp ${CLASSPATH} com.ibm.iotcloud.samples.sigar.Launcher $*