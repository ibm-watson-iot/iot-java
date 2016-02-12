/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */
package com.ibm.iotf.sample.client.gateway;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 

import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.gateway.GatewayClient;
import com.ibm.iotf.devicemgmt.DeviceAction;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;
import com.ibm.iotf.sample.devicemgmt.gateway.GatewayFirmwareHandlerSample;

/**
 *  <p>This class shows how to use the java RXTX library for serial communication 
 *  with Raspberry Pi Gateway to read/write the data from/to Arduino Uno. 
 *  In order to use the RXTX library, you need to install the library on 
 *  Raspberry Pi using the following command:</p>
 *   
 *          <br>sudo apt-get install librxtx-java</br><br>
 *          
 *  <p>Observe that the RXTX jar file is present in this location: /usr/share/java/RXTXcomm.jar.</p>
 *  
 *  <p>Ideally the Arduino Uno connects to Raspberry pi using the port "/dev/ttyACM0". 
 *  However the port might change at times, to find the exact port, 
 *  run the following command in Raspberry Pi without the Arduino Uno plugged in:</p>
 *  
 *      <br>ls /dev/tty*<br>
 *      
 *  <p> Now plug in your Arduio Uno and run the command again. If a new name appears, 
 *  then this is the name of your port. If the port name is different from /dev/ttyACM0, 
 *  then run the GatewaySample with the port name passes to it.</p>
 *  
 *  <p>Also, note that on Raspberry Pi, the RXTX library places its lock in the folder 
 *  /var/lock. If this doesn't exist, communication will not work, although you won't 
 *  receive any obvious errors as to why. To create this directory if missing, open the 
 *  terminal application and create the lock directory as the root user or equivalent:</p>
 *  
 *      sudo mkdir /var/lock
 *      
 */
public class ArduinoSerialInterface implements SerialPortEventListener, DeviceInterface {
	/**
	 * IoT Foundation related paramteres
	 */
	private String deviceId;
	private String deviceType;
	private String port;
	
	private GatewayClient gwClient;
	
	public ArduinoSerialInterface(String deviceId, String deviceType, String port, GatewayClient gatewayClient) {
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.gwClient = gatewayClient;
		this.port = port;
	}
	
	SerialPort serialPort;
    /**
	* A BufferedReader which will be fed by a InputStreamReader 
	* converting the bytes into characters 
	* making the displayed results codepage independent
	*/
	private BufferedReader input;
	/** The output stream to the port */
	private BufferedWriter output;
	private String downloadedFirmwareName;
	private volatile boolean bDisplay = true;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	private static final String CLASS_NAME = ArduinoSerialInterface.class.getName();

