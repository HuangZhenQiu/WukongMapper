package edu.uci.eecs.wukong.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import edu.uci.eecs.wukong.colocation.ColocationGraphEdge;
import edu.uci.eecs.wukong.colocation.ColocationGraphNode;
import edu.uci.eecs.wukong.colocation.FlowGraph;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.util.Pair;

public class CollocationGraph {
	private List<ColocationGraphNode> mNodes;
	private List<ColocationGraphEdge> mEdges;

	public CollocationGraph(FlowGraph graph, WukongSystem system) {
		this.mNodes = new ArrayList<ColocationGraphNode>();
		this.mEdges = new ArrayList<ColocationGraphEdge>();
		this.initCollocation(graph, system);
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
	
	public void initCollocation(FlowGraph graph, WukongSystem system) {
		rawInitCollocationGraph(graph);
		
		ArrayList<Pair<ColocationGraphNode, ColocationGraphNode>> pair_list = new ArrayList<Pair<ColocationGraphNode, ColocationGraphNode>>();
		for (int i = 0; i < mNodes.size() - 1; i++) {
			for (int j = i + 1; j < mNodes.size(); j++) {
				Pair<ColocationGraphNode, ColocationGraphNode> pair = new Pair<ColocationGraphNode, ColocationGraphNode>(mNodes.get(i), mNodes.get(j));
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
						for(ColocationGraphNode remain_node: mNodes){
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

	private Set<Integer> getIntersection(ColocationGraphNode node1,
			ColocationGraphNode node2) {
		Set<Integer> intersection = new HashSet<Integer>(
				node1.getInvolveWuClasses());
		intersection.retainAll(node2.getInvolveWuClasses());
		return intersection;
	}

	private Set<Integer> getUnion(ColocationGraphNode node1,
			ColocationGraphNode node2) {
		Set<Integer> union = new HashSet<Integer>(
				node1.getInvolveWuClasses());
		union.addAll(node2.getInvolveWuClasses());
		return union;

	}
	
	private Set<Edge> getEdgeUnion(ColocationGraphNode node1, ColocationGraphNode node2) {
		Set<Edge> union = new HashSet<Edge>(node1.getMergingEdges());
		union.addAll(node2.getMergingEdges());
		return union;
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

	private boolean addEdge(ColocationGraphEdge edge) {
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

	private boolean isEdgeExist(ColocationGraphEdge e) {
		for (ColocationGraphEdge edge : getEdges()) {
			if (edge.equals(e)) {
				return true;
			}
		}
		return false;
	}

	public List<ColocationGraphNode> getNodes() {
		return mNodes;
	}

	public List<ColocationGraphEdge> getEdges() {
		return mEdges;
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

	public void deleteNodeOnly(ColocationGraphNode node) {
		getNodes().remove(node);
	}

	public void deleteEdge(ColocationGraphEdge edge) {
		edge.getInNode().decreaseDegree();
		edge.getOutNode().decreaseDegree();
		getEdges().remove(edge);
		// System.out.println("Deleting edge: " + edge.getInNode().getNodeId() +
		// ", " + edge.getOutNode().getNodeId());
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

		System.out.println("Links:" + mEdges.size());
		for (int i = 0; i < mEdges.size(); i++) {
			System.out.println(mEdges.get(i).toString());
		}

	}
}
