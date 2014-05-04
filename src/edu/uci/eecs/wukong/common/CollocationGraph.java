package edu.uci.eecs.wukong.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.util.ObjectCloner;

public class CollocationGraph {
	private ArrayList<CollocationGraphNode> mNodes;
	private List<CollocationGraphEdge> mEdges;
	
	public CollocationGraph(FlowGraph graph, WukongSystem system){
		mNodes = new ArrayList<CollocationGraphNode>();
		mEdges = new ArrayList<CollocationGraphEdge>();
		this.initCollocation(graph, system);
	}

	public void convertFBPEdges(FlowGraph graph) {
		for (Edge fbp_edge : graph.getEdges()) {
			HashSet<Integer> sets = new HashSet<Integer>();
			sets.add(fbp_edge.getInWuClass().getWuClassId());
			sets.add(fbp_edge.getOutWuClass().getWuClassId());

			CollocationGraphNode node = new CollocationGraphNode(sets,
					fbp_edge.getDataVolumn());
			addNode(node);
		}
	}
	
	public void initCollocation(FlowGraph graph, WukongSystem system){
		
		this.convertFBPEdges(graph);
			
		for(int i=0;i<mNodes.size();i++){
			for(int j=0;j<mNodes.size();j++){
				if(i==j){
					continue;
				}
				CollocationGraphNode node1 = mNodes.get(i);
				CollocationGraphNode node2 = mNodes.get(j);
	    		
				if (getIntersection(node1, node2).size() != 0) { //¦³¥æ¶°
					HashSet<Integer> union = getUnion(node1, node2);
	    			if(!system.isHostable(union)){
	    				CollocationGraphEdge edge = new CollocationGraphEdge(node1, node2);
	    				if(!isEdgeExist(edge)){
	    					mEdges.add(edge);
	    				}
	    			}
	    			else{
	    				// hostable @@
	    				
	    				CollocationGraphNode node = new CollocationGraphNode(union, node1.getWeight() + node2.getWeight());
	    				if(addNode(node)){
	    					System.out.println("node1:" + node.getInvolveWuClasses() + " node2: " + node1.getInvolveWuClasses() + " New node " + node.getInvolveWuClasses());
	    				}
	    				else{
	    					System.out.println("node1:" + node.getInvolveWuClasses() + " node2: " + node1.getInvolveWuClasses() + " Node exists" + node.getInvolveWuClasses());
	    				}
	    				
	    				CollocationGraphEdge edge1 = new CollocationGraphEdge(node1, node);
	    				if(!(node1.getInvolveWuClasses().size() == node.getInvolveWuClasses().size() && node1.getInvolveWuClasses().containsAll(node.getInvolveWuClasses())) && addEdge(edge1)){
	    					System.out.println("New edge from" + node1.getInvolveWuClasses() + " to " + node.getInvolveWuClasses() + " <" + node1.getNodeId() + ", " + node.getNodeId() +" >");
	    				}
	    				CollocationGraphEdge edge2 = new CollocationGraphEdge(node2, node);
	    				if(!(node2.getInvolveWuClasses().size() == node.getInvolveWuClasses().size() && node2.getInvolveWuClasses().containsAll(node.getInvolveWuClasses())) && addEdge(edge2)){
	    					System.out.println("New edge from" + node2.getInvolveWuClasses() + " to " + node.getInvolveWuClasses() + " <" + node2.getNodeId() + ", " + node.getNodeId() +" >");
	    				}
	    				
	    				CollocationGraphEdge edge3 = new CollocationGraphEdge(node2, node1);
	    				if(!(node2.getInvolveWuClasses().size() == node1.getInvolveWuClasses().size() && node2.getInvolveWuClasses().containsAll(node1.getInvolveWuClasses())) && addEdge(edge3)){
	    					System.out.println("New edge from" + node2.getInvolveWuClasses() + " to " + node1.getInvolveWuClasses() + " <" + node2.getNodeId() + ", " + node1.getNodeId() +" >");
	    				}
	    				
	    			}
	    		}
	    		
			}
		}
	}
	
	
	private HashSet<Integer> getIntersection(CollocationGraphNode node1, CollocationGraphNode node2){
		HashSet<Integer> intersection = (HashSet<Integer>)ObjectCloner.deepCopy(node1.getInvolveWuClasses());
		intersection.retainAll(node2.getInvolveWuClasses());
		return intersection;
	}
	private HashSet<Integer> getUnion(CollocationGraphNode node1, CollocationGraphNode node2){
		HashSet<Integer> union = (HashSet<Integer>)ObjectCloner.deepCopy(node1.getInvolveWuClasses());
		union.addAll(node2.getInvolveWuClasses());
		return union;

	}
	
