package edu.uci.eecs.wukong.scalability.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;

import edu.uci.eecs.wukong.common.CongestionZone;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.Gateway;
import edu.uci.eecs.wukong.common.Region;
import edu.uci.eecs.wukong.common.WuClass;
import edu.uci.eecs.wukong.common.WuDevice;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.util.Util;
import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.VarType;

public class ScalabilitySelectionMapper extends AbstractRegionMapper {
	private boolean relaxation;
	private Map<String, String> transformedVariables;

	public ScalabilitySelectionMapper(WukongSystem system, FlowBasedProcess fbp,
			MapType type, boolean relaxation, int timeout) {
		super(system, fbp, type, timeout);
		this.relaxation = relaxation;
		this.transformedVariables = new HashMap<String, String>();
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
	protected Problem buildCongestionZoneProblem(CongestionZone zone) {
		Problem problem = new Problem();
		Linear linear = new Linear();

		//add optimization target function
		linear.add(1.0, 'g');
		problem.setObjective(linear, OptType.MIN);
		
		this.applyUpperBoundConstraints(zone, problem);
		
		this.applyRegionWuClassConstraints(zone, problem);
		
		if(this.type == MapType.WITH_LATENCY) {
			this.applyEndToEndLatencyConstraints(zone, problem);
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
		
		return problem;
	}


	/**
	 * Transform the minmax problem to minimization problem, so that the number of reprogram device under
	 *  each gateway need to be less than g, which is the upper bound variable in objective function min(g).
	 * 
	 */
	protected void applyUpperBoundConstraints(CongestionZone zone, Problem problem) {
		// For each gateway, we create a constraint
		for (Gateway gateway : zone.getGateways()) {
			Linear linear = new Linear();
			List<WuDevice> devices = gateway.getTargetDevice(this.fbp);
			for (WuDevice device : devices) {
				String deviceVariable = Util.generateDeviceVariable(device.getWuDeviceId());
				List<String> wuClassVariables = new ArrayList<String> ();
				for (WuClass wuClass : device.getHostableWuClass(fbp)) {
					wuClassVariables.add(Util.generateVariableId(wuClass.getWuClassId(), wuClass.getDeviceId()));
					applyDeviceConstraint(deviceVariable, wuClassVariables, problem);
				}
				
				linear.add(1, deviceVariable);
			}
			linear.add(-1, 'g');
			problem.add(linear, Operator.LE, 0);
		}
	}
	
	/**
	 * Device Constraint is to determine whether a device need to be reprogrammed under the selection result
	 * of WuObjects hosted in the device.
	 * 
	 * d_k = max(x_1_k, x_2_k, x_3_k)
	 * Express as:
	 * 
	 * d_k >= x_1_k
	 * d_k >= x_2_k
	 * d_k >= x_3_k
	 * d_k <= x_1_k + x_2_k + x_3_k
	 * 
	 * @param deviceVariable
	 * @param wuClassVariables
	 */
	private void applyDeviceConstraint(String deviceVariable, List<String> wuClassVariables, Problem problem) {
		for (String wuClassVariable : wuClassVariables) {
			Linear linear = new Linear();
			linear.add(1, deviceVariable);
			linear.add(-1, wuClassVariable);
			problem.add(linear, Operator.GE, 0);
		}
		
		Linear linear = new Linear();
		linear.add(1, deviceVariable);
		for (String wuClassVariable : wuClassVariables) {
			linear.add(-1, wuClassVariable);
		}
		problem.add(linear, Operator.LE, 0);
	}

	@Override
	protected void applyResult(CongestionZone zone, Result result) {
		
	}

	protected void applyRegionWuClassConstraints(CongestionZone zone, Problem problem) {
		Iterator<Region> regIter = zone.getRegions().iterator();
		
		while (regIter.hasNext()) {
			Region region = regIter.next();
			Map<Integer, List<WuDevice>> map = region.getWuClassToDeviceMap();
			Set<Integer> classes = map.keySet();
			for(Integer classId : classes) {
				List<WuDevice> devices = map.get(classId);
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
	
	@Override
	protected void applyEndToEndLatencyConstraints(CongestionZone zone, Problem problem) {
		
	}
}
