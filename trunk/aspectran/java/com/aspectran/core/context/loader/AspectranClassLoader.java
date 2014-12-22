package com.aspectran.core.context.loader;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.resource.ResourceManager;

public class AspectranClassLoader extends ClassLoader {
	
	private final AspectranClassLoader top;

	private AspectranClassLoader child;
	
	private ResourceManager resourceManager;
	
	public AspectranClassLoader() {
		this(getDefaultClassLoader());
	}
	
	public AspectranClassLoader(ClassLoader parent) {
		super(parent);
		this.top = this;
		this.resourceManager = null;
	}
	
	public AspectranClassLoader(String resourceLocation) {
		this(resourceLocation, getDefaultClassLoader());
	}

	public AspectranClassLoader(String resourceLocation, ClassLoader parent) {
		this(resourceLocation, parent, null);
	}
	
	protected AspectranClassLoader(String resourceLocation, ClassLoader parent, AspectranClassLoader top) {
		super(parent);
		
		this.top = top == null ? this : top;

		this.resourceManager = new ResourceManager(resourceLocation);
	}

	protected AspectranClassLoader createChild(String resourceLocation) {
		child = new AspectranClassLoader(resourceLocation, getParent(), top);
		return child;
	}
	
	protected AspectranClassLoader getTop() {
		return top;
	}
	
	protected AspectranClassLoader getChild() {
		return child;
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
		reload(top);
	}
	
	protected void reload(AspectranClassLoader acl) {
		if(acl.getResourceManager() != null)
			acl.getResourceManager().reset();
		
		if(acl.getChild() != null)
			reload(acl.getChild());
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
	
	public static AspectranClassLoader newInstance(String[] resourceLocations) {
		return newInstance(resourceLocations, getDefaultClassLoader());
	}
	
	public static AspectranClassLoader newInstance(String[] resourceLocations, ClassLoader parent) {
		AspectranClassLoader top = null;
		AspectranClassLoader acl = null;
		
		for(int i = 0; i < resourceLocations.length; i++) {
			if(acl == null)
				acl = new AspectranClassLoader(resourceLocations[i], parent);
			else
				acl = acl.createChild(resourceLocations[i]);
			
			if(i == 0)
				top = acl;
		}
		
		if(top == null)
			top = new AspectranClassLoader(parent);
		
		return top;
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
