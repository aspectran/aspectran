package com.aspectran.core.context.reload;

import com.aspectran.core.context.ActivityContext;


public interface ActivityContextReloadable {

	public ActivityContext reload();

	public ActivityContext getActivityContext();
	
	public String[] getObservingPaths();
	
	public String getApplicationBasePath();
	
}
