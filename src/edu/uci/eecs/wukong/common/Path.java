package edu.uci.eecs.wukong.common;

import java.util.ArrayList;
import java.util.List;

public class Path implements Comparable<Path> {
	private List<WuClass> pathNodes;
	
	public Path() {
		this.pathNodes = new ArrayList<WuClass> ();
	}
	
	public void addNode(WuClass wuClass) {
		pathNodes.add(wuClass);
	}
	
	public List<WuClass> getPathNodes() {
		return this.pathNodes;
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
	
	public int getHops() {
		int hops = 0;
		for (int i = 0; i < pathNodes.size() - 1; i++) {
			if (pathNodes.get(i).getDevice().equals(pathNodes.get(i + 1).getDevice())) {
				hops += 0;
			} else if (pathNodes.get(i).getDevice().getGateway().equals(pathNodes.get(i + 1).getDevice().getGateway())) {
				hops += 1;
			} else {
				hops += 2;
			}
		}
		
		return hops;
	}

	@Override
	public int compareTo(Path path) {
		if (this.getHops() > path.getHops()) {
			return -1;
		} else if (this.getHops() == path.getHops()) {
			return 0;
		} else {
			return 1;
		}
	}
}
