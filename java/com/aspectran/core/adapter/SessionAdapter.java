/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.adapter;

import com.aspectran.core.context.bean.scope.Scope;

/**
 * The Interface SessionAdapter.
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public interface SessionAdapter {

	/**
	 * Gets the adaptee.
	 *
	 * @return the adaptee
	 */
	public <T> T getAdaptee();
	
	/**
	 * Gets the scope.
	 *
	 * @return the scope
	 */
	public Scope getScope();
	
	/**
	 * Gets the attribute.
	 *
	 * @param name the name
	 * @return the attribute
	 */
	public <T> T getAttribute(String name);

	/**
	 * Sets the attribute.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public void setAttribute(String name, Object value);
	
}
