package edu.uci.eecs.wukong.util;

import edu.uci.eecs.wukong.common.FlowBasedProcessEdge;
import edu.uci.eecs.wukong.common.WuClass;
import edu.uci.eecs.wukong.common.WuObject;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.WuDevice;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.uci.eecs.wukong.util.GraphGenerator;
import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;

public class WuKongSystemFactory {

	private int landMarkNumber;
	private int classNumber;
	private int deviceNumber;
	private int distanceRange;
	private int K; /** Colocation Parameter **/
	
	public WuKongSystemFactory(int classNumber, int deviceNumber, int landMarkNumber, int distanceRange) {
		this.landMarkNumber = landMarkNumber;
		this.classNumber = classNumber;
		this.deviceNumber = deviceNumber;
		this.distanceRange = distanceRange;
	}
	
	public WuKongSystemFactory(int classNumber, int deviceNumber, int landMarkNumber, int distanceRange, int K){
		this.landMarkNumber = landMarkNumber;
		this.classNumber = classNumber;
		this.deviceNumber = deviceNumber;
		this.distanceRange = distanceRange;
		this.K = K;
	}
	
	public WukongSystem createMultiHopWukongSystem(int k, int replica, TYPE type) {		
		SimpleDirectedGraph<Object, DefaultEdge> graph;
		
		switch (type) {
			case LINEAR:
				graph = GraphGenerator.generateLinearGraph(classNumber / 2);
				break;
			case STAR:
				graph = GraphGenerator.generateStarGraph(classNumber / 2);
				break;
			case RANDOM:
				graph = GraphGenerator.generateRandomGraph(classNumber / 2 , classNumber / 2 - 1);			
				break;
			case SCALE_FREE:
				graph = GraphGenerator.generateScaleFreeGraph(classNumber / 2);
				break;
			default:
				graph = GraphGenerator.generateRandomGraph(classNumber, classNumber - 1);
			
		}
		
		Double[][] distances = getDistanceMatrixBasedOnGraph(graph, distanceRange);
		return createWukongSystemWithDistance(k, replica, distances);
	}
	
	public WukongSystem createRandomWukongSystem(int k, int replica) {
		Double[][] distances = getRandomDeviceDistanceMatrix(deviceNumber, distanceRange);
		return createWukongSystemWithDistance(k, replica, distances);
	}
	
	private WukongSystem createWukongSystemWithDistance(int K, int replica, Double[][] distances){
		
		
		if(replica * classNumber >= K * deviceNumber) {
			System.out.println("Not creatable");
			return null;
		}
		
		WukongSystem system = new WukongSystem();
		List<WuDevice> devices = new ArrayList<WuDevice>();
		
		Random ran = new Random();
		
		int[] globalClassMap = new int[classNumber]; // used for global recording wuclasses.
		Arrays.fill(globalClassMap, 0);
		int[] classMap = new int[classNumber]; // used for recording wuclasses on a device. 
		
		/* initial all devices */ 
		for(int i = 0; i < deviceNumber; i++) {
			List<Integer> objectIds = new ArrayList<Integer>();
			WuDevice device = new WuDevice(i + 1, Double.MAX_VALUE, objectIds, getRandomDistance(landMarkNumber, distanceRange), new ArrayList<Double>(Arrays.asList(distances[i])), system);
			devices.add(device);
		}
		
		// distribute replica of non-duplicate wuclasses to devices, all globalclassmap should reach replica after this operation 
		for (int i = 0; i < classNumber; i++) {
			while(globalClassMap[i] < replica) {
				ran.setSeed(System.nanoTime() + i * i);
				int deviceId = Math.abs(ran.nextInt()) % deviceNumber;
				
				if (!devices.get(deviceId).isWuObjectExist(i)){
					if(devices.get(deviceId).getAllWuObjectClassId().size() < K){
						devices.get(deviceId).addWuObject(i);
						globalClassMap[i] ++;
					}
				}
			}
		}
		
		for (int i = 0; i < deviceNumber; i++) {
			Arrays.fill(classMap, 0);
			
			List<WuObject> objectIds = devices.get(i).getWuObjects();
			for( WuObject objectId: objectIds){
				classMap[objectId.getWuClassId()] ++;
			}
			
			while(devices.get(i).getWuObjects().size() < K || devices.get(i).getWuObjects().size() == 0){
				ran.setSeed(System.nanoTime() + i * i);
				int classId = Math.abs(ran.nextInt()) % (classNumber - 1) + 1;
				
				if(classMap[classId] < replica){
					devices.get(i).addWuObject(classId);
					classMap[classId] ++;
				}
			}
			
		}
		
		system.initialize(devices, distances, false, classNumber, landMarkNumber);
		return system;
	}
	
