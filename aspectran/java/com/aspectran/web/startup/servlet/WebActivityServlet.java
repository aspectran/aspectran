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
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.core.service.AspectranService;
import com.aspectran.core.service.AspectranServiceControllerListener;
import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.activity.WebActivityDefaultHandler;
import com.aspectran.web.activity.WebActivityImpl;
import com.aspectran.web.service.WebAspectranService;
import com.aspectran.web.startup.listener.AspectranServiceListener;

/**
 * Servlet implementation class for Servlet: Translets.
 */
public class WebActivityServlet extends HttpServlet implements Servlet {

	/** @serial */
	static final long serialVersionUID = 6659683668233267847L;

	private final Logger logger = LoggerFactory.getLogger(WebActivityServlet.class);

	private AspectranService aspectranService;

	protected ActivityContext activityContext;
	
	private long pauseTimeout;
	
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
		logger.info("Initializing WebActivityServlet...");

		try {
			ServletContext servletContext = getServletContext();
			ServletConfig servletConfig = getServletConfig();
			
			String aspectranConfigParam = servletConfig.getInitParameter(WebAspectranService.ASPECTRAN_CONFIG_PARAM);

			if(aspectranConfigParam == null) {
				aspectranService = (AspectranService)servletContext.getAttribute(AspectranServiceListener.ASPECTRAN_SERVICE_ATTRIBUTE);
				
				if(aspectranService != null)
					aspectranService = aspectranService.createWrapperAspectranService();
			}

			if(aspectranService == null) {
				AspectranClassLoader aspectranClassLoader = new AspectranClassLoader();
				aspectranService = new WebAspectranService(servletContext, aspectranConfigParam, aspectranClassLoader);
			}
			
			aspectranService.setAspectranServiceControllerListener(new AspectranServiceControllerListener() {
				public void started() {
					activityContext = aspectranService.getActivityContext();
					pauseTimeout = 0;
				}
				
				public void restarted() {
					started();
				}

				public void paused(long timeout) {
					if(timeout <= 0)
						timeout = 315360000000L; //86400000 * 365 * 10 = 10 Years;
					pauseTimeout = System.currentTimeMillis() + timeout;
				}
				
				public void resumed() {
					pauseTimeout = 0;
				}

				public void stopped() {
					paused(-1L);
				}
			});
			
			aspectranService.start();
			
		} catch(Exception e) {
			logger.error("WebActivityServlet was failed to initialize: " + e.toString(), e);
			throw new UnavailableException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String requestUri = req.getRequestURI();

		if(pauseTimeout > 0L) {
			if(pauseTimeout >= System.currentTimeMillis()) {
				logger.info("aspectran service is paused, did not respond to the request uri [" + requestUri + "]");
				res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				return;
			} else {
				pauseTimeout = 0L;
			}
		}
		
		WebActivity activity = null;

		try {
			activity = new WebActivityImpl(activityContext, req, res);
			activity.ready(requestUri);
			activity.perform();
			activity.finish();
		} catch(TransletNotFoundException e) {
			if(activity != null) {
				String activityDefaultHandler = activityContext.getActivityDefaultHandler();

				if(activityDefaultHandler != null) {
					try {
						System.out.println("activity.getBean(activityDefaultHandler):" + activity.getBean(activityDefaultHandler));
						
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

		boolean cleanlyDestoryed = aspectranService.dispose();
		
		if(cleanlyDestoryed)
			logger.info("Successfully destroyed WebActivityServlet: " + this.getServletName());
		else
			logger.error("WebActivityServlet were not destroyed cleanly: " + this.getServletName());

		logger.info("Do not terminate the server while the all scoped bean destroying.");
	}
	
}