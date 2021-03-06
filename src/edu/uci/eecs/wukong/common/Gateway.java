package edu.uci.eecs.wukong.common;

import edu.uci.eecs.wukong.common.WuDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Gateway {
	private static int id = 0;
	private int gatewayId;
	private Map<Integer, WuDevice> devices;
	
	public Gateway() {
		this.gatewayId = id ++;
		this.devices = new HashMap<Integer, WuDevice> ();
	}
	
	public int getGatewayId() {
		return gatewayId;
	}
	
	public List<WuDevice> getAllDevices(){
		List<WuDevice> ds = new ArrayList<WuDevice>(this.devices.values());
		return ds;
	}
	
	public void addDevice(WuDevice device) {
		if (!devices.containsKey(device.getWuDeviceId())) {
			this.devices.put(device.getWuDeviceId(), device);
			device.setGateway(this);
		}
	}
	
	public boolean deploy(WuClass wuClass) {
		Iterator<WuDevice> deviceIter = devices.values().iterator();
		while(deviceIter.hasNext()) {
			WuDevice device = deviceIter.next();
			if (device.deployComponent(wuClass)) {
				wuClass.deploy(device);
				System.out.println("Deploy wuClassId " + wuClass.getWuClassId() + " at device " + device.getWuDeviceId());
				return true;
			}
		}
		return false;
	}
	
	public int getDeviceNumber() {
		return this.devices.size();
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
	
	public List<WuDevice> getTargetDevice(FlowBasedProcess process) {
		List<WuDevice> result = new ArrayList<WuDevice> ();
		
		for (WuDevice device : devices.values()) {
			if (process.isTarget(device)) {
				result.add(device);
			}
		}
		
		return result;
	}
}
