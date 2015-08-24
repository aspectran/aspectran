package com.aspectran.web.adapter;

import java.util.Enumeration;

import javax.servlet.ServletContext;

import com.aspectran.core.adapter.AbstractApplicationAdapter;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.service.AspectranService;

/**
 * The Class WebApplicationAdapter.
 * 
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public class WebApplicationAdapter extends AbstractApplicationAdapter implements ApplicationAdapter {
	
	/**
	 * Instantiates a new web application adapter.
	 *
	 * @param servletContext the servlet context
	 */
	public WebApplicationAdapter(AspectranService aspectranService, ServletContext servletContext) {
		super(aspectranService, servletContext);
		super.setApplicationBasePath(servletContext.getRealPath("/"));
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)((ServletContext)adaptee).getAttribute(name);
	}
	
	public void setAttribute(String name, Object o) {
		((ServletContext)adaptee).setAttribute(name, o);
	}
	
	public Enumeration<String> getAttributeNames() {
		return ((ServletContext)adaptee).getAttributeNames();
	}

	public void removeAttribute(String name) {
		((ServletContext)adaptee).removeAttribute(name);
	}
	
}
