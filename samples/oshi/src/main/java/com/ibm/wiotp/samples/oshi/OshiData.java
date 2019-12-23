package com.ibm.wiotp.samples.oshi;

import java.text.DecimalFormat;
import org.joda.time.DateTime;

import com.ibm.wiotp.sdk.MessageInterface;

public class OshiData implements MessageInterface<OshiData> {
	private static final DecimalFormat twoDForm = new DecimalFormat("#.##");

	private String name;
	private double mem;
	private double cpu;
	private DateTime timestamp;

	public OshiData() {
		this(null, -1, -1, null);
	}

	public OshiData(String name, double mem, double cpu) {
		this(name, mem, cpu, null);
	}

	public OshiData(String name, double mem, double cpu, DateTime timestamp) {
		this.name = name;
		setMem(mem);
		setCpu(cpu);
		this.timestamp = timestamp;
	}

	public String getName() {
		return name;
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

	public void setMem(double mem) {
		this.mem = Double.valueOf(twoDForm.format(mem));
	}

	public void setCpu(double cpu) {
		this.cpu = Double.valueOf(twoDForm.format(cpu * 100));
	}

	public String toString() {
		return this.name + ":" + "/" + this.mem + "/" + this.cpu;
	}

	@Override
	public OshiData getData() {
		return this;
	}

	@Override
	public DateTime getTimestamp() {
		return timestamp;
	}
}
