package com.aspectran.core.context.refresh;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityContextRefreshTimer {
	
	private final Logger logger = LoggerFactory.getLogger(ActivityContextRefreshTimer.class);

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
		logger.debug("ActivityContextRefreshTimer is initialized successfully.");
	}
	
	public void start(int refreshTime) {
		this.refreshTime = refreshTime;
		
		start();
	}
	
	public void start() {
		stop();
		
		logger.debug("starting ActivityContextRefreshTimer...");
		
		timerTask = new ActivityContextRefreshTimerTask(contextRefreshable);
		timer = new Timer();
		timer.schedule(timerTask, 0, refreshTime * 1000L);
	}
	
	public void cancel() {
		stop();
	}
	
	protected void stop() {
		if(timer != null) {
			logger.debug("stopping ActivityContextRefreshTimer...");
			
			timer.cancel();
			timer = null;
			timerTask.cancel();
			timerTask = null;
		}
	}

}