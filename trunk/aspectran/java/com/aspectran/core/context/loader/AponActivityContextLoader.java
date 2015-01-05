package com.aspectran.core.context.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.XmlActivityContextBuilder;

public class AponActivityContextLoader extends AbstractActivityContextLoader {

	private final Logger logger = LoggerFactory.getLogger(AponActivityContextLoader.class);

	public AponActivityContextLoader() {
	}
	
	public ActivityContext load(String rootContext) {
		return buildAponActivityContext(rootContext);
	}

	protected ActivityContext buildAponActivityContext(String rootContext) {
		logger.info("build ActivityContext [" + rootContext + "]");
		long startTime = System.currentTimeMillis();

		ActivityContextBuilder builder = new XmlActivityContextBuilder(applicationAdapter, aspectranClassLoader);
		ActivityContext activityContext = builder.build(rootContext);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		logger.info("ActivityContext build completed in " + elapsedTime + " ms");
		
		return activityContext;
	}
	
}
