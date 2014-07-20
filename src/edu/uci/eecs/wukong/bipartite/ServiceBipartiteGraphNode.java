package edu.uci.eecs.wukong.bipartite;

import java.util.List;

import edu.uci.eecs.wukong.common.WuClass;
import edu.uci.eecs.wukong.common.WuObject;

public class ServiceBipartiteGraphNode {
	
	private int wuclassId = -1;
	private boolean isMatched = false;
	
	private WuClass wuClass; 
	private WuObject wuObject;
	
	public ServiceBipartiteGraphNode(int wuclassId){
		this.wuclassId = wuclassId;
	}

	public void setWuClass(WuClass wuClass) {
		this.wuClass = wuClass;
	}
	public WuClass getWuClass(){
		return this.wuClass;
	}

	public void setWuObject(WuObject wuObject) {
		this.wuObject = wuObject;
	}
	
	public WuObject getWuObject(){
		return this.wuObject;
	}
	
	public int getClassId() { 
		return this.wuclassId;
	}
	public String toString() {
		return "Node: " + this.wuclassId;
	}
	
	public void checkMatch() { 
		isMatched = true;
	}
	public void resetMatch(){
		isMatched = false;
	}
	public boolean isMatched(){
		return isMatched;
	}
}
