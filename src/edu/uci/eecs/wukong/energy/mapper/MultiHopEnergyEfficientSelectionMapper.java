package edu.uci.eecs.wukong.energy.mapper;

import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;


public class MultiHopEnergyEfficientSelectionMapper extends AbstractSelectionMapper {

	public MultiHopEnergyEfficientSelectionMapper(WukongSystem system,
			FlowBasedProcess fbp, MapType type, int timeout) {
		super(system, fbp, type, timeout);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void applyUpperBoundConstraints(Problem problem) {
		// TODO Auto-generated method stub
		
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
