package edu.uci.eecs.wukong.energy.mapper;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;


/**
 * 
 * 
 * 
 * @author Peter Huang
 *
 */
public class GreedyBasedMapper extends AbstractMapper{
	
	public GreedyBasedMapper(WukongSystem system, FlowBasedProcess fbp, MapType type){
		super(system, fbp, type);
	}
	
	public boolean map() {
		
		if (system == null || fbp == null) {
			System.out.println("Wukong System or FBP is null.");
			return false;
		}
		
		if (system.getDeviceNumber() == 0 || system.getWuClassNunber() == 0 ) {
			System.out.println("There is no device or wuclass within the wuclass system.");
			return false;
		}
		
		if (fbp.getEdgeNumber() == 0) {
			System.out.println("There is no edge within the fbp.");
		}
		
		if(!system.deploy(fbp)){
			return false;
		}
		
		if(fbp.isDeployed()) {
			//fbp.print();
//			System.out.println("System total energy consumpiton is: " + system.getTotalEnergyConsumption());
		} else {
			System.out.println("FBP is not successfully deployed.");
			//fbp.print();
		}
		
		return true;
	}
	
	public static void main(String argues[]) {
		
		if(argues.length < 2) {
			System.out.println("Please input paths of two initialization files");
			System.exit(-1);
		}
		
		try {
			
			File fbpConfig = new File(argues[0]);
			BufferedReader fbpConfigReader = new BufferedReader(new FileReader(fbpConfig));
			FlowBasedProcess fbp = new FlowBasedProcess(TYPE.RANDOM);
			
			File systemConfig = new File(argues[1]);
			BufferedReader systemConfigReader = new BufferedReader(new FileReader(systemConfig));
			WukongSystem system = new WukongSystem();
			
			try {

				
				fbp.initialize(fbpConfigReader);
				system.initialize(systemConfigReader);
				
				GreedyBasedMapper mapper = new GreedyBasedMapper(system, fbp, MapType.ONLY_LOCATION);
				mapper.map();
				
			} finally {
				fbpConfigReader.close();
				systemConfigReader.close();
				
			}
		
		} catch (IOException e) {
			
			System.out.println(e.toString());
		} 
	}

}
