package edu.uci.eecs.wukong.common;

import edu.uci.eecs.wukong.common.WuDevice;

import java.util.HashMap;
import java.util.Map;

public class Gateway {
	private int gatewayId;
	private Map<Integer, WuDevice> devices;
	
	public Gateway(int gatewayId) {
		this.gatewayId = gatewayId;
		this.devices = new HashMap<Integer, WuDevice> ();
	}
	
	public void addDevice(WuDevice device) {
		if (!devices.containsKey(device.getWuDeviceId())) {
			this.devices.put(device.getWuDeviceId(), device);
			device.setGateway(this);
		}
	}
	
	public int reprogramDeviceNumber() {
		int number = 0;
		for (WuDevice device : devices.values()) {
			if (device.isEnabled()) {
				number += 1;
			}
		}
		
		return number;
	}
	
	/**
	 * Check whether a gateway connects with a target device
	 * 
	 * @param process the given process need to map
	 * 
	 * @return
	 */
	public boolean isTargetGateway(FlowBasedProcess process) {
		for (WuDevice device : devices.values()) {
			if (process.isTarget(device)) {
				return true;
			}
		}
		
		return false;
	}
}
