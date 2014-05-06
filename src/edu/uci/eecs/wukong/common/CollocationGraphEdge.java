package edu.uci.eecs.wukong.common;

public class CollocationGraphEdge {

	public CollocationGraphNode inNode;
	public CollocationGraphNode outNode;

	/*
	 * Each collocation edge represents the dependency of merging. If two nodes
	 * have a common edge, it means that two merging can not happen at the same
	 * time
	 */

	public CollocationGraphEdge(CollocationGraphNode in,
			CollocationGraphNode out) {
		inNode = in;
		outNode = out;
	}

	public CollocationGraphNode getInNode() {
		return inNode;
	}

	public CollocationGraphNode getOutNode() {
		return outNode;
	}

	public boolean equals(CollocationGraphEdge edge) {
		if (edge.getInNode().getNodeId() == this.getInNode().getNodeId()
				&& edge.getOutNode().getNodeId() == this.getOutNode()
						.getNodeId()) {
			return true;
		} else if (edge.getOutNode().getNodeId() == this.getInNode()
				.getNodeId()
				&& edge.getInNode().getNodeId() == this.getOutNode()
						.getNodeId()) {
			return true;
		}
		return false;
	}

	public boolean isInLink(CollocationGraphNode node) {
		return (this.getOutNode().getNodeId() == node.getNodeId());
	}

	public boolean isOutLink(CollocationGraphNode node) {
		return (this.getInNode().getNodeId() == node.getNodeId());
	}

	public String toString() {
		return "<" + this.getInNode().getNodeId() + ", "
				+ this.getOutNode().getNodeId() + ">"
				+ this.getInNode().getInvolveWuClasses() + " v.s "
				+ this.getOutNode().getInvolveWuClasses();
	}
}
