package edu.uci.eecs.wukong.energy.mapper;

import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.VarType;

import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WuDevice;
import edu.uci.eecs.wukong.common.WukongSystem;

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
 * 
 *    
 * @author Peter
 *
 */

public class DistanceAwareSelectionBasedMapper extends AbstractSelectionMapper{
	private boolean relaxation;
	private HashMap<String, String> transformedVariables;
	private HashMap<Integer, ImmutableList<WuDevice>> candidates;
	
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
		// TODO Auto-generated method stub
		
	}
	
	private void applyOptimizationGoal(Problem problem) {
		
	}
	
	/**
	 * 
	 * @param problem
	 */
	private void applyTransformedConstraints(Problem problem) {
		
	}
}
