package edu.uci.eecs.wukong.energy.mapper;


public interface Mapper {
	
	public static enum MapType {
		BOTH,
		ONLY_LOCATION,
		ONLY_ENERGY;
	}
	
	public boolean map();
}
