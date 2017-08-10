/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;
import com.ibm.iotf.client.application.AutoReconnect;

public class CommunicationProxyServer implements Runnable {
	static final Class<?> cclass = AutoReconnect.class;
	private static final String className = cclass.getName();
	private static final Logger log = Logger.getLogger(className);
	private int localDevicePort;
	private boolean portNumberSet = false;
	private String host;
	private int remoteServerPort;
	private Thread proxyThread;
	private Object enableLock = new Object();
	private boolean enableProxy = true;
	private boolean running = true;
	Socket client = null, server = null;
	ServerSocket serverSocket = null;
	private volatile int delayInServerResponse = 0;
	private volatile int delayInClientPublish = 0;
//	private static final String CLASS_NAME = AbstractClient.class.getName();
//	final String METHOD = "connect";
	

	public CommunicationProxyServer(String host, int remoteServerPort, int localDevicePort) {
		this.localDevicePort = localDevicePort;
		this.remoteServerPort = remoteServerPort;
		this.host = host;
		proxyThread = new Thread(this);
	}
	
	public void startProxyServer(){
		synchronized (enableLock) {
			enableProxy = true;
		}
		running = true;
		proxyThread.start();
	}
	
	public void enableProxyServer(){
		synchronized (enableLock) {
			enableProxy = true;
		}
		running  = true;
		if(proxyThread.isAlive() == false){
			proxyThread.start();
		}
	}
	
	public void disableProxyServer(){
		synchronized (enableLock) {
			enableProxy = false;
		}
		killOpenSockets();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void stopProxyServer(){
		synchronized (enableLock) {
			enableProxy = false;
		}
		running = false;
		killOpenSockets();
	}
	
	private void killOpenSockets(){
		try {
			if(serverSocket != null){
				serverSocket.close();
			}
		} catch (IOException ex){
			// Do nothing as we want to close;
		}
		try {
			if(client != null){
				client.close();
			}
		} catch (IOException ex){
			// Do nothing as we want to close;
		}
		try {
			if(server != null){	
				server.close();
			}
		} catch (IOException ex){
			// Do nothing as we want to close;
		}
	}
	
	public void addDelayInServerResponse(int delayInMilliSeconds) {
		this.delayInServerResponse  = delayInMilliSeconds;
	}
	
	public void addDelayInClientPublish(int delayInMilliSeconds) {
		this.delayInClientPublish  = delayInMilliSeconds;
	}

	@Override
	public void run() {
		try {
			
			final byte[] request = new byte[1024];
			byte[] reply = new byte[4096];
			boolean runNow = true;
			while(running){
				synchronized (enableLock) {
					runNow = enableProxy;
				}
				
				while(runNow){
					synchronized (enableLock) {
						runNow = enableProxy;
						if(!enableProxy){
							break;
						}
					}
					if(serverSocket == null|| serverSocket.isClosed()){
						serverSocket = new ServerSocket(localDevicePort);
						localDevicePort = serverSocket.getLocalPort();
						portNumberSet = true;
					}
					
					try {
						client = serverSocket.accept();
						
						log.fine("Proxy: Client Opened Connection to Proxy...");
						
						final InputStream streamFromClient = client.getInputStream();
						final OutputStream streamToClient = client.getOutputStream();
						
						try {
							server = new Socket(host, remoteServerPort);
						} catch (IOException ex){
							log.warning("CommunicationProxyServer cannot connect to " + host + ":" + remoteServerPort);
							client.close();
							continue;
						}
						log.fine("Proxy Server: Proxy Server successfully connected to Server");
						
						final InputStream streamFromServer = server.getInputStream();
						final OutputStream streamToServer = server.getOutputStream();
						
						Thread thread = new Thread() {
							public void run() {
								int bytesRead;
								try {
									while((bytesRead = streamFromClient.read(request)) != -1) {
										if(delayInClientPublish > 0) {
											log.info("Waiting for specified time (delayInMilliSeconds) - "+delayInClientPublish);
											try {
												Thread.sleep(delayInClientPublish);
											} catch (InterruptedException e) {
												//e.printStackTrace();
											}
											delayInClientPublish = 0;
										}
										streamToServer.write(request, 0, bytesRead);
										streamToServer.flush();
									}
								} catch (IOException ex){
									try {
										client.close();
										server.close();
									} catch (IOException e) {
										
									}
								}
							}
						};
					
					thread.start();
					
					int bytesRead;
					try {
							while ((bytesRead = streamFromServer.read(reply))!= -1){
								if(delayInServerResponse > 0) {
									log.info("Waiting for specified time (delayInMilliSeconds) - "+delayInServerResponse);
									try {
										Thread.sleep(delayInServerResponse);
									} catch (InterruptedException e) {
										//e.printStackTrace();
									}
									delayInServerResponse = 0;
								}
								streamToClient.write(reply, 0, bytesRead);
								streamToClient.flush();
							}
						
					 } catch (IOException ex){
						 client.close();
						 server.close();
					}
					
					streamToClient.close();
					}  catch (IOException ex) {
						//ex.printStackTrace();
						break;
					} finally {
						try {
							if(server != null){
								server.close();
							}
							if(client != null){
								client.close();
							}
						} catch(IOException ex) {
						}
					}
				}
			}
			log.fine("Proxy Server: Proxy Server Thread finishing..");
			if(!serverSocket.isClosed()){
				serverSocket.close();
			}
			log.fine("Proxy Server: Server Socket Closed, returning...");
			
		} catch(IOException ex) {
//			log.warning("Proxy: 5 Thread Connection lost: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public int getlocalDevicePort() {
		return localDevicePort;
	}

	public boolean isPortSet() {
		return portNumberSet;
	}
}
