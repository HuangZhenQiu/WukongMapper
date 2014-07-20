package edu.uci.eecs.wukong.common;

import java.util.Random;

public class WuObject {
	
	private Integer wuClassId;
//	private WuDevice device;
	private boolean active;
	private double[] non_functional_properties; 
	public WuObject(Integer wuClassId) {
		this.wuClassId = wuClassId;
//		this.device = device;
		this.active = false;
	}
	
	public Integer getWuClassId() {
		return this.wuClassId;
	}
	public void generateProperties(double[] properties){
		this.non_functional_properties = properties;
	}
	
	public void initProperties(int dimension) {
		double[] properties = new double[dimension];
		Random random = new Random();
		random.setSeed(dimension + System.nanoTime());
		
		for(int i = 0; i < dimension; i++){
			double property = Math.abs(random.nextInt()) % 100;
			properties[i] = property / 100.0;
		}
		this.non_functional_properties = properties;
	}
	public double[] getProperties(){
		return non_functional_properties;
	}
	public boolean isActive() {
		return active;
	}
	
	
	//They are only accessible by WuDevice
	protected void activate() {
		this.active = true;
	}
	
	protected void deactivate() {
		this.active = false;
	}
	
}
