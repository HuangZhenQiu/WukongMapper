package edu.uci.eecs.wukong.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.util.ObjectCloner;
import edu.uci.eecs.wukong.util.Pair;

public class CollocationGraph {
	private ArrayList<CollocationGraphNode> mNodes;
	private List<CollocationGraphEdge> mEdges;

	public CollocationGraph(FlowGraph graph, WukongSystem system) {
		mNodes = new ArrayList<CollocationGraphNode>();
		mEdges = new ArrayList<CollocationGraphEdge>();
		this.initCollocation(graph, system);
	}
	
	public CollocationGraph(FlowGraph graph, WukongSystem system, int flag) {
		mNodes = new ArrayList<CollocationGraphNode>();
		mEdges = new ArrayList<CollocationGraphEdge>();
		this.initCollocation2(graph, system);
	}

	public void rawInitCollocationGraph(FlowGraph graph) {
		for (Edge fbp_edge : graph.getEdges()) {
			HashSet<Integer> sets = new HashSet<Integer>();
			sets.add(fbp_edge.getInWuClass().getWuClassId());
			sets.add(fbp_edge.getOutWuClass().getWuClassId());
			ArrayList<Edge> edge_list = new ArrayList<FlowBasedProcess.Edge>();
			edge_list.add(fbp_edge);
			CollocationGraphNode node = new CollocationGraphNode(sets, fbp_edge.getDataVolumn(), edge_list);
			addNode(node);
		}
	}

	
	public void initCollocation2(FlowGraph graph, WukongSystem system) {
		rawInitCollocationGraph(graph);
		
		ArrayList<Pair<CollocationGraphNode, CollocationGraphNode>> pair_list = new ArrayList<Pair<CollocationGraphNode, CollocationGraphNode>>();
		for (int i = 0; i < mNodes.size() - 1; i++) {
			for (int j = i + 1; j < mNodes.size(); j++) {
				Pair<CollocationGraphNode, CollocationGraphNode> pair = new Pair<CollocationGraphNode, CollocationGraphNode>(mNodes.get(i), mNodes.get(j));
				pair_list.add(pair);
			}
		}
		
		while(pair_list.size() != 0){
			Pair<CollocationGraphNode, CollocationGraphNode> pair = pair_list.remove(0);
			CollocationGraphNode node1 = pair.getFirst();
			CollocationGraphNode node2 = pair.getSecond();

			if (getIntersection(node1, node2).size() != 0) {
				HashSet<Integer> union = getUnion(node1, node2);
				if (!system.isHostable(union)) {
					CollocationGraphEdge edge = new CollocationGraphEdge(node1, node2);
					addEdge(edge);
				} else {
					// hostable @@
					ArrayList<Edge> edges = new ArrayList<FlowBasedProcess.Edge>(node1.getMergingEdges());
					edges.addAll(node2.getMergingEdges());
					CollocationGraphNode node = new CollocationGraphNode(union, node1.getWeight() + node2.getWeight(), edges);
					
					if (!addNode(node)) { 
						/* Node exists */
						node = getNode(node);
					}
					else{ 
						/* Node does not exist */
						for(CollocationGraphNode remain_node: mNodes){
							if (!remain_node.equal(node1) && !remain_node.equal(node2)) {
								Pair<CollocationGraphNode, CollocationGraphNode> new_pair = new Pair<CollocationGraphNode, CollocationGraphNode>(node, remain_node);
								pair_list.add(new_pair);
							}
						}
					}

					CollocationGraphEdge edge1 = new CollocationGraphEdge(node1, node);
					
					if (!node1.equal(node)) {
						addEdge(edge1);
					}

					CollocationGraphEdge edge2 = new CollocationGraphEdge(
							node2, node);
					if (!node2.equal(node)) {
						addEdge(edge2);
					}

					CollocationGraphEdge edge3 = new CollocationGraphEdge(
							node2, node1);
					if (!node2.equal(node1)) {
						addEdge(edge3);
					}

				}
			}
		}
	}
	public void initCollocation(FlowGraph graph, WukongSystem system) {
		rawInitCollocationGraph(graph);
		for (int i = 0; i < mNodes.size(); i++) {
			for (int j = 0; j < mNodes.size(); j++) {
				if (i == j) {
					continue;
				}
				CollocationGraphNode node1 = mNodes.get(i);
				CollocationGraphNode node2 = mNodes.get(j);

				if (getIntersection(node1, node2).size() != 0) {
					HashSet<Integer> union = getUnion(node1, node2);
					if (!system.isHostable(union)) {
						CollocationGraphEdge edge = new CollocationGraphEdge(
								node1, node2);
						addEdge(edge);

					} else {
						// hostable @@
						ArrayList<Edge> edges = new ArrayList<FlowBasedProcess.Edge>(node1.getMergingEdges());
						edges.addAll(node2.getMergingEdges());
						CollocationGraphNode node = new CollocationGraphNode(union, node1.getWeight() + node2.getWeight(), edges);
						
						if (!addNode(node)) {
							// System.out.println("node1:" +
							// node.getInvolveWuClasses() + " node2: " +
							// node1.getInvolveWuClasses() + " New node " +
							// node.getInvolveWuClasses());
							node = getNode(node);
						}

						CollocationGraphEdge edge1 = new CollocationGraphEdge(node1, node);
						
						if (!node1.equal(node)) {
							addEdge(edge1);
						}

						CollocationGraphEdge edge2 = new CollocationGraphEdge(
								node2, node);
						if (!node2.equal(node)) {
							addEdge(edge2);
						}

						CollocationGraphEdge edge3 = new CollocationGraphEdge(
								node2, node1);
						if (!node2.equal(node1)) {
							addEdge(edge3);
						}

					}
				}

			}
		}
	}

