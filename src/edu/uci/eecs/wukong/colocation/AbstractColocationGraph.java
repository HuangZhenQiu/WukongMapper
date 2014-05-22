package edu.uci.eecs.wukong.colocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;

public abstract class AbstractColocationGraph {
	
	protected FlowGraph graph;
	protected WukongSystem system;
	protected List<ColocationGraphEdge> edges;
	protected List<ColocationGraphNode> nodes;

	
	public AbstractColocationGraph(FlowGraph graph, WukongSystem system) {
		this.edges = new ArrayList<ColocationGraphEdge>();
		this.nodes = new ArrayList<ColocationGraphNode>();

		this.graph = graph;
		this.system = system;
	}
	
	protected abstract void init();
	
	
	/*
	 * 
	 * Set operation
	 * 
	 */
	protected Set<Integer> getIntersection(ColocationGraphNode node1, ColocationGraphNode node2) {
		Set<Integer> intersection = new HashSet<Integer>(node1.getInvolveWuClasses());
		intersection.retainAll(node2.getInvolveWuClasses());
		return intersection;
	}

	protected Set<Integer> getUnion(ColocationGraphNode node1, ColocationGraphNode node2) {
		Set<Integer> union = new HashSet<Integer>(node1.getInvolveWuClasses());
		union.addAll(node2.getInvolveWuClasses());
		return union;
	}
	
	protected Set<Edge> getEdgeUnion(ColocationGraphNode node1, ColocationGraphNode node2) {
		Set<Edge> union = new HashSet<Edge>(node1.getMergingEdges());
		union.addAll(node2.getMergingEdges());
		return union;
	}

	public List<ColocationGraphEdge> getAllEdges() {
		return edges;
	}
	
	public boolean isEdgeExist(ColocationGraphEdge e) {
		for (ColocationGraphEdge edge : getAllEdges()) {
			if (edge.equals(e)) {
				return true;
			}
		}
		return false;
	}
	protected boolean addEdge(ColocationGraphEdge edge) {
		if (!isEdgeExist(edge)) {
			edge.getInNode().addNeighbors(edge.getOutNode());
			edge.getOutNode().addNeighbors(edge.getInNode());
			edges.add(edge);
			return true;
		}
		return false;
	}

	
	public void deleteEdge(ColocationGraphEdge edge) {
		edge.getInNode().getNeighbors().remove(edge.getOutNode());
		edge.getOutNode().getNeighbors().remove(edge.getInNode());
		getAllEdges().remove(edge);
		// System.out.println("Deleting edge: " + edge.getInNode().getNodeId() +
		// ", " + edge.getOutNode().getNodeId());
	}
	
	protected void printEdges() {
		System.out.println("Links:" + edges.size());
		for (int i = 0; i < edges.size(); i++) {
			System.out.println(edges.get(i).toString());
		}
	}
	
	protected void printNodes() {
		System.out.println("Nodes:" + getNodes().size());
		for (int i = 0; i < getNodes().size(); i++) {
			System.out.println(getNodes().get(i).toString());
		}
	}
	

	public List<ColocationGraphNode> getNeighbors(ColocationGraphNode node) {
		List<ColocationGraphNode> neighbors = new ArrayList<ColocationGraphNode>();

		for (ColocationGraphEdge edge : getAllEdges()) {
			if (edge.isOutLink(node)) {
				neighbors.add(edge.getOutNode());
			} else if (edge.isInLink(node)) {
				neighbors.add(edge.getInNode());
			}
		}
		return neighbors;
	}

	public double getNeighborWeight(ColocationGraphNode node) {
		double sum = 0;
		List<ColocationGraphNode> neighbors = getNeighbors(node);
		for (ColocationGraphNode neighbor : neighbors) {
			sum += neighbor.getWeight();
		}
		return sum;
	}

	
	private boolean isNodeExist(ColocationGraphNode node) {
		for (int i = 0; i < getNodes().size(); i++) {
			ColocationGraphNode n = getNodes().get(i);
			if (n.equal(node)) {
				return true;
			}

		}
		return false;
	}

	protected boolean addNode(ColocationGraphNode node) {
		if (!isNodeExist(node)) {
			getNodes().add(node);
			node.setNodeId(getNodes().indexOf(node));
			return true;
		}
		return false;
	}
	
	public List<ColocationGraphNode> getNodes() {
		return nodes;
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

		for (int i = 0; i < getAllEdges().size(); i++) {
			ColocationGraphEdge edge = getAllEdges().get(i);
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

	
}
