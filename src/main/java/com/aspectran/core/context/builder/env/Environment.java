package com.aspectran.core.context.builder.env;

import com.aspectran.core.adapter.ApplicationAdapter;

public interface Environment {

	ApplicationAdapter getApplicationAdapter();

	void setApplicationAdapter(ApplicationAdapter applicationAdapter);
	
	String[] getActiveProfiles();

	String[] getDefaultProfiles();

	boolean acceptsProfiles(String... profiles);	

	static String joinProfiles(String[] profiles) {
		return String.join(",", profiles);
	}	

}
