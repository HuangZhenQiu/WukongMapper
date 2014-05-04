package edu.uci.eecs.wukong.util;

import edu.uci.eecs.wukong.common.CollocationGraph;
import edu.uci.eecs.wukong.common.CollocationGraphNode;
import edu.uci.eecs.wukong.common.FlowGraph;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.WukongSystem;

import java.util.ArrayList;
import java.util.List;

public class WeightedIndependentSetSelector {

	WukongSystem system;

	public WeightedIndependentSetSelector(WukongSystem system) {
		this.system = system;
	}

	public List<Edge> select(FlowGraph graph) {

		// graph.print();
		// List<Edge> edges = new LinkedList<Edge>();
		CollocationGraph collocationGraph = new CollocationGraph(graph, system);
//		gmin2(collocationGraph);
//		gmin(collocationGraph);
		CollocationGraph ans = gmax(collocationGraph);
		ans.print();
		// collocationGraph.
		return null;
	}

	public void gmin(CollocationGraph graph) {
		ArrayList<CollocationGraphNode> max_independent_set = new ArrayList<CollocationGraphNode>();
		CollocationGraph to_be_compute = graph;

		while (to_be_compute.getNodes().size() != 0) {
			CollocationGraphNode node = gminChoose(to_be_compute);
			max_independent_set.add(node);
			System.out.println("choose" + node.getNodeId());
			to_be_compute.deleteAndItsNeighbors(node);
		}
	}

	public void gmin2(CollocationGraph graph) {
		ArrayList<CollocationGraphNode> max_independent_set = new ArrayList<CollocationGraphNode>();
		CollocationGraph to_be_compute = graph;

		while (to_be_compute.getNodes().size() != 0) {
			CollocationGraphNode node = gwmin2Choose(to_be_compute);
			max_independent_set.add(node);
			System.out.println("choose" + node.getNodeId());
			to_be_compute.deleteAndItsNeighbors(node);
		}
	}

	public CollocationGraph gmax(CollocationGraph graph) {
		CollocationGraph to_be_compute = graph;
		while (to_be_compute.getEdges().size() != 0) {
			CollocationGraphNode node = gmaxChoose(to_be_compute);
			to_be_compute.deleteNodeAndEdges(node);
		}
		return to_be_compute;
	}

	public CollocationGraphNode gminChoose(CollocationGraph graph) {

		ArrayList<CollocationGraphNode> lists = (ArrayList<CollocationGraphNode>) graph
				.getNodes();

		double value = 0;
		CollocationGraphNode selected = lists.get(0);
		for (CollocationGraphNode node : lists) {
			double comparing = node.getWeight() / (graph.getDegree(node) + 1);

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
			if (graph.getDegree(node) != 0) {
				double comparing = node.getWeight()
						/ (graph.getDegree(node) * (graph.getDegree(node) + 1));
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
