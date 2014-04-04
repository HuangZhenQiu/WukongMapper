package edu.uci.eecs.wukong.common;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.Set;
import java.util.Iterator;

import java.lang.Comparable;
import java.io.BufferedReader;
import java.io.IOException;

import com.google.common.collect.ImmutableList;


/**
 * 
 * 
 * @author Peter Huang
 *
 */
public class FlowBasedProcess {
	
	public static class LocationConstraint {
		private int landMarkId;
		private Double distance;
		
		public LocationConstraint(int landMarkId, Double distance) {
			this.landMarkId = landMarkId;
			this.distance = distance;
		}

		public int getLandMarkId() {
			return landMarkId;
		}

		public void setLandMarkId(int landMarkId) {
			this.landMarkId = landMarkId;
		}

		public Double getDistance() {
			return distance;
		}

		public void setDistance(Double distance) {
			this.distance = distance;
		}
		
	}
	
	public static class WuClass {
		private int wuClassId;
		private int deviceId;
		private LocationConstraint locationConstraint;
		private boolean deployed;
		private Double energyCost; //It is used after merge.

		
		public WuClass(int wuClassId, LocationConstraint locationConstraint) {
			this.wuClassId = wuClassId;
			this.deviceId = -1;
			this.locationConstraint = locationConstraint;
			this.deployed = false;
			this.energyCost = 0.0;
		}
		
		public void reset() {
			this.deviceId = -1;
			this.deployed = false;
			this.energyCost = 0.0;
		}
		
		public void deploy(int deviceId) {
			this.deviceId = deviceId;
			this.deployed = true;
		}
		
		public void undeploy() {
			this.deployed = false;
		}

		public int getWuClassId() {
			return wuClassId;
		}

		public void setWuClassId(int wuClassId) {
			this.wuClassId = wuClassId;
		}

		public int getDeviceId() {
			return deviceId;
		}

		public void setDeviceId(int deviceId) {
			this.deviceId = deviceId;
		}

		public boolean isDeployed() {
			return deployed;
		}

		public void setDeployed(boolean deployed) {
			this.deployed = deployed;
		}
		
		public LocationConstraint getLocationConstraint() {
			return this.locationConstraint;
		}

		public Double getEnergyCost() {
			return energyCost;
		}

		//It is only used by FBP itself, after merge.
		private void setEnergyCost(Double energy) {
			this.energyCost = energy;
		}
		
	}
	
	public static class Edge implements Comparable<Edge>{

		private WuClass inWuClass;
		private WuClass outWuClass;
		private double transmissionEnergy;
		private double receivingEnergy;
		private double weight;
		private boolean isMerged;
		
		public Edge(WuClass inWuClass, WuClass outWuClass, double transmissionEnegery, double receivingEnergy) {
			this.inWuClass = inWuClass;
			this.outWuClass = outWuClass;
			this.transmissionEnergy = transmissionEnegery;
			this.receivingEnergy = receivingEnergy;
			this.weight = this.transmissionEnergy + this.receivingEnergy;
			this.isMerged = false;
		}
		
		@Override
		public int compareTo(Edge edge){
			
			if(this.weight > edge.weight) {
				return -1;
			} else if (this.weight < edge.weight) {
				return 1;
			} else 
				return 0;
		}
		
		public void merge() {
			
			if(inWuClass.isDeployed() && outWuClass.isDeployed() 
					&& inWuClass.getDeviceId() == outWuClass.getDeviceId()) {
				//this.transmissionEnergy = 0.0;
				//this.receivingEnergy = 0.0;
				//this.weight = 0.0;
				this.isMerged = true;
			}
		}
		
		public WuClass getInWuClass() {
			return inWuClass;
		}

		public void setInWuClass(WuClass inWuClass) {
			this.inWuClass = inWuClass;
		}

		public WuClass getOutWuClass() {
			return outWuClass;
		}

		public void setOutWuClass(WuClass outWuClass) {
			this.outWuClass = outWuClass;
		}
		
		public boolean isUndeployed() {
			
			return !inWuClass.deployed && !outWuClass.deployed;
		}
		
		public boolean isFullDeployed() {
			
			return inWuClass.deployed && outWuClass.deployed;
		}
		
		public boolean isPartialDeployed() {
			
			return !isFullDeployed() && (inWuClass.deployed || outWuClass.deployed);
		}
		
		public Integer getUndeployedClassId() {
			
			if (isFullDeployed() || !isPartialDeployed()) {
				return null;
			} else {
				
				if (inWuClass.deployed) {
					return outWuClass.wuClassId;
				} else {
					return inWuClass.wuClassId;
				}
				
			}
		}
		
		public boolean isMerged() {
			
			return this.isMerged;
		}
		
