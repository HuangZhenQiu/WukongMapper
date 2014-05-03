package edu.uci.eecs.wukong.energy.mapper;

import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Operator;
import net.sf.javailp.Result;
import net.sf.javailp.VarType;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WuClass;
import edu.uci.eecs.wukong.common.WuDevice;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.util.Util;

import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.ImmutableList;

/**
 * It is the algorithm for MEDES 2014 paper which consider the energy consumption 
 * of transmission distance.
 * 
 * Use rule to reduce Mixed Integer None-linear Programming Problem to MIP problem.
 * 
 * Assume that one link of l_i_j FBP can map to a set H<i,j> of pair of devices p<dn, dm> 
 * within the system. Then we can define the optimization problem as below:
 * 
 * Assume we have N classes in a FBP, and M devices in Wukong System.
 * L_i_j is a link within FBP, H(i,j) is a set of device pair <Dn, Dm> that can host the link
 * 
 * x_i_n * x_j_m => y_i_n_j_m
 *
 *
 * min(Segma_ij(Segma_H(i,j)(x_i_n * x_j_m * w_n_m))   1 <= i <= N; 1 <= j <= N;  1<= n <= M; 1 <= m <= M
 * 
 *
 * 
 *    
 * @author Peter
 *
 */

public class DistanceAwareSelectionBasedMapper extends AbstractSelectionMapper{
	private boolean relaxation;
	private double lagestDeviceEnergyConsumption;
	private HashMap<String, String> transformedVariables;
	
	public DistanceAwareSelectionBasedMapper(WukongSystem system,
			FlowBasedProcess fbp, MapType type, int timeout, boolean relaxation) {
		super(system, fbp, type, timeout);
		this.relaxation = relaxation;
		this.lagestDeviceEnergyConsumption = 0.0;
		this.transformedVariables = new HashMap<String, String> ();
	}

	@Override
	protected Problem buildProblem() {
		Problem problem = new Problem();
		Linear linear = new Linear();

		//add optimization target function
		linear.add(1.0, 'y');
		problem.setObjective(linear, OptType.MIN);
		
		this.applyUpperBoundConstraints(problem);
		
		this.applyWuClassConstraints(problem);
		
		if(this.type == MapType.BOTH || this.type == MapType.ONLY_LOCATION) {
			this.applyLocationConstraints(problem);
		}
		
		if(this.type == MapType.BOTH || this.type == MapType.ONLY_ENERGY) {
			this.applyWuDeviceEnergyConstraints(problem);
		}
		
		Set<String> varNames = variables.keySet();
		for(String name : varNames) {
			if(relaxation) {
				problem.setVarType(name, VarType.REAL);
			} else {
				problem.setVarType(name, VarType.BOOL);
			}
		}
		
		Set<String> transformedNames= transformedVariables.keySet();
		for(String name : transformedNames) {
			if(relaxation) {
				problem.setVarType(name, VarType.REAL);
			} else {
				problem.setVarType(name, VarType.BOOL);
			}
		}
		
		//System.out.println(problem.toString());
		
		return problem;
	}

	@Override
	protected void applyResult(Result result) {
		// TODO Distinguish the ILP and relaxed Linear Programming cases
		
		//System.out.println(result.toString());
		Set<String> variableIds= variables.keySet();
		for(String variableId : variableIds) {
			if(result.getBoolean(variableId) && !Util.isTransformedVariable(variableId)) {
				
				Integer wuClassId = Util.getWuClassIdFromVariableId(variableId);
				Integer wuDeviceId = Util.getWuDeviceIdFromVariableId(variableId);
				this.fbp.deploy(wuClassId, wuDeviceId);
				this.system.deploy(wuDeviceId, wuClassId);
			}
		}
		
		//in case select two sensor in the same device
		fbp.merge();
		
		lagestDeviceEnergyConsumption = result.get('y').doubleValue();
		
	}
	
	protected HashMap<String, Boolean> roundUp(Result result) {
		// TODO heuristic algorithm and round up should be defined here
		
		return new HashMap<String, Boolean>();
	}
	
	
	@Override
	protected void applyWuDeviceEnergyConstraints(Problem problem) {
		
	}
	
