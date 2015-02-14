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
	
	private final String applicationBasePath;
	
	/**
	 * Instantiates a new web application adapter.
	 *
	 * @param servletContext the servlet context
	 */
	public WebApplicationAdapter(ServletContext servletContext) {
		super(servletContext);
		
		this.applicationBasePath = servletContext.getRealPath("/");
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)((ServletContext)adaptee).getAttribute(name);
	}

	public void setAttribute(String name, Object value) {
		((ServletContext)adaptee).setAttribute(name, value);
	}
	
	public String getApplicationBasePath() {
		return applicationBasePath;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{applicationBasePath=").append(applicationBasePath);
		sb.append("}");
		
		return sb.toString();
	}
	
}
