package edu.uci.eecs.wukong.scalability;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.mapper.Mapper.MapType;
import edu.uci.eecs.wukong.scalability.mapper.ScalabilitySelectionMapper;
import edu.uci.eecs.wukong.scalability.mapper.StaticMapper;
import edu.uci.eecs.wukong.scalability.mapper.UniformMapper;
import edu.uci.eecs.wukong.util.FlowBasedProcessFactory;
import edu.uci.eecs.wukong.util.WuKongSystemFactory;
import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;

public class ScalabilitySimulator {
	private FlowBasedProcessFactory fbpFactory;
	private WuKongSystemFactory wukongFactory;
	
	public ScalabilitySimulator() {
		//WukongProperties.getProperty();
		this.fbpFactory = new FlowBasedProcessFactory(10, 20, 4, 100 /**distance range**/, 100 /**weight**/);
		this.wukongFactory = new WuKongSystemFactory(30, 100, 10, 100, 10, 10, false);
	}
	
	public void run() {
		int staticMax = 0;
		long staticExecutionTime = 0;
		int uniformMax = 0;
		long uniformExecutionTime = 0;
		int optimalMax = 0;
		long optimalExectionTime = 0;
		int optimalWithRunTime = 0;
		long optimalWithRunTimeExecutionTime = 0;
		int roundUpMax = 0;
		long roundUpExecutionTime = 0;
		float staticMissRatio = 0;
		float uniformMissRatio = 0;
		float optimalMissRatio = 0;
		float optimalWithRunTimeMissRatio = 0;
		float roundUpMissRatio = 0;
		
		for (int i = 0; i < 100;) {
			FlowBasedProcess fbp = fbpFactory.createFlowBasedProcess(TYPE.RANDOM);
			WukongSystem system = wukongFactory.createRandomWuKongSystem();
			
			if (!fbp.getDominatePaths(10).isEmpty()) {
			
				System.out.println("=================Optimal with Latency==================================");
				
				ScalabilitySelectionMapper optimalRunTimeMapper = new ScalabilitySelectionMapper(
						system, fbp, MapType.WITH_LATENCY, false, 20000);
				
				long start = System.currentTimeMillis();
				optimalRunTimeMapper.map();
				long timeUsed = System.currentTimeMillis() - start;
				optimalRunTimeMapper.printLatencyHops();
				// If there is a tractable result
				if (optimalRunTimeMapper.getMissDeadlineRatio() == 0) {
				
					optimalWithRunTimeMissRatio += optimalRunTimeMapper.getMissDeadlineRatio();
					optimalWithRunTime += system.getMaxReprogramGateway();
					optimalWithRunTimeExecutionTime += timeUsed;
					
					System.out.println("=================Static Mapping==================================");
					
					
					fbp.reset();
					system.reset();
					
					StaticMapper staticMapper = new StaticMapper(system, fbp, MapType.WITHOUT_LATENCY);
					start = System.currentTimeMillis();
					staticMapper.map();
					staticExecutionTime += (System.currentTimeMillis() - start);
					System.out.println(staticExecutionTime);
					staticMapper.printLatencyHops();
					staticMissRatio += staticMapper.getMissDeadlineRatio();
					staticMax += system.getMaxReprogramGateway();
					
					System.out.println("====================Uniform Mapping===============================");
					
					fbp.reset();
					system.reset();
					
		
					UniformMapper uniformMapper = new UniformMapper(system, fbp, MapType.WITHOUT_LATENCY);
					start = System.currentTimeMillis();
					uniformMapper.map();
					uniformExecutionTime += (System.currentTimeMillis() - start);
					uniformMapper.printLatencyHops();
					uniformMissRatio += uniformMapper.getMissDeadlineRatio();
					uniformMax += system.getMaxReprogramGateway();
					
					System.out.println("=====================Optimal Without Latency==============================");
					
					fbp.reset();
					system.reset();
					ScalabilitySelectionMapper optimalMapper = new ScalabilitySelectionMapper(
							system, fbp, MapType.WITHOUT_LATENCY, false, 20000);
					start = System.currentTimeMillis();
					optimalMapper.map();
					optimalExectionTime += (System.currentTimeMillis() - start);
					optimalMapper.printLatencyHops();
					optimalMissRatio += optimalMapper.getMissDeadlineRatio();
					optimalMax += system.getMaxReprogramGateway();
					
					System.out.println("==================Optimal Round Up=================================");
					fbp.reset();
					system.reset();
					ScalabilitySelectionMapper roundUpMapper = new ScalabilitySelectionMapper(
							system, fbp, MapType.WITH_LATENCY, true, 20000);
					start = System.currentTimeMillis();
					roundUpMapper.map();
					roundUpExecutionTime += (System.currentTimeMillis() - start);
					roundUpMissRatio += roundUpMapper.getMissDeadlineRatio();
					roundUpMax += system.getMaxReprogramGateway();
					
					i++;
				}
			}
		}
		
		System.out.println("Static Max:" + staticMax);
		System.out.println("Static Execution Time:" + staticExecutionTime);
		System.out.println("Static Miss Ratio:" + staticMissRatio);
		System.out.println("Uniform Max:" + uniformMax);
		System.out.println("Uniform Execution Time:" + uniformExecutionTime);
		System.out.println("Uniform Miss Ratio:" + uniformMissRatio);
		System.out.println("Optimal Max:" + optimalMax);
		System.out.println("Optimal Execution Time:" + optimalExectionTime);
		System.out.println("Optimal Miss Ratio:" + optimalMissRatio);
		System.out.println("Optimal With Runtime Max:" + optimalWithRunTime);
		System.out.println("Optimal With Runtime Execution Time:" + optimalWithRunTimeExecutionTime);
		System.out.println("Optimal With Runtime Miss Ratio:" + optimalWithRunTimeMissRatio);
		System.out.println("Roundup Max:" + roundUpMax);
		System.out.println("Roundup Execution Time:" + roundUpExecutionTime);
 
		System.out.println("Roundup Runtime Miss Ratio:" + roundUpMissRatio);
	}
	
	public static void main(String[] args) {
		ScalabilitySimulator simulator = new ScalabilitySimulator();
		simulator.run();
	}
}
