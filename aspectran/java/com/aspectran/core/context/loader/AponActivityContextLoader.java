package com.aspectran.core.context.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.AponActivityContextBuilder;

public class AponActivityContextLoader extends AbstractActivityContextLoader {

	private final Logger logger = LoggerFactory.getLogger(AponActivityContextLoader.class);
	
	private String encoding;

	public AponActivityContextLoader() {
	}
	
	public AponActivityContextLoader(String encoding) {
		this.encoding = encoding;
	}
	
	public ActivityContext load(String rootContext) {
		logger.info("build ActivityContext [" + rootContext + "]");
		long startTime = System.currentTimeMillis();

		ActivityContextBuilder builder = new AponActivityContextBuilder(applicationAdapter, encoding);
		ActivityContext activityContext = builder.build(rootContext);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		logger.info("ActivityContext build completed in " + elapsedTime + " ms");
		
		return activityContext;
	}
	
}