	@Override
	protected void applyUpperBoundConstraints(Problem problem) {
		//for each device there is a upper bound constraint
		for(WuDevice device : this.system.getDevices()) {

			Linear linear = new Linear();
			
			//add consumption of wuclasses together
			for(WuClass wuClass : device.getHostableWuClass(this.fbp)) {
				ImmutableList<Edge> inEdges = this.fbp.getInEdge(wuClass.getWuClassId());
				ImmutableList<Edge> outEdges = this.fbp.getOutEdge(wuClass.getWuClassId());
				
				//add receiving cost of inlink
				for(Edge edge: inEdges) {
					ImmutableList<WuDevice> outDevices = this.system.getPossibleHostDevice(edge.getInWuClass().getWuClassId());
					for(WuDevice outDevice: outDevices) {
						if(!outDevice.equals(device)) {
							
							String sourceVariable = Util.generateVariableId(edge.getInWuClass().getWuClassId(), outDevice.getWuDeviceId());
							String destVariable = Util.generateVariableId(wuClass.getWuClassId(), device.getWuDeviceId());
							String transformed = Util.generateTransformedVariableId(edge.getInWuClass().getWuClassId(), outDevice.getWuDeviceId(),
									wuClass.getWuClassId(), device.getWuDeviceId());
							
							if(!transformedVariables.containsKey(transformed)) {
								transformedVariables.put(transformed, transformed);
								applyTransformedConstraints(problem, sourceVariable, destVariable, transformed);
							}
							
							
							//add receiving energy on device
							linear.add(Util.getReceivingEnergyConsumption(edge.getDataVolumn()), transformed);
						}
					}
				}
				
				//add transmission cost of outlink
				for(Edge edge: outEdges) {
					ImmutableList<WuDevice> inDevices = this.system.getPossibleHostDevice(edge.getOutWuClass().getWuClassId());
					for(WuDevice inDevice: inDevices) {
						if(!inDevice.equals(device)) {
							
							String sourceVariable = Util.generateVariableId(wuClass.getWuClassId(), device.getWuDeviceId());
							String destVariable = Util.generateVariableId(edge.getOutWuClass().getWuClassId(), inDevice.getWuDeviceId());
							String transformed = Util.generateTransformedVariableId(wuClass.getWuClassId(), device.getWuDeviceId(),
									edge.getOutWuClass().getWuClassId(), inDevice.getWuDeviceId());
							
							if(!transformedVariables.containsKey(transformed)) {
								transformedVariables.put(transformed, transformed);
								applyTransformedConstraints(problem, sourceVariable, destVariable, transformed);
							}
							
							
							//add transmission energy on device
							linear.add(Util.getTransmissionEnergyConsumption(edge.getDataVolumn(), system.getDistance(device, inDevice)), transformed);
						}
					}
				}
			}
			
			linear.add(-1, 'y');
			problem.add(linear, Operator.LE, 0);
		}
	}
	
	/**
	 * 
	 * @param problem
	 * @param source   x_i_n
	 * @param desct    x_j_m
	 * @param transformed   y_i_n_j_m
	 */
	private void applyTransformedConstraints(Problem problem, String source, String desct, String transformed) {
		
		// 1) y_i_n_j_m >= 0
		Linear linear = new Linear();
		linear.add(1, transformed);
		problem.add(linear, Operator.GE, 0);
		
		// 2) x_i - y_i_n_j_m >= 0
		linear = new Linear();
		linear.add(1, source);
		linear.add(-1, transformed);
		problem.add(linear, Operator.GE, 0);
		
		// 3) x_j - y_i_n_j_m >= 0
		linear = new Linear();
		linear.add(1, desct);
		linear.add(-1, transformed);
		problem.add(linear, Operator.GE, 0);
		
		// 4) 1 - x_i_n - x_j_m + y_i_n_j_m  >=0
		linear = new Linear();
		linear.add(-1, source);
		linear.add(-1, desct);
		linear.add(1, transformed);
		problem.add(linear, Operator.GE, -1);
		
		
	}

	public double getLagestDeviceEnergyConsumption() {
		return lagestDeviceEnergyConsumption;
	}

	public void setLagestDeviceEnergyConsumption(
			double lagestDeviceEnergyConsumption) {
		this.lagestDeviceEnergyConsumption = lagestDeviceEnergyConsumption;
	}
	
	
}
