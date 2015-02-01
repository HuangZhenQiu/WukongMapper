package edu.uci.eecs.wukong.energy.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;
import edu.uci.eecs.wukong.colocation.ColocationGraphNode;
import edu.uci.eecs.wukong.colocation.FlowGraph;
import edu.uci.eecs.wukong.colocation.ColocationGraph;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcessEdge;
import edu.uci.eecs.wukong.energy.mapper.OptimalGreedyBasedMapper.GreedyType;
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
			
			ImmutableList<FlowBasedProcessEdge> mergableEdges = fbp.getMergableEdges(system);
			for(FlowBasedProcessEdge edge: mergableEdges){
				graph.addEdge(edge);
			}
			
//			System.out.println("Result from updated Colocation graph");
//			UpdatedColocationGraph collocationGraph = new UpdatedColocationGraph(graph, system);
//			collocationGraph.print();
			
			System.out.println("Result from layered Colocation graph");
			ColocationGraph collocationGraph2 = new ColocationGraph(graph, system);
			collocationGraph2.print();
			WeightedIndependentSetSelector selector = new WeightedIndependentSetSelector(system, GreedyType.GWMIN2);
			List<ColocationGraphNode> nodes = selector.select_layer(graph);
			
			for(ColocationGraphNode node: nodes){
				System.out.println("I am node" + node.getNodeId());
				System.out.println(node.getInvolveWuClasses());
			}
			
			
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
