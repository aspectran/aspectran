/**
 * 
 */
package com.aspectran.core.context.bean.scope;


/**
 *
 * @author Gulendol
 * @since 2011. 3. 12.
 *
 */
public interface Scope {
	
	public ScopedBeanMap getScopeBeanMap();

	public void setScopedBeanMap(ScopedBeanMap scopedBeanMap);
	
	public void destroy();
	
}
