package edu.uci.eecs.wukong.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.Iterator;
import java.util.LinkedList;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.FlowBasedProcessEdge;
import edu.uci.eecs.wukong.common.LocationConstraint;
import edu.uci.eecs.wukong.common.WuClass;
import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;

public class FlowBasedProcessTreeFactory extends FlowBasedProcessFactory {
	
	private int sensorNumber;
	private Random random = new Random();
	private int[][] trees;
	
	private GraphGenerator generator;
	
	public FlowBasedProcessTreeFactory(int landMarkNumber, int classNumber, int virtualNumber,
			int distanceRange, int dataVolumnRange, int sensorNumber) {
		super(landMarkNumber, classNumber, virtualNumber, distanceRange, dataVolumnRange);
		this.sensorNumber = sensorNumber;
		this.loadfile(sensorNumber);
	}
	
	public void loadfile(int sensorNumber){
		// read binary tree
		String thisLine = null;
		String root = System.getProperty("user.dir");
		try{
			// open input stream test.txt for reading purpose.
			BufferedReader br = new BufferedReader(new FileReader(new File(root + "/data/testcatalantrees_" + String.format ("%02d", sensorNumber) + "_leafs.txt")));
			int lc = 0;
			trees = new int[100][];
			while ((thisLine = br.readLine()) != null) {
				String[] strArray = thisLine.split(",");
				trees[lc] = new int[strArray.length];				
				for (int i = 0; i < strArray.length; ++i)
					trees[lc][i] = Integer.parseInt(strArray[i]);
				lc++;
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public FlowBasedProcess createFlowBasedProcess(int loop_index){		
		
		SimpleDirectedGraph<Object, DefaultEdge> g =
	            new SimpleDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);
		HashMap<Object, WuClass> nodeMap = new HashMap<Object, WuClass>();
				
		
		// generate binary tree
		BinarySearchTree b = new BinarySearchTree();
		for(int i = 0; i < sensorNumber-1; i++) {
		    b.insert(trees[loop_index][i]);
		}
		// transform tree to graph
		LinkedList<Node> nodelist = new LinkedList<Node>();
		LinkedList<Object> objectlist = new LinkedList<Object>();
		Node currentNode = b.root;
		Object currentObject = new Object();
		g.addVertex(currentObject);
		WuClass wuclass = new WuClass(generateWuClassId(false), generatRandomLocationConstraint(distanceRange));
		wuclass.setVirtual(false);
		nodeMap.put(currentObject, wuclass);
		while(true){		
			Object leftObject = new Object();
			if (currentNode.left != null){
				nodelist.add(currentNode.left);
				objectlist.add(leftObject);
			} else {
				wuclass = new WuClass(generateWuClassId(false), generatRandomLocationConstraint(distanceRange));
				wuclass.setVirtual(false);
				nodeMap.put(leftObject, wuclass);
			}
			g.addVertex(leftObject);
			g.addEdge(leftObject, currentObject);

			Object rightObject = new Object();
			if (currentNode.right != null){
				nodelist.add(currentNode.right);
				objectlist.add(rightObject);
			} else {
				wuclass = new WuClass(generateWuClassId(false), generatRandomLocationConstraint(distanceRange));
				wuclass.setVirtual(false);
				nodeMap.put(rightObject, wuclass);
			}
			g.addVertex(rightObject);
			g.addEdge(rightObject, currentObject);
			
			if (nodelist.size() == 0)
				break;

			currentNode = nodelist.pop();
			currentObject = objectlist.pop();
			g.addVertex(currentObject);
			wuclass = new WuClass(generateWuClassId(true), generatRandomLocationConstraint(distanceRange));
			wuclass.setVirtual(true);
			nodeMap.put(currentObject, wuclass);
		}
		
		// graph generated complete
		SimpleDirectedGraph<Object, DefaultEdge> graph = g;
				
		List<FlowBasedProcessEdge> edges = buildEdges(nodeMap, graph);
		HashMap<Integer, WuClass> classMap =  new HashMap<Integer, WuClass>();
		Iterator<WuClass> classIterator = nodeMap.values().iterator();
		while(classIterator.hasNext()) {
			wuclass = classIterator.next();
			if(ifExistInEdges(wuclass, edges)){
				classMap.put(wuclass.getWuClassId(), wuclass);
			}
		}
		
		return new FlowBasedProcess(graph, nodeMap, classMap, edges, TYPE.RANDOM);
	}

	private int generateWuClassId(boolean Virutal) {
		random.setSeed(classNumber + System.nanoTime());
		Integer classId;
		int halfClassNum = (int)Math.round(classNumber/2.0);
		if (Virutal){
			classId = random.nextInt(classNumber-halfClassNum) + halfClassNum;
		} else {
			classId = random.nextInt(halfClassNum);
		}
		return classId;
	}
	
	private List<FlowBasedProcessEdge> buildEdges(HashMap<Object, WuClass> objectMap, SimpleDirectedGraph<Object, DefaultEdge> graph) {
		Iterator<DefaultEdge> edgeIterator = graph.edgeSet().iterator();
		List<FlowBasedProcessEdge> edges = new ArrayList<FlowBasedProcessEdge>();
		Random random = new Random();
		
		while(edgeIterator.hasNext()) {
			DefaultEdge edge = edgeIterator.next();
			Object source = graph.getEdgeSource(edge);
			Object target = graph.getEdgeTarget(edge);
			random.setSeed(dataVolumnRange + System.nanoTime());
			//The meaning of weight becomes data volumn
			Integer weight = Math.abs(random.nextInt()) % dataVolumnRange;
			while(weight == 0) {
				weight = Math.abs(random.nextInt()) % dataVolumnRange;
			}
			FlowBasedProcessEdge fbpEdge = new FlowBasedProcessEdge(objectMap.get(source), objectMap.get(target), weight);
			if(!isEdgeExist(edges, fbpEdge)){
				edges.add(fbpEdge);
			}
		}
		
		return edges;
	}

	private HashMap<Object, WuClass> assignClassIdToGraphNode(SimpleDirectedGraph<Object, DefaultEdge> graph) {
		
		HashMap<Object, WuClass> idMap = new HashMap<Object, WuClass>();
		Random random = new Random();
		Set<Object> vertexes = graph.vertexSet();
		Iterator<Object> objects = vertexes.iterator();
		int[] classMap = new int[classNumber];
		Arrays.fill(classMap, 0);
		while(objects.hasNext()) {
			Object object= objects.next();
			random.setSeed(classNumber + System.nanoTime());
			Integer classId = Math.abs(random.nextInt() % classNumber);
			while(classMap[classId] == 1) {
				classId = Math.abs(random.nextInt() % classNumber);
			}
			classMap[classId] = 1;
			WuClass wuclass = new WuClass(classId, generatRandomLocationConstraint(distanceRange));
			idMap.put(object, wuclass);
		}
		
		return idMap;
	}
	
	private LocationConstraint generatRandomLocationConstraint(int range) {
		
		Random random = new Random();
		random.setSeed(landMarkNumber + System.nanoTime());
		Integer landMarkId = Math.abs(random.nextInt()) % landMarkNumber;
		
		random.setSeed(range + System.nanoTime());
		Double distance = new Double(Math.abs(random.nextInt() % range)) + 30;
		
		return new LocationConstraint(landMarkId, distance);
	}

}

class BinarySearchTree {
	public Node root;
	public BinarySearchTree(){
		this.root = null;
	}
	
	public boolean find(int id){
		Node current = root;
		while(current!=null){
			if(current.data==id){
				return true;
			}else if(current.data>id){
				current = current.left;
			}else{
				current = current.right;
			}
		}
		return false;
	}
	public boolean delete(int id){
		Node parent = root;
		Node current = root;
		boolean isLeftChild = false;
		while(current.data!=id){
			parent = current;
			if(current.data>id){
				isLeftChild = true;
				current = current.left;
			}else{
				isLeftChild = false;
				current = current.right;
			}
			if(current ==null){
				return false;
			}
		}
		//if i am here that means we have found the node
		//Case 1: if node to be deleted has no children
		if(current.left==null && current.right==null){
			if(current==root){
				root = null;
			}
			if(isLeftChild ==true){
				parent.left = null;
			}else{
				parent.right = null;
			}
		}
		//Case 2 : if node to be deleted has only one child
		else if(current.right==null){
			if(current==root){
				root = current.left;
			}else if(isLeftChild){
				parent.left = current.left;
			}else{
				parent.right = current.left;
			}
		}
		else if(current.left==null){
			if(current==root){
				root = current.right;
			}else if(isLeftChild){
				parent.left = current.right;
			}else{
				parent.right = current.right;
			}
		}else if(current.left!=null && current.right!=null){
			
			//now we have found the minimum element in the right sub tree
			Node successor	 = getSuccessor(current);
			if(current==root){
				root = successor;
			}else if(isLeftChild){
				parent.left = successor;
			}else{
				parent.right = successor;
			}			
			successor.left = current.left;
		}		
		return true;		
	}
	
	public Node getSuccessor(Node deleleNode){
		Node successsor =null;
		Node successsorParent =null;
		Node current = deleleNode.right;
		while(current!=null){
			successsorParent = successsor;
			successsor = current;
			current = current.left;
		}
		//check if successor has the right child, it cannot have left child for sure
		// if it does have the right child, add it to the left of successorParent.
//		successsorParent
		if(successsor!=deleleNode.right){
			successsorParent.left = successsor.right;
			successsor.right = deleleNode.right;
		}
		return successsor;
	}
	public void insert(int id){
		Node newNode = new Node(id);
		if(root==null){
			root = newNode;
			return;
		}
		Node current = root;
		Node parent = null;
		while(true){
			parent = current;
			if(id<current.data){				
				current = current.left;
				if(current==null){
					parent.left = newNode;
					return;
				}
			}else{
				current = current.right;
				if(current==null){
					parent.right = newNode;
					return;
				}
			}
		}
	}
	public void display(Node root){
		if(root!=null){
			display(root.left);
			System.out.print(" " + root.data);
			display(root.right);
		}
	}
//	public static void main(String arg[]){
//		BinarySearchTree b = new BinarySearchTree();
//		b.insert(3);b.insert(8);
//		b.insert(1);b.insert(4);b.insert(6);b.insert(2);b.insert(10);b.insert(9);
//		b.insert(20);b.insert(25);b.insert(15);b.insert(16);
//		System.out.println("Original Tree : ");
//		b.display(b.root);		
//		System.out.println("");
//		System.out.println("Check whether Node with value 4 exists : " + b.find(4));
//		System.out.println("Delete Node with no children (2) : " + b.delete(2));		
//		b.display(root);
//		System.out.println("\n Delete Node with one child (4) : " + b.delete(4));		
//		b.display(root);
//		System.out.println("\n Delete Node with Two children (10) : " + b.delete(10));		
//		b.display(root);
//	}
}

class Node{
	int data;
	Node left;
	Node right;	
	public Node(int data){
		this.data = data;
		left = null;
		right = null;
	}
}