package edu.uci.eecs.wukong.util;

import java.lang.StringBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.uci.eecs.wukong.common.WuClass;

public class Util {
	
	public static String generateDeviceVariable(Integer deviceId) {
		StringBuilder builder = new StringBuilder();
		builder.append("d_").append(deviceId.toString());
		return builder.toString();
	}
	
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
	
	public static boolean isTransformedVariable(String name) {
		if(name.startsWith("y")){
			return true;
		}
		
		return false;
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
	
	public static HashMap<Object, Integer> assignIdToGraphNode(
			SimpleDirectedGraph<Object, DefaultEdge> graph) {
		Set<Object> vertexes = graph.vertexSet();
		int idSize = vertexes.size();
		HashMap<Object, Integer> idMap = new HashMap<Object, Integer>();
		Random random = new Random();
		
		Iterator<Object> objects = vertexes.iterator();
		int[] classMap = new int[idSize];
		Arrays.fill(classMap, 0);
		while(objects.hasNext()) {
			Object object= objects.next();
			random.setSeed(idSize + System.nanoTime());
			Integer classId = Math.abs(random.nextInt() % idSize);
			while(classMap[classId] == 1) {
				classId = Math.abs(random.nextInt() % idSize);
			}
			classMap[classId] = 1;
			idMap.put(object, classId);
		}
		
		return idMap;
	}
	
	/**
	 * It is an implementation of Bellman Ford algorithm
	 * @param graph
	 * @return
	 */
	public static Double[][] findShortestPath(Double[][] graph) {
		int length = graph.length;
		Double[][] distance = new Double[length][length];
		Integer[][] precedence = new Integer[length][length];
		
		for(int i=0; i<length; i++) {
			for(int j=0; j<length; j++) {
				distance[i][j] = graph[i][j];
				
				if (graph[i][j] != Double.MAX_VALUE) {
					precedence[i][j] = i;
				} else {
					precedence[i][j] = -1;
				}
			}
		}
		
		for(int k=0; k<length; k++) {
			for (int i=0; i<length; i++) {
				for (int j=0; j<length; j++) {
					if(graph[i][k] !=Double.MAX_VALUE && graph[k][j] !=Double.MAX_VALUE && distance[i][j] > graph[i][k] + graph[k][j]) {
						distance[i][j] = graph[i][k] + graph[k][j];
						precedence[i][j] = k;
						//distance[j][i] = graph[i][k] + graph[k][j];
					}
				}
			}
		}
		
		return distance;
	}

}
