package com.aspectran.core.context.loader.reload;

import java.net.URL;
import java.util.Enumeration;

import com.aspectran.core.context.ActivityContext;


public interface ActivityContextReloadable {

	public ActivityContext reload();

	public ActivityContext getActivityContext();
	
	public Enumeration<URL> getResources();
	
}
