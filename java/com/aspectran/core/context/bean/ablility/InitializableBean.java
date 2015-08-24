/**
 * 
 */
package com.aspectran.core.context.bean.ablility;

/**
 * The Interface InitializableBean.
 * 
 * @since 2011. 2. 20.
 */
public interface InitializableBean {

	public static final String INITIALIZE_METHOD_NAME = "initialize";
	
	/**
	 * Invoke on initialization after it has set all bean properties supplied.
	 *
	 * @throws Exception the exception
	 */
	public void initialize() throws Exception;

}
