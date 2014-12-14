/*
 *  Copyright (c) 2009 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.web.startup.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.reload.ActivityContextReloadingHandler;
import com.aspectran.core.context.loader.reload.ActivityContextReloadingTimer;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.var.option.Options;
import com.aspectran.scheduler.AspectranScheduler;
import com.aspectran.scheduler.quartz.QuartzAspectranScheduler;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.startup.loader.WebActivityContextLoader;
import com.aspectran.web.startup.loader.WebActivityContextLoadingObserver;
import com.aspectran.web.startup.servlet.AutoReloadingOptions;

public class AspectranSchedulerListener implements ServletContextListener {

	private final Logger logger = LoggerFactory.getLogger(AspectranSchedulerListener.class);

	public static final String ASPECTRAN_SCHEDULER_PARAM = "aspectran:scheduler";

	private ActivityContext activityContext;
	
	private AspectranScheduler aspectranScheduler;
	
	private ActivityContextReloadingTimer contextRefreshTimer;
	
	private String rootContext;
	
	private int startDelaySeconds;
	
	private boolean waitOnShutdown;

	/**
	 * Initialize the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextInitialized(ServletContextEvent event) {
		logger.info("initializing AspectranScheduler...");

		ServletContext servletContext = event.getServletContext();
		
		String schedulerParam = servletContext.getInitParameter(ASPECTRAN_SCHEDULER_PARAM);
		
		if(!StringUtils.hasText(schedulerParam)) {
			logger.error("AspectranScheduler has not been started. \"aspectran:scheduler\" context-param is required.");
			return;
		}
		
		try {
			Options schedulerOptions = new AspectranSchedulerOptions(schedulerParam);
			rootContext = schedulerOptions.getString(AspectranSchedulerOptions.rootContext);
			startDelaySeconds = schedulerOptions.getInt(AspectranSchedulerOptions.startDelaySeconds, -1);
			waitOnShutdown = schedulerOptions.getBoolean(AspectranSchedulerOptions.waitOnShutdown, true);
			boolean startup = schedulerOptions.getBoolean(AspectranSchedulerOptions.startup, false);

			if(!startup) {
				logger.info("AspectranScheduler is not startup.");
				return;
			}
			
			String[] observingPaths = null;
			int observationInterval = -1;
			boolean autoReloadingStartup = true;

			Options autoReloadingOptions = schedulerOptions.getOptions(AspectranSchedulerOptions.autoReloading);
			
			if(autoReloadingOptions != null) {
				observingPaths = autoReloadingOptions.getStringArray(AutoReloadingOptions.observingPath);
				observationInterval = autoReloadingOptions.getInt(AutoReloadingOptions.observationInterval, -1);
				autoReloadingStartup = autoReloadingOptions.getBoolean(AspectranSchedulerOptions.startup, true);
			}

			if(observingPaths == null || observingPaths.length == 0)
				autoReloadingStartup = false;
			
			if(startDelaySeconds == -1) {
				logger.info("Scheduler context-param 'startDelaySeconds' is not specified. Therefore, defaulting to 5 seconds.");
				startDelaySeconds = 5;
			}
			
			if(!autoReloadingStartup) {
				WebActivityContextLoader aspectranContextLoader = new WebActivityContextLoader(servletContext, rootContext);
				activityContext = aspectranContextLoader.load();
			} else {
				if(observationInterval == -1) {
					logger.info("Scheduler context-param 'autoReloading' option 'observationInterval' is not specified. Therefore, defaulting to 10 seconds.");
					observationInterval = 10;
				}
				
				ActivityContextReloadingHandler contextRefreshHandler = new ActivityContextReloadingHandler() {
					public void handle(ActivityContext newActivityContext) {
						reload(newActivityContext);
					}
				};
				
				WebActivityContextLoadingObserver observer = new WebActivityContextLoadingObserver(servletContext, rootContext);
				activityContext = observer.load();
				contextRefreshTimer = observer.startTimer(contextRefreshHandler, observingPaths, observationInterval);
			}
			
			initScheduler();

		} catch(Exception e) {
			logger.error("AspectranScheduler failed to initialize: " + e.toString(), e);
		}			
	}
	
	protected void initScheduler() throws Exception {
		aspectranScheduler = new QuartzAspectranScheduler(activityContext);
		
		if(waitOnShutdown)
			aspectranScheduler.setWaitOnShutdown(true);
		
		aspectranScheduler.startup(startDelaySeconds);
	}
	
	/**
	 * Close the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextDestroyed(ServletContextEvent event) {
		if(contextRefreshTimer != null)
			contextRefreshTimer.cancel();
		
		boolean cleanlyDestoryed = true;

		if(!shutdownScheduler())
			cleanlyDestoryed = false;

		if(!destroyContext())
			cleanlyDestoryed = false;
		
		try {
			WebApplicationAdapter.destoryWebApplicationAdapter(event.getServletContext());
		} catch(Exception e) {
			cleanlyDestoryed = false;
			logger.error("WebApplicationAdapter was failed to destroy: " + e.toString(), e);
		}

		if(cleanlyDestoryed)
			logger.info("AspectranScheduler was successfully destroyed.");
		else
			logger.error("AspectranScheduler was failed to destroy cleanly.");

		logger.info("Do not terminate the server while the all scoped bean destroying.");
	}

	protected boolean shutdownScheduler() {
		if(aspectranScheduler != null) {
			try {
				aspectranScheduler.shutdown();
				logger.info("AspectranScheduler has been shutdown successfully.");
			} catch(Exception e) {
				logger.error("AspectranScheduler was failed to shutdown cleanly: " + e.toString(), e);
				return false;
			}
		}
		
		return true;
	}
	
	protected boolean destroyContext() {
		if(activityContext != null) {
			try {
				activityContext.destroy();
				logger.info("AspectranContext was destroyed successfully.");
			} catch(Exception e) {
				logger.error("AspectranContext was failed to destroy: " + e.toString(), e);
				return false;
			}
		}
		
		return true;
	}
	
	protected void reload(ActivityContext newActivityContext) {
		if(contextRefreshTimer != null)
			contextRefreshTimer.cancel();
		
		shutdownScheduler();
		destroyContext();
		
		activityContext = newActivityContext;
		
		try {
			initScheduler();
		} catch(Exception e) {
			logger.error("Scheduler was failed to initialize: " + e.toString(), e);
		}
		
		if(contextRefreshTimer != null)
			contextRefreshTimer.start();
	}
	
}
