package com.aspectran.web.adapter;

import javax.servlet.ServletContext;

import com.aspectran.core.adapter.AbstractApplicationAdapter;
import com.aspectran.core.adapter.ApplicationAdapter;

/**
 * The Class WebApplicationAdapter.
 * 
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public class WebApplicationAdapter extends AbstractApplicationAdapter implements ApplicationAdapter {
	
	public static final String WEB_APPLICATION_ADAPTER_ATTRIBUTE = 
			WebApplicationAdapter.class.getName() + ".WEB_APPLICATION_ADAPTER";
	
	/**
	 * Instantiates a new web application adapter.
	 *
	 * @param servletContext the servlet context
	 */
	public WebApplicationAdapter(ServletContext servletContext) {
		super(servletContext);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractApplicationAdapter#getAttribute(java.lang.String)
	 */
	public synchronized Object getAttribute(String name) {
		return ((ServletContext)adaptee).getAttribute(name);
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractApplicationAdapter#setAttribute(java.lang.String, java.lang.Object)
	 */
	public synchronized void setAttribute(String name, Object value) {
		//if(ApplicationScope.APPLICATION_SCOPE_ATTRIBUTE.equals(name))
		//	throw new IllegalArgumentException("The specified attribute name is not allowed. Reserved attribute name '" + name + "'");
		
		((ServletContext)adaptee).setAttribute(name, value);
	}
	
	public static WebApplicationAdapter determineWebApplicationAdapter(ServletContext servletContext) {
		if(servletContext.getAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE) != null) {
			return (WebApplicationAdapter)servletContext.getAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE);
		}
		
		WebApplicationAdapter webApplicationAdapter = new WebApplicationAdapter(servletContext);
		servletContext.setAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE, webApplicationAdapter);
		
		return webApplicationAdapter;
	}

}
