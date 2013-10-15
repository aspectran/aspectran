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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.web.context.AspectranContextLoader;

public class AspectranContextLoaderListener implements ServletContextListener {

	public static final String ASPECTRAN_CONTEXT_CONFIG_LOCATION_PARAM = "aspectran:contextConfigLocation";
	
	private AspectranContextLoader aspectranContextLoader;
	
	/**
	 * Initialize the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextInitialized(ServletContextEvent event) {
		// context-relative path to our configuration resource for the aspectran
		String contextConfigLocation = event.getServletContext().getInitParameter(ASPECTRAN_CONTEXT_CONFIG_LOCATION_PARAM);
		
		aspectranContextLoader = new AspectranContextLoader(event.getServletContext(), contextConfigLocation);
		event.getServletContext().setAttribute(AspectranContextLoader.ASPECTRAN_CONTEXT_LOADER_ATTRIBUTE, aspectranContextLoader);
	}

	/**
	 * Close the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextDestroyed(ServletContextEvent event) {
		if(aspectranContextLoader != null) {
			event.getServletContext().removeAttribute(AspectranContextLoader.ASPECTRAN_CONTEXT_LOADER_ATTRIBUTE);
			
			AspectranContext aspectranContext = aspectranContextLoader.getAspectranContext();
			
			if(aspectranContext != null) {
				aspectranContext.destroy();
				aspectranContext = null;
			}
		}
	}

}
