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

	private String[] resourceLocations;
	
	private ResourceManager resourceManager;

	public AspectranClassLoader() {
		super(getDefaultClassLoader());
	}
	
	public AspectranClassLoader(String[] resourceLocations) {
		this(resourceLocations, getDefaultClassLoader());
	}

	public AspectranClassLoader(String[] resourceLocations, ClassLoader parent) {
		super(parent);
		
		setResourceLocations(resourceLocations);
	}

	public String[] getResourceLocations() {
		return resourceLocations;
	}

	public void setResourceLocations(String[] resourceLocations) {
		synchronized(this) {
			destroy();
			
			ResourceManager rm = null;
			
			for(int i = 0; i < resourceLocations.length; i++) {
				rm = new ResourceManager(resourceLocations[i], rm);
			}
			
			this.resourceManager = rm;
			this.resourceLocations = resourceLocations;
		}
	}
	
	public void reset() {
		setResourceLocations(resourceLocations);
	}
	
	public void destroy() {
		synchronized(this) {
			if(resourceManager != null) {
				resourceManager.release();
				resourceManager = null;
			}
		}
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
