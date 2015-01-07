@echo on

set SAMPLE_HOME=%~dp0
set LIB_DIR=%SAMPLE_HOME%..\..\lib

set LOG_PROPS=%SAMPLE_HOME%..\common\logging.properties

set CLASSPATH=%LIB_DIR%\org.eclipse.paho.client.mqttv3.jar;%LIB_DIR%\gson-2.2.4.jar;
set CLASSPATH=%CLASSPATH%;%LIB_DIR%\com.ibm.iotcloud.client-1.0.0.jar;%LIB_DIR%\com.ibm.iotcloud.samples-1.0.0.jar;
set CLASSPATH=%CLASSPATH%;%SAMPLE_HOME%\..\common\lib\*;%SAMPLE_HOME%\lib\*

java -Djava.util.logging.config.file=%LOG_PROPS% -cp %CLASSPATH% com.ibm.iotcloud.samples.sigar.Launcher %*