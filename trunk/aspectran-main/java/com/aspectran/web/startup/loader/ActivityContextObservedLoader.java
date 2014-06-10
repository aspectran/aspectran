package com.aspectran.web.startup.loader;

import javax.servlet.ServletContext;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.reload.ActivityContextReloadable;
import com.aspectran.core.context.reload.ActivityContextReloadingHandler;
import com.aspectran.core.context.reload.ActivityContextReloadingTimer;

public class ActivityContextObservedLoader extends ActivityContextLoader implements ActivityContextReloadable {

	private ActivityContextReloadingHandler activityContextReloadingHandler;
	
	private String[] observingPaths;
	
	public ActivityContextObservedLoader(ServletContext servletContext, String contextConfigLocation) {
		super(servletContext, contextConfigLocation);
	}
	
	public ActivityContextObservedLoader(ServletContext servletContext, String contextConfigLocation, ClassLoader classLoader) {
		super(servletContext, contextConfigLocation, classLoader);
	}

	public ActivityContextReloadingTimer startTimer(ActivityContextReloadingHandler activityContextReloadingHandler, String[] observingPaths, int observationInterval) {
		this.activityContextReloadingHandler = activityContextReloadingHandler;
		this.observingPaths = observingPaths;
		
		ActivityContextReloadingTimer timer = new ActivityContextReloadingTimer(this);
		timer.start(observationInterval);
		
		return timer;
	}
	
	public ActivityContext reload() {
		ActivityContext newActivityContext = load();
		activityContextReloadingHandler.handle(newActivityContext);
		
		return newActivityContext;
	}
	
	public String[] getObservingPaths() {
		return observingPaths;
	}
	
}
