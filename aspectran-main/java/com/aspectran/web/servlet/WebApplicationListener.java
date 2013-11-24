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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.util.StringUtils;
import com.aspectran.web.adapter.WebApplicationAdapter;

public class WebApplicationListener implements ServletContextListener {

	private final Logger logger = LoggerFactory.getLogger(WebApplicationListener.class);

	private static final String ASPECTRAN_CONTEXT_CONFIG_LOCATION_PARAM = "aspectran:contextConfigLocation";
	
	private AspectranContext aspectranContext;

	public AspectranContext getAspectranContext() {
		return aspectranContext;
	}

	/**
	 * Initialize the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextInitialized(ServletContextEvent event) {
		logger.info("initializing WebApplicationListener...");

		// context-relative path to our configuration resource for the aspectran
		String contextConfigLocation = event.getServletContext().getInitParameter(ASPECTRAN_CONTEXT_CONFIG_LOCATION_PARAM);

		if(StringUtils.hasText(contextConfigLocation)) {
			AspectranContextLoader loader = new AspectranContextLoader(event.getServletContext(), contextConfigLocation);
			aspectranContext = loader.getAspectranContext();
		}
		
		WebApplicationAdapter.createWebApplicationAdapter(event.getServletContext(), aspectranContext);
	}

	/**
	 * Close the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextDestroyed(ServletContextEvent event) {
		try {
			WebApplicationAdapter.destoryWebApplicationAdapter(event.getServletContext());
		} catch(Exception e) {
			logger.error("WebApplicationListener failed to destroy cleanly: " + e.toString());
		}

		logger.info("WebApplicationListener successful destroyed.");
	}

}
