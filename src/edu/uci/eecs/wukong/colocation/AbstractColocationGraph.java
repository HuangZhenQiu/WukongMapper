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

	public AbstractColocationGraph(FlowGraph graph, WukongSystem system) {
		this.edges = new ArrayList<ColocationGraphEdge>();
		this.graph = graph;
		this.system = system;
	}
	
	protected abstract void init();
	
	protected Set<Integer> getIntersection(ColocationGraphNode node1,
			ColocationGraphNode node2) {
		Set<Integer> intersection = new HashSet<Integer>(
				node1.getInvolveWuClasses());
		intersection.retainAll(node2.getInvolveWuClasses());
		return intersection;
	}

	protected Set<Integer> getUnion(ColocationGraphNode node1,
			ColocationGraphNode node2) {
		Set<Integer> union = new HashSet<Integer>(
				node1.getInvolveWuClasses());
		union.addAll(node2.getInvolveWuClasses());
		return union;

	}
	
	protected Set<Edge> getEdgeUnion(ColocationGraphNode node1, ColocationGraphNode node2) {
		Set<Edge> union = new HashSet<Edge>(node1.getMergingEdges());
		union.addAll(node2.getMergingEdges());
		return union;
	}

	public boolean isEdgeExist(ColocationGraphEdge e) {
		for (ColocationGraphEdge edge : getEdges()) {
			if (edge.equals(e)) {
				return true;
			}
		}
		return false;
	}

	public List<ColocationGraphEdge> getEdges() {
		return edges;
	}


	public List<ColocationGraphNode> getNeighbors(ColocationGraphNode node) {
		List<ColocationGraphNode> neighbors = new ArrayList<ColocationGraphNode>();

		for (ColocationGraphEdge edge : getEdges()) {
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

	public void deleteEdge(ColocationGraphEdge edge) {
		edge.getInNode().decreaseDegree();
		edge.getOutNode().decreaseDegree();
		getEdges().remove(edge);
		// System.out.println("Deleting edge: " + edge.getInNode().getNodeId() +
		// ", " + edge.getOutNode().getNodeId());
	}


	protected void printEdges(){
		System.out.println("Links:" + edges.size());
		for (int i = 0; i < edges.size(); i++) {
			System.out.println(edges.get(i).toString());
		}
	}
}
