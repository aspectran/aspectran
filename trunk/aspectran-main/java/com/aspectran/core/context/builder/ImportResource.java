/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.context.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.var.type.ImportResourceType;

/**
 * <p>Created: 2008. 04. 24 오전 11:23:36</p>
 * 
 * @author Gulendol
 */
public class ImportResource {
	
	private ClassLoader classLoader;

	private ImportResourceType importResourceType;
	
	private String resource;
	
	public ImportResource(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	/**
	 * Gets the resource.
	 * 
	 * @return the resource
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * Sets the resource.
	 * 
	 * @param resource the new resource
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setResource(String resource) throws IOException {
		this.importResourceType = ImportResourceType.RESOURCE;
		this.resource = resource;
	}
	
	/**
	 * Sets the resource.
	 * 
	 * @param resource the new resource
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setFile(String file) throws IOException {
		this.importResourceType = ImportResourceType.FILE;
		this.resource = file;
	}
	
	/**
	 * Sets the resource.
	 * 
	 * @param resource the new resource
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setUrl(String url) throws IOException {
		this.importResourceType = ImportResourceType.URL;
		this.resource = url;
	}
	
	/**
	 * Gets the input stream.
	 * 
	 * @return the input stream
	 */
	public InputStream getInputStream() throws IOException {
		InputStream inputStream = null;
		
		if(importResourceType == ImportResourceType.RESOURCE) {
			inputStream = classLoader.getResourceAsStream(resource);
			
			if(inputStream == null) {
				throw new IOException("Could not find resource " + resource);
			}
		} else if(importResourceType == ImportResourceType.FILE) {
			File file = new File(resource);
			inputStream = new FileInputStream(file);
		} else if(importResourceType == ImportResourceType.URL) {
			inputStream = ResourceUtils.getUrlAsStream(resource);
		}
		
		return inputStream;
	}
}
