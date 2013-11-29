package com.aspectran.web.startup;

import javax.servlet.ServletContext;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.refresh.ActivityContextRefreshHandler;
import com.aspectran.core.context.refresh.ActivityContextRefreshTimer;
import com.aspectran.core.context.refresh.ActivityContextRefreshable;
import com.aspectran.core.context.refresh.RefreshableClassLoader;

public class RefreshableActivityContextLoader extends ActivityContextLoader implements ActivityContextRefreshable {

	private static final ClassLoader classLoader = new RefreshableClassLoader();
	
	private ActivityContextRefreshHandler contextRefreshHandler;
	
	public RefreshableActivityContextLoader(ServletContext servletContext, String contextConfigLocation, ActivityContextRefreshHandler contextRefreshHandler) {
		super(servletContext, contextConfigLocation, classLoader);
		this.contextRefreshHandler = contextRefreshHandler;
	}

	public ActivityContext load() {
		ActivityContext newContext = build();
		
		ActivityContextRefreshTimer timer = new ActivityContextRefreshTimer(this);
		timer.start(5);
		
		return newContext;
	}
	
	public ActivityContext refresh() {
		ActivityContext newContext = load();
		contextRefreshHandler.handle(newContext);
		
		return newContext;
	}

	public static ActivityContext load(ServletContext servletContext, String contextConfigLocation, ActivityContextRefreshHandler contextRefreshHandler) {
		ActivityContextLoader aspectranContextLoader = new RefreshableActivityContextLoader(servletContext, contextConfigLocation, contextRefreshHandler);
		ActivityContext newContext = aspectranContextLoader.load();
		
		return newContext;
	}
	
}
