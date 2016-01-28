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
package com.aspectran.core.context.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.aspectran.core.context.loader.resource.InvalidResourceException;
import com.aspectran.core.context.loader.resource.LocalResourceManager;
import com.aspectran.core.context.loader.resource.ResourceManager;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AspectranClassLoader.
 */
public class AspectranClassLoader extends ClassLoader {
	
	private final Log log = LogFactory.getLog(AspectranClassLoader.class);
	
	private final int id;
	
	private final AspectranClassLoader root;

	private final String resourceLocation;
	
	private final ResourceManager resourceManager;
	
	private final List<AspectranClassLoader> children = new LinkedList<AspectranClassLoader>();

	private final boolean firstborn;
	
	private int reloadCount;
	
	private Set<String> excludeClassNames;
	
	private Set<String> excludePackageNames;
	
	public AspectranClassLoader() throws InvalidResourceException {
		this(getDefaultClassLoader());
	}
	
	public AspectranClassLoader(ClassLoader parent) throws InvalidResourceException {
		this((String)null, parent);
	}
	
	public AspectranClassLoader(String resourceLocation) throws InvalidResourceException {
		this(resourceLocation, getDefaultClassLoader());
	}

	public AspectranClassLoader(String resourceLocation, ClassLoader parent) throws InvalidResourceException {
		super(parent);
		
		this.id = 1000;
		this.root = this;
		this.firstborn = true;
		this.resourceLocation = resourceLocation;
		this.resourceManager = new LocalResourceManager(resourceLocation, this);
		
		if(log.isDebugEnabled())
			log.debug("created a root AspectranClassLoader. " + this);
	}
	
	public AspectranClassLoader(String[] resourceLocations) throws InvalidResourceException {
		this(resourceLocations, getDefaultClassLoader());
	}
	
	public AspectranClassLoader(String[] resourceLocations, ClassLoader parent) throws InvalidResourceException {
		this(parent);
		
		AspectranClassLoader acl = this;
		
		for(String resourceLocation : resourceLocations) {
			acl = acl.createChild(resourceLocation);
		}
	}

	protected AspectranClassLoader(String resourceLocation, AspectranClassLoader parent) throws InvalidResourceException {
		super(parent);
		
		int brotherSize = parent.addChild(this);
		
		this.id = (Math.abs(parent.getId() / 1000) + 1) * 1000 + parent.getChildren().size();
		this.root = parent.getRoot();
		this.firstborn = (brotherSize == 1);
		this.resourceLocation = resourceLocation;
		this.resourceManager = new LocalResourceManager(resourceLocation, this);
	}
	
	public void setResourceLocation(String resourceLocation) throws InvalidResourceException {
		synchronized(children) {
			if(children.size() > 0) {
				children.clear();
			}
			
			createChild(resourceLocation);
		}
	}
	
	public void setResourceLocations(String[] resourceLocations) throws InvalidResourceException {
		synchronized(children) {
			if(children.size() > 0) {
				children.clear();
			}
			
			AspectranClassLoader acl = this;
			
			for(String resourceLocation : resourceLocations) {
				if(resourceLocation != null)
					acl = acl.createChild(resourceLocation);
			}
		}
	}
	
	private AspectranClassLoader createChild(String resourceLocation) throws InvalidResourceException {
		if(!firstborn)
			throw new UnsupportedOperationException("Only the firstborn AspectranClassLoader can create a child.");
		
		AspectranClassLoader child = new AspectranClassLoader(resourceLocation, this);
		
		log.debug("create a new child AspectranClassLoader " + child);
		
		return child;
	}
	
	public AspectranClassLoader wishBrother(String resourceLocation) throws InvalidResourceException {
		AspectranClassLoader parent = (AspectranClassLoader)getParent();

		return parent.createChild(resourceLocation);
	}
	
	/**
	 * Add a package name to exclude.
	 *
	 * @param packageName the package name to exclude
	 */
	public void excludePackage(String packageName) {
		if(excludePackageNames == null) {
			excludePackageNames = new HashSet<String>();
		}

		excludePackageNames.add(packageName + ClassUtils.PACKAGE_SEPARATOR);
	}
	
	/**
	 * Add a class name to exclude.
	 *
	 * @param className the class name to exclude
	 */
	public void excludeClass(String className) {
		if(isExcludePackage(className))
			return;
		
		if(excludeClassNames == null) {
			excludeClassNames = new HashSet<String>();
		}
		
		excludeClassNames.add(className);
	}
	
