package edu.uci.eecs.wukong.common;

public class WuObject {
	
	private Integer wuClassId;
	private WuDevice device;
	private boolean active;
	
	public WuObject(Integer wuClassId, WuDevice device) {
		this.wuClassId = wuClassId;
		this.device = device;
		this.active = false;
	}
	
	public Integer getWuClassId() {
		return this.wuClassId;
	}
	
	public boolean isActive() {
		return active;
	}
	
	
	//They are only accessible by WuDevice
	protected void activate() {
		this.active = true;
	}
	
	protected void deactivate() {
		this.active = false;
	}
	
}
