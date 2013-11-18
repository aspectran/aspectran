/**
 * 
 */
package com.aspectran.core.context.bean;

import com.aspectran.core.activity.CoreActivity;

/**
 *
 * @author Gulendol
 *
 * <p>Created: 2012. 11. 9. 오전 11:36:47</p>
 *
 */
public interface BeanRegistry {

	public Object getBean(String id, CoreActivity activity);
	
	public void destroy();
	
}
