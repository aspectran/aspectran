package com.aspectran.core.context.builder.env;

public interface Environment {

	String[] getActiveProfiles();

	String[] getDefaultProfiles();

	boolean acceptsProfiles(String... profiles);	

	static String joinProfiles(String[] profiles) {
		return String.join(",", profiles);
	}	

}
