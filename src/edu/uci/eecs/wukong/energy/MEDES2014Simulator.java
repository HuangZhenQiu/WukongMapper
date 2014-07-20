package edu.uci.eecs.wukong.energy;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;
import edu.uci.eecs.wukong.energy.common.CollocationGraphTest;
import edu.uci.eecs.wukong.energy.mapper.GreedyBasedMapper;
import edu.uci.eecs.wukong.energy.mapper.Mapper.MapType;
import edu.uci.eecs.wukong.energy.mapper.OptimalGreedyBasedMapper.GreedyType;
import edu.uci.eecs.wukong.energy.mapper.OptimalGreedyBasedMapper;
import edu.uci.eecs.wukong.util.FlowBasedProcessFactory;
import edu.uci.eecs.wukong.util.WuKongSystemFactory;

public class MEDES2014Simulator {
	private FlowBasedProcessFactory fbpFactory;

	private WuKongSystemFactory wukongFactory;

	
	private static int ITERATION = 1;
	public MEDES2014Simulator() {
		// WukongProperties.getProperty();
		this.fbpFactory = new FlowBasedProcessFactory(10 /* landmark number */, 20 /* class number */, 100 /* distance range */, 100 /* weight */);
		this.wukongFactory = new WuKongSystemFactory(20 /* class number */, 10 /* device number */, 10 /* landmark number */, 100 /* distance range */);
	}
	
	public double GMINSimulation(WukongSystem system, FlowBasedProcess fbp) {
		long start = System.currentTimeMillis();
		OptimalGreedyBasedMapper optimalGreedyBasedMapper = new OptimalGreedyBasedMapper(
				system, fbp, MapType.ONLY_LOCATION, GreedyType.GWMIN);
		optimalGreedyBasedMapper.map();
		long end = System.currentTimeMillis();
		return (end - start);
	}
	
	public double GMIN2Simulation(WukongSystem system, FlowBasedProcess fbp) {
		long start = System.currentTimeMillis();
		OptimalGreedyBasedMapper optimalGreedyBasedMapper = new OptimalGreedyBasedMapper(
				system, fbp, MapType.ONLY_LOCATION, GreedyType.GWMIN2);
		optimalGreedyBasedMapper.map();
		long end = System.currentTimeMillis();
		return (end - start);
	}
	
	public double GMAXSimulation(WukongSystem system, FlowBasedProcess fbp) {
		long start = System.currentTimeMillis();
		OptimalGreedyBasedMapper optimalGreedyBasedMapper = new OptimalGreedyBasedMapper(
				system, fbp, MapType.ONLY_LOCATION, GreedyType.GWMAX);
		optimalGreedyBasedMapper.map();
		long end = System.currentTimeMillis();
		return (end - start);
	}
	public double GreedySimulation(WukongSystem system, FlowBasedProcess fbp) {
		long start = System.currentTimeMillis();		
		GreedyBasedMapper greedyMapper = new GreedyBasedMapper(system, fbp, MapType.ONLY_LOCATION);
		greedyMapper.map();
		long end = System.currentTimeMillis();
		return (end - start);
	}
	
	public void run() {
		
		double GreedyExecutionTime = 0;
		double GminExecutionTime = 0;
		double Gmin2ExecutionTime = 0;
		double GmaxExecutionTime = 0;

		double GminSavingRatio = 0;
		double Gmin2SavingRatio = 0;
		double GmaxSavingRatio = 0;
		double GreedySavingRatio = 0;
		
		double GminLargestRatio = 0;
		double Gmin2LargestRatio = 0;
		double GmaxLargestRatio = 0;
		double GreedyLargestRatio = 0;
		
		for (int i = 0; i < ITERATION; i++) {
			
			/* 
			 * GMIN test
			 */
			FlowBasedProcess fbp = fbpFactory.createFlowBasedProcess(TYPE.RANDOM);
			WukongSystem system = wukongFactory.createRandomWuKongSystem();
			
			fbp.print();
			System.out.println(system.toString());
			double initialTotal = fbp.getTotalEnergyConsumption();
			System.out.println("Gmin test: " + i);
			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
			
			GminExecutionTime += GMINSimulation(system, fbp);
			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
			
			fbp.print();
			System.out.println(system.toString());
			System.out.println("");
			
			double gminTotal = system.getTotalEnergyConsumption();
			double gminLargest = system.getLargestDeviceEnergtConsumption();
			GminSavingRatio += gminTotal / initialTotal;
			
			
			/* 
			 * GMIN2 test
			 */
			
			fbp.reset();
			system.reset();
			
			System.out.println("Gmin2 test: " + i);
			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
			
			Gmin2ExecutionTime += GMIN2Simulation(system, fbp);
			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
			System.out.println("");
			
			double gmin2Total = system.getTotalEnergyConsumption();
			double gmin2Largest = system.getLargestDeviceEnergtConsumption();
			Gmin2SavingRatio += gmin2Total / initialTotal;
			
			/* 
			 * GMAX test
			 */
			
			fbp.reset();
			system.reset();
			System.out.println("Gmax test: " + i);
			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
			
			GmaxExecutionTime += GMAXSimulation(system, fbp);
			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
			System.out.println("");
			double gmaxTotal = system.getTotalEnergyConsumption();
			double gmaxLargest = system.getLargestDeviceEnergtConsumption();
			
			GmaxSavingRatio += gmaxTotal / initialTotal;
			
			/* 
			 * Greedy test
			 */
			
			fbp.reset();
			system.reset();
			
			System.out.println("Greedy test: " + i);
			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
			
			GreedyExecutionTime += GreedySimulation(system, fbp);
			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
			System.out.println("");
			
			double greedyTotal = system.getTotalEnergyConsumption();
			double greedyLargest = system.getLargestDeviceEnergtConsumption();
			GreedySavingRatio += greedyTotal / initialTotal;
			
			double largest = Math.max(Math.max(greedyLargest, Math.max(gmaxLargest, gmin2Largest)), gminLargest);
			GminLargestRatio += gminLargest / largest;
			Gmin2LargestRatio += gmin2Largest / largest;
			GmaxLargestRatio += gmaxLargest / largest;
			GreedyLargestRatio += greedyLargest / largest;
			
		}

		
		System.out.println("SRatio " + GminSavingRatio / ITERATION + ", " + Gmin2SavingRatio / ITERATION + ", "+GmaxSavingRatio / ITERATION +", " + GreedySavingRatio /ITERATION);
		System.out.println("LRatio " + GminLargestRatio / ITERATION + ", " + Gmin2LargestRatio / ITERATION + ", "+GmaxLargestRatio / ITERATION +", " + GreedyLargestRatio /ITERATION);
		
		System.out.println("Average Execution Time for Gmin method:" + GminExecutionTime / ITERATION);
		System.out.println("Average Execution Time for Gmin2 method:" + Gmin2ExecutionTime / ITERATION);
		System.out.println("Average Execution Time for Gmax method:" + GmaxExecutionTime / ITERATION);
		System.out.println("Average Execution Time for Greedy method:" + GreedyExecutionTime / ITERATION);
		
		CollocationGraphTest test = new CollocationGraphTest();
		test.run();
		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MEDES2014Simulator simulator = new MEDES2014Simulator();
		simulator.run();
	}
}
