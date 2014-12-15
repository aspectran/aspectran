package com.aspectran.core.context.loader.reload;

import java.util.TimerTask;

import com.aspectran.core.context.ActivityContext;

public class ActivityContextReloadingTimerTask extends TimerTask {

	private ActivityContextReloadable activityContextReloadable;
	
	private String[] resources;
	
	private boolean modified = false;
	
	public ActivityContextReloadingTimerTask(ActivityContextReloadable activityContextReloadable) {
		this.activityContextReloadable = activityContextReloadable;
		this.resources = activityContextReloadable.getResources();
	}
	
	public void run() {
		if(resources == null)
			return;
		
		for(String resource : resources) {
			modified = true;
		}
		
		if(modified) {
			ActivityContext newActivityContext = activityContextReloadable.reload();
			modified = false;
		}
		
	}

}

