package com.aspectran.core.context.loader.reload;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityContextReloadingTimer {
	
	private final Logger logger = LoggerFactory.getLogger(ActivityContextReloadingTimer.class);

	private ActivityContextReloadDelegate activityContextReloadDelegate;
	
	private Timer timer;
	
	private TimerTask timerTask;
	
	public ActivityContextReloadingTimer(ActivityContextReloadDelegate activityContextReloadDelegate) {
		this.activityContextReloadDelegate = activityContextReloadDelegate;
		
		init();
	}
	
	private void init() {
		logger.debug("ActivityContextobservationIntervalr is initialized successfully.");
	}
	
	public void start(int observationInterval) {
		stop();
		
		logger.debug("starting ActivityContextobservationIntervalr...");
		
		timerTask = new ActivityContextReloadingTimerTask(activityContextReloadDelegate);
		
		timer = new Timer();
		timer.schedule(timerTask, 0, observationInterval * 1000L);
	}
	
	public void cancel() {
		stop();
	}
	
	protected void stop() {
		if(timer != null) {
			logger.debug("stopping ActivityContextobservationIntervalr...");
			
			timer.cancel();
			timer = null;
			
			timerTask.cancel();
			timerTask = null;
		}
	}

}