	public WukongSystem createRandomWuKongSystem() {
		return createRandomWukongSystem(6, 1);
	}
	
	public WukongSystem createRandomMultiProtocolWuKongSystem(int numberChannel){
		WukongSystem system = createRandomWukongSystem(10, 1);
		int[][] channels = getRandomDeviceChannelMatrix(deviceNumber, numberChannel);
		system.setChannel(channels);
		return system;
	}
	
	public List<Double> getRandomDistance(int landMarkNumber, int distanceRange) {
		Random random = new Random();
		List<Double> distances= new ArrayList<Double>();
		for(int i = 0; i < landMarkNumber; i++) {
			random.setSeed(System.nanoTime() + i*2);
			distances.add(new Double(Math.abs(random.nextInt()) % distanceRange));
			
		}
		
		return distances;
	}
	
	public Double[][] getDistanceMatrixBasedOnGraph(SimpleDirectedGraph<Object, DefaultEdge> graph, int distanceRange) {
		Random random = new Random();
		HashMap<Object, Integer> idMap = Util.assignIdToGraphNode(graph);
		Set<Object> nodes = graph.vertexSet();
		Double[][] nodeArray = new Double[nodes.size()][nodes.size()];
		Arrays.fill(nodeArray, Double.MAX_VALUE);
		for(int i=0; i< nodes.size(); i++) {
			nodeArray[i][i] = 0.0;
		}
		
		Set<DefaultEdge> edges = graph.edgeSet();
		for (DefaultEdge edge : edges) {
			Object source = graph.getEdgeSource(edge);
			Object target = graph.getEdgeTarget(edge);
			random.setSeed(System.nanoTime());
			Double distance = new Double(Math.abs(random.nextInt()) % distanceRange);
			int sourceId = idMap.get(source);
			int targetId = idMap.get(target);
			nodeArray[sourceId][targetId] = distance;
			nodeArray[targetId][sourceId] = distance;
		}
		
		return Util.findShortestPath(nodeArray);
	}
	
	public Double[][] getRandomDeviceDistanceMatrix(int deviceNumber, int distanceRange) {
		Random random = new Random();
		Double[][] matrix = new Double[deviceNumber][deviceNumber];
		for(int i= 0; i < deviceNumber; i++) {
			for(int j=i+1; j < deviceNumber; j++) {
				random.setSeed(System.nanoTime() + i*2);
				Double distance = new Double(Math.abs(random.nextInt()) % distanceRange);
				matrix[i][j] = distance;
				matrix[j][i] = distance;
			}
			matrix[i][i] = 0.0; 
		}
		
		return matrix;
	}
	
	public int[][] getRandomDeviceChannelMatrix(int deviceNumber, int channelNumber) { 
		Random random = new Random();
		int[][] matrix = new int[deviceNumber][deviceNumber];
		
		for(int i = 0; i < deviceNumber; i++){
			for(int j=i+1; j < deviceNumber; j++) {
				random.setSeed(System.nanoTime() + i*2);
				int channel = Math.abs(random.nextInt()) % channelNumber + 1;
				
				switch (channel) {
				case 1:
//					matrix[i][j] = 70;
//					matrix[j][i] = 70;
					matrix[i][j] = 50;
					matrix[j][i] = 50;
					break;
				case 2: 
					matrix[i][j] = 50;
					matrix[j][i] = 50;
					break;
				case 3:
					matrix[i][j] = 50;
					matrix[j][i] = 50;
//					matrix[i][j] = 90;
//					matrix[j][i] = 90;
					break;
				case 4: 
					matrix[i][j] = 50;
					matrix[j][i] = 50;
//					matrix[i][j] = 110;
//					matrix[j][i] = 110;
					break;
				case 5:
					matrix[i][j] = 50;
					matrix[j][i] = 50;
//					matrix[i][j] = 70;
//					matrix[j][i] = 70;
					break;
				default:
					break;
				}
			}
			matrix[i][i] = 0;
		}
		return matrix;
	}
	
}
