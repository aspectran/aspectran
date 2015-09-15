/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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