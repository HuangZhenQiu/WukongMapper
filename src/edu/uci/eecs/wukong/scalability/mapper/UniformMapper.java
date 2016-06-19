package edu.uci.eecs.wukong.scalability.mapper;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uci.eecs.wukong.common.CongestionZone;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.Gateway;
import edu.uci.eecs.wukong.common.Region;
import edu.uci.eecs.wukong.common.WuClass;
import edu.uci.eecs.wukong.common.WuDevice;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.mapper.AbstractMapper;

/**
 * Uniform Mapper implements the mapping algorithm that uniformly choose one of 
 * the gateway, and find a device under that gateway to host target component.
 * The outcome of this algorithm is to balance the load of each gateway.
 * 
 * @author peter
 *
 */
public class UniformMapper extends AbstractMapper {

	public UniformMapper(WukongSystem system, FlowBasedProcess fbp, MapType type) {
		super(system, fbp, type);
	}

	@Override
	public boolean map() {
		boolean success = true;
		int seed = 1;
		Random random = new Random();
		List<CongestionZone> zones = this.system.getCongestionZones(this.fbp);
		for (CongestionZone zone : zones) {
			Iterator<Region> regIter = zone.getRegions().iterator();
			System.out.println("Deploy in zone " + zone.getZoneId());
			while (regIter.hasNext()) {
				Region region = regIter.next();
				if (region.deployable(fbp)) { 
					List<Gateway> gateways = region.getAllGateways();
					for (WuClass wuClass : this.fbp.getAllComponents()) {					
						if (!wuClass.isVirtual()) {
							int time = 0;
//							while (!wuClass.isDeployed() &&  time < 50) {
//								int gatewayIndex = Math.abs(random.nextInt() % gateways.size());
//								Gateway gateway = gateways.get(gatewayIndex);
//								if (gateway.deploy(wuClass)) {
//									break;
//								}
//								time ++;
//							}
							
							List<WuDevice> devices = region.getHostableDevice(wuClass.getWuClassId());
							time = 0;
							while(!wuClass.isDeployed() && time < 50) {
								random.setSeed(System.nanoTime() + (seed) * (seed++));
								int deviceIndex = random.nextInt(devices.size());
								WuDevice device = devices.get(deviceIndex);
								if (device.deployComponent(wuClass)) {
									System.out.println("Deploy wuClassId " + wuClass.getWuClassId() + " at device " + device.getWuDeviceId());
									wuClass.deploy(device);
									break;
								}
								time ++;
							}
							
							if (!wuClass.isDeployed()) {
								// In worst case
								for (WuDevice device : devices) {
									if (device.deployComponent(wuClass)) {
										wuClass.deploy(device);
										System.out.println("Guess wrong for more than " + time);
										break;
									}
								}
								if (!wuClass.isDeployed()) {
									success = false;
									System.out.println("Can't find mapping for Component "
											+ wuClass.getWuClassId() + " in region " + region.getRegionId()
											+ " congestion zone " + zone.getZoneId());
								}
							}
							
							
						} else {
							region.deployAtRondomDevice(wuClass);
						}
					}
	
					latencyHops.add(fbp.getLatencyHop(MAX_HOP));
					fbp.reset();
				}
			}
		}
		
		return success;
	}

}
