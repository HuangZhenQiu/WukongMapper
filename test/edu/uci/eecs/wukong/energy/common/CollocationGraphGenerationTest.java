package edu.uci.eecs.wukong.energy.common;


import junit.framework.TestCase;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;
import edu.uci.eecs.wukong.util.FlowBasedProcessFactory;
import edu.uci.eecs.wukong.util.WuKongSystemFactory;

public class CollocationGraphGenerationTest extends TestCase{
	
	
	
	public void testCollocationGraphInitization() {
		String root = System.getProperty("user.dir");
		try {
			
			FlowBasedProcessFactory fbpFactory = new FlowBasedProcessFactory(10 /* landmark number */, 30 /* class number */, 100 /* distance range */, 100 /* weight */);
			WuKongSystemFactory wukongFactory = new WuKongSystemFactory(30 /* class number */, 20 /* device number */, 10 /* landmark number */, 100 /* distance range */);
			
			FlowBasedProcess fbp = fbpFactory.createFlowBasedProcess(TYPE.STAR);
			WukongSystem system = wukongFactory.createRandomWuKongSystem();
			
			fbp.toFile(root + "/data/fbp2.txt");
			system.toFile(root + "/data/wukong2.txt");
			
			System.out.println("Finish generating random fbp and system");
			
		} catch (Exception e) {
			
		} finally {
			
		}
	}

}
