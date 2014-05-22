package edu.uci.eecs.wukong.colocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;

/*
 * Each collocation node represents a way to do merge on original FBP.
 * 
 */
public class ColocationGraphNode {
	
	private static int id = 0;
	private int nodeId;
	private Set<Integer> mWuClasses;

	private Set<Edge> mMergeEdges;
	private double mWeight = 0.0;
	private int deployDevice = -1;

	/*
	 * 
	 * Neighbors of this colocation graph node
	 * 
	 */
	private List<ColocationGraphNode> mNeighbors = new ArrayList<ColocationGraphNode>();

	public void addNeighbors(ColocationGraphNode node) {
		mNeighbors.add(node);
	}

	public boolean isNeighborExist(ColocationGraphNode node){
		for (ColocationGraphNode neigobor : getNeighbors()) {
			if (neigobor.equal(node)) {
				return true;
			}
		}
		return false;
	}
	public List<ColocationGraphNode> getNeighbors() {
		return mNeighbors;
	}

	public void removeNeighbors(ColocationGraphNode node) {
		for (ColocationGraphNode iter : mNeighbors) {
			if (iter.equal(node)) {
				mNeighbors.remove(node);
			}
		}
	}
	
	/*
	 * 
	 * Parents
	 * 
	 */
	private List<ColocationGraphNode> mParents = new ArrayList<ColocationGraphNode>();
	
	public List<ColocationGraphNode> getParents() {
		return mParents;
	}
	
	public boolean isParentExist(ColocationGraphNode node) {
		for (ColocationGraphNode parent : getParents()) {
			if (parent.equal(node)) {
				return true;
			}
		}
		return false;
	}
	
	public void addParents(ColocationGraphNode node) {
		if (!isParentExist(node)) {
			getParents().add(node);
		}
	}
	
	public void removeParents(ColocationGraphNode node) {
		if (isParentExist(node)) {
			getParents().remove(node);
		}
	}
	
	
	/*
	 * Constructor
	 */

	public ColocationGraphNode(Set<Integer> wuclasses,
			double amountOfSavingEnergy, Set<Edge> mergingEdges) {
		this.mWeight = amountOfSavingEnergy;
		this.mWuClasses = new HashSet<Integer>(wuclasses);
		this.nodeId = id++;
		this.mMergeEdges = mergingEdges;
	}

	
	/* Getter and Setter */
	public Set<Edge> getMergingEdges() {
		return mMergeEdges;
	}
	public Set<Integer> getInvolveWuClasses() {
		return mWuClasses;
	}

	public int getNodeId() {
		return nodeId;
	}

	public int getDegree() {
		return mNeighbors.size();
	}

	public void setNodeId(int id) {
		this.nodeId = id;
	}

	public double getWeight() {
		return mWeight;
	}

	public void setWeight(double weight) {
		this.mWeight = weight;
	}

	public boolean equal(ColocationGraphNode node) {
		if ((this.getInvolveWuClasses().size() == node.getInvolveWuClasses()
				.size() && this.getInvolveWuClasses().containsAll(
				node.getInvolveWuClasses()))) {
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		return "ID: " + this.getNodeId() + ", weight: " + this.getWeight()
				+ ", degree:" + this.getDegree() + ", wuclasses: "
				+ this.getInvolveWuClasses();
	}
}