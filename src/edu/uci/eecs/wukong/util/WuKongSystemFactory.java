package edu.uci.eecs.wukong.util;

import edu.uci.eecs.wukong.common.WuObject;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.WuDevice;
import edu.uci.eecs.wukong.common.Gateway;
import edu.uci.eecs.wukong.common.Region;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.uci.eecs.wukong.util.GraphGenerator;
import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;

public class WuKongSystemFactory {

	private int landMarkNumber;
	private int classNumber;
	private int deviceNumber;
	private int gatewayNumber;
	private int regionNumber;
	private int distanceRange;
	private int K; /** Colocation Parameter **/
	private boolean withDistance;
	
	public WuKongSystemFactory(int classNumber, int deviceNumber, int landMarkNumber, int distanceRange,
			int gatewayNumber, int regionNumber, boolean withDistance) {
		this.landMarkNumber = landMarkNumber;
		this.classNumber = classNumber;
		this.deviceNumber = deviceNumber;
		this.distanceRange = distanceRange;
		this.gatewayNumber = gatewayNumber;
		this.regionNumber = regionNumber;
		this.withDistance = withDistance;
	}
	
	public WukongSystem createMultiHopWukongSystem(int k, int replica, TYPE type, boolean withDistance) {		
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
		
		if (withDistance) {
			Double[][] distances = getDistanceMatrixBasedOnGraph(graph, distanceRange);
			return createWukongSystemWithDistance(k, replica, distances, withDistance);
		} else {
			return createWukongSystemWithDistance(k, replica, null, withDistance);
		}
	}
	
	public WukongSystem createRandomWukongSystem(int k, int replica) {
		if (withDistance) {
			Double[][] distances = getRandomDeviceDistanceMatrix(deviceNumber, distanceRange);
			return createWukongSystemWithDistance(k, replica, distances, withDistance);
		} else {
			return createWukongSystemWithDistance(k, replica, null, withDistance);
		}
		
	}
	
	private WukongSystem createWukongSystemWithDistance(int K, int replica, Double[][] distances, boolean withDistance){
		
		
		if(replica * classNumber >= K * deviceNumber) {
			System.out.println("Not creatable");
			return null;
		}
		
		WukongSystem system = new WukongSystem(false);
		List<WuDevice> devices = new ArrayList<WuDevice>();
		List<Region> regions = new ArrayList<Region>();
		List<Gateway> gateways = new ArrayList<Gateway>();
		
		Random ran = new Random();
		
		int[][] globalClassMap = new int[regionNumber][classNumber]; // used for global recording wuclasses.
		for (int i = 0; i < regionNumber; i++) {
			Arrays.fill(globalClassMap[i], 0);
		}
		int[] classMap = new int[classNumber]; // used for recording wuclasses on a device. 
	
		
		// create regions
		for (int i = 0; i < regionNumber; i++) {
			Region region = new Region(system);
			regions.add(region);
		}
		
		// create gateways
		for (int i = 0; i < gatewayNumber; i++) {
			Gateway gateway = new Gateway();
			gateways.add(gateway);
		}
		
		/* initial all devices */ 
		for(int i = 0; i < deviceNumber; i++) {
			List<Integer> objectIds = new ArrayList<Integer>();
			WuDevice device = createDevice(i, system, objectIds, distances);
			devices.add(device);
			
			// set gateway
			ran.setSeed(System.nanoTime() + i * i);
			int gatewayId = Math.abs(ran.nextInt()) % gatewayNumber;
			gateways.get(gatewayId).addDevice(devices.get(i));
			
			// set region
			ran.setSeed(System.nanoTime() + i * i);
			int regionId = Math.abs(ran.nextInt()) % regionNumber;
			regions.get(regionId).addDevice(devices.get(i));
			
			// Check out if every region reach average device number
			if (i % 10 == 0) {
				int avr =  i / regions.size();
				for (Region region : regions) {
					if (region.getDeviceNumber() < avr) {
						i ++;
						
						objectIds = new ArrayList<Integer>();
						device = createDevice(i, system, objectIds, distances);
						devices.add(device);
						
						// set gateway
						ran.setSeed(System.nanoTime() + i * i);
						gatewayId = Math.abs(ran.nextInt()) % gatewayNumber;
						gateways.get(gatewayId).addDevice(device);
						
						// set region
						region.addDevice(device);
					}
				}
			} 
		}
		
		
		// distribute replica of non-duplicate wuclasses to devices, all globalclassmap should reach replica after this operation
		for (int i = 0; i < regionNumber; i++) {
			for (int j = 0; j < classNumber; j++) {
				int time = 0;
				while(globalClassMap[i][j] < replica) {
					ran.setSeed(System.nanoTime() + i * i);
					int index = Math.abs(ran.nextInt() % regions.get(i).getDeviceNumber());
					
					WuDevice device = regions.get(i).getWuDevice(index);
					if (!device.isWuObjectExist(j) || time >10){
						if(device.getAllWuObjectClassId().size() < K || time >10){
							device.addWuObject(j);
							globalClassMap[i][j] ++;
						}
					}
					time++;
				}
			}
		}
		
		
		for (int i = 0; i < deviceNumber; i++) {
			Arrays.fill(classMap, 0);
			
			List<WuObject> objectIds = devices.get(i).getWuObjects();
			for ( WuObject objectId: objectIds) {
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
		
		for (int i = 0; i < regionNumber; i++) {
			regions.get(i).loadClassMap();
		}
		
		system.initialize(devices, regions, gateways, distances, false, classNumber, landMarkNumber);
		return system;
	}
	
	
	private WuDevice createDevice(int i, WukongSystem system, List<Integer> objectIds, Double[][] distances) {
		WuDevice device;
		if (withDistance) {
			device = new WuDevice(i + 1, Double.MAX_VALUE, objectIds,
				getRandomDistance(landMarkNumber, distanceRange), new ArrayList<Double>(Arrays.asList(distances[i])), system);
		} else {
			device = new WuDevice(i + 1, Double.MAX_VALUE, objectIds,
				getRandomDistance(landMarkNumber, distanceRange), null, system);
		}
		
		return device;
	}
	
	public WukongSystem createRandomWuKongSystem() {
		return createRandomWukongSystem(5, 3);
	}
	
	public WukongSystem createRandomMultiProtocolWuKongSystem(int numberChannel){
		WukongSystem system = createRandomWukongSystem(10, 1);
		if (withDistance) {
			int[][] channels = getRandomDeviceChannelMatrix(deviceNumber, numberChannel);
			system.setChannel(channels);
		}
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
