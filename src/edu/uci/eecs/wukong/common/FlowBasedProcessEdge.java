package edu.uci.eecs.wukong.common;

import java.util.List;

public class FlowBasedProcessEdge implements Comparable<FlowBasedProcessEdge>{

	private WuClass inWuClass;
	private WuClass outWuClass;
	private List<WuDevice> targetDevices;
	int dataVolumn; //bits
	double transmissionEnergy;  //distance unaware energy consumption
	double receivingEnergy;
	double weight;
	private boolean isMerged;
	
	public FlowBasedProcessEdge(WuClass inWuClass, WuClass outWuClass, int dataVolumn) {
		this.inWuClass = inWuClass;
		this.outWuClass = outWuClass;
		this.dataVolumn = dataVolumn;
		this.transmissionEnergy = 50 /*nJ/bit*/ * this.dataVolumn * 1.4 /*layout parameter*/ ;
		this.receivingEnergy = 50 /*nJ/bit*/ * this.dataVolumn;
		this.weight = this.transmissionEnergy + this.receivingEnergy;
		this.isMerged = false;
	}
	
	@Override
	public int compareTo(FlowBasedProcessEdge edge){
		
		if(this.weight > edge.weight) {
			return -1;
		} else if (this.weight < edge.weight) {
			return 1;
		} else 
			return 0;
	}
	
	public void merge() {
		
		if(inWuClass.isDeployed() && outWuClass.isDeployed() 
				&& inWuClass.getDevice().getWuDeviceId() == outWuClass.getDevice().getWuDeviceId()) {
			//this.transmissionEnergy = 0.0;
			//this.receivingEnergy = 0.0;
			//this.weight = 0.0;
			this.isMerged = true;
		}
	}
	
	public void unmerge(){
		this.isMerged = false;
	}
	public List<WuDevice> getTargetDevices() {
		return targetDevices;
	}

	public void setTargetDevices(List<WuDevice> targetDevices) {
		this.targetDevices = targetDevices;
	}

	public int getDataVolumn() {
		return dataVolumn;
	}

	public void setDataVolumn(int dataVolumn) {
		this.dataVolumn = dataVolumn;
	}
	
	public WuClass getInWuClass() {
		return inWuClass;
	}

	public void setInWuClass(WuClass inWuClass) {
		this.inWuClass = inWuClass;
	}

	public WuClass getOutWuClass() {
		return outWuClass;
	}

	public void setOutWuClass(WuClass outWuClass) {
		this.outWuClass = outWuClass;
	}
	
	public boolean isUndeployed() {
		
		return !inWuClass.isDeployed() && !outWuClass.isDeployed();
	}
	
	public boolean isFullDeployed() {
		
		return inWuClass.isDeployed() && outWuClass.isDeployed();
	}
	
	public boolean isPartialDeployed() {
		
		return !isFullDeployed() && (inWuClass.isDeployed() || outWuClass.isDeployed());
	}
	
	public Integer getUndeployedClassId() {
		
		if (isFullDeployed() || !isPartialDeployed()) {
			return null;
		} else {
			
			if (inWuClass.isDeployed()) {
				return outWuClass.getWuClassId();
			} else {
				return inWuClass.getWuClassId();
			}
			
		}
	}
	
	public boolean isMerged() {
		if( (this.getInWuClass().getDevice().getWuDeviceId() != -1 )&& (this.getOutWuClass().getDevice().getWuDeviceId() != -1)){
			return this.isMerged || this.getInWuClass().getDevice().getWuDeviceId() == this.getOutWuClass().getDevice().getWuDeviceId();
		}
		return false;
	}
	
	public Integer getPartiallyDeployedDeviceId() {
		if (isFullDeployed() || !isPartialDeployed()) {
			return null;
		} else {
			if(inWuClass.isDeployed()) {
				return inWuClass.getDevice().getWuDeviceId();
			} else {
				return outWuClass.getDevice().getWuDeviceId();
			}
		}
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public String toString(){
		return "Edge: " + this.inWuClass.getWuClassId() + ", " + this.outWuClass.getWuClassId();
	}
}
