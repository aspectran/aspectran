package com.aspectran.core.context.loader.reload;

import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.context.service.ActivityContextService;


public class ActivityContextReloadDelegate {
	
	private final ActivityContextService activityContextService;
	
	public ActivityContextReloadDelegate(ActivityContextService activityContextService) {
		this.activityContextService = activityContextService;
	}

	public boolean reload() {
		return activityContextService.restart();
	}
	
	public AspectranClassLoader getAspectranClassLoader() {
		return activityContextService.getAspectranClassLoader();
	}
	
}
