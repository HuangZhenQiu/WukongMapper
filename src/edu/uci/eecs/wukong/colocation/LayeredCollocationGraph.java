package edu.uci.eecs.wukong.colocation;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.util.Pair;

public class LayeredCollocationGraph extends AbstractColocationGraph{

	List<ArrayList<ColocationGraphNode>> layers;
	
	public LayeredCollocationGraph(FlowGraph graph, WukongSystem system) {
		super(graph, system);
		this.layers = new ArrayList<ArrayList<ColocationGraphNode>>();
		this.init();
	}
	
	
	public void init() {
		
		ArrayList<ColocationGraphNode> first = new ArrayList<ColocationGraphNode>();
		for (Edge fbpEdge : graph.getEdges()) {
			Set<Integer> sets = new HashSet<Integer>();
			sets.add(fbpEdge.getInWuClass().getWuClassId());
			sets.add(fbpEdge.getOutWuClass().getWuClassId());
			Set<Edge> edgeSet = new HashSet<Edge>();
			edgeSet.add(fbpEdge);
			ColocationGraphNode node = new ColocationGraphNode(sets, fbpEdge.getWeight(), edgeSet);
			first.add(node);
		}
		layers.add(first);
		
		for (int i = 0; i < layers.size(); i++) {
			
			ArrayList<Pair<ColocationGraphNode, ColocationGraphNode>> pair_list = new ArrayList<Pair<ColocationGraphNode, ColocationGraphNode>>();
			for (int k = 0; k < layers.get(i).size() - 1; k++) {
				for (int j = k + 1; j < layers.get(i).size(); j++) {
					Pair<ColocationGraphNode, ColocationGraphNode> pair = new Pair<ColocationGraphNode, ColocationGraphNode>(layers.get(i).get(k), layers.get(i).get(j));
					pair_list.add(pair);
				}
			}
			
			while(pair_list.size() > 0){
				System.out.println("Pair list size:" + pair_list.size());
				Pair<ColocationGraphNode, ColocationGraphNode> pair = pair_list.remove(0);
				ColocationGraphNode node1 = pair.getFirst();
				ColocationGraphNode node2 = pair.getSecond();
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
						ColocationGraphNode node = new ColocationGraphNode(union, node1.getWeight() + node2.getWeight(), edges);
						
						System.out.println("Creating new node:" +node.getInvolveWuClasses());
						int size = node.getInvolveWuClasses().size();
						
						
						System.out.println("layers size: " + layers.size());
						if(layers.size()-1 < size-2) {
							ArrayList<ColocationGraphNode> layer = new ArrayList<ColocationGraphNode>();
							layers.add(layer);
						}
						
						ArrayList<ColocationGraphNode> layer = layers.get(size-2);
						
						boolean found = false;
						for (ColocationGraphNode check : layer) {
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
		for(ArrayList<ColocationGraphNode> layer:layers){
			
			System.out.println("Size: " + layers.indexOf(layer) + ", sized " + layer.size());
			for(ColocationGraphNode node:layer) {
				System.out.println("node: " + node.getInvolveWuClasses());
			}
		}
	}
	
}
