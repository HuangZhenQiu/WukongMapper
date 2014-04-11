package edu.uci.eecs.wukong.common;

import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.FlowBasedProcess.WuClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class FlowGraph {
	
	public static class EdgeNode {
		protected static int currentId = 0;
		private int id;
		private Edge edge;
		private double weight;
		
		public EdgeNode(Edge edge) {
			this.id = currentId++;
			this.edge = edge;
			this.weight = edge.getWeight();
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public Edge getEdge() {
			return edge;
		}

		public void setEdge(Edge edge) {
			this.edge = edge;
		}

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.weight = weight;
		}
		
		
	}
	
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
}
