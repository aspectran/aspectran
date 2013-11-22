package com.aspectran.web.adapter;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.adapter.AbstractApplicationAdapter;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.bean.scope.Scope;

/**
 * The Class WebApplicationAdapter.
 * 
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public class WebApplicationAdapter extends AbstractApplicationAdapter implements ApplicationAdapter {
	
	public static final String WEB_APPLICATION_ADAPTER_ATTRIBUTE = 
			WebApplicationAdapter.class.getName() + ".WEB_APPLICATION_ADAPTER";

	private static final Logger logger = LoggerFactory.getLogger(WebApplicationAdapter.class);
	
	private AspectranContext aspectranContext;
	
	/**
	 * Instantiates a new web application adapter.
	 *
	 * @param servletContext the servlet context
	 */
	public WebApplicationAdapter(ServletContext servletContext, AspectranContext aspectranContext) {
		super(servletContext);
		
		this.aspectranContext = aspectranContext;
		
		if(aspectranContext != null) {
			aspectranContext.setApplicationAdapter(this);
		}
	}

	public AspectranContext getAspectranContext() {
		return aspectranContext;
	}
	
	public synchronized Object getAttribute(String name) {
		return ((ServletContext)adaptee).getAttribute(name);
	}

	public synchronized void setAttribute(String name, Object value) {
		((ServletContext)adaptee).setAttribute(name, value);
	}
	
	public static WebApplicationAdapter determineWebApplicationAdapter(ServletContext servletContext) {
		return (WebApplicationAdapter)servletContext.getAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE);
	}
	
	public static WebApplicationAdapter createWebApplicationAdapter(ServletContext servletContext, AspectranContext aspectranContext) {
		WebApplicationAdapter webApplicationAdapter = new WebApplicationAdapter(servletContext, aspectranContext);
		servletContext.setAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE, webApplicationAdapter);
		
		logger.debug("WebApplicationAdapter attribute was saved.");
		
		return webApplicationAdapter;
	}

	public static void destoryWebApplicationAdapter(ServletContext servletContext) {
		WebApplicationAdapter applicationAdapter = determineWebApplicationAdapter(servletContext);
		
		if(applicationAdapter != null) {
			Scope scope = applicationAdapter.getScope();
	
			if(scope != null)
				scope.destroy();
			
			AspectranContext aspectranContext = applicationAdapter.getAspectranContext();
			
			if(aspectranContext != null) {
				aspectranContext.destroy();
			}
		}
		
		if(servletContext.getAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE) != null) {
			servletContext.removeAttribute(WebApplicationAdapter.WEB_APPLICATION_ADAPTER_ATTRIBUTE);
			logger.debug("WebApplicationAdapter attribute was removed.");
		}
	}
	
}
