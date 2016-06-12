package edu.uci.eecs.wukong.scalability;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WuDevice;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.mapper.AbstractMapper;
import edu.uci.eecs.wukong.mapper.Mapper.MapType;
import edu.uci.eecs.wukong.scalability.mapper.ScalabilitySelectionMapper;
import edu.uci.eecs.wukong.scalability.mapper.StaticMapper;
import edu.uci.eecs.wukong.scalability.mapper.UniformMapper;
import edu.uci.eecs.wukong.util.FlowBasedProcessFactory;
import edu.uci.eecs.wukong.util.FlowBasedProcessTreeFactory;
import edu.uci.eecs.wukong.util.FlowBasedProcessVerificationFactory;
import edu.uci.eecs.wukong.util.WuKongSystemFactory;
import edu.uci.eecs.wukong.util.WuKongSystemPhysicalFirstFactory;
import edu.uci.eecs.wukong.util.WuKongSystemVerificationFactory;
import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;

public class VerificationScalabilitySimulator {
	private FlowBasedProcessVerificationFactory fbpFactory;
	private WuKongSystemVerificationFactory wukongFactory;
//	private int[] sensorNumbers = new int[]{8};
//	private int[] gatewayNumbers = new int[]{20, 60, 100};
//	private int[] devicePerGateways = new int[]{20, 60, 100};
	private int regionNumber = 2;
	private int objectPerDevice = 5;
	private int classNumber = 4;
	private int sampleNumber = 10000;
	private int sensorNumber;
	private int dominantPath;
	private String directory;
	
	public VerificationScalabilitySimulator(int sensorNumber, String dir) {
		//WukongProperties.getProperty();

		this.sensorNumber = sensorNumber;
		this.dominantPath = 10;//(int) Math.ceil(Math.log(sensorNumber)/Math.log(2));
		this.directory = dir;
		
//		this.fbpFactory = new FlowBasedProcessFactory(30, classNumber, 4, 100 /**distance range**/, 100 /**weight**/, sensorNumber);
//		this.wukongFactory = new WuKongSystemFactory(classNumber, devicePerGateway*gatewayNumber, objectPerDevice, 10, 100, gatewayNumber, regionNumber, false);
	}
	
	public void run() {
		

		// Given FBP, study the difference of systems
//		FlowBasedProcess[][] fbps = new FlowBasedProcess[sensorNumbers.length][];
//		int count = 0;
//		for(Integer sensorNumber: sensorNumbers){
//			fbps[count] = new FlowBasedProcess[sampleNumber];
//			this.fbpFactory = new FlowBasedProcessFactory(30, classNumber, 4, 100 /**distance range**/, 100 /**weight**/, sensorNumber);
//			for(int structureId = 0; structureId < sampleNumber; structureId++){
//				fbps[count][structureId] = this.fbpFactory.createFlowBasedProcess(structureId);
//			}
//			count++;
//		}
		

		int gatewayNumber = 3;
		int deviceNumber = 8;
		
		this.wukongFactory = new WuKongSystemVerificationFactory(classNumber, deviceNumber, objectPerDevice, 10, 100, gatewayNumber, regionNumber, false);
		AbstractMapper.MAX_HOP = this.dominantPath;
		this.fbpFactory = new FlowBasedProcessVerificationFactory(30, classNumber, 4, 100 /**distance range**/, 100 /**weight**/, sensorNumber);

		int staticMax = 0;
		long staticExecutionTime = 0;
		float staticMissRatio = 0;
		
		int uniformMax = 0;
		long uniformExecutionTime = 0;
		float uniformMissRatio = 0;
		
		int optimalMax = 0;
		long optimalExectionTime = 0;
		float optimalMissRatio = 0;
		
		int optimalWithRunTime = 0;
		long optimalWithRunTimeExecutionTime = 0;
		float optimalWithRunTimeMissRatio = 0;

		int roundUpMax = 0;
		long roundUpExecutionTime = 0;
		float roundUpMissRatio = 0;
		
		for (int i = 0; i < sampleNumber;){

//			if (!fbp.getDominatePaths(this.dominantPath).isEmpty()){
			if (true) {
				FlowBasedProcess fbp = this.fbpFactory.createFlowBasedProcess();
				WukongSystem system = wukongFactory.createRandomWuKongSystem();
				ScalabilitySelectionMapper optimalRunTimeMapper = new ScalabilitySelectionMapper(
						system, fbp, MapType.WITH_LATENCY, false, 1800);

				System.out.println("=================Optimal With Latency Mapping==================================");
				System.out.println("@@@ i = "+ i + " @@@@@@@@@@@@@@@@@@@@@@@@@@");
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
					System.out.println("@@@ i = "+ i + " @@@@@@@@@@@@@@@@@@@@@@@@@@");
					
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
					System.out.println("@@@ i = "+ i + " @@@@@@@@@@@@@@@@@@@@@@@@@@");
					
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
					System.out.println("@@@ i = "+ i + " @@@@@@@@@@@@@@@@@@@@@@@@@@");
					
					fbp.reset();
					system.reset();
					ScalabilitySelectionMapper optimalMapper = new ScalabilitySelectionMapper(
							system, fbp, MapType.WITHOUT_LATENCY, false, 1800);
					start = System.currentTimeMillis();
					optimalMapper.map();
					optimalExectionTime += (System.currentTimeMillis() - start);
					optimalMapper.printLatencyHops();
					optimalMissRatio += optimalMapper.getMissDeadlineRatio();
					optimalMax += system.getMaxReprogramGateway();
					
					System.out.println("==================Optimal Round Up=================================");
					System.out.println("@@@ i = "+ i + " @@@@@@@@@@@@@@@@@@@@@@@@@@");
					
					fbp.reset();
					system.reset();
					ScalabilitySelectionMapper roundUpMapper = new ScalabilitySelectionMapper(
							system, fbp, MapType.WITH_LATENCY, true, 1800);
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
//		TreeScalabilitySimulator simulator = new TreeScalabilitySimulator(Integer.parseInt(args[0]), args[1]);
		VerificationScalabilitySimulator simulator = new VerificationScalabilitySimulator(3, "/home/l");
		simulator.run();
	}
}
