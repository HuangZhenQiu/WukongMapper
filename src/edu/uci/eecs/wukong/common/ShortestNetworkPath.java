package edu.uci.eecs.wukong.common;

import java.util.List;
import java.util.ArrayList;

public class ShortestNetworkPath {
	private int nodeNumber;
	private boolean multipleHop;
	private Double[][] graph;
	private Double[][] distance;  // End to End shortest path
	private Integer[][] precedence;
	
	public ShortestNetworkPath(Double[][] graph, Boolean multipleHop) {
		this.nodeNumber = graph.length;
		this.graph = graph;
		if (!multipleHop) {
			distance = graph;
		} else {
			distance = new Double[nodeNumber][nodeNumber];
			precedence = new Integer[nodeNumber][nodeNumber];
			findShortestPath();
		}
	}
	
	/**
	 * It is an implementation of Bellman Ford algorithm
	 * @param graph
	 * @return
	 */
	private void findShortestPath() {
		for(int i=0; i<nodeNumber; i++) {
			for(int j=0; j<nodeNumber; j++) {
				distance[i][j] = graph[i][j];
				
				if (graph[i][j] != Double.MAX_VALUE) {
					precedence[i][j] = i;
				} else {
					precedence[i][j] = -1;
				}
			}
		}
		
		for(int k=0; k<nodeNumber; k++) {
			for (int i=0; i<nodeNumber; i++) {
				for (int j=0; j<nodeNumber; j++) {
					if(graph[i][k] !=Double.MAX_VALUE && graph[k][j] !=Double.MAX_VALUE && distance[i][j] > graph[i][k] + graph[k][j]) {
						distance[i][j] = graph[i][k] + graph[k][j];
						precedence[i][j] = k;
						//distance[j][i] = graph[i][k] + graph[k][j];
					}
				}
			}
		}
	}
	
	public Double getShortestDistance(int start, int end) {
		return distance[start][end];
	}
	
	public List<Double> getDistanceOnShortestPath(int start, int end) {
		List<Double> distances = new ArrayList<Double>();
		
		if(start > nodeNumber - 1 || end > nodeNumber - 1) {
			return distances;
		}
		
		if(multipleHop) {
			while(start != precedence[start][end]) {
				distances.add(distance[start][end]);
				end = precedence[start][end];
			}
			distances.add(distance[start][end]);
		} else {
			distances.add(distance[start][end]);
		}
		
		return distances;
	}
}