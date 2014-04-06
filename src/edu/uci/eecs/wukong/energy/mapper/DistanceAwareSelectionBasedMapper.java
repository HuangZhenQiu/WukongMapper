package edu.uci.eecs.wukong.energy.mapper;

import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;

/**
 * It is the algorithm for MEDES 2014 paper which consider the energy consumption 
 * of transmission distance.
 * 
 * Use rule to reduce Mixed Integer None-linear Programming Problem to MIP problem.
 * 
 * Assume that one link of lij FBP can map to a set H<i,j> of pair of devices p<dn, dm> 
 * within the system. Then we can define the optimization problem as below:
 * 
 * 
 *    
 * @author Peter
 *
 */

public class DistanceAwareSelectionBasedMapper extends AbstractSelectionMapper{
	private boolean relaxation;
	
	public DistanceAwareSelectionBasedMapper(WukongSystem system,
			FlowBasedProcess fbp, MapType type, boolean relaxation) {
		super(system, fbp, type);
		this.relaxation = relaxation;
		// TODO Auto-generated constructor stub
	}

	public void map() {
		
	}

	@Override
	protected Problem buildProblem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void applyResult(Result result) {
		// TODO Auto-generated method stub
		
	}
}
