package edu.uci.eecs.wukong.common;

import edu.uci.eecs.wukong.common.Gateway;
import edu.uci.eecs.wukong.common.Region;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines all of the devices under multiple regions, in which
 * there are shared gateways.
 * 
 * @author peter
 */
public class CongestionZone {
	private static int id = 0;
	private int zoneId;
	private Set<WuDevice> devices;
	private Set<Region> regions;
	private Set<Gateway> gateways;
	
	public CongestionZone() {
		this.zoneId = id ++;
		this.devices = new HashSet<WuDevice> ();
		this.regions = new HashSet<Region> ();
		this.gateways = new HashSet<Gateway> ();
	}
	
	public void addDevice(WuDevice device) {
		devices.add(device);
		regions.add(device.getRegion());
		gateways.add(device.getGateway());
	}
	
	public void addDevices(List<WuDevice> devices) {
		for (WuDevice device : devices) {
			addDevice(device);
		}
	}
	
	public Set<Gateway> getGateways() {
		return this.gateways;
	}
	
	public Set<Region> getRegions() {
		return this.regions;
	}
	
	public boolean isCongestable(CongestionZone zone) {
		for (Region region : regions) {
			if (zone.regions.contains(region)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void join(CongestionZone zone) {
		devices.addAll(zone.devices);
		regions.addAll(zone.regions);
		gateways.addAll(zone.gateways);
	}
	
	public int getRegionNumber() {
		return this.regions.size();
	}
}
