package edu.uci.eecs.wukong.common;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;

/*
 * Each collocation node represents a way to do merge on original FBP.
 * 
 */
public class CollocationGraphNode {
	private static int id = 0;
	private int nodeId;

	private HashSet<Integer> mWuClasses = new HashSet<Integer>();
	
	private List<Edge> mMergeEdges = new ArrayList<FlowBasedProcess.Edge>();
	private double mWeight = 0.0;
	private int mDegree = 0;
	
	private int deployDevice = -1;

	public CollocationGraphNode(HashSet<Integer> wuclasses,
			double amountOfSavingEnergy, List<Edge> mergingEdges) {
		this.mWeight = amountOfSavingEnergy;
		this.mWuClasses = new HashSet<Integer>(wuclasses);
		this.nodeId = id++;
		this.mMergeEdges = mergingEdges;
	}

	public List<Edge> getMergingEdges() {
		return mMergeEdges;
	}
	public HashSet<Integer> getInvolveWuClasses() {
		return mWuClasses;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void increaseDegree() {
		mDegree++;
	}

	public void decreaseDegree() {
		mDegree--;
	}

	public int getDegree() {
		return mDegree;
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

	public boolean equal(CollocationGraphNode node) {
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