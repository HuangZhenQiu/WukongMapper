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
	
	/*
	 * Convert original FBP to Collocation graph with nodes.
	 */
	public void convertFBPEdges(FlowGraph graph){
		for(Edge fbp_edge: graph.getEdges()){
			HashSet<Integer> sets = new HashSet<Integer>();
			sets.add(fbp_edge.getInWuClass().getWuClassId());
			sets.add(fbp_edge.getOutWuClass().getWuClassId());
			
			CollocationGraphNode node = new CollocationGraphNode(sets, fbp_edge.getDataVolumn());
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
	    				CollocationGraphEdge edge = new CollocationGraphEdge(node1.getNodeId(), node2.getNodeId());
	    				if(!isEdgeExist(edge)){
	    					mEdges.add(edge);
	    				}
	    			}
	    			else{
	    				// hostable @@
	    				
	    				CollocationGraphNode node = new CollocationGraphNode(union, node1.getWeight() + node2.getWeight());
	    				if(addNode(node)){
	    					CollocationGraphEdge edge1 = new CollocationGraphEdge(node1.getNodeId(), node.getNodeId());
		    				addEdge(edge1);
		    				CollocationGraphEdge edge2 = new CollocationGraphEdge(node2.getNodeId(), node.getNodeId());
		    				addEdge(edge2);
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
	private void addEdge(CollocationGraphEdge edge){
		if(!isEdgeExist(edge)){
			mEdges.add(edge);
		}
	}

	private boolean isEdgeExist(CollocationGraphEdge edge) {
		for (int i = 0; i < mEdges.size(); i++) {
			if (mEdges.get(i).equals(edge)) {
				return true;
			}
		}
		return false;
	}
	
	
	public void print(){

		System.out.println("Collocation graph information:");
		System.out.println("Nodes:");
		for (int i = 0; i < mNodes.size(); i++) {
			System.out.println("ID: " + mNodes.get(i).getNodeId() + ", weight: " + mNodes.get(i).getWeight() + ", wuclasses: "+ mNodes.get(i).getInvolveWuClasses());
		}
		
		System.out.println("Links");
		for(int i = 0; i < mEdges.size(); i++) {
			System.out.println("From ID: " + mEdges.get(i).inIndex + " To ID:" + mEdges.get(i).outIndex);
		}
	}
}
