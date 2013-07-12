/**
 * 
 */
package com.aspectran.core.bean;

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Gulendol
 * @since 2011. 1. 7.
 *
 */
public class ScopeBeanMap extends HashMap<String, ScopeBean> implements Iterable<ScopeBean> {
	
	/** @serial */
	static final long serialVersionUID = -3559362779320716165L;

	/**
	 * Adds a instantiated bean.
	 *
	 * @param scopeBean the instantiated bean
	 * @return the instantiated bean
	 */
	public ScopeBean putScopeBean(ScopeBean scopeBean) {
		return put(scopeBean.getBeanRule().getId(), scopeBean);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<ScopeBean> iterator() {
		return this.values().iterator();
	}

}
