package edu.uci.eecs.wukong.energy.mapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.HashMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryLpSolve;
import net.sf.javailp.VarType;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WuDevice;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.LocationConstraint;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;
import edu.uci.eecs.wukong.util.Util;

public class SelectionBasedMapper extends AbstractMapper{
	
	protected SolverFactory factory;
	protected HashMap<String, String> variables;
	
	public SelectionBasedMapper(WukongSystem system, FlowBasedProcess fbp, MapType type, int timeout) {
		super(system, fbp, type);
		this.factory = new SolverFactoryLpSolve();
		this.factory.setParameter(Solver.VERBOSE, 0);
		this.factory.setParameter(Solver.TIMEOUT, timeout);
		this.variables = new HashMap<String, String>();
	}
	
	public void map() {
		
		if (system == null || fbp == null) {
			System.out.println("Wukong System or FBP is null.");
			return;
		}
		
		if (system.getDeviceNumber() == 0 || system.getWuClassNunber() == 0 ) {
			System.out.println("There is no device or wuclass within the wuclass system.");
			return;
		}
		
		if (fbp.getEdgeNumber() == 0) {
			System.out.println("There is no edge within the fbp.");
		}
		

		Problem problem = buildProblem();
		Solver solver = factory.get(); // you should use this solver only once for one problem
		Result result = solver.solve(problem);
		
		//System.out.println(result);
		applyResult(result);
		//System.out.println("System total energy consumption is:" + system.getTotalEnergyConsumption());
	}
	
	protected void applyResult(Result result) {

		Set<String> variableIds= variables.keySet();
		ImmutableList<Integer> classes= fbp.getPreDeployedWuClasses();
		for(String variableId : variableIds) {
			
			if(result.getBoolean(variableId)) {
				Integer wuClassId = Util.getWuClassIdFromVariableId(variableId);
				Integer wuDeviceId = Util.getWuDeviceIdFromVariableId(variableId);
				if(!classes.contains(wuClassId)) {
					this.fbp.deploy(wuClassId, wuDeviceId);
					this.system.deploy(wuDeviceId, wuClassId);
				}
			}
		}
		
		//in case select two sensor in the same device
		fbp.merge();
	}

	
	protected Problem buildProblem() {
		
		Problem problem = new Problem();
		
		//add optimization target function
		Linear linear = new Linear();
		linear.add(1, 'y');
		problem.setObjective(linear, OptType.MIN);
		
		this.applyWuClassConstraints(problem);
		this.applyUpperBoundConstraints(problem);
		
		if(this.type == MapType.BOTH || this.type == MapType.ONLY_LOCATION) {
			this.applyLocationConstraints(problem);
		}
		
		if(this.type == MapType.BOTH || this.type == MapType.ONLY_ENERGY) {
			this.applyWuDeviceEnergyConstraints(problem);
		}
		
		//we check it, even though there maybe no merged edge.
		this.applyMergedWuClassConstraints(problem);
	
		Set<String> varNames = variables.keySet();
		for(String name : varNames) {
			problem.setVarType(name, VarType.BOOL);
		}
		
		//System.out.println(problem.toString());
		
		return problem;
	}

	
	/**
	 * 
	 * Each Object selected for the functionality of particular wuclass in the fbp should 
	 * confirm to the location constraints like below:
	 *    djk * x_i_j <= Dik
	 *    
	 * djk is the distance between the device j and landmark k. Dik is the distance constraint
	 * from the wuclass i to landmark k;
	 */
	private void applyLocationConstraints(Problem problem) {
		ImmutableList<WuDevice> wuDevices= system.getDevices();
		
		for(WuDevice device : wuDevices) {
			ImmutableList<Integer> classIds= device.getAllWuObjectId();

			for(Integer classId : classIds) {
				Linear linear =  new Linear();
				LocationConstraint constraint = fbp.getLocationConstraintByWuClassId(classId);
				if(constraint != null) {
					Double distance = device.getDistance(constraint.getLandMarkId());
					String varName = Util.generateVariableId(classId, device.getWuDeviceId());
					linear.add(distance, varName);
					variables.put(varName, varName);
					problem.add(linear, "<=", constraint.getDistance());
				}
			}
		}
	}
	
	/* *
	 * Each wudevice's energy consumption should be less than y.
	 * Then we minimize y. It is a solution to resolve the
	 * min(max(E(device)))
	 * 
	 */
	private void applyUpperBoundConstraints(Problem problem) {
		ImmutableList<WuDevice> wuDevices= system.getDevices();
		
		for(WuDevice device : wuDevices) {
			ImmutableList<Integer> classIds= device.getAllWuObjectId();
			
			Linear linear = new Linear();
			for(Integer classId : classIds) {
				Double energyCost = fbp.getWuClassEnergyConsumption(classId);
				if(energyCost != null) {
					String varName = Util.generateVariableId(classId, device.getWuDeviceId());
					linear.add(energyCost,  varName);
					variables.put(varName, varName);
				}
			}
			linear.add(-1, 'y');
			problem.add(linear, "<=", 0);
		}
		
	}
	
	/**
	 * Each WuClass can only be selected only.
	 * @param problem
	 */
	private void applyWuClassConstraints(Problem problem) {
		
		ImmutableMap<Integer, List<WuDevice>> classDeviceMap = system.getWuClassDeviceMap();
		ImmutableSet<Integer> classes = classDeviceMap.keySet();
		for(Integer classId : classes) {
			List<WuDevice> devices = classDeviceMap.get(classId);
			Linear linear = new Linear();
			for(WuDevice device : devices) {
				String varName = Util.generateVariableId(classId, device.getWuDeviceId());
				linear.add(1, varName);
				variables.put(varName, varName);
			}
			problem.add(linear, "=", 1);
		}
	}
	
	private void applyWuDeviceEnergyConstraints(Problem problem) {
		
		ImmutableList<WuDevice> wuDevices= system.getDevices();
		
		for(WuDevice device : wuDevices) {
			ImmutableList<Integer> classIds= device.getAllWuObjectId();
			
			Linear linear = new Linear();
			for(Integer classId : classIds) {
				Double energyCost = fbp.getWuClassEnergyConsumption(classId);
				String varName = Util.generateVariableId(classId, device.getWuDeviceId());
				linear.add(energyCost,  varName);
				variables.put(varName, varName);
			}

			problem.add(linear, "<=", device.getEnergyConstraint());
		}
	}
	
	/**
	 * 
	 * 
	 * @param problem
	 */
	private void applyMergedWuClassConstraints(Problem problem) {
		ImmutableList<Edge> edges= fbp.getMergedEdges();

		for(Edge edge : edges) {
			
			Linear inNodeLinear = new Linear();
			String varName = Util.generateVariableId(edge.getInWuClass().getWuClassId(), edge.getInWuClass().getDeviceId());
			inNodeLinear.add(1, varName);
			variables.put(varName, varName);
			problem.add(inNodeLinear, "=", 1);
			
			Linear outNodeLinear = new Linear();
			varName = Util.generateVariableId(edge.getOutWuClass().getWuClassId(), edge.getOutWuClass().getDeviceId());
			outNodeLinear.add(1, varName);
			variables.put(varName, varName);
			problem.add(outNodeLinear, "=", 1);
		}
	}
	
	public static void main(String argues[]) {
		
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
				
				SelectionBasedMapper mapper = new SelectionBasedMapper(system, fbp, MapType.ONLY_LOCATION, 200);
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
