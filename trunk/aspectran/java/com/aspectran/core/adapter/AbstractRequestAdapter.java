package com.aspectran.core.adapter;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import com.aspectran.core.activity.request.AbstractRequest;

/**
 * The Class AbstractRequestAdapter.
  *
 * @since 2011. 3. 13.
*/
public abstract class AbstractRequestAdapter extends AbstractRequest implements RequestAdapter {

	/** The adaptee. */
	protected Object adaptee;
	
	/**
	 * Instantiates a new abstract request adapter.
	 *
	 * @param adaptee the adaptee
	 */
	public AbstractRequestAdapter(Object adaptee) {
		this.adaptee = adaptee;
	}
	
	public Object getAdaptee() {
		return adaptee;
	}
	
	public abstract String getCharacterEncoding();
	
	public abstract void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException;
	
	public abstract String getParameter(String name);
	
	public abstract String[] getParameterValues(String name);
	
	public abstract Object getAttribute(String name);
	
	public abstract void setAttribute(String name, Object o);
	
	public abstract Enumeration<String> getAttributeNames();
	
}
