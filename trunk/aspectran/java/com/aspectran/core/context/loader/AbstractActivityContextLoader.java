package com.aspectran.core.context.loader;


public abstract class AbstractActivityContextLoader implements ActivityContextLoader {

	protected String applicationBasePath;
	
	protected AspectranClassLoader aspectranClassLoader;
	
	public AbstractActivityContextLoader() {
	}
	
	public AbstractActivityContextLoader(String applicationBasePath) {
		this.applicationBasePath = applicationBasePath;
	}
	
	public AspectranClassLoader getAspectranClassLoader() {
		return aspectranClassLoader;
	}

	public void setAspectranClassLoader(AspectranClassLoader aspectranClassLoader) {
		this.aspectranClassLoader = aspectranClassLoader;
	}

}
