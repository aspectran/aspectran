package com.aspectran.core.adapter;

import com.aspectran.core.activity.request.AbstractRequest;

/**
 * The Class AbstractRequestAdapter.
  *
 * @since 2011. 3. 13.
*/
public abstract class AbstractRequestAdapter extends AbstractRequest implements RequestAdapter {

	/** The adaptee. */
	protected Object adaptee;
	
	/**
	 * Instantiates a new abstract request adapter.
	 *
	 * @param adaptee the adaptee
	 */
	public AbstractRequestAdapter(Object adaptee) {
		this.adaptee = adaptee;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAdaptee() {
		return (T)adaptee;
	}
	
}
