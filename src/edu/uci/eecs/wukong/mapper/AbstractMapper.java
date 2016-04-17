package edu.uci.eecs.wukong.mapper;

import java.util.ArrayList;
import java.util.List;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;

public abstract class AbstractMapper implements Mapper {
	public static int MAX_HOP = 10;
	protected WukongSystem system;
	protected FlowBasedProcess fbp;
	protected MapType type;
	protected List<Integer> latencyHops;
	
	public AbstractMapper(WukongSystem system, FlowBasedProcess fbp, MapType type) {
		
		this.system = system;
		this.fbp = fbp;
		this.system.setCurrentFBP(fbp);
		this.type = type;
		this.latencyHops = new ArrayList<Integer> ();
	}

	public abstract boolean map();
	
	public float getMissDeadlineRatio() {
		float missCounter = 0;
		if (latencyHops.size() == 0) {
			return missCounter;
		}
			
		for (int i = 0; i < latencyHops.size(); i++) {
			if (latencyHops.get(i) > MAX_HOP) {
				missCounter += 1;
			}
		}
		
		return missCounter / latencyHops.size();
	}

}
