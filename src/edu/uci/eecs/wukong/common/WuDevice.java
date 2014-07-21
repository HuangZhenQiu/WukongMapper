package edu.uci.eecs.wukong.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.lang.Comparable;

import com.google.common.collect.ImmutableList;

/**
 * 
 * 
 * @author Peter Huang
 *
 */
public class WuDevice implements Comparable<WuDevice>{

	public class WuObject {
		
		private Integer wuClassId;
		private WuDevice device;
		private boolean active;
		
		public WuObject(Integer wuClassId, WuDevice device) {
			this.wuClassId = wuClassId;
			this.device = device;
			this.active = false;
		}
		
		public Integer getWuClassId() {
			return this.wuClassId;
		}
		
		public boolean isActive() {
			return active;
		}
		
		
		//They are only accessible by WuDevice
		protected void activate() {
			this.active = true;
		}
		
		protected void deactivate() {
			this.active = false;
		}
		
	}

	private int wuDeviceId;
	private List<WuObject> wuObjects;
	private HashMap<Integer, WuObject> wuObjectMap;
	private WukongSystem system;
	
	//Distance to landmark
	private List<Double> distances;
	
	private List<Double> deviceDistances;
	private Double energyConstraint;
	private Double initialConsumption;
	private Double currentConsumption;
	
	/**
	 * Constructor from file input.
	 * 
	 * @param wuDeviceId
	 * @param energyConstraint
	 */
	public WuDevice(int wuDeviceId, Double energyConstraint, WukongSystem system) {
		this.wuDeviceId =  wuDeviceId;
		this.energyConstraint = energyConstraint;
		this.initialConsumption = 0.0;
		this.currentConsumption = 0.0;
		this.wuObjects = new ArrayList<WuObject>();
		this.wuObjectMap = new HashMap<Integer, WuObject>();
		this.distances = new ArrayList<Double>();
		this.deviceDistances = new ArrayList<Double>();
		this.system = system;
	}
	
	/**
	 * Constructor from simulation data.
	 * 
	 * @param wuDeviceId
	 * @param energyConstraint
	 * @param distances
	 */
	public WuDevice(int wuDeviceId, Double energyConstraint, List<Integer> wuClassIds, List<Double> distances,
			List<Double> deviceDistances,  WukongSystem system){
		this.wuDeviceId =  wuDeviceId;
		this.energyConstraint = energyConstraint;
		this.currentConsumption = 0.0;
		this.initialConsumption = 0.0;
		this.wuObjects = new ArrayList<WuObject>();
		this.wuObjectMap = new HashMap<Integer, WuObject>();
		this.distances = distances;
		this.deviceDistances = deviceDistances;
		this.system = system;
		
		for (Integer id: wuClassIds) {
			WuObject object = new WuObject(id, this);
			wuObjects.add(object);
			wuObjectMap.put(id, object);
		}
		
	}
	
	public ImmutableList<WuClass> getHostableWuClass(FlowBasedProcess fbp) {
		ImmutableList.Builder<WuClass> builder = ImmutableList.<WuClass>builder();
		for(WuObject object : wuObjects) {
			if(fbp.getWuClass(object.getWuClassId())!=null)
			builder.add(fbp.getWuClass(object.getWuClassId()));
		}
		
		return builder.build();
	}
	
	public void reset(){
		for (WuObject object : wuObjects) {
			object.deactivate();
		}
		
		this.currentConsumption = 0.0;
	}
	
	public int compareTo(WuDevice device) {
		if (this.getCurrentConsumption() > device.getCurrentConsumption()) {
			return 1;
		} else if (this.getCurrentConsumption() < device.getCurrentConsumption()) {
			return -1;
		}
		
		return 0;
	}
	
	// read the distance into system
	public void initialize(BufferedReader input) throws IOException{
		
		//read wuobject information
		String content = input.readLine();
		StringTokenizer tokenizer = new StringTokenizer(content);
		
		while (tokenizer.hasMoreTokens()) {
			Integer classId = Integer.parseInt(tokenizer.nextToken());
			WuObject object = new WuObject(classId, this);
			wuObjects.add(object);
			wuObjectMap.put(classId, object);
		}
		
		//read distance to landmarks
		content = input.readLine();
		tokenizer = new StringTokenizer(content);
		while (tokenizer.hasMoreTokens()) {
			Double distance = Double.parseDouble(tokenizer.nextToken());
			distances.add(distance);
		}
		
		//read distance to devices
		content = input.readLine();
		tokenizer = new StringTokenizer(content);
		while (tokenizer.hasMoreTokens()) {
			Double distance = Double.parseDouble(tokenizer.nextToken());
			deviceDistances.add(distance);
		}
	}
	
	
	public boolean isWuObjectExist(Integer wuclassId) {
		for (Integer wuobject : getWuObjects()) {
			if (wuclassId == wuobject) {
				return true;
			}
		}
		return false;
	}
	
	public List<Integer> getWuObjects() {
		List<Integer> ids = new ArrayList<Integer>();
		
		for (WuObject wuObject : wuObjects) {
			ids.add(wuObject.wuClassId);
		}
		return ids;
	}
	
	public ImmutableList<Integer> getAllWuObjectId() {
		List<Integer> ids = new ArrayList<Integer>();
		
		for (WuObject wuObject : wuObjects) {
			ids.add(wuObject.wuClassId);
		}
		
		return ImmutableList.<Integer>builder().addAll(ids).build();
	}
	
