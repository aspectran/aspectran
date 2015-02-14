package com.aspectran.core.adapter;


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
	
	@SuppressWarnings("unchecked")
	public <T> T getAdaptee() {
		return (T)adaptee;
	}
	
}
