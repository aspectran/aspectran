package com.aspectran.core.adapter;


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
	
	@SuppressWarnings("unchecked")
	public <T> T getAdaptee() {
		return (T)adaptee;
	}

}
