package com.aspectran.web.adapter;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.aspectran.base.adapter.AbstractRequestAdapter;
import com.aspectran.base.adapter.RequestAdapter;

/**
 * The Class HttpServletRequestAdapter.
 * 
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public class HttpServletRequestAdapter extends AbstractRequestAdapter implements RequestAdapter {
	
	/**
	 * Instantiates a new http servlet request adapter.
	 *
	 * @param request the request
	 */
	public HttpServletRequestAdapter(HttpServletRequest request) {
		super(request);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractRequestAdapter#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return ((HttpServletRequest)adaptee).getCharacterEncoding();
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractRequestAdapter#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
		((HttpServletRequest)adaptee).setCharacterEncoding(characterEncoding);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractRequestAdapter#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		return ((HttpServletRequest)adaptee).getParameter(name);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractRequestAdapter#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String name) {
		return ((HttpServletRequest)adaptee).getParameterValues(name);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractRequestAdapter#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		return ((HttpServletRequest)adaptee).getAttribute(name);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractRequestAdapter#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object o) {
		((HttpServletRequest)adaptee).setAttribute(name, o);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractRequestAdapter#getAttributeNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration<String> getAttributeNames() {
		return ((HttpServletRequest)adaptee).getAttributeNames();
	}

}
