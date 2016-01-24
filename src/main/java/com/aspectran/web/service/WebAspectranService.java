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
package com.aspectran.web.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContextException;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.loader.config.AspectranContextConfig;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.core.service.AspectranServiceControllerListener;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreAspectranService;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.startup.listener.AspectranServiceListener;
import com.aspectran.web.startup.servlet.WebActivityServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The Class WebAspectranService.
 */
public class WebAspectranService extends CoreAspectranService {
	
	private static final Log log = LogFactory.getLog(CoreAspectranService.class);
	
	public static final String ASPECTRAN_CONFIG_PARAM = "aspectran:config";

	public static final String ASPECTRAN_DEFAULT_SERVLET_NAME_PARAM = "aspectran:defaultServletName";
	
	private static final String DEFAULT_ROOT_CONTEXT = "/WEB-INF/aspectran/config/aspectran-config.xml";
	
	private DefaultServletHttpRequestHandler defaultServletHttpRequestHandler;
	
	protected long pauseTimeout;
	
	public WebAspectranService(ServletContext servletContext) {
		WebApplicationAdapter waa = new WebApplicationAdapter(this, servletContext);
		setApplicationAdapter(waa);
	}
	
	private void initialize(String aspectranConfigParam) throws AspectranServiceException {
		AspectranConfig aspectranConfig;
		
		if(aspectranConfigParam != null) {
			aspectranConfig = new AspectranConfig(aspectranConfigParam);
		} else {
			aspectranConfig = new AspectranConfig();
		}

		Parameters contextParameters = aspectranConfig.getParameters(AspectranConfig.context);

		if(contextParameters == null) {
			contextParameters = aspectranConfig.newParameters(AspectranConfig.context);
		}

		String rootContext = contextParameters.getString(AspectranContextConfig.root);

		if(rootContext == null || rootContext.length() == 0) {
			contextParameters.putValue(AspectranContextConfig.root, DEFAULT_ROOT_CONTEXT);
		}

		initialize(aspectranConfig);
	}
	
	/**
	 * Process the actual dispatching to the activity. 
	 *
	 * @param request current HTTP servlet request
	 * @param response current HTTP servlet response
	 * @throws IOException if an input or output error occurs while the activity is handling the HTTP request
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String requestUri = request.getRequestURI();

		if(pauseTimeout > 0L) {
			if(pauseTimeout >= System.currentTimeMillis()) {
				log.info("aspectran service is paused, did not respond to the request uri: " + requestUri);
				response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				return;
			} else {
				pauseTimeout = 0L;
			}
		}
		
		try {
			Activity activity = new WebActivity(activityContext, request, response);
			activity.ready(requestUri, request.getMethod());
			activity.perform();
			activity.finish();
		} catch(TransletNotFoundException e) {
			if(log.isTraceEnabled()) {
				log.trace("translet is not found: " + requestUri);
			}
			try {
				if(!defaultServletHttpRequestHandler.handle(request, response)) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			} catch(Exception e2) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				log.error(e.getMessage(), e2);
			}
		} catch(Exception e) {
			log.error("WebActivity service failed.", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Sets the default servlet http request handler.
	 *
	 * @param servletContext the servlet context
	 * @param defaultServletName the default servlet name
	 */
	public void setDefaultServletHttpRequestHandler(ServletContext servletContext, String defaultServletName) {
		defaultServletHttpRequestHandler = new DefaultServletHttpRequestHandler(servletContext);
		if(defaultServletName != null)
			defaultServletHttpRequestHandler.setDefaultServletName(defaultServletName);
	}

