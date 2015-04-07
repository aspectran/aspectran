/**
 * 
 */
package com.aspectran.core.context.bean.ablility;

/**
 * The Interface FactoryBean.
 *
 * @param <T> the generic type
 * 
 * @since 2015. 4. 2.
 */
public interface FactoryBean<T> {

	public T getObject() throws Exception;

}
