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
package com.aspectran.core.context.loader.resource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;

import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.ResourceUtils;


/**
 * <p>Created: 2014. 12. 24 오후 4:54:13</p>
 */
public class ResourceEntries extends LinkedHashMap<String, URL> {

	/** @serial */
	private static final long serialVersionUID = -6936820061673430782L;
	
	private final List<URL> jarFileList = new LinkedList<URL>(); 

	public ResourceEntries() {
		super();
	}
	
	public void putResource(String resourceName, File file) {
		URL url;
		
		try {
			url = file.toURI().toURL();
		} catch(MalformedURLException e) {
			throw new InvalidResourceException("invalid resource file: " + file, e);
		}
		
		putResource(resourceName, url);
	}
	
	public void putResource(File file, JarEntry entry) {
		String resourceName = entry.getName();
		
		//"jar:file:///C:/proj/parser/jar/parser.jar!/test.xml"
		URL url;
		
		try {
			url = new URL(ResourceUtils.JAR_URL_PREFIX + file.toURI() + ResourceUtils.JAR_URL_SEPARATOR + resourceName);
		} catch(MalformedURLException e) {
			throw new InvalidResourceException("invalid resource jar file: " + file, e);
		}
		
		putResource(resourceName, url);
	}
	
	public void putResource(String resourceName, URL url) {
		if(resourceName.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
			String className = resourceToClassName(resourceName);
			put(className, url);
		} else {
			put(resourceName, url);
		}
	}
	
	public void addJarFile(URL url) {
		jarFileList.add(url);
	}
	
	public void addJarFile(File file) {
		URL url;
		
		try {
			url = file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new InvalidResourceException("invalid resource jar file: " + file, e);
		}

		addJarFile(url);
	}
	
	public List<URL> getJarFileList() {
		return jarFileList;
	}

	public void clear() {
		super.clear();
		jarFileList.clear();
	}
	
	private static String resourceToClassName(String resourceName) {
		String className = resourceName.substring(0, resourceName.length() - ClassUtils.CLASS_FILE_SUFFIX.length());
		className = className.replace(ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR, ClassUtils.PACKAGE_SEPARATOR_CHAR);
		return className;
	}
	
}
