package com.aspectran.core.context.loader.reload;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.AspectranClassLoader;


public interface ActivityContextReloadable {

	public ActivityContext reloadActivityContext();

	public ActivityContext getActivityContext();
	
	public AspectranClassLoader getAspectranClassLoader();
	
}
