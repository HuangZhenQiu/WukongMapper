package edu.uci.eecs.wukong.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.uci.eecs.wukong.util.Util;

public class WuClassHierarchy {

	private int classNumber = 0;
	
	private List<WuClassConcept> vertices; 
	private WuClassConcept root;
 	
	private int[] classMap;
	public WuClassHierarchy(int classnumber){
		
		this.classNumber = classnumber;
		this.classMap = new int[this.classNumber];
		this.vertices = new ArrayList<WuClassConcept>();
		randomInit();
	}
	public int randomWuClassId(){
		Random random = new Random();
		random.setSeed(classNumber + System.nanoTime());
		Integer classId = Math.abs(random.nextInt() % classNumber);
		while(classMap[classId] == 1) {
			classId = Math.abs(random.nextInt() % classNumber);
		}
		return classId;
	}
	
	public double evaluate(WuClass class1, WuObject class2){
		double class_coff = evaluate(class1.getWuClassId(), class2.getWuClassId());
		
		double ans = 1;
		double dist = 0;
		for (int i = 0; i < class2.getProperties().length; i++){
			dist += class1.getUserPreferences()[i] * (1-class2.getProperties()[i]) * (1-class2.getProperties()[i]);
		}
		dist = Math.sqrt(dist);
		return class_coff*(ans-dist);
	}
	
	public double evaluate(int class1, int class2){
		
		WuClassConcept relation1 = getRelation(class1);
		WuClassConcept relation2 = getRelation(class2);
		
		return evaluate(relation1, relation2);
	}
	
	public double evaluate(WuClassConcept relation1, WuClassConcept relation2){
		if(relation1.src.getWuClassId() == relation2.src.getWuClassId()){
			return 1.0;
		}
		
		/* upward */ 
		WuClassConcept search = relation2;
		while(true){
			if(search.src.getWuClassId() == relation1.src.getWuClassId()){
				return 0.9;
			}
			
			if(search.isRoot()){
				break;
			}
			else{
				search = search.getFather();
			}
		}
		
		/* downward */ 
		search = relation1;
		while(true){
			if(search.src.getWuClassId() == relation2.src.getWuClassId()){
				return 0.9;
			}
			if(search.isRoot()){
				break;
			}
			else{
				search = search.getFather();
			}
		}
		return 0;
	}
	public WuClassConcept getRelation(int class1){
		for(WuClassConcept relation: vertices){
			if(relation.src.getWuClassId() == class1){
				return relation;
			}
		}
		return null;
	}
	public void randomInit(){
		Util.reset(classMap);
		
		int classId = randomWuClassId();
		classMap[classId] = 1;
		WuClass wuclass = new WuClass(classId, null);
		root = new WuClassConcept(wuclass, 0);
		vertices.add(root);
		
		/* deal with root */
		
		ArrayList<WuClassConcept> temp = new ArrayList<WuClassHierarchy.WuClassConcept>();
		temp.add(root);
		
		while(!temp.isEmpty()){
			WuClassConcept node = temp.remove(0);
			
			for(int j = 0; j < 3*(node.depth+1); j++){
				if(vertices.size() < classNumber) {
					int classid = randomWuClassId();
					classMap[classid] = 1;
					WuClass wuClass = new WuClass(classid, null);
					WuClassConcept relation2 = new WuClassConcept(wuClass, node.depth + 1);
					relation2.setFather(node);
					node.addChildren(relation2);
					vertices.add(relation2);
					temp.add(relation2);
				}
			}
		}
	}
	public class WuClassConcept{
		private WuClass src;
		private WuClassConcept father= null;
		private List<WuClassConcept> children;
		private int depth = 0;
		
		public WuClassConcept(WuClass src, int depth) {
			this.src = src;
			this.depth = depth;
			this.children = new ArrayList<WuClassConcept>(); 
		}
		public void setFather(WuClassConcept father) {
			this.father = father;
		}
		public WuClassConcept getFather(){
			return this.father;
		}
		public String toString(){
			return "WuClass Relation: <" + src.getWuClassId() +", "; 
		}
		public boolean hasChildren(){
			return (children.size() > 0);
		}
		public void addChildren(WuClassConcept child){
			children.add(child);
		}
		public List<WuClassConcept> getChildren(){
			return children;
		}
		public boolean isRoot(){
			return (depth == 0);
		}
	}
	
	public void print(){
		for (WuClassConcept wuClassRelation: vertices){
			System.out.println(wuClassRelation.src.getWuClassId());
		}
		recursivePrint(root);
	}
	public void recursivePrint(WuClassConcept relation){
		if(!relation.isRoot()){
			System.out.println("Id: " + relation.src.wuClassId + " and my parent is:" + relation.getFather().src.getWuClassId());
		}
		else{
			System.out.println("Root Id: " + relation.src.wuClassId);
		}
		for(WuClassConcept node: relation.getChildren()){
			recursivePrint(node);
		}
		
	}
}
