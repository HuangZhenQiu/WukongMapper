package edu.uci.eecs.wukong.util;

import junit.framework.TestCase;

public class UtilTest extends TestCase{
	public void testFindShortestPath() {
		Double[][] test = new Double[4][4];
		for (int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
				test[i][j] = Double.MAX_VALUE;
			}
		}
		
		test[0][0] = 0.0;
		test[1][1] = 0.0;
		test[2][2] = 0.0;
		test[3][3] = 0.0;
		
		test[0][1] = 2.0;
		test[0][2] = 3.0;
		test[1][3] = 4.0;
		test[2][3] = 5.0;
		
		test[1][0] = 2.0;
		test[2][0] = 3.0;
		test[3][1] = 4.0;
		test[3][2] = 5.0;
		
		Double[][] result = Util.findShortestPath(test);
		assertEquals(result[0][3], 6.0);
		assertEquals(result[1][2], 5.0);
	}
}
