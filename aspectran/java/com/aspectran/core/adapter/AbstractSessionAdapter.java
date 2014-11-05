package com.aspectran.core.adapter;

import com.aspectran.core.context.bean.scope.Scope;

/**
 * The Class AbstractSessionAdapter.
 *
 * @since 2011. 3. 13.
 */
public abstract class AbstractSessionAdapter implements SessionAdapter {
	
	/** The adaptee. */
	protected Object adaptee;
	
	/**
	 * Instantiates a new abstract session adapter.
	 *
	 * @param adaptee the adaptee
	 */
	public AbstractSessionAdapter(Object adaptee) {
		this.adaptee = adaptee;
	}
	
	public Object getAdaptee() {
		return adaptee;
	}
	
	public abstract Scope getScope();
	
	public abstract Object getAttribute(String name);

	public abstract void setAttribute(String name, Object value);

}
