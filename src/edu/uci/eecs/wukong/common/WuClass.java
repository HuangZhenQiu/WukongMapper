package edu.uci.eecs.wukong.common;

import edu.uci.eecs.wukong.common.LocationConstraint;

public class WuClass {

	private int wuClassId;
	private WuDevice wudevice;
	private LocationConstraint locationConstraint;
	private boolean virtual;
	private boolean deployed;
	private Double energyCost; //It is used after merge.


	public WuClass(int wuClassId, LocationConstraint locationConstraint) {
		this.wuClassId = wuClassId;
		this.wudevice = null;
		this.locationConstraint = locationConstraint;
		this.deployed = false;
		this.energyCost = 0.0;
	}
	
	public void reset() {
		this.wudevice = null;
		this.deployed = false;
		this.wudevice = null;
		this.energyCost = 0.0;
	}
	
	public boolean isVirtual() {
		return this.virtual;
	}
	
	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}
	
	public synchronized void deploy(WuDevice device) {
		this.wudevice = device;
		this.deployed = true;
	}
	
	public synchronized void undeploy() {
		if (deployed) {
			this.wudevice.undeployComponent(this);
			this.deployed = false;
		}
	}

	public int getWuClassId() {
		return wuClassId;
	}

	public void setWuClassId(int wuClassId) {
		this.wuClassId = wuClassId;
	}

	public WuDevice getDevice() {
		return this.wudevice;
	}

	public void setDevice(WuDevice device) {
		this.wudevice = device;
	}

	public boolean isDeployed() {
		return deployed;
	}

	public void setDeployed(boolean deployed) {
		this.deployed = deployed;
	}
	
	public LocationConstraint getLocationConstraint() {
		return this.locationConstraint;
	}

	public Double getEnergyCost() {
		return energyCost;
	}

	//It is only used by FBP itself, after merge.
	public void setEnergyCost(Double energy) {
		this.energyCost = energy;
	}
	
	public boolean equal(WuClass wuclass){
		return this.wuClassId == wuclass.wuClassId;
	}
}