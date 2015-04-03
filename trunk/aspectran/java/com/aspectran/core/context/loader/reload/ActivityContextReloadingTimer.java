package com.aspectran.core.context.loader.reload;

import java.util.Timer;
import java.util.TimerTask;

import com.aspectran.core.service.AspectranService;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

public class ActivityContextReloadingTimer {
	
	private final Log log = LogFactory.getLog(ActivityContextReloadingTimer.class);

	private AspectranService aspectranService;
	
	private Timer timer;
	
	private TimerTask timerTask;
	
	public ActivityContextReloadingTimer(AspectranService aspectranService) {
		this.aspectranService = aspectranService;
		
		init();
	}
	
	private void init() {
		log.debug("ActivityContextRefreshTimer is initialized successfully.");
	}
	
	public void start(int observationInterval) {
		stop();
		
		log.debug("Starting ActivityContextRefreshTimer...");
		
		timerTask = new ActivityContextReloadingTimerTask(aspectranService);
		
		timer = new Timer();
		timer.schedule(timerTask, 0, observationInterval * 1000L);
	}
	
	public void cancel() {
		stop();
	}
	
	protected void stop() {
		if(timer != null) {
			log.debug("Stopping ActivityContextRefreshTimer...");
			
			timer.cancel();
			timer = null;
			
			timerTask.cancel();
			timerTask = null;
		}
	}

}