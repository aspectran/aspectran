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

/**
 * The Class RegulatedApplicationAdapter.
 * 
 * @since 2016. 3. 26.
 */
public class RegulatedApplicationAdapter implements ApplicationAdapter {

	private ApplicationAdapter applicationAdapter;

	/**
	 * Instantiates a new Regulated application adapter.
	 *
	 * @param applicationAdapter the application adapter
	 */
	public RegulatedApplicationAdapter(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}

	@Override
	public <T> T getAdaptee() {
		return applicationAdapter.getAdaptee();
	}

	@Override
	public ApplicationScope getApplicationScope() {
		return applicationAdapter.getApplicationScope();
	}

	@Override
	public <T> T getAttribute(String name) {
		return applicationAdapter.getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		applicationAdapter.setAttribute(name, value);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return applicationAdapter.getAttributeNames();
	}

	@Override
	public void removeAttribute(String name) {
		applicationAdapter.removeAttribute(name);
	}

	@Override
	public ClassLoader getClassLoader() {
		return applicationAdapter.getClassLoader();
	}

	@Override
	public void setClassLoader(ClassLoader classLoader) {
		throw new UnsupportedOperationException();
	}

	public String getBasePath() {
		return applicationAdapter.getBasePath();
	}

	@Override
	public String toRealPath(String filePath) throws IOException {
		return applicationAdapter.toRealPath(filePath);
	}

	@Override
	public File toRealPathAsFile(String filePath) throws IOException {
		return applicationAdapter.toRealPathAsFile(filePath);
	}

}
