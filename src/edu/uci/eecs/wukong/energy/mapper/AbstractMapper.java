package edu.uci.eecs.wukong.energy.mapper;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;

public abstract class AbstractMapper implements Mapper {
	
	protected WukongSystem system;
	protected FlowBasedProcess fbp;
	protected MapType type;
	
	public AbstractMapper(WukongSystem system, FlowBasedProcess fbp, MapType type) {
		
		this.system = system;
		this.fbp = fbp;
		this.system.setCurrentFBP(fbp);
		this.type = type;
	}

	public abstract void map();

}
