package com.aspectran.core.service;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.AspectranClassLoader;

public interface AspectranService extends AspectranServiceController {

	public void setActivityContextServiceListener(AspectranServiceListener activityContextServiceListener);
	
	public AspectranClassLoader getAspectranClassLoader();
	
	public ActivityContext getActivityContext();
	
	public boolean dispose();

}
