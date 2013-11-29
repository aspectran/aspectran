package com.aspectran.core.context.refresh;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityContextRefreshTimer {

	private ActivityContextRefreshable contextRefreshable;
	
	private Timer timer;
	
	public ActivityContextRefreshTimer(ActivityContextRefreshable contextRefreshable) {
		this.contextRefreshable = contextRefreshable;
	}
	
	public void start(int refreshTime) {
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