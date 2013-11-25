package com.aspectran.web.adapter;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.adapter.AbstractApplicationAdapter;
import com.aspectran.core.adapter.ApplicationAdapter;
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
	
	private int activityContextCount = 0;
	
	/**
	 * Instantiates a new web application adapter.
	 *
	 * @param servletContext the servlet context
	 */
	public WebApplicationAdapter(ServletContext servletContext) {
		super(servletContext);
	}

	public Object getAttribute(String name) {
		return ((ServletContext)adaptee).getAttribute(name);
	}

	public void setAttribute(String name, Object value) {
		((ServletContext)adaptee).setAttribute(name, value);
	}
	
	public synchronized int getActivityContextCount() {
		return activityContextCount;
	}
	
	private synchronized int increaseActivityContextCount() {
		return ++activityContextCount;
	}
	
	private synchronized int decreaseActivityContextCount() {
		return --activityContextCount;
	}
	
	public static WebApplicationAdapter determineWebApplicationAdapter(ServletContext servletContext) {
		WebApplicationAdapter webApplicationAdapter = (WebApplicationAdapter)servletContext.getAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE);
		
		if(webApplicationAdapter == null)
			webApplicationAdapter = createWebApplicationAdapter(servletContext);

		webApplicationAdapter.increaseActivityContextCount();
		
		return webApplicationAdapter;
	}
	
	public static WebApplicationAdapter getWebApplicationAdapter(ServletContext servletContext) {
		return (WebApplicationAdapter)servletContext.getAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE);
	}
	
	protected static WebApplicationAdapter createWebApplicationAdapter(ServletContext servletContext) {
		WebApplicationAdapter webApplicationAdapter = new WebApplicationAdapter(servletContext);
		servletContext.setAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE, webApplicationAdapter);
		
		logger.debug("WebApplicationAdapter attribute was created.");
		
		return webApplicationAdapter;
	}

	public static void destoryWebApplicationAdapter(ServletContext servletContext) {
		WebApplicationAdapter webApplicationAdapter = getWebApplicationAdapter(servletContext);
		
		if(webApplicationAdapter != null) {
			int activityContextCount = webApplicationAdapter.decreaseActivityContextCount();
			
			if(activityContextCount == 0) {
				Scope scope = webApplicationAdapter.getScope();
		
				if(scope != null)
					scope.destroy();

				if(servletContext.getAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE) != null) {
					servletContext.removeAttribute(WebApplicationAdapter.WEB_APPLICATION_ADAPTER_ATTRIBUTE);
					logger.debug("WebApplicationAdapter attribute was removed.");
				} else {
					logger.debug("WebApplicationAdapter attribute was already removed.");
				}
			}
			
			if(activityContextCount < 0) {
				logger.debug("activityContextCount is less than 0.");
			}
		}
	}
	
}
