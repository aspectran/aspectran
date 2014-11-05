package com.aspectran.web.adapter;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;

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
	
	public String getCharacterEncoding() {
		return ((HttpServletRequest)adaptee).getCharacterEncoding();
	}
	
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
		((HttpServletRequest)adaptee).setCharacterEncoding(characterEncoding);
	}
	
	public String getParameter(String name) {
		return ((HttpServletRequest)adaptee).getParameter(name);
	}
	
	public String[] getParameterValues(String name) {
		return ((HttpServletRequest)adaptee).getParameterValues(name);
	}
	
	public Enumeration<String> getParameterNames() {
		return ((HttpServletRequest)adaptee).getParameterNames();
	}
	
	public Object getAttribute(String name) {
		return ((HttpServletRequest)adaptee).getAttribute(name);
	}
	
	public void setAttribute(String name, Object o) {
		((HttpServletRequest)adaptee).setAttribute(name, o);
	}
	
	public Enumeration<String> getAttributeNames() {
		return ((HttpServletRequest)adaptee).getAttributeNames();

	}

	public void removeAttribute(String name) {
		((HttpServletRequest)adaptee).removeAttribute(name);
	}
}
