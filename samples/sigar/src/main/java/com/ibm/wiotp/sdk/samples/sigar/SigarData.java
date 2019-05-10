package com.ibm.wiotp.sdk.samples.sigar;

import java.text.DecimalFormat;
import org.joda.time.DateTime;

import com.ibm.wiotp.sdk.MessageInterface;

public class SigarData implements MessageInterface<SigarData>{
	private static final DecimalFormat twoDForm = new DecimalFormat("#.##");
    
    private String name;
    private double disk;
    private double mem;
    private double cpu;
    private DateTime timestamp;
    
    public SigarData() {
    	this(null, -1, -1, -1, null);
    }
    
    public SigarData(String name, double disk, double mem, double cpu) {
    	this(name, disk, mem, cpu, null);
    }

    public SigarData(String name, double disk, double mem, double cpu, DateTime timestamp) {
    	this.name = name;
    	setDisk(disk);
    	setMem(mem);
    	setCpu(cpu);
    	this.timestamp = timestamp;
    }

    public String getName() {
    	return name;
    }
    
    public double getDisk() {
    	return disk;
    }

    public double getMem() {
    	return mem;
    }

    public double getCpu() {
    	return cpu;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
    public void setDisk(double disk) {
    	this.disk = Double.valueOf(twoDForm.format(disk));
    }

    public void setMem(double mem) {
    	this.mem = Double.valueOf(twoDForm.format(mem));
    }

    public void setCpu(double cpu) {
    	this.cpu = Double.valueOf(twoDForm.format(cpu * 100));
    }
    
    public String toString() {
    	return this.name + ":" + "/" + this.disk + "/" + this.mem + "/" + this.cpu;    	
    }
    
	@Override
	public SigarData getData() {
		return this;
	}

	@Override
	public DateTime getTimestamp() {
		return timestamp;
	}
}
