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
		this.fbpFactory = new FlowBasedProcessFactory(30, 30, 100 /**distance range**/, 100 /**weight**/);
		this.wukongFactory = new WuKongSystemFactory(30, 100, 10, 100, 10, 10, false);
	}
	
	public void run() {
		int staticMax = 0;
		int uniformMax = 0;
		int optimalMax = 0;
		int optimalWithRunTime = 0;
		float staticMissRatio = 0;
		float uniformMissRatio = 0;
		float optimalMissRatio = 0;
		float optimalWithRunTimeMissRatio = 0;
		
		for (int i = 0; i < 100;) {
			FlowBasedProcess fbp = fbpFactory.createFlowBasedProcess(TYPE.RANDOM);
			WukongSystem system = wukongFactory.createRandomWuKongSystem();
			
			if (!fbp.getDominatePaths(10).isEmpty()) {
			
				System.out.println("===================================================");
				
				
				ScalabilitySelectionMapper optimalRunTimeMapper = new ScalabilitySelectionMapper(
						system, fbp, MapType.WITH_LATENCY, false, 20000);
				
				optimalRunTimeMapper.map();
				optimalRunTimeMapper.printLatencyHops();
				// If there is a tractable result
				if (optimalRunTimeMapper.getMissDeadlineRatio() == 0) {

					optimalWithRunTimeMissRatio += optimalRunTimeMapper.getMissDeadlineRatio();
					optimalWithRunTime += system.getMaxReprogramGateway();
					
					System.out.println("===================================================");
					
					
					fbp.reset();
					system.reset();
					
					StaticMapper staticMapper = new StaticMapper(system, fbp, MapType.WITHOUT_LATENCY);
					staticMapper.map();
					staticMapper.printLatencyHops();
					staticMissRatio += staticMapper.getMissDeadlineRatio();
					staticMax += system.getMaxReprogramGateway();
					
					System.out.println("===================================================");
					
					fbp.reset();
					system.reset();
					
		
					UniformMapper uniformMapper = new UniformMapper(system, fbp, MapType.WITHOUT_LATENCY);
					uniformMapper.map();
					uniformMapper.printLatencyHops();
					uniformMissRatio += uniformMapper.getMissDeadlineRatio();
					uniformMax += system.getMaxReprogramGateway();
					
					System.out.println("===================================================");
					
					fbp.reset();
					system.reset();
					ScalabilitySelectionMapper optimalMapper = new ScalabilitySelectionMapper(
							system, fbp, MapType.WITHOUT_LATENCY, false, 20000);
					optimalMapper.map();
					optimalMapper.printLatencyHops();
					optimalMissRatio += optimalMapper.getMissDeadlineRatio();
					optimalMax += system.getMaxReprogramGateway();
					
					i++;
				}
			}
		}
		
		System.out.println("Static Max:" + staticMax);
		System.out.println("Static Miss Ratio:" + staticMissRatio);
		System.out.println("Uniform Max:" + uniformMax);
		System.out.println("Uniform Miss Ratio:" + uniformMissRatio);
		System.out.println("Optimal Max:" + optimalMax);
		System.out.println("Optimal Miss Ratio:" + optimalMissRatio);
		System.out.println("Optimal With Runtime Max:" + optimalWithRunTime);
		System.out.println("Optimal With Runtime Miss Ratio:" + optimalWithRunTimeMissRatio);
	}
	
	public static void main(String[] args) {
		ScalabilitySimulator simulator = new ScalabilitySimulator();
		simulator.run();
	}
}
