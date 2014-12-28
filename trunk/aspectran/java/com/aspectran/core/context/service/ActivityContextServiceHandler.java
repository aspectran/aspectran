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
		throw new UnsupportedOperationException();
	}
	
	public void resume() {
		throw new UnsupportedOperationException();
	}
	
	public ActivityContext restart() {
		return activityContextService.restart();
	}
	
}
