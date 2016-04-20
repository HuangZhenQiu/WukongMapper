package edu.uci.eecs.wukong.common;

/**
 * 
 * Variable for roundup
 * 
 * @author peter
 *
 */
public class Variable implements Comparable<Variable> {
	private String name;
	private Double value;
	
	public Variable(String name, Double value) {
		this.name = name;
		this.value = value;
	}
	
	public boolean equals(Variable variable) {
		if (variable == null)
			return false;
		if (variable.name.equals(this.name) &&
				variable.value.equals(this.value)) {
			return true;
		}
		
		return false;
	}
	
	public String getName() {
		return this.name;
	}

	@Override
	public int compareTo(Variable variable) {
		if (value > variable.value) {
			return 1;
		} else if (value.equals(variable.value)) {
			return 0;
		} else {
			return -1;
		}
	}
}
