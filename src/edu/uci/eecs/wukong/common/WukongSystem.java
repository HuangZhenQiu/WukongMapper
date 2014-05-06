package edu.uci.eecs.wukong.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * 
 * 
 * @author Peter Huang
 *
 * TODO: Split out the binding between system and fbp
 * TODO: Support deploy multiple FBP into WukongSystem
 */
public class WukongSystem {

	private List<WuDevice> devices;
	
	private Double[][] distances;
	
	private HashMap<Integer, WuDevice> deviceMap;
	
	private HashMap<Integer, List<WuDevice>> wuClassDeviceMap;
	
	private FlowBasedProcess currentFBP;
	
	private Integer deviceNumber;
	
	private Integer wuClassNumber;
	
	private Integer landmarkNumber;
	
	
	public WukongSystem() {
		devices = new ArrayList<WuDevice>();
		deviceMap = new HashMap<Integer, WuDevice>();
		wuClassDeviceMap = new HashMap<Integer, List<WuDevice>>();
	}
	
	public WukongSystem(List<WuDevice> devices, int wuClassNumber, int landmarkNumber) {
		this.wuClassNumber = wuClassNumber;
		this.landmarkNumber = landmarkNumber;
		this.devices = devices;
		this.distances = new Double[this.devices.size()][this.devices.size()];
		this.deviceMap = new HashMap<Integer, WuDevice>();
		this.wuClassDeviceMap = new HashMap<Integer, List<WuDevice>>();
		initializeMap(this.devices);
	}
	
	public void initialize(List<WuDevice> devices, Double[][] distances, int wuClassNumber, int landmarkNumber) {
		this.wuClassNumber = wuClassNumber;
		this.landmarkNumber = landmarkNumber;
		this.devices.addAll(devices);
		this.deviceNumber = this.devices.size();
		this.distances = distances;
		initializeMap(this.devices);
	}
	
	
	private void initializeMap(List<WuDevice> devices) {
		Iterator<WuDevice> iterator =  devices.iterator();
		while(iterator.hasNext()) {
			WuDevice device = iterator.next();
			deviceMap.put(device.getWuDeviceId(), device);
			ImmutableList<Integer> classes= device.getAllWuObjectId();
			for (int i=0; i < classes.size(); i ++) {
				if(wuClassDeviceMap.containsKey(classes.get(i))) {
					wuClassDeviceMap.get(classes.get(i)).add(device);
				} else {
					List<WuDevice> devList = new ArrayList<WuDevice>();
					devList.add(device);
					wuClassDeviceMap.put(classes.get(i), devList);
				}
			}
			
		}
	}
	
	//read data from file
	public void initialize(BufferedReader input){
		 
		String content = "";
		
		try {
			
			//break the comments
			while((content = input.readLine()) != null) {
				
				if(content.length() > 0 && !content.startsWith("#")) {
					break;
				}
			}
			
			//begin initialization
			StringTokenizer tokenizer = new StringTokenizer(content);
			this.wuClassNumber = Integer.parseInt(tokenizer.nextToken());
			this.deviceNumber = Integer.parseInt(tokenizer.nextToken());
			this.landmarkNumber = Integer.parseInt(tokenizer.nextToken());
			
			//initialize the distance matrix;
			this.distances =  new Double[deviceNumber][deviceNumber];
			
			for(int i=0; i<deviceNumber; i++) {
				initializeWuDevice(input);
			}
			
		} catch(IOException e) {
			
			System.out.println("Wukong System initialization error!");
			System.exit(-1);
		}
	}
	
	private WuDevice initializeWuDevice(BufferedReader input) throws IOException{
		String content = input.readLine();
		StringTokenizer tokenizer = new StringTokenizer(content);
		Integer deviceId = Integer.parseInt(tokenizer.nextToken());
		Double energyConstraint = Double.parseDouble(tokenizer.nextToken());
		WuDevice device = new WuDevice(deviceId, energyConstraint, this);
		device.initialize(input);
		devices.add(device);
		deviceMap.put(deviceId, device);
		
		List<Integer> classIds = device.getAllWuObjectId();
		for (Integer id : classIds) {
			List<WuDevice> devices = wuClassDeviceMap.get(id);
			if (devices == null) {
				devices = new ArrayList<WuDevice>();
			}
			devices.add(device);
			wuClassDeviceMap.put(id, devices);
		}
		
		int id = device.getWuDeviceId() -1;
		for(int i = 0; i < this.deviceNumber; i ++) {
			distances[id][i] = device.getDeviceDistances().get(i); 
		}
		
		return device;
	}
	
	/**
	 * 
	 * @param source
	 * @param dest
	 * @return
	 */
	public Double getDistance(WuDevice source, WuDevice dest) {
		return distances[source.getWuDeviceId() - 1][dest.getWuDeviceId() - 1]; // Ids start from 1
	}
	
