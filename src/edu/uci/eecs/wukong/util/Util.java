package edu.uci.eecs.wukong.util;

import java.lang.StringBuilder;

public class Util {
	
	public static String generateVariableId(Integer classId, Integer deviceId) {
		StringBuilder builder = new StringBuilder();
		builder.append("x_").append(classId.toString())
			   .append("_").append(deviceId.toString());
		
		return builder.toString();
	}
	
	public static double getReceivingEnergyConsumption(int bytes) {
		return 50 * bytes;
	}
	
	public static double getTransmissionEnergyConsumption(int bytes, double distance) {
		return 50 * bytes + 0.01* bytes * distance * distance;
	}
	
	public static String generateTransformedVariableId(Integer sourceClassId, Integer sourceDeviceId,
			Integer destClassId, Integer destDeviceId) {
		
		StringBuilder builder = new StringBuilder();
		builder.append("y_").append(sourceClassId.toString()).append("_").append(sourceDeviceId.toString())
				.append("_").append(destClassId.toString()).append("_").append(destDeviceId.toString());
		
		return builder.toString();
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
