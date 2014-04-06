package edu.uci.eecs.wukong.energy.mapper;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;

/**
 * It is the algorithm for TETS Journal which is using the optimal greedy algorithm
 * for weighted set cover for the first step of mapping.
 * 
 * A FBP can be divided into several sub-graph which are mergeable links by removing
 * unmergable links. Then, an optimal greedy algorithm for weighted set cover can be 
 * used for each sub-graph.
 * 
 * 
 * @author Peter
 *
 */

public class OptimalGreedyBasedMapper extends AbstractMapper {
	
	
	public OptimalGreedyBasedMapper(WukongSystem system, FlowBasedProcess fbp,
			MapType type) {
		super(system, fbp, type);
		// TODO Auto-generated constructor stub
	}

	public void map() {
		
	}

}
