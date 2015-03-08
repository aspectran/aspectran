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

import com.aspectran.web.service.WebAspectranService;
import com.aspectran.web.startup.listener.AspectranServiceListener;

/**
 * Servlet implementation class for Servlet: Translets.
 */
public class WebActivityServlet extends HttpServlet implements Servlet {

	/** @serial */
	static final long serialVersionUID = 6659683668233267847L;

	private static final Logger logger = LoggerFactory.getLogger(WebActivityServlet.class);

	private WebAspectranService aspectranService;
	
	private boolean standalone;

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
			
			WebAspectranService rootAspectranService = (WebAspectranService)servletContext.getAttribute(AspectranServiceListener.ASPECTRAN_SERVICE_ATTRIBUTE);

			if(rootAspectranService == null) {
				logger.info("Standalone AspectranService.");
				aspectranService = WebAspectranService.newInstance(this);
				standalone = true;
			} else {
				logger.info("Root AspectranService exists.");
				aspectranService = WebAspectranService.newInstance(this, rootAspectranService);
				standalone = (rootAspectranService != aspectranService);
			}
		} catch(Exception e) {
			//logger.error("WebActivityServlet was failed to initialize", e);
			throw new UnavailableException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
		aspectranService.service(this, req, res);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();

		if(standalone) {
			boolean cleanlyDestoryed = aspectranService.dispose();
			
			if(cleanlyDestoryed)
				logger.info("Successfully destroyed WebActivityServlet: " + this.getServletName());
			else
				logger.error("WebActivityServlet were not destroyed cleanly: " + this.getServletName());
	
			logger.info("Do not terminate the server while the all scoped bean destroying.");
		}
	}
	
}