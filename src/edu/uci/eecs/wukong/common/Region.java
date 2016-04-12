package edu.uci.eecs.wukong.common;

import edu.uci.eecs.wukong.common.Gateway;
import edu.uci.eecs.wukong.common.WuDevice;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.Set;


public class Region {
	private static int id = 0;
	private int regionId;
	private Map<Integer, WuDevice> devices;
	private Map<Integer, List<WuDevice>> classToDeviceMap;
	private Set<Gateway> gateways;
	
	public Region() {
		this.regionId = id ++;
		this.devices = new HashMap<Integer, WuDevice> ();
		this.gateways = new HashSet<Gateway> ();
		this.classToDeviceMap = new HashMap<Integer, List<WuDevice>> ();
	}
	
	public void addDevice(WuDevice device) {
		if (!devices.containsKey(device.getWuDeviceId())) {
			this.devices.put(device.getWuDeviceId(), device);
			device.setRegion(this);
			
			for (Integer wuClassId : device.getAllWuObjectClassId()) {
				if(!classToDeviceMap.containsKey(wuClassId)) {
					List<WuDevice> devices = new ArrayList<WuDevice> ();
					classToDeviceMap.put(wuClassId, devices);
				}
				
				classToDeviceMap.get(wuClassId).add(device);
			}
		}
		
		gateways.add(device.getGateway());
	}
	
	public int getRegionId() {
		return this.regionId;
	}
	
	public Map<Integer, List<WuDevice>> getWuClassToDeviceMap() {
		return this.classToDeviceMap;
	}
	
	public List<WuDevice> getHostableDevice(Integer wuClassId) {
		return classToDeviceMap.get(wuClassId);
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
