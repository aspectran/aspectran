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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.ActivityContextLoader;
import com.aspectran.core.context.loader.ActivityContextLoadingManager;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.startup.loader.AspectranWebClassLoader;
import com.aspectran.web.startup.loader.WebActivityContextLoader;

public class AspectranSchedulerListener implements ServletContextListener {

	private final Logger logger = LoggerFactory.getLogger(AspectranSchedulerListener.class);

	private ActivityContextLoadingManager activityContextLoadingManager;

	protected ActivityContext activityContext;

	/**
	 * Initialize the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextInitialized(ServletContextEvent event) {
		logger.info("initializing AspectranScheduler...");

		try {
			ServletContext servletContext = event.getServletContext();
			
			String aspectranConfigText = servletContext.getInitParameter(WebActivityContextLoader.ASPECTRAN_CONFIG_PARAM);
			
			AspectranConfig aspectranConfig = new AspectranConfig(aspectranConfigText);
			
			AspectranClassLoader aspectranClassLoader = new AspectranWebClassLoader();
			
			ApplicationAdapter applicationAdapter = WebApplicationAdapter.determineWebApplicationAdapter(servletContext);
			
			ActivityContextLoader activityContextLoader = new WebActivityContextLoader(applicationAdapter, aspectranClassLoader);
			
			activityContextLoadingManager = new ActivityContextLoadingManager(aspectranConfig);
			
			activityContext = activityContextLoadingManager.createActivityContext(activityContextLoader);
			
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
		boolean cleanlyDestoryed = activityContextLoadingManager.destroyActivityContext();
		
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
	
}
