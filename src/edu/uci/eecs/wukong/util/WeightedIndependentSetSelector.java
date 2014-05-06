package edu.uci.eecs.wukong.util;

import edu.uci.eecs.wukong.common.CollocationGraph;
import edu.uci.eecs.wukong.common.CollocationGraphNode;
import edu.uci.eecs.wukong.common.FlowGraph;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.energy.mapper.OptimalGreedyBasedMapper.GreedyType;

import java.util.ArrayList;
import java.util.List;

public class WeightedIndependentSetSelector {
	private WukongSystem system;
	private GreedyType greedyType;

	public WeightedIndependentSetSelector(WukongSystem system, GreedyType greedyType) {
		this.system = system;
		this.greedyType = greedyType;
	}

	public List<CollocationGraphNode> select(FlowGraph graph) {

		CollocationGraph collocationGraph = new CollocationGraph(graph, system);
		List<CollocationGraphNode> maxIndependentSet = new ArrayList<CollocationGraphNode>();
		
		while(!collocationGraph.getNodes().isEmpty()) {
			CollocationGraphNode node;
			switch(greedyType){
				case GWMIN:
					node = gminChoose(collocationGraph);
					collocationGraph.deleteNodeAndEdges(node);
					break;
				case GWMAX:
					node = gmaxChoose(collocationGraph);
					collocationGraph.deleteAndItsNeighbors(node);
					break;
				default:
					node = gwmin2Choose(collocationGraph);
					collocationGraph.deleteNodeAndEdges(node);
					break;
			}
			
			
		}

		return maxIndependentSet;
	}

	public CollocationGraphNode gminChoose(CollocationGraph graph) {

		ArrayList<CollocationGraphNode> lists = (ArrayList<CollocationGraphNode>) graph
				.getNodes();

		double value = 0;
		CollocationGraphNode selected = lists.get(0);
		for (CollocationGraphNode node : lists) {
			double comparing = node.getWeight() / (node.getDegree() + 1);

			if (comparing > value) {
				value = comparing;
				selected = node;
			}

		}
		return selected;
	}

	public CollocationGraphNode gmaxChoose(CollocationGraph graph) {

		ArrayList<CollocationGraphNode> lists = (ArrayList<CollocationGraphNode>) graph
				.getNodes();

		CollocationGraphNode selected = lists.get(0);
		double value = -1;

		for (CollocationGraphNode node : lists) {
			if (node.getDegree() != 0) {
				double comparing = node.getWeight()
						/ (node.getDegree()  * (node.getDegree()  + 1));
				if (comparing < value || value == -1) {
					value = comparing;
					selected = node;
				}
			}
		}
		return selected;
	}

	public CollocationGraphNode gwmin2Choose(CollocationGraph graph) {
		ArrayList<CollocationGraphNode> lists = (ArrayList<CollocationGraphNode>) graph.getNodes();

		CollocationGraphNode selected = lists.get(0);
		double value = 0;

		for (CollocationGraphNode node : lists) {

			double comparing = node.getWeight() / graph.getNeighborWeight(node);
			if (comparing > value) {
				value = comparing;
				selected = node;
			}
		}
		return selected;
	}
}
