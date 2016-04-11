package edu.uci.eecs.wukong.common;

import edu.uci.eecs.wukong.common.Gateway;
import edu.uci.eecs.wukong.common.WuDevice;

import java.util.HashMap;
import java.util.Map;

import java.util.HashSet;
import java.util.Set;


public class Region {
	private int regionId;
	private Map<Integer, WuDevice> devices;
	private Set<Gateway> gateways;
	
	public Region(int regionId) {
		this.regionId = regionId;
		this.devices = new HashMap<Integer, WuDevice> ();
		this.gateways = new HashSet<Gateway> ();
	}
	
	public void addDevice(WuDevice device) {
		if (!devices.containsKey(device.getWuDeviceId())) {
			this.devices.put(device.getWuDeviceId(), device);
			device.setRegion(this);
		}
		
		gateways.add(device.getGateway());
	}
	
	public Set<Gateway> getPotentialTargetGateways(FlowBasedProcess process) {
		Set<Gateway> gateways = new HashSet<Gateway> ();
		for (WuDevice device : this.devices.values()){
			if (process.isTarget(device)) {
				gateways.add(device.getGateway());
			}
		}
		
		return gateways;
	}
	
	public CongestionZone createCongestionZone(FlowBasedProcess process) {
		CongestionZone zone =  new CongestionZone();
		for (WuDevice device : this.devices.values()){
			if (process.isTarget(device)) {
				zone.addDevice(device);
			}
		}
		
		return zone;
	}
}
