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
package com.aspectran.core.adapter;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import com.aspectran.core.context.bean.scope.ApplicationScope;
import com.aspectran.core.service.AspectranServiceController;

/**
 * The Interface ApplicationAdapter.
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public interface ApplicationAdapter {
	
	/**
	 * Gets the Adaptee object.
	 *
	 * @param <T> the generic type
	 * @return the Adaptee object
	 */
	public <T> T getAdaptee();

	/**
	 * Gets the application scope.
	 *
	 * @return the scope
	 */
	public ApplicationScope getApplicationScope();
	
	/**
	 * Gets the attribute.
	 *
	 * @param <T> the generic type
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
	
	/**
	 * Gets the attribute names.
	 *
	 * @return the attribute names
	 */
	public Enumeration<String> getAttributeNames();
	
	/**
	 * Removes the attribute.
	 *
	 * @param name the name
	 */
	public void removeAttribute(String name);
	
	/**
	 * Gets the aspectran service controller.
	 *
	 * @return the aspectran service controller
	 */
	public AspectranServiceController getAspectranServiceController();

	/**
	 * Gets the class loader.
	 *
	 * @return the class loader
	 */
	public ClassLoader getClassLoader();
	
	/**
	 * Return the base path that the current application is mapped to.
	 *
	 * @return the application base path
	 */
	public String getApplicationBasePath();
	
	/**
	 * Returns to convert the given file path with the real file path.
	 *
	 * @param filePath the specified file path
	 * @return the real file path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String toRealPath(String filePath) throws IOException;
	
	/**
	 * Returns to convert the given file path with the real file path.
	 * 
	 * @param filePath the specified file path
	 * @return the real file path
	 */
	public File toRealPathAsFile(String filePath);

}
