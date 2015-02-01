package edu.uci.eecs.wukong.colocation;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcessEdge;
import edu.uci.eecs.wukong.util.Pair;



/*
 * 
 * This Colocation Graph generator will initialize a graph with only one layer. The concept is supposed to be similar with naive greedy merging algorithm.   
 * 
 */
public class OneLayerColocationGraph extends AbstractColocationGraph{

	List<ArrayList<ColocationGraphNode>> layers;
	public OneLayerColocationGraph(FlowGraph graph, WukongSystem system) {
		super(graph, system);
		this.layers = new ArrayList<ArrayList<ColocationGraphNode>>();
		this.init();
	}
	
	public void init() {
		
		rawInitCollocationGraph(graph);

		ArrayList<ColocationGraphNode> first = new ArrayList<ColocationGraphNode>();
		for(ColocationGraphNode node: getNodes()){
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
				Pair<ColocationGraphNode, ColocationGraphNode> pair = pair_list.remove(0);
				ColocationGraphNode node1 = pair.getFirst();
				ColocationGraphNode node2 = pair.getSecond();
				
			
				if (getIntersection(node1, node2).size() != 0) {
					Set<Integer> union = getUnion(node1, node2);
					
					if (!system.isHostable(union)){
						ColocationGraphEdge edge = new ColocationGraphEdge(node1, node2);
						addEdge(edge);
					}
				}
			}
			
		}
	}
}