	public void excludePackage(String[] packageNames) {
		if(packageNames == null) {
			excludePackageNames = null;
			return;
		}
		
		for(String packageName : packageNames) {
			excludePackage(packageName);
		}
	}
	
	public void excludeClass(String[] classNames) {
		if(classNames == null) {
			excludeClassNames = null;
			return;
		}
		
		for(String className : classNames) {
			excludeClass(className);
		}
	}
	
	private boolean isExcluded(String className) {
		if(isExcludePackage(className))
			return true;
		
		if(isExcludeClass(className))
			return true;
		
		return false;
	}
	
	private boolean isExcludePackage(String className) {
		if(excludePackageNames != null) {
			for(String packageName : excludePackageNames) {
				if(className.startsWith(packageName))
					return true;
			}
		}
		
		return false;
	}
	
	private boolean isExcludeClass(String className) {
		return (excludeClassNames != null && excludeClassNames.contains(className));
	}
	
	public int getId() {
		return id;
	}

	public AspectranClassLoader getRoot() {
		return root;
	}
	
	public boolean isRoot() {
		return this == root;
	}
	
	public List<AspectranClassLoader> getChildren() {
		return children;
	}
	
	private int addChild(AspectranClassLoader child) {
		synchronized(children) {
			children.add(child);
			return children.size();
		}
	}
	
	public boolean isFirstborn() {
		return firstborn;
	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	public String getResourceLocation() {
		return resourceLocation;
	}

	public synchronized void reload() throws InvalidResourceException {
		reload(root);
	}
	
	private void reload(AspectranClassLoader self) throws InvalidResourceException {
		self.increaseReloadCount();
		
		log.debug("reload AspectranClassLoader " + self);

		if(self.getResourceManager() != null)
			self.getResourceManager().reset();
		
		AspectranClassLoader firstborn = null;
		List<AspectranClassLoader> kickoutList = new ArrayList<AspectranClassLoader>();
		
		for(AspectranClassLoader child : self.getChildren()) {
			if(child.isFirstborn()) {
				firstborn = child;
			} else {
				kickoutList.add(child);
			}
		}
		
		if(kickoutList.size() > 0)
			self.kickout(kickoutList);
		
		if(firstborn != null) {
			reload(firstborn);
		}
	}
	
	private void increaseReloadCount() {
		reloadCount++;
	}
	
	private void kickout(AspectranClassLoader child) {
		log.debug("kickout a child AspectranClassLoader: " + child);

		ResourceManager rm = child.getResourceManager();
		if(rm != null) {
			rm.release();
		}
		children.remove(child);
	}
	
	private void kickout(List<AspectranClassLoader> childList) {
		for(AspectranClassLoader child : childList) {
			kickout(child);
		}
	}
	
	public URL[] extractResources() {
		Enumeration<URL> res = ResourceManager.getResources(getAspectranClassLoaders(root));
		List<URL> resources = new LinkedList<URL>();
		
		URL url = null;
		
		while(res.hasMoreElements()) {
			url = res.nextElement();
			
			if(!ResourceUtils.URL_PROTOCOL_JAR.equals(url.getProtocol()))
				resources.add(url);
		}
		
		return resources.toArray(new URL[resources.size()]);
	}
	
	public Enumeration<URL> getResources(String name) throws IOException {
		ClassLoader parentClassLoader = root.getParent();
		Enumeration<URL> parentResources = null;
		
		if(parentClassLoader != null)
			parentResources = parentClassLoader.getResources(name);
		
		return ResourceManager.getResources(getAspectranClassLoaders(root), name, parentResources);
	}
	
	public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
	    // First check if the class is already loaded
		Class<?> c = findLoadedClass(name);

		if(c == null) {
	    	byte[] classData = null;

	    	try  {
		    	classData = loadClassData(name, root);
	    	} catch(InvalidResourceException e) {
	    		log.error("failed to load class '" + name + "'", e);
	    	}

	    	if(classData != null) {
	    		c = defineClass(name, classData, 0, classData.length);
	    		resolveClass(c);
	    	}
	    }
	    
	    if(c == null && root.getParent() != null) {
	    	try {
                c = root.getParent().loadClass(name);
	        } catch(ClassNotFoundException e) {
	            // If still not found, then invoke
	            // findClass to find the class.
	            c = findClass(name);
	        }
	    }

	    return c;		
    }
	
