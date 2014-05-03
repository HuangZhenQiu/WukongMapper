package edu.uci.eecs.wukong.energy.mapper;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowGraph;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;
import edu.uci.eecs.wukong.energy.mapper.Mapper.MapType;
import edu.uci.eecs.wukong.util.WeightedIndependentSetSelector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

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
	
	
	public OptimalGreedyBasedMapper(WukongSystem system, FlowBasedProcess fbp,
			MapType type) {
		super(system, fbp, type);
		// TODO Auto-generated constructor stub
	}

	public boolean map() {
		merge();
		return false;
	}
	
	private ImmutableList<Edge> merge() {
		ImmutableList<Edge> mergableEdges = this.fbp.getMergableEdges(this.system);
		
//		ImmutableList<FlowGraph> graphs = split(mergableEdges);
		
//		for(FlowGraph graph: graphs){
//			graph.print();
//		}
		FlowGraph graph = new FlowGraph();
		for(Edge edge: mergableEdges){
			graph.addEdge(edge);
		}
		WeightedIndependentSetSelector selector = new WeightedIndependentSetSelector(system);
		List<Edge> answers = selector.select(graph);
//		graph.print();
		return mergableEdges;
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
				
				OptimalGreedyBasedMapper mapper = new OptimalGreedyBasedMapper(system, fbp, MapType.ONLY_LOCATION);
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
