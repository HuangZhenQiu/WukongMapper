package edu.uci.eecs.wukong.energy.mapper;

import java.util.HashMap;
import java.util.List;

import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryLpSolve;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WuDevice;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.LocationConstraint;
import edu.uci.eecs.wukong.util.Util;

public abstract class AbstractSelectionMapper extends AbstractMapper {
	
	protected HashMap<String, String> variables;
	protected SolverFactory factory;

	public AbstractSelectionMapper(WukongSystem system, FlowBasedProcess fbp,
			MapType type, int timeout) {
		super(system, fbp, type);
		this.variables = new HashMap<String, String>();
		this.factory = new SolverFactoryLpSolve();
		this.factory.setParameter(Solver.VERBOSE, 0);
		this.factory.setParameter(Solver.TIMEOUT, timeout);
	}

	@Override
	public boolean map() {
		
		if (system == null || fbp == null) {
			System.out.println("Wukong System or FBP is null.");
			return false;
		}
		
		if (system.getDeviceNumber() == 0 || system.getWuClassNunber() == 0 ) {
			System.out.println("There is no device or wuclass within the wuclass system.");
			return false;
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
		
		return true;
	}
	
	
	/***
	 * It is constraints can be used for energy harvesting, so that devices can have a
	 * upper bound for energy usage.
	 * 
	 * 
	 * @param problem
	 */
	protected void applyWuDeviceEnergyConstraints(Problem problem) {
		
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

			problem.add(linear, Operator.LE, device.getEnergyConstraint());
		}
	}
	
	
	/**
	 * It is the constraints come from transforming min-max problem to min problem
	 * 
	 * @param problem
	 */
	protected abstract void applyUpperBoundConstraints(Problem problem);
	
	/**
	 * The entry point for setting the IP constraints for a mapping problem.
	 * 
	 * @return
	 */
	protected abstract Problem buildProblem();
	
	/**
	 * 
	 * Apply selection result into Wukong System, then caculate the final energy consumption
	 * 
	 * @param result
	 */
	protected abstract void applyResult(Result result);
	

	
	
	/**
	 * 
	 * Each Object selected for the functionality of particular wuclass in the fbp should 
	 * confirm to the location constraints like below:
	 *    djk * x_i_j <= Dik
	 *    
	 * djk is the distance between the device j and landmark k. Dik is the distance constraint
	 * from the wuclass i to landmark k;
	 */
	protected void applyLocationConstraints(Problem problem) {
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
					problem.add(linear, Operator.LE, constraint.getDistance());
				}
			}
		}
	}
	
	/**
	 * Each WuClass can only be selected only.
	 * @param problem
	 */
	protected void applyWuClassConstraints(Problem problem) {
		
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
			problem.add(linear, Operator.EQ, 1);
		}
	}

}
