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
import com.aspectran.core.context.refresh.ActivityContextRefreshHandler;
import com.aspectran.core.context.refresh.ActivityContextRefreshTimer;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.var.option.Options;
import com.aspectran.scheduler.AspectranScheduler;
import com.aspectran.scheduler.quartz.QuartzAspectranScheduler;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.startup.ActivityContextLoader;
import com.aspectran.web.startup.RefreshableActivityContextLoader;
import com.aspectran.web.startup.servlet.SchedulerOptions;

public class AspectranSchedulerListener implements ServletContextListener {

	private final Logger logger = LoggerFactory.getLogger(AspectranSchedulerListener.class);

	public static final String ASPECTRAN_SCHEDULER_PARAM = "aspectran:scheduler";

	private ActivityContext activityContext;
	
	private AspectranScheduler aspectranScheduler;
	
	private ActivityContextRefreshTimer contextRefreshTimer;
	
	private String contextConfigLocation;
	
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
			contextConfigLocation = schedulerOptions.getString(AspectranSchedulerOptions.contextConfigLocation);
			startDelaySeconds = schedulerOptions.getInt(SchedulerOptions.startDelaySeconds, -1);
			waitOnShutdown = schedulerOptions.getBoolean(SchedulerOptions.waitOnShutdown, true);
			boolean startup = schedulerOptions.getBoolean(AspectranSchedulerOptions.startup, false);
			String autoReload = (String)schedulerOptions.getString("autoReload", "0");
			int refreshTime = 0;
			
			try {
				refreshTime = Integer.parseInt(autoReload);
			} catch(NumberFormatException e) {
				boolean isAutoReload = Boolean.parseBoolean(autoReload);
				if(isAutoReload)
					refreshTime = 5; //default refresh time
			}
			
			if(startup)
				return;
			
			if(startDelaySeconds == -1) {
				logger.info("Scheduler option 'startDelaySeconds is' not specified, defaulting to 5 seconds.");
				startDelaySeconds = 5;
			}
			
			if(refreshTime == 0) {
				ActivityContextLoader aspectranContextLoader = new ActivityContextLoader(servletContext, contextConfigLocation);
				activityContext = aspectranContextLoader.load();
			} else {
				ActivityContextRefreshHandler contextRefreshHandler = new ActivityContextRefreshHandler() {
					public void handle(ActivityContext newContext) {
						reload(newContext);
					}
				};
				
				RefreshableActivityContextLoader loader = new RefreshableActivityContextLoader(servletContext, contextConfigLocation);
				activityContext = loader.load();
				contextRefreshTimer = loader.startTimer(contextRefreshHandler, 5);
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
			logger.error("WebApplicationAdapter failed to destroy: " + e.toString(), e);
		}

		if(cleanlyDestoryed)
			logger.info("AspectranScheduler successful destroyed.");
		else
			logger.error("AspectranScheduler failed to destroy cleanly.");

		logger.info("Do not terminate the server while the all scoped bean destroying.");
	}

	protected boolean shutdownScheduler() {
		if(aspectranScheduler != null) {
			try {
				aspectranScheduler.shutdown();
				logger.info("AspectranScheduler successful shutdown.");
			} catch(Exception e) {
				logger.error("AspectranScheduler failed to shutdown cleanly: " + e.toString(), e);
				return false;
			}
		}
		
		return true;
	}
	
	protected boolean destroyContext() {
		if(activityContext != null) {
			try {
				activityContext.destroy();
				logger.info("AspectranContext successful destroyed.");
			} catch(Exception e) {
				logger.error("AspectranContext failed to destroy: " + e.toString(), e);
				return false;
			}
		}
		
		return true;
	}
	
	protected void reload(ActivityContext newContext) {
		if(contextRefreshTimer != null)
			contextRefreshTimer.cancel();
		
		shutdownScheduler();
		destroyContext();
		
		activityContext = newContext;
		
		try {
			initScheduler();
		} catch(Exception e) {
			logger.error("Scheduler failed to initialize: " + e.toString(), e);
		}
		
		if(contextRefreshTimer != null)
			contextRefreshTimer.start();
	}
	
}
