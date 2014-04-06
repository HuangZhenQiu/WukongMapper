package edu.uci.eecs.wukong.energy.mapper;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;

/**
 * It is the algorithm for MEDES 2014 paper which consider the energy consumption 
 * of transmission distance.
 * 
 * Use rule to reduce Mixed Integer None-linear Programming Problem to MIP problem.
 *
 * @author Peter
 *
 */

public class DistanceAwareSelectionBasedMapper extends AbstractMapper{
	private boolean relaxation;
	
	public DistanceAwareSelectionBasedMapper(WukongSystem system,
			FlowBasedProcess fbp, MapType type, boolean relaxation) {
		super(system, fbp, type);
		this.relaxation = relaxation;
		// TODO Auto-generated constructor stub
	}

	public void map() {
		
	}
}
