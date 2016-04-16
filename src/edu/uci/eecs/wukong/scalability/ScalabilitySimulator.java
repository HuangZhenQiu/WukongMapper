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
		this.fbpFactory = new FlowBasedProcessFactory(30, 10, 100 /**distance range**/, 100 /**weight**/);
		this.wukongFactory = new WuKongSystemFactory(30, 300, 10, 100, 10, 10, false);
	}
	
	public void run() {
		int staticMax = 0;
		int uniformMax = 0;
		int optimalMax = 0;
		
		for (int i = 0; i < 100; i ++) {
			FlowBasedProcess fbp = fbpFactory.createFlowBasedProcess(TYPE.STAR);
			WukongSystem system = wukongFactory.createRandomWuKongSystem();
			
			System.out.println("===================================================");
			
			StaticMapper staticMapper = new StaticMapper(system, fbp, MapType.WITHOUT_LATENCY);
			staticMapper.map();
			staticMax += system.getMaxReprogramGateway();
			
			System.out.println("===================================================");
			
			fbp.reset();
			system.reset();
			

			UniformMapper uniformMapper = new UniformMapper(system, fbp, MapType.WITHOUT_LATENCY);
			uniformMapper.map();
			uniformMax += system.getMaxReprogramGateway();
			
			System.out.println("===================================================");
			
			fbp.reset();
			system.reset();
			ScalabilitySelectionMapper optimalMapper = new ScalabilitySelectionMapper(
					system, fbp, MapType.WITHOUT_LATENCY, false, 20000);
			optimalMapper.map();
			optimalMax += system.getMaxReprogramGateway();
		}
		
		System.out.println("Static Max:" + staticMax);
		System.out.println("Uniform Max:" + uniformMax);
		System.out.println("Optimal Max:" + optimalMax);
	}
	
	public static void main(String[] args) {
		ScalabilitySimulator simulator = new ScalabilitySimulator();
		simulator.run();
	}
}
