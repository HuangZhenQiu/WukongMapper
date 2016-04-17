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
		this.wukongFactory = new WuKongSystemFactory(30, 300, 10, 100, 10, 10, false);
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
		
		for (int i = 0; i < 10; i ++) {
			FlowBasedProcess fbp = fbpFactory.createFlowBasedProcess(TYPE.SCALE_FREE);
			WukongSystem system = wukongFactory.createRandomWuKongSystem();
			
			System.out.println("===================================================");
			
			StaticMapper staticMapper = new StaticMapper(system, fbp, MapType.WITHOUT_LATENCY);
			staticMapper.map();
			staticMissRatio += staticMapper.getMissDeadlineRatio();
			staticMax += system.getMaxReprogramGateway();
			
			System.out.println("===================================================");
			
			fbp.reset();
			system.reset();
			

			UniformMapper uniformMapper = new UniformMapper(system, fbp, MapType.WITHOUT_LATENCY);
			uniformMapper.map();
			uniformMissRatio += uniformMapper.getMissDeadlineRatio();
			uniformMax += system.getMaxReprogramGateway();
			
			System.out.println("===================================================");
			
			fbp.reset();
			system.reset();
			ScalabilitySelectionMapper optimalMapper = new ScalabilitySelectionMapper(
					system, fbp, MapType.WITHOUT_LATENCY, false, 20000);
			optimalMapper.map();
			optimalMissRatio += optimalMapper.getMissDeadlineRatio();
			optimalMax += system.getMaxReprogramGateway();
			
			
			fbp.reset();
			system.reset();
			optimalMapper = new ScalabilitySelectionMapper(
					system, fbp, MapType.WITH_LATENCY, false, 20000);
			optimalMapper.map();
			optimalWithRunTimeMissRatio += optimalMapper.getMissDeadlineRatio();
			optimalWithRunTime += system.getMaxReprogramGateway();
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
