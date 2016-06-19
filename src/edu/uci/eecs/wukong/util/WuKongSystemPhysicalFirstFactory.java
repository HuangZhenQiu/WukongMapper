package edu.uci.eecs.wukong.util;

import edu.uci.eecs.wukong.common.WuObject;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.WuDevice;
import edu.uci.eecs.wukong.common.Gateway;
import edu.uci.eecs.wukong.common.Region;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.uci.eecs.wukong.util.GraphGenerator;
import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;

public class WuKongSystemPhysicalFirstFactory extends WuKongSystemFactory {

	private int objectPerDevice;
	private int devicePerGateway;
	
	public WuKongSystemPhysicalFirstFactory(int classNumber, int deviceNumber, int objectPerDevice, int landMarkNumber, int distanceRange,
			int gatewayNumber, int regionNumber, boolean withDistance) {
		super(classNumber, deviceNumber, landMarkNumber, distanceRange, gatewayNumber, regionNumber, withDistance);
		this.objectPerDevice = objectPerDevice;
		this.devicePerGateway = deviceNumber / gatewayNumber;
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
	
		List<Integer> regionIdSeq = new ArrayList<Integer>();
		// create regions
		for (int i = 0; i < regionNumber; i++) {
			Region region = new Region(system);
			regions.add(region);
			regionIdSeq.add(i);
		}
		Collections.shuffle(regionIdSeq, new Random(System.nanoTime()));
		
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
		}

		int[] deviceNumberInGateway = new int[gatewayNumber];
		for (int i = 0; i < gatewayNumber; i++){
			deviceNumberInGateway[i] = gateways.get(i).getDeviceNumber();
		}
		int g_i = 0;
		int[] regionPerGateways = this.nextDistribution(gatewayNumber, (double)(regionNumber)/gatewayNumber, 1, deviceNumberInGateway, 0.015);
		for (Gateway g: gateways)
		{
			List<Integer> regionIds = new ArrayList<Integer>();
			for (int regionPerGateway = regionPerGateways[g_i++]; regionPerGateway > 0; regionPerGateway--){
				if(regionPerGateway == regionPerGateways[g_i-1]) System.out.println(regionPerGateways[g_i-1]);
				regionIds.add(regionIdSeq.remove(0));
			}
			
			int d_i = 0;
			for (WuDevice device: g.getAllDevices())
			{
				// set region
				int regionId = regionIds.get((d_i++)%regionIds.size());//ran.nextInt(regionIds.size()));
				this.assignPhysicalWuObjects(device, regionId, globalClassMap, K);
				regions.get(regionId).addDevice(device);
//				// Check out if every region reach average device number
//				if (i % 10 == 0) {
//					int avr =  i / regions.size();
//					regionId = 0;
//					for (Region region : regions) {
//						if (region.getDeviceNumber() < avr) {
//							i ++;
//							
//							List<Integer> objectIds = new ArrayList<Integer>();
//							device = this.createDevice(i, system, objectIds, distances);
//							devices.add(device);
//							
//							// set gateway
//							ran.setSeed(System.nanoTime() + i * i);
//							int gatewayId = ran.nextInt(gatewayNumber);
//							gateways.get(gatewayId).addDevice(device);
//							
//							// set region
//							region.addDevice(device);
//							this.assignPhysicalWuObjects(device, regionId, globalClassMap, K);
//						}
//						regionId++;
//					}
//				}
			}
		}
		
		// distribute replica of non-duplicate wuclasses to devices, all globalclassmap should reach replica after this operation
		for (int i = 0; i < regionNumber; i++) {
			for (int j = 0; j < classNumber; j++) {
				int time = 0;
				while(globalClassMap[i][j] < replica) {
					ran.setSeed(System.nanoTime() + i * j);
					if(regions.get(i).getDeviceNumber() == 0) System.out.println("r " + i + " # " + regions.get(i).getDeviceNumber());
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
	
	private int[] nextDistribution(int size, double expected_mean, int min, int[] max, double eps){
		int[] numbers = new int[size];
		Random ran = new Random();
		double sample_mean = 0;
		for (int i = 0; i < size; i++){
			int range = max[i] - min + 1;
			int number = ran.nextInt(range)+min;
			sample_mean += number;
			numbers[i] = number;
		}
		sample_mean /= size;

		while (Math.abs(sample_mean - expected_mean) >= eps || sample_mean > expected_mean){
			if (expected_mean > sample_mean){
				for (int i = 0; i < size; ++i){
					int number = numbers[i];
					if (number < expected_mean && number < max[i]){
						int new_number;
						if (max[i] <= Math.ceil(expected_mean)){
							new_number = max[i];
						} else {
							new_number = ran.nextInt(max[i] - (int)Math.ceil(expected_mean) + 1) + (int)Math.ceil(expected_mean);							
						}
						sample_mean = (sample_mean*size - number + new_number)/size;
						numbers[i] = new_number;
						break;
					}
				}
				
			}else if (expected_mean < sample_mean){
				for (int i = 0; i < size; ++i){
					int number = numbers[i];
					if (number > expected_mean){
						int new_number;
						new_number = ran.nextInt((int)Math.floor(expected_mean) - min + 1) + min;
						sample_mean = (sample_mean*size - number + new_number)/size;
						numbers[i] = new_number;
						break;
					}
				}
			}
			System.out.println(expected_mean + " " + sample_mean + " " +size*expected_mean + " " + size*sample_mean);
			
			if (Math.abs(size*expected_mean - size*sample_mean) < 3){
				int diff = (int) Math.round(Math.abs(size*expected_mean - size*sample_mean));
				System.out.println("diff " + diff);
				if (sample_mean < expected_mean){
					for (; diff > 0; diff--){
						for (int i = 0; i < size; ++i){
							if (numbers[i] < max[i]){
								numbers[i]++;
								break;
							}
						}
					}
				} else {
					while (diff > 0){
						int i = ran.nextInt(size);
						if (numbers[i] > min){
							numbers[i]--;
						}else{
							continue;
						}
						diff--;
					}
				}
				break;
			}
			
		}
		
		return numbers;
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
		int wuObjectNumber = K;//ran.nextInt(K) + 1;
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