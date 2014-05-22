package edu.uci.eecs.wukong.energy.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;
import edu.uci.eecs.wukong.colocation.FlowGraph;
import edu.uci.eecs.wukong.colocation.UpdatedColocationGraph;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;

public class CollocationGraphTest extends TestCase{
	
	
	public void testCollocationGraphInitization() {
		String root = System.getProperty("user.dir");
		try {
			FileReader inputStream = new FileReader(new File(root + "/data/fbp2.txt"));
			FlowBasedProcess fbp = new FlowBasedProcess(FlowBasedProcess.TYPE.LINEAR);
			fbp.initialize(new BufferedReader(inputStream));
			
			inputStream = new FileReader(new File(root + "/data/wukong2.txt"));
			WukongSystem system = new WukongSystem();
			system.initialize(new BufferedReader(inputStream));
			FlowGraph graph = new FlowGraph();
			
			ImmutableList<Edge> mergableEdges = fbp.getMergableEdges(system);
			for(Edge edge: mergableEdges){
				graph.addEdge(edge);
			}
			
			UpdatedColocationGraph collocationGraph = new UpdatedColocationGraph(graph, system);
			collocationGraph.print();
			
//			LayeredCollocationGraph collocationGraph2 = new LayeredCollocationGraph(graph, system);
//			collocationGraph2.print();
			
//			WeightedIndependentSetSelector selector = new WeightedIndependentSetSelector(
//					system, GreedyType.GWMIN);
//			List<CollocationGraphNode> nodes = selector.select(graph);
//			for(CollocationGraphNode node: nodes){
//				System.out.println("I am node" + node.getNodeId());
//				System.out.println(node.getInvolveWuClasses());
//			}
//			for (CollocationGraphNode node : collocationGraph.getNodes()) {
//				System.out.println("I am node" + node.getNodeId());
//				System.out.println(node.getNeighbors().size());
//			}
//			CollocationGraph collocationGraph2 = new CollocationGraph(graph, system);
//			collocationGraph2.print();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}
	

}
