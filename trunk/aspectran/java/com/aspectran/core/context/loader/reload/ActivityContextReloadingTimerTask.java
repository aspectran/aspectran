package com.aspectran.core.context.loader.reload;

import java.util.TimerTask;

import com.aspectran.core.context.ActivityContext;


public class ActivityContextReloadingTimerTask extends TimerTask {

	private ActivityContextReloadable activityContextReloadable;
	
	private String[] observingPaths;
	
	private String applicationBasePath;
	
	
	private boolean modified = false;
	
	public ActivityContextReloadingTimerTask(ActivityContextReloadable activityContextReloadable) {
		this.activityContextReloadable = activityContextReloadable;
		this.observingPaths = activityContextReloadable.getObservingPaths();
		this.applicationBasePath = activityContextReloadable.getApplicationBasePath();
	}
	
	public void run() {
		if(modified) {
			ActivityContext newActivityContext = activityContextReloadable.reload();
		}
		
		modified = false;
	}

}

