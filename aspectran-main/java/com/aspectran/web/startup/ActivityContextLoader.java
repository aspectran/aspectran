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
	
	private String applicationBasePath;
	
	private WebApplicationAdapter applicationAdapter;
	
	private String contextConfigLocation;
	
	private ClassLoader classLoader;
	
	private ActivityContext activityContext;
	
	public ActivityContextLoader(ServletContext servletContext, String contextConfigLocation) {
		this(servletContext, contextConfigLocation, null);
	}
	
	public ActivityContextLoader(ServletContext servletContext, String contextConfigLocation, ClassLoader classLoader) {
		this.applicationBasePath = servletContext.getRealPath("/");
		
		this.applicationAdapter = WebApplicationAdapter.determineWebApplicationAdapter(servletContext);

		if(contextConfigLocation == null)
			this.contextConfigLocation = DEFAULT_CONTEXT_CONFIG_LOCATION;
		else
			this.contextConfigLocation = contextConfigLocation;
		
		this.classLoader = classLoader;
		
	}

	protected ActivityContext build() {
		try {
			logger.info("building ActivityContext [" + contextConfigLocation + "]");
			long startTime = System.currentTimeMillis();

			ActivityContextBuilder builder = new XmlActivityContextBuilder(applicationBasePath, contextConfigLocation);
			
			if(classLoader != null)
				builder.setClassLoader(classLoader);
			
			activityContext = builder.build();
			
			activityContext.setApplicationAdapter(applicationAdapter);
			
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
	
	public ActivityContext load() {
		return build();
	}
	
	public ActivityContext getActivityContext() {
		return activityContext;
	}

	public String getApplicationBasePath() {
		return applicationBasePath;
	}
	
}
