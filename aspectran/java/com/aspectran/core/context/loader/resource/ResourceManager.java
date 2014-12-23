/*
 *  Copyright (c) 2009 Jeong Ju Ho, All rights reserved.
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
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.ResourceUtils;


/**
 * <p>Created: 2014. 12. 18 오후 5:51:13</p>	
 */
public class ResourceManager {
	
	private final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

	private final String resourceLocation;
	
	private final AspectranClassLoader owner;
	
	private final boolean archived;
	
	private final Map<String, URL> resourceEntryMap = new LinkedHashMap<String, URL>();
	
	private final Map<String, Class<?>> classCache = new HashMap<String, Class<?>>();

	private final List<File> jarFileList = new ArrayList<File>(); 
	
	public ResourceManager(String resourceLocation, AspectranClassLoader owner) {
		File f = new File(resourceLocation);
		
		if(!f.isDirectory())
			throw new InvalidResourceException("invalid resource directory name: " + resourceLocation);
		
		this.resourceLocation = resourceLocation;
		this.owner = owner;
		
		if(resourceLocation.endsWith(ResourceUtils.JAR_FILE_SUFFIX) ||
				resourceLocation.endsWith(ResourceUtils.ZIP_FILE_SUFFIX)) {
			this.archived = true;
		} else {
			this.archived = false;
		}
		
		findResource();
	}

	public String getResourceLocation() {
		return resourceLocation;
	}
	
	protected boolean isArchived() {
		return archived;
	}

	public URL getResource(String name) {
		return resourceEntryMap.get(name);
	}
	
	public Enumeration<URL> getResources() {
		final Iterator<URL> currentResources = resourceEntryMap.values().iterator();
		
		return new Enumeration<URL>() {
			public boolean hasMoreElements() {
				return currentResources.hasNext();
			}
			
			public URL nextElement() {
				return currentResources.next();
			}
		};
	}
	
	public Enumeration<URL> getResources(String name) {
		final String filterName = name;
		final Iterator<Map.Entry<String, URL>> currentResources = resourceEntryMap.entrySet().iterator();
		
		return new Enumeration<URL>() {
			private Map.Entry<String, URL> entry;
			
			public synchronized boolean hasMoreElements() {
				if(entry != null)
					return true;
				
				while(currentResources.hasNext()) {
					Map.Entry<String, URL> entry2 = currentResources.next();
					
					if(entry2.getKey().startsWith(filterName)) {
						entry = entry2;
						return true;
					}					
				}
				
				return false;
			}

			public synchronized URL nextElement() {
				if(entry == null) {
					if(!hasMoreElements())
						throw new NoSuchElementException();
				}

				URL url = entry.getValue();
				entry = null;

				return url;
			}
		};
	}
	
	public Class<?> loadClass(String name) throws ResourceNotFoundException {
		synchronized(classCache) {
			Class<?> clazz = classCache.get(name);
			
			if(clazz == null) {
				URL url = resourceEntryMap.get(name);
				
				if(url == null) {
					throw new ResourceNotFoundException(name);
				}
				
				clazz = loadClass(url);
				classCache.put(name, clazz);
			}
			
			return clazz;
		}
	}
	
	protected Class<?> loadClass(URL url) {
		return null;
	}
	
	public void reset() {
		release();
		findResource();
	}
	
	public void release() {
		resourceEntryMap.clear();
		classCache.clear();
		jarFileList.clear();
	}
	
	private void findResource() {
		try {
			File file = new File(resourceLocation);
			
			if(archived) {
				findResourceFromJAR(file);
				jarFileList.add(file);
			} else {
				findResource(file);
				
				if(jarFileList.size() > 0) {
					for(File jarFile : jarFileList) {
						findResourceFromJAR(jarFile);
					}
				}
			}
		} catch(Exception e) {
			throw new InvalidResourceException("Faild to find resource from " + resourceLocation, e);
		}
	}
	
	private void findResource(File target) {
		target.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if(file.isDirectory()) {
					findResource(file);
				} else if(file.isFile()) {
					String filePath = file.getAbsolutePath();
					
					if(filePath.endsWith(ResourceUtils.JAR_FILE_SUFFIX)) {
						owner.wishBrother(filePath);
						jarFileList.add(file);
					} else {
						String resourceName = filePath.substring(resourceLocation.length() + 1);
						resourceName = resourceName.replace('\\', ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR);
						
						URL url;
						try {
							url = file.toURI().toURL();
						} catch(MalformedURLException e) {
							throw new InvalidResourceException("invalid resource: " + filePath, e);
						}
						
						if(resourceName.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
							String className = resourceToClassName(resourceName);
							resourceEntryMap.put(className, url);
						} else {
							resourceEntryMap.put(resourceName, url);
						}
					}
				}
				return false;
			}
		});
	}
	
	private void findResourceFromJAR(File target) throws IOException {
		JarFile jarFile = null;
		
		try {
			jarFile = new JarFile(target);
			
			for(Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				String resourceName = entry.getName();

				//"jar:file:/C:/proj/parser/jar/parser.jar!/test.xml"
				URL url = new URL(ResourceUtils.JAR_URL_PREFIX + target.toURI() + ResourceUtils.JAR_URL_SEPARATOR + resourceName);
				
				if(resourceName.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
					String className = resourceToClassName(resourceName);
					resourceEntryMap.put(className, url);
				} else {
					resourceEntryMap.put(resourceName, url);
				}
			}
			
		} catch(IOException e) {
			logger.error("invalid resource: " + target, e);
		} finally {
			if(jarFile != null)
				jarFile.close();
		}
	}
	
	private static String resourceToClassName(String resourceName) {
		String className = resourceName.substring(0, resourceName.length() - ClassUtils.CLASS_FILE_SUFFIX.length());
		className = className.replace(ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR, ClassUtils.PACKAGE_SEPARATOR_CHAR);
		return className;
	}
	
	public static void main(String[] args) {
		try {
			File file = new File("/c:/Users/Gulendol/Projects/aspectran/ADE/workspace/aspectran.example/webapp/WEB-INF/lib/cglib-nodep-3.1.jar");
			
			JarFile jarFile = new JarFile(file);
			System.out.println(file.toURI().toString());
			for(Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				System.out.println(entry);
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
