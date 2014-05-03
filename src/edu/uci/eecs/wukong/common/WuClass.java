package edu.uci.eecs.wukong.common;

import edu.uci.eecs.wukong.common.FlowBasedProcess.LocationConstraint;

public class WuClass {
//	private int wuClassId;
//	private int deviceId;
//	private LocationConstraint locationConstraint;
//	private boolean deployed;
//	private Double energyCost; //It is used after merge.

	public int wuClassId;
	public int deviceId;
	public LocationConstraint locationConstraint;
	public boolean deployed;
	public Double energyCost; //It is used after merge.

	
	public WuClass(int wuClassId, LocationConstraint locationConstraint) {
		this.wuClassId = wuClassId;
		this.deviceId = -1;
		this.locationConstraint = locationConstraint;
		this.deployed = false;
		this.energyCost = 0.0;
	}
	
	public void reset() {
		this.deviceId = -1;
		this.deployed = false;
		this.energyCost = 0.0;
	}
	
	public void deploy(int deviceId) {
		this.deviceId = deviceId;
		this.deployed = true;
	}
	
	public void undeploy() {
		this.deployed = false;
	}

	public int getWuClassId() {
		return wuClassId;
	}

	public void setWuClassId(int wuClassId) {
		this.wuClassId = wuClassId;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
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
	private void setEnergyCost(Double energy) {
		this.energyCost = energy;
	}
	
}