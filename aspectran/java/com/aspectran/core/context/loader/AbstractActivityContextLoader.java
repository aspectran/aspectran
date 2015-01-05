package com.aspectran.core.context.loader;

import com.aspectran.core.adapter.ApplicationAdapter;


public abstract class AbstractActivityContextLoader implements ActivityContextLoader {

	protected ApplicationAdapter applicationAdapter;
	
	protected AspectranClassLoader aspectranClassLoader;
	
	public AbstractActivityContextLoader() {
	}
	
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}

	public AspectranClassLoader getAspectranClassLoader() {
		return aspectranClassLoader;
	}

	public void setAspectranClassLoader(AspectranClassLoader aspectranClassLoader) {
		this.aspectranClassLoader = aspectranClassLoader;
	}

}
