package edu.uci.eecs.wukong.energy;

import java.io.FileInputStream;
import java.util.Properties;

public class WukongProperties {
	
	private static Properties properties;

	static {
		try {
			properties.load(new FileInputStream("../Bayesian/config.properties"));
		} catch (Exception e) {
			System.out.println("Properties load exception: " + e.toString());
		}
	}
	
	public static String getProperty(String name) {
		return properties.getProperty(name);
	}
}
