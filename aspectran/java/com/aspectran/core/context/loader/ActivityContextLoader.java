package com.aspectran.core.context.loader;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;

public interface ActivityContextLoader {

	public ApplicationAdapter getApplicationAdapter();
	
	public void setApplicationAdapter(ApplicationAdapter applicationAdapter);

	public AspectranClassLoader getAspectranClassLoader();

	public void setAspectranClassLoader(AspectranClassLoader aspectranClassLoader);

	public ActivityContext load(String rootContext);
	
}
