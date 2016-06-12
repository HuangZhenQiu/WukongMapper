package edu.uci.eecs.wukong.common;

import edu.uci.eecs.wukong.common.Gateway;
import edu.uci.eecs.wukong.common.Region;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Defines all of the devices under multiple regions, in which
 * there are shared gateways.
 * 
 * @author peter
 */
public class CongestionZone {
	private static int id = 0;
	private int zoneId;
	private SortedSet<WuDevice> devices;
	private SortedSet<Region> regions;
	private SortedSet<Gateway> gateways;
	
	public CongestionZone() {
		this.zoneId = id ++;
//		this.devices = new HashSet<WuDevice> ();
//		this.regions = new HashSet<Region> ();
//		this.gateways = new HashSet<Gateway> ();
		this.devices = new TreeSet<WuDevice> (new SortWuDeviceById());
		this.regions = new TreeSet<Region> (new SortRegionById());
		this.gateways = new TreeSet<Gateway> (new SortGatewayById());
	}
	
	public int getZoneId() {
		return this.zoneId;
	}
	
	public void addDevice(WuDevice device) {
		devices.add(device);
		gateways.add(device.getGateway());
		regions.add(device.getRegion());
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
	   /*for (Gateway gateway : zone.getGateways()) {
			if (gateways.contains(gateway)) {
				return true;
			}
		}*/
		
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

class SortWuDeviceById implements Comparator<Object>{  
	  
    @Override  
    public int compare(Object o1, Object o2) {  
    	Integer th1=((WuDevice)o1).getWuDeviceId();  
    	Integer th2=((WuDevice)o2).getWuDeviceId();  
        return th1.compareTo(th2);  
    }  
}

class SortRegionById implements Comparator<Object>{  
	  
    @Override  
    public int compare(Object o1, Object o2) {  
    	Integer th1=((Region)o1).getRegionId();  
    	Integer th2=((Region)o2).getRegionId();  
        return th1.compareTo(th2);  
    }  
}

class SortGatewayById implements Comparator<Object>{  
	  
    @Override  
    public int compare(Object o1, Object o2) {  
    	Integer th1=((Gateway)o1).getGatewayId();  
    	Integer th2=((Gateway)o2).getGatewayId();  
        return th1.compareTo(th2);  
    }  
}
