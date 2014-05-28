package edu.uci.eecs.wukong.energy;

import edu.uci.eecs.wukong.energy.common.CollocationGraphTransformationTest;

public class ColocationGraphTransformationSimulator {
	
	
	private static final int device_number = 50;
	private static final int class_number = 30;
	
	public CollocationGraphTransformationTest getTest(int K, int replica){
		CollocationGraphTransformationTest test = new CollocationGraphTransformationTest(K, replica) {
			protected void runTest(){
				testCollocationGraphTrasformation();
			}
		};
		test.setClassNumber(class_number);
		test.setDeviceNumber(device_number);
		
		return test;
	}
	public void run() {

		System.out.println("Comparing replica: ");
		/* fake running */
		CollocationGraphTransformationTest test = getTest(4, 2);
		test.run();
		
		/* K = 4 and replica = 2 */
		test = getTest(4, 2);
		test.run();
		
		/* K = 4 and replica = 3 */
		test = getTest(4, 3);
		test.run();
		
		/* K = 4 and replica = 4 */
		test = getTest(4, 4);
		test.run();
		
		System.out.println("Comparing K: ");
		/* K = 4 and replica = 2 */
		test = getTest(4, 2);
		test.run();
		
		/* K = 5 and replica = 2 */
		test = getTest(5, 2);
		test.run();
		
		/* K = 6 and replica = 2 */
		test = getTest(6, 2);
		test.run();
		
		/* K = 7 and replica = 2 */
		test = getTest(7, 2);
		test.run();
		
		/* K = 8 and replica = 2 */
		test = getTest(8, 2);
		test.run();
		
	}
	public static void main(String[] args) {
		ColocationGraphTransformationSimulator simulator = new ColocationGraphTransformationSimulator();
		simulator.run();
	}

}