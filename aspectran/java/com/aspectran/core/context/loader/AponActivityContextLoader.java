package com.aspectran.core.context.loader;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.AponActivityContextBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

public class AponActivityContextLoader extends AbstractActivityContextLoader {

	private final Log log = LogFactory.getLog(AponActivityContextLoader.class);
	
	private String encoding;

	public AponActivityContextLoader() {
	}
	
	public AponActivityContextLoader(String encoding) {
		this.encoding = encoding;
	}
	
	public ActivityContext load(String rootContext) {
		log.info("build ActivityContext: " + rootContext);
		long startTime = System.currentTimeMillis();

		ActivityContextBuilder builder = new AponActivityContextBuilder(applicationAdapter, encoding);
		ActivityContext activityContext = builder.build(rootContext);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		log.info("ActivityContext build completed in " + elapsedTime +" ms.");
		
		return activityContext;
	}
	
}
