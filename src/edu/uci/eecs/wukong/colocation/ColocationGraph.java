package edu.uci.eecs.wukong.colocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.util.Pair;

public class ColocationGraph extends AbstractColocationGraph{
	private List<ColocationGraphNode> mNodes;

	public ColocationGraph(FlowGraph graph, WukongSystem system) {
		super(graph, system);
		this.mNodes = new ArrayList<ColocationGraphNode>();
		this.init();
	}
	
	public ColocationGraph(FlowGraph graph, WukongSystem system, int flag) {
		super(graph, system);
		this.mNodes = new ArrayList<ColocationGraphNode>();
		this.initCollocation2(graph, system);
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

	
	public void initCollocation2(FlowGraph graph, WukongSystem system) {
		rawInitCollocationGraph(graph);
		
		ArrayList<Pair<ColocationGraphNode, ColocationGraphNode>> pair_list = new ArrayList<Pair<ColocationGraphNode, ColocationGraphNode>>();
		for (int i = 0; i < mNodes.size() - 1; i++) {
			for (int j = i + 1; j < mNodes.size(); j++) {
				Pair<ColocationGraphNode, ColocationGraphNode> pair = new Pair<ColocationGraphNode, ColocationGraphNode>(mNodes.get(i), mNodes.get(j));
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
					Set<Edge> edges = new HashSet<FlowBasedProcess.Edge>(node1.getMergingEdges());
					edges.addAll(node2.getMergingEdges());
					ColocationGraphNode node = new ColocationGraphNode(union, node1.getWeight() + node2.getWeight(), edges);
					
					if (!addNode(node)) { 
						/* Node exists */
						node = getNode(node);
					}
					else{ 
						/* Node does not exist */
						for(ColocationGraphNode remain_node: mNodes){
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

					ColocationGraphEdge edge2 = new ColocationGraphEdge(
							node2, node);
					if (!node2.equal(node)) {
						addEdge(edge2);
					}

					ColocationGraphEdge edge3 = new ColocationGraphEdge(
							node2, node1);
					if (!node2.equal(node1)) {
						addEdge(edge3);
					}

				}
			}
		}
	}
	public void init() {
		rawInitCollocationGraph(graph);
		for (int i = 0; i < mNodes.size(); i++) {
			ColocationGraphNode node1 = mNodes.get(i);
			for (int j = 0; j < mNodes.size(); j++) {
				if (i == j) {
					continue;
				}
				ColocationGraphNode node2 = mNodes.get(j);

				if (getIntersection(node1, node2).size() != 0) {
					Set<Integer> union = getUnion(node1, node2);
					if (!system.isHostable(union)) {
						addEdge(new ColocationGraphEdge(node1, node2));

					} else {
						// hostable @@
						if(node1.getInvolveWuClasses().containsAll(node2.getInvolveWuClasses()) ||
								node2.getInvolveWuClasses().containsAll(node1.getInvolveWuClasses())) {
							//won't create new node, but create an edge for node1 != node2 anyway
							addEdge(new ColocationGraphEdge(node1, node2)); 
						} else {
							
							//in case node != node1 != node2, so we make three edges
							ColocationGraphNode node = new ColocationGraphNode(union,
									node1.getWeight() + node2.getWeight(), getEdgeUnion(node1, node2));
							
							if (!addNode(node)) {
								node = getNode(node);
							}

							addEdge(new ColocationGraphEdge(node1, node));
							addEdge(new ColocationGraphEdge(node2, node));
							//i =!j ensures node1 != node2
							addEdge(new ColocationGraphEdge(node1, node2)); 

						}
						
					}
				}

			}
		}
	}
	
	private boolean isNodeExist(ColocationGraphNode node) {
		for (int i = 0; i < mNodes.size(); i++) {
			ColocationGraphNode n = mNodes.get(i);
			if (n.equal(node)) {
				return true;
			}

		}
		return false;
	}

	private boolean addNode(ColocationGraphNode node) {
		if (!isNodeExist(node)) {
			mNodes.add(node);
			node.setNodeId(mNodes.indexOf(node));
			return true;
		}
		return false;
	}
	
	public List<ColocationGraphNode> getNodes() {
		return mNodes;
	}

	private boolean addEdge(ColocationGraphEdge edge) {
		if (!isEdgeExist(edge)) {
			edge.getInNode().increaseDegree();
			edge.getOutNode().increaseDegree();
			edge.getInNode().addNeighbors(edge.getOutNode());
			edge.getOutNode().addNeighbors(edge.getInNode());
			edges.add(edge);
			return true;
		}
		return false;
	}
	
	public ColocationGraphNode getNode(int nodeId) {
		for (int i = 0; i < getNodes().size(); i++) {
			if (getNodes().get(i).getNodeId() == nodeId) {
				return getNodes().get(i);
			}
		}
		return null;
	}
	
	public ColocationGraphNode getNode(ColocationGraphNode node) {
		for (int i = 0; i < getNodes().size(); i++) {
			if (getNodes().get(i).equal(node)) {
				return getNodes().get(i);
			}
		}
		return null;
	}

	public void deleteNodeOnly(ColocationGraphNode node) {
		getNodes().remove(node);
	}
	
	/*
	 * 
	 * Operation to delete node in collocation graph
	 */

	public void deleteNode(ColocationGraphNode node) {
		deleteNodeAndEdges(node);
	}

	public void deleteNodeAndEdges(ColocationGraphNode node) {

		for (int i = 0; i < getEdges().size(); i++) {
			ColocationGraphEdge edge = getEdges().get(i);
			if (edge.getInNode().equal(node)) {
				deleteEdge(edge);
				i--;
			} else if (edge.getOutNode().equal(node)) {
				deleteEdge(edge);
				i--;
			}
		}
		deleteNodeOnly(node);
	}
	
	public void deleteAndItsNeighbors(ColocationGraphNode node) {

		List<ColocationGraphNode> nodes_to_be_deleted = getNeighbors(node);
		nodes_to_be_deleted.add(node);

		for (ColocationGraphNode n : nodes_to_be_deleted) {
			deleteNode(n);
		}
	}
	
	public void print() {

		System.out.println("Collocation graph information:");

		System.out.println("Nodes:" + mNodes.size());
		for (int i = 0; i < mNodes.size(); i++) {
			System.out.println(mNodes.get(i).toString());
		}

		System.out.println("Links:" + edges.size());
		for (int i = 0; i < edges.size(); i++) {
			System.out.println(edges.get(i).toString());
		}

	}
}
