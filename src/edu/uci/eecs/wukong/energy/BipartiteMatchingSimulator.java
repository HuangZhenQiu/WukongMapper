package edu.uci.eecs.wukong.energy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.uci.eecs.wukong.bipartite.ServiceBipartiteGraph;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WuClass;
import edu.uci.eecs.wukong.common.WuClassHierarchy;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;
import edu.uci.eecs.wukong.util.FlowBasedProcessFactory;
import edu.uci.eecs.wukong.util.GraphGenerator;
import edu.uci.eecs.wukong.util.Util;
import edu.uci.eecs.wukong.util.WuKongSystemFactory;


public class BipartiteMatchingSimulator {

	private FlowBasedProcessFactory fbpFactory;
	
	private WuKongSystemFactory wukongFactory;
	private int simulation_iteration = 100; 
	private int dimension = 5;
	private int class_number = 20;
	
	public BipartiteMatchingSimulator() {
		//WukongProperties.getProperty();
		this.fbpFactory = new FlowBasedProcessFactory(10, class_number, 100 /**distance range**/, 100 /**weight**/);
		this.wukongFactory = new WuKongSystemFactory(class_number, 20, 10, 100);
	}
	
	public void run() {
		double[] all_solution = new double[simulation_iteration];
		double[] all_solution_2 = new double[simulation_iteration];
		for(int i = 0; i< simulation_iteration; i++){
			System.out.println("Iteration:" + i);
			FlowBasedProcess fbp = fbpFactory.createFlowBasedProcess(TYPE.RANDOM, dimension, 100);
			WukongSystem system = wukongFactory.createRandomWukongSystem(30, 20, dimension);
			
			WuClassHierarchy hierarchy = new WuClassHierarchy(class_number);
			
			ServiceBipartiteGraph bipartiteGraph = new ServiceBipartiteGraph(fbp, system, hierarchy, dimension);
			double total_score = bipartiteGraph.ILP_matching();
			all_solution[i] = total_score;
			double total_greedy_score = bipartiteGraph.greedy_matching();
			all_solution_2[i] = total_greedy_score;
		}
		
		double avr_ilp = 0;
		double avr_greedy = 0;
		for(int i=0;i<simulation_iteration;i++){
			avr_ilp += all_solution[i] / (double)simulation_iteration;
			avr_greedy += all_solution_2[i] / (double)simulation_iteration;
		}
		
		System.out.println("Total result:" + avr_ilp + ", " + avr_greedy);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BipartiteMatchingSimulator simulator = new BipartiteMatchingSimulator();
		simulator.run();
	}

}
