package com.aspectran.core.context.refresh;

import java.util.TimerTask;

import com.aspectran.core.context.ActivityContext;


public class ActivityContextRefreshTimerTask extends TimerTask {

	private ActivityContextRefreshable activityContextRefreshable;
	
	private String[] observingPaths;
	
	private String applicationBasePath;
	
	
	private boolean modified = false;
	
	public ActivityContextRefreshTimerTask(ActivityContextRefreshable activityContextRefreshable) {
		this.activityContextRefreshable = activityContextRefreshable;
		this.observingPaths = activityContextRefreshable.getObservingPaths();
		this.applicationBasePath = activityContextRefreshable.getApplicationBasePath();
	}
	
	public void run() {
		if(modified) {
			ActivityContext newActivityContext = activityContextRefreshable.refresh();
		}
		
		modified = false;
	}

}

