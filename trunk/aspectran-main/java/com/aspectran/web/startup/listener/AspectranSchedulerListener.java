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
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.var.option.Options;
import com.aspectran.scheduler.AspectranScheduler;
import com.aspectran.scheduler.quartz.QuartzAspectranScheduler;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.startup.ActivityContextLoader;

public class AspectranSchedulerListener implements ServletContextListener {

	private final Logger logger = LoggerFactory.getLogger(AspectranSchedulerListener.class);

	public static final String ASPECTRAN_SCHEDULER_PARAM = "aspectran:scheduler";

	private ActivityContext activityContext;
	
	private AspectranScheduler aspectranScheduler;
	
	/**
	 * Initialize the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextInitialized(ServletContextEvent event) {
		logger.info("initializing AspectranScheduler...");

		ServletContext servletContext = event.getServletContext();
		
		String aspectranSchedulerParam = servletContext.getInitParameter(ASPECTRAN_SCHEDULER_PARAM);
		
		if(!StringUtils.hasText(aspectranSchedulerParam)) {
			logger.error("AspectranScheduler has not been started. \"aspectran:scheduler\" context-param is required.");
			return;
		}
		
		try {
			Options options = new AspectranSchedulerOptions(aspectranSchedulerParam);
			String contextConfigLocation = (String)options.getValue(AspectranSchedulerOptions.contextConfigLocation);
			Integer startDelaySeconds = (Integer)options.getValue(AspectranSchedulerOptions.startDelaySeconds);
			Boolean waitOnShutdown = (Boolean)options.getValue(AspectranSchedulerOptions.waitOnShutdown);
			Boolean startup = (Boolean)options.getValue(AspectranSchedulerOptions.startup);
			
			if(Boolean.FALSE.equals(startup))
				return;
			
			activityContext = ActivityContextLoader.load(servletContext, contextConfigLocation);
			
			aspectranScheduler = new QuartzAspectranScheduler(activityContext);
			
			if(Boolean.TRUE.equals(waitOnShutdown))
				aspectranScheduler.setWaitOnShutdown(true);
			
			if(startDelaySeconds == null) {
				logger.info("Scheduler option 'startDelaySeconds is' not specified, defaulting to 5 seconds.");
				startDelaySeconds = 5;
			}
			
			aspectranScheduler.startup(startDelaySeconds);
			
		} catch(Exception e) {
			logger.error("AspectranScheduler failed to initialize: " + e.toString(), e);
		}			
	}

	/**
	 * Close the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextDestroyed(ServletContextEvent event) {
		boolean cleanlyDestoryed = true;

		if(aspectranScheduler != null) {
			try {
				aspectranScheduler.shutdown();
				logger.info("AspectranScheduler successful shutdown.");
			} catch(Exception e) {
				cleanlyDestoryed = false;
				logger.error("AspectranScheduler failed to shutdown cleanly: " + e.toString(), e);
			}
		}

		if(activityContext != null) {
			try {
				activityContext.destroy();
				logger.info("AspectranContext successful destroyed.");
			} catch(Exception e) {
				cleanlyDestoryed = false;
				logger.error("AspectranContext failed to destroy: " + e.toString(), e);
			}
		}
		
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

}
