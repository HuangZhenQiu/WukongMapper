package edu.uci.eecs.wukong.energy.mapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.mapper.Mapper.MapType;
import junit.framework.TestCase;

import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;

public class DistanceAwareSelectionBasedMapperTest extends TestCase {
	
	public void testMapperInitialization() {
		
		String root = System.getProperty("user.dir");
		try {
			FileReader inputStream = new FileReader(new File(root + "/data/fbp.txt"));
			FlowBasedProcess fbp = new FlowBasedProcess(TYPE.LINEAR);
			fbp.initialize(new BufferedReader(inputStream));
			
			inputStream = new FileReader(new File(root + "/data/wukong.txt"));
			WukongSystem system = new WukongSystem();
			system.initialize(new BufferedReader(inputStream));
			
			
			DistanceAwareSelectionBasedMapper mapper = new DistanceAwareSelectionBasedMapper(system, fbp,
					MapType.ONLY_LOCATION, 100, false);
			
			mapper.map();
			
			// Since we take the communication distance into consideration
			TestCase.assertEquals(fbp.getDistanceAwareTotalEnergyConsumption(system), 520.0);
			// We use the 1.2 as the location parameter for transmission consumption.
			TestCase.assertEquals(system.getTotalEnergyConsumption(), 550.0);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
			TestCase.assertEquals(true, false);
		} 
		
	}

}
