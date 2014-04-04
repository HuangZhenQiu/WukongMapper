package edu.uci.eecs.wukong.util;

public class Util {
	
	public static String generateVariableId(Integer classId, Integer deviceId) {
		
		return "x_" + classId.toString() + "_" + deviceId.toString();
	}
	
	public static Integer getWuClassIdFromVariableId(String variableId) {

		String[] items = variableId.split("_");
		if(items.length != 3 || !items[0].equals("x")) {
			return null;
		}

		Integer wuClassId = null;

		try {
			wuClassId =  Integer.valueOf(items[1]);
		} catch (NumberFormatException e) {
			System.out.println("Invalide VariableId: " + variableId);
		}
		return wuClassId;

	}
	
	public static Integer getWuDeviceIdFromVariableId(String variableId) {

		String[] items = variableId.split("_");
		if(items.length != 3 || !items[0].equals("x")) {
			return null;
		}

		Integer wuDeviceId = null;

		try {
			wuDeviceId =  Integer.valueOf(items[2]);
		} catch (NumberFormatException e) {
			System.out.println("Invalide VariableId: " + variableId);
		}
		return wuDeviceId;

	}
	
	public static void reset(int[] classMap) {
		for(int i=0; i < classMap.length; i++) {
			classMap[i] = 0;
		}
	}

}