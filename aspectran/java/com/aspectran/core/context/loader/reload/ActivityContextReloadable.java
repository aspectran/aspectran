package com.aspectran.core.context.loader.reload;

import com.aspectran.core.context.ActivityContext;


public interface ActivityContextReloadable {

	public ActivityContext reload();

	public ActivityContext getActivityContext();
	
	public String[] getResources();
	
	public String getApplicationBasePath();
	
}
