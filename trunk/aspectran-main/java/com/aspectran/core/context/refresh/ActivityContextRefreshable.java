package com.aspectran.core.context.refresh;

import com.aspectran.core.context.ActivityContext;


public interface ActivityContextRefreshable {

	public ActivityContext refresh();

	public ActivityContext getActivityContext();
	
	public String[] getRefreshableFiles();
	
	public String getApplicationBasePath();
	
}
