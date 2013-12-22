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

import com.aspectran.core.var.type.ImportResourceType;

/**
 * <p>Created: 2008. 04. 24 오전 11:23:36</p>
 * 
 * @author Gulendol
 */
public class FileImportStream {
	
	private final static ImportResourceType importResourceType = ImportResourceType.FILE;
	
	private String resource;

	private String basePath;
	
	private long lastModified;
	
	public FileImportStream(String basePath) {
		this.basePath = basePath;
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
	public void setResource(String file) throws IOException {
		this.resource = file;
	}
	
	/**
	 * Sets the resource.
	 * 
	 * @param resource the new resource
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setFile(String file) throws IOException {
		this.resource = file;
	}
	
	/**
	 * Gets the input stream.
	 * 
	 * @return the input stream
	 */
	public InputStream getInputStream() throws IOException {
		File file;
		
		if(basePath == null)
			file = new File(resource);
		else
			file = new File(basePath, resource);
		
		if(!file.exists() || file.isFile()) {
			throw new IOException("Could not find resource " + file.getAbsolutePath());
		}
		
		lastModified = file.lastModified();
		
		InputStream inputStream = new FileInputStream(file);
		
		return inputStream;
	}

	public long getLastModified() {
		return lastModified;
	}
	
	public ImportResourceType getImportResourceType() {
		return importResourceType;
	}
	
}
