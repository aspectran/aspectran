package com.aspectran.web.startup;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.XmlActivityContextBuilder;
import com.aspectran.web.adapter.WebApplicationAdapter;

public class ActivityContextLoader {

	private final Logger logger = LoggerFactory.getLogger(ActivityContextLoader.class);

	public static final String CONTEXT_CONFIG_LOCATION_PARAM = "contextConfigLocation";
	
	private static final String DEFAULT_CONTEXT_CONFIG_LOCATION = "WEB-INF/aspectran/aspectran.xml";
	
	private ServletContext servletContext;
	
	private ActivityContext activityContext;
	
	public ActivityContextLoader(ServletContext servletContext, String contextConfigLocation) {
		this.servletContext = servletContext;
		
		if(contextConfigLocation == null)
			contextConfigLocation = DEFAULT_CONTEXT_CONFIG_LOCATION;

		loadActivityContext(contextConfigLocation);
	}

	private void loadActivityContext(String contextConfigLocation) {
		logger.info("loading ActivityContext [" + contextConfigLocation + "]");
		
		long startTime = System.currentTimeMillis();

		try {
			String applicationBasePath = servletContext.getRealPath("/");
			
			ActivityContextBuilder builder = new XmlActivityContextBuilder(applicationBasePath, contextConfigLocation);
			activityContext = builder.build();
			
			long elapsedTime = System.currentTimeMillis() - startTime;
			logger.info("ActivityContext initialization completed in " + elapsedTime + " ms");
		} catch(RuntimeException ex) {
			logger.error("ActivityContext failed to initialize: " + ex);
			throw ex;
		} catch(Error err) {
			logger.error("ActivityContext failed to initialize: " + err);
			throw err;
		}
	}
	
	public ActivityContext getActivityContext() {
		WebApplicationAdapter applicationAdapter = WebApplicationAdapter.determineWebApplicationAdapter(servletContext);
		
		activityContext.setApplicationAdapter(applicationAdapter);
		
		return activityContext;
	}
	
	public static ActivityContext load(ServletContext servletContext, String contextConfigLocation) {
		ActivityContextLoader aspectranContextLoader = new ActivityContextLoader(servletContext, contextConfigLocation);
		ActivityContext activityContext = aspectranContextLoader.getActivityContext();
		
		return activityContext;
	}
	
}
