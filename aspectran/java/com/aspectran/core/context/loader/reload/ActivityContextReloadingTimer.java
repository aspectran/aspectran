package com.aspectran.core.context.loader.reload;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.service.AspectranService;

public class ActivityContextReloadingTimer {
	
	private final Logger logger = LoggerFactory.getLogger(ActivityContextReloadingTimer.class);

	private AspectranService aspectranService;
	
	private Timer timer;
	
	private TimerTask timerTask;
	
	public ActivityContextReloadingTimer(AspectranService aspectranService) {
		this.aspectranService = aspectranService;
		
		init();
	}
	
	private void init() {
		logger.debug("ActivityContextRefreshTimer is initialized successfully.");
	}
	
	public void start(int observationInterval) {
		stop();
		
		logger.debug("Starting ActivityContextRefreshTimer...");
		
		timerTask = new ActivityContextReloadingTimerTask(aspectranService);
		
		timer = new Timer();
		timer.schedule(timerTask, 0, observationInterval * 1000L);
	}
	
	public void cancel() {
		stop();
	}
	
	protected void stop() {
		if(timer != null) {
			logger.debug("Stopping ActivityContextRefreshTimer...");
			
			timer.cancel();
			timer = null;
			
			timerTask.cancel();
			timerTask = null;
		}
	}

}