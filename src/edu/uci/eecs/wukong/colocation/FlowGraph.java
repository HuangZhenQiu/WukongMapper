package edu.uci.eecs.wukong.colocation;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WuClass;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class FlowGraph {
	
	
	
	private Set<Integer> wuClassSet;
	private List<Edge> edges;
	public List<Edge> getEdges(){
		return edges;
	}
	public FlowGraph() {
		wuClassSet = new HashSet<Integer>();
		edges =  new ArrayList<Edge>();
	}
	
	public void addEdge(Edge edge) {
		if(!edges.contains(edge)) {
//		if(!edges.contains(edge) && isConnect(edge)) {
			edges.add(edge);
			wuClassSet.add(edge.getInWuClass().getWuClassId());
			wuClassSet.add(edge.getOutWuClass().getWuClassId());
		}
	}
	
	private boolean isConnect(WuClass wuClass) {
		if(wuClassSet.contains(wuClass.getWuClassId())){
			return true;
		}
		
		return false;
	}
	
	public boolean isConnect(Edge edge) {
		if(isConnect(edge.getInWuClass()) || isConnect(edge.getOutWuClass())) {
			return true;
		}
		
		return false;
	}
	
	public void merge(FlowGraph newGraph) {
		wuClassSet.addAll(newGraph.wuClassSet);
		edges.addAll(newGraph.edges);
	}
	
	public void print(){
		System.out.print("Flowgraph:");
		for(Integer wuclass : wuClassSet){
			System.out.print(wuclass + " ");
		}
		System.out.println("");
		for(int i = 0; i < edges.size(); i++){
			System.out.println("Edge<" + edges.get(i).getInWuClass().getWuClassId() + ", " + edges.get(i).getOutWuClass().getWuClassId() + ">");
		}
	}
}
