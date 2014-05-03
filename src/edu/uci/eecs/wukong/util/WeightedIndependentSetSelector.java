package edu.uci.eecs.wukong.util;

import edu.uci.eecs.wukong.common.CollocationGraph;
import edu.uci.eecs.wukong.common.CollocationGraphNode;
import edu.uci.eecs.wukong.common.FlowGraph;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.WukongSystem;

import java.util.List;

public class WeightedIndependentSetSelector {
	
	WukongSystem system;
	
	public WeightedIndependentSetSelector(WukongSystem system){
		this.system = system;
	}
	public List<Edge> select(FlowGraph graph) {
		
//		graph.print();
//		List<Edge> edges = new LinkedList<Edge>();
		CollocationGraph collocationGraph = new CollocationGraph(graph, system);
		collocationGraph.print();
		
//		collocationGraph.
		return null;
	}
}
