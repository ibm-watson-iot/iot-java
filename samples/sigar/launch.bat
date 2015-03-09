@echo on

set SAMPLE_HOME=%~dp0
set LIB_DIR=%SAMPLE_HOME%..\..\lib
set OUTPUT_LIB_DIR=%SAMPLE_HOME%..\..\output\lib

set LOG_PROPS=%SAMPLE_HOME%..\common\logging.properties

set CLASSPATH=%LIB_DIR%\org.eclipse.paho.client.mqttv3.jar;%LIB_DIR%\gson-2.2.4.jar;
set CLASSPATH=%CLASSPATH%;%OUTPUT_LIB_DIR%\com.ibm.iotf.client-1.0.0.jar;%OUTPUT_LIB_DIR%\com.ibm.iotf.samples-1.0.0.jar;
set CLASSPATH=%CLASSPATH%;%SAMPLE_HOME%\..\common\lib\*;%SAMPLE_HOME%\lib\*

java -Djava.util.logging.config.file=%LOG_PROPS% -cp %CLASSPATH% com.ibm.iotf.samples.sigar.SigarIoTDevice %*