package edu.uci.eecs.wukong.energy.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;
import edu.uci.eecs.wukong.common.CollocationGraph;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.FlowGraph;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.util.WeightedIndependentSetSelector;

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
			
			FlowGraph graph = new FlowGraph();
			
			ImmutableList<Edge> mergableEdges = fbp.getMergableEdges(system);
			for(Edge edge: mergableEdges){
				graph.addEdge(edge);
			}
//			WeightedIndependentSetSelector selector = new WeightedIndependentSetSelector(system);
			
			CollocationGraph collocationGraph = new CollocationGraph(graph, system);
			collocationGraph.print();
			
//			List<Edge> answers = selector.select(graph);
			
		} catch (Exception e) {
			
		} finally {
			
		}
	}

}
