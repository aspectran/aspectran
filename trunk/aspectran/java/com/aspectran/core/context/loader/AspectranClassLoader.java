package com.aspectran.core.context.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.resource.ResourceManager;

public class AspectranClassLoader extends ClassLoader {
	
	private final AspectranClassLoader root;

	private final List<AspectranClassLoader> children = new LinkedList<AspectranClassLoader>();
	
	private final boolean firstborn;
	
	private final ResourceManager resourceManager;
	
	public AspectranClassLoader() {
		this(getDefaultClassLoader());
	}
	
	public AspectranClassLoader(ClassLoader parent) {
		super(parent);
		this.root = this;
		this.resourceManager = null;
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
		this.resourceManager = new ResourceManager(resourceLocation, this);
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
	
	protected AspectranClassLoader getRoot() {
		return root;
	}
	
	protected List<AspectranClassLoader> getChildren() {
		return children;
	}
	
	public boolean isFirstborn() {
		return firstborn;
	}

	protected String getResourceLocation() {
		if(resourceManager == null)
			return null;
		
		return resourceManager.getResourceLocation();
	}

	protected ResourceManager getResourceManager() {
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
	
	public Enumeration<URL> getResources() {
		if(resourceManager == null)
			return null;
		
		return resourceManager.getResources();
	}
	
	public Enumeration<URL> getAllResources() {
		if(resourceManager == null)
			return null;
		
		return resourceManager.getResources();
	}
	
	public List<File> extractResourceFileList() {
		return null;
	}

	public Enumeration<URL> getResources(String name) {
		if(resourceManager == null)
			return null;
		
		return resourceManager.getResources(name);
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

}
