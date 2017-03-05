/**
 * Copyright 2008-2017 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.web.startup.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.aspectran.core.service.AspectranService;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.service.WebAspectranService;

/**
 * The listener interface for receiving AspectranService events.
 */
public class AspectranServiceListener implements ServletContextListener {

	private static final Log log = LogFactory.getLog(AspectranServiceListener.class);

	private AspectranService aspectranService;

	@Override
	public void contextInitialized(ServletContextEvent event) {
		log.info("Initializing AspectranServiceListener...");
		
		try {
			aspectranService = WebAspectranService.build(event.getServletContext());
		} catch (Exception e) {
			log.error("AspectranServiceListener initialization failed.", e);
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		if (aspectranService != null) {
			log.info("Do not terminate the server while the all scoped bean destroying.");

			aspectranService.shutdown();
			aspectranService = null;

			log.info("Successfully destroyed AspectranServiceListener: " + this);
		}
	}
	
}
