package com.aspectran.core.context.loader;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.XmlActivityContextBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

public class XmlActivityContextLoader extends AbstractActivityContextLoader {

	private final Log log = LogFactory.getLog(XmlActivityContextLoader.class);
	
	public XmlActivityContextLoader() {
	}
	
	public ActivityContext load(String rootContext) {
		log.info("build ActivityContext: " + rootContext);
		long startTime = System.currentTimeMillis();

		ActivityContextBuilder builder = new XmlActivityContextBuilder(applicationAdapter);
		builder.setHybridLoading(isHybridLoading());
		ActivityContext activityContext = builder.build(rootContext);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		log.info("ActivityContext build completed in " + elapsedTime + " ms.");
		
		return activityContext;
	}
	
}
