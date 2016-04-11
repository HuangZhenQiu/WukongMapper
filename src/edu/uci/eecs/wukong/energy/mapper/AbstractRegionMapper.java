package edu.uci.eecs.wukong.energy.mapper;

import java.util.HashMap;

import edu.uci.eecs.wukong.common.CongestionZone;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryLpSolve;

public abstract class AbstractRegionMapper extends AbstractMapper {
	
	protected HashMap<String, String> variables;
	protected SolverFactory factory;

	public AbstractRegionMapper(WukongSystem system,
			FlowBasedProcess fbp, MapType type, int timeout) {
		super(system, fbp, type);
		this.variables = new HashMap<String, String>();
		this.factory = new SolverFactoryLpSolve();
		this.factory.setParameter(Solver.VERBOSE, 0);
		this.factory.setParameter(Solver.TIMEOUT, timeout);
	}

	@Override
	public boolean map() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * The constraint for min-max formulation
	 * 
	 * @param zone
	 * @param problem
	 */
	protected abstract void applyUpperBoundConstraints(CongestionZone zone, Problem problem);
	
	/**
	 * The constraint for limit type of Wuclasses chosen in each region.
	 * 
	 * @param zone
	 * @param problem
	 */
	protected abstract void applyRegionWuClassConstraints(CongestionZone zone, Problem problem);
	
	/**
	 * The entry point for setting the IP constraints for a mapping problem.
	 * 
	 * @return
	 */
	protected abstract Problem buildCongestionZoneProblem(CongestionZone zone);
	
	/**
	 * 
	 * Apply selection result into Wukong System, then caculate the final energy consumption
	 * 
	 * @param result
	 */
	protected abstract void applyResult(CongestionZone zone, Result result);
}
