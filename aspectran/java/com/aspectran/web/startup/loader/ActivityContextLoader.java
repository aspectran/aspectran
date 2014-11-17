package com.aspectran.web.startup.loader;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.XmlActivityContextBuilder;
import com.aspectran.web.adapter.WebApplicationAdapter;

public class ActivityContextLoader {

	private final Logger logger = LoggerFactory.getLogger(ActivityContextLoader.class);

	public static final String ROOT_CONTEXT_PARAM = "contextConfigLocation";
	
	private static final String DEFAULT_ROOT_CONTEXT = "WEB-INF/aspectran.xml";
	
	private WebApplicationAdapter applicationAdapter;
	
	private String rootContext;
	
	private ClassLoader classLoader;
	
	private ActivityContext activityContext;
	
	public ActivityContextLoader(ServletContext servletContext, String rootContext) {
		this(servletContext, rootContext, null);
	}
	
	public ActivityContextLoader(ServletContext servletContext, String rootContext, ClassLoader classLoader) {
		this.applicationAdapter = WebApplicationAdapter.determineWebApplicationAdapter(servletContext);

		if(rootContext == null)
			this.rootContext = DEFAULT_ROOT_CONTEXT;
		else
			this.rootContext = rootContext;
		
		this.classLoader = classLoader;
		
	}

	protected ActivityContext build() {
		try {
			logger.info("build ActivityContext [" + rootContext + "]");
			long startTime = System.currentTimeMillis();

			ActivityContextBuilder builder = new XmlActivityContextBuilder(applicationAdapter, classLoader);
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
	
	public ActivityContext load() {
		return build();
	}
	
	public ActivityContext getActivityContext() {
		return activityContext;
	}

	public String getApplicationBasePath() {
		return applicationAdapter.getApplicationBasePath();
	}
	
}
