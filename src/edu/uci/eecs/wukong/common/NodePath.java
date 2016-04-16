package edu.uci.eecs.wukong.common;

import java.util.List;
import java.util.ArrayList;

public class NodePath {
	private List<Object> nodes;
	
	public NodePath() {
		this.nodes = new ArrayList<Object> ();
	}
	
	public NodePath addNode(Object node) {
		this.nodes.add(node);
		return this;
	}
	
	public boolean contains(Object object) {
		return nodes.contains(object);
	}
	
	public int getLength() {
		return nodes.size();
	}
	
	public NodePath copy() {
		NodePath newPath = new NodePath();
		for (Object object : nodes) {
			newPath.addNode(object);
		}
		
		return newPath;
	}
	
	public List<Object> getNodes() {
		return nodes;
	}
	
	public Object getLastNode() {
		if (!nodes.isEmpty()) {
			return this.nodes.get(nodes.size() - 1);
		}
		
		return null;
	}
}
