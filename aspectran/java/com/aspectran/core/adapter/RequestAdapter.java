package com.aspectran.core.adapter;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import com.aspectran.core.var.FileItemMap;

/**
 * The Interface RequestAdapter.
 *
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public interface RequestAdapter {
	
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
	 */
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException;
	
	/**
	 * Gets the parameter.
	 *
	 * @param name the name
	 * @return the parameter
	 */
	public String getParameter(String name);
	
	/**
	 * Gets the parameter values.
	 *
	 * @param name the name
	 * @return the parameter values
	 */
	public String[] getParameterValues(String name);
	
	public Enumeration<String> getParameterNames();
	
	/**
	 * Returns the value of the named attribute as an <code>Object</code>, or <code>null</code> if no attribute of the given name exists.
	 *
	 * @param name the name
	 * @return the attribute
	 */
	public Object getAttribute(String name);
	
	/**
	 * Stores an attribute in this request.
	 *
	 * @param name the name
	 * @param o the value
	 */
	public void setAttribute(String name, Object value);
	
	/**
	 * Returns an <code>Enumeration</code> containing the
	 * names of the attributes available to this request.
	 * This method returns an empty <code>Enumeration</code>
	 * if the request has no attributes available to it.
	 *
	 * @return the attribute names
	 */
	public Enumeration<String> getAttributeNames();
	
	public void removeAttribute(String name);
	
	public FileItemMap getFileItemMap();

	public void setFileItemMap(FileItemMap fileItemMap);

	public FileItemMap touchFileItemMap();
	
	/**
	 * Checks if is max length exceeded.
	 *
	 * @return true, if is max length exceeded
	 */
	public boolean isMaxLengthExceeded();

	/**
	 * Sets the max length exceeded.
	 *
	 * @param maxLengthExceeded the new max length exceeded
	 */
	public void setMaxLengthExceeded(boolean maxLengthExceeded);
	
}
