/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.bean;

/**
 * The Interface BeanRegistry.
 *
 * @author Juho Jeong
 * @since 2012. 11. 9
 */
public interface BeanRegistry extends BeanFactory {

	/**
	 * Return an instance.
	 *
	 * @param <T> the generic type
	 * @param id the id of the bean to retrieve
	 * @return an instance of the bean
	 */
	public <T> T getBean(String id);
	
	/**
	 * Return the bean instance that matches the given object type.
	 *
	 * @param <T> the generic type
	 * @param classType type the bean must match; can be an interface or superclass. null is disallowed.
	 * @return an instance of the bean
	 * @since 1.3.1
	 */
	public <T> T getBean(Class<T> classType);

	/**
	 * Return an instance.
	 * If the bean is not of the required type then throw a BeanNotOfRequiredTypeException.
	 *
	 * @param <T> the generic type
	 * @param id the id of the bean to retrieve
	 * @param requiredType type the bean must match; can be an interface or superclass. null is disallowed.
	 * @return an instance of the bean
	 * @since 1.3.1
	 */
	public <T> T getBean(String id, Class<T> requiredType);

	public <T> T getConfigBean(Class<T> requiredType);

}
