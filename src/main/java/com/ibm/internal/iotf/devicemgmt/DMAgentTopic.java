/**
 *****************************************************************************
 Copyright (c) 2016-17 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Extended from DeviceClient
 *****************************************************************************
 *
 */
package com.ibm.internal.iotf.devicemgmt;

public interface DMAgentTopic {

	public String getManageTopic();

	public String getUpdateLocationTopic();

	public String getClearDiagErrorCodesTopic();

	public String getClearDiagLogsTopic();

	public String getAddErrorCodesTopic();

	public String getAddDiagLogTopic();

	public String getUnmanageTopic();

	public String getDMServerTopic();

	public String getNotifyTopic();

}
