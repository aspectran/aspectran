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
package com.aspectran.web.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.util.StringUtils;
import com.aspectran.scheduler.AspectranScheduler;
import com.aspectran.scheduler.quartz.QuartzAspectranScheduler;
import com.aspectran.web.adapter.WebApplicationAdapter;

public class SchedulerListener implements ServletContextListener {

	private final Logger logger = LoggerFactory.getLogger(SchedulerListener.class);

	public static final String START_DELAY_SECONDS_PARAM = "aspectran:scheduler:startDelaySeconds";

	public static final String WAIT_ON_SHUTDOWN_PARAM = "aspectran:scheduler:waitOnShutdown";
	
	public static final String ASPECTRAN_SCHEDULER_ATTRIBUTE = 
			AspectranScheduler.class.getName() + ".ASPECTRAN_SCHEDULER";
	
	private AspectranScheduler aspectranScheduler;
	
	private boolean waitOnShutdown;

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
			
			servletContext.setAttribute(ASPECTRAN_SCHEDULER_ATTRIBUTE, aspectranScheduler);
			logger.debug("AspectranScheduler attribute was saved.");
			
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
		if(aspectranScheduler != null) {
			try {
				aspectranScheduler.shutdown(waitOnShutdown);
			
				ServletContext servletContext = event.getServletContext();
				servletContext.removeAttribute(ASPECTRAN_SCHEDULER_ATTRIBUTE);
				
				logger.debug("AspectranScheduler attribute was removed.");
				logger.info("AspectranScheduler successful shutdown.");
			} catch(Exception e) {
				logger.error("AspectranScheduler failed to shutdown cleanly: " + e.toString(), e);
			}
		}
	}
	
	private AspectranContext getAspectranContext(ServletContext servletContext) {
		WebApplicationAdapter webApplicationAdapter = WebApplicationAdapter.determineWebApplicationAdapter(servletContext);
		
		if(webApplicationAdapter != null)
			return webApplicationAdapter.getAspectranContext();
		
		return null;
	}

}
