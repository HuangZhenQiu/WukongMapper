package edu.uci.eecs.wukong.util;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.Iterator;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.LocationConstraint;
import edu.uci.eecs.wukong.common.WuClass;

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
		List<Edge> edges = buildEdges(nodeMap, graph);
		
		HashMap<Integer, WuClass> classMap =  new HashMap<Integer, WuClass>();
		Iterator<WuClass> classIterator = nodeMap.values().iterator();
		while(classIterator.hasNext()) {
			WuClass wuclass = classIterator.next();
			classMap.put(wuclass.getWuClassId(), wuclass);
		}
		
		
		return new FlowBasedProcess(classMap, edges, FlowBasedProcess.TYPE.LINEAR);
	}
	
	private List<Edge> buildEdges(HashMap<Object, WuClass> objectMap, SimpleDirectedGraph<Object, DefaultEdge> graph) {
		Iterator<DefaultEdge> edgeIterator = graph.edgeSet().iterator();
		List<Edge> edges = new ArrayList<Edge>();
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
			Edge fbpEdge = new Edge(objectMap.get(source), objectMap.get(target), weight);
			edges.add(fbpEdge);
		}
		
		return edges;
	}
	
	private HashMap<Object, WuClass> assignClassIdToGraphNode(SimpleDirectedGraph<Object, DefaultEdge> graph) {
		
		HashMap<Object, WuClass> idMap = new HashMap<Object, WuClass>();
		Random random = new Random();
		Set<Object> vertexes = graph.vertexSet();
		Iterator<Object> objects = vertexes.iterator();
		int[] classMap = new int[classNumber];
		Util.reset(classMap);
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