	public Double getDistance(int source, int dest) {
		if((1<= source && source <=this.deviceNumber.intValue())
				&& (1<= dest && dest <= this.deviceNumber)) {
			return distances[source - 1][dest - 1]; // Ids start from 1
		}
		
		return 0.0;
	}
	
	public ImmutableList<WuDevice> findWudevice(int wuClassId) {
		ImmutableList.Builder<WuDevice> builder = ImmutableList.<WuDevice>builder();
		for(WuDevice device : this.devices) {
			if (device.hasWuObject(wuClassId)) {
				builder.add(device);
			}
		}
		
		return builder.build();
	}
	
	public void reset() {
		this.currentFBP = null;
		for(WuDevice device : devices) {
			device.reset();
		}
	}
	
	public FlowBasedProcess getCurrentFBP() {
		
		return currentFBP;
	}

	public void setCurrentFBP(FlowBasedProcess currentFBP) {
		this.currentFBP = currentFBP;
	}
	
	public Double getLargestDeviceEnergtConsumption() {
		Double cost = Double.MIN_VALUE;
		for(WuDevice device : devices) {
			if (device.getCurrentConsumption() > cost) {
				cost = device.getCurrentConsumption();
			}
		}
		return cost;
	}
	
	public Double getTotalEnergyConsumption() {
		Double cost = 0.0;
		for(WuDevice device : devices) {
			cost+= device.getCurrentConsumption();
		}
		
		return cost;
	}

	public ImmutableList<WuDevice> getDevices() {
		
		return ImmutableList.<WuDevice>builder().addAll(this.devices).build();
	}
	
	public ImmutableMap<Integer, List<WuDevice>> getWuClassDeviceMap() {
		return ImmutableMap.<Integer, List<WuDevice>>builder().putAll(this.wuClassDeviceMap).build();
	}
	
	public ImmutableList<WuDevice> getPossibleHostDevice(Integer wuClassId) {
		return ImmutableList.<WuDevice>builder().addAll(wuClassDeviceMap.get(wuClassId)).build();
	}

	public int getDeviceNumber() {
		
		return devices == null ? 0 : devices.size();
	}
	
	public int getLandmarkNumber () {
		
		return this.landmarkNumber;
	}
	
	public int getWuClassNunber () {
		
		return this.wuClassNumber;
	}
	
