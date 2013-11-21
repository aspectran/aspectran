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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.bean.scope.ApplicationScope;
import com.aspectran.web.adapter.WebApplicationAdapter;

public class AspectranWebApplicationListener implements ServletContextListener {

	private final Logger logger = LoggerFactory.getLogger(AspectranWebApplicationListener.class);

	private ApplicationAdapter applicationAdapter;

	/**
	 * Initialize the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextInitialized(ServletContextEvent event) {
		logger.info("initializing AspectranApplicationListener...");

		applicationAdapter = WebApplicationAdapter.determineWebApplicationAdapter(event.getServletContext());
	}

	/**
	 * Close the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextDestroyed(ServletContextEvent event) {
		try {
			if(applicationAdapter != null) {
				ApplicationScope scope = (ApplicationScope)applicationAdapter.getAttribute(ApplicationScope.APPLICATION_SCOPE_ATTRIBUTE);

				if(scope != null)
					scope.destroy();
				
				ServletContext servletContext = event.getServletContext();
				servletContext.removeAttribute(WebApplicationAdapter.WEB_APPLICATION_ADAPTER_ATTRIBUTE);
				
				logger.debug("WebApplicationAdapter attribute was removed.");
			}
		} catch(Exception e) {
			logger.error("AspectranApplicationListener failed to destroy cleanly: " + e.toString());
		}

		logger.info("AspectranApplicationListener successful destroyed.");
	}

}
