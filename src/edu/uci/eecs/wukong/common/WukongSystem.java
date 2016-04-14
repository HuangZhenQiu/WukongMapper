package edu.uci.eecs.wukong.common;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Iterator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.uci.eecs.wukong.colocation.ColocationGraphNode;
import edu.uci.eecs.wukong.common.FlowBasedProcessEdge;
import edu.uci.eecs.wukong.common.ShortestNetworkPath;

/**
 * 
 * 
 * @author Peter Huang
 * 
 *         TODO: Split out the binding between system and fbp
 *         TODO: Support deploy multiple FBP into WukongSystem
 */
public class WukongSystem {

	private List<WuDevice> devices;
	private List<Region> regions;
	private List<Gateway> gateways;
	private ShortestNetworkPath shortestNetworkPath;
	private int[][] channels;

	public void setChannel(int[][] channel) {
		this.channels = channel;
	}

	public int[][] getChannel() {
		return this.channels;
	}

	private HashMap<Integer, WuDevice> deviceMap;
	private HashMap<Integer, List<WuDevice>> wuClassDeviceMap;
	private FlowBasedProcess currentFBP;
	private Integer deviceNumber;
	private Integer wuClassNumber;
	private Integer landmarkNumber;
	private boolean isDistanceAware;

	public WukongSystem(boolean isDistanceAware) {
		this.devices = new ArrayList<WuDevice>();
		this.regions = new ArrayList<Region>();
		this.gateways = new ArrayList<Gateway>();
		this.deviceMap = new HashMap<Integer, WuDevice>();
		this.wuClassDeviceMap = new HashMap<Integer, List<WuDevice>>();
		this.isDistanceAware = isDistanceAware;
	}
	
	public WukongSystem(List<WuDevice> devices, List<Region> regions, List<Gateway> gateways,
			int wuClassNumber, int landmarkNumber, boolean isDistanceAware) {
		this(devices, wuClassNumber, landmarkNumber, isDistanceAware);
		this.regions = regions;
		this.gateways = gateways;
	}

	public WukongSystem(List<WuDevice> devices, int wuClassNumber, int landmarkNumber, boolean isDistanceAware) {
		this.wuClassNumber = wuClassNumber;
		this.landmarkNumber = landmarkNumber;
		this.devices = devices;
		if (isDistanceAware) {
			this.shortestNetworkPath = new ShortestNetworkPath(new Double[this.devices.size()][this.devices.size()], false);
		}
		this.deviceMap = new HashMap<Integer, WuDevice>();
		this.wuClassDeviceMap = new HashMap<Integer, List<WuDevice>>();
		this.isDistanceAware = isDistanceAware;
		initializeMap(this.devices);
	}

	public void initialize(List<WuDevice> devices, List<Region> regions, List<Gateway> gateways,
			Double[][] distances, boolean multipleHop, int wuClassNumber, int landmarkNumber) {
		this.wuClassNumber = wuClassNumber;
		this.landmarkNumber = landmarkNumber;
		this.devices.addAll(devices);
		this.regions.addAll(regions);
		this.gateways.addAll(gateways);
		this.deviceNumber = this.devices.size();
		if (this.isDistanceAware) {
			this.shortestNetworkPath = new ShortestNetworkPath(distances, multipleHop);
		}
		initializeMap(this.devices);
	}

	private void initializeMap(List<WuDevice> devices) {
		Iterator<WuDevice> iterator = devices.iterator();
		while (iterator.hasNext()) {
			WuDevice device = iterator.next();
			deviceMap.put(device.getWuDeviceId(), device);
			ImmutableList<Integer> classes = device.getAllWuObjectClassId();
			for (int i = 0; i < classes.size(); i++) {
				if (wuClassDeviceMap.containsKey(classes.get(i))) {
					wuClassDeviceMap.get(classes.get(i)).add(device);
				} else {
					List<WuDevice> devList = new ArrayList<WuDevice>();
					devList.add(device);
					wuClassDeviceMap.put(classes.get(i), devList);
				}
			}

		}
	}
	
