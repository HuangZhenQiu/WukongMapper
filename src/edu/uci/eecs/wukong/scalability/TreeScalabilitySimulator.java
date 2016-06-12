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
import edu.uci.eecs.wukong.util.WuKongSystemFactory;
import edu.uci.eecs.wukong.util.WuKongSystemPhysicalFirstFactory;
import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;

public class TreeScalabilitySimulator {
	private FlowBasedProcessTreeFactory fbpFactory;
	private WuKongSystemPhysicalFirstFactory wukongFactory;
//	private int[] sensorNumbers = new int[]{8};
	private int[] gatewayNumbers = new int[]{20, 60, 100};
	private int[] devicePerGateways = new int[]{20, 60, 100};
	private int regionNumber = 100;
	private int objectPerDevice = 5;
	private int classNumber = 100;
	private int sampleNumber = 20;
	private int sensorNumber;
	private int dominantPath;
	private String directory;
	
	public TreeScalabilitySimulator(int sensorNumber, String dir) {
		//WukongProperties.getProperty();

		this.sensorNumber = sensorNumber;
		this.dominantPath = (int) (sensorNumber*2);//(int) Math.ceil(Math.log(sensorNumber)/Math.log(2));
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
		
		
		this.fbpFactory = new FlowBasedProcessTreeFactory(30, classNumber, 4, 100 /**distance range**/, 100 /**weight**/, sensorNumber);
		AbstractMapper.MAX_HOP = this.dominantPath;
		PrintWriter writer, writer2;
		try {
			writer = new PrintWriter(directory + "/output_"+sensorNumber+".txt", "UTF-8");
			writer2 = new PrintWriter(directory + "/status_"+sensorNumber+".txt", "UTF-8");
			for (Integer gatewayNumber: gatewayNumbers){
				for (Integer devicePerGateway: devicePerGateways){
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

					this.wukongFactory = new WuKongSystemPhysicalFirstFactory(classNumber, devicePerGateway*gatewayNumber, objectPerDevice, 10, 100, gatewayNumber, regionNumber, false);
					for (int i = 0; i < sampleNumber;){
						FlowBasedProcess fbp = this.fbpFactory.createFlowBasedProcess(i);
						WukongSystem system = wukongFactory.createRandomWuKongSystem();

//						if (!fbp.getDominatePaths(this.dominantPath).isEmpty()){
						if (true) {
							ScalabilitySelectionMapper optimalRunTimeMapper = new ScalabilitySelectionMapper(
									system, fbp, MapType.WITH_LATENCY, false, 1800);
							
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
								System.out.println("@@@ gatewayNumber: "+ gatewayNumber + " devicePerGateway: " + devicePerGateway + " i = "+ i + " @@@@@@@@@@@@@@@@@@@@@@@@@@");
								
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
								System.out.println("@@@ gatewayNumber: "+ gatewayNumber + " devicePerGateway: " + devicePerGateway + " i = "+ i + " @@@@@@@@@@@@@@@@@@@@@@@@@@");
								
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
								System.out.println("@@@ gatewayNumber: "+ gatewayNumber + " devicePerGateway: " + devicePerGateway + " i = "+ i + " @@@@@@@@@@@@@@@@@@@@@@@@@@");
								
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
								System.out.println("@@@ gatewayNumber: "+ gatewayNumber + " devicePerGateway: " + devicePerGateway + " i = "+ i + " @@@@@@@@@@@@@@@@@@@@@@@@@@");
								
								fbp.reset();
								system.reset();
								ScalabilitySelectionMapper roundUpMapper = new ScalabilitySelectionMapper(
										system, fbp, MapType.WITH_LATENCY, true, 1800);
								start = System.currentTimeMillis();
								roundUpMapper.map();
								roundUpExecutionTime += (System.currentTimeMillis() - start);
								roundUpMissRatio += roundUpMapper.getMissDeadlineRatio();
								roundUpMax += system.getMaxReprogramGateway();
								
								writer2.println("gatewayNumber: "+ gatewayNumber + " devicePerGateway: " + devicePerGateway + " i = "+ i);
								writer2.flush();
								i++;

							}
						}
						
					}
					
					writer.println("sensorNumber: " + sensorNumber + " gatewayNumber: "+ gatewayNumber + " devicePerGateway: " + devicePerGateway);
					writer.println("Static Max:" + staticMax);
					writer.println("Static Execution Time:" + staticExecutionTime);
					writer.println("Static Miss Ratio:" + staticMissRatio);
					writer.println("Uniform Max:" + uniformMax);
					writer.println("Uniform Execution Time:" + uniformExecutionTime);
					writer.println("Uniform Miss Ratio:" + uniformMissRatio);
					writer.println("Optimal Max:" + optimalMax);
					writer.println("Optimal Execution Time:" + optimalExectionTime);
					writer.println("Optimal Miss Ratio:" + optimalMissRatio);
					writer.println("Optimal With Runtime Max:" + optimalWithRunTime);
					writer.println("Optimal With Runtime Execution Time:" + optimalWithRunTimeExecutionTime);
					writer.println("Optimal With Runtime Miss Ratio:" + optimalWithRunTimeMissRatio);
					writer.println("Roundup Max:" + roundUpMax);
					writer.println("Roundup Execution Time:" + roundUpExecutionTime);
					writer.println("Roundup Runtime Miss Ratio:" + roundUpMissRatio);
					writer.flush();
				}
			}
			writer.close();
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {		
//		TreeScalabilitySimulator simulator = new TreeScalabilitySimulator(Integer.parseInt(args[0]), args[1]);
		TreeScalabilitySimulator simulator = new TreeScalabilitySimulator(2, "/home/l");
		simulator.run();
	}
}
