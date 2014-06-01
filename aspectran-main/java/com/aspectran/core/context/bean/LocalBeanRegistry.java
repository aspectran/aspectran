/**
 * 
 */
package com.aspectran.core.context.bean;

import com.aspectran.core.context.aspect.AspectRuleRegistry;


/**
 *
 * @author Gulendol
 *
 * <p>Created: 2012. 11. 9. 오전 11:36:47</p>
 *
 */
public interface LocalBeanRegistry extends BeanRegistry {
	
	public void createSingletonBean(AspectRuleRegistry aspectRuleRegistry);

	public void destroy();

}
