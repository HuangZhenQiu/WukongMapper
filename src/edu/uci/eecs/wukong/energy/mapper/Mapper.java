package edu.uci.eecs.wukong.energy.mapper;


public interface Mapper {
	
	public static enum MapType {
		BOTH,
		ONLY_LOCATION,
		ONLY_ENERGY,
		WITH_LATENCY,
		WITHOUT_LATENCY;
	}
	
	public boolean map();
}
