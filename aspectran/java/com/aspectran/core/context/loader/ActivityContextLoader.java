package com.aspectran.core.context.loader;

import com.aspectran.core.context.ActivityContext;

public interface ActivityContextLoader {

	public AspectranClassLoader getAspectranClassLoader();

	public void setAspectranClassLoader(AspectranClassLoader aspectranClassLoader);

	public ActivityContext load(String rootContext);
	
}
