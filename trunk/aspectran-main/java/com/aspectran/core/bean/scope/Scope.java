/**
 * 
 */
package com.aspectran.core.bean.scope;

import com.aspectran.core.bean.ScopeBeanMap;

/**
 *
 * @author Gulendol
 * @since 2011. 3. 12.
 *
 */
public interface Scope {
	
	public ScopeBeanMap getScopeBeanMap();

	public void setInstantiatedBeanMap(ScopeBeanMap instantiatedBeanMap);
	
	public void destroy();
	
}
