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
	
	public RefreshableActivityContextLoader(ServletContext servletContext, String contextConfigLocation) {
		super(servletContext, contextConfigLocation, classLoader);
	}

	public ActivityContextRefreshTimer startTimer(ActivityContextRefreshHandler contextRefreshHandler, int refreshTime) {
		this.contextRefreshHandler = contextRefreshHandler;
		
		ActivityContextRefreshTimer timer = new ActivityContextRefreshTimer(this);
		timer.start(refreshTime);
		
		return timer;
	}
	
	public ActivityContext refresh() {
		ActivityContext newContext = load();
		contextRefreshHandler.handle(newContext);
		
		return newContext;
	}
	
	public String[] getMonitoringClassFiles() {
		String[] files = new String[0];
		
		return files;
	}
	
}
