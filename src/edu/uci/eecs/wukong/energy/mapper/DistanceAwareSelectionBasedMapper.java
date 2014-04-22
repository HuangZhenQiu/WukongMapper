package edu.uci.eecs.wukong.energy.mapper;

import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Operator;
import net.sf.javailp.Result;
import net.sf.javailp.VarType;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
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
	private HashMap<String, String> transformedVariables;
	
	public DistanceAwareSelectionBasedMapper(WukongSystem system,
			FlowBasedProcess fbp, MapType type, int timeout, boolean relaxation) {
		super(system, fbp, type, timeout);
		this.relaxation = relaxation;
		this.transformedVariables = new HashMap<String, String> ();
	}

	@Override
	protected Problem buildProblem() {
		Problem problem = new Problem();
		
		//add optimization target function
		this.applyOptimizationGoal(problem);
		
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
		
		//System.out.println(problem.toString());
		
		return problem;
	}

	@Override
	protected void applyResult(Result result) {
		// TODO Distinguish the ILP and relaxed Linear Programming cases
		
	}
	
	@Override
	protected void applyWuDeviceEnergyConstraints(Problem problem) {
		
	}

	
	private void applyOptimizationGoal(Problem problem) {
		
		Linear linear = new Linear();

		//add optimization target function
		linear.add(1, 'y');
		problem.setObjective(linear, OptType.MIN);
		
		//Go through every link of the FBP
		for(Edge edge : this.fbp.getEdges()) {
			Integer sourceClassId = edge.getInWuClass().getWuClassId();
			Integer destClassId = edge.getOutWuClass().getWuClassId();
			ImmutableList<WuDevice> sourceCandidates = this.system.findWudevice(sourceClassId);
			ImmutableList<WuDevice> destCandidates = this.system.findWudevice(destClassId);
			
			//Go through every possible combination of deployment of the link
			for(WuDevice sd : sourceCandidates) {
				for(WuDevice td : destCandidates) {
					String sourceVariable = Util.generateVariableId(sourceClassId, sd.getWuDeviceId());
					String destVariable = Util.generateVariableId(destClassId, td.getWuDeviceId());
					String transformed = Util.generateTransformedVariableId(sourceClassId, sd.getWuDeviceId(),
							destClassId, td.getWuDeviceId());
					transformedVariables.put(transformed, transformed);
					
					applyTransformedConstraints(problem, sourceVariable, destVariable, transformed);
					
					if(sd.getWuDeviceId() != td.getWuDeviceId()) { //does not deploy on the same device
						linear.add(this.system.getDistance(sd, td), transformed);
					}
				}
			}
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
		linear.add(1, 1);
		linear.add(-1, source);
		linear.add(-1, desct);
		linear.add(1, transformed);
		problem.add(linear, Operator.GE, 0);
		
		
	}
}
