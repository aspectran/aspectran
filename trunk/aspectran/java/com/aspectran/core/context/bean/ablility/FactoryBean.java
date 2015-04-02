/**
 * 
 */
package com.aspectran.core.context.bean.ablility;

/**
 *
 * @author Gulendol
 * @since 2015. 4. 2.
 *
 */
public interface FactoryBean<T> {

	public T getObject() throws Exception;

}
