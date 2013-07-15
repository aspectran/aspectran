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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.util.Resources;

/**
 * <p>Created: 2008. 04. 24 오전 11:23:36</p>
 * 
 * @author Gulendol
 */
public class ImportableResource {
	
	private static final Log log = LogFactory.getLog(ImportableResource.class);

	private String resource;
	
	private InputStream inputStream;
	
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
		try {
			this.resource = resource;
			
			if(resource == null)
				return;
			
			inputStream = Resources.getResourceAsStream(resource);
		} catch(IOException e) {
			log.error("Cannot read resource '" + resource + "'");
			throw e;
		}
	}
	
	/**
	 * Sets the url.
	 * 
	 * @param url the new url
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setUrl(String url) throws IOException {
		try {
			resource = url;
			
			if(url == null)
				return;
			
			inputStream = Resources.getUrlAsStream(url);
		} catch(IOException e) {
			log.error("Cannot read url '" + resource + "'");
			throw e;
		}
	}

	/**
	 * Sets the file.
	 * 
	 * @param file the new file
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setFile(File file) throws IOException {
		try {
			resource = file.getAbsolutePath();
			
			inputStream = new FileInputStream(file);
		} catch(IOException e) {
			log.error("Cannot read file '" + resource + "'");
			throw e;
		}
	}

	/**
	 * Gets the input stream.
	 * 
	 * @return the input stream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}
}
