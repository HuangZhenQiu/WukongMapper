package edu.uci.eecs.wukong.common;

import java.util.HashSet;

import edu.uci.eecs.wukong.util.ObjectCloner;

public class CollocationGraphNode {

	private int mNodeId = 0;

	/*
	 * 
	 * Each collocation node represents a way to do merge on original FBP.
	 */
	private HashSet<Integer> mWuClasses = new HashSet<Integer>();
	private double mWeight = 0.0;
	private int degree = 0;

	public CollocationGraphNode(HashSet<Integer> wuclasses,
			double amountOfSavingEnergy) {
		this.mWeight = amountOfSavingEnergy;
		this.mWuClasses = (HashSet<Integer>) ObjectCloner.deepCopy(wuclasses);
	}

	public HashSet<Integer> getInvolveWuClasses() {
		return mWuClasses;
	}

	public int getNodeId() {
		return mNodeId;
	}

	public void increaseDegree() {
		degree++;
	}

	public void decreaseDegree() {
		degree--;
	}

	public int getDegree() {
		return degree;
	}

	public void setNodeId(int id) {
		this.mNodeId = id;
	}

	public double getWeight() {
		return mWeight;
	}

	public void setWeight(double weight) {
		this.mWeight = weight;
	}

	public boolean equal(CollocationGraphNode node){
		if((this.getInvolveWuClasses().size() == node.getInvolveWuClasses().size() && 
				this.getInvolveWuClasses().containsAll(node.getInvolveWuClasses()))){
			return true;
		} else{
			return false;
		}
	}
	
	public String toString(){
		return "ID: " + this.getNodeId() + ", weight: " + this.getWeight() + ", degree:" + this.getDegree() + ", wuclasses: "+ this.getInvolveWuClasses();
	}
}