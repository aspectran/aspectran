package com.aspectran.core.context.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.XmlActivityContextBuilder;

public class XmlActivityContextLoader extends AbstractActivityContextLoader {

	private final Logger logger = LoggerFactory.getLogger(XmlActivityContextLoader.class);
	
	public XmlActivityContextLoader() {
	}
	
	public ActivityContext load(String rootContext) {
		logger.info("build ActivityContext: ", rootContext);
		long startTime = System.currentTimeMillis();

		ActivityContextBuilder builder = new XmlActivityContextBuilder(applicationAdapter);
		builder.setHybridLoading(isHybridLoading());
		ActivityContext activityContext = builder.build(rootContext);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		logger.info("ActivityContext build completed in {} ms.", elapsedTime);
		
		return activityContext;
	}
	
}
