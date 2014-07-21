package edu.uci.eecs.wukong.util;

import edu.uci.eecs.wukong.common.WuObject;
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
			System.out.println(device);
		}
		
		// distribute replica of non-duplicate wuclasses to devices, all globalclassmap should reach replica after this operation 
		for (int i = 0; i < classNumber; i ++) {
			
			while(globalClassMap[i] < replica ) {
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
			Util.reset(classMap);
			
			List<WuObject> objectIds = devices.get(i).getWuObjects();
			for( WuObject objectId: objectIds){
				classMap[objectId.getWuClassId()] ++;
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
