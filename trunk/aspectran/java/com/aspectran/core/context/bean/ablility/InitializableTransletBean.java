/**
 * 
 */
package com.aspectran.core.context.bean.ablility;

import com.aspectran.core.activity.Translet;

/**
 *
 * @author Gulendol
 * @since 2015. 3. 30.
 *
 */
public interface InitializableTransletBean {

	/**
	 * Invoke on initialization after it has set all bean properties supplied.
	 *
	 * @throws Exception the exception
	 */
	public void initialize(Translet translet) throws Exception;

}
