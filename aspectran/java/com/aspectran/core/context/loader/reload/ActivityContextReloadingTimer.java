package com.aspectran.core.context.loader.reload;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityContextReloadingTimer {
	
	private final Logger logger = LoggerFactory.getLogger(ActivityContextReloadingTimer.class);

	private ActivityContextReloadable activityContextReloadble;
	
	private Timer timer;
	
	private TimerTask timerTask;
	
	private int refreshTime = 5;
	
	public ActivityContextReloadingTimer(ActivityContextReloadable activityContextReloadble) {
		this.activityContextReloadble = activityContextReloadble;
		
		init();
	}
	
	public ActivityContextReloadingTimer(ActivityContextReloadable activityContextReloadble, int refreshTime) {
		this.activityContextReloadble = activityContextReloadble;
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
		
		timerTask = new ActivityContextReloadingTimerTask(activityContextReloadble);
		
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