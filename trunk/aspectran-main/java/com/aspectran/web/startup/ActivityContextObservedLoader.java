package com.aspectran.web.startup;

import javax.servlet.ServletContext;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.refresh.ActivityContextRefreshHandler;
import com.aspectran.core.context.refresh.ActivityContextRefreshTimer;
import com.aspectran.core.context.refresh.ActivityContextRefreshable;
import com.aspectran.core.context.refresh.DynamicClassLoader;

public class ActivityContextObservedLoader extends ActivityContextLoader implements ActivityContextRefreshable {

	private static final ClassLoader classLoader = new DynamicClassLoader();
	
	private ActivityContextRefreshHandler activityContextRefreshHandler;
	
	private String[] observingPaths;
	
	public ActivityContextObservedLoader(ServletContext servletContext, String contextConfigLocation) {
		super(servletContext, contextConfigLocation, classLoader);
	}

	public ActivityContextRefreshTimer startTimer(ActivityContextRefreshHandler activityContextRefreshHandler, String[] observingPaths, int refreshTime) {
		this.activityContextRefreshHandler = activityContextRefreshHandler;
		this.observingPaths = observingPaths;
		
		ActivityContextRefreshTimer timer = new ActivityContextRefreshTimer(this);
		timer.start(refreshTime);
		
		return timer;
	}
	
	public ActivityContext refresh() {
		ActivityContext newActivityContext = load();
		activityContextRefreshHandler.handle(newActivityContext);
		
		return newActivityContext;
	}
	
	public String[] getObservingPaths() {
		return observingPaths;
	}
	
}
