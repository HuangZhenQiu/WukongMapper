package edu.uci.eecs.wukong.scalability.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import edu.uci.eecs.wukong.common.CongestionZone;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.Gateway;
import edu.uci.eecs.wukong.common.Path;
import edu.uci.eecs.wukong.common.Region;
import edu.uci.eecs.wukong.common.Region.DevicePair;
import edu.uci.eecs.wukong.common.Variable;
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
				// System.out.println(result);
				applyResult(zone, result);
				// System.out.println("System total energy consumption is:" + system.getTotalEnergyConsumption());
			} else {
				System.out.println("Can't find solution");
				latencyHops.add(-1);
				break;
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
				
				// Physical WuObjects needed in the fbp
				for (WuClass wuClass : device.getHostableWuClass(fbp)) {
					String wuClassVariable = Util.generateVariableId(wuClass.getWuClassId(), device.getWuDeviceId());
					wuClassVariables.add(wuClassVariable);
					variables.put(wuClassVariable, wuClassVariable);
				}
				
				// Virtual WuClass can be deployed to any wuclass
				for (WuClass virtualClass : fbp.getVirtualWuClasses()) {
					String wuClassVariable = Util.generateVariableId(virtualClass.getWuClassId(), device.getWuDeviceId());
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
		if (!relaxation) {
			Set<String> variableIds = variables.keySet();
			for(String variableId : variableIds) {
				if(result.getBoolean(variableId) && Util.isMappingVariable(variableId)) {
					Integer wuClassId = Util.getWuClassIdFromVariableId(variableId);
					Integer wuDeviceId = Util.getWuDeviceIdFromVariableId(variableId);
					// System.out.println("Deploy wuClassId " + wuClassId + " at device" + wuDeviceId);
					system.deployComponent(wuDeviceId, fbp.getWuClass(wuClassId));
					fbp.getWuClass(wuClassId).deploy(system.getDevice(wuDeviceId));
				}
			}
		} else {
			Map<Integer, List<Variable>> roundMap = roundUp(result);
			for (Integer wuClassId : roundMap.keySet()) {
				List<Variable> variables = roundMap.get(wuClassId);
				for (int i = 0; i< zone.getRegionNumber(); i++) {
					String variableId = variables.get(i).getName();
					Integer wuDeviceId = Util.getWuDeviceIdFromVariableId(variableId);
					// System.out.println("Deploy wuClassId " + wuClassId + " at device" + wuDeviceId);
					system.deployComponent(wuDeviceId, fbp.getWuClass(wuClassId));
					fbp.getWuClass(wuClassId).deploy(system.getDevice(wuDeviceId));
				}
			}	
			
			// use heuristic algorithm to improve runtime latency, if it is possible.
			relocate(fbp);
		}

		if (!fbp.isPhyscallyDeployed()) {
			System.out.println("Didn't find feasible solution!!!");
		}
		
		latencyHops.add(fbp.getLatencyHop(MAX_HOP));
		fbp.reset();
		
		System.out.println("g value : " + result.get('g'));
	}
	
	/**
	 * If we use linear relaxation, we need to round up the result generated by Linear Programming.
	 * 
	 */
	private Map<Integer, List<Variable>> roundUp(Result result) {
		Map<Integer, List<Variable>> variableMap = new HashMap<Integer, List<Variable>> ();
		for (String var : this.variables.values()) {
			if (Util.isMappingVariable(var)) {
				Integer wuClassId = Util.getWuClassIdFromVariableId(var);
				Double value = (Double) result.get(var);
				Variable variable = new Variable(var, value);
				
				if (!variableMap.containsKey(wuClassId)) {
					List<Variable> variables = new ArrayList<Variable> ();
					variableMap.put(wuClassId, variables);
				}
				
				variableMap.get(wuClassId).add(variable);
			}
		}
		
		for (List<Variable> list : variableMap.values()) {
			Collections.sort(list);
		}
		
		return variableMap;
	}
	
	/**
	 * Since round up can't guarantee optimal solution. We may use heuristic algorithm to improve
	 * the result generated by round up algorithm.
	 * 
	 */
	private void relocate(FlowBasedProcess fbp) {
		List<Path> paths = fbp.getDominatePaths(MAX_HOP);
		Set<WuClass> fixedWuClasses = new HashSet<WuClass> ();  
		Collections.sort(paths);
		// Start from longest path
		for (Path path : paths) {
			if (path.getHops() > MAX_HOP) {
				relocate(path, fixedWuClasses);
				fixedWuClasses.addAll(path.getPathNodes());
			}
		}
	}
	
	private void relocate(Path path, Set<WuClass> fixedWuClasses) {
		List<WuClass> nodes = path.getPathNodes();
		for (int i = 0; i < nodes.size() - 2; i = i + 3) {
			WuClass head = nodes.get(i);
			WuClass tail = nodes.get(i + 2);
			WuClass middle = nodes.get(i + 1);
			
			WuDevice oldDevice = middle.getDevice();
			// If these components are not in the same gateway
			if (head.getDevice().getGateway().equals(tail.getDevice().getGateway())) {
				// If the middle is not in the same gateway
				if (!head.getDevice().getGateway().equals(middle.getDevice().getGateway())) {
					if (head.getDevice().equals(tail.getDevice())) {
						// Firstly look up the device
						if (head.getDevice().deployable(middle.getWuClassId())) {
							// Release old device
							middle.undeploy();
							middle.deploy(head.getDevice());
						} else {
							if (head.getDevice().getGateway().deploy(middle)) {
								oldDevice.undeployComponent(middle);
							}
						}
					} else {
						if (oldDevice != head.getDevice() &&  oldDevice != tail.getDevice()) {
							if (head.getDevice().deployable(middle.getWuClassId())) {
								middle.undeploy();
								middle.deploy(head.getDevice());
							} else if (tail.getDevice().deployable(middle.getWuClassId())) {
								middle.undeploy();
								middle.deploy(tail.getDevice());
							} else if (head.getDevice().getGateway().deploy(middle)) {
								oldDevice.undeployComponent(middle);
							}
						}
					}
				} else {
					// All three are in the same gateway, try to collocate them.
					if (oldDevice != head.getDevice() &&  oldDevice != tail.getDevice()) {
						if (head.getDevice().deployable(middle.getWuClassId())) {
							middle.undeploy();
							middle.deploy(head.getDevice());
						} else if (tail.getDevice().deployable(middle.getWuClassId())) {
							middle.undeploy();
							middle.deploy(tail.getDevice());
						}
					}
				}
			} else {
				if (!middle.getDevice().getGateway().equals(head.getDevice().getGateway()) &&
						!middle.getDevice().getGateway().equals(tail.getDevice())) {
					if (head.getDevice().deployable(middle.getWuClassId())) {
						middle.undeploy();
						middle.deploy(head.getDevice());
					} else if (tail.getDevice().deployable(middle.getWuClassId())) {
						middle.undeploy();
						middle.deploy(tail.getDevice());
					} else if (head.getDevice().getGateway().deploy(middle)) {
						oldDevice.undeployComponent(middle);
					} else if (tail.getDevice().getGateway().deploy(middle)) {
						oldDevice.undeployComponent(middle);
					}
				}
			}
		}
	}

	protected void applyRegionWuClassConstraints(CongestionZone zone, Problem problem) {
		Iterator<Region> regIter = zone.getRegions().iterator();
		

		while (regIter.hasNext()) {
			Region region = regIter.next();
			Map<Integer, List<WuDevice>> map = region.getWuClassToDeviceMap();
			// Physical WuClass
			for(WuClass wuClass : fbp.getAllComponents()) {
				if (!wuClass.isVirtual()) {
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
			
			// Virtual WuClass
			for (WuClass virtualClass : fbp.getVirtualWuClasses()) {
				Linear linear = new Linear();
				for (WuDevice device : region.getAllDevices()) {
					String varName = Util.generateVariableId(virtualClass.getWuClassId(), device.getWuDeviceId());
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
						this.variables.put(sourceVariable, sourceVariable);
						this.variables.put(destVariable, destVariable);
						this.transformedVariables.put(transformed, transformed);
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
						this.variables.put(sourceVariable, sourceVariable);
						this.variables.put(destVariable, destVariable);
						this.transformedVariables.put(transformed, transformed);
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