	/**
	 * This method does the following,
	 * 
	 * 1. Iterate through all of the system ports looking for a match for the Arduino Uno, 
	 *    and then attempts to connect to it. 
	 * 
	 * 2. Connnect to the port, configure the communication parameters, like the bit rate, timeout and etc..
	 * 
	 * 3. Open the input and output streams for communication
	 * 
	 * 4. Add an event listener to receive events from the Arduino, 
	 *    and tell it to call us back when there's data available
	 */
	public void initialize() {
		System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			if (currPortId.getName().equals(this.port)) {
					portId = currPortId;
					break;
			}
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream()));
			 
			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception | Error e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * The Raspberry Pi gateway can send data to Arduino Uno by writing data 
	 * on the output stream that we opened earlier:
	 */
	public void sendCommand(String cmd) {
		if(this.output != null) {
			try {
				System.out.println("writing the cmd to arduino "+cmd);
				this.output.write(cmd);
				this.output.write(" "); // delimiter
				this.output.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * The RXTX library calls the serialEvent() method when the data is available to 
	 * read from the serial port. This is where the Raspberry Pi gets the sensor 
	 * readings (the PIR movement sensor and the internal temperature) from Arduino. 
	 * 
	 * Arduino sends the values in the following format,
	 * 
	 *      a. The name of the event - Required by Watson IoT Platform followed by space
	 *      b. comma separated datapoints, for example, temp and pir readings as shown below,
	 *      
	 *      "status temp:35.22,pir:0"
	 * 
	 * This method converts the above data into from Arduino Uno into JSON format send it to Watson Iot Platform. 
	 */
	public void serialEvent(SerialPortEvent oEvent) {
		String line = null;
		synchronized(this) {
			if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				try {
					line = input.readLine();
					if(bDisplay)
						System.out.println(line);
				} catch (Exception e) {
					System.err.println(e.toString());
				}
			}
			// Ignore all the other eventTypes, but you should consider the other ones.
		}
		
		try {
			String[] tokens = line.split(" ");
			if(tokens.length != 2) {
				return;
			}
			String eventName = tokens[0];
			JsonElement event = new JsonParser().parse("{" + tokens[1] + "}");
			boolean status = this.gwClient.publishDeviceEvent(this.deviceType, this.deviceId, eventName, event);
			if(status == false) {
				System.err.println("Failed to publish the temperature from Arduino");
			} else {
				//System.out.println("Successfully published the temperature from Ardunio to IoT Foundation");
			}
		} catch(Exception e) {
			System.err.println("Failed to parse the sensor readings from Arduino "+e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void setFirmwareName(String downloadedFirmwareName) {
		this.downloadedFirmwareName = downloadedFirmwareName;
		
	}

	/**
	 * <p>A sample firmware update method that installs the arduino.hex sketch
	 * to Arduino Uno with the following command,</p>
	 * 
	 * <p>avrdude -q -V -p atmega328p -C /etc/avrdude.conf -c arduino -b 115200 -P /dev/ttyACM0 -U flash:w:arduino_v1.hex:i</p>
	 * 
	 */
	@Override
	public void updateFirmware(DeviceFirmware deviceFirmware) {
		
		System.out.println(CLASS_NAME + ": Firmware update start... for device = "+deviceFirmware.getDeviceId());
			
		final String INSTALL_LOG_FILE = "install.log";
		// Code to update the firmware on the Raspberry Pi Gateway
		ProcessBuilder pkgInstaller = null;
		Process p = null;
		
		Properties prop = new Properties();
		try {
			FileInputStream in = new FileInputStream("avrdude.properties");
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		close();
		
		// Build a command that will push the hex file to Arduino Uno
		try {
			pkgInstaller = new ProcessBuilder("avrdude", "-q", "-V", 
											  "-p", trimedValue(prop.getProperty("partno", "atmega328p")),
											  "-C", trimedValue(prop.getProperty("Config", "/etc/avrdude.conf")),
											  "-c", "arduino",
											  "-b", "115200",
											  "-P", this.port,
											  "-U", "flash:w:"+ downloadedFirmwareName +":i");
			
			pkgInstaller.redirectErrorStream(true);
			pkgInstaller.redirectOutput(new File(INSTALL_LOG_FILE));
			try {
				p = pkgInstaller.start();
				boolean status = GatewayFirmwareHandlerSample.waitForCompletion(p, 5);
				System.out.println(GatewayFirmwareHandlerSample.getInstallLog(INSTALL_LOG_FILE));
				if(status == false) {
					p.destroy();
					deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
					return;
				}
				
				System.out.println("Firmware Update command "+status);
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
				deviceFirmware.setState(FirmwareState.IDLE);
			} catch (IOException e) {
				e.printStackTrace();
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
			} catch (InterruptedException e) {
				e.printStackTrace();
				deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.UNSUPPORTED_IMAGE);
			}
		} catch (OutOfMemoryError oom) {
			deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.OUT_OF_MEMORY);
		}
		
		/**
		 * Delete the temporary firmware file
		 */
		GatewayFirmwareHandlerSample.deleteFile(downloadedFirmwareName);
		GatewayFirmwareHandlerSample.deleteFile(INSTALL_LOG_FILE);
		
		this.downloadedFirmwareName = null;
		System.out.println(CLASS_NAME + ": Firmware update End...");
		this.initialize();
	}

	/**
	 * A sample method to handle the Arduino's reboot request from the DM server.
	 * 
	 * <p>In this case its assumed that the sketch program running in Arduino Uno
	 * will reboot when it receives 0 in the serial port.</p>
	 * 
	 * This method just sends a 0 and reinitializes the connection to Arduino Uno.
	 */
	@Override
	public void reboot(DeviceAction action) {
		// The Arduino is programmed to reboot when it receive a command 0
		sendCommand("0");
		// close the streams and wait for a 10 seconds for the Arduino Uno to restart
		close();
		try {
			Thread.sleep(1000 * 10);
		} catch(InterruptedException ie) {}
		
		this.initialize();
		// We must send a manage request inorder to complete the reboot request successfully
		try {
			ManagedGateway gateway = ((ManagedGateway) this.gwClient);
			gateway.sendDeviceManageRequest(this.deviceType, this.deviceId, 0, true, true);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * To trun on/off the sensor event output in the console (System.out)
	 */
	public void toggleDisplay() {
		this.bDisplay = !this.bDisplay;
	}
	
	private static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}
	
}