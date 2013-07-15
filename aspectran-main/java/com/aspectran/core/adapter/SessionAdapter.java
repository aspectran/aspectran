package com.aspectran.core.adapter;

/**
 * The Interface SessionAdapter.
 *
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public interface SessionAdapter {

	/**
	 * Gets the adaptee.
	 *
	 * @return the adaptee
	 */
	public Object getAdaptee();
	
	/**
	 * Gets the attribute.
	 *
	 * @param name the name
	 * @return the attribute
	 */
	public Object getAttribute(String name);

	/**
	 * Sets the attribute.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public void setAttribute(String name, Object value);
	
}
