package edu.uci.eecs.wukong.common;

import edu.uci.eecs.wukong.common.WuDevice;

import java.util.HashMap;
import java.util.Map;

public class Region {
	private int regionId;
	private Map<Integer, WuDevice> devices;
	
	public Region(int regionId) {
		this.regionId = regionId;
		this.devices = new HashMap<Integer, WuDevice> ();
	}
	
	public void addDevice(WuDevice device) {
		if (!devices.containsKey(device.getWuDeviceId())) {
			this.devices.put(device.getWuDeviceId(), device);
			device.setRegion(this);
		}
		
	}
}
