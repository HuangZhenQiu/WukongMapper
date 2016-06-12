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

public class WuKongSystemPhysicalFirstFactory extends WuKongSystemFactory {

	private int objectPerDevice;
	
	public WuKongSystemPhysicalFirstFactory(int classNumber, int deviceNumber, int objectPerDevice, int landMarkNumber, int distanceRange,
			int gatewayNumber, int regionNumber, boolean withDistance) {
		super(classNumber, deviceNumber, landMarkNumber, distanceRange, gatewayNumber, regionNumber, withDistance);
		this.objectPerDevice = objectPerDevice;
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
			int gatewayId = ran.nextInt(gatewayNumber);
			gateways.get(gatewayId).addDevice(devices.get(i));
			
			// set region
			ran.setSeed(System.nanoTime() + i * i);
			int regionId = ran.nextInt(regionNumber);
			regions.get(regionId).addDevice(devices.get(i));
			this.assignPhysicalWuObjects(device, regionId, globalClassMap, K);
			
			// Check out if every region reach average device number
			if (i % 10 == 0) {
				int avr =  i / regions.size();
				regionId = 0;
				for (Region region : regions) {
					if (region.getDeviceNumber() < avr) {
						i ++;
						
						objectIds = new ArrayList<Integer>();
						device = this.createDevice(i, system, objectIds, distances);
						devices.add(device);
						
						// set gateway
						ran.setSeed(System.nanoTime() + i * i);
						gatewayId = ran.nextInt(gatewayNumber);
						gateways.get(gatewayId).addDevice(device);
						
						// set region
						region.addDevice(device);
						this.assignPhysicalWuObjects(device, regionId, globalClassMap, K);
					}
					regionId++;
				}
			} 
		}
		
		// distribute replica of non-duplicate wuclasses to devices, all globalclassmap should reach replica after this operation
		for (int i = 0; i < regionNumber; i++) {
			for (int j = 0; j < classNumber; j++) {
				int time = 0;
				while(globalClassMap[i][j] < replica) {
					ran.setSeed(System.nanoTime() + i * j);
					int index = ran.nextInt(regions.get(i).getDeviceNumber());
					
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
		
		// ensure each deivce has enough wuobjects
		for (int i = 0; i < deviceNumber; i++) {
			Arrays.fill(classMap, 0);
			
			List<WuObject> objectIds = devices.get(i).getWuObjects();
			for ( WuObject objectId: objectIds) {
				classMap[objectId.getWuClassId()] ++;
			}
			
			while(devices.get(i).getWuObjects().size() < K || devices.get(i).getWuObjects().size() == 0){
				ran.setSeed(System.nanoTime() + i * i);
				int classId = ran.nextInt(classNumber);
				
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
	
	private void assignPhysicalWuObjects(WuDevice device, int regionId, int[][] globalClassMap, int K){
		Random ran = new Random();
		int halfClassNum = (int)Math.round(classNumber/2.0);
		int wuObjectNumber = ran.nextInt(K) + 1;
		while (device.getWuObjects().size() < wuObjectNumber){
			ran.setSeed(System.nanoTime() + wuObjectNumber);
			int classId = ran.nextInt(halfClassNum);
			if (!device.isWuObjectExist(classId)){
				device.addWuObject(classId);
				globalClassMap[regionId][classId]++;
				
			}
		}
	}
	
	public WukongSystem createRandomWuKongSystem() {
//		return createRandomWukongSystem(5, 3);
		return createWukongSystemWithDistance(objectPerDevice, 2, null, withDistance);
	}
}