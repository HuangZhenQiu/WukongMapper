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

public class WuKongSystemVerificationFactory extends WuKongSystemFactory {

	private int objectPerDevice;
	
	public WuKongSystemVerificationFactory(int classNumber, int deviceNumber, int objectPerDevice, int landMarkNumber, int distanceRange,
			int gatewayNumber, int regionNumber, boolean withDistance) {
		super(classNumber, deviceNumber, landMarkNumber, distanceRange, gatewayNumber, regionNumber, withDistance);
		this.objectPerDevice = objectPerDevice;
	}
	
	private WukongSystem createWukongSystemWithDistance_Base(int K, int replica, Double[][] distances, boolean withDistance){
			
//		if(replica * classNumber >= K * deviceNumber) {
//			System.out.println("Not creatable");
//			return null;
//		}
		
		WukongSystem system = new WukongSystem(false);
		List<WuDevice> devices = new ArrayList<WuDevice>();
		List<Region> regions = new ArrayList<Region>();
		List<Gateway> gateways = new ArrayList<Gateway>();	
		
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
			int gatewayId = -1;
			switch(i){
				case 0:
				case 4:
					gatewayId = 0;
					break;
					
				case 2:
				case 3:
				case 1:
				case 5:
					gatewayId = 1;
					break;
					
				case 6:
				case 7:
					gatewayId = 2;
					break;
			}
			gateways.get(gatewayId).addDevice(devices.get(i));
			
			// set region
			int regionId = -1;
			switch(i){
				case 0:
				case 4:
				case 1:
				case 5:
					regionId = 0;
					break;
					
				case 2:
				case 3:
				case 6:
				case 7:
					regionId = 1;
					break;
			}
			regions.get(regionId).addDevice(devices.get(i));
			
			// set wuobjects
			switch(i){
				case 0:
				case 2:
					device.addWuObject(1);
					break;

				case 4:
				case 6:
					device.addWuObject(3);
					device.addWuObject(2);
					break;
					
				case 3:
					device.addWuObject(2);
					break;

				case 1:
					device.addWuObject(3);
					break;
					
				case 5:
				case 7:
					device.addWuObject(0);
					break;
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
			device = new WuDevice(i, Double.MAX_VALUE, objectIds,
				getRandomDistance(landMarkNumber, distanceRange), new ArrayList<Double>(Arrays.asList(distances[i])), system);
		} else {
			device = new WuDevice(i, Double.MAX_VALUE, objectIds,
				getRandomDistance(landMarkNumber, distanceRange), null, system);
		}
		
		return device;
	}
	
	public WukongSystem createRandomWuKongSystem() {
		return createWukongSystemWithDistance_Base(0, 0, null, withDistance);
	}
}