/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
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

public interface DMServerTopic {

	String getDMServerTopic();

	String getDeviceUpdateTopic();

	String getObserveTopic();

	String getCancelTopic();

	String getInitiateRebootTopic();

	String getInitiateFactoryReset();

	String getInitiateFirmwareDownload();

	String getInitiateFirmwareUpdate();

}
