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

import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.util.ResourceUtils;


/**
 * <p>Created: 2014. 12. 18 오후 5:51:13</p>	
 */
public class ResourceManager {
	
	protected final ResourceEntries resourceEntries = new ResourceEntries();
	
	public ResourceManager() {
	}
	
	protected ResourceEntries getResourceEntries() {
		return resourceEntries;
	}
	
	public URL getResource(String name) {
		return resourceEntries.get(name);
	}

	public static Enumeration<URL> getResources(final Iterator<AspectranClassLoader> owners) {
		
		return new Enumeration<URL>() {
			private Iterator<URL> values;
			private URL next;
			private URL current;

			private boolean hasNext() {
				while(true) {
					if(values == null) {
						if(!owners.hasNext())
							return false;
						
						values = owners.next().getResourceManager().getResourceEntries().values().iterator();
					}
					
					while(values.hasNext()) {
						next = values.next();
						return true;
					}
					
					values = null;
				}
			}
			
			public synchronized boolean hasMoreElements() {
				if(next != null)
					return true;
				
				return hasNext();
			}

			public synchronized URL nextElement() {
				if(next == null) {
					if(!hasNext())
						throw new NoSuchElementException();
				}

				current = next;
				next = null;

				return current;
			}
		};
	}

	public static Enumeration<URL> getResources(final Iterator<AspectranClassLoader> owners, String name) {
		return getResources(owners, name, null);
	}
	
	public static Enumeration<URL> getResources(final Iterator<AspectranClassLoader> owners, String name, final Enumeration<URL> inherited) {
		if(name.endsWith(ResourceUtils.RESOURCE_NAME_SPEPARATOR))
			name = name.substring(0, name.length() - 1);
		
		System.out.println("-find resource from parent: " + name);
		System.out.println("--parent results: " + inherited);
		
		System.out.println("find resource from self: " + name);
		
		final String filterName = name;
		
		return new Enumeration<URL>() {
			private URL next;
			private URL current;
			private boolean nomore; //for parent
			
			private boolean hasNext() {
				do {
					if(!owners.hasNext())
						return false;
					
					next = owners.next().getResourceManager().getResource(filterName);
				} while(next == null);
				
				return (next != null);
			}
			
			public synchronized boolean hasMoreElements() {
				if(!nomore) {
					if(inherited != null && inherited.hasMoreElements())
						return true;
					else
						nomore = true;
				}

				if(next == null)
					return hasNext();
				else
					return true;
			}

			public synchronized URL nextElement() {
				if(!nomore) {
					if(inherited != null && inherited.hasMoreElements())
						return inherited.nextElement();
				}
				
				if(next == null) {
					if(!hasNext())
						throw new NoSuchElementException();
				}
				
				current = next;
				next = null;
				
				System.out.println("--self results: " + current);

				return current;
			}
		};
	}
	
	public static Enumeration<URL> searchResources(final Iterator<AspectranClassLoader> owners, String name) {
		return searchResources(owners, name, null);
	}

	public static Enumeration<URL> searchResources(final Iterator<AspectranClassLoader> owners, String name, final Enumeration<URL> inherited) {
		if(name.endsWith(ResourceUtils.RESOURCE_NAME_SPEPARATOR))
			name = name.substring(0, name.length() - 1);
		
		System.out.println("find resource from parent: " + name);
		System.out.println("parent results: " + inherited);
		
		while(inherited.hasMoreElements()) {
			System.out.println("p: " + inherited.nextElement().toString());
		}
		
		System.out.println("find resource from self: " + name);
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

					if(!hasNext())
						throw new NoSuchElementException();
				}

				URL url = entry.getValue();
				entry = null;

				return url;
			}
		};
	}
	
	public int getResourceEntriesSize() {
		return resourceEntries.size();
	}
//
//	public Class<?> loadClass(String name) throws ResourceNotFoundException {
//		synchronized(classCache) {
//			Class<?> c = classCache.get(name);
//			
//			if(c == null) {
//				URL url = resourceEntries.get(name);
//				
//				if(url == null) {
//					throw new ResourceNotFoundException(name);
//				}
//				
//				c = loadClass(url);
//				classCache.put(name, c);
//			}
//			
//			return c;
//		}
//	}
	
//	protected Class<?> loadClass(URL url) {
//		URLConnection connection = url.openConnection();
//		InputStream input = connection.getInputStream();
//		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//		int data = input.read();
//		
//		while(data != -1) {
//			buffer.write(data);
//			data = input.read();
//		}
//		
//		input.close();
//		
//		byte[] classData = buffer.toByteArray();
//		
//		return owner.defineClass("reflection.MyObject", classData, 0, classData.length);
//	}
	
	public void reset() {
		release();
	}
	
	public void release() {
		resourceEntries.clear();
	}
	
}
