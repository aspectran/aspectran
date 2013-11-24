package com.aspectran.web.servlet;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.builder.AspectranContextBuilder;
import com.aspectran.core.context.builder.XmlAspectranContextBuilder;

public class AspectranContextLoader {

	private final Logger logger = LoggerFactory.getLogger(AspectranContextLoader.class);

	public static final String ASPECTRAN_CONTEXT_LOADER_ATTRIBUTE = AspectranContextLoader.class.getName();
	
	public static final String CONTEXT_CONFIG_LOCATION_PARAM = "contextConfigLocation";
	
	private static final String DEFAULT_CONTEXT_CONFIG_LOCATION = "WEB-INF/aspectran/aspectran.xml";
	
	private ServletContext servletContext;
	
	private AspectranContext aspectranContext;
	
	public AspectranContextLoader(ServletContext servletContext, String contextConfigLocation) {
		this.servletContext = servletContext;
		
		if(contextConfigLocation == null)
			contextConfigLocation = DEFAULT_CONTEXT_CONFIG_LOCATION;

		loadAspectranContext(contextConfigLocation);
	}

	private void loadAspectranContext(String contextConfigLocation) {
		//servletContext.log("Loading AspectranContext: " + contextConfigLocation);
		
		logger.info("loading AspectranContext [" + contextConfigLocation + "]");
		
		long startTime = System.currentTimeMillis();

		try {
			String applicationBasePath = servletContext.getRealPath("/");
			
			AspectranContextBuilder builder = new XmlAspectranContextBuilder(applicationBasePath, contextConfigLocation);
			aspectranContext = builder.build();
			
			long elapsedTime = System.currentTimeMillis() - startTime;
			logger.info("AspectranContext initialization completed in " + elapsedTime + " ms");
		} catch(RuntimeException ex) {
			logger.error("AspectranContext failed to initialize: " + ex);
			throw ex;
		} catch(Error err) {
			logger.error("AspectranContext failed to initialize: " + err);
			throw err;
		}
	}
	
	public AspectranContext getAspectranContext() {
		return aspectranContext;
	}
	
}