	public boolean isMergable(WuClass source, WuClass dest) {
		for(WuDevice device: devices) {
			if(device.hasWuObject(source.getWuClassId()) 
					&& device.hasWuObject(dest.getWuClassId())){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isHostable(Set<Integer> done){
		for(WuDevice device: devices){
			Set<Integer> wuobjects = new HashSet<Integer>();
			for(Integer integer: device.getAllWuObjectId()){
				wuobjects.add(integer);
			}
			
			if (wuobjects.containsAll(done)) {
				return true;
			}
		}
		return false;
	}
	
	public WuDevice getHostableDevice(Set<Integer> done){
		for(WuDevice device: devices){
			Set<Integer> wuobjects = new HashSet<Integer>();
			for(Integer integer: device.getAllWuObjectId()){
				wuobjects.add(integer);
			}
			
			if (wuobjects.containsAll(done)) {
				return device;
			}
		}
		return null;
	}
	
	public boolean merge(FlowBasedProcess fbp) {
		List<FlowBasedProcess.Edge> temporaryList = new ArrayList<FlowBasedProcess.Edge>();
		PriorityQueue<WuDevice> deviceQueue = new PriorityQueue<WuDevice>();
		deviceQueue.addAll(devices);

		return merge(fbp, temporaryList, deviceQueue);
	}
	
	private boolean merge(FlowBasedProcess fbp, List<FlowBasedProcess.Edge> temporaryList, PriorityQueue<WuDevice> deviceQueue) {
		
		if(fbp == null) {
			return false;
		}
		
		this.currentFBP = fbp;

		PriorityQueue<FlowBasedProcess.Edge> edgeQueue = new PriorityQueue<FlowBasedProcess.Edge>();
		edgeQueue.addAll(fbp.getEdges());
		
		
		FlowBasedProcess.Edge currentEdge; 
		while (edgeQueue.size() > 0){
			currentEdge = edgeQueue.poll();
			
			//if both two end is undeployed
			if (currentEdge.isUndeployed()) {
				
				Iterator<WuDevice> itr = deviceQueue.iterator();
				while (itr.hasNext()) {
					
					WuDevice currentDevice = itr.next();
					if (currentDevice.deploy(currentEdge.getInWuClass().getWuClassId(), currentEdge.getOutWuClass().getWuClassId())) {
						currentEdge.getInWuClass().deploy(currentDevice.getWuDeviceId());
						currentEdge.getOutWuClass().deploy(currentDevice.getWuDeviceId());
						currentEdge.merge();
						deviceQueue.remove(currentDevice);
						deviceQueue.add(currentDevice);//reorder the priority queue.
						break;
					}
				}
				
				if (!currentEdge.isFullDeployed()) {
					temporaryList.add(currentEdge);
				}
				
			} else if (currentEdge.isPartialDeployed()) {
				
				Integer undeployedClassId  = currentEdge.getUndeployedClassId();
				Integer deviceId = currentEdge.getPartiallyDeployedDeviceId();
				WuDevice device = deviceMap.get(deviceId);
				
				if(device != null) {
					
					if(device.deploy(undeployedClassId)){
						currentEdge.getInWuClass().deploy(device.getWuDeviceId());
						currentEdge.getOutWuClass().deploy(device.getWuDeviceId());
						currentEdge.merge();
						deviceQueue.remove(device);
						deviceQueue.add(device);
					} else {
						temporaryList.add(currentEdge);
					}
					
				} else {
					//System.exit(-1);
				}
				
			} else { //two ends of the edge  are deployed.
				continue;
			}
			
		}
		
		updateEnergyConsumption();
		
		return true;
	}
	
	private void updateEnergyConsumption() {
		
		for(WuDevice device: devices) {
			device.updateEnergyConsumption();
		}
	}
	
	public boolean deploy(Integer deviceId, Integer wuClassId) {
		
		WuDevice device = this.deviceMap.get(deviceId);
		if(device == null) {
			System.out.println("Error Device Id: " + deviceId);
			return false;
		}
		return device.deploy(wuClassId);
	}

	public boolean deployWithNoMerge(FlowBasedProcess fbp, ArrayList<FlowBasedProcess.Edge> temporaryList){
		PriorityQueue<WuDevice> deviceQueue = new PriorityQueue<WuDevice>();
		deviceQueue.addAll(devices);
		
		for (FlowBasedProcess.Edge edge : temporaryList) {
			System.out.println("Checking edge: "+edge.getInWuClass().wuClassId + ", " + edge.getOutWuClass().wuClassId);
			if(edge.isPartialDeployed()) {
				Integer undeployedClassId  = edge.getUndeployedClassId();
				deployOneEnd(edge, undeployedClassId, deviceQueue);
			}
			
			if(edge.isUndeployed()) {
				deployOneEnd(edge, edge.getInWuClass().getWuClassId(), deviceQueue);
				deployOneEnd(edge, edge.getOutWuClass().getWuClassId(), deviceQueue);
			}
			
			
			if (!edge.isFullDeployed()) {
				System.out.println("Edge <" + edge.getInWuClass().getWuClassId() + ", " + edge.getOutWuClass().getWuClassId() + "> is undepoyable.");
				return false;
			}
		}
		return true;
	}
	
	public boolean deploy(FlowBasedProcess fbp) {
		
		
		List<FlowBasedProcess.Edge> temporaryList = new ArrayList<FlowBasedProcess.Edge>();
		PriorityQueue<WuDevice> deviceQueue = new PriorityQueue<WuDevice>();
		deviceQueue.addAll(devices);
		
		if(!merge(fbp, temporaryList, deviceQueue)) {
			
			return false;
		}
		
		for (FlowBasedProcess.Edge edge : temporaryList) {
				
			if(edge.isPartialDeployed()) {
				Integer undeployedClassId  = edge.getUndeployedClassId();
				deployOneEnd(edge, undeployedClassId, deviceQueue);
			}
			
			if(edge.isUndeployed()) {
				deployOneEnd(edge, edge.getInWuClass().getWuClassId(), deviceQueue);
				deployOneEnd(edge, edge.getOutWuClass().getWuClassId(), deviceQueue);
			}
			
			
			if (!edge.isFullDeployed()) {
				System.out.println("Edge <" + edge.getInWuClass().getWuClassId() + ", " + edge.getOutWuClass().getWuClassId() + "> is undepoyable.");
				return false;
			}
		}
		
		return true;
	}
	
	private boolean deployOneEnd(FlowBasedProcess.Edge edge, int wuclassId, PriorityQueue<WuDevice> deviceQueue) {
		Iterator<WuDevice> itr = deviceQueue.iterator();
		while (itr.hasNext()) {
			
			WuDevice currentDevice = itr.next();
			if (currentDevice.deploy(wuclassId)) {
				
				if (edge.getInWuClass().getWuClassId() == wuclassId) {
					edge.getInWuClass().deploy(currentDevice.getWuDeviceId());
				} else {
					edge.getOutWuClass().deploy(currentDevice.getWuDeviceId());
				}
				deviceQueue.remove(currentDevice);
				deviceQueue.add(currentDevice);//reorder the priority queue.
				return true;
			}
		}
		
		return false;
	}
	
	public String toString() {
		String str = "";
		for(WuDevice device : devices) {
			str = str +  device.toString() + "\n";
		}
		
		str = str + "Total Energt Consumption is:" + this.getTotalEnergyConsumption();
		return str;
	}

 }
