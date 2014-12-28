package com.aspectran.core.context.service;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.ActivityContextLoader;
import com.aspectran.core.context.loader.ActivityContextLoadingManager;
import com.aspectran.core.context.loader.config.AspectranConfig;

public class ActivityContextService extends ActivityContextLoadingManager {

	private ActivityContextReloadListener activityContextReloadListener;
	
	private ActivityContextPauseResumeListener activityContextPauseResumeListener;
	
	public ActivityContextService(AspectranConfig aspectranConfig, ActivityContextLoader activityContextLoader) {
		super(aspectranConfig, activityContextLoader);
	}
	
	public void setActivityContextReloadListener(
			ActivityContextReloadListener activityContextReloadingListener) {
		this.activityContextReloadListener = activityContextReloadingListener;
	}

	public synchronized ActivityContext start() {
		createActivityContext();
		
		ActivityContextServiceHandler activityContextServiceHandler = new ActivityContextServiceHandler(this);
		getApplicationAdapter().putActivityContextServiceHandler(activityContext, activityContextServiceHandler);
		
		return activityContext;
	}
	
	public synchronized boolean stop() {
		return destroyActivityContext();
	}
	
	public synchronized ActivityContext restart() {
		reloadActivityContext();
		
		if(activityContextReloadListener != null)
			activityContextReloadListener.reloaded();
		
		return activityContext;
	}

	public synchronized void pause() {
		if(activityContextPauseResumeListener != null)
			activityContextPauseResumeListener.paused();
	}
	
	public synchronized void resume() {
		if(activityContextPauseResumeListener != null)
			activityContextPauseResumeListener.resumed();
	}
	
}
