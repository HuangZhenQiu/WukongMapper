package edu.uci.eecs.wukong.scalability;

import edu.uci.eecs.wukong.util.FlowBasedProcessFactory;
import edu.uci.eecs.wukong.util.WuKongSystemFactory;

public class ScalabilitySimulator {
	private FlowBasedProcessFactory fbpFactory;
	
	private WuKongSystemFactory wukongFactory;
	
	public ScalabilitySimulator() {
		//WukongProperties.getProperty();
		this.fbpFactory = new FlowBasedProcessFactory(10, 20, 100 /**distance range**/, 100 /**weight**/);
		this.wukongFactory = new WuKongSystemFactory(20, 20, 10, 100, 0, 0);
	}
	
	public void run() {
		
	}
	
	public void main() {
		ScalabilitySimulator simulator = new ScalabilitySimulator();
		simulator.run();
	}
}
