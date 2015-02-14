package com.aspectran.core.adapter;

import com.aspectran.core.context.bean.scope.Scope;

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
	public <T> T getAdaptee();
	
	/**
	 * Gets the scope.
	 *
	 * @return the scope
	 */
	public Scope getScope();
	
	/**
	 * Gets the attribute.
	 *
	 * @param name the name
	 * @return the attribute
	 */
	public <T> T getAttribute(String name);

	/**
	 * Sets the attribute.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public void setAttribute(String name, Object value);
	
}
