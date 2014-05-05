package edu.uci.eecs.wukong.energy.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.TestCase;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;

public class CollocationGraphTest extends TestCase{
	
	
	public void testCollocationGraphInitization() {
		String root = System.getProperty("user.dir");
		try {
			FileReader inputStream = new FileReader(new File(root + "/data/fbp.txt"));
			FlowBasedProcess fbp = new FlowBasedProcess(FlowBasedProcess.TYPE.LINEAR);
			fbp.initialize(new BufferedReader(inputStream));
			
			inputStream = new FileReader(new File(root + "/data/wukong.txt"));
			WukongSystem system = new WukongSystem();
			system.initialize(new BufferedReader(inputStream));
			
			//TODO test the collocation graph 
		} catch (Exception e) {
			
		} finally {
			
		}
	}

}
