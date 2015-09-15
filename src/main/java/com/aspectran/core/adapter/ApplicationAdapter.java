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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.service.AspectranServiceController;

/**
 * The Interface ApplicationAdapter.
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public interface ApplicationAdapter {
	
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
	
	public Enumeration<String> getAttributeNames();
	
	public void removeAttribute(String name);
	
	public AspectranServiceController getAspectranServiceController();

	public ClassLoader getClassLoader();
	
	public String getApplicationBasePath();
	
	public String toRealPath(String filePath) throws IOException;
	
	public File toRealPathAsFile(String filePath);

}
