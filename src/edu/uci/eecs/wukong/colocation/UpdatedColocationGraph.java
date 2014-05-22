package edu.uci.eecs.wukong.colocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.util.Pair;

public class UpdatedColocationGraph extends AbstractColocationGraph{
	
	
	public UpdatedColocationGraph(FlowGraph graph, WukongSystem system) {
		super(graph, system);
		this.init();
	}

	public void rawInitCollocationGraph(FlowGraph graph) {
		for (Edge fbpEdge : graph.getEdges()) {
			Set<Integer> sets = new HashSet<Integer>();
			sets.add(fbpEdge.getInWuClass().getWuClassId());
			sets.add(fbpEdge.getOutWuClass().getWuClassId());
			Set<Edge> edgeSet = new HashSet<Edge>();
			edgeSet.add(fbpEdge);
			ColocationGraphNode node = new ColocationGraphNode(sets, fbpEdge.getWeight(), edgeSet);
			addNode(node);
		}
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
		
		while (pair_list.size() != 0) {
			Pair<ColocationGraphNode, ColocationGraphNode> pair = pair_list.remove(0);
			ColocationGraphNode node1 = pair.getFirst();
			ColocationGraphNode node2 = pair.getSecond();

			if (getIntersection(node1, node2).size() != 0 && !( node1.getInvolveWuClasses().containsAll(node2.getInvolveWuClasses()) && node2.getInvolveWuClasses().containsAll(node1.getInvolveWuClasses())) ) {
				Set<Integer> union = getUnion(node1, node2);
				if (!system.isHostable(union)) {
					ColocationGraphEdge edge = new ColocationGraphEdge(node1, node2);
					if(!isEdgeExist(edge)){
						addEdge(edge);
					}
				}
				else {
					// hostable @@
					Set<Edge> edges = new HashSet<FlowBasedProcess.Edge>(node1.getMergingEdges());
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
					
					ColocationGraphEdge edge = new ColocationGraphEdge(node1, node2);
					if(!isEdgeExist(edge)){
						addEdge(edge);
					}
					
					ArrayList<ColocationGraphEdge> edgesToBeAdd = new ArrayList<ColocationGraphEdge>();
					
					for (ColocationGraphNode check: node1.getNeighbors()){
						if(!node.equal(check)){
							ColocationGraphEdge edge2 = new ColocationGraphEdge(node, check);
							edgesToBeAdd.add(edge2);
						}
					}
					for (ColocationGraphNode check: node2.getNeighbors()){
						if(!node.equal(check)){
							ColocationGraphEdge edge2 = new ColocationGraphEdge(node, check);
							edgesToBeAdd.add(edge2);
						}
					}
					for (ColocationGraphEdge edge2: edgesToBeAdd){
						if(!isEdgeExist(edge2)){
							addEdge(edge2);
						}
					}
				}
			}
		}
	}
	
	public void print() {
		System.out.println("Collocation graph information:");
		printNodes();
		printEdges();
	}
}
