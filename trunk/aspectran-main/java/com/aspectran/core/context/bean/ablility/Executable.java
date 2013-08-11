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
public interface Executable {

	public static final String EXECUTE_METHOD_NAME = "execute";

	/**
	 * Invoke on destruction of a singleton.
	 *
	 * @throws Exception the exception
	 */
	public void execute() throws Exception;

}
