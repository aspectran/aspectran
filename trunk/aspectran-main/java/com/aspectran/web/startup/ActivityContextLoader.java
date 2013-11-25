package com.aspectran.web.startup;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.XmlActivityContextBuilder;

public class ActivityContextLoader {

	private final Logger logger = LoggerFactory.getLogger(ActivityContextLoader.class);

	public static final String ASPECTRAN_CONTEXT_LOADER_ATTRIBUTE = ActivityContextLoader.class.getName();
	
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
		//servletContext.log("Loading ActivityContext: " + contextConfigLocation);
		
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
		return activityContext;
	}
	
}
