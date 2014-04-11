package edu.uci.eecs.wukong.energy.mapper;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowGraph;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

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
		ImmutableList<FlowGraph> graphs = split(mergableEdges);
		
		
		
		return mergableEdges;
	}
	
	private ImmutableList<FlowGraph> split(ImmutableList<Edge> mergableEdges) {
		List<FlowGraph> graphs= new LinkedList<FlowGraph>();
		graphs.add(new FlowGraph());
		
		for(Edge edge : mergableEdges) {
			FlowGraph first = null;
			FlowGraph second = null;
			for(FlowGraph graph : graphs) {
				if(graph.isConnect(edge)) {
					if(first == null) {
						first = graph;
					} else if (second == null){
						second = graph;
					}
				}
			}
			
			if(first == null) {
				//create a new graph
				FlowGraph graph = new FlowGraph();
				graph.addEdge(edge);
				graphs.add(graph);
			} else if (second == null) {
				first.addEdge(edge);
			} else {
				// merge two graph
				first.addEdge(edge);
				first.merge(second);
				graphs.remove(second);
			}
		}
	
		return ImmutableList.<FlowGraph>builder().addAll(graphs).build();
	}

}
