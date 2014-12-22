package com.aspectran.core.context.loader;

import java.net.URL;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.XmlActivityContextBuilder;
import com.aspectran.core.context.loader.reload.ActivityContextReloadable;
import com.aspectran.core.context.loader.reload.ActivityContextReloadingHandler;
import com.aspectran.core.context.loader.reload.ActivityContextReloadingTimer;

public class ActivityContextLoader implements ActivityContextReloadable {

	private final Logger logger = LoggerFactory.getLogger(ActivityContextLoader.class);

	private final ApplicationAdapter applicationAdapter;
	
	private AspectranClassLoader aspectranClassLoader;
	
	private ActivityContextReloadingHandler activityContextReloadingHandler;
	
	private String rootContext;
	
	private ActivityContext activityContext;

	public ActivityContextLoader(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}

	public void setResourceLocations(String[] resourceLocations) {
		AspectranClassLoader acl = AspectranClassLoader.newInstance(resourceLocations);
		this.aspectranClassLoader = acl;
	}
	
	public AspectranClassLoader getAspectranClassLoader() {
		return aspectranClassLoader;
	}

	public void setAspectranClassLoader(AspectranClassLoader aspectranClassLoader) {
		this.aspectranClassLoader = aspectranClassLoader;
	}

	protected ActivityContext buildActivityContext() {
		try {
			logger.info("build ActivityContext [" + rootContext + "]");
			long startTime = System.currentTimeMillis();

			ActivityContextBuilder builder = new XmlActivityContextBuilder(applicationAdapter, aspectranClassLoader);
			activityContext = builder.build(rootContext);
			
			long elapsedTime = System.currentTimeMillis() - startTime;
			logger.info("ActivityContext initialization completed in " + elapsedTime + " ms");
			
			return activityContext;
		} catch(RuntimeException ex) {
			logger.error("ActivityContext failed to initialize: " + ex);
			throw ex;
		} catch(Error err) {
			logger.error("ActivityContext failed to initialize: " + err);
			throw err;
		}
	}
	
	public ActivityContext load(String rootContext) {
		this.rootContext = rootContext;
		
		return buildActivityContext();
	}
	
	public ActivityContext reload() {
		if(aspectranClassLoader != null)
			aspectranClassLoader.reload();
		
		ActivityContext newActivityContext = buildActivityContext();
		
		if(activityContextReloadingHandler != null)
			activityContextReloadingHandler.handle(newActivityContext);
		
		return newActivityContext;
	}
	
	public ActivityContext getActivityContext() {
		return activityContext;
	}

	public String getApplicationBasePath() {
		return applicationAdapter.getApplicationBasePath();
	}
	
	public ActivityContextReloadingTimer startTimer(ActivityContextReloadingHandler activityContextReloadingHandler, int observationInterval) {
		this.activityContextReloadingHandler = activityContextReloadingHandler;
		
		ActivityContextReloadingTimer timer = new ActivityContextReloadingTimer(this);
		timer.start(observationInterval);
		
		return timer;
	}
	
	public Enumeration<URL> getResources() {
		if(aspectranClassLoader == null)
			return null;
		
		return aspectranClassLoader.getResources();
	}
	
}
