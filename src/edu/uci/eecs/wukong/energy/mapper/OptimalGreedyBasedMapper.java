package edu.uci.eecs.wukong.energy.mapper;

import edu.uci.eecs.wukong.common.CollocationGraphNode;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WuDevice;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowGraph;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;
import edu.uci.eecs.wukong.util.WeightedIndependentSetSelector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import com.google.common.collect.ImmutableList;

/**
 * It is the algorithm for TETS Journal which is using the optimal greedy algorithm
 * for weighted set cover for the first step of mapping.
 * 
 * A FBP can be divided into several sub-graph which are mergeable links by removing
 * unmergable links. Then, an optimal greedy algorithm for weighted set cover can be 
 * used for each sub-graph.
 * 
 * 
 * @author Peter
 *
 */

public class OptimalGreedyBasedMapper extends AbstractMapper {
	public static enum GreedyType{
		GWMIN,
		GWMAX,
		GWMIN2,
	}
	
	private GreedyType greedyType;
	
	public OptimalGreedyBasedMapper(WukongSystem system, FlowBasedProcess fbp,
			MapType type, GreedyType greedyType) {
		super(system, fbp, type);
		this.greedyType = greedyType;
	}

	public boolean map() {
		LinkedList<CollocationGraphNode> answers = (LinkedList<CollocationGraphNode>) merge();
		
		for(int i = 0; i < answers.size();i++){
			CollocationGraphNode node = answers.get(i);
			WuDevice device = system.getHostableDevice(node.getInvolveWuClasses());
			
			for(Edge edge: node.getMergingEdges()){
				edge.getInWuClass().deploy(device.getWuDeviceId());
				edge.getOutWuClass().deploy(device.getWuDeviceId());
			}
			System.out.println("device " + device.getWuDeviceId() + " will merge" + node.getMergingEdges());
		}
		
		ArrayList<Edge> temp = new ArrayList<FlowBasedProcess.Edge>();
		for(Edge edge : fbp.getEdges()){
			if(!edge.isFullDeployed()){
				temp.add(edge);
			}
		}
		
		if(!system.deployWithNoMerge(fbp, temp)){
			return false;
		}
		
		if(fbp.isDeployed()) {
			//fbp.print();
//			System.out.println("System total energy consumpiton is: " + system.getTotalEnergyConsumption());
		} else {
			System.out.println("FBP is not successfully deployed.");
			//fbp.print();
		}
		
		return true;
	}
	
	private List<CollocationGraphNode> merge() {
		List<CollocationGraphNode> answers = new LinkedList<CollocationGraphNode>();
		ImmutableList<Edge> mergableEdges = this.fbp.getMergableEdges(this.system);
		
		ImmutableList<FlowGraph> graphs = split(mergableEdges);
		for(FlowGraph graph: graphs){
//			graph.print();
			WeightedIndependentSetSelector selector = new WeightedIndependentSetSelector(system, greedyType);
//			System.out.println(selector.select(graph));
			answers.addAll(selector.select(graph));
		}
		return answers;
	}
	
	private ImmutableList<FlowGraph> split(ImmutableList<Edge> mergableEdges) {
		List<FlowGraph> graphs= new LinkedList<FlowGraph>();
		graphs.add(new FlowGraph());
		
		for(Edge edge : mergableEdges) {
			FlowGraph first = null;
			FlowGraph second = null;
			for(FlowGraph graph : graphs) {
				if(graph.isConnect(edge)) {
					if(first == null) {
						first = graph;
					} else if (second == null){
						second = graph;
					}
				}
			}
			
			if(first == null) {
				//create a new graph
				FlowGraph graph = new FlowGraph();
				graph.addEdge(edge);
				graphs.add(graph);
			} else if (second == null) {
				first.addEdge(edge);
			} else {
				// merge two graph
				first.addEdge(edge);
				first.merge(second);
				graphs.remove(second);
			}
		}
	
		return ImmutableList.<FlowGraph>builder().addAll(graphs).build();
	}
	public static void main(String argues[]){

		if(argues.length < 2) {
			System.out.println("Please input paths of two initialization files");
			System.exit(-1);
		}
		
		try {
			
			File fbpConfig = new File(argues[0]);
			BufferedReader fbpConfigReader = new BufferedReader(new FileReader(fbpConfig));
			FlowBasedProcess fbp = new FlowBasedProcess(TYPE.RANDOM);
			
			File systemConfig = new File(argues[1]);
			BufferedReader systemConfigReader = new BufferedReader(new FileReader(systemConfig));
			WukongSystem system = new WukongSystem();
			
			try {

				
				fbp.initialize(fbpConfigReader);
				system.initialize(systemConfigReader);
				
				OptimalGreedyBasedMapper mapper = new OptimalGreedyBasedMapper(system, fbp, MapType.ONLY_LOCATION, GreedyType.GWMIN);
				mapper.map();
				
			} finally {
				fbpConfigReader.close();
				systemConfigReader.close();
				
			}
		
		} catch (IOException e) {
			
			System.out.println(e.toString());
		} 
	}
}
