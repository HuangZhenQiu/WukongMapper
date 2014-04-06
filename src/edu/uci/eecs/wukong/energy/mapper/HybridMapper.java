package edu.uci.eecs.wukong.energy.mapper;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;

public class HybridMapper extends DistanceUnawareSelectionBasedMapper {

	public HybridMapper(WukongSystem system, FlowBasedProcess fbp, MapType type, int timeout) {
		super(system, fbp, type, timeout);
	}
	
	@Override
	public void map() {
		
		if (system == null || fbp == null) {
			System.out.println("Wukong System or FBP is null.");
			return;
		}
		
		if (system.getDeviceNumber() == 0 || system.getWuClassNunber() == 0 ) {
			System.out.println("There is no device or wuclass within the wuclass system.");
			return;
		}
		
		if (fbp.getEdgeNumber() == 0) {
			System.out.println("There is no edge within the fbp.");
		}

		if(system.merge(fbp)) {
			
			Problem problem = buildProblem();
			Solver solver = factory.get(); // you should use this solver only once for one problem
			Result result = solver.solve(problem);

			//System.out.println(result);
			applyResult(result);
			//System.out.println(system);
		
		} else {
			System.out.println("Failed to merge the fbp within current system.");
		}

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
				
				HybridMapper mapper = new HybridMapper(system, fbp, MapType.ONLY_LOCATION, 200);
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
