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

public class AspectranSubClassLoader extends ClassLoader {

	private String[] resourceLocations;
	
	private ResourceManager resourceManager;

	public AspectranSubClassLoader() {
		super(getParentClassLoader());
	}
	
	public AspectranSubClassLoader(ClassLoader parent) {
		super(parent);
	}
	
	public AspectranSubClassLoader(String[] resourceLocations) {
		this(resourceLocations, getParentClassLoader());
	}

	public AspectranSubClassLoader(String[] resourceLocations, ClassLoader parent) {
		super(parent);
		
		setResourceLocations(resourceLocations);
	}
	
	public String[] getResourceLocations() {
		return resourceLocations;
	}

	protected void setResourceLocation(String resourceLocation) {
		if(resourceManager != null) {
			resourceManager.release();
			resourceManager = null;
		}

		resourceManager = new ResourceManager(resourceLocation);
	}
	
	public synchronized void setResourceLocations(String[] resourceLocations) {
		ClassLoader parent = this;
		
		for(int i = resourceLocations.length - 1; i >= 0 ; i--) {
			if(i == 0) {
				setResourceLocation(resourceLocations[i]);
			} else {
				AspectranSubClassLoader acl = new AspectranSubClassLoader(parent);
				acl.setResourceLocation(resourceLocations[i]);
				parent = acl;
			}
		}
		
		this.resourceLocations = resourceLocations;
	}
	
	public void reload() {
		setResourceLocations(resourceLocations);
	}
	
	public Enumeration<URL> getResources() {
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
	
	private static ClassLoader getParentClassLoader() {
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
