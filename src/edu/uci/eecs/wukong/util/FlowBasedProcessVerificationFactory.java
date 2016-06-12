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

public class FlowBasedProcessVerificationFactory extends FlowBasedProcessFactory {
		
	private int sensorNumber;
	public FlowBasedProcessVerificationFactory(int landMarkNumber, int classNumber, int virtualNumber,
			int distanceRange, int dataVolumnRange, int sensorNumber) {
		super(landMarkNumber, classNumber, virtualNumber, distanceRange, dataVolumnRange);
		this.sensorNumber = 3;
		this.classNumber = 4;
	}
	
	public FlowBasedProcess createFlowBasedProcess(){		
		
		SimpleDirectedGraph<Object, DefaultEdge> g =
	            new SimpleDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);
		HashMap<Object, WuClass> nodeMap = new HashMap<Object, WuClass>();

		// 3 to 1 tree graph
		Object currentObject = new Object();
		g.addVertex(currentObject);
		WuClass wuclass = new WuClass(0, generatRandomLocationConstraint(distanceRange));
		wuclass.setVirtual(false);
		nodeMap.put(currentObject, wuclass);
		
		for (int i = 0; i < sensorNumber; ++i){
			Object leafObject = new Object();
			g.addVertex(leafObject);
			g.addEdge(leafObject, currentObject);
			wuclass = new WuClass(i+1, generatRandomLocationConstraint(distanceRange));
			wuclass.setVirtual(false);
			nodeMap.put(leafObject, wuclass);
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
