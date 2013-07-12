package com.aspectran.web.adapter;

import javax.servlet.ServletContext;

import com.aspectran.base.adapter.AbstractApplicationAdapter;
import com.aspectran.base.adapter.ApplicationAdapter;
import com.aspectran.core.bean.scope.ApplicationScope;

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
	public WebApplicationAdapter(ServletContext servletContext) {
		super(servletContext);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractApplicationAdapter#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		return ((ServletContext)adaptee).getAttribute(name);
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractApplicationAdapter#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object value) {
		if(ApplicationScope.APPLICATION_SCOPE_ATTRIBUTE.equals(name))
			throw new IllegalArgumentException("The specified attribute name is not allowed. Reserved attribute name '" + name + "'");
		
		((ServletContext)adaptee).setAttribute(name, value);
	}

}
