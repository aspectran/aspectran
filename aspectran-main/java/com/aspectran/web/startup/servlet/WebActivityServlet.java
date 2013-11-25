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

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.AspectranContextException;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.core.util.StringUtils;
import com.aspectran.scheduler.AspectranScheduler;
import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.activity.WebActivityDefaultHandler;
import com.aspectran.web.activity.WebActivityImpl;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.startup.AspectranContextLoader;

/**
 * Servlet implementation class for Servlet: Translets.
 */
public class WebActivityServlet extends HttpServlet implements Servlet {

	/** @serial */
	static final long serialVersionUID = 6659683668233267847L;

	private final Logger logger = LoggerFactory.getLogger(WebActivityServlet.class);

	protected AspectranContext aspectranContext;

	private AspectranScheduler aspectranScheduler;
	
	//private boolean standalone;

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
			
			WebApplicationAdapter applicationAdapter = WebApplicationAdapter.determineWebApplicationAdapter(servletContext);
			
			String contextConfigLocation = getServletConfig().getInitParameter(AspectranContextLoader.CONTEXT_CONFIG_LOCATION_PARAM);

			if(StringUtils.hasText(contextConfigLocation)) {
				AspectranContextLoader aspectranContextLoader = new AspectranContextLoader(servletContext, contextConfigLocation);
				aspectranContext = aspectranContextLoader.getAspectranContext();
				aspectranContext.setApplicationAdapter(applicationAdapter);

//				standalone = true;
//			} else {
//				if(applicationAdapter != null)
//					aspectranContext = applicationAdapter.getAspectranContext();
			}
			
			if(aspectranContext == null)
				new AspectranContextException("AspectranContext is not found.");

			//aspectranScheduler = new QuartzAspectranScheduler(aspectranContext);
			//aspectranScheduler.startup(startDelaySeconds);
			
		} catch(Exception e) {
			logger.error("WebActivityServlet failed to initialize: " + e.toString());
			throw new UnavailableException(e.getMessage());
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

			activity = new WebActivityImpl(aspectranContext, req, res);
			activity.init(requestUri);
			activity.run();

		} catch(TransletNotFoundException e) {
			if(activity != null) {
				String activityDefaultHandler = aspectranContext.getActivityDefaultHandler();

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

		boolean cleanlyDestoryed = true;

		if(aspectranScheduler != null) {
			try {
				aspectranScheduler.shutdown();
				logger.info("AspectranScheduler successful shutdown.");
			} catch(Exception e) {
				cleanlyDestoryed = false;
				logger.error("AspectranScheduler failed to shutdown cleanly: " + e.toString(), e);
			}
		}

		if(aspectranContext != null) {
			try {
				aspectranContext.destroy();
				logger.info("AspectranContext successful destroyed.");
			} catch(Exception e) {
				cleanlyDestoryed = false;
				logger.error("AspectranContext failed to destroy: " + e.toString(), e);
			}
		}
		
		try {
			WebApplicationAdapter.destoryWebApplicationAdapter(getServletContext());
		} catch(Exception e) {
			cleanlyDestoryed = false;
			logger.error("WebApplicationAdapter failed to destroy: " + e.toString(), e);
		}

		if(cleanlyDestoryed)
			logger.info("WebActivityServlet successful destroyed.");
		else
			logger.error("WebActivityServlet failed to destroy cleanly.");

		logger.info("Do not terminate the server while the all scoped bean destroying.");
	}
}