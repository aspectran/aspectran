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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.util.ResourceUtils;


/**
 * <p>Created: 2014. 12. 18 오후 5:51:13</p>	
 */
public class ResourceManager {
	
	private final AspectranClassLoader owner;
	
	protected final ResourceEntries resourceEntries = new ResourceEntries();
	
	private final Map<String, Class<?>> classCache = new HashMap<String, Class<?>>();

	public ResourceManager(AspectranClassLoader owner) {
		this.owner = owner;
	}
	
	protected ResourceEntries getResourceEntries() {
		return resourceEntries;
	}

	public URL getResource(String name) {
		return resourceEntries.get(name);
	}
	
	public Enumeration<URL> getResources() {
		final Iterator<AspectranClassLoader> owners = AspectranClassLoader.getAspectranClassLoaders(owner);
		
		return new Enumeration<URL>() {
			private Iterator<Map.Entry<String, URL>> current;
			
			public synchronized boolean hasMoreElements() {
				if(current == null || !current.hasNext()) {
					if(!owners.hasNext())
						return false;
					
					current = owners.next().getResourceManager().getResourceEntries().entrySet().iterator();
				}
				
				return current.hasNext();
			}

			public synchronized URL nextElement() {
				if(current == null)
					current = owners.next().getResourceManager().getResourceEntries().entrySet().iterator();

				return current.next().getValue();
			}
		};
	}
	
	public Enumeration<URL> getResources(String name) throws IOException {
		return getResources(name, null);
	}
	
	public Enumeration<URL> getResources(String name, final Enumeration<URL> inherited) throws IOException {
		if(name.endsWith(ResourceUtils.RESOURCE_NAME_SPEPARATOR))
			name = name.substring(0, name.length() - 1);
		
		System.out.println("find resource from parent: " + name);
		System.out.println("parent results: " + inherited);
		
		while(inherited.hasMoreElements()) {
			System.out.println("p: " + inherited.nextElement().toString());
		}
		
		System.out.println("find resource from self: " + name);
		final Iterator<AspectranClassLoader> owners = AspectranClassLoader.getAspectranClassLoaders(owner);
		final String filterName = name;
		
		return new Enumeration<URL>() {
			private Iterator<Map.Entry<String, URL>> current;
			private Map.Entry<String, URL> entry;
			private boolean nomore; //for parent
			
			private boolean hasNext() {
				while(true) {
					if(current == null) {
						if(!owners.hasNext())
							return false;
						
						current = owners.next().getResourceManager().getResourceEntries().entrySet().iterator();
					}
					
					while(current.hasNext()) {
						Map.Entry<String, URL> entry2 = current.next();
						//System.out.println("current: " + entry2.getKey());
						
						//if(entry2.getKey().startsWith(filterName)) {
						if(entry2.getKey().equals(filterName)) {
							entry = entry2;
							return true;
						}
					}
					
					current = null;
				}
			}
			
			public synchronized boolean hasMoreElements() {
				if(entry != null)
					return true;
				
				if(!nomore) {
					if(inherited != null && inherited.hasMoreElements())
						return true;
					else
						nomore = true;
				}

				return hasNext();
			}

			public synchronized URL nextElement() {
				if(entry == null) {
					if(!nomore) {
						if(inherited != null && inherited.hasMoreElements())
							return inherited.nextElement();
					}

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
				URL url = resourceEntries.get(name);
				
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
	}
	
	public void release() {
		resourceEntries.clear();
		classCache.clear();
	}
	
	public URL[] extractResources() {
		Enumeration<URL> res = getResources();
		List<URL> resources = new LinkedList<URL>();
		
		URL url = null;
		
		while(res.hasMoreElements()) {
			url = res.nextElement();
			
			if(!ResourceUtils.URL_PROTOCOL_JAR.equals(url.getProtocol()))
				resources.add(url);
		}
		
		return resources.toArray(new URL[resources.size()]);
	}
	
}
