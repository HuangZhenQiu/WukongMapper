package edu.uci.eecs.wukong.util;

import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.WuDevice;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;


public class WuKongSystemFactory {

	private int landMarkNumber;
	private int classNumber;
	private int deviceNumber;
	private int distanceRange;
	
	private int K;
	
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
	
	public WukongSystem createRandomWukongSystem(int K, int replica){
		
		
		if(replica * classNumber >= K * deviceNumber) {
			System.out.println("Not creatable");
			return null;
		}
		
		WukongSystem system = new WukongSystem();
		List<WuDevice> devices = new ArrayList<WuDevice>();
		
		Random ran = new Random();
		
		int[] globalClassMap = new int[classNumber]; // used for global recording wuclasses.
		Util.reset(globalClassMap);
		int[] classMap = new int[classNumber]; // used for recording wuclasses on a device. 
		Double[][] distances = getRandomDeviceDistanceMatrix(deviceNumber, distanceRange);
		
		/* initial all devices */ 
		for(int i = 0; i < deviceNumber; i++) {
			List<Integer> objectIds = new ArrayList<Integer>();
			WuDevice device = new WuDevice(i + 1, Double.MAX_VALUE, objectIds, getRandomDistance(landMarkNumber, distanceRange), new ArrayList<Double>(Arrays.asList(distances[i])), system);
			devices.add(device);
		}
		
		// distribute replica of non-duplicate wuclasses to devices, all globalclassmap should reach replica after this operation 
		for (int i = 0; i < classNumber; i ++) {
			
			while(globalClassMap[i] < replica ) {
				ran.setSeed(System.nanoTime() + i * i);
				int deviceId = Math.abs(ran.nextInt()) % deviceNumber;
				
				if (!devices.get(deviceId).isWuObjectExist(i)){
					if(devices.get(deviceId).getAllWuObjectId().size() < K){
						devices.get(deviceId).addWuObject(i);
						globalClassMap[i] ++;
					}
				}
			}
		}
		
		for (int i = 0; i < deviceNumber; i++) {
			Util.reset(classMap);
			
			List<Integer> objectIds = devices.get(i).getWuObjects();
			for( Integer objectId: objectIds){
				classMap[objectId] ++;
			}
			
			while(devices.get(i).getWuObjects().size() < K){
				ran.setSeed(System.nanoTime() + i * i);
				int classId = Math.abs(ran.nextInt()) % (classNumber - 1) + 1;
				
				if(classMap[classId] < replica){
					devices.get(i).addWuObject(classId);
					classMap[classId] ++;
				}
			}
			
		}
		
		system.initialize(devices, distances, classNumber, landMarkNumber);
		return system;
	}
	
	public WukongSystem createRandomWuKongSystem() {
		return createRandomWukongSystem(6, 1);
	}
	
	public WukongSystem createRandomMultiProtocolWuKongSystem(){
		WukongSystem system = createRandomWukongSystem(6, 1);
		int[][] channels = getRandomDeviceChannelMatrix(deviceNumber, 5);
		system.setChannel(channels);
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
	
	public int[][] getRandomDeviceChannelMatrix(int deviceNumber, int channelNumber) { 
		Random random = new Random();
		int[][] matrix = new int[deviceNumber][deviceNumber];
		
		for(int i = 0; i < deviceNumber; i++){
			for(int j=i+1; j < deviceNumber; j++) {
				random.setSeed(System.nanoTime() + i*2);
				int channel = Math.abs(random.nextInt()) % channelNumber + 1;
				matrix[i][j] = channel;
				matrix[j][i] = channel;
			}
			matrix[i][i] = 0;
		}
		return matrix;
	}
	
}
