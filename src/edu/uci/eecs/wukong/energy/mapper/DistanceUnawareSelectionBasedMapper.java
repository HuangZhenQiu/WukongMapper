package edu.uci.eecs.wukong.energy.mapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.VarType;
import edu.uci.eecs.wukong.common.FlowBasedProcess;
import edu.uci.eecs.wukong.common.WuDevice;
import edu.uci.eecs.wukong.common.WukongSystem;
import edu.uci.eecs.wukong.common.FlowBasedProcess.Edge;
import edu.uci.eecs.wukong.common.FlowBasedProcess.TYPE;
import edu.uci.eecs.wukong.util.Util;

public class DistanceUnawareSelectionBasedMapper extends AbstractSelectionMapper{
	
	public DistanceUnawareSelectionBasedMapper(WukongSystem system, FlowBasedProcess fbp, MapType type, int timeout) {
		super(system, fbp, type, timeout);
	}
	
	@Override
	protected void applyResult(Result result) {

		Set<String> variableIds= variables.keySet();
		ImmutableList<Integer> classes= fbp.getPreDeployedWuClasses();
		for(String variableId : variableIds) {
			
			if(result.getBoolean(variableId)) {
				Integer wuClassId = Util.getWuClassIdFromVariableId(variableId);
				Integer wuDeviceId = Util.getWuDeviceIdFromVariableId(variableId);
				if(!classes.contains(wuClassId)) {
					this.fbp.deploy(wuClassId, wuDeviceId);
					this.system.deploy(wuDeviceId, wuClassId);
				}
			}
		}
		
		//in case select two sensor in the same device
		fbp.merge();
	}

	@Override
	protected Problem buildProblem() {
		
		Problem problem = new Problem();
		
		//add optimization target function
		Linear linear = new Linear();
		linear.add(1, 'y');
		problem.setObjective(linear, OptType.MIN);
		
		this.applyWuClassConstraints(problem);
		this.applyUpperBoundConstraints(problem);
		
		if(this.type == MapType.BOTH || this.type == MapType.ONLY_LOCATION) {
			this.applyLocationConstraints(problem);
		}
		
		if(this.type == MapType.BOTH || this.type == MapType.ONLY_ENERGY) {
			this.applyWuDeviceEnergyConstraints(problem);
		}
		
		//we check it, even though there maybe no merged edge.
		this.applyMergedWuClassConstraints(problem);
	
		Set<String> varNames = variables.keySet();
		for(String name : varNames) {
			problem.setVarType(name, VarType.BOOL);
		}
		
		//System.out.println(problem.toString());
		
		return problem;
	}
	
	@Override
	protected void applyWuDeviceEnergyConstraints(Problem problem) {
		
	}

	
	/**
	 * Each wudevice's energy consumption should be less than y.
	 * Then we minimize y. It is a solution to resolve the
	 * min(max(E(device)))
	 * 
	 */
	protected void applyUpperBoundConstraints(Problem problem) {
		ImmutableList<WuDevice> wuDevices= system.getDevices();
		
		for(WuDevice device : wuDevices) {
			ImmutableList<Integer> classIds= device.getAllWuObjectId();
			
			Linear linear = new Linear();
			for(Integer classId : classIds) {
				Double energyCost = fbp.getWuClassEnergyConsumption(classId);
				if(energyCost != null) {
					String varName = Util.generateVariableId(classId, device.getWuDeviceId());
					linear.add(energyCost,  varName);
					variables.put(varName, varName);
				}
			}
			linear.add(-1, 'y');
			problem.add(linear, Operator.LE, 0);
		}
		
	}
	
	/**
	 * 
	 * 
	 * @param problem
	 */
	private void applyMergedWuClassConstraints(Problem problem) {
		ImmutableList<Edge> edges= fbp.getMergedEdges();

		for(Edge edge : edges) {
			
			Linear inNodeLinear = new Linear();
			String varName = Util.generateVariableId(edge.getInWuClass().getWuClassId(), edge.getInWuClass().getDeviceId());
			inNodeLinear.add(1, varName);
			variables.put(varName, varName);
			problem.add(inNodeLinear, Operator.EQ, 1);
			
			Linear outNodeLinear = new Linear();
			varName = Util.generateVariableId(edge.getOutWuClass().getWuClassId(), edge.getOutWuClass().getDeviceId());
			outNodeLinear.add(1, varName);
			variables.put(varName, varName);
			problem.add(outNodeLinear, Operator.EQ, 1);
		}
	}
	
	public static void main(String argues[]) {
		
		if(argues.length < 2) {
			System.out.println("Please input paths of two initialization files");
			System.exit(-1);
		}
		
		try {
			
			File fbpConfig = new File(argues[0]);
			BufferedReader fbpConfigReader = new BufferedReader(new FileReader(fbpConfig));
			FlowBasedProcess fbp = new FlowBasedProcess(TYPE.RANDOM);
			
			File systemConfig = new File(argues[1]);
			BufferedReader systemConfigReader = new BufferedReader(new FileReader(systemConfig));
			WukongSystem system = new WukongSystem();
			
			try {
				fbp.initialize(fbpConfigReader);
				system.initialize(systemConfigReader);
				
				DistanceUnawareSelectionBasedMapper mapper = new DistanceUnawareSelectionBasedMapper(system, fbp, MapType.ONLY_LOCATION, 200);
				mapper.map();
				
			} finally {
				fbpConfigReader.close();
				systemConfigReader.close();
				
			}
		
		} catch (IOException e) {
			
			System.out.println(e.toString());
		} 	
	}
}
