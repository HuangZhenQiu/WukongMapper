package edu.uci.eecs.wukong.common;

import java.util.ArrayList;
import java.util.List;

public class Path {
	List<WuClass> pathNodes;
	
	public Path() {
		pathNodes = new ArrayList<WuClass> ();
	}
	
	public void addNode(WuClass wuClass) {
		pathNodes.add(wuClass);
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder("[");
		for (WuClass wuClass : pathNodes) {
			if (!wuClass.equal(pathNodes.get(pathNodes.size() - 1))) {
				builder.append(wuClass.getWuClassId() + ",");
			} else {
				builder.append(wuClass.getWuClassId() + "]");
			}
		}
		
		return builder.toString();
	}
}
