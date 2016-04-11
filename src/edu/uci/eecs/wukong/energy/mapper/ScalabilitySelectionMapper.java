package edu.uci.eecs.wukong.energy.mapper;

import java.util.List;

import edu.uci.eecs.wukong.common.CongestionZone;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WukongSystem;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;

public class ScalabilitySelectionMapper extends AbstractRegionMapper {


	public ScalabilitySelectionMapper(WukongSystem system, FlowBasedProcess fbp,
			MapType type, int timeout) {
		super(system, fbp, type, timeout);
		// TODO Auto-generated constructor stub
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

		List<CongestionZone> zones = this.system.getCongestionZones(this.fbp);
		
		// In region based mapping, we probably need to map to multiple congestion zones
		// for each of which we need to create a ILP problem and resolve it.
		for (CongestionZone zone : zones) {
			Problem problem = buildCongestionZoneProblem(zone);
			Solver solver = factory.get(); // you should use this solver only once for one problem
			Result result = solver.solve(problem);
			
			//System.out.println(result);
			applyResult(zone, result);
			//System.out.println("System total energy consumption is:" + system.getTotalEnergyConsumption());
		}
		
		return true;
	}

	@Override
	protected void applyUpperBoundConstraints(CongestionZone zone, Problem problem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Problem buildCongestionZoneProblem(CongestionZone zone) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void applyResult(CongestionZone zone, Result result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void applyRegionWuClassConstraints(CongestionZone zone, Problem problem) {
		// TODO Auto-generated method stub
		
	}
}
