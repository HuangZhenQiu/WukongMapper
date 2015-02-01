package edu.uci.eecs.wukong.energy.mapper;

import junit.framework.TestCase;
import org.junit.Test;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;

public class BasicMapperTest extends TestCase {
	
	@Test
	public void testFlowBasedProcessInitialization() {
		String root = System.getProperty("user.dir");
		try {
			FileReader inputStream = new FileReader(new File(root + "/data/fbp.txt"));
			FlowBasedProcess fbp = new FlowBasedProcess(TYPE.LINEAR);
			fbp.initialize(new BufferedReader(inputStream));
			TestCase.assertEquals(fbp.getEdgeNumber().intValue(), 2);
			
		} catch (Exception e) {
			System.out.println(e.toString());
			TestCase.assertEquals(true, false);
		} 
	}
	
	@Test
	public void testWukongSystemInitialization() {
		String root = System.getProperty("user.dir");
		try {
			FileReader inputStream = new FileReader(new File(root + "/data/wukong.txt"));
			WukongSystem system = new WukongSystem();
			system.initialize(new BufferedReader(inputStream));
			TestCase.assertEquals(system.getDeviceNumber(), 4);
			
		} catch (Exception e) {
			System.out.println(e.toString());
			TestCase.assertEquals(true, false);
		} 
	}

}
