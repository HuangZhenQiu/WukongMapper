package edu.uci.eecs.wukong.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;

public class CollocationGraph {
	private List<CollocationGraphNode> mNodes;
	private List<CollocationGraphEdge> mEdges;

	public CollocationGraph(FlowGraph graph, WukongSystem system) {
		this.mNodes = new ArrayList<CollocationGraphNode>();
		this.mEdges = new ArrayList<CollocationGraphEdge>();
		this.initCollocation(graph, system);
	}

	public void convertFBPEdges(FlowGraph graph) {
		for (Edge fbpEdge : graph.getEdges()) {
			Set<Integer> sets = new HashSet<Integer>();
			sets.add(fbpEdge.getInWuClass().getWuClassId());
			sets.add(fbpEdge.getOutWuClass().getWuClassId());
			Set<Edge> edgeSet = new HashSet<Edge>();
			edgeSet.add(fbpEdge);
			CollocationGraphNode node = new CollocationGraphNode(sets, fbpEdge.getWeight(), edgeSet);
			addNode(node);
		}
	}

	public void initCollocation(FlowGraph graph, WukongSystem system) {

		this.convertFBPEdges(graph);
		for (int i = 0; i < mNodes.size(); i++) {
			for (int j = 0; j < mNodes.size(); j++) {
				if (i == j) {
					continue;
				}
				CollocationGraphNode node1 = mNodes.get(i);
				CollocationGraphNode node2 = mNodes.get(j);

				if (getIntersection(node1, node2).size() != 0) {
					Set<Integer> union = getUnion(node1, node2);
					if (!system.isHostable(union)) {
						addEdge(new CollocationGraphEdge(node1, node2));

					} else {
						// hostable @@
						if(node1.getInvolveWuClasses().containsAll(node2.getInvolveWuClasses()) ||
								node2.getInvolveWuClasses().containsAll(node1.getInvolveWuClasses())) {
							//won't create new node, but create an edge for node1 != node2 anyway
							addEdge(new CollocationGraphEdge(node1, node2)); 
						} else {
							
							//in case node != node1 != node2, so we make three edges
							CollocationGraphNode node = new CollocationGraphNode(union,
									node1.getWeight() + node2.getWeight(), getEdgeUnion(node1, node2));
							
							if (!addNode(node)) {
								node = getNode(node);
							}

							addEdge(new CollocationGraphEdge(node1, node));
							addEdge(new CollocationGraphEdge(node2, node));
							//i =!j ensures node1 != node2
							addEdge(new CollocationGraphEdge(node1, node2)); 

						}
						
					}
				}

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
			return true;
		}
		return false;
	}

	private boolean addEdge(CollocationGraphEdge edge) {
		if (!isEdgeExist(edge)) {
			edge.getInNode().increaseDegree();
			edge.getOutNode().increaseDegree();
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
