package edu.uci.eecs.wukong.energy;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.energy.mapper.DistanceUnawareSelectionBasedMapper;
import edu.uci.eecs.wukong.energy.mapper.DistanceAwareSelectionBasedMapper;
import edu.uci.eecs.wukong.energy.mapper.HybridMapper;
import edu.uci.eecs.wukong.mapper.Mapper.MapType;
import edu.uci.eecs.wukong.energy.mapper.GreedyBasedMapper;
import edu.uci.eecs.wukong.util.FlowBasedProcessFactory;
import edu.uci.eecs.wukong.util.WuKongSystemFactory;
import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;

public class Ithing2014Simulator {

	private FlowBasedProcessFactory fbpFactory;
	
	private WuKongSystemFactory wukongFactory;
	
	public Ithing2014Simulator() {
		//WukongProperties.getProperty();
		this.fbpFactory = new FlowBasedProcessFactory(10, 20, 100 /**distance range**/, 100 /**weight**/);
		this.wukongFactory = new WuKongSystemFactory(20, 20, 10, 100, 0, 0, true);
	}
	
	public void run() {
		
		double greedyTotalRatio = 0.0;
		double hybridTotalRatio = 0.0;
		double selectionTotalRatio = 0.0;
		double distanceSelectionTotalRatio = 0.0;
		
		double greedyLargestRatio = 0.0 ;
		double hybridLargestRatio = 0.0;
		double selectionLargestRatio = 0.0;
		double distanceSelectionLargestRatio = 0.0;
		
		long hybridExecutionTime = 0;
		
		for (int i = 0; i < 100; i ++) {
		
			FlowBasedProcess fbp = fbpFactory.createFlowBasedProcess(TYPE.LINEAR);
			WukongSystem system = wukongFactory.createRandomWuKongSystem();
			System.out.println("FBP orignial energy consumption: " + fbp.getTotalEnergyConsumption());
			double initialTotal = fbp.getTotalEnergyConsumption();
			
			GreedyBasedMapper greedyMapper = new GreedyBasedMapper(system, fbp, MapType.ONLY_LOCATION);
			if(!greedyMapper.map()) {
				continue;
			}
			
			System.out.println("After Greedy Map!");
			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
			double greedyTotal = system.getTotalEnergyConsumption();
			double greedyLargest = system.getLargestDeviceEnergtConsumption();
			greedyTotalRatio += greedyTotal/initialTotal;
			
			fbp.reset();
			system.reset();
			
			//System.out.println(fbp.getTotalEnergyConsumption());
			//System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
			long start = System.currentTimeMillis();
			HybridMapper hybridMapper = new HybridMapper(system, fbp, MapType.ONLY_LOCATION, 20000);
			hybridMapper.map();
			long end = System.currentTimeMillis();
			hybridExecutionTime += end - start;
			
			System.out.println("After Hybrid Map!");
			//System.out.println(fbp.getTotalEnergyConsumption());
			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
			double hybridTotal = system.getTotalEnergyConsumption();
			double hybridLargest = system.getLargestDeviceEnergtConsumption();
			hybridTotalRatio += hybridTotal/initialTotal;
			
			fbp.reset();
			system.reset();
			//System.out.println("After reset!");
			//System.out.println(fbp.getTotalEnergyConsumption());
			//System.out.println(system.getTotalEnergyConsumption());
			DistanceUnawareSelectionBasedMapper mapper = new DistanceUnawareSelectionBasedMapper(system, fbp, MapType.ONLY_LOCATION, 20000);
			mapper.map();
			System.out.println("After Selection Map!");
			//System.out.println(fbp.getTotalEnergyConsumption());
			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
			//System.out.println(system.toString());
			
			double selectionTotal = system.getTotalEnergyConsumption();
			double selectionLargest = system.getLargestDeviceEnergtConsumption();
			selectionTotalRatio += selectionTotal/initialTotal;
			
			fbp.reset();
			system.reset();
			
			DistanceAwareSelectionBasedMapper distanceMapper = new DistanceAwareSelectionBasedMapper(system, fbp, MapType.ONLY_LOCATION, 20000, false);
			distanceMapper.map();
			
			double distanceSelectionTotal = fbp.getDistanceAwareTotalEnergyConsumption(system);
			double distanceSelectionLargest = distanceMapper.getLagestDeviceEnergyConsumption();
			distanceSelectionTotalRatio += distanceSelectionTotal/initialTotal;
			
			System.out.println("After Distance Selection Map!");
			//System.out.println(fbp.getTotalEnergyConsumption());
			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
			
		
			double largest = Math.max(Math.max(greedyLargest, Math.max(hybridLargest, selectionLargest)), distanceSelectionLargest);
			greedyLargestRatio +=  greedyLargest/largest;
			hybridLargestRatio += hybridLargest/largest;
			selectionLargestRatio += selectionLargest/largest;
			distanceSelectionLargestRatio += distanceSelectionLargest/largest;
		}
		
		System.out.println(greedyTotalRatio/ 100 + "     "+ hybridTotalRatio/ 100 + "     "+ selectionTotalRatio / 100 + "       " + distanceSelectionTotalRatio / 100);
		System.out.println(greedyLargestRatio/ 100 + "     "+ hybridLargestRatio/ 100 + "     "+ selectionLargestRatio / 100 + "     " + distanceSelectionLargestRatio/100);
		
		//System.out.println(hybridTotalRatio/ 100);
		//System.out.println(hybridLargestRatio/ 100);
		System.out.println("Average Execution Time:" + hybridExecutionTime);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Ithing2014Simulator simulator = new Ithing2014Simulator();
		simulator.run();
	}

}
