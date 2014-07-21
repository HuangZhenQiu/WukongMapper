package edu.uci.eecs.wukong.colocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcessEdge;
import edu.uci.eecs.wukong.util.Pair;

public class ColocationGraph extends AbstractColocationGraph{

	public ColocationGraph(FlowGraph graph, WukongSystem system) {
		super(graph, system);
		this.init();
	}
	
	public void init() {
		rawInitCollocationGraph(graph);
		
		ArrayList<Pair<ColocationGraphNode, ColocationGraphNode>> pair_list = new ArrayList<Pair<ColocationGraphNode, ColocationGraphNode>>();
		for (int i = 0; i < getNodes().size() - 1; i++) {
			for (int j = i + 1; j < getNodes().size(); j++) {
				Pair<ColocationGraphNode, ColocationGraphNode> pair = new Pair<ColocationGraphNode, ColocationGraphNode>(getNodes().get(i), getNodes().get(j));
				pair_list.add(pair);
			}
		}
		
		while(pair_list.size() != 0){
			Pair<ColocationGraphNode, ColocationGraphNode> pair = pair_list.remove(0);
			ColocationGraphNode node1 = pair.getFirst();
			ColocationGraphNode node2 = pair.getSecond();

			if (getIntersection(node1, node2).size() != 0) {
				Set<Integer> union = getUnion(node1, node2);
				if (!system.isHostable(union)) {
					ColocationGraphEdge edge = new ColocationGraphEdge(node1, node2);
					addEdge(edge);
				} else {
					// hostable @@
					Set<FlowBasedProcessEdge> edges = new HashSet<FlowBasedProcessEdge>(node1.getMergingEdges());
					edges.addAll(node2.getMergingEdges());
					ColocationGraphNode node = new ColocationGraphNode(union, node1.getWeight() + node2.getWeight(), edges);
					
					if (!addNode(node)) { 
						/* Node exists */
						node = getNode(node);
					}
					else{ 
						/* Node does not exist */
						for(ColocationGraphNode remain_node: getNodes()){
							if (!remain_node.equal(node1) && !remain_node.equal(node2)) {
								Pair<ColocationGraphNode, ColocationGraphNode> new_pair = new Pair<ColocationGraphNode, ColocationGraphNode>(node, remain_node);
								pair_list.add(new_pair);
							}
						}
					}

					ColocationGraphEdge edge1 = new ColocationGraphEdge(node1, node);
					if (!node1.equal(node)) {
						addEdge(edge1);
					}

					ColocationGraphEdge edge2 = new ColocationGraphEdge(node2, node);
					if (!node2.equal(node)) {
						addEdge(edge2);
					}

					ColocationGraphEdge edge3 = new ColocationGraphEdge(node2, node1);
					if (!node2.equal(node1)) {
						addEdge(edge3);
					}

				}
			}
		}
	}
}