	private boolean isNodeExist(CollocationGraphNode node) {
		for (int i = 0; i < mNodes.size(); i++) {
			CollocationGraphNode n = mNodes.get(i);
			if(n.getInvolveWuClasses().size() == node.getInvolveWuClasses().size() && n.getInvolveWuClasses().containsAll(node.getInvolveWuClasses())){ 
				node.setNodeId(n.getNodeId());
				return true;
			}
			
		}
		return false;
	}
	
	private boolean addNode(CollocationGraphNode node){
		if(!isNodeExist(node)){
			mNodes.add(node);
			node.setNodeId(mNodes.indexOf(node));
			return true;
		}
		return false;
	}
	private boolean addEdge(CollocationGraphEdge edge){
		if(!isEdgeExist(edge)){
			mEdges.add(edge);
			return true;
		}
		return false;
	}

	private boolean isEdgeExist(CollocationGraphEdge e) {
		for (CollocationGraphEdge edge : getEdges()) {
			if (edge.equals(e)) {
				return true;
			}
		}
		return false;
	}
	
	public List<CollocationGraphNode> getNodes(){
		return mNodes;
	}
	public List<CollocationGraphEdge> getEdges(){
		return mEdges;
	}
	
	public int getDegree(CollocationGraphNode node) {
		int degree = 0;
		for (CollocationGraphEdge edge : getEdges()) {
			if (edge.isOutLink(node)) {
				degree++; // out degree
			} else if (edge.isInLink(node)) {
				degree++; // in degree
			}
		}
		return degree;
	}

	public CollocationGraphNode getNode(int nodeId) {
		for (int i = 0; i < getNodes().size(); i++) {
			if (getNodes().get(i).getNodeId() == nodeId) {
				return getNodes().get(i);
			}
		}
		return null;
	}

	public double getNeighborWeight(CollocationGraphNode node) {
		double sum = 0;

		for(CollocationGraphEdge edge: getEdges()){
			if (edge.isOutLink(node)) {
				sum += edge.getInNode().getWeight();
			} else if (edge.isInLink(node)) {
				sum += edge.getOutNode().getWeight();
			}

		}
		return sum;
	}
	
	
	/*
	 * 
	 * Operation to delete node in collocation graph
	 * 
	 */
	
	public void deleteNode(CollocationGraphNode node){
		for (CollocationGraphNode n : getNodes()) {
			if (n.equal(node)) {
				getNodes().remove(n);
				return;
			}
		}
	}
	
	public void deleteNodeAndEdges(CollocationGraphNode node){
		
		for (int i=0;i<getEdges().size(); i++){
			CollocationGraphEdge edge = getEdges().get(i);
			if(edge.getInNode().equal(node)){
				
//				System.out.println("Deleting edge: " + edge.getInNode().getNodeId() + ", " + edge.getOutNode().getNodeId());
				getEdges().remove(i);
				i--;
			}
			else if(edge.getOutNode().equal(node)){
//				System.out.println("Deleting edge: " + edge.getInNode().getNodeId() + ", " + edge.getOutNode().getNodeId());
				getEdges().remove(i);
				i--;
			}
		}
//		System.out.println("Deleting node: " + node.getNodeId()+", " + node.getInvolveWuClasses());
		deleteNode(node);
	}
	
	public void deleteAndItsNeighbors(CollocationGraphNode node){
		
		ArrayList<CollocationGraphNode> nodes = new ArrayList<CollocationGraphNode>();
		
		for(CollocationGraphEdge edge: getEdges()){
			if(edge.isOutLink(node)){
//				deleteNodeAndEdges(edge.getOutNode());
				nodes.add(edge.getOutNode());
			}
			else if(edge.isInLink(node)){
//				deleteNodeAndEdges(edge.getInNode());
				nodes.add(edge.getInNode());
			}
		}
		
		for(CollocationGraphNode n :nodes){
			deleteNodeAndEdges(n);
		}
		deleteNodeAndEdges(node);
	}
	
	public void print(){

		System.out.println("Collocation graph information:");
		
		System.out.println("Nodes:" + mNodes.size());
		for (int i = 0; i < mNodes.size(); i++) {
			System.out.println("ID: " + mNodes.get(i).getNodeId() + ", weight: " + mNodes.get(i).getWeight() + ", wuclasses: "+ mNodes.get(i).getInvolveWuClasses());
		}
		
		System.out.println("Links:" + mEdges.size());
		for(int i = 0; i < mEdges.size(); i++) {
			System.out.println("<" + mEdges.get(i).getInNode().getNodeId() + ", " + mEdges.get(i).getOutNode().getNodeId()+">" + mEdges.get(i).getInNode().getInvolveWuClasses() + " v.s " + mEdges.get(i).getOutNode().getInvolveWuClasses());
		}

	}
}
