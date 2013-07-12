package com.aspectran.base.adapter;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import com.aspectran.core.activity.request.AbstractRequest;

/**
 * The Class AbstractRequestAdapter.
  *
 * @author Gulendol
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
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.RequestAdapter#getAdaptee()
	 */
	public Object getAdaptee() {
		return adaptee;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.RequestAdapter#getCharacterEncoding()
	 */
	public abstract String getCharacterEncoding();
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.RequestAdapter#setCharacterEncoding(java.lang.String)
	 */
	public abstract void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException;
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.RequestAdapter#getParameter(java.lang.String)
	 */
	public abstract String getParameter(String name);
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.RequestAdapter#getParameterValues(java.lang.String)
	 */
	public abstract String[] getParameterValues(String name);
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.RequestAdapter#getAttribute(java.lang.String)
	 */
	public abstract Object getAttribute(String name);
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.RequestAdapter#setAttribute(java.lang.String, java.lang.Object)
	 */
	public abstract void setAttribute(String name, Object o);
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.RequestAdapter#getAttributeNames()
	 */
	public abstract Enumeration<String> getAttributeNames();
	
}
