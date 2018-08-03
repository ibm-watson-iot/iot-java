package com.ibm.iotf.sample.gateway;

import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

public class SystemObject {
	
	public long getMemoryUsed() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024;
    }
	
	private static final String osInfo;
	
	static {
       StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("os.name"));
        sb.append(":");
        sb.append(System.getProperty("os.version"));
        sb.append(":");
        sb.append(System.getProperty("os.arch"));
        
        String str = sb.toString();
        osInfo = str.replace(' ', '_');
    }
	
	public static String getName() {
		return osInfo;
	}
	
	/**
	 * Method to get the current CPU usage using the Operating System Mbean 
	 * @return
	 * @throws MalformedObjectNameException
	 * @throws ReflectionException
	 * @throws InstanceNotFoundException
	 */
	 public double getProcessCpuLoad() throws MalformedObjectNameException, 
						ReflectionException, InstanceNotFoundException {

	    MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
	    ObjectName name	= ObjectName.getInstance("java.lang:type=OperatingSystem");
	    AttributeList list = beanServer.getAttributes(name, new String[]{ "ProcessCpuLoad" });

	    if (list.isEmpty()) {
	    	return 0;
	    }

	    Attribute att = (Attribute)list.get(0);
	    Double value  = (Double)att.getValue();

	    // In general it takes couple of seconds before we get a real value
	    if (value <= -1.0)      return 0; 

	    // returns a percentage value with 1 decimal point precision
	    return ((int)(value * 1000) / 10.0); 
	}
}
