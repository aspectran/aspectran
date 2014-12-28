package com.aspectran.core.context.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.XmlActivityContextBuilder;

public class ActivityContextLoader {

	private final Logger logger = LoggerFactory.getLogger(ActivityContextLoader.class);

	private final ApplicationAdapter applicationAdapter;
	
	private AspectranClassLoader aspectranClassLoader;
	
	private ActivityContext activityContext;

	public ActivityContextLoader(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}
	
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	public AspectranClassLoader getAspectranClassLoader() {
		return aspectranClassLoader;
	}

	public void setAspectranClassLoader(AspectranClassLoader aspectranClassLoader) {
		this.aspectranClassLoader = aspectranClassLoader;
	}

	protected ActivityContext buildXmlActivityContext(String rootContext) {
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
		return buildXmlActivityContext(rootContext);
	}
	
}
