/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.service.WebAspectranService;
import com.aspectran.web.startup.listener.AspectranServiceListener;

/**
 * The Class WebActivityServlet.
 */
public class WebActivityServlet extends HttpServlet implements Servlet {

	/** @serial */
	private static final long serialVersionUID = 6659683668233267847L;

	private static final Log log = LogFactory.getLog(WebActivityServlet.class);

	private WebAspectranService aspectranService;
	
	private boolean standalone;

	/**
	 * Instantiates a new WebActivityServlet.
	 */
	public WebActivityServlet() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		log.info("Initializing WebActivityServlet...");

		try {
			ServletContext servletContext = getServletContext();
			
			WebAspectranService rootAspectranService = (WebAspectranService)servletContext.getAttribute(AspectranServiceListener.ASPECTRAN_SERVICE_ATTRIBUTE);

			if(rootAspectranService == null) {
				log.info("Running AspectranService in standalone mode inside a servlet.");

				aspectranService = WebAspectranService.newInstance(this);
				
				standalone = true;
			} else {
				aspectranService = WebAspectranService.newInstance(this, rootAspectranService);
				
				standalone = (rootAspectranService != aspectranService);
			}
		} catch(Exception e) {
			throw new UnavailableException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
		aspectranService.service(req, res);
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
				log.info("Successfully destroyed WebActivityServlet: " + this.getServletName());
			else
				log.error("WebActivityServlet were not destroyed cleanly: " + this.getServletName());
	
			log.info("Do not terminate the server while the all scoped bean destroying.");
		}
	}
	
}