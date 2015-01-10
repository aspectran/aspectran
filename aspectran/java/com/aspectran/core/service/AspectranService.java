package com.aspectran.core.service;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.AspectranClassLoader;

public interface AspectranService extends AspectranServiceController {

	public void setAspectranServiceControllerListener(AspectranServiceControllerListener activityContextServiceListener);
	
	public AspectranClassLoader getAspectranClassLoader();
	
	public ApplicationAdapter getApplicationAdapter();
	
	public ActivityContext getActivityContext();
	
	public boolean dispose();
	
	public AspectranService createAspectranServiceWrapper();

}
