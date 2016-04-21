package edu.uci.eecs.wukong.common;

import edu.uci.eecs.wukong.common.Gateway;
import edu.uci.eecs.wukong.common.WuDevice;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.Set;


public class Region {
	private static int id = 0;
	private static Random random = new Random();
	private int regionId;
	private WukongSystem system;
	private Map<Integer, WuDevice> deviceMap;
	private List<WuDevice> devices;
	private Map<Integer, List<WuDevice>> classToDeviceMap;
	private Set<Gateway> gateways;
	
	public static class DevicePair {
		public WuDevice start;
		public WuDevice end;
		
		public DevicePair(WuDevice start, WuDevice end) {
			this.start = start;
			this.end = end;
		}
	}
	   
	public Region(WukongSystem system) {
		this.regionId = id ++;
		this.deviceMap = new HashMap<Integer, WuDevice> ();
		this.devices = new ArrayList<WuDevice> ();
		this.gateways = new HashSet<Gateway> ();
		this.system = system;
		this.classToDeviceMap = new HashMap<Integer, List<WuDevice>> ();
	}
	
	public void removeDeviceForClass(Integer wuClassId, WuDevice device) {
		if (classToDeviceMap.containsKey(wuClassId)) {
			classToDeviceMap.get(wuClassId).remove(device);
		}
	}
	
	public boolean deployable(FlowBasedProcess fbp) {
		
		for (WuClass wuClass : fbp.getPhysicalWuClasses()) {
			if (!classToDeviceMap.containsKey(wuClass.getWuClassId())) {
				return false;
			}
		}
		
		return true;
	}
	
	
	public int getRegionId() {
		return this.regionId;
	}
	
	
	public int getDeviceNumber() {
		return deviceMap.size();
	}
	
	public List<DevicePair> getInnerGatewayHostPair(WuClass start, WuClass end) {
		List<DevicePair> pairs = new ArrayList<DevicePair>();
		List<WuDevice> startDevices = classToDeviceMap.get(start.getWuClassId());
		for (WuDevice startDevice : startDevices) {
			for (WuDevice endDevice : classToDeviceMap.get(end.getWuClassId())) {
				if (startDevice.getGateway().equals(endDevice.getGateway())) {
					pairs.add(new DevicePair(startDevice, endDevice));
				}
			}
		}
		
		return pairs;
	}
	
	public List<DevicePair> getCrossGatewayHostPair(WuClass start, WuClass end) {
		List<DevicePair> pairs = new ArrayList<DevicePair>();
		List<WuDevice> startDevices = classToDeviceMap.get(start.getWuClassId());
		for (WuDevice startDevice : startDevices) {
			for (WuDevice endDevice : classToDeviceMap.get(end.getWuClassId())) {
				if (!startDevice.getGateway().equals(endDevice.getGateway())) {
					pairs.add(new DevicePair(startDevice, endDevice));
				}
			}
		}
		
		return pairs;
	}
	
	public void addDevice(WuDevice device) {
		if (!deviceMap.containsKey(device.getWuDeviceId())) {
			this.deviceMap.put(device.getWuDeviceId(), device);
			device.setRegion(this);
			this.devices.add(device);
		}
		
		gateways.add(device.getGateway());
	}
	
	public void loadClassMap() {
		for (WuDevice device : devices) {
			for (Integer wuClassId : device.getAllWuObjectClassId()) {
				if(!classToDeviceMap.containsKey(wuClassId)) {
					List<WuDevice> devices = new ArrayList<WuDevice> ();
					classToDeviceMap.put(wuClassId, devices);
				}
				
				classToDeviceMap.get(wuClassId).add(device);
			}
		}
	}
	
	public void deployAtRondomDevice(WuClass wuClass) {
		if (wuClass.isVirtual()) {
			int index = Math.abs(random.nextInt() % this.devices.size());
			this.devices.get(index).deployComponent(wuClass);
			wuClass.deploy(this.devices.get(index));
		}
	}
	
	public WuDevice getWuDevice(int index) {
		return this.devices.get(index);
	}
	
	public boolean hostFirstDevice(WuClass wuClass) {
		if (!wuClass.isVirtual() && classToDeviceMap.containsKey(wuClass.getWuClassId())) {
			for (WuDevice device : classToDeviceMap.get(wuClass.getWuClassId())) {
				if (device.deployComponent(wuClass)) {
					System.out.println("Deploy wuClassId " + wuClass.getWuClassId() + " at device" + device.getWuDeviceId());
					wuClass.deploy(device);
					return true;
				}
			}
		} else if (wuClass.isVirtual()) {
			this.devices.get(0).deployComponent(wuClass);
			wuClass.deploy(this.devices.get(0));
			return true;
		}
		
		System.out.println("Fail to find host device for WuClass " + wuClass.getWuClassId());
		return false;
	}
	
	public Map<Integer, List<WuDevice>> getWuClassToDeviceMap() {
		return this.classToDeviceMap;
	}
	
	public List<WuDevice> getHostableDevice(Integer wuClassId) {
		return classToDeviceMap.get(wuClassId);
	}
	
	public List<WuDevice> getAllDevices() {
		return this.devices;
	}
	
	public List<Gateway> getAllGateways() {
		return new ArrayList<Gateway>(this.gateways);
	}
	
	public Set<Gateway> getPotentialTargetGateways(FlowBasedProcess process) {
		Set<Gateway> gateways = new HashSet<Gateway> ();
		for (WuDevice device : this.devices){
			if (process.isTarget(device)) {
				gateways.add(device.getGateway());
			}
		}
		
		return gateways;
	}
	
	public CongestionZone createCongestionZone(FlowBasedProcess process) {
		CongestionZone zone =  new CongestionZone();
		for (WuDevice device : this.devices){
			if (process.isTarget(device)) {
				zone.addDevice(device);
			}
		}
		
		return zone;
	}
}
