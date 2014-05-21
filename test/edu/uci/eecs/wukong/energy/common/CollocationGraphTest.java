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
			
			ColocationGraph collocationGraph = new ColocationGraph(graph, system, 1);
			collocationGraph.print();
			for (ColocationGraphNode node : collocationGraph.getNodes()) {
				System.out.println("I am node" + node.getNodeId());
				System.out.println(node.getNeighbors().size());
			}
//			CollocationGraph collocationGraph2 = new CollocationGraph(graph, system);
//			collocationGraph2.print();
			
		} catch (Exception e) {
			
		} finally {
			
		}
	}

}
