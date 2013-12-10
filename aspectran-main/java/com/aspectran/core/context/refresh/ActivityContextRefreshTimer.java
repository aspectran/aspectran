package com.aspectran.core.context.refresh;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityContextRefreshTimer {

	private ActivityContextRefreshable contextRefreshable;
	
	private Timer timer;
	
	private int refreshTime = 5;
	
	public ActivityContextRefreshTimer(ActivityContextRefreshable contextRefreshable) {
		this.contextRefreshable = contextRefreshable;
	}
	
	public ActivityContextRefreshTimer(ActivityContextRefreshable contextRefreshable, int refreshTime) {
		this.contextRefreshable = contextRefreshable;
		this.refreshTime = refreshTime;
	}
	
	public void start(int refreshTime) {
		this.refreshTime = refreshTime;
		
		start();
	}
	
	public void start() {
		TimerTask timerTask = new ActivityContextRefreshTimerTask(contextRefreshable);
		
		timer = new Timer();
		timer.schedule(timerTask, new Date(), refreshTime);
	}
	
	public void cancel() {
		if(timer != null) {
			timer.cancel();
		}
	}

}