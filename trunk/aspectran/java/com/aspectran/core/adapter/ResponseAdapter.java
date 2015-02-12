package com.aspectran.core.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.context.rule.RedirectResponseRule;

/**
 * The Interface ResponseAdapter.
 *
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public interface ResponseAdapter {

	/**
	 * Gets the adaptee.
	 *
	 * @return the adaptee
	 */
	public Object getAdaptee();
	
	/**
	 * Gets the character encoding.
	 *
	 * @return the character encoding
	 */
	public String getCharacterEncoding();
	
	/**
	 * Sets the character encoding.
	 *
	 * @param characterEncoding the new character encoding
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException;
	
	/**
	 * Gets the content type.
	 *
	 * @return the content type
	 */
	public abstract String getContentType();

	/**
	 * Sets the content type.
	 *
	 * @param contentType the new content type
	 */
	public abstract void setContentType(String contentType);

	/**
	 * Gets the output stream.
	 *
	 * @return the output stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public OutputStream getOutputStream() throws IOException;
	
	/**
	 * Gets the writer.
	 *
	 * @return the writer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Writer getWriter() throws IOException;
	
	/**
	 * Redirect.
	 *
	 * @param requestUri the request uri
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void redirect(String requestUri) throws IOException;
	
	/**
	 * Redirect.
	 *
	 * @param activity the translet
	 * @param redirectResponseRule the redirect response rule
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String redirect(CoreActivity activity, RedirectResponseRule redirectResponseRule) throws IOException;
	
}
