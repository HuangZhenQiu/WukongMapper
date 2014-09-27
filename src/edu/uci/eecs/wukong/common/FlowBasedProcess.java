package edu.uci.eecs.wukong.common;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.common.collect.ImmutableList;

import edu.uci.eecs.wukong.util.Util;


/**
 * 
 * 
 * @author Peter Huang
 *
 */
public class FlowBasedProcess {
	
	public static enum TYPE {LINEAR, STAR, RANDOM, SCALE_FREE};
	private List<FlowBasedProcessEdge> edges;
	
	private HashMap<Integer, WuClass> wuClassMap;
	
	//<WuClassId, Edges End at WuClassId>
	private HashMap<Integer, List<FlowBasedProcessEdge>> inEdgeMap;
	
	//<WuClassId, Edges begin from WuClassId>
	private HashMap<Integer, List<FlowBasedProcessEdge>> outEdgeMap;
	
	//WuClassId to LandMarkId distance constraint
	private HashMap<Integer, LocationConstraint> locationConstraints;
	
	
	
	private FlowBasedProcess.TYPE type;
	
	
	public FlowBasedProcess(HashMap<Integer, WuClass> wuClassMap, List<FlowBasedProcessEdge> edges, TYPE type) {
		this.type = type;
		this.edges = edges;
		this.wuClassMap = wuClassMap;
		this.inEdgeMap = new HashMap<Integer, List<FlowBasedProcessEdge>>();
		this.outEdgeMap = new HashMap<Integer, List<FlowBasedProcessEdge>>();
		this.locationConstraints = new HashMap<Integer, LocationConstraint>();
		this.setupMaps();
	}
	
