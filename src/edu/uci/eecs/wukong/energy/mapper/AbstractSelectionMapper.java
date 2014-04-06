package edu.uci.eecs.wukong.energy.mapper;

import java.util.HashMap;
import java.util.List;

import net.sf.javailp.Linear;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WuDevice;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.LocationConstraint;
import edu.uci.eecs.wukong.util.Util;

public abstract class AbstractSelectionMapper extends AbstractMapper {
	
	protected HashMap<String, String> variables;

	public AbstractSelectionMapper(WukongSystem system, FlowBasedProcess fbp,
			MapType type) {
		super(system, fbp, type);
		variables = new HashMap<String, String>();
		// TODO Auto-generated constructor stub
	}

	public abstract void map();
	
	protected abstract Problem buildProblem();

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
					problem.add(linear, "<=", constraint.getDistance());
				}
			}
		}
	}
	
	/**
	 * Each wudevice's energy consumption should be less than y.
	 * Then we minimize y. It is a solution to resolve the
	 * min(max(E(device)))
	 * 
	 */
	protected void applyUpperBoundConstraints(Problem problem) {
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
			problem.add(linear, "=", 1);
		}
	}

}
