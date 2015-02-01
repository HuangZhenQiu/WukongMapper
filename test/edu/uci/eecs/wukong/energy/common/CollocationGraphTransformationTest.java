package edu.uci.eecs.wukong.energy.common;

import java.util.List;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;
import edu.uci.eecs.wukong.colocation.ColocationGraphNode;
import edu.uci.eecs.wukong.colocation.FlowGraph;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcessEdge;
import edu.uci.eecs.wukong.energy.mapper.OptimalGreedyBasedMapper.GreedyType;
import edu.uci.eecs.wukong.util.FlowBasedProcessFactory;
import edu.uci.eecs.wukong.util.WeightedIndependentSetSelector;
import edu.uci.eecs.wukong.util.WuKongSystemFactory;
import edu.uci.eecs.wukong.util.GraphGenerator.TYPE; 

public class CollocationGraphTransformationTest extends TestCase{
	
	
	private int K;
	private int replica;
	
	private int classNumber; 
	private int deviceNumber;
	
	private TYPE type;
	public CollocationGraphTransformationTest(int K, int replica){
		super();
		this.K = K;
		this.replica = replica;
		this.type = TYPE.RANDOM;
	}
	
	public void setType(TYPE type) {
		this.type = type;
	}
	
	public void setDeviceNumber(int number) {
		this.deviceNumber = number;
	}
	
	public void setClassNumber(int number) {
		this.classNumber = number;
	}
	
	public void testCollocationGraphTrasformation() {
		
		
		int iteration = 1000; 
		int duration = 0;
		int node_size = 0;
		int edge_size = 0;
		FlowBasedProcessFactory fbpFactory = new FlowBasedProcessFactory(10 /* landmark number */, classNumber, 100 /* distance range */, 100 /* weight */);
		WuKongSystemFactory wukongFactory = new WuKongSystemFactory(classNumber, deviceNumber, 10 /* landmark number */, 100 /* distance range */);
		
		for(int i = 0; i < iteration; i++){
			try {
	
				
				FlowBasedProcess fbp = fbpFactory.createFlowBasedProcess(type);
				WukongSystem system = wukongFactory.createRandomWukongSystem(K, replica);
				
				FlowGraph graph = new FlowGraph();
				ImmutableList<FlowBasedProcessEdge> mergableEdges = fbp.getMergableEdges(system);
				for(FlowBasedProcessEdge edge: mergableEdges){
					graph.addEdge(edge);
				}
				long start = System.currentTimeMillis();
				WeightedIndependentSetSelector selector = new WeightedIndependentSetSelector(system, GreedyType.GWMIN);
				List<ColocationGraphNode> nodes = selector.select_layer(graph);
				
				node_size += nodes.size();
				
//				List<ColocationGraphNode> nodes = selector.select_layer(graph);
				long end = System.currentTimeMillis();
//				System.out.println("Running time: " + (end-start));
				duration += (end-start);
//				System.out.print("Node selection: ");
//				for(ColocationGraphNode node: nodes){
//					System.out.print(node.getNodeId() + ": ");
//					System.out.print(node.getInvolveWuClasses() + ", ");
//				}
//				System.out.println();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				
			}
		}
		System.out.println("Average node space: " + (double) node_size / (double) iteration + ", average edge space: " + (double) edge_size / (double) iteration);
		System.out.println("Time elapsed: " + (double) duration / (double) iteration);
	}
	

}
