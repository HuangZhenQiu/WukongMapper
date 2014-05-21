package edu.uci.eecs.wukong.colocation;


public class ColocationGraphEdge {

	public ColocationGraphNode inNode;
	public ColocationGraphNode outNode;

	/*
	 * Each collocation edge represents the dependency of merging. If two nodes
	 * have a common edge, it means that two merging can not happen at the same
	 * time
	 */

	public ColocationGraphEdge(ColocationGraphNode in,
			ColocationGraphNode out) {
		inNode = in;
		outNode = out;
	}

	public ColocationGraphNode getInNode() {
		return inNode;
	}

	public ColocationGraphNode getOutNode() {
		return outNode;
	}

	public boolean equals(ColocationGraphEdge edge) {
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

	public boolean isInLink(ColocationGraphNode node) {
		return (this.getOutNode().getNodeId() == node.getNodeId());
	}

	public boolean isOutLink(ColocationGraphNode node) {
		return (this.getInNode().getNodeId() == node.getNodeId());
	}

	public String toString() {
		return "<" + this.getInNode().getNodeId() + ", "
				+ this.getOutNode().getNodeId() + ">"
				+ this.getInNode().getInvolveWuClasses() + " v.s "
				+ this.getOutNode().getInvolveWuClasses();
	}
}