	public URL getResource(String name) {
		URL url = super.getResource(name);

		if(url == null) {
			Enumeration<URL> res = ResourceManager.getResources(getAspectranClassLoaders(root), name);
			
			if(res.hasMoreElements())
				url = res.nextElement();
		}

		if(url == null)
			return findResource(name);
		
		return url;
	}
	
	private byte[] loadClassData(String className, AspectranClassLoader owner) throws InvalidResourceException {
		if(isExcluded(className))
			return null;
		
		String resourceName = classNameToResourceName(className);
		
		URL url = null;
		Enumeration<URL> res = ResourceManager.getResources(getAspectranClassLoaders(owner), resourceName);
		
		if(res.hasMoreElements())
			url = res.nextElement();

		if(url == null)
			return null;
		
		try {
	        URLConnection connection = url.openConnection();
	        InputStream input = connection.getInputStream();
	        ByteArrayOutputStream output = new ByteArrayOutputStream();
	
			byte[] buffer = new byte[8192];
			int len = 0;

			while((len = input.read(buffer)) >= 0) {
				output.write(buffer, 0, len);
			}   
	        
	        input.close();

	        return output.toByteArray();
		} catch(IOException e) {
			throw new InvalidResourceException("cannot read a class file: " + url, e);
		}
	}

	public Iterator<AspectranClassLoader> getAllAspectranClassLoaders() {
		return getAspectranClassLoaders(root);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{id=").append(id);
		if(getParent() instanceof AspectranClassLoader)
			sb.append(", parent=").append(((AspectranClassLoader)getParent()).getId());
		else
			sb.append(", parent=").append(getParent().getClass().getName());
		sb.append(", root=").append(this == root);
		sb.append(", firstborn=").append(firstborn);
		sb.append(", resourceLocation=").append(resourceLocation);
		sb.append(", numberOfResource=").append(resourceManager.getResourceEntriesSize());
		sb.append(", numberOfChildren=").append(children.size());
		sb.append(", reloadCount=").append(reloadCount);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static Iterator<AspectranClassLoader> getAspectranClassLoaders(final AspectranClassLoader root) {
		return new Iterator<AspectranClassLoader>() {
			private AspectranClassLoader next = root;
			private Iterator<AspectranClassLoader> children = root.getChildren().iterator();
			private AspectranClassLoader firstChild;
			private AspectranClassLoader current;
			
			public boolean hasNext() {
				return (next != null);
			}
			
			public AspectranClassLoader next() {
				if(next == null)
					throw new NoSuchElementException();
				
				current = next;
				
				if(children.hasNext()) {
					next = children.next();

					if(firstChild == null) {
						firstChild = next;
					}
				} else {
					if(firstChild != null) {
						children = firstChild.getChildren().iterator();
						
						if(children.hasNext()) {
							next = children.next();
							firstChild = next;
						} else {
							next = null;
						}
					} else {
						next = null;
					}
				}

				return current;
			}
			
			public void remove() {
				throw new UnsupportedOperationException("remove");
			}
		};
	}
	
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch(Throwable ex) {
		}

		if(cl == null) {
			cl = AspectranClassLoader.class.getClassLoader();
		}
		
		if(cl == null) {
			cl = ClassLoader.getSystemClassLoader();
		}
		
		return cl;
	}
	
	public static String resourceNameToClassName(String resourceName) {
		String className = resourceName.substring(0, resourceName.length() - ClassUtils.CLASS_FILE_SUFFIX.length());
		className = className.replace(ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR, ClassUtils.PACKAGE_SEPARATOR_CHAR);
		return className;
	}
	
	public static String classNameToResourceName(String className) {
		String resourceName = className.replace(ClassUtils.PACKAGE_SEPARATOR_CHAR, ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR) + 
				ClassUtils.CLASS_FILE_SUFFIX;
		return resourceName;
	}
	
	public static String packageNameToResourceName(String packageName) {
		String resourceName = packageName.replace(ClassUtils.PACKAGE_SEPARATOR_CHAR, ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR);
		
		if(resourceName.endsWith(ResourceUtils.RESOURCE_NAME_SPEPARATOR))
			resourceName = resourceName.substring(0, resourceName.length() - 1);
		
		return resourceName;
	}
	
}
