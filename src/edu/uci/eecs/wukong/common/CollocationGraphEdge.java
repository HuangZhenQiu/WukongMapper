package edu.uci.eecs.wukong.common;

public class CollocationGraphEdge{
	public int inIndex;
	public int outIndex;
	
	/*
	 * 
	 * Each collocation edge represents the dependency of merging. 
	 * If two nodes have a common edge, it means that two merging can not happen at the same time 
	 * 
	 */
	
	public CollocationGraphEdge(int in, int out){
		inIndex = in;
		outIndex = out;
	}
	
	public boolean equals(CollocationGraphEdge edge){
		if(edge.inIndex == this.inIndex && edge.outIndex == this.outIndex){
			return true;
		}
		else if(edge.outIndex == this.inIndex && edge.inIndex == this.outIndex){
			return true;
		}
		return false;
	}
}
