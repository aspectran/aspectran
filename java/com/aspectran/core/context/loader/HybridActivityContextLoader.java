package com.aspectran.core.context.loader;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

public class HybridActivityContextLoader extends AbstractActivityContextLoader {

	private final Log log = LogFactory.getLog(HybridActivityContextLoader.class);
	
	private static final String DEFAULT_ENCODING = "utf-8";
	
	private String encoding;

	public HybridActivityContextLoader() {
		this(null);
	}
	
	public HybridActivityContextLoader(String encoding) {
		this.encoding = (encoding == null) ? DEFAULT_ENCODING : encoding;
	}
	
	public ActivityContext load(String rootContext) {
		log.info("build ActivityContext: " + rootContext);
		long startTime = System.currentTimeMillis();

		ActivityContextBuilder builder = new HybridActivityContextBuilder(applicationAdapter, encoding);
		builder.setHybridLoading(isHybridLoading());
		ActivityContext activityContext = builder.build(rootContext);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		log.info("ActivityContext build completed in " + elapsedTime + " ms.");
		
		return activityContext;
	}
	
}
