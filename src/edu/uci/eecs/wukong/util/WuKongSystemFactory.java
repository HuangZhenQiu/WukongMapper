package edu.uci.eecs.wukong.util;

import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.WuDevice;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Date;


public class WuKongSystemFactory {

	private int landMarkNumber;
	private int classNumber;
	private int deviceNumber;
	private int distanceRange;
	
	public WuKongSystemFactory(int classNumber, int deviceNumber, int landMarkNumber, int distanceRange) {
		
		this.landMarkNumber = landMarkNumber;
		this.classNumber = classNumber;
		this.deviceNumber = deviceNumber;
		this.distanceRange = distanceRange;
	}
	
	public WukongSystem createRandomWuKongSystem() {
		WukongSystem system = new WukongSystem();
		List<WuDevice> devices = new ArrayList<WuDevice>();
		Random ran = new Random();
		int[] glabalClassMap = new int[classNumber];
		Util.reset(glabalClassMap);
		int[] classMap = new int[classNumber];

		for(int i = 0; i < deviceNumber; i++) {
			Util.reset(classMap);
			List<Integer> objectIds = new ArrayList<Integer>();
			for(int j = 0; j < 6; j++) {
				ran.setSeed(System.nanoTime() + j * j);
				int  classId = Math.abs(ran.nextInt()) % classNumber;
				if(classMap[classId] == 0) {
					objectIds.add(classId);
					classMap[classId] = 1;
					glabalClassMap[classId] = 1;
				}
			}
			
			WuDevice device = new WuDevice(i + 1, 10000.0, objectIds, getRandomDistance(landMarkNumber, distanceRange),
					getRandomDistance(landMarkNumber, distanceRange), system);
			devices.add(device);
		}
		
		//add wuclass haven't been added
		for (int i = 0; i < classNumber; i ++) {
			
			if(glabalClassMap[i] == 0) {
				ran.setSeed(System.nanoTime() + i * i);
				int deviceId = Math.abs(ran.nextInt()) % deviceNumber;
				devices.get(deviceId).addWuObject(i);
			}
		}
		
		system.initialize(devices, landMarkNumber, classNumber);
		 
		return system;
	}
	
	private List<Double> getRandomDistance(int landMarkNumber, int distanceRange) {
		Random random = new Random();
		List<Double> distances= new ArrayList<Double>();
		for(int i = 0; i < landMarkNumber; i++) {
			random.setSeed(System.nanoTime() + i*2);
			//distances.add(new Double(Math.abs(random.nextInt()) % distanceRange));
			distances.add(0.0);
		}
		
		return distances;
	}
}
