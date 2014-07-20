package edu.uci.eecs.wukong.bipartite;

public class ServiceBipartiteGraphEdge {


	private ServiceBipartiteGraphNode nodeStart = null;
	private ServiceBipartiteGraphNode nodeEnd = null;
	
	private double weight = 0;

	public ServiceBipartiteGraphEdge(ServiceBipartiteGraphNode node1,ServiceBipartiteGraphNode node2) {
		this.nodeStart = node1;
		this.nodeEnd = node2;		
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public double getWeight(){
		return this.weight;
	}
	public ServiceBipartiteGraphNode getLeft(){
		return nodeStart;
	}
	public ServiceBipartiteGraphNode getRight(){
		return nodeEnd;
	}
}
