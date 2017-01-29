/**
 * Copyright 2008-2016 Juho Jeong
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

/**
 * The Class WebActivityServlet.
 */
public class WebActivityServlet extends HttpServlet implements Servlet {

	/** @serial */
	private static final long serialVersionUID = 6659683668233267847L;

	private static final Log log = LogFactory.getLog(WebActivityServlet.class);

	private WebAspectranService webAspectranService;
	
	private boolean standalone;

	/**
	 * Instantiates a new WebActivityServlet.
	 */
	public WebActivityServlet() {
		super();
	}

	@Override
	public void init() throws ServletException {
		log.info("Initializing WebActivityServlet...");

		try {
			ServletContext servletContext = getServletContext();
			Object attr = servletContext.getAttribute(WebAspectranService.ROOT_WEB_ASPECTRAN_SERVICE_ATTRIBUTE);

			if (attr != null) {
				if (!(attr instanceof WebAspectranService)) {
					throw new IllegalStateException("Context attribute is not of type WebAspectranService: " + attr);
				}
				
				WebAspectranService rootAspectranService = (WebAspectranService)attr;
				webAspectranService = WebAspectranService.newInstance(this, rootAspectranService);
				standalone = (rootAspectranService != webAspectranService);
			} else {
				log.info("AspectranService is running in standalone mode inside the servlet.");

				webAspectranService = WebAspectranService.newInstance(this);
				standalone = true;
			}
		} catch (Exception e) {
			log.error("Unable to initialize WebActivityServlet.", e);
			throw new UnavailableException(e.getMessage());
		}
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
		webAspectranService.service(req, res);
	}

	@Override
	public void destroy() {
		super.destroy();

		if (standalone) {
			log.info("Do not terminate the application server while destroying all scoped beans.");

			webAspectranService.shutdown();

			log.info("Successfully destroyed the Web Activity Servlet: " + this.getServletName());
		}
	}
	
}