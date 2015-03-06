package com.aspectran.core.context.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;

public class HybridActivityContextLoader extends AbstractActivityContextLoader {

	private final Logger logger = LoggerFactory.getLogger(HybridActivityContextLoader.class);
	
	private static final String DEFAULT_ENCODING = "utf-8";
	
	private String encoding;

	public HybridActivityContextLoader() {
		this(null);
	}
	
	public HybridActivityContextLoader(String encoding) {
		this.encoding = (encoding == null) ? DEFAULT_ENCODING : encoding;
	}
	
	public ActivityContext load(String rootContext) {
		logger.info("build ActivityContext [" + rootContext + "]");
		long startTime = System.currentTimeMillis();

		ActivityContextBuilder builder = new HybridActivityContextBuilder(applicationAdapter, encoding);
		builder.setHybridLoading(isHybridLoading());
		ActivityContext activityContext = builder.build(rootContext);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		logger.info("ActivityContext build completed in " + elapsedTime + " ms");
		
		return activityContext;
	}
	
}
