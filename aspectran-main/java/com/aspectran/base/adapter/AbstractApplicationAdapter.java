package com.aspectran.base.adapter;

/**
 * The Class AbstractApplicationAdapter.
  *
 * @author Gulendol
 * @since 2011. 3. 13.
*/
public abstract class AbstractApplicationAdapter implements ApplicationAdapter {
	
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
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.SessionAdapter#getAdaptee()
	 */
	public Object getAdaptee() {
		return adaptee;
	}
	
	/**
	 * Sets the adaptee.
	 *
	 * @param adaptee the new adaptee
	 */
	public void setAdaptee(Object adaptee) {
		this.adaptee = adaptee;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.SessionAdapter#getAttribute(java.lang.String)
	 */
	public abstract Object getAttribute(String name);

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.SessionAdapter#setAttribute(java.lang.String, java.lang.Object)
	 */
	public abstract void setAttribute(String name, Object value);

}
