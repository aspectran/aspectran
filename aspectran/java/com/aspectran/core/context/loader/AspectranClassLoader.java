package com.aspectran.core.context.loader;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.resource.LocalResourceManager;
import com.aspectran.core.context.loader.resource.ResourceManager;

public class AspectranClassLoader extends ClassLoader {
	
	private final AspectranClassLoader root;

	private final ResourceManager resourceManager;
	
	private final List<AspectranClassLoader> children = new LinkedList<AspectranClassLoader>();

	private final boolean firstborn;
	
	public AspectranClassLoader() {
		this(getDefaultClassLoader());
	}
	
	public AspectranClassLoader(ClassLoader parent) {
		super(parent);
		this.root = this;
		this.resourceManager = new LocalResourceManager(null, this);;
		this.firstborn = false;
	}
	
	public AspectranClassLoader(String resourceLocation) {
		this(resourceLocation, getDefaultClassLoader());
	}

	public AspectranClassLoader(String resourceLocation, ClassLoader parent) {
		this(resourceLocation, parent, null, false);
	}
	
	public AspectranClassLoader(String[] resourceLocations) {
		this(resourceLocations, getDefaultClassLoader());
	}		
	
	public AspectranClassLoader(String[] resourceLocations, ClassLoader parent) {
		this(parent);
		
		AspectranClassLoader acl = this;
		
		for(int i = 0; i < resourceLocations.length; i++) {
			acl = acl.createChild(resourceLocations[i]);
		}
	}

	protected AspectranClassLoader(String resourceLocation, ClassLoader parent, AspectranClassLoader root, boolean firstborn) {
		super(parent);
		
		this.root = root == null ? this : root;
		this.resourceManager = new LocalResourceManager(resourceLocation, this);
		this.firstborn = firstborn;
	}
	
	protected AspectranClassLoader createChild(String resourceLocation) {
		boolean firstborn = (children.size() == 0);
		
		AspectranClassLoader child = new AspectranClassLoader(resourceLocation, this, root, firstborn);
		children.add(child);
		
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
		return root.resourceManager.extractResources();
	}
	
	public Enumeration<URL> getResources(String name) throws IOException {
		ClassLoader parentClassLoader = root.getParent();
		Enumeration<URL> parentResources = null;
		
		if(parentClassLoader != null)
			parentResources = parentClassLoader.getResources(name);
		
		return root.getResourceManager().getResources(name, parentResources);
	}
	
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		//URL url = null;
		
		//if(resourceManager != null)
		//	url = resourceManager.get
		
        if(!"reflection.MyObject".equals(name))
                return super.loadClass(name);

        try {
            String url = "file:C:/data/projects/tutorials/web/WEB-INF/" +
                            "classes/reflection/MyObject.class";
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

            while(data != -1){
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            return defineClass("reflection.MyObject",
                    classData, 0, classData.length);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
	
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			//String path = root + File.separatorChar + name.replace('.', File.separatorChar) + ".class";
			String path = "";

			FileInputStream file = new FileInputStream(path);
			byte[] classByte = new byte[file.available()];
			file.read(classByte);

			return defineClass(name, classByte, 0, classByte.length);
		} catch (IOException ex) {
			throw new ClassNotFoundException();
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
				
				if(!children.hasNext()) {
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
				} else {
					next = children.next();
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

	/**
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		try {
			String[] resourceLocations = new String[2];
			resourceLocations[0] = "/WEB-INF/classes";
			resourceLocations[1] = "/WEB-INF/lib";
			
			resourceLocations = ActivityContextLoadingManager.checkResourceLocations("/c:/Users/Gulendol/Projects/aspectran/ADE/workspace/aspectran.example/webapp", resourceLocations);
			
			for(String r : resourceLocations) {
				System.out.println("resourceLocation: " + r);
			}
			
			AspectranClassLoader acl = new AspectranClassLoader(resourceLocations);
			//ResourceManager rm = acl.getResourceManager();
			//ClassLoader acl =AspectranClassLoader.getDefaultClassLoader();
			
			//Enumeration<URL> res = acl.getResources("org/junit/After.class");
			//Enumeration<URL> res = acl.getResources("com/aspectran/core/context/bean/BeanRegistry.class");
			Enumeration<URL> res = acl.getResources("com/");
			
			while(res.hasMoreElements()) {
				System.out.println(res.nextElement().toString());
			}
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
