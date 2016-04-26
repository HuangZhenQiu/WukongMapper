package edu.uci.eecs.wukong.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
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

	private int wuDeviceId;
	private List<WuObject> wuObjects;
	private List<WuObject> virtualObjects;
	private WukongSystem system;
	private Gateway gateway;
	private Region region;
	private boolean enabled;
	
	
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
		this.virtualObjects = new ArrayList<WuObject>();
		this.distances = new ArrayList<Double>();
		this.deviceDistances = new ArrayList<Double>();
		this.system = system;
	}
	
	public void setRegion(Region region) {
		this.region = region;
	}
	
	public Region getRegion() {
		return this.region;
	}
	
	public void removeObject(WuClass wuclass) {
		List<WuObject> removed = new ArrayList<WuObject> ();
		for (WuObject object : wuObjects) {
			if (object.getWuClassId().equals(wuclass.getWuClassId())) {
				removed.add(object);
			}
		}
		
		wuObjects.removeAll(removed);
		this.region.removeDeviceForClass(wuclass.getWuClassId(), this);
	}
	
	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}
	
	public Gateway getGateway() {
		return this.gateway;
	}
	
	public boolean isEnabled() {
		for (WuObject object : wuObjects) {
			if (object.isActive()) {
				return true;
			}
		}
		
		if (!this.virtualObjects.isEmpty()) {
			return true;
		}
		
		return false;
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
		this.virtualObjects = new ArrayList<WuObject>();
		this.distances = distances;
		this.deviceDistances = deviceDistances;
		this.system = system;
		
		for (Integer wuclassId: wuClassIds) {
			WuObject wuObject = new WuObject(wuclassId, this);
			wuObjects.add(wuObject);
		}
		
	}
	
	public ImmutableList<WuClass> getHostableWuClass(FlowBasedProcess fbp) {
		ImmutableList.Builder<WuClass> builder = ImmutableList.<WuClass>builder();
		for(WuObject object : wuObjects) {
			if(fbp.getWuClass(object.getWuClassId())!=null) {
				builder.add(fbp.getWuClass(object.getWuClassId()));
			}
		}
		
		// add the consideration of virtual wuclass
		for (WuClass wuClass : fbp.getAllComponents()) {
			if (wuClass.isVirtual()) {
				builder.add(wuClass);
			}
		}
		
		return builder.build();
	}
	
	public void reset(){
		for (WuObject object : wuObjects) {
			object.deactivate();
		}
		
		this.virtualObjects.clear();
		this.currentConsumption = 0.0;
	}
	
	public boolean deployComponent(WuClass wuClass) {
		
		if (wuClass.isVirtual()) {
			this.virtualObjects.add(new WuObject(wuClass.getWuClassId(), this));
			return true;
		}
		
		for (WuObject object : wuObjects) {
			if (object.getWuClassId() == wuClass.getWuClassId() &&
					! object.isActive()) {
				object.activate();
				return true;
			}
		}
		
		return false;
	}
	
	public boolean undeployComponent(WuClass wuClass) {
		if (wuClass.isVirtual()) {
			this.virtualObjects.remove(wuClass);
			return true;
		}
		
		for (WuObject object : wuObjects) {
			if (object.getWuClassId() == wuClass.getWuClassId() &&
					object.isActive()) {
				object.activate();
				return true;
			}
		}
		
		return false;
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
//			wuObjectMap.put(classId, object);
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
	
	
	/* 
	 * 
	 * Operations for WuObject list
	 * 
	 */
	public boolean isHostable(Set<Integer> done){
		if(done.containsAll(this.getAllWuObjectClassId())){
			return true;
		}
		else if(this.getAllWuObjectClassId().containsAll(done)){
			return true;
		}
		return false;
	}

	public List<WuObject> getWuObjects() {
		return wuObjects;
	}
	
	public ImmutableList<Integer> getAllWuObjectClassId() {
		List<Integer> ids = new ArrayList<Integer>();
		
		for (WuObject wuObject : getWuObjects()) {
			ids.add(wuObject.getWuClassId());
		}
		
		return ImmutableList.<Integer>builder().addAll(ids).build();
	}
	
	public ImmutableList<Integer> getAllActiveWuObjectClassId() {
		List<Integer> ids = new ArrayList<Integer>();
		for (WuObject wuObject : getWuObjects()) {
			if (wuObject.isActive()) {
				ids.add(wuObject.getWuClassId());
			}
		}
		
		return ImmutableList.<Integer>builder().addAll(ids).build();
	}
	
	private List<Integer> getCopiedAllActiveWuObjectId() {
		List<Integer> copy = new ArrayList<Integer>();
		copy.addAll(getAllActiveWuObjectClassId());
		
		return copy;
	}
	
	public WuObject getWuObject(int wuclassId) {
		for(WuObject object: getWuObjects()){
			if(object.getWuClassId() == wuclassId) {
				return object;
			}
		}
		return null;
	}
	
	public boolean deploy(Integer inNodeId, Integer outNodeId) {
		WuObject inNode = getWuObject(inNodeId);
		WuObject outNode = getWuObject(outNodeId);
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
					system.getCurrentFBP().deploy(inNodeId, this);
					outNode.activate();
					system.getCurrentFBP().deploy(outNodeId, this);
					
					//update the current energy consumption
					currentConsumption = energyConsumption;
					return true;
				}
			}
		}
		return false;
	}
				
	public boolean isWuObjectExist(WuObject object){
		for (WuObject wuObject: getWuObjects()){
			if(wuObject.getWuClassId() == object.getWuClassId()){
				return true;
			}
		}
		return false;
	}
	
	public boolean isWuObjectExist(Integer wuclassId) {
		for (WuObject wuobject : getWuObjects()) {
			if (wuobject.getWuClassId() == wuclassId) {
				return true;
			}
		}
		return false;
	}
	
	public boolean deployable(Integer wuClassId) {
		for (WuObject wuobject : getWuObjects()) {
			if (wuobject.getWuClassId() == wuClassId &&
					!wuobject.isActive()) {
				return true;
			}
		}
		return false;
	}
	
	public void addWuObject(int wuClassId) {
		if(!isWuObjectExist(wuClassId)){
			WuObject object = new WuObject(wuClassId, this);
			this.wuObjects.add(object);
		}
	}
	
	public boolean deploy(Integer nodeId, int[][] channels) {
		WuObject node = getWuObject(nodeId);
		if (node != null){
			List<Integer> nodes = new ArrayList<Integer>();
			nodes.add(nodeId);
			Double energyConsumption = getAfterDeploymentEngeryConsumption(nodes);
			node.activate();
			system.getCurrentFBP().deploy(nodeId, this);
				
				//update the current energy consumption
			currentConsumption = energyConsumption;
			return true;
		}
		else{
			// Can't deploy nodeId to this device
			System.out.println("Can't find out the wuclass" + nodeId);
		}
		return false;
	}
	
	public boolean deploy(Integer nodeId) {

		WuObject node = getWuObject(nodeId);
		if (node != null){
			List<Integer> nodes = new ArrayList<Integer>();
			nodes.add(nodeId);
			Double energyConsumption = getAfterDeploymentEngeryConsumption(nodes);
			node.activate();
			system.getCurrentFBP().deploy(nodeId, this);
				
			//update the current energy consumption
			currentConsumption = energyConsumption;
			return true;
		} else{
			System.out.println("Can't find out the wuclass" + nodeId);
		}
		
		return false;
	}
		
	public void updateEnergyConsumption() {
		currentConsumption = getAfterDeploymentEngeryConsumption(null);
	}
	
	private Double getAfterDeploymentEngeryConsumption(List<Integer> wuclassesToBeDeployed) {
		
		//TODO: modify it to make it good for initial energtConsumption;
		Double energyConsumption = initialConsumption;
		List<Integer> activeIds = getCopiedAllActiveWuObjectId();
		
		if(wuclassesToBeDeployed != null) {
			activeIds.addAll(wuclassesToBeDeployed);
		}

		for (Integer id : activeIds) {
			energyConsumption += system.getCurrentFBP().getNodeEnergyConsumption(id, activeIds);
		}
		
		return energyConsumption;
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
