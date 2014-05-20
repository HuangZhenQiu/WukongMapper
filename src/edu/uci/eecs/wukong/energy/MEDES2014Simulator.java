package edu.uci.eecs.wukong.energy;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;
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
		this.fbpFactory = new FlowBasedProcessFactory(10, 20, 100 /**
		 * distance
		 * range
		 **/
		, 100 /** weight **/
		);
		this.wukongFactory = new WuKongSystemFactory(20, 20, 10, 100);
	}
	
	
	public long GMINSimulation(WukongSystem system, FlowBasedProcess fbp) {
		long start = System.currentTimeMillis();
		OptimalGreedyBasedMapper optimalGreedyBasedMapper = new OptimalGreedyBasedMapper(
				system, fbp, MapType.ONLY_LOCATION, GreedyType.GWMIN);
		optimalGreedyBasedMapper.map();
		long end = System.currentTimeMillis();
		return end - start;
	}
	
	public long GMIN2Simulation(WukongSystem system, FlowBasedProcess fbp) {
		long start = System.currentTimeMillis();
		OptimalGreedyBasedMapper optimalGreedyBasedMapper = new OptimalGreedyBasedMapper(
				system, fbp, MapType.ONLY_LOCATION, GreedyType.GWMIN2);
		optimalGreedyBasedMapper.map();
		long end = System.currentTimeMillis();
		return end - start;
	}
	
	public long GMAXSimulation(WukongSystem system, FlowBasedProcess fbp) {
		long start = System.currentTimeMillis();
		OptimalGreedyBasedMapper optimalGreedyBasedMapper = new OptimalGreedyBasedMapper(
				system, fbp, MapType.ONLY_LOCATION, GreedyType.GWMAX);
		optimalGreedyBasedMapper.map();
		long end = System.currentTimeMillis();
		return end - start;
	}
	
	
	public void run() {
		
		long GminExecutionTime = 0;
		long Gmin2ExecutionTime = 0;
		long GmaxExecutionTime = 0;

		for (int i = 0; i < ITERATION; i++) {

			FlowBasedProcess fbp = fbpFactory.createFlowBasedProcess(TYPE.LINEAR);
			WukongSystem system = wukongFactory.createRandomWuKongSystem();
			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
			
			GminExecutionTime += GMINSimulation(system, fbp);
			System.out.println("");
			
			
			fbp.reset();
			system.reset();
			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
			
			Gmin2ExecutionTime += GMIN2Simulation(system, fbp);
			System.out.println("");
			
			fbp.reset();
			system.reset();
			System.out.println("FBP orignial energy consumption: "+ fbp.getTotalEnergyConsumption());
			
			GmaxExecutionTime += GMAXSimulation(system, fbp);
			System.out.println("");
		}

		System.out.println("Average Execution Time:" + GminExecutionTime / ITERATION);
		System.out.println("Average Execution Time:" + Gmin2ExecutionTime / ITERATION);
		System.out.println("Average Execution Time:" + GmaxExecutionTime / ITERATION);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MEDES2014Simulator simulator = new MEDES2014Simulator();
		simulator.run();
	}
}
