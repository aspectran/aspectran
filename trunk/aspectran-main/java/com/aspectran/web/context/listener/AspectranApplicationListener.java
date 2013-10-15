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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.bean.scope.ApplicationScope;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.context.AspectranContextLoader;

public class AspectranApplicationListener implements ServletContextListener {

	private ApplicationAdapter applicationAdapter;
	
	/**
	 * Initialize the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextInitialized(ServletContextEvent event) {
		applicationAdapter = WebApplicationAdapter.determineWebApplicationAdapter(event.getServletContext());
		event.getServletContext().setAttribute(WebApplicationAdapter.WEB_APPLICATION_ADAPTER_ATTRIBUTE, applicationAdapter);
	}

	/**
	 * Close the translets root context.
	 * 
	 * @param event the event
	 */
	public void contextDestroyed(ServletContextEvent event) {
		if(applicationAdapter != null) {
			event.getServletContext().removeAttribute(WebApplicationAdapter.WEB_APPLICATION_ADAPTER_ATTRIBUTE);
			
			ApplicationScope scope = (ApplicationScope)applicationAdapter.getAttribute(ApplicationScope.APPLICATION_SCOPE_ATTRIBUTE);
			
			if(scope != null)
				scope.destroy();
		}
	}

}
