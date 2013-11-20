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
package com.aspectran.web.context.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.util.StringUtils;
import com.aspectran.scheduler.AspectranScheduler;
import com.aspectran.scheduler.quartz.QuartzAspectranScheduler;
import com.aspectran.web.context.AspectranContextLoader;

public class AspectranSchedulerListener implements ServletContextListener {

	private final Logger logger = LoggerFactory.getLogger(AspectranSchedulerListener.class);

	public static final String START_DELAY_SECONDS_PARAM = "aspectran:scheduler:startDelaySeconds";

	public static final String WAIT_ON_SHUTDOWN_PARAM = "aspectran:scheduler:waitOnShutdown";
	
	private AspectranScheduler aspectranScheduler;
	
	boolean waitOnShutdown;

	/**
	 * Initialize the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextInitialized(ServletContextEvent event) {
		logger.info("initializing AspectranScheduler...");

		ServletContext servletContext = event.getServletContext();
		
		AspectranContext aspectranContext = getAspectranContext(servletContext);
		
		if(aspectranContext == null) {
			logger.error("AspectranScheduler has not been started. AspectranContext was not found.");
			return;
		}
		
		String startDelaySecondsVal = servletContext.getInitParameter(START_DELAY_SECONDS_PARAM);
		String waitOnShutdownVal = servletContext.getInitParameter(WAIT_ON_SHUTDOWN_PARAM);
		
        int startDelaySeconds = 0;
        try {
            if(StringUtils.hasText(startDelaySecondsVal))
                startDelaySeconds = Integer.parseInt(startDelaySecondsVal);
        } catch(Exception e) {
            logger.error("Cannot parse value of 'startDelaySeconds' to an integer: " + startDelaySecondsVal + ", defaulting to 5 seconds.");
            startDelaySeconds = 5;
        }
        
        waitOnShutdown = Boolean.parseBoolean(waitOnShutdownVal);
		
		try {
			aspectranScheduler = new QuartzAspectranScheduler(aspectranContext);
			aspectranScheduler.startup(startDelaySeconds);
			
			logger.info("AspectranScheduler has been started...");
		} catch(Exception e) {
			logger.error("AspectranScheduler failed to initialize: " + e.toString(), e);
			
			if(aspectranScheduler != null) {
				try {
					aspectranScheduler.shutdown();
				} catch(Exception e1) {
					logger.error("AspectranScheduler failed to shutdown cleanly: " + e.toString(), e);
				}
			}
		}
	}

	/**
	 * Close the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextDestroyed(ServletContextEvent event) {
		try {
			if(aspectranScheduler != null)
				aspectranScheduler.shutdown(waitOnShutdown);
		} catch(Exception e) {
			logger.error("AspectranScheduler failed to shutdown cleanly: " + e.toString(), e);
		}

		logger.info("AspectranScheduler successful shutdown.");
	}
	
	private AspectranContext getAspectranContext(ServletContext servletContext) {
		AspectranContextLoader aspectranContextLoader = (AspectranContextLoader)servletContext.getAttribute(AspectranContextLoader.ASPECTRAN_CONTEXT_LOADER_ATTRIBUTE);
		
		if(aspectranContextLoader != null)
			return aspectranContextLoader.getAspectranContext();
		
		return null;
	}

}
