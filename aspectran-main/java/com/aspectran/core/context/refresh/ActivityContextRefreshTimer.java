package com.aspectran.core.context.refresh;

import java.util.Timer;
import java.util.TimerTask;

public class ActivityContextRefreshTimer {

	private ActivityContextRefreshable contextRefreshable;
	
	private Timer timer;
	
	private TimerTask timerTask;
	
	private int refreshTime = 5;
	
	public ActivityContextRefreshTimer(ActivityContextRefreshable contextRefreshable) {
		this.contextRefreshable = contextRefreshable;
		
		init();
	}
	
	public ActivityContextRefreshTimer(ActivityContextRefreshable contextRefreshable, int refreshTime) {
		this.contextRefreshable = contextRefreshable;
		this.refreshTime = refreshTime;
		
		init();
	}
	
	private void init() {
		timerTask = new ActivityContextRefreshTimerTask(contextRefreshable);
		timer = new Timer();
	}
	
	public void start(int refreshTime) {
		this.refreshTime = refreshTime;
		
		start();
	}
	
	public void start() {
		timer.schedule(timerTask, 0, refreshTime * 1000L);
	}
	
	public void cancel() {
		if(timer != null) {
			timer.cancel();
		}
	}

}