	private HashSet<Integer> getIntersection(CollocationGraphNode node1,
			CollocationGraphNode node2) {
		HashSet<Integer> intersection = new HashSet<Integer>(
				node1.getInvolveWuClasses());
		intersection.retainAll(node2.getInvolveWuClasses());
		return intersection;
	}

	private HashSet<Integer> getUnion(CollocationGraphNode node1,
			CollocationGraphNode node2) {
		HashSet<Integer> union = new HashSet<Integer>(
				node1.getInvolveWuClasses());
		union.addAll(node2.getInvolveWuClasses());
		return union;

	}

	private boolean isNodeExist(CollocationGraphNode node) {
		for (int i = 0; i < mNodes.size(); i++) {
			CollocationGraphNode n = mNodes.get(i);
			if (n.equal(node)) {
				return true;
			}

		}
		return false;
	}

	private boolean addNode(CollocationGraphNode node) {
		if (!isNodeExist(node)) {
			mNodes.add(node);
			node.setNodeId(mNodes.indexOf(node));
//			 System.out.println("New node:" + node.getNodeId() +
//						 " degree: " + node.getDegree() + "involved wuclasses " + node.getInvolveWuClasses());
			return true;
		}
		return false;
	}

	private boolean addEdge(CollocationGraphEdge edge) {
		if (!isEdgeExist(edge)) {
			edge.getInNode().increaseDegree();
			edge.getOutNode().increaseDegree();
			
			edge.getInNode().addNeighbors(edge.getOutNode());
			edge.getOutNode().addNeighbors(edge.getInNode());
			// System.out.println("node:" + edge.getInNode().getNodeId() +
			// " degree: " + edge.getInNode().getDegree());
			// System.out.println("node:" + edge.getOutNode().getNodeId() +
			// " degree: " + edge.getOutNode().getDegree());
//			 System.out.println("New edge from" +
//			 edge.getInNode().getInvolveWuClasses() + " to " +
//			 edge.getOutNode().getInvolveWuClasses() + " <" +
//			 edge.getInNode().getNodeId() + ", " +
//			 edge.getOutNode().getNodeId() +" >");
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

	public List<CollocationGraphNode> getNodes() {
		return mNodes;
	}

	public List<CollocationGraphEdge> getEdges() {
		return mEdges;
	}

	public CollocationGraphNode getNode(int nodeId) {
		for (int i = 0; i < getNodes().size(); i++) {
			if (getNodes().get(i).getNodeId() == nodeId) {
				return getNodes().get(i);
			}
		}
		return null;
	}
	
	public CollocationGraphNode getNode(CollocationGraphNode node) {
		for (int i = 0; i < getNodes().size(); i++) {
			if (getNodes().get(i).equal(node)) {
				return getNodes().get(i);
			}
		}
		return null;
	}

	public List<CollocationGraphNode> getNeighbors(CollocationGraphNode node) {
		List<CollocationGraphNode> neighbors = new ArrayList<CollocationGraphNode>();

		for (CollocationGraphEdge edge : getEdges()) {
			if (edge.isOutLink(node)) {
				neighbors.add(edge.getOutNode());
			} else if (edge.isInLink(node)) {
				neighbors.add(edge.getInNode());
			}
		}
		return neighbors;
	}

	public double getNeighborWeight(CollocationGraphNode node) {
		double sum = 0;
		List<CollocationGraphNode> neighbors = getNeighbors(node);
		for (CollocationGraphNode neighbor : neighbors) {
			sum += neighbor.getWeight();
		}
		return sum;
	}

	/*
	 * 
	 * Operation to delete node in collocation graph
	 */

	public void deleteNode(CollocationGraphNode node) {
		deleteNodeAndEdges(node);
	}

	public void deleteNodeAndEdges(CollocationGraphNode node) {

		for (int i = 0; i < getEdges().size(); i++) {
			CollocationGraphEdge edge = getEdges().get(i);
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

	public void deleteNodeOnly(CollocationGraphNode node) {
		getNodes().remove(node);
	}

	public void deleteEdge(CollocationGraphEdge edge) {
		edge.getInNode().decreaseDegree();
		edge.getOutNode().decreaseDegree();
		getEdges().remove(edge);
		// System.out.println("Deleting edge: " + edge.getInNode().getNodeId() +
		// ", " + edge.getOutNode().getNodeId());
	}

	public void deleteAndItsNeighbors(CollocationGraphNode node) {

		List<CollocationGraphNode> nodes_to_be_deleted = getNeighbors(node);
		nodes_to_be_deleted.add(node);

		for (CollocationGraphNode n : nodes_to_be_deleted) {
			deleteNode(n);
		}
	}

	public void print() {

		System.out.println("Collocation graph information:");

		System.out.println("Nodes:" + mNodes.size());
		for (int i = 0; i < mNodes.size(); i++) {
			System.out.println(mNodes.get(i).toString());
		}

		System.out.println("Links:" + mEdges.size());
		for (int i = 0; i < mEdges.size(); i++) {
			System.out.println(mEdges.get(i).toString());
		}

	}
}
