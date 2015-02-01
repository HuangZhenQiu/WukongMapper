package edu.uci.eecs.wukong.util;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.Iterator;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.FlowBasedProcessEdge;
import edu.uci.eecs.wukong.common.LocationConstraint;
import edu.uci.eecs.wukong.common.WuClass;
import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;

public class FlowBasedProcessFactory {
	
	private int landMarkNumber;
	private int classNumber;
	private int distanceRange;
	private int dataVolumnRange;
	
	private GraphGenerator generator;
	
	public FlowBasedProcessFactory(int landMarkNumber, int classNumber,
			int distanceRange, int dataVolumnRange) {
		this.landMarkNumber = landMarkNumber;
		this.classNumber = classNumber;
		this.distanceRange = distanceRange;
		this.dataVolumnRange = dataVolumnRange;
		this.generator = new GraphGenerator();
	}
	
	/**
	 * By default the size of fbp is of half of total number of wuclasses.
	 * @param type
	 * @return
	 */
	public FlowBasedProcess createFlowBasedProcess(TYPE type) {
		
		SimpleDirectedGraph<Object, DefaultEdge> graph;
		
		switch (type) {
			case LINEAR:
				graph = generator.generateLinearGraph(classNumber / 2);
				break;
			case STAR:
				graph = generator.generateStarGraph(classNumber / 2);
				break;
			case RANDOM:
				graph = generator.generateRandomGraph(classNumber / 2 , classNumber / 2 - 1);			
				break;
			case SCALE_FREE:
				graph = generator.generateScaleFreeGraph(classNumber / 2);
				break;
			default:
				graph = generator.generateRandomGraph(classNumber, classNumber - 1);
			
		}
		
		HashMap<Object, WuClass> nodeMap = assignClassIdToGraphNode(graph);
		List<FlowBasedProcessEdge> edges = buildEdges(nodeMap, graph);
		
		HashMap<Integer, WuClass> classMap =  new HashMap<Integer, WuClass>();
		Iterator<WuClass> classIterator = nodeMap.values().iterator();
		while(classIterator.hasNext()) {
			WuClass wuclass = classIterator.next();
			if(ifExistInEdges(wuclass, edges)){
				classMap.put(wuclass.getWuClassId(), wuclass);
			}
		}
		
		return new FlowBasedProcess(classMap, edges, type);
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

	public boolean ifExistInEdges(WuClass wuclass, List<FlowBasedProcessEdge> edges) {
		for( FlowBasedProcessEdge edge : edges) {
			if(edge.getInWuClass().equal(wuclass) || edge.getOutWuClass().equal(wuclass)){
				return true;
			}
		}
		return false;
	}

	public boolean isEdgeExist(List<FlowBasedProcessEdge> edges, FlowBasedProcessEdge fbpedge) { 
		for(FlowBasedProcessEdge edge: edges) { 
			if(fbpedge.getInWuClass().equal(edge.getInWuClass()) && fbpedge.getOutWuClass().equal(edge.getOutWuClass())) {
				return true;
			}
			else if(fbpedge.getInWuClass().equal(edge.getOutWuClass()) && fbpedge.getOutWuClass().equal(edge.getInWuClass())){
				return true;
			}
		}
		return false;
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
