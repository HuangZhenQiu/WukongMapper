package edu.uci.eecs.wukong.energy.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;
import edu.uci.eecs.wukong.colocation.ColocationGraph;
import edu.uci.eecs.wukong.colocation.ColocationGraphNode;
import edu.uci.eecs.wukong.colocation.FlowGraph;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;
import edu.uci.eecs.wukong.util.FlowBasedProcessFactory;
import edu.uci.eecs.wukong.util.WeightedIndependentSetSelector;
import edu.uci.eecs.wukong.util.WuKongSystemFactory;

public class CollocationGraphGenerationTest extends TestCase{
	
	
	
	public void testCollocationGraphInitization() {
		String root = System.getProperty("user.dir");
		try {
			
			FlowBasedProcessFactory fbpFactory = new FlowBasedProcessFactory(10 /* landmark number */, 20 /* class number */, 100 /* distance range */, 100 /* weight */);
			WuKongSystemFactory wukongFactory = new WuKongSystemFactory(20 /* class number */, 10 /* device number */, 10 /* landmark number */, 100 /* distance range */);
			
			FlowBasedProcess fbp = fbpFactory.createFlowBasedProcess(TYPE.RANDOM);
			WukongSystem system = wukongFactory.createRandomWuKongSystem();
			
			fbp.toFile(root + "/data/fbp2.txt");
			system.toFile(root + "/data/wukong2.txt");
			
			
			
		} catch (Exception e) {
			
		} finally {
			
		}
	}

}