	/**
	 * Returns a new instance of WebAspectranService.
	 *
	 * @param servletContext the servlet context
	 * @return the web aspectran service
	 * @throws AspectranServiceException the aspectran service exception
	 */
	public static WebAspectranService newInstance(ServletContext servletContext) throws AspectranServiceException {
		String aspectranConfigParam = servletContext.getInitParameter(ASPECTRAN_CONFIG_PARAM);
		WebAspectranService aspectranService = newInstance(servletContext, aspectranConfigParam);
		
		String defaultServletName = servletContext.getInitParameter(ASPECTRAN_DEFAULT_SERVLET_NAME_PARAM);
		aspectranService.setDefaultServletHttpRequestHandler(servletContext, defaultServletName);
		
		servletContext.setAttribute(AspectranServiceListener.ASPECTRAN_SERVICE_ATTRIBUTE, aspectranService);
		log.debug("AspectranServiceListener attribute in ServletContext was created. " + AspectranServiceListener.ASPECTRAN_SERVICE_ATTRIBUTE + ": " + aspectranService);
		
		return aspectranService;
	}

	/**
	 * Returns a new instance of WebAspectranService.
	 *
	 * @param servlet the web activity servlet
	 * @return the web aspectran service
	 * @throws AspectranServiceException the aspectran service exception
	 */
	public static WebAspectranService newInstance(WebActivityServlet servlet) throws AspectranServiceException {
		ServletContext servletContext = servlet.getServletContext();
		ServletConfig servletConfig = servlet.getServletConfig();
		
		String aspectranConfigParam = servletConfig.getInitParameter(ASPECTRAN_CONFIG_PARAM);
		WebAspectranService aspectranService = newInstance(servletContext, aspectranConfigParam);
		
		String defaultServletName = servletConfig.getInitParameter(ASPECTRAN_DEFAULT_SERVLET_NAME_PARAM);
		aspectranService.setDefaultServletHttpRequestHandler(servletContext, defaultServletName);
		
		return aspectranService;
	}

	/**
	 * Returns a new instance of WebAspectranService.
	 *
	 * @param servlet the servlet
	 * @param rootAspectranService the root aspectran service
	 * @return the web aspectran service
	 */
	public static WebAspectranService newInstance(WebActivityServlet servlet, WebAspectranService rootAspectranService) throws AspectranServiceException {
		ServletContext servletContext = servlet.getServletContext();
		ServletConfig servletConfig = servlet.getServletConfig();
		
		String aspectranConfigParam = servletConfig.getInitParameter(ASPECTRAN_CONFIG_PARAM);
		String defaultServletName = servletConfig.getInitParameter(ASPECTRAN_DEFAULT_SERVLET_NAME_PARAM);
		
		if(aspectranConfigParam != null) {
			WebAspectranService aspectranService = newInstance(servletContext, aspectranConfigParam);
			aspectranService.setDefaultServletHttpRequestHandler(servletContext, defaultServletName);

			return aspectranService;
		} else {
			return rootAspectranService;
		}
	}

	/**
	 * Returns a new instance of WebAspectranService.
	 *
	 * @param servletContext the servlet context
	 * @param aspectranConfigParam the aspectran config param
	 * @return the web aspectran service
	 * @throws AspectranServiceException the aspectran service exception
	 */
	private static WebAspectranService newInstance(ServletContext servletContext, String aspectranConfigParam) throws AspectranServiceException {
		WebAspectranService aspectranService = new WebAspectranService(servletContext);
		aspectranService.initialize(aspectranConfigParam);
		
		WebAspectranService.addAspectranServiceControllerListener(aspectranService);
		
		aspectranService.startup();
		
		return aspectranService;
	}
	
	private static void addAspectranServiceControllerListener(final WebAspectranService aspectranService) {
		aspectranService.setAspectranServiceControllerListener(new AspectranServiceControllerListener() {
			public void started() {
				aspectranService.pauseTimeout = 0;
			}
			
			public void refreshed() {
				started();
			}
			
			public void paused(long timeout) {
				if(timeout <= 0)
					timeout = 315360000000L; //86400000 * 365 * 10 = 10 Years;
				
				aspectranService.pauseTimeout = System.currentTimeMillis() + timeout;
			}
			
			public void resumed() {
				aspectranService.pauseTimeout = 0;
			}
			
			public void stopped() {
				paused(-1L);
			}
		});
	}

}
