package edu.uci.eecs.wukong.common;

import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.FlowBasedProcess.WuClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class FlowGraph {
	
	private Set<Integer> wuClassSet;
	private List<Edge> edges;
	
	public FlowGraph() {
		wuClassSet = new HashSet<Integer>();
		edges =  new ArrayList<Edge>();
	}
	
	public void addEdge(Edge edge) {
		if(!edges.contains(edge) && isConnect(edge)) {
			edges.add(edge);
			wuClassSet.add(edge.getInWuClass().getWuClassId());
			wuClassSet.add(edge.getOutWuClass().getWuClassId());
		}
	}
	
	public boolean isConnect(WuClass wuClass) {
		if(wuClassSet.contains(wuClass.getWuClassId())){
			return true;
		}
		
		return false;
	}
	
	private boolean isConnect(Edge edge) {
		if(isConnect(edge.getInWuClass()) || isConnect(edge.getOutWuClass())) {
			return true;
		}
		
		return false;
	}
	
	public void merge(FlowGraph newGraph) {
		wuClassSet.addAll(newGraph.wuClassSet);
		edges.addAll(newGraph.edges);
	}
	

}
