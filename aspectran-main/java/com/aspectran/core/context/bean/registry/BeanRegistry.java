/**
 * 
 */
package com.aspectran.core.context.bean.registry;

import com.aspectran.core.activity.AspectranActivity;

/**
 *
 * @author Gulendol
 *
 * <p>Created: 2012. 11. 9. 오전 11:36:47</p>
 *
 */
public interface BeanRegistry {

	public Object getBean(String id, AspectranActivity activity);
	
	
}
