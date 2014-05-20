package edu.uci.eecs.wukong.util;

import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.WuDevice;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
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
		Double[][] distances = getRandomDeviceDistanceMatrix(deviceNumber, distanceRange);

		for(int i = 0; i < deviceNumber; i++) {
			Util.reset(classMap);
			List<Integer> objectIds = new ArrayList<Integer>();
			for(int j = 0; j < 3; j++) {
				ran.setSeed(System.nanoTime() + j * j);
				int  classId = Math.abs(ran.nextInt()) % (classNumber - 1) + 1;
				classId = findClassId(glabalClassMap, classId);
				if(classMap[classId] == 0) {
					objectIds.add(classId);
					classMap[classId] = 1;
				}
			}
			
			WuDevice device = new WuDevice(i + 1, Double.MAX_VALUE, objectIds, getRandomDistance(landMarkNumber, distanceRange),
					new ArrayList<Double>(Arrays.asList(distances[i])), system);
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
		
		system.initialize(devices, distances, classNumber, landMarkNumber);
		 
		return system;
	}
	
	private int findClassId(int[] glabalClassMap, int number) {
		if(glabalClassMap[number] == 0) {
			glabalClassMap[number] = 1;
			return number;
		} else {
			int i = number;
			while(glabalClassMap[number] == 1 && i <= classNumber) {
				i = (i + 1)% classNumber;
				if(i == number) {
					break;
				}
			}
			
			if(i!= number && i <= classNumber) {
				glabalClassMap[i] = 1;
				return i;
			} else {
				return number;
			}
		}
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
}
