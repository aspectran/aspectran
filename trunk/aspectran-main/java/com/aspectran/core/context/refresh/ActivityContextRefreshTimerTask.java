package com.aspectran.core.context.refresh;

import java.util.TimerTask;

import com.aspectran.core.context.ActivityContext;


public class ActivityContextRefreshTimerTask extends TimerTask {

	private ActivityContextRefreshable contextRefreshable;
	
	public ActivityContextRefreshTimerTask(ActivityContextRefreshable contextRefreshable) {
		this.contextRefreshable = contextRefreshable;
	}
	
	public void run() {
		ActivityContext newContext = contextRefreshable.refresh();
	}

}

