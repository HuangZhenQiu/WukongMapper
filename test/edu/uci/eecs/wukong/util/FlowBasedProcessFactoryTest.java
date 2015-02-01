package edu.uci.eecs.wukong.util;

import junit.framework.TestCase;
import org.junit.Test;

import edu.uci.eecs.wukong.util.FlowBasedProcessFactory;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.FlowBasedProcessEdge;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

import edu.uci.eecs.wukong.util.GraphGenerator.TYPE;

public class FlowBasedProcessFactoryTest extends TestCase{
	
	private FlowBasedProcessFactory factory;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		factory = new FlowBasedProcessFactory(20, 20, 10, 50);
	}
	
	@Test
	public void testCreateLinearFBP() {
		FlowBasedProcess process = factory.createFlowBasedProcess(TYPE.LINEAR);
		ImmutableList<FlowBasedProcessEdge> lists = process.getEdges();
		UnmodifiableIterator<FlowBasedProcessEdge> iterator = lists.iterator();
		while(iterator.hasNext()) {
			assertEquals(true, iterator.next().getWeight() < 50);
		}
	}
	

}
