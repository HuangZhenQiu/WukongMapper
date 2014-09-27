package edu.uci.eecs.wukong.colocation;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcessEdge;
import edu.uci.eecs.wukong.util.Pair;

public class LayeredCollocationGraph extends AbstractColocationGraph{

	List<ArrayList<ColocationGraphNode>> layers;
	
	public LayeredCollocationGraph(FlowGraph graph, WukongSystem system) {
		super(graph, system);
		this.layers = new ArrayList<ArrayList<ColocationGraphNode>>();
		this.init();
	}
	
	public LayeredCollocationGraph(FlowGraph graph, WukongSystem system, boolean one_layer) {
		super(graph, system);
		this.layers = new ArrayList<ArrayList<ColocationGraphNode>>();
		if(one_layer){
			this.bare_init();
		}
		else{
			this.init();
		}
	}
	
	public void bare_init() {
		rawInitCollocationGraph(graph);

		ArrayList<ColocationGraphNode> first = new ArrayList<ColocationGraphNode>();
		for(ColocationGraphNode node: getNodes()){
			first.add(node);
		}
		
		layers.add(first);
		
		for (int i = 0; i < layers.size(); i++) {
			
			ArrayList<Pair<ColocationGraphNode, ColocationGraphNode>> pair_list = new ArrayList<Pair<ColocationGraphNode, ColocationGraphNode>>();
			
			for (int k = 0; k < layers.get(i).size() - 1; k++) {
				for (int j = k + 1; j < layers.get(i).size(); j++) {
					Pair<ColocationGraphNode, ColocationGraphNode> pair = new Pair<ColocationGraphNode, ColocationGraphNode>(layers.get(i).get(k), layers.get(i).get(j));
					pair_list.add(pair);
				}
			}
			
			while(pair_list.size() > 0){
				Pair<ColocationGraphNode, ColocationGraphNode> pair = pair_list.remove(0);
				ColocationGraphNode node1 = pair.getFirst();
				ColocationGraphNode node2 = pair.getSecond();
				
			
				if (getIntersection(node1, node2).size() != 0) {
					Set<Integer> union = getUnion(node1, node2);
					
					if (!system.isHostable(union)){
						ColocationGraphEdge edge = new ColocationGraphEdge(node1, node2);
						addEdge(edge);
					}
				}
			}
			
		}
	}
	
	public void init() {
		rawInitCollocationGraph(graph);

		ArrayList<ColocationGraphNode> first = new ArrayList<ColocationGraphNode>();
		for(ColocationGraphNode node: getNodes()){
			first.add(node);
		}
		
		layers.add(first);
		
		for(int i = 0; i < 9; i ++) {
			ArrayList<ColocationGraphNode> layer = new ArrayList<ColocationGraphNode>();
			layers.add(layer);
		}
		
		for (int i = 0; i < layers.size(); i++) {
			
			ArrayList<ColocationGraphEdge> edgesToBeAdd = new ArrayList<ColocationGraphEdge>();
			for (int k = 0; k < layers.get(i).size(); k++) {
				ColocationGraphNode node1 = layers.get(i).get(k);
				
				for(ColocationGraphNode parent: node1.getParents()){
					for(ColocationGraphNode neighbor: parent.getNeighbors()){
						ColocationGraphEdge edge = new ColocationGraphEdge(neighbor, node1);
						if(!isEdgeExist(edge)){
							edgesToBeAdd.add(edge);
						}
					}
				}
			}
			
			for (ColocationGraphEdge edge2: edgesToBeAdd){
				if(!isEdgeExist(edge2)){
					addEdge(edge2);
				}
			}
			
			ArrayList<Pair<ColocationGraphNode, ColocationGraphNode>> pair_list = new ArrayList<Pair<ColocationGraphNode, ColocationGraphNode>>();
			
			for (int k = 0; k < layers.get(i).size() - 1; k++) {
				for (int j = k + 1; j < layers.get(i).size(); j++) {
					Pair<ColocationGraphNode, ColocationGraphNode> pair = new Pair<ColocationGraphNode, ColocationGraphNode>(layers.get(i).get(k), layers.get(i).get(j));
					pair_list.add(pair);
				}
			}
			
			while(pair_list.size() > 0){
				Pair<ColocationGraphNode, ColocationGraphNode> pair = pair_list.remove(0);
				ColocationGraphNode node1 = pair.getFirst();
				ColocationGraphNode node2 = pair.getSecond();
				
			
				if (getIntersection(node1, node2).size() != 0) {
					Set<Integer> union = getUnion(node1, node2);
					
					ColocationGraphEdge edge = new ColocationGraphEdge(node1, node2);
					addEdge(edge);
					
					if (system.isHostable(union)) {
						// hostable @@
						
						Set<FlowBasedProcessEdge> edges = new HashSet<FlowBasedProcessEdge>(node1.getMergingEdges());
						edges.addAll(node2.getMergingEdges());
						double new_weight = 0;
						new_weight = node1.getWeight() + node2.getWeight();
//						for(FlowBasedProcessEdge e: edges){
//							new_weight += e.getDataVolumn();
//						}
						 
						ColocationGraphNode node = new ColocationGraphNode(union, new_weight, edges);
						node.addParents(node1);
						node.addParents(node2);
						
						int layer_index = node.getInvolveWuClasses().size()-2;
						
						if(layers.size()-1 < layer_index) {
							System.out.println(layer_index + ", " + (layers.size()-1));
							for(int k = 0; k < layer_index - layers.size() + 1; k ++) {
								ArrayList<ColocationGraphNode> layer = new ArrayList<ColocationGraphNode>();
								layers.add(layer);
							}
						}
						
						ColocationGraphNode check = null;
						if ((check = isNodeExist(node, layers.get(layer_index)))!=null){
							check.addParents(node1);
							check.addParents(node2);
						}else {
							layers.get(layer_index).add(node);
							addNode(node);
						}
					}
				}
			}
			
		}
	}
	
	private ColocationGraphNode isNodeExist(ColocationGraphNode node, ArrayList<ColocationGraphNode> layer) {
		for(ColocationGraphNode check : layer){
			if(check.equal(node)){
				return check;
			}
		}
		return null;
	}
	
	public void printLayeredColocationGraph(){

		printNodes();
		
		for(ColocationGraphNode node: getNodes()){
			System.out.println(node.getParents());
		}
		
		printEdges();
	}
}
