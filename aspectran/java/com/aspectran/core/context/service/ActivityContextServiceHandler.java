package com.aspectran.core.context.service;

import com.aspectran.core.context.ActivityContext;


public class ActivityContextServiceHandler {
	
	private ActivityContextService activityContextService;
	
	public ActivityContextServiceHandler(ActivityContextService activityContextService) {
		this.activityContextService = activityContextService;
	}
	
	public ActivityContext start() {
		return activityContextService.start();
	}

	public boolean stop() {
		return activityContextService.stop();
	}
	
	public void pause() {
	}
	
	public void resume() {
		
	}
	
	public ActivityContext restart() {
		return activityContextService.restart();
	}
	
}
