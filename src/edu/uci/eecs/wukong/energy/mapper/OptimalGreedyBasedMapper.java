package edu.uci.eecs.wukong.energy.mapper;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;

import java.util.List;
import java.util.ArrayList;

import com.google.common.collect.ImmutableList;

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
	
	private ImmutableList<Edge> merge() {
		ImmutableList<Edge> mergableEdges = this.fbp.getMergableEdges(this.system);
		//TODO select mergeable set
		
		return mergableEdges;
	}

}
