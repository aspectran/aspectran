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
	
	public Object getAdaptee() {
		return adaptee;
	}
	
	public abstract String getCharacterEncoding();
	
	public abstract void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException;
	
	public abstract String getContentType();

	public abstract void setContentType(String contentType);
	
	public abstract OutputStream getOutputStream() throws IOException;
	
	public abstract Writer getWriter() throws IOException;
	
	public abstract void redirect(String reqeustUri) throws IOException;
	
	public abstract String redirect(CoreActivity activity, RedirectResponseRule redirectResponseRule) throws IOException;
	
}
