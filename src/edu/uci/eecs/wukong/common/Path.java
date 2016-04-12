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
}
