package com.ibm.wiotp.sdk.samples.sigar;

import java.text.DecimalFormat;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInfo;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class SigarData {
	
	private static final DecimalFormat twoDForm = new DecimalFormat("#.##");
    
    
    private String name;
    private double disk;
    private double mem;
    private double cpu;
    
    public SigarData(String name, double disk, double mem, double cpu) {
    	this.name = name;
    	setDisk(disk);
    	setMem(mem);
    	setCpu(cpu);
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
    
    public static SigarData create() throws InterruptedException {
        Sigar sigar = new Sigar();
        return create(sigar);
    }

    public static SigarData create(Sigar sigar) throws InterruptedException {
        try {
	        Mem mem = sigar.getMem();
	        CpuPerc perc = sigar.getCpuPerc();
	        
	        NetInfo info = sigar.getNetInfo();
	        String name = info.getHostName();

	        long totalDiskCapacity = 0;
	        long totalUsedDiskCapacity = 0;
	        
	        FileSystem[] fileSystems = sigar.getFileSystemList();
	        for (FileSystem fs : fileSystems) {
	            if (fs.getType() == FileSystem.TYPE_LOCAL_DISK) {
	            	FileSystemUsage fileSystemUsage = sigar.getFileSystemUsage(fs.getDirName());
	                totalDiskCapacity += fileSystemUsage.getTotal();
	                totalUsedDiskCapacity += fileSystemUsage.getUsed();
	            }
	        }
	        
	        double totalDiskUsage = (totalUsedDiskCapacity / (double)totalDiskCapacity) * 100;
	        
	        SigarData data = new SigarData(name, totalDiskUsage, mem.getUsedPercent(), perc.getCombined());
	        return data;
        } catch (SigarException e) {
        	return  null;
        }
    }

}
