package com.ibm.iotf.test.common;

import java.beans.PropertyChangeEvent;

import com.ibm.wiotp.sdk.util.LoggerUtility;

public class TestPropertyChangeListener implements java.beans.PropertyChangeListener{
	static final String CLASS_NAME = TestPropertyChangeListener.class.getName();
	private Object oldValue = null;
	private Object newValue = null;
	private String propertyName = null;
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final String METHOD = "propertyChange";
		propertyName = evt.getPropertyName();
		oldValue = evt.getOldValue();
		newValue = evt.getNewValue();
		LoggerUtility.info(CLASS_NAME, METHOD, "Changed Property : " + propertyName);
		LoggerUtility.info(CLASS_NAME, METHOD, "Old value : " + oldValue + " New value : " + newValue);
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public Object getOldValue() {
		return oldValue;
	}
	
	public Object getNewValue() {
		return newValue;
	}

}
