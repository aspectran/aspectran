package com.aspectran.core.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.context.rule.RedirectResponseRule;

/**
 * The Class AbstractResponseAdapter.
 *
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public abstract class AbstractResponseAdapter implements ResponseAdapter {

	/** The adaptee. */
	protected Object adaptee;
	
	/**
	 * Instantiates a new abstract response adapter.
	 *
	 * @param adaptee the adaptee
	 */
	public AbstractResponseAdapter(Object adaptee) {
		this.adaptee = adaptee;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.ResponseAdapter#getAdaptee()
	 */
	public Object getAdaptee() {
		return adaptee;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.ResponseAdapter#getCharacterEncoding()
	 */
	public abstract String getCharacterEncoding();
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.ResponseAdapter#setCharacterEncoding(java.lang.String)
	 */
	public abstract void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException;
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.ResponseAdapter#getContentType()
	 */
	public abstract String getContentType();

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.ResponseAdapter#setContentType(java.lang.String)
	 */
	public abstract void setContentType(String contentType);
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.ResponseAdapter#getOutputStream()
	 */
	public abstract OutputStream getOutputStream() throws IOException;
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.ResponseAdapter#getWriter()
	 */
	public abstract Writer getWriter() throws IOException;
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.ResponseAdapter#redirect(java.lang.String)
	 */
	public abstract void redirect(String reqeustUri) throws IOException;
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.ResponseAdapter#redirect(org.jhlabs.translets.activity.Activity, org.jhlabs.translets.context.rule.RedirectResponseRule)
	 */
	public abstract String redirect(CoreActivity activity, RedirectResponseRule redirectResponseRule) throws IOException;
	
}
