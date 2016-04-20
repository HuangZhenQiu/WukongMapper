package edu.uci.eecs.wukong.scalability.mapper;

import java.util.Iterator;
import java.util.List;

import edu.uci.eecs.wukong.common.CongestionZone;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.Region;
import edu.uci.eecs.wukong.common.WuClass;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.mapper.AbstractMapper;

/**
 * Static Mapper implements the first candidate algorithm in which
 * each component map to the device with lowest device id.
 * 
 * 
 * @author peter
 *
 */
public class StaticMapper extends AbstractMapper {

	public StaticMapper(WukongSystem system, FlowBasedProcess fbp, MapType type) {
		super(system, fbp, type);
	}

	@Override
	public boolean map() {
		boolean success = true;
		List<CongestionZone> zones = this.system.getCongestionZones(this.fbp);
		for (CongestionZone zone : zones) {
			Iterator<Region> regIter = zone.getRegions().iterator();
			while (regIter.hasNext()) {
				Region region = regIter.next();
				if (region.deployable(fbp)) { 
					for (WuClass wuClass : this.fbp.getAllComponents()) {
						if (!region.hostFirstDevice(wuClass)) {
							// System.out.println("Mapping fail in Region " + region.getRegionId());
							success = false;
							break;
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
