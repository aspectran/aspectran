package com.aspectran.core.context.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.resource.InvalidResourceException;
import com.aspectran.core.context.loader.resource.LocalResourceManager;
import com.aspectran.core.context.loader.resource.ResourceManager;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.ResourceUtils;

public class AspectranClassLoader extends ClassLoader {
	
	private final Logger logger = LoggerFactory.getLogger(AspectranClassLoader.class);
	
	private final AspectranClassLoader root;

	private final String resourceLocation;
	
	private final ResourceManager resourceManager;
	
	private final List<AspectranClassLoader> children = new LinkedList<AspectranClassLoader>();

	private final boolean firstborn;
	
	public AspectranClassLoader() {
		this(getDefaultClassLoader());
	}
	
	public AspectranClassLoader(ClassLoader parent) {
		super(parent);
		this.root = this;
		this.resourceLocation = null;
		this.resourceManager = new LocalResourceManager(null, this);
		this.firstborn = false;
	}
	
	public AspectranClassLoader(String resourceLocation) {
		this(resourceLocation, getDefaultClassLoader());
	}

	public AspectranClassLoader(String resourceLocation, ClassLoader parent) {
		super(parent);
		this.root = this;
		this.resourceLocation = resourceLocation;
		this.resourceManager = new LocalResourceManager(resourceLocation, this);
		this.firstborn = false;
	}
	
	public AspectranClassLoader(String[] resourceLocations) {
		this(resourceLocations, getDefaultClassLoader());
	}		
	
	public AspectranClassLoader(String[] resourceLocations, ClassLoader parent) {
		this(parent);
		
		AspectranClassLoader acl = this;
		
		for(String resourceLocation : resourceLocations) {
			acl = acl.createChild(resourceLocation);
		}
	}

	protected AspectranClassLoader(String resourceLocation, AspectranClassLoader parent, AspectranClassLoader root, boolean firstborn) {
		super(parent);
		
		parent.getChildren().add(this);
		
		this.root = root == null ? this : root;
		this.resourceLocation = resourceLocation;
		this.resourceManager = new LocalResourceManager(resourceLocation, this);
		this.firstborn = firstborn;
	}
	
	protected AspectranClassLoader createChild(String resourceLocation) {
		boolean firstborn = (children.size() == 0);

		logger.debug("create a child AspectranClassLoader. {resourceLocation: " + resourceLocation + ", firstborn: " + firstborn + "}");
		
		AspectranClassLoader child = new AspectranClassLoader(resourceLocation, this, root, firstborn);
		return child;
	}
	
	public AspectranClassLoader wishBrother(String resourceLocation) {
		AspectranClassLoader parent = (AspectranClassLoader)getParent();

		return parent.createChild(resourceLocation);
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
	
	public boolean isFirstborn() {
		return firstborn;
	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	public String getResourceLocation() {
		return resourceLocation;
	}

	public void reload() {
		reload(root);
	}
	
	protected void reload(AspectranClassLoader self) {
		if(self.getResourceManager() != null)
			self.getResourceManager().reset();
		
		AspectranClassLoader firstbon = null;
		
		for(AspectranClassLoader child : self.getChildren()) {
			if(child.isFirstborn()) {
				firstbon = child;
			} else {
				self.kickout(child);
				ResourceManager rm = child.getResourceManager();
				if(rm != null) {
					rm.release();
				}
			}
		}
		
		if(firstbon != null) {
			ResourceManager rm = firstbon.getResourceManager();
			if(rm != null) {
				rm.reset();
			}
			reload(firstbon);
		}
	}
	
	protected void leave() {
		AspectranClassLoader parent = (AspectranClassLoader)getParent();
		parent.kickout(this);
	}
	
	protected void kickout(AspectranClassLoader child) {
		children.remove(child);
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
	        try {
	        	byte[] classData = loadClassData(name);
	        	System.out.println("classData: " + classData);
	            if(getParent() != null && classData == null) {
	                c = getParent().loadClass(name);
	                System.out.println("getParent().loadClass: " + c);
	            } else {
	            	c = defineClass(name, classData, 0, classData.length);
	            	System.out.println("defineClass: " + c);
	            }
	        } catch(ClassNotFoundException e) {
	            // If still not found, then invoke
	            // findClass to find the class.
	            c = findClass(name);
	        }
	    }

	    return c;		
    }
	
	public Class<?> findClass(String name) throws ClassNotFoundException {
		throw new ClassNotFoundException(name);
	}
	
	public URL getResource(String name) {
		URL url = super.getResource(name);
		
		if(url == null) {
			Enumeration<URL> res = ResourceManager.getResources(getAspectranClassLoaders(root), name);
			
			if(res.hasMoreElements())
				return res.nextElement();
		}
		
		return findResource(name);
	}

	protected URL findResource(String name) {
		return null;
	}
	
	protected byte[] loadClassData(String className) {
		String resourceName = classNameToResourceName(className);
		
		URL url = null;
		Enumeration<URL> res = ResourceManager.getResources(getAspectranClassLoaders(root), resourceName);
		
		if(res.hasMoreElements())
			url = res.nextElement();
System.out.println();
System.out.println("resourceName: " + resourceName);
System.out.println("url: " + url);
		if(url == null)
			return null;
		
		try {
	        URLConnection connection = url.openConnection();
	        InputStream input = connection.getInputStream();
	        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	        int data = input.read();
	
	        while(data != -1) {
	            buffer.write(data);
	            data = input.read();
	        }
	
	        input.close();

	        return buffer.toByteArray();
		} catch(IOException e) {
			throw new InvalidResourceException("cannot read a class file: " + resourceName, e);
		}
	}

	public Iterator<AspectranClassLoader> getAllAspectranClassLoaders() {
		return getAspectranClassLoaders(root);
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
		};
	}

	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch(Throwable ex) {
		}

		if(cl == null) {
			cl = ActivityContext.class.getClassLoader();
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
	
	/**
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		try {
			String[] resourceLocations = new String[2];
			//resourceLocations[0] = "/WEB-INF/classes";
			//resourceLocations[1] = "/WEB-INF/lib";
			resourceLocations[0] = "/WEB-INF/aspectran/classes";
			resourceLocations[1] = "/WEB-INF/aspectran/lib";
			//resourceLocations[3] = "/WEB-INF/aspectran/xml";
			
			resourceLocations = ActivityContextLoadingManager.checkResourceLocations("c:/Users/Gulendol/Projects/aspectran/ADE/workspace/aspectran.example/webapp", resourceLocations);
			
			for(String r : resourceLocations) {
				System.out.println("resourceLocation: " + r);
			}
			
			AspectranClassLoader acl = new AspectranClassLoader(resourceLocations);
			
			//acl.extractResources();
			
			//ResourceManager rm = acl.getResourceManager();
			//ClassLoader acl =AspectranClassLoader.getDefaultClassLoader();
			
			//Enumeration<URL> res = acl.getResources("org/junit/After.class");
			//Enumeration<URL> res = acl.getResources("com/aspectran/core/context/bean/BeanRegistry.class");
			//Enumeration<URL> res = acl.getResources("web.xml");
			
//			while(res.hasMoreElements()) {
//				System.out.println(res.nextElement().toString());
//			}

			URL[] res = acl.extractResources();
			for(URL url : res) {
				System.out.println(url);
			}
			
			acl.loadClass("com.aspectran.web.activity.multipart.MultipartFileItem");
			acl.reload();

			res = acl.extractResources();
			for(URL url : res) {
				System.out.println(url);
			}

			acl.loadClass("com.aspectran.web.activity.multipart.MultipartFileItem");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
