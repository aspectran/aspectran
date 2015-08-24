/**
 * 
 */
package com.aspectran.core.context.bean.scope;

import java.util.HashMap;
import java.util.Iterator;

/**
 * The Class ScopedBeanMap.
 *
 * @author Gulendol
 * @since 2011. 1. 7.
 */
public class ScopedBeanMap extends HashMap<String, ScopedBean> implements Iterable<ScopedBean> {
	
	/** @serial */
	static final long serialVersionUID = -3559362779320716165L;

	/**
	 * Adds a instantiated bean.
	 *
	 * @param scopedBean the instantiated bean
	 * @return the instantiated bean
	 */
	public ScopedBean putScopeBean(ScopedBean scopedBean) {
		return put(scopedBean.getBeanRule().getId(), scopedBean);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<ScopedBean> iterator() {
		return this.values().iterator();
	}

}