		public Integer getPartiallyDeployedDeviceId() {
			if (isFullDeployed() || !isPartialDeployed()) {
				return null;
			} else {
				if(inWuClass.deployed) {
					return inWuClass.deviceId;
				} else {
					return outWuClass.deviceId;
				}
			}
		}

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.weight = weight;
		}
		
		
	}
	
	public static enum TYPE {LINEAR, STAR, RANDOM, SCALE_FREE};
	
	private List<Edge> edges;
	
	private HashMap<Integer, WuClass> wuClassMap;
	
	//<WuClassId, Edges End at WuClassId>
	private HashMap<Integer, List<Edge>> inEdgeMap;
	
	//<WuClassId, Edges begin from WuClassId>
	private HashMap<Integer, List<Edge>> outEdgeMap;
	
	//WuClassId to LandMarkId distance constraint
	private HashMap<Integer, LocationConstraint> locationConstraints;
	
	private boolean isMerged;
	
	private FlowBasedProcess.TYPE type;
	
	
	public FlowBasedProcess(HashMap<Integer, WuClass> wuClassMap, List<Edge> edges, TYPE type) {
		this.isMerged = false;
		this.type = type;
		this.edges = edges;
		this.wuClassMap = wuClassMap;
		this.inEdgeMap = new HashMap<Integer, List<Edge>>();
		this.outEdgeMap = new HashMap<Integer, List<Edge>>();
		this.locationConstraints = new HashMap<Integer, LocationConstraint>();
		this.setupMaps();
	}
	
	public FlowBasedProcess(TYPE type) {
		
		this.edges = new ArrayList<Edge>();
		this.wuClassMap = new HashMap<Integer, WuClass>();
		this.inEdgeMap = new HashMap<Integer, List<Edge>>();
		this.outEdgeMap = new HashMap<Integer, List<Edge>>();
		this.locationConstraints = new HashMap<Integer, LocationConstraint>();
		this.isMerged = false;
		this.type = type;
	}
	
	//Read from file
	public void initialize(BufferedReader input) {
		
		String content = "";
		
		try {
			//break the comments
			while ((content = input.readLine()) != null) {
				
				if (content.length() > 0 && !content.startsWith("#")) {
					break;
				}
			}
			
			//begin initialization
			Integer classNumber  = Integer.parseInt(content);
			
			for (int i =0; i<classNumber; i++) {
				content = input.readLine();
				StringTokenizer tokenizer = new StringTokenizer(content);
				Integer classId = Integer.parseInt(tokenizer.nextToken());
				Integer landmarkId = Integer.parseInt(tokenizer.nextToken());
				Double distanceConstraint= Double.parseDouble(tokenizer.nextToken());
				LocationConstraint constraint= new LocationConstraint(landmarkId, distanceConstraint);
				WuClass wuClass = new WuClass(classId, constraint);
				wuClassMap.put(classId, wuClass);
				locationConstraints.put(classId, constraint);
				
			}
			
			content = input.readLine();
			Integer edgeNumber  = Integer.parseInt(content);
			
			for (int i =0; i<edgeNumber; i++) {
				content = input.readLine();
				StringTokenizer tokenizer = new StringTokenizer(content);
				Integer inNode = Integer.parseInt(tokenizer.nextToken());
				Integer outNode = Integer.parseInt(tokenizer.nextToken());
				Double transmitCost = Double.parseDouble(tokenizer.nextToken());
				Double receivingCost = Double.parseDouble(tokenizer.nextToken());
				Edge edge = new Edge(wuClassMap.get(inNode), wuClassMap.get(outNode), transmitCost, receivingCost);
				edges.add(edge);
			}
			
		} catch(IOException e) {
			
			System.out.println("Error in initialize FBP from file");
			System.exit(-1);
		}
		

		this.setupMaps();
	}
	
	
	private void setupMaps() {
		
		for (Edge edge: edges) {

			//Edge a -> b, put a->b into a's output edges
			List<Edge> outEdges = outEdgeMap.get(edge.inWuClass.wuClassId);
			if (outEdges!=null) {
				outEdges.add(edge);
			} else {
				outEdges = new ArrayList<Edge>();
				outEdges.add(edge);
				outEdgeMap.put(edge.inWuClass.wuClassId, outEdges);
			}
			
			//Edge a -> b, put a->b into b's input edges
			List<Edge> inEdges = inEdgeMap.get(edge.outWuClass.wuClassId);
			if (inEdges!=null) {
				inEdges.add(edge);
			} else {
				inEdges = new ArrayList<Edge>();
				inEdges.add(edge);
				inEdgeMap.put(edge.outWuClass.wuClassId, inEdges);
			}
		}
		
		if (this.locationConstraints.isEmpty()) {
			
			Iterator<WuClass>  wuClassIterator = wuClassMap.values().iterator();
			while(wuClassIterator.hasNext()) {
				WuClass wuclass = wuClassIterator.next();
				locationConstraints.put(wuclass.wuClassId, wuclass.getLocationConstraint());
			}
		}
	}
	
	public void merge() {
		
		for(Edge edge : edges) {
			if(edge.isFullDeployed() && edge.getInWuClass().getDeviceId() == edge.getOutWuClass().getDeviceId())  {
				edge.isMerged = true;
			}
		}
	}
	
	public ImmutableList<Edge> getMergedEdges() {
		ImmutableList.Builder<Edge> builder = ImmutableList.<Edge>builder();
		
		for(Edge edge : edges) {
			if(edge.isMerged()) {
				builder.add(edge);
			}
		}
		return builder.build();
	}
	
	public ImmutableList<Integer> getPreDeployedWuClasses() {
		ImmutableList.Builder<Integer> builder = ImmutableList.<Integer>builder();
		for(Edge edge : edges) {
			if(edge.isMerged()) {
				builder.add(edge.inWuClass.getWuClassId());
				builder.add(edge.outWuClass.getWuClassId());
			}
		}
		
		return builder.build();
	}
	
	public void reset() {
		
		Set<Integer> classIds= wuClassMap.keySet();
		for (Integer classId : classIds) {
			wuClassMap.get(classId).reset();
		}
		
		for (int i=0; i < edges.size(); i++) {
			edges.get(i).isMerged = false;
		}
	}
	
	public Double getNodeEnergyConsumption(Integer nodeId, List<Integer> deviceNeighbor) {
		
		Double energyConsumption = 0.0;
		List<Edge> inEdges= inEdgeMap.get(nodeId);
		
		if (inEdges !=null) {
			for (Edge inEdge : inEdges) {
				 if (!deviceNeighbor.contains(inEdge.inWuClass.wuClassId)) {
					 
					 if(!inEdge.isMerged) {
						 energyConsumption += inEdge.receivingEnergy;
					 } 
				 }
			}
		}
		
		
		List<Edge> outEdges = outEdgeMap.get(nodeId);
		
		if (outEdges !=null) {
			for (Edge outEdge : outEdges) {
				 if (!deviceNeighbor.contains(outEdge.outWuClass.wuClassId)) {
					 
					 if(!outEdge.isMerged) {
						 energyConsumption += outEdge.transmissionEnergy;
					 }
				 }
			}
		}
		
		return energyConsumption;
	}
	
	public boolean deploy(int wuClassId, int deviceId) {
		WuClass wuClass = wuClassMap.get(wuClassId);
		if (wuClass != null) {
			wuClass.deploy(deviceId);
			return true;
		}
		
		return false;
	}
	
	public boolean isDeployed() {
		
		Collection<WuClass> classes= wuClassMap.values();
		for (WuClass wuclass : classes) {
			
			if (!wuclass.isDeployed()) {
				
				return false;
			}
		}
		
		return true;
	}
	
	public void print() {
		
		for(Edge edge : edges) {
			System.out.println("Edge<" + edge.getInWuClass().getWuClassId() + ", "
					+ edge.getOutWuClass().getWuClassId() + ">  -->   Device<"
					+ edge.getInWuClass().deviceId +", " + edge.getOutWuClass().deviceId + ">");
		}
	}
	

	public ImmutableList<Edge> getEdges() {
		return ImmutableList.<Edge>builder().addAll(this.edges).build();
	}
	
	public Integer getEdgeNumber() {
		return edges == null ? 0 : edges.size();
	}	
	
	public LocationConstraint getLocationConstraintByWuClassId(Integer classId) {
		
		return locationConstraints.get(classId);
	}
	
	public Double getTotalEnergyConsumption() {
		Double total = 0.0;
		for (int i = 0; i < this.edges.size(); i++) {
			
			if(!edges.get(i).isMerged) {
				total += edges.get(i).weight;
			}
		}
		return total;
	}
	
	public Double getWuClassEnergyConsumption(Integer classId) {
		
		/*if (!this.isMerged) {
			System.out.println("Try to access WuClass Energy Consumption of Unmerged FBP.");
			return null;
		}*/
		
		WuClass wuClass= this.wuClassMap.get(classId);
		if (wuClass == null) {
			return null;
		} else {
			double energy = 0;
			List<Edge> inEdges = inEdgeMap.get(classId);
			if(inEdges != null) {
				for(Edge edge : inEdges) {
					if(!edge.isMerged()) {
						energy += edge.receivingEnergy;
					}
				}
			}
			
			List<Edge> outEdges = outEdgeMap.get(classId);
			if(outEdges != null) {
				for(Edge edge : outEdges) {
					if(!edge.isMerged()) {
						energy += edge.transmissionEnergy;
					}
				}
			}
			
			wuClass.energyCost = energy;
			
			return wuClass.getEnergyCost();
		}
	}

}