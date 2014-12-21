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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.ActivityContextLoader;
import com.aspectran.core.context.loader.ActivityContextLoadingManager;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.activity.WebActivityDefaultHandler;
import com.aspectran.web.activity.WebActivityImpl;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.startup.loader.WebAspectranClassLoader;
import com.aspectran.web.startup.loader.WebActivityContextLoader;

/**
 * Servlet implementation class for Servlet: Translets.
 */
public class WebActivityServlet extends HttpServlet implements Servlet {

	/** @serial */
	static final long serialVersionUID = 6659683668233267847L;

	private final Logger logger = LoggerFactory.getLogger(WebActivityServlet.class);
	
	private ActivityContextLoadingManager activityContextLoadingManager;

	protected ActivityContext activityContext;
	
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
			
			String aspectranConfigText = getServletConfig().getInitParameter(WebActivityContextLoader.ASPECTRAN_CONFIG_PARAM);
			
			AspectranConfig aspectranConfig = new AspectranConfig(aspectranConfigText);
			
			AspectranClassLoader aspectranClassLoader = new WebAspectranClassLoader();
			
			ApplicationAdapter applicationAdapter = WebApplicationAdapter.determineWebApplicationAdapter(servletContext);
			
			ActivityContextLoader activityContextLoader = new WebActivityContextLoader(applicationAdapter, aspectranClassLoader);
			
			activityContextLoadingManager = new ActivityContextLoadingManager(aspectranConfig);
			
			activityContext = activityContextLoadingManager.createActivityContext(activityContextLoader);
			
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

		boolean cleanlyDestoryed = activityContextLoadingManager.destroyActivityContext();
		
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
	
}