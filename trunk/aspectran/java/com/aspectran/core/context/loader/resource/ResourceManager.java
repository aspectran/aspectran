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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.util.ResourceUtils;


/**
 * <p>Created: 2014. 12. 18 오후 5:51:13</p>	
 */
public class ResourceManager {
	
	private final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

	private final String resourceLocation;
	
	private final Map<String, URL> resourcePool = new LinkedHashMap<String, URL>();
	
	private final Map<String, Class<?>> classPool = new HashMap<String, Class<?>>();
	
	private final AspectranClassLoader owner;
	
	private final boolean archived;
	
	public ResourceManager(String resourceLocation, AspectranClassLoader owner) {
		File f = new File(resourceLocation);
		
		if(!f.isDirectory())
			throw new InvalidResourceException("invalid resource directory name: " + resourceLocation);
		
		this.resourceLocation = resourceLocation;
		this.owner = owner;
		
		if(resourceLocation.endsWith(ResourceUtils.JAR_FILE_SUFFIX) ||
				resourceLocation.endsWith(ResourceUtils.ZIP_FILE_SUFFIX)) {
			this.archived = true;
			//findResource(resourceLocation);
		} else {
			this.archived = false;
			findResource();
		}
	}

	public String getResourceLocation() {
		return resourceLocation;
	}
	
	protected boolean isArchived() {
		return archived;
	}

	public URL getResource(String name) {
		return resourcePool.get(name);
	}
	
	public Enumeration<URL> getResources() {
		final Iterator<URL> currentResources = resourcePool.values().iterator();
		
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
		final Iterator<Map.Entry<String, URL>> currentResources = resourcePool.entrySet().iterator();
		
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
		synchronized(classPool) {
			Class<?> clazz = classPool.get(name);
			
			if(clazz == null) {
				URL url = resourcePool.get(name);
				
				if(url == null) {
					throw new ResourceNotFoundException(name);
				}
				
				clazz = loadClass(url);
				classPool.put(name, clazz);
			}
			
			return clazz;
		}
	}
	
	protected Class<?> loadClass(URL url) {
		return null;
	}
	
	private void findResource() {
		File dir = new File(resourceLocation);
		
		List<File> jarFileList = new ArrayList<File>(); 
		
		findResource(dir, jarFileList);
		
		if(jarFileList.size() > 0) {
			for(File jarFile : jarFileList) {
				findResourceFroJAR(jarFile);
			}
		}
	}
	
	private void findResource(File target, final List<File> jarFileList) {
		target.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if(file.isDirectory()) {
					findResource(file, jarFileList);
				} else if(file.isFile()) {
					String name = file.getAbsolutePath();
					
					if(name.endsWith(ResourceUtils.JAR_FILE_SUFFIX)) {
						owner.wishBrother(name);
						jarFileList.add(file);
					} else {
						name = name.substring(resourceLocation.length() + 1);
						name = name.replace('\\', ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR);
						
						try {
							URL url = file.toURI().toURL();
							resourcePool.put(name, url);
						} catch(MalformedURLException e) {
							logger.error("invalid resource: " + file, e);
						}
					}
				}
				return false;
			}
		});
	}
	
	private void findResourceFroJAR(File target) {
		
	}

	public void reset() {
		release();
		findResource();
	}
	
	public void release() {
		if(resourcePool != null) {
			resourcePool.clear();
		}

		if(classPool != null) {
			classPool.clear();
		}
	}
}
