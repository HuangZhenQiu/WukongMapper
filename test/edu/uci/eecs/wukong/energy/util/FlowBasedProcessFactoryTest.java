package edu.uci.eecs.wukong.energy.util;

import junit.framework.TestCase;
import org.junit.Test;

import edu.uci.eecs.wukong.util.FlowBasedProcessFactory;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

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
		ImmutableList<Edge> lists = process.getEdges();
		UnmodifiableIterator<Edge> iterator = lists.iterator();
		while(iterator.hasNext()) {
			assertEquals(true, iterator.next().getWeight() < 50);
		}
	}
	

}
