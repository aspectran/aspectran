/**
 * 
 */
package com.aspectran.core.context.bean.ablility;

/**
 *
 * @author Gulendol
 * @since 2011. 2. 20.
 *
 */
public interface DisposableBean {

	public static final String DESTROY_METHOD_NAME = "destroy";

	/**
	 * Invoke on destruction of a singleton.
	 *
	 * @throws Exception the exception
	 */
	public void destroy() throws Exception;

}
