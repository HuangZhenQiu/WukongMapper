package edu.uci.eecs.wukong.energy;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;
import edu.uci.eecs.wukong.energy.mapper.GreedyBasedMapper;
import edu.uci.eecs.wukong.energy.mapper.Mapper.MapType;
import edu.uci.eecs.wukong.energy.mapper.GreedyBasedMapper;
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
		
	}
	
	public double GMINFrameworkSimulation(WukongSystem system, FlowBasedProcess fbp, GreedyType type) { 
		long start = System.currentTimeMillis();
		OptimalGreedyBasedMapper optimalGreedyBasedMapper = new OptimalGreedyBasedMapper(system, fbp, MapType.ONLY_LOCATION, type);
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
	
	public double ReduceGreedySimulation(WukongSystem system, FlowBasedProcess fbp) { 
		long start = System.currentTimeMillis();
		OptimalGreedyBasedMapper optimalGreedyBasedMapper = new OptimalGreedyBasedMapper(system, fbp, MapType.ONLY_LOCATION, GreedyType.LIKE_NAIVE);
		optimalGreedyBasedMapper.map();
		long end = System.currentTimeMillis();
		return (end - start);
	}
	
	
	
	
	public void run(int iteration, int deviceNumber, int classNumber, int transmissionWeight, TYPE fbpType, int K) {
		
		this.fbpFactory = new FlowBasedProcessFactory(10 /* landmark number */, classNumber /* class number */, 100 /* distance range */, transmissionWeight /* weight */);
		this.wukongFactory = new WuKongSystemFactory(classNumber /* class number */, deviceNumber /* device number */, 10 /* landmark number */, 100 /* distance range */);
		
		double GreedyExecutionTime = 0;
		double GminExecutionTime = 0;
		double Gmin2ExecutionTime = 0;
		double maximumExecutionTime = 0;
		double maximumImportanceExecutionTime = 0;
		double GmaxExecutionTime = 0;
		double likeNaiveExecutionTime = 0;

		double GreedySavingRatio = 0;
		double GminSavingRatio = 0;
		double Gmin2SavingRatio = 0;
		double maximumSavingRatio = 0;
		double maximumImportanceSavingRatio = 0;
		double GmaxSavingRatio = 0;
		double likeNaiveSavingRatio = 0;
		
		double GreedyLargestRatio = 0;
		double GminLargestRatio = 0;
		double Gmin2LargestRatio = 0;
		double maximumLargestRatio = 0;
		double maximumImportanceLargestRatio = 0;
		double GmaxLargestRatio = 0;
		double likeNaiveLargestRatio = 0;
		
		
		for (int i = 0; i < iteration; i++) {
			
			/* 
			 * GMIN test
			 */
			FlowBasedProcess fbp = fbpFactory.createFlowBasedProcess(fbpType);
//			WukongSystem system = wukongFactory.createRandomWuKongSystem();
			WukongSystem system = wukongFactory.createRandomWukongSystem(K, 1);
//			fbp.print();
//			System.out.println(system.toString());
			double initialTotal = fbp.getTotalEnergyConsumption();
//			System.out.println("Gmin test: " + i);
//			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
//			
			GminExecutionTime += GMINFrameworkSimulation(system, fbp, GreedyType.GWMIN);
//			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
//			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
//			System.out.println("FBP Total Energy Consumption: " + fbp.getTotalEnergyConsumption());
			
//			fbp.print();
//			System.out.println(system.toString());
//			System.out.println("");
			
			double gminTotal = fbp.getTotalEnergyConsumption();
			double gminLargest = system.getLargestDeviceEnergtConsumption();
			GminSavingRatio += gminTotal / initialTotal;
			
			
			/* 
			 * GMIN2 test
			 */
			
			fbp.reset();
			system.reset();
			
//			System.out.println("Gmin2 test: " + i);
//			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
			
			Gmin2ExecutionTime += GMINFrameworkSimulation(system, fbp, GreedyType.GWMIN2);
//			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
//			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
//			System.out.println("");
			
			double gmin2Total = fbp.getTotalEnergyConsumption();
			double gmin2Largest = system.getLargestDeviceEnergtConsumption();
			Gmin2SavingRatio += gmin2Total / initialTotal;
			
			/* 
			 * maximum test
			 */
			
			fbp.reset();
			system.reset();
			
//			System.out.println("Maximum test: " + i);
//			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
//			
			maximumExecutionTime += GMINFrameworkSimulation(system, fbp, GreedyType.MAXIMUM);
//			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
//			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
//			System.out.println("");
			
			double maximumTotal = fbp.getTotalEnergyConsumption();
			double maximumLargest = system.getLargestDeviceEnergtConsumption();
			maximumSavingRatio += maximumTotal / initialTotal;
			
			/* 
			 * maximum importance test
			 */
			
			fbp.reset();
			system.reset();
			
//			System.out.println("Maximum importance test: " + i);
//			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
			
			maximumImportanceExecutionTime += GMINFrameworkSimulation(system, fbp, GreedyType.MAX_IMPORTANCE);
//			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
//			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
//			System.out.println("");
			
			double maximumImportanceTotal = fbp.getTotalEnergyConsumption();
			double maximumImportanceLargest = system.getLargestDeviceEnergtConsumption();
			maximumImportanceSavingRatio += maximumImportanceTotal / initialTotal;
			
			/* 
			 * GMAX test
			 */
			
			fbp.reset();
			system.reset();
//			System.out.println("Gmax test: " + i);
//			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
//			
			GmaxExecutionTime += GMAXSimulation(system, fbp);
//			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
//			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
//			System.out.println("");
			double gmaxTotal = fbp.getTotalEnergyConsumption();
			double gmaxLargest = system.getLargestDeviceEnergtConsumption();
			
			GmaxSavingRatio += gmaxTotal / initialTotal;
			
			/* 
			 * Like naive greedy test
			 */
			
			fbp.reset();
			system.reset();
			
//			System.out.println("Like naive greedy test: " + i);
//			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
//			
			likeNaiveExecutionTime += ReduceGreedySimulation(system, fbp);
//			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
//			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
//			System.out.println("");
//			
			double likeNaiveTotal = fbp.getTotalEnergyConsumption();
			double likeNaiveLargest = system.getLargestDeviceEnergtConsumption();
			likeNaiveSavingRatio += likeNaiveTotal / initialTotal;
			
//			fbp.print();
//			System.out.println(system.toString());
//			System.out.println("");
			/* 
			 * Greedy test
			 */
			
			fbp.reset();
			system.reset();
			
//			System.out.println("Greedy test: " + i);
//			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
//			
			GreedyExecutionTime += GreedySimulation(system, fbp);
//			System.out.println("Largest Device Energy Consumption: " + system.getLargestDeviceEnergtConsumption());
//			System.out.println("System Total Energy Consumption: " + system.getTotalEnergyConsumption());
//			System.out.println("");
			
			double greedyTotal = fbp.getTotalEnergyConsumption();
			double greedyLargest = system.getLargestDeviceEnergtConsumption();
			GreedySavingRatio += greedyTotal / initialTotal;
//			fbp.print();
//			System.out.println(system.toString());
//			System.out.println("");
			
			double largest = Math.max(Math.max(greedyLargest, Math.max(gmaxLargest, gmin2Largest)), gminLargest);
			GminLargestRatio += gminLargest / largest;
			Gmin2LargestRatio += gmin2Largest / largest;
			GmaxLargestRatio += gmaxLargest / largest;
			GreedyLargestRatio += greedyLargest / largest;
			
		}

		
		System.out.println("SRatio " + GminSavingRatio / iteration + ", " + Gmin2SavingRatio / iteration + ", "+GmaxSavingRatio / iteration +", " + maximumSavingRatio /iteration+", " + maximumImportanceSavingRatio /iteration +", " + likeNaiveSavingRatio /iteration + ", " + GreedySavingRatio /iteration);
		System.out.println("LRatio " + GminLargestRatio / ITERATION + ", " + Gmin2LargestRatio / ITERATION + ", "+GmaxLargestRatio / ITERATION +", " + GreedyLargestRatio /ITERATION);
		
		System.out.println("Average Execution Time for Gmin method:" + GminExecutionTime / iteration);
		System.out.println("Average Execution Time for Gmin2 method:" + Gmin2ExecutionTime / iteration);
		System.out.println("Average Execution Time for Maximum method:" + maximumExecutionTime / iteration);
		System.out.println("Average Execution Time for Maximum Importance method:" + maximumImportanceExecutionTime / iteration);
		System.out.println("Average Execution Time for Gmax method:" + GmaxExecutionTime / iteration);
		System.out.println("Average Execution Time for Like navie greedy method:" + likeNaiveExecutionTime / iteration);
		System.out.println("Average Execution Time for Greedy method:" + GreedyExecutionTime / iteration);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MEDES2014Simulator simulator = new MEDES2014Simulator();
		simulator.run(ITERATION, 2000, 50, 100, TYPE.LINEAR, 6);
	}
}
