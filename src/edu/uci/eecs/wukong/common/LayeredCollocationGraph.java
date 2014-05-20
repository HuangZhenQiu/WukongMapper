package edu.uci.eecs.wukong.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.util.Pair;

public class LayeredCollocationGraph {

	ArrayList<ArrayList<CollocationGraphNode>> layers = new ArrayList<ArrayList<CollocationGraphNode>>();
	ArrayList<CollocationGraphEdge> edges = new ArrayList<CollocationGraphEdge>();
	
	public LayeredCollocationGraph(FlowGraph graph, WukongSystem system) {
		this.initCollocationLayerByLayer(graph, system);
	}
	
	
	public void initCollocationLayerByLayer(FlowGraph graph, WukongSystem system) {
		
		layers = new ArrayList<ArrayList<CollocationGraphNode>>();
		ArrayList<CollocationGraphNode> first = new ArrayList<CollocationGraphNode>();
		for (Edge fbpEdge : graph.getEdges()) {
			Set<Integer> sets = new HashSet<Integer>();
			sets.add(fbpEdge.getInWuClass().getWuClassId());
			sets.add(fbpEdge.getOutWuClass().getWuClassId());
			Set<Edge> edgeSet = new HashSet<Edge>();
			edgeSet.add(fbpEdge);
			CollocationGraphNode node = new CollocationGraphNode(sets, fbpEdge.getWeight(), edgeSet);
			first.add(node);
		}
		layers.add(first);
		
		for (int i = 0; i < layers.size(); i++) {
			
			ArrayList<Pair<CollocationGraphNode, CollocationGraphNode>> pair_list = new ArrayList<Pair<CollocationGraphNode, CollocationGraphNode>>();
			for (int k = 0; k < layers.get(i).size() - 1; k++) {
				for (int j = k + 1; j < layers.get(i).size(); j++) {
					Pair<CollocationGraphNode, CollocationGraphNode> pair = new Pair<CollocationGraphNode, CollocationGraphNode>(layers.get(i).get(k), layers.get(i).get(j));
					pair_list.add(pair);
				}
			}
			
			while(pair_list.size() > 0){
				System.out.println("Pair list size:" + pair_list.size());
				Pair<CollocationGraphNode, CollocationGraphNode> pair = pair_list.remove(0);
				CollocationGraphNode node1 = pair.getFirst();
				CollocationGraphNode node2 = pair.getSecond();
				System.out.println("Check pair" + node1.getInvolveWuClasses() + ", " + node2.getInvolveWuClasses());
				if (getIntersection(node1, node2).size() != 0) {
					Set<Integer> union = getUnion(node1, node2);
					if (!system.isHostable(union)) {
//						CollocationGraphEdge edge = new CollocationGraphEdge(node1, node2);
//						addEdge(edge);
					} else {
						// hostable @@
						
						Set<Edge> edges = new HashSet<FlowBasedProcess.Edge>(node1.getMergingEdges());
						edges.addAll(node2.getMergingEdges());
						CollocationGraphNode node = new CollocationGraphNode(union, node1.getWeight() + node2.getWeight(), edges);
						
						System.out.println("Creating new node:" +node.getInvolveWuClasses());
						int size = node.getInvolveWuClasses().size();
						
						
						System.out.println("layers size: " + layers.size());
						if(layers.size()-1 < size-2) {
							ArrayList<CollocationGraphNode> layer = new ArrayList<CollocationGraphNode>();
							layers.add(layer);
						}
						
						ArrayList<CollocationGraphNode> layer = layers.get(size-2);
						
						boolean found = false;
						for (CollocationGraphNode check : layer) {
							if(check.equal(node)){
								found = true;
							}
						}
						if(!found){
							layer.add(node);
						}
					}
				}
			}
			
		}
		
//		System.out.println(pair_list.size());
		for(ArrayList<CollocationGraphNode> layer:layers){
			
			System.out.println("Size: " + layers.indexOf(layer) + ", sized " + layer.size());
			for(CollocationGraphNode node:layer) {
				System.out.println("node: " + node.getInvolveWuClasses());
			}
		}
	}
	
	
	private Set<Integer> getIntersection(CollocationGraphNode node1,
			CollocationGraphNode node2) {
		Set<Integer> intersection = new HashSet<Integer>(
				node1.getInvolveWuClasses());
		intersection.retainAll(node2.getInvolveWuClasses());
		return intersection;
	}

	private Set<Integer> getUnion(CollocationGraphNode node1,
			CollocationGraphNode node2) {
		Set<Integer> union = new HashSet<Integer>(
				node1.getInvolveWuClasses());
		union.addAll(node2.getInvolveWuClasses());
		return union;

	}
	
	private Set<Edge> getEdgeUnion(CollocationGraphNode node1, CollocationGraphNode node2) {
		Set<Edge> union = new HashSet<Edge>(node1.getMergingEdges());
		union.addAll(node2.getMergingEdges());
		return union;
	}
	
}
