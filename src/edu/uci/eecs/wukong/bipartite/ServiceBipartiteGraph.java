package edu.uci.eecs.wukong.bipartite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryLpSolve;
import net.sf.javailp.VarType;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WuClass;
import edu.uci.eecs.wukong.common.WuClassHierarchy;
import edu.uci.eecs.wukong.common.WuObject;
import edu.uci.eecs.wukong.common.WukongSystem;

public class ServiceBipartiteGraph {
	
	private List<ServiceBipartiteGraphNode> partitionA;
	private List<ServiceBipartiteGraphNode> partitionB;
	
	private List<ServiceBipartiteGraphEdge> edges;
	private int dimension;
	
	private WuClassHierarchy hierarchy;
	private SolverFactory factory;
	private HashMap<String, String> variables;
	
	public ServiceBipartiteGraph(FlowBasedProcess fbp, WukongSystem system, WuClassHierarchy hierarchy, int dimension) {
		this.factory = new SolverFactoryLpSolve();
		this.factory.setParameter(Solver.VERBOSE, 0);
		this.factory.setParameter(Solver.TIMEOUT, 100);
		
		this.partitionA = new ArrayList<ServiceBipartiteGraphNode>();
		this.partitionB = new ArrayList<ServiceBipartiteGraphNode>();
		this.edges = new ArrayList<ServiceBipartiteGraphEdge>();
		this.dimension = dimension;
		
		this.hierarchy = hierarchy;
		
		
		initPartitionA(fbp);
		initPartitionB(system);
		initMatching();
		
		
	}
	
	public void init() { 
		
	}

	public double ILP_matching(){
		buildProblem();
		Problem problem = buildProblem();
		Solver solver = factory.get(); // you should use this solver only once for one problem
		Result result = solver.solve(problem);
		System.out.println(result);
		
		for (ServiceBipartiteGraphNode nodeLeft : partitionA) {
			for (ServiceBipartiteGraphNode nodeRight : partitionB) {
				String varName = partitionA.indexOf(nodeLeft) + "_" + partitionB.indexOf(nodeRight);
				if(result.getBoolean(varName)){
					System.out.println(varName);
				}
			}
		}
		return result.getObjective().doubleValue();
	}
	
	
	public double greedy_matching(){
		double total_weight = 0;
		for (ServiceBipartiteGraphNode nodeLeft : partitionA) {
			
			double weight = 0;
			int match = 0;
			for (ServiceBipartiteGraphNode nodeRight : partitionB) {
				if(findEdge(nodeLeft, nodeRight).getWeight() > weight && !nodeRight.isMatched()){
					weight = findEdge(nodeLeft, nodeRight).getWeight();
					match = partitionB.indexOf(nodeRight);
				}
			}
			partitionB.get(match).checkMatch();
			total_weight += weight;
//			System.out.println("index " + partitionA.indexOf(nodeLeft) + " match to " + "index "+ match);
		}
		System.out.println("Total weighting score:" + total_weight);
		return total_weight;
	}
	public void initPartitionA(FlowBasedProcess fbp) {
		List<WuClass> wuClasses = fbp.getWuClassList();
		for(WuClass wuClass : wuClasses){
			ServiceBipartiteGraphNode node = new ServiceBipartiteGraphNode(wuClass.getWuClassId());
			node.setWuClass(wuClass);
//			for(int i=0; i< dimension;i++){
//				System.out.print(wuClass.getUserPreferences()[i] +", ");
//			}
//			System.out.println("");
			partitionA.add(node);
		}
	}
	
	public void initPartitionB(WukongSystem system) { 
		List<WuObject> wuObjects = system.getServiceList();
		for(WuObject wuObject : wuObjects ){
			ServiceBipartiteGraphNode node = new ServiceBipartiteGraphNode(wuObject.getWuClassId());
			node.setWuObject(wuObject);
//			for(int i=0; i< dimension;i++){
//				System.out.print(wuObject.getProperties()[i] +", ");
//			}
//			System.out.println("");
			partitionB.add(node);
		}
	}
	
	public void initMatching(){
		for (ServiceBipartiteGraphNode nodeLeft : partitionA) {
			for (ServiceBipartiteGraphNode nodeRight : partitionB) {
				ServiceBipartiteGraphEdge edge = new ServiceBipartiteGraphEdge(nodeLeft, nodeRight);
				edge.setWeight(hierarchy.evaluate(nodeLeft.getWuClass(), nodeRight.getWuObject()));
				edges.add(edge);
			}
		}
	}
	
	public ServiceBipartiteGraphEdge findEdge(ServiceBipartiteGraphNode left, ServiceBipartiteGraphNode right) {
		for(ServiceBipartiteGraphEdge edge: edges) {
			if(edge.getLeft().equals(left) && edge.getRight().equals(right)){
				return edge;
			}
		}
		return null;
	}
	
	public Problem buildProblem() {
		Problem problem = new Problem();
		Linear linear = new Linear();

		//add optimization target function
		for (ServiceBipartiteGraphNode nodeLeft : partitionA) {
			for (ServiceBipartiteGraphNode nodeRight : partitionB) {
				ServiceBipartiteGraphEdge edge = findEdge(nodeLeft, nodeRight);
				linear.add(edge.getWeight(), partitionA.indexOf(nodeLeft)+"_"+partitionB.indexOf(nodeRight));
			}
		}
		problem.setObjective(linear, OptType.MAX);
		
		for (ServiceBipartiteGraphNode nodeLeft : partitionA) {
			Linear linear2 = new Linear();
			
			for (ServiceBipartiteGraphNode nodeRight : partitionB) {
				String varName = partitionA.indexOf(nodeLeft) + "_" + partitionB.indexOf(nodeRight);
				linear2.add(1, varName);
				problem.setVarType(varName,	VarType.BOOL);
			}
			problem.add(linear2, Operator.EQ, 1);
		}
		
		for (ServiceBipartiteGraphNode nodeRight : partitionB) {
			Linear linear2 = new Linear();
			
			for (ServiceBipartiteGraphNode nodeLeft : partitionA) {
				String varName = partitionA.indexOf(nodeLeft) + "_" + partitionB.indexOf(nodeRight);
				linear2.add(1, varName);
				problem.setVarType(varName,	VarType.BOOL);
			}
			problem.add(linear2, Operator.LE, 1);
		}
		
//		System.out.println(problem.toString());
		
		return problem;
	}

	/* 
	 * 
	 * Debug for service bipartite graph
	 * 
	 */
	public void print() {
		System.out.println("PartitionA: ");
		for(ServiceBipartiteGraphNode node: partitionA) {
			System.out.println(node);
		}
		System.out.println("PartitionB: ");
		for(ServiceBipartiteGraphNode node: partitionB) {
			System.out.println(node);
		}
		System.out.println("Matching:");
		for(ServiceBipartiteGraphEdge edge: edges){
			if(edge.getWeight() != 0){
				System.out.println("Matching edge: index: " + partitionA.indexOf(edge.getLeft()) + "wuclassid: "+ edge.getLeft().getClassId() + ", index " + partitionB.indexOf(edge.getRight())+ " wuclassid: "+ edge.getRight().getClassId() +": matching" + edge.getWeight());
			}
		}
	}
}
