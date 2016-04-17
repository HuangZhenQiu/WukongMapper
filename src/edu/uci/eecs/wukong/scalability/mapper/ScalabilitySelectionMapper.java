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
import edu.uci.eecs.wukong.common.Path;
import edu.uci.eecs.wukong.common.Region;
import edu.uci.eecs.wukong.common.Region.DevicePair;
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
			variables.clear();
			System.out.println("Resolving mapping in congestion zone: " + zone.getZoneId());
			Problem problem = buildCongestionZoneProblem(zone);
			Solver solver = factory.get(); // you should use this solver only once for one problem
			Result result = solver.solve(problem);
			
			if (result != null) {
				//System.out.println(result);
				applyResult(zone, result);
				//System.out.println("System total energy consumption is:" + system.getTotalEnergyConsumption());
			}
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
		
		problem.setVarType('g', VarType.REAL);
		
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
					String wuClassVariable = Util.generateVariableId(wuClass.getWuClassId(), device.getWuDeviceId());
					wuClassVariables.add(wuClassVariable);
					variables.put(wuClassVariable, wuClassVariable);
				}
				applyDeviceConstraint(deviceVariable, wuClassVariables, problem);
				variables.put(deviceVariable, deviceVariable);
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
		Set<String> variableIds= variables.keySet();
		for(String variableId : variableIds) {
			if(result.getBoolean(variableId) && Util.isMappingVariable(variableId)) {
				Integer wuClassId = Util.getWuClassIdFromVariableId(variableId);
				Integer wuDeviceId = Util.getWuDeviceIdFromVariableId(variableId);
				System.out.println("Deploy wuClassId " + wuClassId + " at device" + wuDeviceId);
				system.deployComponent(wuDeviceId, wuClassId);
				fbp.getWuClass(wuClassId).deploy(system.getDevice(wuDeviceId));
			}
		}
		
		latencyHops.add(fbp.getLatencyHop(MAX_HOP));
		fbp.reset();
		
		System.out.println("g value : " + result.get('g'));
		
	}

	protected void applyRegionWuClassConstraints(CongestionZone zone, Problem problem) {
		Iterator<Region> regIter = zone.getRegions().iterator();
		
		while (regIter.hasNext()) {
			Region region = regIter.next();
			Map<Integer, List<WuDevice>> map = region.getWuClassToDeviceMap();
			for(WuClass wuClass : fbp.getAllComponents()) {
				List<WuDevice> devices = map.get(wuClass.getWuClassId());
				Linear linear = new Linear();
				for(WuDevice device : devices) {
					String varName = Util.generateVariableId(wuClass.getWuClassId(), device.getWuDeviceId());
					linear.add(1, varName);
					variables.put(varName, varName);
				}
				problem.add(linear, Operator.EQ, 1);
			}
		}
	}
	
	/**
	 * Latency(Path) = Sigma(Latency(Link_i_j))    for Link_i_j belongs to Path
	 * O(L_i_j) represents the inner gateway device pair set that can host link L_i_j
	 * R(L_i_j) represetns the cross gateway device pair set that can host link L_i_j
	 * 
	 * Latency(L_i_j) =     Sigma(X_i_n *X_j_m)     +    2 * Sigma(X_i_n * X_j_m)
	 *                  P_n_m belongs tp O(L_i_j)      P_n_m belongs tp R(L_i_j) 
	 * 
	 */
	protected void applyEndToEndLatencyConstraints(CongestionZone zone, Problem problem) {
		List<Path> paths = this.fbp.getDominatePaths(10);
		for (Region region : zone.getRegions()) {
			Linear linear = new Linear();
			for (Path path : paths) {
				List<WuClass> classes = path.getPathNodes();
				for (int i = 0; i < classes.size() - 1; i++) {
					List<DevicePair> innerPair = region.getInnerGatewayHostPair(classes.get(i), classes.get(i+1));
					for (DevicePair pair : innerPair) {
						String sourceVariable =
								Util.generateVariableId(classes.get(i).getWuClassId(), pair.start.getWuDeviceId());
						String destVariable =
								Util.generateVariableId(classes.get(i + 1).getWuClassId(), pair.end.getWuDeviceId());
						String transformed = Util.generateTransformedVariableId(classes.get(i).getWuClassId(), pair.start.getWuDeviceId(),
								classes.get(i+1).getWuClassId(), pair.end.getWuDeviceId());
						Util.applyTransformedConstraints(problem, sourceVariable, destVariable, transformed);
						linear.add(1, transformed);
					}
					
					
					List<DevicePair> crossPair = region.getCrossGatewayHostPair(classes.get(i), classes.get(i+1));
					
					for (DevicePair pair : crossPair) {
						String sourceVariable =
								Util.generateVariableId(classes.get(i).getWuClassId(), pair.start.getWuDeviceId());
						String destVariable =
								Util.generateVariableId(classes.get(i + 1).getWuClassId(), pair.end.getWuDeviceId());
						String transformed = Util.generateTransformedVariableId(classes.get(i).getWuClassId(), pair.start.getWuDeviceId(),
								classes.get(i+1).getWuClassId(), pair.end.getWuDeviceId());
						Util.applyTransformedConstraints(problem, sourceVariable, destVariable, transformed);
						linear.add(2, transformed);
					}
				}
			}
			if (!paths.isEmpty()) {
				problem.add(linear, Operator.LE, 11);
			}
		}
	}
}
