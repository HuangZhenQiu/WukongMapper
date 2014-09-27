package edu.uci.eecs.wukong.util;

import edu.uci.eecs.wukong.colocation.AbstractColocationGraph;
import edu.uci.eecs.wukong.colocation.BruteForceColocationGraph;
import edu.uci.eecs.wukong.colocation.ColocationGraphNode;
import edu.uci.eecs.wukong.colocation.FlowGraph;
import edu.uci.eecs.wukong.colocation.ColocationGraph;
import edu.uci.eecs.wukong.colocation.OneLayerColocationGraph;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.energy.mapper.OptimalGreedyBasedMapper.GreedyType;

import java.util.ArrayList;
import java.util.List;

public class WeightedIndependentSetSelector {
	private WukongSystem system;
	private GreedyType greedyType;

	public WeightedIndependentSetSelector(WukongSystem system,
			GreedyType greedyType) {
		this.system = system;
		this.greedyType = greedyType;
	}

	public List<ColocationGraphNode> select(FlowGraph graph) {

		BruteForceColocationGraph collocationGraph = new BruteForceColocationGraph(graph, system);
		List<ColocationGraphNode> maxIndependentSet = new ArrayList<ColocationGraphNode>();
		switch (greedyType) {
		case GWMAX:
			maxIndependentSet = gmaxFramework(collocationGraph);
			break;
		default:
			maxIndependentSet = gminFramework(collocationGraph);
			break;
		}

		return maxIndependentSet;
	}
	
	public List<ColocationGraphNode> select_layer(FlowGraph graph) {
		AbstractColocationGraph collocationGraph = new ColocationGraph(graph, system);
		List<ColocationGraphNode> maxIndependentSet = new ArrayList<ColocationGraphNode>();
		
		if(greedyType == GreedyType.LIKE_NAIVE) {
			collocationGraph = new OneLayerColocationGraph(graph, system);
		}
		switch (greedyType) {
		case GWMAX:
			maxIndependentSet = gmaxFramework(collocationGraph);
			break;
		default:
			maxIndependentSet = gminFramework(collocationGraph);
			break;
		}

		return maxIndependentSet;
	}

	public List<ColocationGraphNode> gminFramework(AbstractColocationGraph graph) {
		List<ColocationGraphNode> maxIndependentSet = new ArrayList<ColocationGraphNode>();
		while (!graph.getNodes().isEmpty()) {
			ColocationGraphNode node;
			switch (greedyType) {
			case GWMIN:
				node = gminChoose(graph);
				break;
			case GWMIN2:
				node = gwmin2Choose(graph);
				break;
			case MAXIMUM:
				node = maximumChoose(graph);
				break;
			case MAX_IMPORTANCE:
				node = maximumImportanceChoose(graph);
				break;
			case LIKE_NAIVE:
				node = maximumChoose(graph);
				break;
			default:
				node = gwmin2Choose(graph);
				break;
			}
			maxIndependentSet.add(node);
			graph.deleteAndItsNeighbors(node);
		}
		return maxIndependentSet;
	}
	
	public List<ColocationGraphNode> gmaxFramework(AbstractColocationGraph graph){
		while (graph.getAllEdges().size() != 0) {
			ColocationGraphNode node = gmaxChoose(graph);
			graph.deleteNodeAndEdges(node);
		}
		return graph.getNodes();
	}

	public ColocationGraphNode gminChoose(AbstractColocationGraph graph) {

		ArrayList<ColocationGraphNode> lists = (ArrayList<ColocationGraphNode>) graph.getNodes();

		double value = 0;
		ColocationGraphNode selected = lists.get(0);
		for (ColocationGraphNode node : lists) {
			double comparing = node.getWeight() / (node.getDegree() + 1);
			if (comparing > value && system.isHostable(node)) {
				value = comparing;
				selected = node;
			}

		}
		return selected;
	}

	public ColocationGraphNode gmaxChoose(AbstractColocationGraph graph) {

		ArrayList<ColocationGraphNode> lists = (ArrayList<ColocationGraphNode>) graph.getNodes();

		ColocationGraphNode selected = lists.get(0);
		double value = -1;

		for (ColocationGraphNode node : lists) {
			if (node.getDegree() != 0) {
				double comparing = node.getWeight() / (node.getDegree() * (node.getDegree() + 1));
				if ((comparing < value || value == -1)
						&& system.isHostable(node)) {
					value = comparing;
					selected = node;
				}
			}
		}
		return selected;
	}

	public ColocationGraphNode gwmin2Choose(AbstractColocationGraph graph) {
		ArrayList<ColocationGraphNode> lists = (ArrayList<ColocationGraphNode>) graph.getNodes();

		ColocationGraphNode selected = lists.get(0);
		double value = 0;

		for (ColocationGraphNode node : lists) {
			double comparing = node.getWeight() / graph.getNeighborWeight(node);
			if (comparing > value && system.isHostable(node)) {
				value = comparing;
				selected = node;
			}
		}
		return selected;
	}
	
	public ColocationGraphNode maximumChoose(AbstractColocationGraph graph) { 
		ArrayList<ColocationGraphNode> lists = (ArrayList<ColocationGraphNode>) graph.getNodes();

		ColocationGraphNode selected = lists.get(0);
		double value = 0;

		for (ColocationGraphNode node : lists) {
			double comparing = node.getWeight();
			if (comparing > value && system.isHostable(node)) {
				value = comparing;
				selected = node;
			}
		}
		return selected;
	}
	
	public ColocationGraphNode maximumImportanceChoose(AbstractColocationGraph graph) {
		ArrayList<ColocationGraphNode> lists = (ArrayList<ColocationGraphNode>) graph.getNodes();

		ColocationGraphNode selected = lists.get(0);
		double value = 0;

		for (ColocationGraphNode node : lists) {
			double comparing =  node.getWeight() - (graph.getNeighborWeight(node) / (node.getDegree() + 1) );
			if (comparing > value && system.isHostable(node)) {
				value = comparing;
				selected = node;
			}
		}
		return selected;
	}
}
