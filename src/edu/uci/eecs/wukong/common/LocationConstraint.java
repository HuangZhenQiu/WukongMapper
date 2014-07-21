package edu.uci.eecs.wukong.common;

public class LocationConstraint {
	private int landMarkId;
	private Double distance;
	
	public LocationConstraint(int landMarkId, Double distance) {
		this.landMarkId = landMarkId;
		this.distance = distance;
	}

	public int getLandMarkId() {
		return landMarkId;
	}

	public void setLandMarkId(int landMarkId) {
		this.landMarkId = landMarkId;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}
}