	public FlowBasedProcess(TYPE type) {
		this.type = type;
		this.edges = new ArrayList<FlowBasedProcessEdge>();
		this.wuClassMap = new HashMap<Integer, WuClass>();
		this.inEdgeMap = new HashMap<Integer, List<FlowBasedProcessEdge>>();
		this.outEdgeMap = new HashMap<Integer, List<FlowBasedProcessEdge>>();
		this.locationConstraints = new HashMap<Integer, LocationConstraint>();
	}
	
	
	
	
	private void setupMaps() {
		
		for (FlowBasedProcessEdge edge: edges) {

			//Edge a -> b, put a->b into a's output edges
			List<FlowBasedProcessEdge> outEdges = outEdgeMap.get(edge.getInWuClass().getWuClassId());
			if (outEdges!=null) {
				outEdges.add(edge);
			} else {
				outEdges = new ArrayList<FlowBasedProcessEdge>();
				outEdges.add(edge);
				outEdgeMap.put(edge.getInWuClass().getWuClassId(), outEdges);
			}
			
			//Edge a -> b, put a->b into b's input edges
			List<FlowBasedProcessEdge> inEdges = inEdgeMap.get(edge.getOutWuClass().getWuClassId());
			if (inEdges!=null) {
				inEdges.add(edge);
			} else {
				inEdges = new ArrayList<FlowBasedProcessEdge>();
				inEdges.add(edge);
				inEdgeMap.put(edge.getOutWuClass().getWuClassId(), inEdges);
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
		for(FlowBasedProcessEdge edge : edges) {
			if(edge.isFullDeployed() && edge.getInWuClass().getDeviceId() == edge.getOutWuClass().getDeviceId())  {
				edge.merge();
			}
		}
	}
	
	public ImmutableList<FlowBasedProcessEdge> getMergedEdges() {
		ImmutableList.Builder<FlowBasedProcessEdge> builder = ImmutableList.<FlowBasedProcessEdge>builder();
		
		for(FlowBasedProcessEdge edge : edges) {
			if(edge.isMerged()) {
				builder.add(edge);
			}
		}
		return builder.build();
	}
	
	public ImmutableList<Integer> getPreDeployedWuClasses() {
		ImmutableList.Builder<Integer> builder = ImmutableList.<Integer>builder();
		for(FlowBasedProcessEdge edge : edges) {
			if(edge.isMerged()) {
				builder.add(edge.getInWuClass().getWuClassId());
				builder.add(edge.getOutWuClass().getWuClassId());
			}
		}
		
		return builder.build();
	}
	
	public ImmutableList<FlowBasedProcessEdge> getInEdge(Integer wuClassId) {
		List<FlowBasedProcessEdge> edges = this.inEdgeMap.get(wuClassId);
		
		if(edges != null) {
			return ImmutableList.<FlowBasedProcessEdge>builder().addAll(edges).build();
		} 
		
		return ImmutableList.<FlowBasedProcessEdge>builder().build();
	}
	
	public ImmutableList<FlowBasedProcessEdge> getOutEdge(Integer wuClassId) {
		List<FlowBasedProcessEdge> edges = this.outEdgeMap.get(wuClassId);
		
		if(edges != null) {
			return ImmutableList.<FlowBasedProcessEdge>builder().addAll(edges).build();
		} 
		
		return ImmutableList.<FlowBasedProcessEdge>builder().build();
	}
	
	public void reset() {
		
		Set<Integer> classIds= wuClassMap.keySet();
		for (Integer classId : classIds) {
			wuClassMap.get(classId).reset();
		}
		
		for (int i=0; i < edges.size(); i++) {
			edges.get(i).unmerge();
		}
	}
	
	public Double getNodeEnergyConsumption(Integer nodeId, List<Integer> deviceNeighbor) {
		
		Double energyConsumption = 0.0;
		List<FlowBasedProcessEdge> inEdges= inEdgeMap.get(nodeId);
		
		if (inEdges !=null) {
			for (FlowBasedProcessEdge inEdge : inEdges) {
				 if (!deviceNeighbor.contains(inEdge.getInWuClass().getWuClassId())) {
					 if(!inEdge.isMerged()) {
						 energyConsumption += inEdge.receivingEnergy;
					 } 
				 }
			}
		}
		
		
		List<FlowBasedProcessEdge> outEdges = outEdgeMap.get(nodeId);
		
		if (outEdges !=null) {
			for (FlowBasedProcessEdge outEdge : outEdges) {
				 if (!deviceNeighbor.contains(outEdge.getOutWuClass().getWuClassId())) {
					 
					 if(!outEdge.isMerged()) {
						 energyConsumption += outEdge.transmissionEnergy;
//						 energyConsumption += outEdge.dataVolumn;
					 }
				 }
			}
		}
		
		return energyConsumption;
	}
	
	public Double getNodeEnergyConsumptionChannel(Integer nodeId, List<Integer> deviceNeighbor, int[][] channels) {
		
		Double energyConsumption = 0.0;
		List<FlowBasedProcessEdge> inEdges= inEdgeMap.get(nodeId);
		
		if (inEdges !=null) {
			for (FlowBasedProcessEdge inEdge : inEdges) {
				 if (!deviceNeighbor.contains(inEdge.getInWuClass().getWuClassId())) {
					 
					 if(!inEdge.isMerged()) {
						 energyConsumption += channels[inEdge.getInWuClass().getDeviceId()][nodeId] * inEdge.dataVolumn;
					 } 
				 }
			}
		}
		
		
		List<FlowBasedProcessEdge> outEdges = outEdgeMap.get(nodeId);
		
		if (outEdges !=null) {
			for (FlowBasedProcessEdge outEdge : outEdges) {
				 if (!deviceNeighbor.contains(outEdge.getOutWuClass().getWuClassId())) {
					 
					 if(!outEdge.isMerged()) {
						 energyConsumption += channels[nodeId][outEdge.getOutWuClass().getDeviceId()] * outEdge.dataVolumn;
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
				System.out.println("wuclass " + wuclass.wuClassId + " undeployed");
				return false;
			}
		}
		
		return true;
	}
	
	public void print() {
		for(FlowBasedProcessEdge edge : edges) {
			System.out.println("Edge<" + edge.getInWuClass().getWuClassId() + ", "
					+ edge.getOutWuClass().getWuClassId() + ">  -->   Device<"
					+ edge.getInWuClass().deviceId +", " + edge.getOutWuClass().deviceId + ">" + ", data volumn " + edge.getDataVolumn());
		}
	}
	

	public ImmutableList<FlowBasedProcessEdge> getEdges() {
		return ImmutableList.<FlowBasedProcessEdge>builder().addAll(this.edges).build();
	}
	
	public Integer getEdgeNumber() {
		return edges == null ? 0 : edges.size();
	}	
	
	public LocationConstraint getLocationConstraintByWuClassId(Integer classId) {
		
		return locationConstraints.get(classId);
	}
	
	public Double getTotalEnergyConsumption() {
		Double total = 0.0;
		
		for(FlowBasedProcessEdge edge: getEdges()){
			if(!edge.isMerged()){
				total += edge.weight;
//				total += 2* edge.dataVolumn;
			}
		}
		return total;
	}
	
	public Double getTotalEnergyConsumption(int[][] channel) {
		double total = 0.0;
		
		for(FlowBasedProcessEdge edge: getEdges()){
			if(!edge.isMerged()){
				
				int src_device = edge.getInWuClass().getDeviceId();
				int dst_device = edge.getOutWuClass().getDeviceId();
				
				if(src_device != -1 && dst_device != -1) {
					total += channel[src_device-1] [dst_device-1] * edge.dataVolumn;
				}
						
//				total += edge.weight;
			}
		}
		return total;
	}
	
	public Double getWorstCaseEnergyConsumption(int max) { 
		Double total = getTotalEnergyConsumption();
		return max*total;
	}
	
	public Double getDistanceAwareTotalEnergyConsumption(WukongSystem system) {
		Double total = 0.0;
		for (FlowBasedProcessEdge edge: this.edges) {
			
			if(edge.getInWuClass().isDeployed() && edge.getOutWuClass().isDeployed()) {
				if(!edge.isMerged()) {
					total += Util.getTransmissionEnergyConsumption(edge.getDataVolumn(),
							system.getDistance(edge.getInWuClass().getDeviceId(), edge.getOutWuClass().getDeviceId()));
					total += Util.getReceivingEnergyConsumption(edge.getDataVolumn());
				}
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
			List<FlowBasedProcessEdge> inEdges = inEdgeMap.get(classId);
			if(inEdges != null) {
				for(FlowBasedProcessEdge edge : inEdges) {
					if(!edge.isMerged()) {
						energy += edge.receivingEnergy;
					}
				}
			}
			
			List<FlowBasedProcessEdge> outEdges = outEdgeMap.get(classId);
			if(outEdges != null) {
				for(FlowBasedProcessEdge edge : outEdges) {
					if(!edge.isMerged()) {
						energy += edge.transmissionEnergy;
					}
				}
			}
			
			wuClass.energyCost = energy;
			
			return wuClass.getEnergyCost();
		}
	}
	
	public WuClass getWuClass(Integer id) {
		return this.wuClassMap.get(id);
	}
	
	public ImmutableList<FlowBasedProcessEdge> getMergableEdges(WukongSystem system) 
	{
		ImmutableList.Builder<FlowBasedProcessEdge> builder = ImmutableList.<FlowBasedProcessEdge>builder();
		for(FlowBasedProcessEdge edge :this.edges) {
			if(system.isMergable(edge.getInWuClass(), edge.getOutWuClass())) {
				builder.add(edge);
			}
		}
		
		return builder.build();
		
	}
	
	public String toFileFormat(){
		String fileString = "";
		
		fileString += "#FBP \n";
		fileString += wuClassMap.size() + "\n";
		Iterator it = wuClassMap.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry pairs= (Map.Entry)it.next();
			int classId = (Integer) pairs.getKey();
			WuClass wuclass = (WuClass) pairs.getValue();
			
			String line = ""+ classId + " " + wuclass.getLocationConstraint().getLandMarkId() + " " + wuclass.getLocationConstraint().getDistance();
			fileString += line + "\n";
		}
		fileString += edges.size() + "\n";
		for(FlowBasedProcessEdge edge : edges){
			String line = "" + edge.getInWuClass().getWuClassId() + " " + edge.getOutWuClass().getWuClassId() + " " + edge.getDataVolumn();
			fileString += line + "\n";
		}
		
		return fileString;
		
	}
	public void toFile(String fileName) throws Exception{
		File file = new File(fileName);
		if ( !file.exists() ){
			file.createNewFile();
		}
		
		FileWriter writer = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw =  new BufferedWriter(writer);
		
		bw.write(this.toFileFormat());
		bw.close();
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
				Integer dataVolumn = Integer.parseInt(tokenizer.nextToken());
				FlowBasedProcessEdge edge = new FlowBasedProcessEdge(wuClassMap.get(inNode), wuClassMap.get(outNode), dataVolumn);
				edges.add(edge);
			}
			
		} catch(IOException e) {
			
			System.out.println("Error in initialize FBP from file");
			System.exit(-1);
		} 
		

		this.setupMaps();
	}
}
