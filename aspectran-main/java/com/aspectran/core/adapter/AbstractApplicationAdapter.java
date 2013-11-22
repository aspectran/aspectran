package com.aspectran.core.adapter;

import com.aspectran.core.context.bean.scope.ApplicationScope;

/**
 * The Class AbstractApplicationAdapter.
  *
 * @since 2011. 3. 13.
*/
public abstract class AbstractApplicationAdapter implements ApplicationAdapter {
	
	protected ApplicationScope scope = new ApplicationScope();
	
	/** The adaptee. */
	protected Object adaptee;
	
	/**
	 * Instantiates a new abstract session adapter.
	 *
	 * @param adaptee the adaptee
	 */
	public AbstractApplicationAdapter(Object adaptee) {
		this.adaptee = adaptee;
	}
	
	public ApplicationScope getScope() {
		return scope;
	}

	public Object getAdaptee() {
		return adaptee;
	}
	
	public abstract Object getAttribute(String name);

	public abstract void setAttribute(String name, Object value);

}