	/**
	 * Given a FlowBasedProcess, find out all of the distinguish congestion zones
	 * that each of them will be mapping target.
	 * 
	 * @param process
	 * @return
	 */
	public List<CongestionZone> getCongestionZones(FlowBasedProcess process) {
		List<CongestionZone> zones = new ArrayList<CongestionZone> ();
		Queue<CongestionZone> zoneQueue = new ArrayDeque<CongestionZone>();
		for (Region region : regions) {
			zoneQueue.add(region.createCongestionZone(process));
		}

		List<CongestionZone> removedZone = new ArrayList<CongestionZone> ();
		while(!zoneQueue.isEmpty()) {
			CongestionZone zone = zoneQueue.poll();
			Iterator<CongestionZone> zoneIter = zoneQueue.iterator();
			removedZone.clear();
			while(zoneIter.hasNext()) {
				CongestionZone newZone = zoneIter.next();
				if(zone!= newZone && zone.isCongestable(newZone)) {
					zone.join(newZone);
					removedZone.add(newZone);
				}
			}
			
			if (zoneQueue.isEmpty()) {
				zones.add(zone);
				return zones;
			}
			
			if (removedZone.isEmpty()) {
				// Add the stand alone zone into list as final result.
				zones.add(zone);
			} else {
				// Add the merged zone into queue
				zoneQueue.add(zone);
				for (CongestionZone removed : removedZone) {
					zoneQueue.remove(removed);
				}
			}
		}
		
		return zones;
	}
	
	
	public WuDevice getDevice(int wudeviceId) { 
		return deviceMap.get(wudeviceId);
	}
	
	public void deployComponent(int wuDeviceId, int wuClassId) {
		deviceMap.get(wuDeviceId).deployComponent(wuClassId);
	}

	// read data from file
	public void initialize(BufferedReader input) {

		String content = "";

		try {

			// break the comments
			while ((content = input.readLine()) != null) {

				if (content.length() > 0 && !content.startsWith("#")) {
					break;
				}
			}

			// begin initialization
			StringTokenizer tokenizer = new StringTokenizer(content);
			this.wuClassNumber = Integer.parseInt(tokenizer.nextToken());
			this.deviceNumber = Integer.parseInt(tokenizer.nextToken());
			this.landmarkNumber = Integer.parseInt(tokenizer.nextToken());

			// initialize the distance matrix;
			Double[][] distances = new Double[deviceNumber][deviceNumber];
			for (int i = 0; i < deviceNumber; i++) {
				initializeWuDevice(input, distances);
			}
			this.shortestNetworkPath = new ShortestNetworkPath(distances, false);

		} catch (IOException e) {
			System.out.println("Wukong System initialization error!");
			System.exit(-1);
		}
	}

	private WuDevice initializeWuDevice(BufferedReader input, Double[][] distances)
			throws IOException {
		String content = input.readLine();
		StringTokenizer tokenizer = new StringTokenizer(content);
		Integer deviceId = Integer.parseInt(tokenizer.nextToken());
		Double energyConstraint = Double.parseDouble(tokenizer.nextToken());
		WuDevice device = new WuDevice(deviceId, energyConstraint, this);
		device.initialize(input);
		devices.add(device);
		deviceMap.put(deviceId, device);

		List<Integer> classIds = device.getAllWuObjectClassId();
		for (Integer id : classIds) {
			List<WuDevice> devices = wuClassDeviceMap.get(id);
			if (devices == null) {
				devices = new ArrayList<WuDevice>();
			}
			devices.add(device);
			wuClassDeviceMap.put(id, devices);
		}

		int id = device.getWuDeviceId() - 1;
		for (int i = 0; i < this.deviceNumber; i++) {
			distances[id][i] = device.getDeviceDistances().get(i);
		}

		return device;
	}
	
	public int getMaxReprogramGateway() {
		int max = 0;
		for (Gateway gateway : gateways) {
			System.out.println("Gateway g " + gateway.getGatewayId() + " has " + gateway.getDeviceNumber() + " reprogam " + gateway.reprogramDeviceNumber() + " devices");
			if (gateway.reprogramDeviceNumber() > max) {
				max = gateway.reprogramDeviceNumber();
			}
		}
		
		return max;
	}

	/**
	 * 
	 * @param source
	 * @param dest
	 * @return
	 */

	public int getDeviceChannel(WuDevice source, WuDevice dest) {
		return channels[source.getWuDeviceId() - 1][dest.getWuDeviceId() - 1];
	}