	public ImmutableList<Integer> getAllActiveWuObjectId() {
		List<Integer> ids = new ArrayList<Integer>();
		for (WuObject wuObject : wuObjects) {
			if (wuObject.active) {
				ids.add(wuObject.wuClassId);
			}
		}
		
		return ImmutableList.<Integer>builder().addAll(ids).build();
	}
	
	public boolean deploy(Integer nodeId) {

		WuObject node = wuObjectMap.get(nodeId); 
		if (node != null){
			LocationConstraint nodeConstraint = system.getCurrentFBP().getLocationConstraintByWuClassId(nodeId);
			
			if (nodeConstraint == null || (nodeConstraint != null
					&& distances.get(nodeConstraint.getLandMarkId()) <= nodeConstraint.getDistance())) {
				List<Integer> nodes = new ArrayList<Integer>();
				nodes.add(nodeId);
				Double energyConsumption = getAfterDeploymentEngeryConsumption(nodes);
				if (energyConsumption < this.energyConstraint) {
					node.activate();
					system.getCurrentFBP().deploy(nodeId, wuDeviceId);
					
					//update the current energy consumption
					currentConsumption = energyConsumption;
					return true;
				}
			}
		}else{
			System.out.println("Can't find out the wuclass" + nodeId);
		}
		
		return false;
	}
	
	public boolean deploy(Integer inNodeId, Integer outNodeId) {
		WuObject inNode = wuObjectMap.get(inNodeId);
		WuObject outNode = wuObjectMap.get(outNodeId);
		if (inNode != null && outNode != null) {
			
			LocationConstraint inNodeConstraint = system.getCurrentFBP().getLocationConstraintByWuClassId(inNodeId);
			LocationConstraint outNodeConstraint = system.getCurrentFBP().getLocationConstraintByWuClassId(outNodeId);
			
			//Apply Location Constraint
			if ((inNodeConstraint == null || (inNodeConstraint != null 
					&& distances.get(inNodeConstraint.getLandMarkId()) <= inNodeConstraint.getDistance()))
					&& (outNodeConstraint == null || (outNodeConstraint != null
					&& distances.get(outNodeConstraint.getLandMarkId()) <= outNodeConstraint.getDistance()))) {
					
				//Apply Energy Constraint
				List<Integer> nodes = new ArrayList<Integer>();
				nodes.add(inNodeId);
				nodes.add(outNodeId);
				Double energyConsumption = getAfterDeploymentEngeryConsumption(nodes);
				if(energyConsumption < this.energyConstraint) {
				
					inNode.activate();
					system.getCurrentFBP().deploy(inNodeId, wuDeviceId);
					outNode.activate();
					system.getCurrentFBP().deploy(outNodeId, wuDeviceId);
					
					//update the current energy consumption
					currentConsumption = energyConsumption;
					return true;
				}
				
			}
		}
		
		return false;
	}
	
	public boolean hasWuObject(int wuClassId) {
		for(WuObject object : wuObjects) {
			if(object.getWuClassId().equals(wuClassId)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void updateEnergyConsumption() {
		currentConsumption = getAfterDeploymentEngeryConsumption(null);
	}
	
	private Double getAfterDeploymentEngeryConsumption(List<Integer> nodes) {
		
		//TODO: modify it to make it good for initial energtConsumption;
		Double energyConsumption = initialConsumption;
		List<Integer> activeIds = getCopiedAllActiveWuObjectId();
		
		if(nodes != null) {
			activeIds.addAll(nodes);
		}

		for (Integer id : activeIds) {
			energyConsumption += system.getCurrentFBP().getNodeEnergyConsumption(id, activeIds);
		}
		
		return energyConsumption;
	}
	
	private List<Integer> getCopiedAllActiveWuObjectId() {
		List<Integer> copy = new ArrayList<Integer>();
		copy.addAll(getAllActiveWuObjectId());
		
		return copy;
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		builder.append("<" + this.wuDeviceId + ", [" );
		for(int i = 0; i < this.wuObjects.size(); i++) {
			if(i <  this.wuObjects.size() - 1) {
				builder.append(wuObjects.get(i).getWuClassId() + ",");
			} else {
				builder.append(wuObjects.get(i).getWuClassId());
			}
		}
		builder.append("]," + this.currentConsumption + ">");
		builder.append(", " + this.energyConstraint);
		return builder.toString();
	}
	
	public void addWuObject(int wuObjectId) {
		WuObject object = new WuObject(wuObjectId, this);
		this.wuObjects.add(object);
		this.wuObjectMap.put(wuObjectId, object);
	}

	public int getWuDeviceId() {
		return wuDeviceId;
	}

	public Double getCurrentConsumption() {
		return currentConsumption;
	}

	public void setCurrentConsumption(Double currentConsumption) {
		this.currentConsumption = currentConsumption;
	}
	
	public Double getDistance(Integer landmarkId) {
		return distances.get(landmarkId);
	}

	public Double getEnergyConstraint() {
		return energyConstraint;
	}
	
	public List<Double> getLandmarkDistances() {
		return distances;
	}
	
	public List<Double> getDeviceDistances() {
		return deviceDistances;
	}

	public void setDeviceDistances(List<Double> deviceDistances) {
		this.deviceDistances = deviceDistances;
	}
	
	
	
}
