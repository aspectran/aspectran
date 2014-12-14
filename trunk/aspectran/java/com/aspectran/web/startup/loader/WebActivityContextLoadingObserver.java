package com.aspectran.web.startup.loader;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.AspectranClassLoader;
import com.aspectran.core.context.loader.reload.ActivityContextReloadable;
import com.aspectran.core.context.loader.reload.ActivityContextReloadingHandler;
import com.aspectran.core.context.loader.reload.ActivityContextReloadingTimer;

public class WebActivityContextLoadingObserver extends WebActivityContextLoader implements ActivityContextReloadable {

	private String rootContext;
	
	private String[] resourceLocations;
	
	private ActivityContextReloadingHandler activityContextReloadingHandler;

	public WebActivityContextLoadingObserver(ApplicationAdapter applicationAdapter, AspectranClassLoader aspectranClassLoader) {
		super(applicationAdapter, aspectranClassLoader);
	}

	public ActivityContext load(String rootContext, String[] resourceLocations) {
		this.rootContext = rootContext;
		this.resourceLocations = resourceLocations;
		
		return super.load(rootContext, resourceLocations);
	}
	
	public ActivityContext reload() {
		ActivityContext newActivityContext = load(rootContext, resourceLocations);
		activityContextReloadingHandler.handle(newActivityContext);
		
		return newActivityContext;
	}
	
	public ActivityContextReloadingTimer startTimer(ActivityContextReloadingHandler activityContextReloadingHandler, int observationInterval) {
		this.activityContextReloadingHandler = activityContextReloadingHandler;
		
		ActivityContextReloadingTimer timer = new ActivityContextReloadingTimer(this);
		timer.start(observationInterval);
		
		return timer;
	}
	
	public String[] getResources() {
		return getAspectranClassLoader().getResources();
	}

}