	public Double getDistance(WuDevice source, WuDevice dest) {
		return shortestNetworkPath.getShortestDistance(source.getWuDeviceId() - 1, dest.getWuDeviceId() - 1);
	}

	public Double getDistance(int source, int dest) {
		if ((1 <= source && source <= this.deviceNumber.intValue())
				&& (1 <= dest && dest <= this.deviceNumber)) {
			return shortestNetworkPath.getShortestDistance(source - 1, dest - 1); // Ids start from 1
		}

		return 0.0;
	}
	
	public List<Double> getDistanceOnShortestPath(int source, int dest) {
		return shortestNetworkPath.getDistanceOnShortestPath(source - 1, dest - 1); 
	}

	public ImmutableList<WuDevice> findWudevice(int wuClassId) {
		ImmutableList.Builder<WuDevice> builder = ImmutableList
				.<WuDevice> builder();
		for (WuDevice device : this.devices) {
			if (device.isWuObjectExist(wuClassId)) {
				builder.add(device);
			}
		}

		return builder.build();
	}

	public void reset() {
		this.currentFBP = null;
		for (WuDevice device : devices) {
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
		for (WuDevice device : devices) {
			if (device.getCurrentConsumption() > cost) {
				cost = device.getCurrentConsumption();
			}
		}
		return cost;
	}

	public Double getTotalEnergyConsumption() {
		Double cost = 0.0;
		for (WuDevice device : devices) {
			cost += device.getCurrentConsumption();
		}

		return cost;
	}

	public WuDevice getWuDevice(int deviceId) {
		for (WuDevice device : devices) {
			if (device.getWuDeviceId() == deviceId) {
				return device;
			}
		}
		return null;
	}

	public ImmutableList<WuDevice> getDevices() {

		return ImmutableList.<WuDevice> builder().addAll(this.devices).build();
	}

	public ImmutableMap<Integer, List<WuDevice>> getWuClassDeviceMap() {
		return ImmutableMap.<Integer, List<WuDevice>> builder()
				.putAll(this.wuClassDeviceMap).build();
	}

	public ImmutableList<WuDevice> getPossibleHostDevice(Integer wuClassId) {
		return ImmutableList.<WuDevice> builder()
				.addAll(wuClassDeviceMap.get(wuClassId)).build();
	}

	public int getDeviceNumber() {

		return devices == null ? 0 : devices.size();
	}

	public int getLandmarkNumber() {

		return this.landmarkNumber;
	}

	public int getWuClassNunber() {

		return this.wuClassNumber;
	}

	public boolean isMergable(WuClass source, WuClass dest) {
		Set<Integer> done = new HashSet<Integer>();
		done.add(source.getWuClassId());
		done.add(dest.getWuClassId());

		return isHostable(done);
	}

	public boolean isHostable(Set<Integer> done) {
		for (WuDevice device : devices) {
			Set<Integer> wuobjects = new HashSet<Integer>();
			for (Integer integer : device.getAllWuObjectClassId()) {
				wuobjects.add(integer);
			}

			if (wuobjects.containsAll(done)) {
				return true;
			}
		}
		return false;
	}

	public WuDevice getHostableDevice(Set<Integer> done) {
		PriorityQueue<WuDevice> deviceQueue = getPrioritizedDeviceQueue();
		while (deviceQueue.size() > 0) {
			WuDevice device = deviceQueue.poll();

			if (device.isHostable(done)) {
				return device;
			}
		}
		return null;
	}

	public boolean isHostable(ColocationGraphNode node) {
		PriorityQueue<WuDevice> deviceQueue = getPrioritizedDeviceQueue();
		while (deviceQueue.size() > 0) {
			WuDevice device = deviceQueue.poll();

			if (device.isHostable(node.getInvolveWuClasses())) {
				return true;
			}
		}
		return false;
	}

	public boolean merge(FlowBasedProcess fbp) {
		List<FlowBasedProcessEdge> temporaryList = new ArrayList<FlowBasedProcessEdge>();
		PriorityQueue<WuDevice> deviceQueue = getPrioritizedDeviceQueue();

		return merge(fbp, temporaryList, deviceQueue);
	}

	private boolean merge(FlowBasedProcess fbp,
			List<FlowBasedProcessEdge> temporaryList,
			PriorityQueue<WuDevice> deviceQueue) {

		if (fbp == null) {
			return false;
		}

		this.currentFBP = fbp;

		PriorityQueue<FlowBasedProcessEdge> edgeQueue = new PriorityQueue<FlowBasedProcessEdge>();
		edgeQueue.addAll(fbp.getEdges());

		FlowBasedProcessEdge currentEdge;
		while (edgeQueue.size() > 0) {
			currentEdge = edgeQueue.poll();

			// if both two end is undeployed
			if (currentEdge.isUndeployed()) {

				Iterator<WuDevice> itr = deviceQueue.iterator();
				while (itr.hasNext()) {

					WuDevice currentDevice = itr.next();
					if (currentDevice.deploy(currentEdge.getInWuClass()
							.getWuClassId(), currentEdge.getOutWuClass()
							.getWuClassId())) {
						currentEdge.getInWuClass().deploy(
								currentDevice.getWuDeviceId());
						currentEdge.getOutWuClass().deploy(
								currentDevice.getWuDeviceId());
						currentEdge.merge();
						deviceQueue.remove(currentDevice);
						deviceQueue.add(currentDevice);// reorder the priority
														// queue.
						break;
					}
				}

				if (!currentEdge.isFullDeployed()) {
					temporaryList.add(currentEdge);
				}

			} else if (currentEdge.isPartialDeployed()) {

				Integer undeployedClassId = currentEdge.getUndeployedClassId();
				Integer deviceId = currentEdge.getPartiallyDeployedDeviceId();
				WuDevice device = deviceMap.get(deviceId);

				if (device != null) {

					if (device.deploy(undeployedClassId)) {
						currentEdge.getInWuClass().deploy(
								device.getWuDeviceId());
						currentEdge.getOutWuClass().deploy(
								device.getWuDeviceId());
						currentEdge.merge();
						deviceQueue.remove(device);
						deviceQueue.add(device);
					} else {
						temporaryList.add(currentEdge);
					}

				} else {
					// System.exit(-1);
				}

			} else { // two ends of the edge are deployed.
				continue;
			}

		}

		updateEnergyConsumption();

		return true;
	}

	private void updateEnergyConsumption() {

		for (WuDevice device : devices) {
			device.updateEnergyConsumption();
		}
	}

	public boolean deploy(Integer deviceId, Integer wuClassId) {
		WuDevice device = getWuDevice(deviceId);
		// WuDevice device = this.deviceMap.get(deviceId);
		if (device == null) {
			System.out.println("Error Device Id: " + deviceId);
			return false;
		}
		return device.deploy(wuClassId);
	}

	/*
	 * 
	 * This function address deploying all remaining nodes onto
	 */
	public boolean deployWithNoMerge(FlowBasedProcess fbp,
			ArrayList<FlowBasedProcessEdge> temporaryList) {
		PriorityQueue<WuDevice> deviceQueue = getPrioritizedDeviceQueue();

		for (FlowBasedProcessEdge edge : temporaryList) {
			// System.out.println("Checking edge: "+edge.getInWuClass().wuClassId
			// + ", " + edge.getOutWuClass().wuClassId);

			if (edge.isUndeployed()) {
				deployOneEnd(edge, edge.getInWuClass().getWuClassId());
				deployOneEnd(edge, edge.getOutWuClass().getWuClassId());
				// deployOneEnd(edge, edge.getInWuClass().getWuClassId(),
				// deviceQueue);
				// deployOneEnd(edge, edge.getOutWuClass().getWuClassId(),
				// deviceQueue);
			}

			if (edge.isPartialDeployed()) {
				Integer undeployedClassId = edge.getUndeployedClassId();

				// deployOneEnd(edge, undeployedClassId, deviceQueue);
				deployOneEnd(edge, undeployedClassId);
			}

			if (!edge.isFullDeployed()) {
				System.out.println("Edge <"
						+ edge.getInWuClass().getWuClassId() + ", "
						+ edge.getOutWuClass().getWuClassId()
						+ "> is undepoyable.");
				// return false;
			}
		}
		return true;
	}

	public PriorityQueue<WuDevice> getPrioritizedDeviceQueue() {
		PriorityQueue<WuDevice> deviceQueue = new PriorityQueue<WuDevice>();
		deviceQueue.addAll(devices);
		return deviceQueue;
	}

	public boolean deploy(FlowBasedProcess fbp) {

		List<FlowBasedProcessEdge> temporaryList = new ArrayList<FlowBasedProcessEdge>();

		PriorityQueue<WuDevice> deviceQueue = getPrioritizedDeviceQueue();
		if (!merge(fbp, temporaryList, deviceQueue)) {

			return false;
		}

		for (FlowBasedProcessEdge edge : temporaryList) {
			
			if (edge.isPartialDeployed()) {
				Integer undeployedClassId = edge.getUndeployedClassId();
				 deployOneEnd(edge, undeployedClassId, deviceQueue);
//				deployOneEnd(edge, undeployedClassId);
			}
			if (edge.isUndeployed()) {
//				deployOneEnd(edge, edge.getInWuClass().getWuClassId());
//				deployOneEnd(edge, edge.getOutWuClass().getWuClassId());

				 deployOneEnd(edge, edge.getInWuClass().getWuClassId(),
				 deviceQueue);
				 deployOneEnd(edge, edge.getOutWuClass().getWuClassId(),
				 deviceQueue);
			}

			if (!edge.isFullDeployed()) {
				System.out.println("Edge <"
						+ edge.getInWuClass().getWuClassId() + ", "
						+ edge.getOutWuClass().getWuClassId()
						+ "> is undepoyable.");
				return false;
			}
		}

		return true;
	}

	private boolean deployOneEnd(FlowBasedProcessEdge edge, int wuclassId, PriorityQueue<WuDevice> deviceQueue) {
		Iterator<WuDevice> itr = deviceQueue.iterator();
		// System.out.println("finding wuclass for " + wuclassId);
		while (itr.hasNext()) {

			WuDevice currentDevice = itr.next();
			// System.out.println(currentDevice);
			if (currentDevice.deploy(wuclassId)) {

				if (edge.getInWuClass().getWuClassId() == wuclassId) {
					edge.getInWuClass().deploy(currentDevice.getWuDeviceId());
				} else {
					edge.getOutWuClass().deploy(currentDevice.getWuDeviceId());
				}
				deviceQueue.remove(currentDevice);
				deviceQueue.add(currentDevice);// reorder the priority queue.
				return true;
			}
		}

		return false;
	}

	private boolean deployOneEnd(FlowBasedProcessEdge edge, int wuclassId) {

		for (WuDevice device : devices) {
			if (device.deploy(wuclassId)) {
				// System.out.println("Deploying "+ wuclassId + " to " +
				// device.getWuDeviceId());
				if (edge.getInWuClass().getWuClassId() == wuclassId) {
					edge.getInWuClass().deploy(device.getWuDeviceId());
				} else {
					edge.getOutWuClass().deploy(device.getWuDeviceId());
				}
				return true;
			}
		}
		return false;
	}

	public String toFileFormat() {

		String fileString = "";

		fileString += "#Wukong System  #(Number of WuClass) #(Number of WuDevice)  #(Number of Landmark) \n";
		fileString += wuClassNumber + " " + deviceNumber + " " + landmarkNumber
				+ "\n";

		for (WuDevice device : devices) {
			String line = device.getWuDeviceId() + " "
					+ device.getEnergyConstraint() + "\n";
			for (Integer object : device.getAllWuObjectClassId()) {
				line += object + " ";
			}
			line += "\n";
			for (Double distance : device.getLandmarkDistances()) {
				line += distance + " ";
			}
			line += "\n";
			for (Double device_distance : device.getDeviceDistances()) {
				line += device_distance + " ";
			}
			line += "\n";
			fileString += line;
		}

		return fileString;
	}

	public void toFile(String fileName) throws Exception {
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter writer = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(writer);

		bw.write(this.toFileFormat());
		bw.close();
	}

	public String toString() {
		String str = "";
		for (WuDevice device : devices) {
			str = str + device.toString() + "\n";
		}

		str = str + "Total Energy Consumption is:"
				+ this.getTotalEnergyConsumption();
		return str;
	}

	public String LouisToString() {

		String str = toString() + "\n";

		for (int i = 0; i < deviceNumber; i++) {
			for (int j = 0; j < deviceNumber; j++) {
				str = str + channels[i][j] + " ";
			}
			str = str + "\n";
		}
		return str;
	}
}
