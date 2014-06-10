/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.web.startup.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.ActivityContextException;
import com.aspectran.core.context.reload.ActivityContextReloadingHandler;
import com.aspectran.core.context.reload.ActivityContextReloadingTimer;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.var.option.Options;
import com.aspectran.scheduler.AspectranScheduler;
import com.aspectran.scheduler.quartz.QuartzAspectranScheduler;
import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.activity.WebActivityDefaultHandler;
import com.aspectran.web.activity.WebActivityImpl;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.startup.listener.AspectranSchedulerOptions;
import com.aspectran.web.startup.loader.ActivityContextLoader;
import com.aspectran.web.startup.loader.ActivityContextObservedLoader;
import com.aspectran.web.startup.loader.WebActivityContextClassLoader;

/**
 * Servlet implementation class for Servlet: Translets.
 */
public class WebActivityServlet extends HttpServlet implements Servlet {

	/** @serial */
	static final long serialVersionUID = 6659683668233267847L;

	private final Logger logger = LoggerFactory.getLogger(WebActivityServlet.class);

	protected ActivityContext activityContext;

	private AspectranScheduler aspectranScheduler;
	
	private ActivityContextReloadingTimer contextReloadingTimer;
	
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	/**
	 * Instantiates a new action servlet.
	 */
	public WebActivityServlet() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		logger.info("initializing WebActivityServlet...");

		try {
			ServletContext servletContext = getServletContext();
			
			String contextConfigLocation = getServletConfig().getInitParameter(ActivityContextLoader.CONTEXT_CONFIG_LOCATION_PARAM);
			String autoReloadingParam = getServletConfig().getInitParameter("autoReloading");

			/*
			observingPath: [
							classpath:**
							WEB-INF/config/sqlmap/**
						]
						observationInterval: 5
						startup: false
			*/
			Options autoReloadingOptions = new AutoReloadingOptions(autoReloadingParam);
			String[] observingPaths = autoReloadingOptions.getStringArray(AutoReloadingOptions.observingPath);
			int observationInterval = autoReloadingOptions.getInt(AutoReloadingOptions.observationInterval, -1);
			boolean autoReloadingStartup = autoReloadingOptions.getBoolean(AspectranSchedulerOptions.startup, true);

			if(observingPaths == null || observingPaths.length == 0)
				autoReloadingStartup = false;
			
			if(StringUtils.hasText(contextConfigLocation)) {
				ClassLoader classLoader = new WebActivityContextClassLoader();
				
				if(!autoReloadingStartup) {
					ActivityContextLoader loader = new ActivityContextLoader(servletContext, contextConfigLocation, classLoader);
					activityContext = loader.load();
				} else {
					if(observationInterval == -1) {
						logger.info("Option 'observationInterval' is not specified, defaulting to 10 seconds.");
						observationInterval = 10;
					}

					ActivityContextReloadingHandler contextReloadingHandler = new ActivityContextReloadingHandler() {
						public void handle(ActivityContext newActivityContext) {
							reload(newActivityContext);
						}
					};
					
					ActivityContextObservedLoader observedLoader = new ActivityContextObservedLoader(servletContext, contextConfigLocation, classLoader);
					activityContext = observedLoader.load();
					contextReloadingTimer = observedLoader.startTimer(contextReloadingHandler, observingPaths, observationInterval);
				}
			}
			
			if(activityContext == null)
				new ActivityContextException("ActivityContext is not loaded.");
			
			initScheduler();
			
		} catch(Exception e) {
			logger.error("WebActivityServlet was failed to initialize: " + e.toString(), e);
			throw new UnavailableException(e.getMessage());
		}
	}

	protected void initScheduler() throws Exception {
		String schedulerParam = getServletConfig().getInitParameter("scheduler");
		
		if(StringUtils.hasText(schedulerParam)) {
			Options schedulerOptions = new SchedulerOptions(schedulerParam);
			Integer startDelaySeconds = (Integer)schedulerOptions.getValue(SchedulerOptions.startDelaySeconds);
			Boolean waitOnShutdown = (Boolean)schedulerOptions.getValue(SchedulerOptions.waitOnShutdown);
			Boolean startup = (Boolean)schedulerOptions.getValue(SchedulerOptions.startup);
			
			if(!Boolean.FALSE.equals(startup)) {
				aspectranScheduler = new QuartzAspectranScheduler(activityContext);
				
				if(Boolean.TRUE.equals(waitOnShutdown))
					aspectranScheduler.setWaitOnShutdown(true);
				
				if(startDelaySeconds == null) {
					logger.info("Scheduler option 'startDelaySeconds' is not specified. So defaulting to 5 seconds.");
					startDelaySeconds = 5;
				}
				
				aspectranScheduler.startup(startDelaySeconds);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		WebActivity activity = null;

		try {
			String requestUri = req.getRequestURI();

			activity = new WebActivityImpl(activityContext, req, res);
			activity.init(requestUri);
			activity.run();
			activity.close();

		} catch(TransletNotFoundException e) {
			if(activity != null) {
				String activityDefaultHandler = activityContext.getActivityDefaultHandler();

				if(activityDefaultHandler != null) {
					try {
						WebActivityDefaultHandler handler = (WebActivityDefaultHandler)activity.getBean(activityDefaultHandler);
						handler.setServletContext(getServletContext());
						handler.handle(req, res);
					} catch(Exception e2) {
						res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						logger.error(e.getMessage(), e2);
					}

					return;
				}
			}

			logger.error(e.getMessage());
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();

		if(contextReloadingTimer != null)
			contextReloadingTimer.cancel();
		
		boolean cleanlyDestoryed = true;

		if(!shutdownScheduler())
			cleanlyDestoryed = false;

		if(!destroyContext())
			cleanlyDestoryed = false;
		
		try {
			WebApplicationAdapter.destoryWebApplicationAdapter(getServletContext());
		} catch(Exception e) {
			cleanlyDestoryed = false;
			logger.error("WebApplicationAdapter was failed to destroy: " + e.toString(), e);
		}

		if(cleanlyDestoryed)
			logger.info("WebActivityServlet was destroyed successfully.");
		else
			logger.error("WebActivityServlet was failed to destroy cleanly.");

		logger.info("Do not terminate the server while the all scoped bean destroying.");
	}
	
	protected boolean shutdownScheduler() {
		if(aspectranScheduler != null) {
			try {
				aspectranScheduler.shutdown();
				logger.info("AspectranScheduler has been shutdown successfully.");
			} catch(Exception e) {
				logger.error("AspectranScheduler was failed to shutdown cleanly: " + e.toString(), e);
				return false;
			}
		}
		
		return true;
	}
	
	protected boolean destroyContext() {
		if(activityContext != null) {
			try {
				activityContext.destroy();
				logger.info("AspectranContext was destroyed successfully.");
			} catch(Exception e) {
				logger.error("AspectranContext was failed to destroy: " + e.toString(), e);
				return false;
			}
		}
		
		return true;
	}
	
	protected void reload(ActivityContext newActivityContext) {
		if(contextReloadingTimer != null)
			contextReloadingTimer.cancel();
		
		shutdownScheduler();
		destroyContext();
		
		activityContext = newActivityContext;
		
		try {
			initScheduler();
		} catch(Exception e) {
			logger.error("Scheduler was failed to initialize: " + e.toString(), e);
		}
		
		if(contextReloadingTimer != null)
			contextReloadingTimer.start();
	}
	
}